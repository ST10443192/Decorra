package com.example.decorra.data

import com.example.decorra.push.PushTokens
import com.example.decorra.util.UserRole
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class Account(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    /** Chat push notifications are on unless the person switched them off. */
    val pushMessages: Boolean = true
)

enum class SignInResult { SUCCESS, INVALID, WRONG_ROLE, NETWORK, FAILED }
enum class RegisterResult { SUCCESS, INVALID_CODE, EMAIL_TAKEN, NETWORK, FAILED }
enum class ActionResult { SUCCESS, WRONG_PASSWORD, NETWORK, FAILED }

/**
 * Accounts live in Firebase Authentication; each person's profile and role live in the
 * Firestore "users" collection. Callbacks run on the main thread.
 */
object AccountRepository {
    private val auth get() = FirebaseAuth.getInstance()
    private val db get() = FirebaseFirestore.getInstance()

    private var profile: Account? = null

    fun currentUid(): String? = auth.currentUser?.uid

    /** The signed-in person's profile, once it has been loaded. */
    fun currentAccount(): Account? = profile?.takeIf { it.id == auth.currentUser?.uid }

    /** Loads the profile of a person who is still signed in from an earlier session. */
    fun restoreSession(onDone: (Account?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onDone(null)
            return
        }
        loadProfile(user.uid) { account ->
            if (account != null) start(account)
            onDone(account)
        }
    }

    fun signIn(email: String, password: String, role: UserRole, onDone: (SignInResult) -> Unit) {
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null) {
                    onDone(SignInResult.FAILED)
                    return@addOnSuccessListener
                }
                loadProfile(uid) { account ->
                    when {
                        account == null -> {
                            auth.signOut()
                            onDone(SignInResult.FAILED)
                        }
                        account.role == role -> {
                            start(account)
                            onDone(SignInResult.SUCCESS)
                        }
                        else -> {
                            auth.signOut()
                            onDone(SignInResult.WRONG_ROLE)
                        }
                    }
                }
            }
            .addOnFailureListener { error ->
                onDone(
                    when (error) {
                        is FirebaseAuthInvalidUserException,
                        is FirebaseAuthInvalidCredentialsException -> SignInResult.INVALID
                        is FirebaseNetworkException -> SignInResult.NETWORK
                        else -> SignInResult.FAILED
                    }
                )
            }
    }

    /**
     * Clients register freely. A decorator also needs the access code that the owner keeps in
     * the Firebase console; the Firestore rules check it, so it cannot be skipped from the app.
     */
    fun register(
        first: String,
        last: String,
        email: String,
        phone: String,
        password: String,
        role: UserRole,
        accessCode: String,
        onDone: (RegisterResult) -> Unit
    ) {
        val cleanEmail = email.trim().lowercase()

        auth.createUserWithEmailAndPassword(cleanEmail, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    onDone(RegisterResult.FAILED)
                    return@addOnSuccessListener
                }
                val data = hashMapOf<String, Any>(
                    "firstName" to first.trim(),
                    "lastName" to last.trim(),
                    "email" to cleanEmail,
                    "phone" to phone.trim(),
                    "role" to if (role == UserRole.DECORATOR) "decorator" else "client",
                    "fcmTokens" to emptyList<String>(),
                    "createdAt" to FieldValue.serverTimestamp()
                )
                if (role == UserRole.DECORATOR) data["accessCode"] = accessCode.trim()

                db.collection("users").document(user.uid).set(data)
                    .addOnSuccessListener {
                        loadProfile(user.uid) { account ->
                            if (account == null) {
                                onDone(RegisterResult.FAILED)
                            } else {
                                start(account)
                                onDone(RegisterResult.SUCCESS)
                            }
                        }
                    }
                    .addOnFailureListener { error ->
                        // No profile was created, so do not leave a half-made account behind.
                        user.delete()
                        val wrongCode = role == UserRole.DECORATOR &&
                                error is FirebaseFirestoreException &&
                                error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
                        onDone(if (wrongCode) RegisterResult.INVALID_CODE else RegisterResult.FAILED)
                    }
            }
            .addOnFailureListener { error ->
                onDone(
                    when (error) {
                        is FirebaseAuthUserCollisionException -> RegisterResult.EMAIL_TAKEN
                        is FirebaseNetworkException -> RegisterResult.NETWORK
                        else -> RegisterResult.FAILED
                    }
                )
            }
    }

    /** Sends a reset link. Reports success even for unknown emails, so accounts cannot be probed. */
    fun resetPassword(email: String, onDone: (ActionResult) -> Unit) {
        auth.sendPasswordResetEmail(email.trim())
            .addOnSuccessListener { onDone(ActionResult.SUCCESS) }
            .addOnFailureListener { error ->
                onDone(
                    when (error) {
                        is FirebaseAuthInvalidUserException -> ActionResult.SUCCESS
                        is FirebaseNetworkException -> ActionResult.NETWORK
                        else -> ActionResult.FAILED
                    }
                )
            }
    }

    fun changePassword(currentPassword: String, newPassword: String, onDone: (ActionResult) -> Unit) {
        val user = auth.currentUser
        val email = user?.email
        if (user == null || email == null) {
            onDone(ActionResult.FAILED)
            return
        }
        user.reauthenticate(EmailAuthProvider.getCredential(email, currentPassword))
            .addOnSuccessListener {
                user.updatePassword(newPassword)
                    .addOnSuccessListener { onDone(ActionResult.SUCCESS) }
                    .addOnFailureListener { onDone(it.toActionResult()) }
            }
            .addOnFailureListener { error ->
                onDone(
                    when (error) {
                        is FirebaseAuthInvalidCredentialsException,
                        is FirebaseAuthInvalidUserException -> ActionResult.WRONG_PASSWORD
                        else -> error.toActionResult()
                    }
                )
            }
    }

    /** Applied to the screen at once; Firestore sends it as soon as the phone is online. */
    fun updateProfile(first: String, last: String, phone: String) {
        val current = currentAccount() ?: return
        val updated = current.copy(firstName = first.trim(), lastName = last.trim(), phone = phone.trim())
        profile = updated
        UserSession.set(updated)
        db.collection("users").document(current.id).update(
            mapOf(
                "firstName" to updated.firstName,
                "lastName" to updated.lastName,
                "phone" to updated.phone
            )
        )

        // A client's name also appears in the decorator's conversation list.
        if (current.role == UserRole.CLIENT) {
            db.collection("threads").document(current.id)
                .update("clientName", "${updated.firstName} ${updated.lastName}".trim())
        }
    }

    fun setPushMessages(enabled: Boolean) {
        val current = currentAccount() ?: return
        profile = current.copy(pushMessages = enabled)
        db.collection("users").document(current.id).update("pushMessages", enabled)
    }

    /** The decorators who can use this app. */
    fun observeTeam(): Flow<List<Account>> = callbackFlow {
        val registration = db.collection("users")
            .whereEqualTo("role", "decorator")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot.documents.mapNotNull { it.toAccount() })
            }
        awaitClose { registration.remove() }
    }

    fun signOut(onDone: () -> Unit) {
        PushTokens.remove {
            auth.signOut()
            profile = null
            UserSession.clear()
            onDone()
        }
    }

    private fun start(account: Account) {
        profile = account
        UserSession.set(account)
    }

    private fun loadProfile(uid: String, onDone: (Account?) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { onDone(it.toAccount()) }
            .addOnFailureListener { onDone(null) }
    }

    private fun Exception.toActionResult(): ActionResult =
        if (this is FirebaseNetworkException) ActionResult.NETWORK else ActionResult.FAILED

    private fun DocumentSnapshot.toAccount(): Account? {
        if (!exists()) return null
        return Account(
            id = id,
            firstName = getString("firstName").orEmpty(),
            lastName = getString("lastName").orEmpty(),
            email = getString("email").orEmpty(),
            phone = getString("phone").orEmpty(),
            role = if (getString("role") == "decorator") UserRole.DECORATOR else UserRole.CLIENT,
            pushMessages = getBoolean("pushMessages") ?: true
        )
    }
}
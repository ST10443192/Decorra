package com.example.decorra.push

import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

object PushChannels {
    const val MESSAGES = "messages"
}

/** Keeps this phone's push token on the signed-in person's profile so the server can reach it. */
object PushTokens {
    private val auth get() = FirebaseAuth.getInstance()
    private val db get() = FirebaseFirestore.getInstance()

    /** Call once the person is signed in. */
    fun register() {
        val uid = auth.currentUser?.uid ?: return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token -> add(uid, token) }
    }

    /** Called when Firebase issues a new token. */
    fun save(token: String) {
        val uid = auth.currentUser?.uid ?: return
        add(uid, token)
    }

    private fun add(uid: String, token: String) {
        db.collection("users").document(uid).update("fcmTokens", FieldValue.arrayUnion(token))
    }

    /**
     * Stops pushes for the person signing out. A queued write can hang when the phone is offline,
     * so this gives up waiting after three seconds and always retires the old token.
     */
    fun remove(onDone: () -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onDone()
            return
        }

        var finished = false
        val finish = {
            if (!finished) {
                finished = true
                onDone()
            }
        }
        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 3_000)

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                db.collection("users").document(uid)
                    .update("fcmTokens", FieldValue.arrayRemove(token))
                    .addOnCompleteListener { finish() }
                FirebaseMessaging.getInstance().deleteToken()
            }
            .addOnFailureListener { finish() }
    }
}
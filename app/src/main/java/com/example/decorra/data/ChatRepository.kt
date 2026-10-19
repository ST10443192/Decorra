package com.example.decorra.data

import com.example.decorra.push.PushNotify
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** One conversation per client. The thread id is the client's Firebase user id. */
data class ChatThread(
    val clientUid: String,
    val clientName: String,
    val clientEmail: String,
    val lastText: String,
    val lastTimeMillis: Long,
    val lastFromClient: Boolean
)

object ChatRepository {
    /** The conversation on screen right now, so its push notifications can be skipped. */
    @Volatile
    var openThreadId: String? = null

    private val db get() = FirebaseFirestore.getInstance()

    fun messages(threadId: String): Flow<List<ChatMessage>> = callbackFlow {
        val registration = db.collection("threads").document(threadId)
            .collection("messages")
            .orderBy("sentAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(
                    snapshot.documents.map { doc ->
                        ChatMessage(
                            id = doc.id,
                            text = doc.getString("text").orEmpty(),
                            fromClient = doc.getBoolean("fromClient") ?: true,
                            timeMillis = doc.millis("sentAt") ?: System.currentTimeMillis(),
                            fileId = doc.getString("fileId"),
                            fileName = doc.getString("fileName"),
                            fileMime = doc.getString("fileMime")
                        )
                    }
                )
            }
        awaitClose { registration.remove() }
    }

    /** Decorators only: every client conversation, newest first. */
    fun threads(): Flow<List<ChatThread>> = callbackFlow {
        val registration = db.collection("threads")
            .orderBy("lastTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(
                    snapshot.documents.map { doc ->
                        ChatThread(
                            clientUid = doc.id,
                            clientName = doc.getString("clientName").orEmpty(),
                            clientEmail = doc.getString("clientEmail").orEmpty(),
                            lastText = doc.getString("lastText").orEmpty(),
                            lastTimeMillis = doc.millis("lastTime") ?: 0L,
                            lastFromClient = doc.getBoolean("lastFromClient") ?: false
                        )
                    }
                )
            }
        awaitClose { registration.remove() }
    }

    /**
     * Queued while the phone is offline and sent when it reconnects.
     * Once the server has the message, the push sender is asked to notify the other side.
     */
    fun send(threadId: String, text: String, asClient: Boolean, attachment: Attachment? = null) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val thread = db.collection("threads").document(threadId)
        val message = thread.collection("messages").document()

        val summary = mutableMapOf<String, Any>(
            "lastText" to text,
            "lastFromClient" to asClient,
            "lastTime" to FieldValue.serverTimestamp()
        )
        if (asClient) {
            summary["clientUid"] = uid
            summary["clientName"] = UserSession.fullName
            summary["clientEmail"] = UserSession.email
        }

        val body = mutableMapOf<String, Any>(
            "text" to text,
            "fromClient" to asClient,
            "senderUid" to uid,
            "sentAt" to FieldValue.serverTimestamp()
        )
        if (attachment != null) {
            body["fileId"] = attachment.fileId
            body["fileName"] = attachment.name
            body["fileMime"] = attachment.mime
        }

        db.batch()
            .set(message, body)
            .set(thread, summary, SetOptions.merge())
            .commit()
            .addOnSuccessListener { PushNotify.chat(threadId) }
    }

    /** A message that has not reached the server yet has an estimated time. */
    private fun DocumentSnapshot.millis(field: String): Long? =
        getTimestamp(field, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.toDate()?.time
}
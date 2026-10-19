package com.example.decorra.push

import com.example.decorra.data.FileRepository
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/** Asks the Cloudflare Worker to send a push for the newest message. Best effort: chat works without it. */
object PushNotify {
    fun chat(threadId: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        user.getIdToken(false).addOnSuccessListener { result ->
            val token = result.token ?: return@addOnSuccessListener
            thread {
                try {
                    val bytes = JSONObject().put("threadId", threadId).toString().toByteArray()
                    val conn = (URL("${FileRepository.BASE_URL}/notify").openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        doOutput = true
                        connectTimeout = 15_000
                        readTimeout = 30_000
                        setRequestProperty("X-Auth-Token", token)
                        setRequestProperty("Content-Type", "application/json")
                        setFixedLengthStreamingMode(bytes.size)
                    }
                    try {
                        conn.outputStream.use { it.write(bytes) }
                        conn.responseCode
                    } finally {
                        conn.disconnect()
                    }
                } catch (_: Exception) {
                    // Push is best effort; the message itself is already saved.
                }
            }
        }
    }
}
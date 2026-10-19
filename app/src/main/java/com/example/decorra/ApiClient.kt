package com.example.decorra

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object ApiClient {


    const val BASE_URL = "https://scoundrel-creatable-among.ngrok-free.dev/revive-technologies/api"

    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    // Set this in your screens to return to login when the token expires (HTTP 401).
    var onUnauthorized: (() -> Unit)? = null

    fun request(
        path: String,
        method: String = "GET",
        body: JSONObject? = null,
        token: String? = null,
        onResult: (success: Boolean, json: JSONObject?, error: String?) -> Unit
    ) {
        val builder = Request.Builder()
            .url("$BASE_URL/$path")
            .addHeader("ngrok-skip-browser-warning", "1")
        if (token != null) builder.addHeader("Authorization", "Bearer $token")

        if (method == "POST") builder.post((body ?: JSONObject()).toString().toRequestBody(JSON))
        else builder.get()

        client.newCall(builder.build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult(false, null, "Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val text = response.body?.string() ?: ""
                try {
                    val json = if (text.trim().startsWith("["))
                        JSONObject().put("data", JSONArray(text))
                    else JSONObject(text)

                    if (response.isSuccessful) {
                        onResult(true, json, null)
                    } else {
                        if (response.code == 401) onUnauthorized?.invoke()
                        onResult(false, json, json.optString("error", "Request failed"))
                    }
                } catch (e: Exception) {
                    onResult(false, null, "Unexpected response from server")
                }
            }
        })
    }
}
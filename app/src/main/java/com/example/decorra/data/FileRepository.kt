package com.example.decorra.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.OpenableColumns
import android.util.LruCache
import android.widget.Toast
import androidx.core.content.FileProvider
import android.media.ExifInterface
import com.example.decorra.R
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max

data class Attachment(val fileId: String, val name: String, val mime: String, val size: Long)

object FileRepository {
    const val BASE_URL = "https://decorra-files.something.workers.dev"

    private const val MAX_BYTES = 10 * 1024 * 1024
    private const val MAX_SIDE = 1600

    val documentTypes = arrayOf("application/pdf")

    class TooLargeException : Exception()

    private val bitmaps = object : LruCache<String, Bitmap>(
        (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt()
    ) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount / 1024
    }

    suspend fun upload(context: Context, uri: Uri, threadId: String): Result<Attachment> =
        withContext(Dispatchers.IO) {
            runCatching {
                val isImage = context.contentResolver.getType(uri).orEmpty().startsWith("image/")
                var name = displayName(context, uri) ?: if (isImage) "photo.jpg" else "document.pdf"
                val bytes = if (isImage) {
                    name = name.substringBeforeLast('.') + ".jpg"
                    compressImage(context, uri)
                } else {
                    context.contentResolver.openInputStream(uri)!!.use { it.readLimited() }
                }
                if (bytes.size > MAX_BYTES) throw TooLargeException()

                val conn = (URL("$BASE_URL/upload").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    connectTimeout = 15_000
                    readTimeout = 60_000
                    setRequestProperty("X-Auth-Token", token())
                    setRequestProperty("Content-Type", "application/octet-stream")
                    setFixedLengthStreamingMode(bytes.size)
                }
                val json = try {
                    conn.outputStream.use { it.write(bytes) }
                    if (conn.responseCode != 200) error("upload failed: ${conn.responseCode}")
                    JSONObject(conn.inputStream.bufferedReader().readText())
                } finally {
                    conn.disconnect()
                }

                val attachment = Attachment(
                    json.getString("fileId"), name, json.getString("mime"), json.getLong("size")
                )
                withTimeout(15_000) {
                    FirebaseFirestore.getInstance().collection("files").document(attachment.fileId)
                        .set(
                            mapOf(
                                "ownerUid" to FirebaseAuth.getInstance().currentUser!!.uid,
                                "threadId" to threadId,
                                "name" to attachment.name,
                                "mime" to attachment.mime,
                                "size" to attachment.size,
                                "createdAt" to FieldValue.serverTimestamp()
                            )
                        ).await()
                }
                attachment
            }
        }

    /** Downloads once, then reuses the copy in the cache. */
    suspend fun download(context: Context, fileId: String, mime: String): File =
        withContext(Dispatchers.IO) {
            val dir = File(context.cacheDir, "files").apply { mkdirs() }
            val file = File(dir, "$fileId.${if (mime == "application/pdf") "pdf" else "jpg"}")
            if (file.exists()) return@withContext file

            val conn = (URL("$BASE_URL/download?id=$fileId").openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 60_000
                setRequestProperty("X-Auth-Token", token())
            }
            try {
                if (conn.responseCode != 200) error("download failed: ${conn.responseCode}")
                val part = File(dir, "$fileId.part")
                conn.inputStream.use { input -> part.outputStream().use { input.copyTo(it) } }
                part.renameTo(file)
            } finally {
                conn.disconnect()
            }
            file
        }

    suspend fun loadBitmap(context: Context, fileId: String, mime: String): Bitmap? {
        bitmaps.get(fileId)?.let { return it }
        val file = download(context, fileId, mime)
        val bitmap = withContext(Dispatchers.IO) { BitmapFactory.decodeFile(file.path) }
        if (bitmap != null) bitmaps.put(fileId, bitmap)
        return bitmap
    }

    fun open(context: Context, file: File, mime: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, mime)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, R.string.cannot_open_file, Toast.LENGTH_LONG).show()
        }
    }

    /** Call when someone signs out, so their files do not stay on the phone. */
    fun clearCache(context: Context) {
        bitmaps.evictAll()
        File(context.cacheDir, "files").deleteRecursively()
    }

    private suspend fun token(): String = suspendCancellableCoroutine { cont ->
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            cont.resumeWithException(IllegalStateException("signed out"))
            return@suspendCancellableCoroutine
        }
        user.getIdToken(false)
            .addOnSuccessListener { cont.resume(it.token.orEmpty()) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    private suspend fun Task<*>.await() = suspendCancellableCoroutine<Unit> { cont ->
        addOnSuccessListener { cont.resume(Unit) }
        addOnFailureListener { cont.resumeWithException(it) }
    }

    private fun displayName(context: Context, uri: Uri): String? =
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { if (it.moveToFirst()) it.getString(0) else null }

    private fun InputStream.readLimited(): ByteArray {
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            val n = read(buffer)
            if (n < 0) break
            out.write(buffer, 0, n)
            if (out.size() > MAX_BYTES) throw TooLargeException()
        }
        return out.toByteArray()
    }

    /** Shrinks to MAX_SIDE, fixes the rotation and saves as JPEG. */
    private fun compressImage(context: Context, uri: Uri): ByteArray {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)!!.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / sample > MAX_SIDE * 2) sample *= 2

        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = resolver.openInputStream(uri)!!.use { BitmapFactory.decodeStream(it, null, options) }
            ?: error("not an image")

        val degrees = resolver.openInputStream(uri)!!.use {
            when (ExifInterface(it).getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        }
        val scale = minOf(1f, MAX_SIDE.toFloat() / max(decoded.width, decoded.height))
        val matrix = Matrix().apply { postRotate(degrees); postScale(scale, scale) }
        val result = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)

        return ByteArrayOutputStream().also { result.compress(Bitmap.CompressFormat.JPEG, 80, it) }
            .toByteArray()
    }
}
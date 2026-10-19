package com.example.decorra.data

import android.content.Context
import java.io.File
import java.util.concurrent.Executors

/** Tiny file store: one JSON file per kind of data, written off the main thread. */
object Storage {
    private lateinit var dir: File
    private val executor = Executors.newSingleThreadExecutor()

    fun init(context: Context) {
        dir = context.filesDir
    }

    fun read(name: String): String? {
        val file = File(dir, name)
        return if (file.exists()) file.readText() else null
    }

    fun write(name: String, text: String) {
        executor.execute {
            val temp = File(dir, "$name.tmp")
            temp.writeText(text)
            temp.renameTo(File(dir, name))
        }
    }
}
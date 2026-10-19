package com.example.decorra.util

import android.content.Context
import com.example.decorra.R
import com.example.decorra.data.Shortage

/** One readable sentence for a toast. */
fun Context.shortageMessage(shortages: List<Shortage>): String {
    val list = shortages.joinToString("; ") {
        getString(R.string.shortage_line, it.itemName, it.requested, it.available)
    }
    return getString(R.string.shortage_message, list)
}
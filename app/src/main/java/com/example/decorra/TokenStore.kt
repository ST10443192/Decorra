package com.example.decorra

import android.content.Context

object TokenStore {
    private const val PREFS = "revive_prefs"
    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun save(ctx: Context, token: String, userId: Int, role: String) {
        prefs(ctx).edit()
            .putString("token", token)
            .putInt("user_id", userId)
            .putString("role", role)
            .apply()
    }

    fun token(ctx: Context): String? = prefs(ctx).getString("token", null)
    fun userId(ctx: Context): Int = prefs(ctx).getInt("user_id", -1)
    fun role(ctx: Context): String? = prefs(ctx).getString("role", null)
    fun clear(ctx: Context) = prefs(ctx).edit().clear().apply()
}
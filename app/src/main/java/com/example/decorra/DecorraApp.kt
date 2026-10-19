package com.example.decorra

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.decorra.data.BusinessRepository
import com.example.decorra.data.InspirationRepository
import com.example.decorra.data.ItemRepository
import com.example.decorra.data.NotificationPreferences
import com.example.decorra.data.NotificationRepository
import com.example.decorra.data.QuoteRepository
import com.example.decorra.data.Storage
import com.example.decorra.data.ThemeRepository
import com.example.decorra.push.PushChannels

class DecorraApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Storage.init(this)
        ThemeRepository.load()
        ItemRepository.load()
        QuoteRepository.load()
        NotificationRepository.load()
        BusinessRepository.load()
        InspirationRepository.load()
        NotificationPreferences.load()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            PushChannels.MESSAGES,
            getString(R.string.channel_messages),
            NotificationManager.IMPORTANCE_HIGH
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
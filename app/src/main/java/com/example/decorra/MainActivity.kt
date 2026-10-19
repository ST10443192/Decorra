package com.example.decorra

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.decorra.data.AccountRepository
import com.example.decorra.data.FileRepository
import com.example.decorra.navigation.AppNavHost
import com.example.decorra.push.PushTokens
import com.example.decorra.ui.theme.DecorraTheme
import com.example.decorra.util.restartAtSplash

class MainActivity : AppCompatActivity() {

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The profile is loaded by the splash screen. If this activity was restored without it
        // (for example after Android ended the process), go back through the splash screen.
        val account = AccountRepository.currentAccount()
        if (account == null) {
            restartAtSplash()
            finish()
            return
        }

        PushTokens.register()
        askForNotificationPermission()

        setContent {
            DecorraTheme {
                AppNavHost(
                    role = account.role,
                    onLogOut = {
                        FileRepository.clearCache(this)
                        AccountRepository.signOut { restartAtSplash() }
                    }
                )
            }
        }
    }

    private fun askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
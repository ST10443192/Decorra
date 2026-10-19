package com.example.decorra.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.decorra.data.AccountRepository
import com.example.decorra.databinding.ActivitySplashBinding
import com.example.decorra.util.openApp

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Someone is still signed in from last time: load their profile and go straight in.
        if (AccountRepository.currentUid() != null) {
            binding.btnLogin.isEnabled = false
            binding.btnRegister.isEnabled = false
            AccountRepository.restoreSession { account ->
                if (isFinishing || isDestroyed) return@restoreSession
                if (account != null) {
                    openApp(account.role)
                    finish()
                } else {
                    binding.btnLogin.isEnabled = true
                    binding.btnRegister.isEnabled = true
                }
            }
        }
    }
}
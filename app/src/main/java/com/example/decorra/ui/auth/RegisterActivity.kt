package com.example.decorra.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.decorra.R
import com.example.decorra.data.AccountRepository
import com.example.decorra.data.RegisterResult
import com.example.decorra.databinding.ActivityRegisterBinding
import com.example.decorra.util.UserRole
import com.example.decorra.util.clearErrorOnEdit
import com.example.decorra.util.openApp
import com.example.decorra.util.openAuth
import com.example.decorra.util.requireEmail
import com.example.decorra.util.requireMatch
import com.example.decorra.util.requireNewPassword
import com.example.decorra.util.requirePhone
import com.example.decorra.util.requireText
import com.example.decorra.util.text

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    private fun currentRole() =
        if (binding.roleToggle.checkedButtonId == R.id.btnDecorator) UserRole.DECORATOR
        else UserRole.CLIENT

    private fun updateCodeField() {
        val isDecorator = currentRole() == UserRole.DECORATOR
        binding.inviteLayout.visibility = if (isDecorator) View.VISIBLE else View.GONE
        if (!isDecorator) binding.inviteLayout.error = null
    }

    private fun setBusy(busy: Boolean) {
        binding.btnCreate.isEnabled = !busy
        binding.btnSwitch.isEnabled = !busy
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null && UserRole.fromIntent(intent) == UserRole.DECORATOR) {
            binding.roleToggle.check(R.id.btnDecorator)
        }
        updateCodeField()
        binding.roleToggle.addOnButtonCheckedListener { _, _, isChecked ->
            if (isChecked) updateCodeField()
        }

        listOf(
            binding.firstLayout, binding.lastLayout, binding.emailLayout, binding.phoneLayout,
            binding.passwordLayout, binding.confirmLayout, binding.inviteLayout
        ).forEach { it.clearErrorOnEdit() }

        binding.btnCreate.setOnClickListener {
            val role = currentRole()

            val valid = listOf(
                binding.firstLayout.requireText(),
                binding.lastLayout.requireText(),
                binding.emailLayout.requireEmail(),
                binding.phoneLayout.requirePhone(),
                binding.passwordLayout.requireNewPassword(),
                binding.confirmLayout.requireMatch(binding.passwordLayout)
            ).all { it }
            val codeValid = role != UserRole.DECORATOR || binding.inviteLayout.requireText()
            if (!valid || !codeValid) return@setOnClickListener

            setBusy(true)
            AccountRepository.register(
                first = binding.firstLayout.text(),
                last = binding.lastLayout.text(),
                email = binding.emailLayout.text(),
                phone = binding.phoneLayout.text(),
                password = binding.passwordLayout.text(),
                role = role,
                accessCode = binding.inviteLayout.text()
            ) { result ->
                if (isFinishing || isDestroyed) return@register
                setBusy(false)
                when (result) {
                    RegisterResult.SUCCESS -> openApp(role)
                    RegisterResult.INVALID_CODE ->
                        binding.inviteLayout.error = getString(R.string.error_invite_invalid)
                    RegisterResult.EMAIL_TAKEN ->
                        binding.emailLayout.error = getString(R.string.error_email_taken)
                    RegisterResult.NETWORK ->
                        Toast.makeText(this, R.string.error_network, Toast.LENGTH_LONG).show()
                    RegisterResult.FAILED ->
                        Toast.makeText(this, R.string.error_generic, Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.btnSwitch.setOnClickListener {
            openAuth(LoginActivity::class.java, currentRole())
            finish()
        }
    }
}
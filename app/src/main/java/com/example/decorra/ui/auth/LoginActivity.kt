package com.example.decorra.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.decorra.R
import com.example.decorra.data.AccountRepository
import com.example.decorra.data.ActionResult
import com.example.decorra.data.SignInResult
import com.example.decorra.databinding.ActivityLoginBinding
import com.example.decorra.databinding.DialogResetPasswordBinding
import com.example.decorra.util.UserRole
import com.example.decorra.util.clearErrorOnEdit
import com.example.decorra.util.openApp
import com.example.decorra.util.openAuth
import com.example.decorra.util.requireEmail
import com.example.decorra.util.requireText
import com.example.decorra.util.text
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private fun currentRole() =
        if (binding.roleToggle.checkedButtonId == R.id.btnDecorator) UserRole.DECORATOR
        else UserRole.CLIENT

    private fun setBusy(busy: Boolean) {
        binding.btnSignIn.isEnabled = !busy
        binding.btnSwitch.isEnabled = !busy
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)



        if (savedInstanceState == null && UserRole.fromIntent(intent) == UserRole.DECORATOR) {
            binding.roleToggle.check(R.id.btnDecorator)
        }

        binding.emailLayout.clearErrorOnEdit()
        binding.passwordLayout.clearErrorOnEdit()

        binding.btnSignIn.setOnClickListener {
            val valid = listOf(
                binding.emailLayout.requireEmail(),
                binding.passwordLayout.requireText()
            ).all { it }
            if (!valid) return@setOnClickListener

            val role = currentRole()
            setBusy(true)
            AccountRepository.signIn(
                email = binding.emailLayout.text(),
                password = binding.passwordLayout.text(),
                role = role
            ) { result ->
                if (isFinishing || isDestroyed) return@signIn
                setBusy(false)
                when (result) {
                    SignInResult.SUCCESS -> openApp(role)
                    SignInResult.INVALID ->
                        binding.passwordLayout.error = getString(R.string.error_login_invalid)
                    SignInResult.WRONG_ROLE ->
                        binding.passwordLayout.error = getString(R.string.error_role_mismatch)
                    SignInResult.NETWORK ->
                        Toast.makeText(this, R.string.error_network, Toast.LENGTH_LONG).show()
                    SignInResult.FAILED ->
                        Toast.makeText(this, R.string.error_generic, Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.btnForgot.setOnClickListener { showResetDialog() }

        binding.btnSwitch.setOnClickListener {
            openAuth(RegisterActivity::class.java, currentRole())
            finish()
        }
    }

    private fun showResetDialog() {
        val dialogBinding = DialogResetPasswordBinding.inflate(layoutInflater)
        // Reset is now an emailed link, so only the email field is needed.
        dialogBinding.passwordLayout.visibility = View.GONE
        dialogBinding.confirmLayout.visibility = View.GONE
        dialogBinding.emailLayout.editText?.setText(binding.emailLayout.text())
        dialogBinding.emailLayout.clearErrorOnEdit()

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.reset_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.reset_action, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

        // The click listener is set after show() so a failed check keeps the dialog open.
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (!dialogBinding.emailLayout.requireEmail()) return@setOnClickListener

                AccountRepository.resetPassword(dialogBinding.emailLayout.text()) { result ->
                    when (result) {
                        ActionResult.SUCCESS -> {
                            Toast.makeText(this, R.string.reset_success, Toast.LENGTH_LONG).show()
                            dialog.dismiss()
                        }
                        ActionResult.NETWORK ->
                            Toast.makeText(this, R.string.error_network, Toast.LENGTH_LONG).show()
                        else ->
                            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        dialog.show()
    }
}
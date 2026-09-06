package com.musclesOS.adil.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.musclesOS.adil.R
import com.musclesOS.adil.databinding.ActivityRegisterScreenBinding
import com.musclesOS.adil.model.AuthState
import com.musclesOS.adil.repository.AuthRepository
import com.musclesOS.adil.ui.auth.viewmodel.AuthViewModel
import com.musclesOS.adil.utils.Validator
import com.musclesOS.adil.utils.bindPasswordToggle
import com.musclesOS.adil.utils.setLoadingState
import kotlinx.coroutines.launch

/**
 * Handles new user registration.
 * Provides form validation for email and password, and manages the sign-up process via AuthViewModel.
 */
class RegisterScreen : AppCompatActivity() {

    lateinit var binding: ActivityRegisterScreenBinding

    private val viewModel: AuthViewModel by lazy {
        AuthViewModel(AuthRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFieldFocusStates()
        binding.ivPasswordToggle.bindPasswordToggle(binding.etPassword)
        binding.ivConfirmPasswordToggle.bindPasswordToggle(binding.etConfirmPassword)

        binding.btnCreateAccount.setOnClickListener {
            registerUser()
        }

        binding.tvSignIn.setOnClickListener {
            startActivity(
                Intent(
                    this@RegisterScreen,
                    LoginActivity::class.java
                )
            )
            finish()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { state ->
                    when (state) {
                        AuthState.Idle -> {}
                        AuthState.Loading -> showLoading(true)
                        is AuthState.RegistrationSuccess -> {
                            showLoading(false)
                            Toast.makeText(
                                this@RegisterScreen,
                                "Email verification link sent",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(
                                Intent(
                                    this@RegisterScreen,
                                    VerifyEmailActivity::class.java
                                )
                            )
                            finish()
                        }
                        is AuthState.Error -> {
                            showLoading(false)
                            Toast.makeText(
                                this@RegisterScreen,
                                state.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun setupFieldFocusStates() {
        val focusedElevation = 4f * resources.displayMetrics.density

        fun updateFieldFocus() {
            val nameFocused = binding.etName.hasFocus()
            val emailFocused = binding.etEmail.hasFocus()
            val passwordFocused = binding.etPassword.hasFocus()
            val confirmPasswordFocused = binding.etConfirmPassword.hasFocus()

            binding.nameField.setBackgroundResource(
                if (nameFocused) R.drawable.bg_figma_field_focused
                else R.drawable.bg_figma_field
            )
            binding.emailField.setBackgroundResource(
                if (emailFocused) R.drawable.bg_figma_field_focused
                else R.drawable.bg_figma_field
            )
            binding.passwordField.setBackgroundResource(
                if (passwordFocused) R.drawable.bg_figma_field_focused
                else R.drawable.bg_figma_field
            )
            binding.confirmPasswordField.setBackgroundResource(
                if (confirmPasswordFocused) R.drawable.bg_figma_field_focused
                else R.drawable.bg_figma_field
            )

            binding.nameField.elevation = if (nameFocused) focusedElevation else 0f
            binding.emailField.elevation = if (emailFocused) focusedElevation else 0f
            binding.passwordField.elevation = if (passwordFocused) focusedElevation else 0f
            binding.confirmPasswordField.elevation = if (confirmPasswordFocused) focusedElevation else 0f
        }

        binding.nameField.setOnClickListener { binding.etName.requestFocus() }
        binding.emailField.setOnClickListener { binding.etEmail.requestFocus() }
        binding.passwordField.setOnClickListener { binding.etPassword.requestFocus() }
        binding.confirmPasswordField.setOnClickListener { binding.etConfirmPassword.requestFocus() }
        binding.etName.setOnFocusChangeListener { _, _ -> updateFieldFocus() }
        binding.etEmail.setOnFocusChangeListener { _, _ -> updateFieldFocus() }
        binding.etPassword.setOnFocusChangeListener { _, _ -> updateFieldFocus() }
        binding.etConfirmPassword.setOnFocusChangeListener { _, _ -> updateFieldFocus() }

        updateFieldFocus()
    }

    private fun togglePasswordVisibility(editText: EditText) {
        val cursorPosition = editText.selectionStart
        val isPasswordVisible = editText.transformationMethod !is PasswordTransformationMethod

        editText.transformationMethod =
            if (isPasswordVisible) PasswordTransformationMethod.getInstance()
            else HideReturnsTransformationMethod.getInstance()

        editText.setSelection(cursorPosition.coerceAtLeast(0))
        editText.requestFocus()
    }

    private fun showLoading(show: Boolean) {
        setLoadingState(
            show,
            binding.loadingScrim,
            binding.loadingAnimation,
            binding.btnCreateAccount
        )
    }

    private fun registerUser() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        Validator.validateName(name)?.let {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            return
        }

        Validator.validateEmail(email)?.let {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            return
        }

        Validator.validatePassword(password)?.let {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.register(name, email, password)
    }
}

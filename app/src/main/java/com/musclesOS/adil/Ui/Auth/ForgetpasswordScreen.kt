package com.musclesOS.adil.Ui.Auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.musclesOS.adil.R
import com.musclesOS.adil.databinding.ActivityForgetpasswordScreenBinding
import com.musclesOS.adil.model.AuthState
import com.musclesOS.adil.Utils.Validator
import com.musclesOS.adil.Utils.requireInternet
import com.musclesOS.adil.repository.AuthRepository
import com.musclesOS.adil.Ui.Auth.viewmodel.AuthViewModel
import com.musclesOS.adil.Utils.setLoadingState
import kotlinx.coroutines.launch

/**
 * Allows users to request a password reset email.
 * Validates the email address and interacts with the AuthViewModel to send the reset link.
 */
class ForgetpasswordScreen : AppCompatActivity() {
    private val repository by lazy {
        AuthRepository()
    }
    private val viewModel by lazy{
        AuthViewModel(repository)
    }
    private lateinit var binding: ActivityForgetpasswordScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityForgetpasswordScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observeAuthState()
        binding.btnResend.setOnClickListener {
            requireInternet {
                lifecycleScope.launch {
                    sendResetLink()
                }

            }

        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }
    /**
     * Updates the UI to show or hide the loading state, dimming the background.
     */
    private fun showLoading(
        show: Boolean
    ) {

        setLoadingState(
            show,
            binding.loadingScrim,
            binding.loadingAnimation,
            binding.btnResend

        )

    }
    /**
     * Validates input and triggers the password reset email process.
     */
    private fun sendResetLink() {

        val email = binding.etEmail.text.toString().trim()

        Validator.validateEmail(email)?.let {

            binding.etEmail.error = it

            return

        }

        viewModel.forgetpassword(email)
    }
    /**
     * Observes the AuthViewModel state to handle loading, success, and error states.
     */
    private fun observeAuthState() {

        lifecycleScope.launch {

            repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                viewModel.authState.collect { state ->

                    when(state) {

                        AuthState.Loading -> {

                            showLoading(true)

                        }

                        AuthState.PasswordResetSent -> {

                            showLoading(false)

                            Toast.makeText(
                                this@ForgetpasswordScreen,
                                "Password reset link sent.",
                                Toast.LENGTH_SHORT
                            ).show()

                            finish()

                        }

                        is AuthState.Error -> {

                            showLoading(false)

                            Toast.makeText(
                                this@ForgetpasswordScreen,
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
}
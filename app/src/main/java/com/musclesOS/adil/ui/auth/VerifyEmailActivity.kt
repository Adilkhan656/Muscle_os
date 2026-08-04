package com.musclesOS.adil.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.musclesOS.adil.MainActivity
import com.musclesOS.adil.databinding.ActivityVerifyEmailBinding
import com.musclesOS.adil.model.AuthState
import com.musclesOS.adil.repository.AuthRepository
import com.musclesOS.adil.ui.auth.viewmodel.AuthViewModel
import com.musclesOS.adil.utils.setLoadingState
import kotlinx.coroutines.launch

/**
 * Activity that handles the email verification state after registration.
 * Allows users to check verification status or resend the verification email.
 */
class VerifyEmailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerifyEmailBinding
    private val repository by lazy {
        AuthRepository()
    }
    private val viewModel by lazy {
        AuthViewModel(repository)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVerifyEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observerAuthstate()

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    signOutAndOpenLogin()
                }
            }
        )

        binding.btnVerified.setOnClickListener {
            viewModel.checkVerification()
        }

        binding.btnResend.setOnClickListener {
            viewModel.resendVerificationEmail()
        }

    }

    /**
     * Navigates to the main activity after successful verification.
     */
    private fun openmain(){
        startActivity(
            Intent(
                this@VerifyEmailActivity, MainActivity::class.java
            )
        )
        finish()
    }

    /**
     * Signs out the user and returns to the Login screen.
     */
    private fun signOutAndOpenLogin() {

        repository.signOut()

        startActivity(
            Intent(
                this@VerifyEmailActivity,
                LoginActivity::class.java
            )
        )

        finish()

    }

    /**
     * Updates the UI to show or hide the loading state, dimming the background.
     */
    private fun showLoading(show: Boolean) {
        setLoadingState(
            show,
            binding.loadingScrim,
            binding.loadingAnimation,
            binding.btnVerified,
            binding.btnResend
        )
    }

    /**
     * Observes the AuthViewModel state to handle verification success, loading, and errors.
     */
    private fun observerAuthstate(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.authState.collect { state ->
                    when(state){

                        AuthState.EmailVerified -> {
                            showLoading(false)
                        openmain()

                        }
                        AuthState.Loading -> {

                            showLoading(true)

                        }

                        AuthState.VerificationEmailSent -> {

                            showLoading(false)
                            Toast.makeText(
                                this@VerifyEmailActivity,
                                "Email verification link sent",
                                Toast.LENGTH_SHORT
                            ).show()

                        }

                        is AuthState.Error -> {
                            showLoading(false)
                            Toast.makeText(
                                this@VerifyEmailActivity,
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

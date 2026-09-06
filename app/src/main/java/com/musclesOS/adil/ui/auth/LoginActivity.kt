package com.musclesOS.adil.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.musclesOS.adil.MainActivity
import com.musclesOS.adil.R
import com.musclesOS.adil.core.AppDestination
import com.musclesOS.adil.core.AppInitializer
import com.musclesOS.adil.databinding.ActivityLoginBinding
import com.musclesOS.adil.model.AuthState
import com.musclesOS.adil.model.FacebookAuthManager
import com.musclesOS.adil.utils.Validator
import com.musclesOS.adil.utils.bindPasswordToggle
import com.musclesOS.adil.utils.requireInternet
import com.musclesOS.adil.repository.AuthRepository
import com.musclesOS.adil.ui.auth.viewmodel.AuthViewModel
import com.musclesOS.adil.ui.onboarding.activity.OnboardingActivity
import com.musclesOS.adil.utils.setLoadingState
import kotlinx.coroutines.launch

/**
 * Handles user authentication via Email, Google, Facebook, or Guest access.
 * Manages form validation, focus states, and interaction with AuthViewModel.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var credentialManager: CredentialManager
    private lateinit var binding: ActivityLoginBinding

    private lateinit var facebookAuthManager: FacebookAuthManager

    private val repository by lazy {
        AuthRepository()
    }

    private val viewModel by lazy {
        AuthViewModel(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.ivPasswordToggle.apply {
            isEnabled = false
            alpha = 0.4f
        }

        enableEdgeToEdge()

        credentialManager = CredentialManager.create(this)
        facebookAuthManager = FacebookAuthManager(this)

        binding.ivPasswordToggle.bindPasswordToggle(binding.etPassword)
        setupFieldFocusStates()
        observeAuthState()
binding.tvForgotPassword.setOnClickListener {
    startActivity(
        Intent(
            this@LoginActivity,
            ForgetpasswordScreen::class.java
        )
    )
}
        binding.registerscreen.setOnClickListener {
            startActivity(
                Intent(
                    this@LoginActivity, RegisterScreen::class.java
                )
            )
        }
        binding.btnGoogleSignIn.setOnClickListener {
requireInternet {
    lifecycleScope.launch {
        startGoogleSignIn()
    }
}


        }
        binding.btnFacebookLogin.setOnClickListener {

            requireInternet {
                facebookAuthManager.login(

                    onSuccess = { token ->

                        viewModel.facebookLogin(token)

                    },

                    onError = {

                        Toast.makeText(
                            this,
                            it.message,
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                )
            }


        }
        binding.btnGuestLogin.setOnClickListener {
            requireInternet {
                lifecycleScope.launch {

                    viewModel.guestLogin()
                }
            }


        }

        binding.btnEmailLogin.setOnClickListener {

           requireInternet {
               lifecycleScope.launch {
                   loginWithEmail()
               }
           }

        }

    }


    /**
     * configures focus listeners for input fields to update their background and elevation.
     */
    private fun setupFieldFocusStates() {

        val focusedElevation =
            4f * resources.displayMetrics.density

        fun updateFieldFocus() {

            val emailFocused =
                binding.etEmail.hasFocus()

            val passwordFocused =
                binding.etPassword.hasFocus()

            binding.emailField.setBackgroundResource(
                if (emailFocused) R.drawable.bg_figma_field_focused
                else R.drawable.bg_figma_field
            )

            binding.passwordField.setBackgroundResource(
                if (passwordFocused) R.drawable.bg_figma_field_focused
                else R.drawable.bg_figma_field
            )

            binding.emailField.elevation =
                if (emailFocused) focusedElevation
                else 0f

            binding.passwordField.elevation =
                if (passwordFocused) focusedElevation
                else 0f

        }

        binding.emailField.setOnClickListener {
            binding.etEmail.requestFocus()
        }

        binding.passwordField.setOnClickListener {
            binding.etPassword.requestFocus()
        }

        binding.etEmail.setOnFocusChangeListener { _, _ ->
            updateFieldFocus()
        }

        binding.etPassword.setOnFocusChangeListener { _, _ ->
            updateFieldFocus()
        }

        updateFieldFocus()

    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        facebookAuthManager
            .getCallbackManager()
            .onActivityResult(
                requestCode,
                resultCode,
                data
            )

    }

    /**
     * Observes the AuthViewModel's state and updates the UI (loading, error, success).
     */
    private fun observeAuthState() {

        lifecycleScope.launch {

            repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.authState.collect { state ->

                    when (state) {

                        AuthState.Idle -> {}

                        AuthState.Loading -> {

                            showLoading(true)

                        }

                        is AuthState.GoogleLoginSuccess,

                        is AuthState.GuestLoginSuccess,

                        is AuthState.SignInWithFacebook,

                        AuthState.LoginSuccess -> {

                            showLoading(false)

                            openMain()

                        }

                        is AuthState.Error -> {

                            showLoading(false)

                            Toast.makeText(
                                this@LoginActivity,
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

    /**
     * Triggers the login process using the email and password provided in the form.
     */
    private fun loginWithEmail() {

        val email =
            binding.etEmail.text.toString().trim()

        val password =
            binding.etPassword.text.toString()

        Validator.validateEmail(email)?.let {

            binding.etEmail.error = it
            return

        }

        Validator.validatePassword(password)?.let {

            binding.etPassword.error = it
            return

        }

        viewModel.login(
            email,
            password
        )

    }

    /**
     * Launches the Google ID Credential Manager flow for one-tap sign-in.
     */
    private suspend fun startGoogleSignIn() {

        val googleIdOption =
            GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(true)
                .setServerClientId(
                    getString(R.string.web_client_id)
                )
                .build()

        val passwordOption =
            GetPasswordOption()

        val request =
            GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .addCredentialOption(passwordOption)
                .build()

        try {

            val result =
                credentialManager.getCredential(
                    context = this,
                    request = request
                )

            handleGoogleResult(result)

        } catch (e: NoCredentialException) {

            Toast.makeText(
                this,
                "No Google account found.",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: GetCredentialException) {

            Toast.makeText(
                this,
                e.message,
                Toast.LENGTH_SHORT
            ).show()

        }

    }

    /**
     * Processes the result from the Credential Manager and notifies the ViewModel.
     */
    private fun handleGoogleResult(
        result: GetCredentialResponse
    ) {

        val credential =
            result.credential

        when {

            credential is CustomCredential &&
                    credential.type ==
                    GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {

                val googleCredential =
                    GoogleIdTokenCredential.createFrom(
                        credential.data
                    )

                viewModel.signIn(
                    googleCredential.idToken
                )

            }

            credential is PasswordCredential -> {

                viewModel.login(
                    credential.id,
                    credential.password
                )

            }

            else -> {

                Toast.makeText(
                    this,
                    "Unsupported credential.",
                    Toast.LENGTH_SHORT
                ).show()

            }

        }

    }

    /**
     * Updates the UI to show or hide the loading state, dimming the background.
     */
    private fun showLoading(show: Boolean) {
        setLoadingState(
            show,
            binding.loadingScrim,
            binding.loadingAnimation,
            binding.btnEmailLogin,
            binding.btnGoogleSignIn,
            binding.btnFacebookLogin,
            binding.btnGuestLogin
        )
    }


    private fun openMain() {
        lifecycleScope.launch {
            val destination = AppInitializer(this@LoginActivity).initialize()
            val intent = when (destination) {
                AppDestination.Main -> Intent(this@LoginActivity, MainActivity::class.java)
                AppDestination.Onboarding -> Intent(this@LoginActivity, OnboardingActivity::class.java)
                else -> Intent(this@LoginActivity, OnboardingActivity::class.java) // Fallback
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onStart() {

        super.onStart()

        if (viewModel.isLoggedIn()) {

            openMain()

        }

    }

    override fun onResume() {
        super.onResume()
        com.musclesOS.adil.OneSignalManager.promptNotificationPermissionIfDaily(this)
    }

}
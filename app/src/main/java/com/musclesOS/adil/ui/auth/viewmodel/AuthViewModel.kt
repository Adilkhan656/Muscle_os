package com.musclesOS.adil.ui.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musclesOS.adil.model.AuthState
import com.musclesOS.adil.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for handling authentication logic and state across the app.
 * Communicates with the AuthRepository and exposes AuthState to the UI.
 */
class AuthViewModel(private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)

    val authState = _authState.asStateFlow()
    /**
     * Triggers Google Sign-In using the provided ID Token.
     */
    fun signIn(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.signInWithGoogle(idToken).onSuccess {
                _authState.value =
                    AuthState.GoogleLoginSuccess(it.uid)

            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Unknown error")
            }
        }
    }
    /**
     * Checks if a user is currently logged in.
     */
    fun isLoggedIn(): Boolean {
        return repository.currentUser() != null
    }
    /**
     * Reloads the user and checks if their email has been verified.
     */
    fun checkVerification() {

        viewModelScope.launch {

            _authState.value = AuthState.Loading

            repository.reloadUser()

            when {

                repository.currentUser() == null -> {

                    _authState.value =
                        AuthState.Error("User not found.")

                }

                repository.isEmailVerified() -> {

                    _authState.value =
                        AuthState.EmailVerified

                }

                else -> {

                    _authState.value =
                        AuthState.Error(
                            "Please verify your email."
                        )

                }

            }

        }

    }
    /**
     * Requests Firebase to resend the verification email to the current user.
     */
    fun resendVerificationEmail(){

        viewModelScope.launch {

            _authState.value = AuthState.Loading

            repository
                .sendVerificationEmail()
                .onSuccess {

                    _authState.value =
                        AuthState.VerificationEmailSent

                }
                .onFailure {

                    _authState.value =
                        AuthState.Error(
                            it.message ?: "Verification email failed"
                        )

                }

        }

    }
    /**
     * Triggers Facebook Sign-In using the provided Access Token.
     */
    fun facebookLogin(
        accessToken: String
    ) {

        viewModelScope.launch {

            _authState.value =
                AuthState.Loading

            repository
                .signInWithFacebook(accessToken)
                .onSuccess {

                    _authState.value =
                        AuthState.SignInWithFacebook(it.uid)

                }
                .onFailure {

                    _authState.value =
                        AuthState.Error(
                            it.message ?: "Facebook Login Failed"
                        )

                }

        }

    }
    /**
     * Registers a new user with email and password and sends a verification email.
     */
    fun register(name: String, email: String, password: String){
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.registerWithEmail(email, password, name).onSuccess {
                repository.sendVerificationEmail()
                    .onSuccess {

                        _authState.value =
                            AuthState.RegistrationSuccess

                    }
                    .onFailure {

                        _authState.value =
                            AuthState.Error(
                                it.message ?: "Verification failed"
                            )

                    }
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Unknown error")
            }
        }
    }
    /**
     * Logs in a user with email and password.
     */
    fun login(email: String,password: String){
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.loginWithEmail(email,password).onSuccess {
                _authState.value =
                    AuthState.LoginSuccess
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Unknown error")
            }
        }
    }

    /**
     * Sends a password reset email to the specified user.
     */
    fun forgetpassword(email: String){
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.sendPasswordResetEmail(email).onSuccess {
                _authState.value =
                    AuthState.PasswordResetSent
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Unknown error")
            }
        }
    }
    /**
     * Signs in the user as an anonymous guest.
     */
    fun guestLogin() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.signInAnonymously()
                .onSuccess {
                    _authState.value =
                        AuthState.GuestLoginSuccess(it.uid)
                }
                .onFailure {
                    _authState.value =
                        AuthState.Error(it.message ?: "Unknown error")
                }
        }
    }
}

package com.musclesOS.adil.model

import com.google.android.gms.auth.api.Auth

sealed class AuthState {

    data object Idle : AuthState()

    data object Loading : AuthState()

    data object RegistrationSuccess : AuthState()

    data object LoginSuccess : AuthState()

    data object PasswordResetSent : AuthState()
    data object EmailVerified : AuthState()
    data object VerificationEmailSent : AuthState()
    data class SignInWithFacebook(
        val uid: String
    ): AuthState()

    data class GoogleLoginSuccess(
        val uid: String
    ) : AuthState()

    data class GuestLoginSuccess(
        val uid: String
    ) : AuthState()

    data class Error(
        val message: String
    ) : AuthState()
}

package com.musclesOS.adil.repository

import android.util.Log
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * Repository class that handles all Firebase Authentication operations.
 * Acts as a single source of truth for authentication data.
 */
class AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    fun currentUser() = auth.currentUser

    /**
     * Authenticates a user with Firebase using a Google ID Token.
     */
    suspend fun signInWithGoogle(
        idToken: String
    ): Result<FirebaseUser> {

        return try {

            val credential =
                GoogleAuthProvider.getCredential(
                    idToken,
                    null
                )

            val result =
                auth.signInWithCredential(
                    credential
                ).await()

            Result.success(
                result.user!!
            )

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
    /**
     * Registers a new user using email and password.
     */
    suspend fun registerWithEmail(
        email: String,
        password: String,
        name: String
    ): Result<FirebaseUser> {

        return try {

            val result =
                auth.createUserWithEmailAndPassword(
                    email,
                    password
                ).await()

            val user = result.user!!
            if (name.isNotBlank()) {
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()
                user.reload().await()
            }

            Result.success(user)

        } catch (e: Exception) {

            Result.failure(e)

        }
    }

    /**
     * Logs in an existing user with email and password.
     * Checks if the email is verified before allowing access.
     */
    suspend fun loginWithEmail(
        email: String,
        password: String
    ): Result<FirebaseUser> {

        return try {

            val result =
                auth.signInWithEmailAndPassword(
                    email,
                    password
                ).await()
            result.user?.reload()?.await()

            if (result.user?.isEmailVerified == true) {

                Result.success(result.user!!)

            } else {

                auth.signOut()

                Result.failure(
                    Exception("Please verify your email.")
                )

            }

        } catch (e: Exception) {

            Result.failure(e)

        }

    }
    /**
     * Refreshes the current user's data from Firebase.
     */
    suspend fun reloadUser() {

        auth.currentUser
            ?.reload()
            ?.await()

    }
    /**
     * Authenticates a user with Firebase using a Facebook Access Token.
     */
    suspend fun signInWithFacebook(
        accessToken: String
    ): Result<FirebaseUser> {

        return try {

            val credential =
                FacebookAuthProvider.getCredential(
                    accessToken
                )

            val result =
                auth.signInWithCredential(
                    credential
                ).await()

            Result.success(result.user!!)

        } catch (e: Exception) {

            Result.failure(e)

        }

    }
    /**
     * Checks if the currently logged-in user has verified their email.
     */
    fun isEmailVerified(): Boolean {

        return auth.currentUser
            ?.isEmailVerified == true

    }
    /**
     * Sends a password reset email to the specified address.
     */
    suspend fun sendPasswordResetEmail(
        email: String
    ): Result<Unit> {

        return try {

            auth.sendPasswordResetEmail(
                email
            ).await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)

        }
    }
    /**
     * Checks if there is a user currently logged into the app.
     */
    fun isLoggedIn(): Boolean {

        return auth.currentUser != null

    }
    /**
     * Sends a verification email to the currently logged-in user.
     */
    suspend fun sendVerificationEmail(): Result<Unit> {

        return try {

            Log.d("VERIFY", "Sending verification email...")

            val user =
                auth.currentUser
                    ?: return Result.failure(
                        Exception("User not found.")
                    )

            user
                .sendEmailVerification()
                .await()

            Log.d("VERIFY", "Verification email sent.")

            Result.success(Unit)

        } catch (e: Exception) {

            Log.e("VERIFY", e.message ?: "Unknown error")

            Result.failure(e)
        }
    }
    /**
     * Signs out the current user from Firebase.
     */
    fun signOut() {
        auth.signOut()
    }
    /**
     * Authenticates the user as an anonymous guest.
     */
    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val result =
                auth.signInAnonymously().await()

            Result.success(
                result.user!!
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

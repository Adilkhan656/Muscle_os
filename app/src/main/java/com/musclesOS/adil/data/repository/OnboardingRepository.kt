package com.musclesOS.adil.data.repository

import com.musclesOS.adil.data.local.UserProfileDao
import com.musclesOS.adil.data.local.UserProfileEntity
import com.musclesOS.adil.data.remote.FirestoreUserProfileDataSource
import kotlinx.coroutines.flow.Flow

class UserProfileRepository(
    private val userProfileDao: UserProfileDao,
    private val firestoreDataSource: FirestoreUserProfileDataSource
) {

    /**
     * Save or update the user's profile in Firestore first, then cache the
     * confirmed remote state in Room.
     */
    suspend fun saveUserProfile(
        userProfile: UserProfileEntity
    ): Result<Unit> {
        return try {
            val remoteResult = firestoreDataSource.saveUserProfile(userProfile)

            if (remoteResult.isFailure) {
                return Result.failure(
                    remoteResult.exceptionOrNull()
                        ?: Exception("Failed to save user profile")
                )
            }

            // Only cache the profile after Firestore has confirmed the write.
            userProfileDao.insertOrUpdateProfile(userProfile)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Observe the user's profile from the local Room cache.
     */
    fun getUserProfile(
        userId: String
    ): Flow<UserProfileEntity?> {
        return userProfileDao.getUserProfile(userId)
    }

    /**
     * Delete the user's local profile.
     */
    suspend fun deleteUserProfile(
        userId: String
    ) {
        userProfileDao.deleteUserProfile(userId)
    }

    /**
     * Checks whether the user has completed onboarding.
     * Firestore is the source of truth; Room is only a cache.
     */
    suspend fun isOnboardingCompleted(userId: String): Boolean {
        val remoteResult = firestoreDataSource.fetchUserProfile(userId)

        remoteResult.getOrNull()?.let { remoteProfile ->
            userProfileDao.insertOrUpdateProfile(remoteProfile)
            return remoteProfile.isOnboardingCompleted
        }

        return false
    }
}

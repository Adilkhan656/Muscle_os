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
     * Save or update the user's profile locally and in Firestore.
     */
    suspend fun saveUserProfile(
        userProfile: UserProfileEntity
    ): Result<Unit> {
        return try {
            // Save to Room
            userProfileDao.insertOrUpdateProfile(userProfile)

            // Save to Firestore
            firestoreDataSource.saveUserProfile(userProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Observe the user's profile.
     *
     * Whenever Room data changes,
     * the Flow will emit the updated profile.
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
     *
     * Firestore is the source of truth for onboarding completion.
     * Room is used only as a local cache and must never override a
     * remote incomplete state with an old local `true` value.
     */
    suspend fun isOnboardingCompleted(userId: String): Boolean {
        // Always check Firestore first so stale Room data cannot mark
        // onboarding as completed before the user actually completes it.
        val remoteResult = firestoreDataSource.fetchUserProfile(userId)

        remoteResult.getOrNull()?.let { remoteProfile ->
            // Keep Room synchronized with the authoritative remote state.
            userProfileDao.insertOrUpdateProfile(remoteProfile)
            return remoteProfile.isOnboardingCompleted
        }

        // No remote profile means onboarding has not been completed yet.
        return false
    }
}

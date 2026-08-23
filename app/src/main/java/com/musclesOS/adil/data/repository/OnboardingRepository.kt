package com.musclesOS.adil.data.repository

import com.musclesOS.adil.data.local.UserProfileDao
import com.musclesOS.adil.data.local.UserProfileEntity
import com.musclesOS.adil.data.remote.FirestoreUserProfileDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

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
     * Logic:
     * 1. Check local Room database.
     * 2. If Room has data and isOnboardingCompleted is true, return true.
     * 3. If Room is empty, check Firestore.
     * 4. If Firestore has data, sync it to Room and return its isOnboardingCompleted status.
     * 5. Otherwise, return false.
     */
    suspend fun isOnboardingCompleted(userId: String): Boolean {
        // 1. Check local Room
        val localProfile = userProfileDao.getUserProfile(userId).firstOrNull()
        if (localProfile != null && localProfile.isOnboardingCompleted) {
            return true
        }

        // 2. Room is empty or incomplete, check Firestore
        val remoteResult = firestoreDataSource.fetchUserProfile(userId)
        val remoteProfile = remoteResult.getOrNull()

        if (remoteProfile != null) {
            // Sync remote profile to local Room
            userProfileDao.insertOrUpdateProfile(remoteProfile)
            return remoteProfile.isOnboardingCompleted
        }

        return false
    }
}

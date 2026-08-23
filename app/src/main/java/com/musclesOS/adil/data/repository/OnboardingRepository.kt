package com.musclesOS.adil.data.repository

import com.musclesOS.adil.data.local.UserProfileDao
import com.musclesOS.adil.data.local.UserProfileEntity
import kotlinx.coroutines.flow.Flow

class UserProfileRepository(
    private val userProfileDao: UserProfileDao
) {

    /**
     * Save or update the user's profile locally.
     */
    suspend fun saveUserProfile(
        userProfile: UserProfileEntity
    ) {
        userProfileDao.insertOrUpdateProfile(userProfile)
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
}
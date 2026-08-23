package com.musclesOS.adil.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(
        userProfile: UserProfileEntity
    )

    @Query("SELECT * FROM user_profile WHERE userId = :userId LIMIT 1")
    fun getUserProfile(
        userId: String
    ): Flow<UserProfileEntity?>

    @Query("DELETE FROM user_profile WHERE userId = :userId")
    suspend fun deleteUserProfile(
        userId: String
    )

    @Query("DELETE FROM user_profile")
    suspend fun deleteAllProfiles()
}
package com.musclesOS.adil.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(

    @PrimaryKey
    val userId: String,

    val gender: String,

    val age: Int,

    val heightCm: Int,

    val weightKg: Double,

    val activityLevel: String,

    val experienceLevel: String,

    val focusAreas: List<String>,

    val goals: List<String>,

    val customGoal: String,

    val isOnboardingCompleted: Boolean,

    val createdAt: Long,

    val updatedAt: Long
)
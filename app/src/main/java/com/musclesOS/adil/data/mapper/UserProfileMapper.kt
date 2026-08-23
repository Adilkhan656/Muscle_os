package com.musclesOS.adil.data.mapper

import com.musclesOS.adil.data.UserProfile
import com.musclesOS.adil.data.local.UserProfileEntity


fun UserProfile.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        userId = userId,
        gender = gender,
        age = age,
        heightCm = heightCm,
        weightKg = weightKg,
        activityLevel = activityLevel,
        experienceLevel = experienceLevel,
        focusAreas = focusAreas,
        goals = goals,
        customGoal = customGoal,
        isOnboardingCompleted = isOnboardingCompleted,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun UserProfileEntity.toDomain(): UserProfile {
    return UserProfile(
        userId = userId,
        gender = gender,
        age = age,
        heightCm = heightCm,
        weightKg = weightKg,
        activityLevel = activityLevel,
        experienceLevel = experienceLevel,
        focusAreas = focusAreas,
        goals = goals,
        customGoal = customGoal,
        isOnboardingCompleted = isOnboardingCompleted,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
package com.musclesOS.adil.data

data class UserProfile(
    val userId: String = "",

    // Basic information
    val gender: String = "",
    val age: Int = 0,

    // Body measurements
    val heightCm: Int = 0,
    val weightKg: Double = 0.0,
    val targetWeightKg: Double = 0.0,

    // Activity / training information
    val activityLevel: String = "",
    val experienceLevel: String = "",

    // Focus areas
    val focusAreas: List<String> = emptyList(),

    // Fitness goals
    val goals: List<String> = emptyList(),

    // Custom goal entered by user
    val customGoal: String = "",

    // Onboarding status
    val isOnboardingCompleted: Boolean = false,

    // Useful for syncing later
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
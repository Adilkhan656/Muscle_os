package com.musclesOS.adil.ui.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.musclesOS.adil.data.UserProfile
import com.musclesOS.adil.data.local.UserProfileEntity
import com.musclesOS.adil.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val repository: UserProfileRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    fun updateGender(gender: String) {
        _userProfile.value = _userProfile.value.copy(gender = gender)
    }

    fun updateAge(age: Int) {
        _userProfile.value = _userProfile.value.copy(age = age)
    }

    fun updateHeight(heightCm: Int) {
        _userProfile.value = _userProfile.value.copy(heightCm = heightCm)
    }

    fun updateWeight(weightKg: Double) {
        _userProfile.value = _userProfile.value.copy(weightKg = weightKg)
    }

    fun updateActivityLevel(activityLevel: String) {
        _userProfile.value = _userProfile.value.copy(activityLevel = activityLevel)
    }

    fun updateExperienceLevel(experienceLevel: String) {
        _userProfile.value = _userProfile.value.copy(experienceLevel = experienceLevel)
    }

    fun updateFocusAreas(focusAreas: List<String>) {
        _userProfile.value = _userProfile.value.copy(focusAreas = focusAreas)
    }

    fun updateGoals(goals: List<String>) {
        _userProfile.value = _userProfile.value.copy(goals = goals)
    }

    fun updateCustomGoal(customGoal: String) {
        _userProfile.value = _userProfile.value.copy(customGoal = customGoal)
    }

    fun markOnboardingCompleted() {
        _userProfile.value = _userProfile.value.copy(
            isOnboardingCompleted = true,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun saveOnboarding(
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                    ?: throw IllegalStateException("User not authenticated")

                val profile = _userProfile.value
                val entity = UserProfileEntity(
                    userId = userId,
                    gender = profile.gender,
                    age = profile.age,
                    heightCm = profile.heightCm,
                    weightKg = profile.weightKg,
                    activityLevel = profile.activityLevel,
                    experienceLevel = profile.experienceLevel,
                    focusAreas = profile.focusAreas,
                    goals = profile.goals,
                    customGoal = profile.customGoal,
                    isOnboardingCompleted = true,
                    createdAt = if (profile.createdAt == 0L) {
                        System.currentTimeMillis()
                    } else {
                        profile.createdAt
                    },
                    updatedAt = System.currentTimeMillis()
                )

                val result = repository.saveUserProfile(entity)

                if (result.isSuccess) {
                    // Only update local state after the remote profile has been saved.
                    markOnboardingCompleted()
                    onSuccess()
                } else {
                    onError(
                        result.exceptionOrNull() as? Exception
                            ?: Exception("Unknown error")
                    )
                }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}

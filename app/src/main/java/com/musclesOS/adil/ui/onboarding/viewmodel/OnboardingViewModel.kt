package com.musclesOS.adil.ui.onboarding.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.musclesOS.adil.data.UserProfile
import com.musclesOS.adil.data.local.UserProfileEntity
import com.musclesOS.adil.data.remote.GroqApiService
import com.musclesOS.adil.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val repository: UserProfileRepository
) : ViewModel() {

    private val groqApiService = GroqApiService()

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

    fun updateTargetWeight(targetWeightKg: Double) {
        _userProfile.value = _userProfile.value.copy(targetWeightKg = targetWeightKg)
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
                    targetWeightKg = profile.targetWeightKg,
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

                if (!result.isSuccess) {
                    onError(
                        result.exceptionOrNull() as? Exception
                            ?: Exception("Unknown error")
                    )
                    return@launch
                }

                Log.d("GROQ_RESPONSE", "Starting Groq request...")

                val groqResult = groqApiService.generateTestPlan(entity)

                if (groqResult.isSuccess) {
                    val response = groqResult.getOrThrow()
                    Log.d("GROQ_RESPONSE", response)
                    Log.d("GROQ_RESPONSE", "Groq request completed successfully.")
                    markOnboardingCompleted()
                    onSuccess()
                } else {
                    val error = groqResult.exceptionOrNull() as? Exception
                        ?: Exception("Unknown Groq error")
                    Log.e("GROQ_RESPONSE", "Groq request failed", error)
                    onError(error)
                }
            } catch (e: Exception) {
                Log.e("GROQ_RESPONSE", "Onboarding/Groq integration failed", e)
                onError(e)
            }
        }
    }
}

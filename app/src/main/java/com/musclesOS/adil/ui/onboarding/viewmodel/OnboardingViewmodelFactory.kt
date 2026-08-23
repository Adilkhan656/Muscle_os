package com.musclesOS.adil.ui.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.musclesOS.adil.data.repository.UserProfileRepository

class OnboardingViewModelFactory(
    private val repository: UserProfileRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                OnboardingViewModel::class.java
            )
        ) {

            @Suppress("UNCHECKED_CAST")
            return OnboardingViewModel(
                repository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}
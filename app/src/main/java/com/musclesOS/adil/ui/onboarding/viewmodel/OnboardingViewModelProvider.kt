package com.musclesOS.adil.ui.onboarding.viewmodel

import android.content.Context
import com.musclesOS.adil.data.local.AppDatabase
import com.musclesOS.adil.data.repository.UserProfileRepository

object OnboardingViewModelProvider {

    fun provideFactory(
        context: Context
    ): OnboardingViewModelFactory {

        val database =
            AppDatabase.getDatabase(context)

        val dao =
            database.userProfileDao()

        val repository =
            UserProfileRepository(dao)

        return OnboardingViewModelFactory(
            repository
        )
    }
}
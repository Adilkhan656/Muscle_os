package com.musclesOS.adil.ui.onboarding.viewmodel

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.musclesOS.adil.data.local.AppDatabase
import com.musclesOS.adil.data.remote.FirestoreUserProfileDataSource
import com.musclesOS.adil.data.repository.UserProfileRepository

object OnboardingViewModelProvider {

    fun provideFactory(
        context: Context
    ): OnboardingViewModelFactory {

        val database =
            AppDatabase.getDatabase(context)

        val dao =
            database.userProfileDao()

        val firestoreDataSource = FirestoreUserProfileDataSource(
            FirebaseFirestore.getInstance(),
            FirebaseAuth.getInstance()
        )

        val repository =
            UserProfileRepository(dao, firestoreDataSource)

        return OnboardingViewModelFactory(
            repository
        )
    }
}
package com.musclesOS.adil.core

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.musclesOS.adil.OneSignalManager
import com.musclesOS.adil.data.local.AppDatabase
import com.musclesOS.adil.data.remote.FirestoreUserProfileDataSource
import com.musclesOS.adil.repository.AuthRepository
import com.musclesOS.adil.data.repository.UserProfileRepository

/** Performs the small amount of work needed before leaving the branded splash. */
class AppInitializer(
    private val context: Context
) {

    private val authRepository by lazy { AuthRepository() }

    private val userProfileRepository by lazy {
        val database = AppDatabase.getDatabase(context)
        val firestoreDataSource = FirestoreUserProfileDataSource(
            FirebaseFirestore.getInstance(),
            FirebaseAuth.getInstance()
        )
        UserProfileRepository(database.userProfileDao(), firestoreDataSource)
    }

    suspend fun initialize(): AppDestination {
        FirebaseApp.initializeApp(context.applicationContext)

        val user = authRepository.currentUser()
        if (user == null) {
            return AppDestination.Welcome
        }

        // Keep OneSignal identity and reusable user properties synchronized with Firebase.
        OneSignalManager.syncUser(user)

        return if (userProfileRepository.isOnboardingCompleted(user.uid)) {
            AppDestination.Main
        } else {
            AppDestination.Onboarding
        }
    }
}

enum class AppDestination {
    Main,
    Welcome,
    Onboarding
}

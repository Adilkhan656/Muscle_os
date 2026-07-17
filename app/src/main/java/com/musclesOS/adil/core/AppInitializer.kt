package com.musclesOS.adil.core

import android.content.Context
import com.google.firebase.FirebaseApp
import com.musclesOS.adil.repository.AuthRepository

/** Performs the small amount of work needed before leaving the branded splash. */
class AppInitializer(
    private val context: Context
) {

    private val authRepository by lazy { AuthRepository() }

    fun initialize(): AppDestination {
        // Safe to call more than once; Firebase returns its existing app instance.
        FirebaseApp.initializeApp(context.applicationContext)

        return if (authRepository.isLoggedIn()) {
            AppDestination.Main
        } else {
            AppDestination.Welcome
        }
    }
}

enum class AppDestination {
    Main,
    Welcome
}

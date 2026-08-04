package com.musclesOS.adil.ui.splash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.musclesOS.adil.core.AppInitializer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Splash screen.
 * Handles app initialization and determines the initial destination.
 * Ensures a minimum splash duration for a smooth branding experience.
 */
class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state.asStateFlow()

    private val minSplashDuration = 1_400L

    init { initialize() }

    /**
     * Orchestrates the app initialization process and sets the final SplashState.
     */
    private fun initialize() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            try {
                // Initialization is independent of rendering, so it must not block the splash UI.
                val destination = withContext(Dispatchers.Default) {
                    AppInitializer(getApplication()).initialize()
                }

                val remaining = minSplashDuration - (System.currentTimeMillis() - startTime)
                if (remaining > 0) delay(remaining)

                _state.value = SplashState.Ready(destination)
            } catch (e: Exception) {
                _state.value = SplashState.Error(e.message ?: "Initialization failed")
            }
        }
    }

}

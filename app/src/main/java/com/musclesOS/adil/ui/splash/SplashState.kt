package com.musclesOS.adil.ui.splash

import com.musclesOS.adil.core.AppDestination

sealed class SplashState {
    object Loading : SplashState()
    data class Ready(val destination: AppDestination) : SplashState()
    data class Error(val message: String) : SplashState()
}

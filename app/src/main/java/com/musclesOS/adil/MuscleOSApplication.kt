package com.musclesOS.adil

import android.app.Application

class MuscleOSApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        OneSignalManager.initialize(this)
    }
}

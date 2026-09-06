package com.musclesOS.adil

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.onesignal.OneSignal
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Centralized OneSignal integration for MuscleOS.
 * Keep direct OneSignal SDK calls inside this class.
 */
object OneSignalManager {

    private const val APP_ID = "91f6f182-79a9-41ca-a760-6fa18d23deb0"
    private const val LAST_PROMPT_KEY = "last_notification_prompt_time"

    fun initialize(context: Context) {
        OneSignal.initWithContext(context.applicationContext, APP_ID)
    }

    /**
     * Prompts the user to enable notifications if they currently have them turned off,
     * showing the request at most once per day.
     */
    fun promptNotificationPermissionIfDaily(activity: ComponentActivity) {
        if (OneSignal.Notifications.permission) return

        if (activity.isFinishing || activity.isDestroyed) return

        val preferences =
            activity.getSharedPreferences("onesignal", Context.MODE_PRIVATE)

        val lastPromptTime = preferences.getLong(LAST_PROMPT_KEY, 0L)
        val currentTime = System.currentTimeMillis()

        if (lastPromptTime > 0L && !isDifferentDay(lastPromptTime, currentTime)) {
            return
        }

        preferences.edit()
            .putLong(LAST_PROMPT_KEY, currentTime)
            .apply()

        activity.runOnUiThread {
            AlertDialog.Builder(activity)
                .setTitle("Enable Notifications")
                .setMessage(
                    "Stay on track with your fitness goals! " +
                            "Enable notifications to receive workout reminders and progress updates."
                )
                .setPositiveButton("Enable") { _, _ ->
                    activity.lifecycleScope.launch {
                        OneSignal.Notifications.requestPermission(true)
                    }
                }
                .setNegativeButton("Maybe Later", null)
                .setCancelable(false)
                .show()
        }
    }

    private fun isDifferentDay(
        lastTimeMs: Long,
        currentTimeMs: Long
    ): Boolean {
        val lastCal = Calendar.getInstance().apply {
            timeInMillis = lastTimeMs
        }

        val currentCal = Calendar.getInstance().apply {
            timeInMillis = currentTimeMs
        }

        return lastCal.get(Calendar.YEAR) != currentCal.get(Calendar.YEAR) ||
                lastCal.get(Calendar.DAY_OF_YEAR) != currentCal.get(Calendar.DAY_OF_YEAR)
    }

    fun login(externalId: String) {
        OneSignal.login(externalId)
    }

    fun logout() {
        OneSignal.logout()
    }

    fun addEmail(email: String) {
        OneSignal.User.addEmail(email)
    }

    fun addSms(phoneNumber: String) {
        OneSignal.User.addSms(phoneNumber)
    }

    fun addTag(key: String, value: String) {
        OneSignal.User.addTag(key, value)
    }

    fun trackEvent(name: String) {
        OneSignal.User.trackEvent(name)
    }
}
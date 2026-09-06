package com.musclesOS.adil

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.onesignal.OneSignal
import com.onesignal.user.subscriptions.IPushSubscriptionObserver
import com.onesignal.user.subscriptions.PushSubscriptionChangedState
import kotlinx.coroutines.launch

/**
 * Centralized OneSignal integration for MuscleOS.
 * Keep direct OneSignal SDK calls inside this class.
 */
object OneSignalManager {

    private const val APP_ID = "91f6f182-79a9-41ca-a760-6fa18d23deb0"
    private const val VERIFICATION_SHOWN_KEY = "onesignal_verification_dialog_shown"

    private var subscriptionObserver: IPushSubscriptionObserver? = null

    fun initialize(context: Context) {
        OneSignal.initWithContext(context.applicationContext, APP_ID)
    }

    fun observePushSubscription(activity: Activity) {
        if (subscriptionObserver != null) return

        val observer = object : IPushSubscriptionObserver {
            override fun onPushSubscriptionChange(state: PushSubscriptionChangedState) {
                checkSubscription(activity, state.current.id)
            }
        }

        subscriptionObserver = observer
        OneSignal.User.pushSubscription.addObserver(observer)

        // Also evaluate the current value immediately in case registration completed
        // before the observer was attached.
        checkSubscription(activity, OneSignal.User.pushSubscription.id)
    }

    private fun checkSubscription(activity: Activity, subscriptionId: String?) {
        if (subscriptionId.isNullOrBlank() || subscriptionId.startsWith("local-")) return
        if (activity.isFinishing || activity.isDestroyed) return

        val preferences = activity.getSharedPreferences("onesignal", Context.MODE_PRIVATE)
        if (preferences.getBoolean(VERIFICATION_SHOWN_KEY, false)) return

        preferences.edit().putBoolean(VERIFICATION_SHOWN_KEY, true).apply()

        activity.runOnUiThread {
            AlertDialog.Builder(activity)
                .setTitle("Your OneSignal SDK integration is complete!")
                .setMessage(
                    "You can now send Push Notifications & In-App Messages through OneSignal. " +
                        "Tap below to enable push notifications."
                )
                .setPositiveButton("Got it") { _, _ ->
                    activity.lifecycleScope.launch {
                        OneSignal.Notifications.requestPermission(true)
                    }
                }
                .setCancelable(false)
                .show()
        }
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

    fun trackEvent(name: String, properties: Map<String, Any?> = emptyMap()) {
        OneSignal.User.trackEvent(name, properties)
    }
}

package com.musclesOS.adil

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.onesignal.OneSignal
import com.onesignal.user.subscriptions.IPushSubscriptionObserver
import com.onesignal.user.subscriptions.PushSubscriptionChangedState
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Centralized OneSignal integration for MuscleOS.
 * Keep direct OneSignal SDK calls inside this class.
 */
object OneSignalManager {

    private const val APP_ID = "91f6f182-79a9-41ca-a760-6fa18d23deb0"

    private const val LAST_PROMPT_KEY = "last_notification_prompt_time"

    private const val USER_NAME_TAG = "user_name"
    private const val ONBOARDING_COMPLETED_TAG = "onboarding_completed"
    private const val WELCOME_READY_TAG = "welcome_notification_ready"

    private const val PREFS_NAME = "onesignal"
    private const val PENDING_WELCOME_KEY = "pending_welcome_notification"

    /**
     * Name of the currently authenticated Firebase user.
     *
     * This is used when onboarding completion happens after login.
     */
    private var currentUserName: String? = null

    /**
     * OneSignal push subscription observer.
     *
     * If onboarding was completed while the push subscription was
     * not ready, this observer completes the welcome trigger once
     * the subscription becomes opted in.
     */
    private val pushSubscriptionObserver =
        object : IPushSubscriptionObserver {

            override fun onPushSubscriptionChange(
                state: PushSubscriptionChangedState
            ) {
                if (!state.current.optedIn) {
                    return
                }

                val context = appContext ?: return

                val preferences =
                    context.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                    )

                val pendingWelcome =
                    preferences.getBoolean(
                        PENDING_WELCOME_KEY,
                        false
                    )

                if (pendingWelcome) {
                    completeWelcomeNotification()
                }
            }
        }

    private var appContext: Context? = null

    fun initialize(context: Context) {

        appContext = context.applicationContext

        OneSignal.initWithContext(
            appContext!!,
            APP_ID
        )

        OneSignal.User.pushSubscription.addObserver(
            pushSubscriptionObserver
        )
    }

    /**
     * Prompts the user to enable notifications if they currently
     * have them turned off, showing the request at most once per day.
     */
    fun promptNotificationPermissionIfDaily(
        activity: ComponentActivity
    ) {
        if (OneSignal.Notifications.permission) return
        if (activity.isFinishing || activity.isDestroyed) return

        val preferences =
            activity.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        val lastPromptTime =
            preferences.getLong(
                LAST_PROMPT_KEY,
                0L
            )

        val currentTime =
            System.currentTimeMillis()

        if (
            lastPromptTime > 0L &&
            !isDifferentDay(
                lastPromptTime,
                currentTime
            )
        ) {
            return
        }

        preferences.edit()
            .putLong(
                LAST_PROMPT_KEY,
                currentTime
            )
            .apply()

        activity.runOnUiThread {

            AlertDialog.Builder(activity)
                .setTitle("Enable Notifications")
                .setMessage(
                    "Stay on track with your fitness goals! " +
                            "Enable notifications to receive workout reminders " +
                            "and progress updates."
                )
                .setPositiveButton("Enable") { _, _ ->

                    activity.lifecycleScope.launch {
                        OneSignal.Notifications.requestPermission(true)
                    }
                }
                .setNegativeButton(
                    "Maybe Later",
                    null
                )
                .setCancelable(false)
                .show()
        }
    }

    /**
     * Synchronizes the Firebase user with OneSignal.
     *
     * IMPORTANT:
     * This method does NOT trigger the welcome Journey.
     *
     * Logging in or restoring a session must never cause the
     * welcome notification to be sent.
     */
    fun syncUser(
        user: FirebaseUser,
        onboardingCompleted: Boolean
    ) {

        /*
         * Identify the user first.
         */
        login(user.uid)

        /*
         * Save the current user's name locally in this manager.
         */
        currentUserName =
            if (user.isAnonymous) {
                null
            } else {
                user.displayName
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
            }

        /*
         * Store the user's name.
         *
         * This does NOT trigger the Journey because the Journey
         * will use welcome_notification_ready instead.
         */
        val tags = mutableMapOf<String, String>()

        currentUserName?.let {
            tags[USER_NAME_TAG] = it
        }

        /*
         * Keep onboarding_completed as an informational tag.
         *
         * It is NOT the Journey trigger anymore.
         */
        tags[ONBOARDING_COMPLETED_TAG] =
            onboardingCompleted.toString()

        OneSignal.User.addTags(tags)

        /*
         * Email is associated after login.
         */
        if (!user.isAnonymous) {

            user.email
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    addEmail(it)
                }
        }
    }

    /**
     * Call this ONLY when the user's onboarding has actually
     * been successfully saved.
     *
     * This is the trigger point for the welcome notification.
     */
    fun completeOnboarding(userName: String? = null) {
        userName?.trim()?.takeIf { it.isNotBlank() }?.let {
            currentUserName = it
        }

        if (OneSignal.User.pushSubscription.optedIn) {
            completeWelcomeNotification()
            return
        }

        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()
            ?.putBoolean(PENDING_WELCOME_KEY, true)
            ?.apply()
    }

    /**
     * Makes the user eligible for the welcome Journey.
     *
     * This is intentionally the ONLY place where
     * welcome_notification_ready=true is set.
     */
    private fun completeWelcomeNotification() {
        val context = appContext ?: return

        val name = currentUserName
            ?: FirebaseAuth.getInstance().currentUser?.displayName?.trim()?.takeIf { it.isNotBlank() }

        val tags = mutableMapOf<String, String>()
        name?.let { tags[USER_NAME_TAG] = it }
        tags[WELCOME_READY_TAG] = "true"
        OneSignal.User.addTags(tags)  // both written in one call, still fine

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(PENDING_WELCOME_KEY, false)
            .apply()
    }

    private fun isDifferentDay(
        lastTimeMs: Long,
        currentTimeMs: Long
    ): Boolean {

        val lastCal =
            Calendar.getInstance().apply {
                timeInMillis = lastTimeMs
            }

        val currentCal =
            Calendar.getInstance().apply {
                timeInMillis = currentTimeMs
            }

        return lastCal.get(Calendar.YEAR) !=
                currentCal.get(Calendar.YEAR) ||
                lastCal.get(Calendar.DAY_OF_YEAR) !=
                currentCal.get(Calendar.DAY_OF_YEAR)
    }

    fun login(externalId: String) {
        OneSignal.login(externalId)
    }

    fun logout() {

        currentUserName = null

        appContext
            ?.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            ?.edit()
            ?.putBoolean(
                PENDING_WELCOME_KEY,
                false
            )
            ?.apply()

        OneSignal.logout()
    }

    fun addEmail(email: String) {
        OneSignal.User.addEmail(email)
    }

    fun addSms(phoneNumber: String) {
        OneSignal.User.addSms(phoneNumber)
    }

    fun addTag(
        key: String,
        value: String
    ) {
        /*
         * Keep this method for other tags in the app.
         *
         * IMPORTANT:
         * We intentionally do NOT intercept
         * onboarding_completed anymore.
         *
         * The welcome Journey can only be triggered by
         * completeOnboarding().
         */
        OneSignal.User.addTag(
            key,
            value
        )
    }

    fun addTags(
        tags: Map<String, String>
    ) {
        OneSignal.User.addTags(tags)
    }
}
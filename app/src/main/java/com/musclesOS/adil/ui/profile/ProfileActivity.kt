package com.musclesOS.adil.ui.profile

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.musclesOS.adil.OneSignalManager
import com.musclesOS.adil.R
import com.musclesOS.adil.data.local.UserProfileEntity
import com.musclesOS.adil.data.remote.FirestoreUserProfileDataSource
import com.musclesOS.adil.databinding.ActivityProfileBinding
import com.musclesOS.adil.repository.AuthRepository
import kotlinx.coroutines.launch
import kotlin.math.abs

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val dataSource by lazy { FirestoreUserProfileDataSource(FirebaseFirestore.getInstance(), auth) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.editProfileButton.setOnClickListener { startActivity(Intent(this, EditProfileActivity::class.java)) }
        binding.logoutButton.setOnClickListener { logout() }
        loadProfile()
    }

    override fun onResume() {
        super.onResume()
        if (::binding.isInitialized) loadProfile()
    }

    private fun loadProfile() {
        val user = auth.currentUser ?: run { logout(); return }
        val displayName = user.displayName?.trim().takeUnless { it.isNullOrBlank() } ?: "MuscleOS Member"
        binding.nameText.text = displayName
        binding.emailText.text = user.email ?: "Email unavailable"
        binding.avatarText.text = displayName.firstOrNull()?.uppercase() ?: "M"

        lifecycleScope.launch {
            dataSource.fetchUserProfile(user.uid).onSuccess { profile ->
                if (profile == null) Toast.makeText(this@ProfileActivity, "Profile data not found.", Toast.LENGTH_SHORT).show()
                else renderProfile(profile)
            }.onFailure {
                Toast.makeText(this@ProfileActivity, it.message ?: "Unable to load profile.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderProfile(profile: UserProfileEntity) {
        setStat(R.id.statAge, profile.age.toString(), "Age")
        setStat(R.id.statHeight, "${profile.heightCm} cm", "Height")
        setStat(R.id.statCurrent, "${format(profile.weightKg)} kg", "Current")
        setStat(R.id.statTarget, "${format(profile.targetWeightKg)} kg", "Target")
        binding.weightProgressArc.setWeights(profile.weightKg, profile.targetWeightKg)
        binding.currentWeightLabel.text = "Current  ${format(profile.weightKg)} kg"
        binding.targetWeightLabel.text = "Target  ${format(profile.targetWeightKg)} kg"
        val distance = abs(profile.weightKg - profile.targetWeightKg)
        binding.weightDirectionText.text = if (distance <= 0.05) "TARGET REACHED" else "${format(distance)} KG TO GO"
        renderTags(binding.goalsContainer, profile.goals)
        renderTags(binding.focusContainer, profile.focusAreas)
        binding.activityText.text = "Activity level   •   ${pretty(profile.activityLevel)}"
        binding.experienceText.text = "Experience       •   ${pretty(profile.experienceLevel)}"
        binding.customGoalText.text = if (profile.customGoal.isBlank()) "Custom goal      •   None" else "Custom goal      •   ${profile.customGoal}"
    }

    private fun setStat(id: Int, value: String, label: String) {
        val root = findViewById<View>(id)
        root.findViewById<TextView>(R.id.statValue).text = value
        root.findViewById<TextView>(R.id.statLabel).text = label
    }

    private fun renderTags(container: LinearLayout, values: List<String>) {
        container.removeAllViews()
        if (values.isEmpty()) {
            container.addView(TextView(this).apply { text = "Nothing selected"; setTextColor(getColor(R.color.premium_muted)); textSize = 13f })
            return
        }
        val scroll = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        values.forEach { value ->
            row.addView(TextView(this).apply {
                text = pretty(value)
                setTextColor(getColor(R.color.premium_text))
                textSize = 13f
                setPadding(dp(14), dp(9), dp(14), dp(9))
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = dp(20).toFloat()
                    setColor(getColor(R.color.profile_chip_bg))
                    setStroke(dp(1), getColor(R.color.profile_chip_stroke))
                }
                layoutParams = LinearLayout.LayoutParams(-2, -2).apply { setMargins(0, 0, dp(8), 0) }
            })
        }
        scroll.addView(row)
        container.addView(scroll)
    }

    private fun logout() {
        AuthRepository().signOut()
        OneSignalManager.logout()
        startActivity(Intent(this, com.musclesOS.adil.ui.auth.LoginActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
        finish()
    }

    private fun pretty(value: String): String = value.replace('_', ' ').replaceFirstChar { it.uppercase() }
    private fun format(value: Double): String = String.format(java.util.Locale.US, "%.1f", value)
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}

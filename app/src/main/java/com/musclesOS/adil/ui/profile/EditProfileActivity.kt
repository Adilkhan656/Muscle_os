package com.musclesOS.adil.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.musclesOS.adil.OneSignalManager
import com.musclesOS.adil.R
import com.musclesOS.adil.data.local.AppDatabase
import com.musclesOS.adil.data.local.UserProfileEntity
import com.musclesOS.adil.data.remote.FirestoreUserProfileDataSource
import com.musclesOS.adil.databinding.ActivityEditProfileBinding
import com.musclesOS.adil.repository.AuthRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val dataSource by lazy { FirestoreUserProfileDataSource(FirebaseFirestore.getInstance(), auth) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupDropdowns()
        binding.backButton.setOnClickListener { finish() }
        binding.logoutButton.setOnClickListener { logout() }
        binding.saveButton.setOnClickListener { saveChanges() }
        loadProfile()
    }

    private fun setupDropdowns() {
        setDropdown(binding.genderInput, listOf("Male", "Female", "Other", "Prefer not to say"))
        setDropdown(binding.activityInput, listOf("Low", "Light", "Moderate", "High", "Very high"))
        setDropdown(binding.experienceInput, listOf("Beginner", "Intermediate", "Advanced"))
    }

    private fun setDropdown(view: android.widget.AutoCompleteTextView, values: List<String>) {
        view.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, values))
        view.setOnClickListener { view.showDropDown() }
    }

    private fun loadProfile() {
        val user = auth.currentUser ?: run { logout(); return }
        binding.nameInput.setText(user.displayName ?: "")
        binding.emailInput.setText(user.email ?: "")

        lifecycleScope.launch {
            dataSource.fetchUserProfile(user.uid).onSuccess { profile ->
                if (profile == null) return@onSuccess
                binding.ageInput.setText(profile.age.toString())
                binding.genderInput.setText(profile.gender, false)
                binding.heightInput.setText(profile.heightCm.toString())
                binding.currentWeightInput.setText(profile.weightKg.toString())
                binding.targetWeightInput.setText(profile.targetWeightKg.toString())
                binding.activityInput.setText(profile.activityLevel, false)
                binding.experienceInput.setText(profile.experienceLevel, false)
                binding.goalsInput.setText(profile.goals.joinToString(", "))
                binding.focusInput.setText(profile.focusAreas.joinToString(", "))
                binding.customGoalInput.setText(profile.customGoal)
            }.onFailure {
                Toast.makeText(this@EditProfileActivity, it.message ?: "Unable to load profile.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveChanges() {
        val user = auth.currentUser ?: run { logout(); return }
        val age = binding.ageInput.text?.toString()?.toIntOrNull()
        val height = binding.heightInput.text?.toString()?.toIntOrNull()
        val currentWeight = binding.currentWeightInput.text?.toString()?.toDoubleOrNull()
        val targetWeight = binding.targetWeightInput.text?.toString()?.toDoubleOrNull()

        if (age == null || age !in 13..100) return showError("Enter a valid age.")
        if (height == null || height !in 100..250) return showError("Enter a valid height.")
        if (currentWeight == null || currentWeight !in 25.0..300.0) return showError("Enter a valid current weight.")
        if (targetWeight == null || targetWeight !in 25.0..300.0) return showError("Enter a valid target weight.")

        val goals = csv(binding.goalsInput.text?.toString())
        val focusAreas = csv(binding.focusInput.text?.toString())
        val name = binding.nameInput.text?.toString()?.trim().orEmpty()
        if (name.isBlank()) return showError("Enter your name.")

        binding.saveButton.isEnabled = false
        lifecycleScope.launch {
            val existing = dataSource.fetchUserProfile(user.uid).getOrNull()
            val profile = UserProfileEntity(
                userId = user.uid,
                gender = binding.genderInput.text?.toString()?.trim().orEmpty(),
                age = age,
                heightCm = height,
                weightKg = currentWeight,
                targetWeightKg = targetWeight,
                activityLevel = binding.activityInput.text?.toString()?.trim().orEmpty(),
                experienceLevel = binding.experienceInput.text?.toString()?.trim().orEmpty(),
                focusAreas = focusAreas,
                goals = goals,
                customGoal = binding.customGoalInput.text?.toString()?.trim().orEmpty(),
                isOnboardingCompleted = true,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val saveResult = dataSource.saveUserProfile(profile)
            if (saveResult.isFailure) {
                binding.saveButton.isEnabled = true
                showError(saveResult.exceptionOrNull()?.message ?: "Unable to save profile.")
                return@launch
            }

            AppDatabase.getDatabase(this@EditProfileActivity).userProfileDao().insertOrUpdateProfile(profile)
            user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build()).await()

            Toast.makeText(this@EditProfileActivity, "Profile updated", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun csv(value: String): List<String> = value.split(",").map { it.trim().lowercase().replace(' ', '_') }.filter { it.isNotBlank() }.distinct()
    private fun showError(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun logout() {
        AuthRepository().signOut()
        OneSignalManager.logout()
        startActivity(Intent(this, com.musclesOS.adil.ui.auth.LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}

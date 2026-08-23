package com.musclesOS.adil.ui.onboarding.fragment

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.musclesOS.adil.R
import com.musclesOS.adil.ui.onboarding.viewmodel.OnboardingViewModelProvider
import com.musclesOS.adil.databinding.FragmentGenderBinding
import com.musclesOS.adil.ui.auth.LoginActivity
import com.musclesOS.adil.ui.onboarding.viewmodel.OnboardingViewModel
import com.musclesOS.adil.utils.animation.OnboardingAnimations

class GenderFragment : Fragment(R.layout.fragment_gender) {

    private var _binding: FragmentGenderBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OnboardingViewModel by activityViewModels {
        OnboardingViewModelProvider.provideFactory(requireContext())
    }
    private var selectedGender: Boolean? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGenderBinding.bind(view)

        setupHeader()
        setupClickListeners()
        restoreSelection()
        applyPremiumAnimations()

        binding.header.backButton.setOnClickListener {
            showExitDialog()
        }
    }

    private fun showExitDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom_alert, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogView.findViewById<TextView>(R.id.btnNo).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<View>(R.id.btnYes).setOnClickListener {
            dialog.dismiss()
            
            // Actually sign out so LoginActivity doesn't redirect us back here
            com.musclesOS.adil.repository.AuthRepository().signOut()
            
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        dialog.show()
        
        // Set fixed width for the dialog
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = (resources.displayMetrics.density * 320).toInt()
        dialog.window?.attributes = layoutParams
    }

    private fun setupHeader() {
        binding.header.progressTag.text = "1 of 6"
        binding.header.progressBar.progress = 100
    }

    private fun setupClickListeners() {
        binding.cardMale.setOnClickListener { selectMale() }
        binding.maleImage.setOnClickListener { selectMale() }

        binding.cardFemale.setOnClickListener { selectFemale() }
        binding.femaleImage.setOnClickListener { selectFemale() }

        binding.skipButton.setOnClickListener {
            findNavController().navigate(R.id.action_genderFragment_to_heightFragment)
        }

        binding.button3.setOnClickListener {
            findNavController().navigate(R.id.action_genderFragment_to_heightFragment)
        }
    }

    private fun restoreSelection() {
        when (viewModel.userProfile.value.gender) {
            "male" -> selectMale()
            "female" -> selectFemale()
        }
    }

    private fun selectMale() {
        if (selectedGender == true) {
            selectedGender = null
            viewModel.updateGender("")
            updateVisuals()
            OnboardingAnimations.deselectGender(binding.cardMale, binding.maleImage)
            return
        }

        selectedGender = true
        viewModel.updateGender("male")
        updateVisuals()

        OnboardingAnimations.selectGender(binding.cardMale, binding.maleImage)
        OnboardingAnimations.deselectGender(binding.cardFemale, binding.femaleImage)
    }

    private fun selectFemale() {
        if (selectedGender == false) {
            selectedGender = null
            viewModel.updateGender("")
            updateVisuals()
            OnboardingAnimations.deselectGender(binding.cardFemale, binding.femaleImage)
            return
        }

        selectedGender = false
        viewModel.updateGender("female")
        updateVisuals()

        OnboardingAnimations.selectGender(binding.cardFemale, binding.femaleImage)
        OnboardingAnimations.deselectGender(binding.cardMale, binding.maleImage)
    }

    /**
     * Updates card strokes instantly for immediate click feedback
     */
    private fun updateVisuals() {
        val orange = resources.getColor(R.color.orange_primary, null)
        val grayStroke = android.graphics.Color.parseColor("#DDD9E2")

        binding.cardMale.strokeColor = if (selectedGender == true) orange else grayStroke
        binding.cardMale.strokeWidth = if (selectedGender == true) 6 else 3

        binding.cardFemale.strokeColor = if (selectedGender == false) orange else grayStroke
        binding.cardFemale.strokeWidth = if (selectedGender == false) 6 else 3
    }

    private fun applyPremiumAnimations() {
        listOf(
            binding.header.backButton,
            binding.header.assessmentTitle,
            binding.header.progressTag,
            binding.header.progressBar
        ).forEachIndexed { index, view ->
            OnboardingAnimations.fadeInSlideIn(view, index)
        }

        OnboardingAnimations.fadeInSlideUp(binding.title, 150)
        OnboardingAnimations.animateGenderEntrance(binding.cardMale, binding.maleImage, 300)
        OnboardingAnimations.animateGenderEntrance(binding.cardFemale, binding.femaleImage, 400)
        OnboardingAnimations.fadeInSlideUp(binding.skipButton, 650)
        OnboardingAnimations.fadeInSlideUp(binding.button3, 730)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
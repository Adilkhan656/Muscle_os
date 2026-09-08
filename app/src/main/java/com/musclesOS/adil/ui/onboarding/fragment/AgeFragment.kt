package com.musclesOS.adil.ui.onboarding.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.musclesOS.adil.R
import com.musclesOS.adil.databinding.FragmentAgeBinding
import com.musclesOS.adil.ui.onboarding.viewmodel.OnboardingViewModel
import com.musclesOS.adil.ui.onboarding.viewmodel.OnboardingViewModelProvider
import com.musclesOS.adil.utils.animation.OnboardingAnimations
import java.util.Locale

class AgeFragment : Fragment(R.layout.fragment_age) {

    private var _binding: FragmentAgeBinding? = null
    private val binding get() = _binding!!

    private val onboardingViewModel: OnboardingViewModel by activityViewModels {
        OnboardingViewModelProvider.provideFactory(requireContext())
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAgeBinding.bind(view)

        setupHeader()
        setupScale()
        initClickListener()
        applyPremiumAnimations()
    }

    private fun setupHeader() {

        binding.header.progressTag.text = "4 of 6"
        binding.header.progressBar.progress = 400
        binding.header.assessmentTitle.text = "Age"

        binding.header.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupScale() {

        binding.ageScale.apply {

            minValue = 10.0
            maxValue = 90.0

            step = 1.0

            val savedAge = onboardingViewModel.userProfile.value.age
            value = if (savedAge > 0) savedAge.toDouble() else 25.0

            vertical = true
            inverted = false
            
            showLabels = false
            showMagnifier = false

            onValueChanged = { selected ->
                updateAgeDisplay(selected)
            }

            valueFormatter = { raw ->
                String.format(Locale.US, "%.0f", raw)
            }
        }

        updateAgeDisplay(binding.ageScale.value)
    }

    private fun applyPremiumAnimations() {
        // Header animation
        listOf(binding.header.backButton, binding.header.progressTag, binding.header.progressBar).forEachIndexed { index, view ->
            OnboardingAnimations.fadeInSlideIn(view, index)
        }

        // Slide up and fade in for the age value
        binding.ageDisplayContainer.alpha = 0f
        binding.ageDisplayContainer.translationY = 50f
        binding.ageDisplayContainer.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setStartDelay(200)
            .start()

        // Fade in for the scale
        binding.ageScale.alpha = 0f
        binding.ageScale.animate()
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(400)
            .start()
            
        // Title and subtitle stagger
        binding.title.alpha = 0f
        binding.title.translationX = -30f
        binding.title.animate().alpha(1f).translationX(0f).setDuration(500).start()
        
        binding.subtitle.alpha = 0f
        binding.subtitle.translationX = -30f
        binding.subtitle.animate().alpha(1f).translationX(0f).setDuration(500).setStartDelay(100).start()
    }

    private fun updateAgeDisplay(
        value: Double
    ) {
        binding.ageValue.text =
            String.format(
                Locale.US,
                "%.0f",
                value
            )
    }

    private fun initClickListener() {

        binding.button3.setOnClickListener {

            onboardingViewModel.updateAge(
                binding.ageScale.value.toInt()
            )

            findNavController().navigate(
                R.id.action_ageFragment_to_focusAreaFragment
            )
        }
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}

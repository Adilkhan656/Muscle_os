package com.musclesOS.adil.ui.onboarding.fragment

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.musclesOS.adil.R
import com.musclesOS.adil.databinding.FragmentWeightBinding
import com.musclesOS.adil.ui.onboarding.viewmodel.OnboardingViewModel
import com.musclesOS.adil.ui.onboarding.viewmodel.OnboardingViewModelProvider
import com.musclesOS.adil.utils.UnitConverter
import com.musclesOS.adil.utils.animation.OnboardingAnimations
import java.util.Locale

class WeightFragment : Fragment(R.layout.fragment_weight) {

    private var _binding: FragmentWeightBinding? = null
    private val binding get() = _binding!!

    private val onboardingViewModel: OnboardingViewModel by activityViewModels {
        OnboardingViewModelProvider.provideFactory(requireContext())
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentWeightBinding.bind(view)

        setupHeader()
        setupInitialWeight()
        setupUnitSelector()
        setupScale()
        initClickListener()
        applyPremiumAnimations()
    }

    private fun applyPremiumAnimations() {
        // Header animation
        listOf(binding.header.backButton, binding.header.progressTag, binding.header.progressBar).forEachIndexed { index, view ->
            OnboardingAnimations.fadeInSlideIn(view, index)
        }

        // Title, Unit Group and Value animation
        listOf(binding.title, binding.unitGroup, binding.weightValue).forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 30f
            view.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(150 + (index * 50).toLong()).start()
        }

        // Scale and Hint animation
        listOf(binding.weightScale, binding.hint).forEach { view ->
            view.alpha = 0f
            view.animate().alpha(1f).setDuration(800).setStartDelay(400).start()
        }

        // Button animation
        binding.button3.alpha = 0f
        binding.button3.animate().alpha(1f).setDuration(500).setStartDelay(600).start()
    }

    private fun setupHeader() {

        binding.header.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
//        binding.header.skip.visibility = View.VISIBLE
//        binding.header.skip.setOnClickListener { findNavController().navigate(R.id.action_weightFragment_to_ageFragment) }

        binding.header.progressTag.text = "3 of 6"
        binding.header.progressBar.progress = 300
    }

    private fun setupInitialWeight() {

        binding.kg.isChecked = true

        updateUnitChoice()
    }

    private fun setupScale() {

        binding.weightScale.apply {

            minValue = 35.0
            maxValue = 160.0

            step = 0.1

            val savedWeight = onboardingViewModel.userProfile.value.weightKg
            value = if (savedWeight > 0.0) savedWeight else 62.0
            
            unit = "kg"

            onValueChanged = { selected ->

                updateWeightDisplay(selected)
            }
            
            onReadoutClicked = {
                showManualWeightDialog()
            }
        }

        updateWeightDisplay(binding.weightScale.value)
    }

    private fun setupUnitSelector() {

        binding.unitGroup.setOnCheckedChangeListener { _, checkedId ->

            val currentValue =
                binding.weightScale.value

            if (checkedId == binding.lbs.id) {

                /*
                 * Current scale value is kg.
                 *
                 * Convert it to pounds.
                 */
                val converted =
                    UnitConverter.kgToLb(currentValue)

                binding.weightScale.apply {

                    minValue =
                        UnitConverter.kgToLb(35.0)

                    maxValue =
                        UnitConverter.kgToLb(160.0)

                    step = 0.1

                    value = converted
                }

            } else {

                /*
                 * Current scale value is pounds.
                 *
                 * Convert it back to kg.
                 */
                val converted =
                    UnitConverter.lbToKg(currentValue)

                binding.weightScale.apply {

                    minValue = 35.0

                    maxValue = 160.0

                    step = 0.1

                    value = converted
                }
            }

            updateUnitChoice()

            updateWeightDisplay(
                binding.weightScale.value
            )
        }
    }

    private fun updateWeightDisplay(
        value: Double
    ) {

        val unit =
            if (binding.lbs.isChecked) {
                "lb"
            } else {
                "kg"
            }

        binding.weightValue.text =
            String.format(
                Locale.US,
                "%.1f %s",
                value,
                unit
            )
        binding.weightScale.unit = unit
    }

    private fun updateUnitChoice() {

        binding.kg.setBackgroundResource(
            if (binding.kg.isChecked) {
                R.drawable.bg_measurement_toggle_selected
            } else {
                android.R.color.transparent
            }
        )

        binding.lbs.setBackgroundResource(
            if (binding.lbs.isChecked) {
                R.drawable.bg_measurement_toggle_selected
            } else {
                android.R.color.transparent
            }
        )

        binding.kg.setTextColor(
            resources.getColor(
                R.color.text_main,
                null
            )
        )

        binding.lbs.setTextColor(
            resources.getColor(
                R.color.text_main,
                null
            )
        )
    }

    private fun initClickListener() {

        binding.button3.setOnClickListener {

            val weightValue = if (binding.kg.isChecked) {
                binding.weightScale.value
            } else {
                UnitConverter.lbToKg(binding.weightScale.value)
            }

            onboardingViewModel.updateWeight(weightValue)

            findNavController().navigate(
                R.id.action_weightFragment_to_ageFragment
            )
        }
    }

    private fun showManualWeightDialog() {
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.hint = "Enter weight"
        
        val currentWeight = binding.weightScale.value
        input.setText(String.format(Locale.US, "%.1f", currentWeight))
        input.setSelection(input.text.length)

        AlertDialog.Builder(requireContext())
            .setTitle("Set Weight")
            .setMessage("Enter your weight in ${if (binding.lbs.isChecked) "lbs" else "kg"}")
            .setView(input)
            .setPositiveButton("Set") { _, _ ->
                val weightStr = input.text.toString()
                val weight = weightStr.toDoubleOrNull()
                if (weight != null) {
                    binding.weightScale.value = weight
                    updateWeightDisplay(weight)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}

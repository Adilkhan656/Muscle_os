package com.musclesOS.adil.ui.onboarding.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.musclesOS.adil.R
import com.musclesOS.adil.databinding.FragmentHeightBinding
import com.musclesOS.adil.utils.UnitConverter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

class HeightFragment : Fragment(R.layout.fragment_height) {

    private var _binding: FragmentHeightBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentHeightBinding.bind(view)

        setupHeader()
        setupScale()
        setupUnitSelector()
        updateUnitChoice()
        initClickListener()
        applyPremiumAnimations()
    }

    private fun applyPremiumAnimations() {
        // Header animation
        listOf(binding.header.backButton, binding.header.skip, binding.header.progress).forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationX = -24f
            view.animate().alpha(1f).translationX(0f).setDuration(300).setStartDelay((index * 50).toLong()).start()
        }

        // Title and Value animation
        listOf(binding.title, binding.heightValue, binding.heightUnitGroup).forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationX = -30f
            view.animate().alpha(1f).translationX(0f).setDuration(500).setStartDelay(150 + (index * 50).toLong()).start()
        }

        // Scale and Hint animation
        listOf(binding.heightScale, binding.hint).forEach { view ->
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
        binding.header.skip.setOnClickListener { findNavController().navigate(R.id.action_heightFragment_to_weightFragment) }
    }

    private fun setupScale() {

        binding.heightScale.apply {
            maxValue = 220.0
            minValue = 120.0

            step = 0.1
            value = 175.0

            onValueChanged = { selected ->
                updateHeightDisplay(selected)
            }
        }

        updateHeightDisplay(binding.heightScale.value)
    }

    private fun setupUnitSelector() {

        binding.cm.isChecked = true

        binding.heightUnitGroup.setOnCheckedChangeListener { _, checkedId ->

            val currentValue = binding.heightScale.value

            if (checkedId == binding.ftIn.id) {

                // cm -> total inches
                val converted =
                    UnitConverter.cmToInches(currentValue)

                binding.heightScale.apply {
                    maxValue = UnitConverter.cmToInches(220.0)
                    minValue = UnitConverter.cmToInches(120.0)

                    step = 0.1
                    value = converted
                }

            } else {

                // total inches -> cm
                val converted =
                    UnitConverter.inchesToCm(currentValue)

                binding.heightScale.apply {
                    maxValue = 220.0
                    minValue = 120.0

                    step = 0.1
                    value = converted
                }
            }

            updateUnitChoice()
            updateHeightDisplay(binding.heightScale.value)
        }
    }

    private fun updateHeightDisplay(value: Double) {

        if (binding.cm.isChecked) {

            binding.heightValue.text = String.format(
                Locale.US,
                "%.1f cm",
                value
            )

            binding.heightScale.displayText = binding.heightValue.text.toString()

        } else {

            val feet = (value / 12).toInt()
            val inches = value - (feet * 12)

            binding.heightValue.text = String.format(
                Locale.US,
                "%d' %.1f\"",
                feet,
                inches
            )
            binding.heightScale.displayText = binding.heightValue.text.toString()
        }
    }

    private fun updateUnitChoice() {

        binding.cm.setBackgroundResource(
            if (binding.cm.isChecked) {
                R.drawable.bg_measurement_toggle_selected
            } else {
                android.R.color.transparent
            }
        )

        binding.ftIn.setBackgroundResource(
            if (binding.ftIn.isChecked) {
                R.drawable.bg_measurement_toggle_selected
            } else {
                android.R.color.transparent
            }
        )

        binding.cm.setTextColor(
            resources.getColor(
                R.color.text_main,
                null
            )
        )

        binding.ftIn.setTextColor(
            resources.getColor(
                R.color.text_main,
                null
            )
        )
    }

    private fun initClickListener() {

        binding.button3.setOnClickListener {

            findNavController().navigate(
                R.id.action_heightFragment_to_weightFragment
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

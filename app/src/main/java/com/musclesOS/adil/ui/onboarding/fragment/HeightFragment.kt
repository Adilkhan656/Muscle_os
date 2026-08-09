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
    }

    private fun setupHeader() {

        binding.header.progress.text = "2 of 5"

        binding.header.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupScale() {

        binding.heightScale.apply {
            maxValue = 220.0
            minValue = 120.0

            step = 0.1
            value = 175.0

            vertical = true
            inverted = false

            onValueChanged = { selected ->
                updateHeightDisplay(selected)
            }

            valueFormatter = { raw ->
                if (binding.ftIn.isChecked) {
                    val feet = (raw / 12).toInt()
                    val inches = raw - (feet * 12)
                    
                    if (abs(inches - inches.roundToInt()) < 0.01) {
                        String.format(Locale.US, "%d' %d\"", feet, inches.roundToInt())
                    } else {
                        String.format(Locale.US, "%d' %.1f\"", feet, inches)
                    }
                } else {
                    String.format(Locale.US, "%.0f", raw)
                }
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

        } else {

            val feet = (value / 12).toInt()
            val inches = value - (feet * 12)

            binding.heightValue.text = String.format(
                Locale.US,
                "%d' %.1f\"",
                feet,
                inches
            )
        }
    }

    private fun updateUnitChoice() {

        binding.cm.setBackgroundResource(
            if (binding.cm.isChecked) {
                R.drawable.bg_onboarding_choice_selected
            } else {
                R.drawable.bg_onboarding_choice
            }
        )

        binding.ftIn.setBackgroundResource(
            if (binding.ftIn.isChecked) {
                R.drawable.bg_onboarding_choice_selected
            } else {
                R.drawable.bg_onboarding_choice
            }
        )

        binding.cm.setTextColor(
            resources.getColor(
                if (binding.cm.isChecked) {
                    R.color.white
                } else {
                    R.color.text_main
                },
                null
            )
        )

        binding.ftIn.setTextColor(
            resources.getColor(
                if (binding.ftIn.isChecked) {
                    R.color.white
                } else {
                    R.color.text_main
                },
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
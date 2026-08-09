package com.musclesOS.adil.ui.onboarding.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.musclesOS.adil.R
import com.musclesOS.adil.databinding.FragmentWeightBinding
import com.musclesOS.adil.utils.UnitConverter
import java.util.Locale

class WeightFragment : Fragment(R.layout.fragment_weight) {

    private var _binding: FragmentWeightBinding? = null
    private val binding get() = _binding!!

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
    }

    private fun setupHeader() {

        binding.header.progress.text = "3 of 5"

        binding.header.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
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

            value = 62.0

            vertical = false
            inverted = false

            onValueChanged = { selected ->

                updateWeightDisplay(selected)
            }

            valueFormatter = { raw ->
                String.format(Locale.US, "%.1f", raw)
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
    }

    private fun updateUnitChoice() {

        binding.kg.setBackgroundResource(
            if (binding.kg.isChecked) {
                R.drawable.bg_onboarding_choice_selected
            } else {
                R.drawable.bg_onboarding_choice
            }
        )

        binding.lbs.setBackgroundResource(
            if (binding.lbs.isChecked) {
                R.drawable.bg_onboarding_choice_selected
            } else {
                R.drawable.bg_onboarding_choice
            }
        )

        binding.kg.setTextColor(
            resources.getColor(
                if (binding.kg.isChecked) {
                    R.color.white
                } else {
                    R.color.text_main
                },
                null
            )
        )

        binding.lbs.setTextColor(
            resources.getColor(
                if (binding.lbs.isChecked) {
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
                R.id.action_weightFragment_to_focusAreaFragment
            )
        }
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}
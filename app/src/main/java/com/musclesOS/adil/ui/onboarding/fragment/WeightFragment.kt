package com.musclesOS.adil.ui.onboarding.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.musclesOS.adil.R
import com.musclesOS.adil.databinding.FragmentWeightBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class WeightFragment : Fragment() {
private var _binding : FragmentWeightBinding?=null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_weight, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWeightBinding.bind(view)
        binding.header.progress.text = "3 of 5"
        binding.header.backButton.setOnClickListener { findNavController().navigateUp() }
        binding.kg.isChecked = true
        fun updateUnitChoice() {
            binding.kg.setBackgroundResource(if (binding.kg.isChecked) R.drawable.bg_onboarding_choice_selected else R.drawable.bg_onboarding_choice)
            binding.lbs.setBackgroundResource(if (binding.lbs.isChecked) R.drawable.bg_onboarding_choice_selected else R.drawable.bg_onboarding_choice)
            binding.kg.setTextColor(resources.getColor(if (binding.kg.isChecked) R.color.white else R.color.text_main, null))
            binding.lbs.setTextColor(resources.getColor(if (binding.lbs.isChecked) R.color.white else R.color.text_main, null))
        }
        updateUnitChoice()
        binding.weightScale.apply {
            minValue = 35
            maxValue = 160
            value = 62
            vertical = false
            inverted = false
            onValueChanged = { selected -> binding.weightValue.text = "$selected ${if (binding.lbs.isChecked) "lb" else "kg"}" }
        }
        binding.unitGroup.setOnCheckedChangeListener { _, _ ->
            binding.weightValue.text = "${binding.weightScale.value} ${if (binding.lbs.isChecked) "lb" else "kg"}"
            updateUnitChoice()
        }
        initClicklistner()
    }

    private fun initClicklistner(){
        binding.button3.setOnClickListener {
            findNavController().navigate(R.id.action_weightFragment_to_focusAreaFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    }

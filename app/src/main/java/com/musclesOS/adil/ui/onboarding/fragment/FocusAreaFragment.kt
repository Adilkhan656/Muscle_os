package com.musclesOS.adil.ui.onboarding.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.musclesOS.adil.R
import com.musclesOS.adil.databinding.FragmentFocusAreaBinding

class FocusAreaFragment : Fragment(R.layout.fragment_focus_area) {

private var _binding : FragmentFocusAreaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_focus_area, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFocusAreaBinding.bind(view)
        binding.header.progress.text = "4 of 5"
        binding.header.backButton.setOnClickListener { findNavController().navigateUp() }
        initClicklistner()
    }
    private fun initClicklistner(){
        val options = listOf(binding.focusStrength, binding.focusCardio, binding.focusMobility, binding.focusWhole)
        options.forEach { option -> option.setOnClickListener {
            options.forEach { it.setBackgroundResource(R.drawable.bg_onboarding_choice) }
            option.setBackgroundResource(R.drawable.bg_onboarding_choice_selected)
        } }
        binding.button3.setOnClickListener {
            findNavController().navigate(R.id.action_focusAreaFragment_to_goalFragment)
        }
    }

    override fun onDestroyView() {

        super.onDestroyView()
        _binding = null
    }
}

package com.musclesOS.adil.ui.onboarding.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.musclesOS.adil.R
import com.musclesOS.adil.databinding.FragmentHeightBinding


class HeightFragment : Fragment(R.layout.fragment_height) {
private var _binding : FragmentHeightBinding? = null
    private val  binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_height, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHeightBinding.bind(view)
        binding.header.progress.text = "2 of 5"
        binding.header.backButton.setOnClickListener { findNavController().navigateUp() }
        binding.heightScale.apply {
            minValue = 120
            maxValue = 220
            value = 175
            vertical = true
            inverted = false
            onValueChanged = { selected -> binding.heightValue.text = "$selected cm" }
        }
initClicklistner()
    }
    private fun initClicklistner(){
        binding.button3.setOnClickListener {
            findNavController().navigate(R.id.action_heightFragment_to_weightFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

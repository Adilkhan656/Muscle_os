package com.musclesOS.adil.ui.onboarding.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.musclesOS.adil.R
import com.musclesOS.adil.databinding.FragmentGenderBinding


class GenderFragment : Fragment(R.layout.fragment_gender) {
private var _binding : FragmentGenderBinding?= null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGenderBinding.bind(view)
        
        binding.backButton.setOnClickListener { requireActivity().finish() }
        initClicklistner()
        applyPremiumAnimations()
    }

    private fun applyPremiumAnimations() {
        // Header animation
        listOf(binding.backButton, binding.assessmentTitle, binding.progress).forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationX = -24f
            view.animate().alpha(1f).translationX(0f).setDuration(300).setStartDelay((index * 50).toLong()).start()
        }

        // Title animation
        binding.title.alpha = 0f
        binding.title.translationX = -30f
        binding.title.animate().alpha(1f).translationX(0f).setDuration(500).setStartDelay(150).start()

        // Cards animation
        listOf(binding.cardMale, binding.cardFemale).forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 40f
            view.animate().alpha(1f).translationY(0f).setDuration(600).setStartDelay(300 + (index * 100).toLong()).start()
        }

        // Bottom buttons animation
        listOf(binding.skipButton, binding.button3).forEach { view ->
            view.alpha = 0f
            view.animate().alpha(1f).setDuration(500).setStartDelay(600).start()
        }
    }

private fun initClicklistner (){
    fun select(male: Boolean) {
        val selected = if (male) R.color.orange_primary else R.color.field_bg
        val unselected = if (male) R.color.field_bg else R.color.orange_primary
        binding.cardMale.setCardBackgroundColor(resources.getColor(selected, null))
        binding.cardFemale.setCardBackgroundColor(resources.getColor(unselected, null))
        binding.maleCheck.visibility = if (male) View.VISIBLE else View.GONE
        binding.femaleCheck.visibility = if (male) View.GONE else View.VISIBLE
    }
    binding.cardMale.setOnClickListener { select(true) }
    binding.cardFemale.setOnClickListener { select(false) }
    binding.skipButton.setOnClickListener { findNavController().navigate(R.id.action_genderFragment_to_heightFragment) }
    binding.button3.setOnClickListener {
        findNavController().navigate(R.id.action_genderFragment_to_heightFragment)
    }
}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

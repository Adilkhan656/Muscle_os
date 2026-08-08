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
        listOf(binding.backButton, binding.assessmentTitle, binding.progress).forEachIndexed { index, headerView ->
            headerView.translationX = -24f
            headerView.alpha = 0f
            headerView.animate().translationX(0f).alpha(1f).setStartDelay((index * 70).toLong()).setDuration(280).start()
        }
        binding.backButton.setOnClickListener { requireActivity().finish() }
        initClicklistner()
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

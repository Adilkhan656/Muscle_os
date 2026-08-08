package com.musclesOS.adil.ui.onboarding.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.musclesOS.adil.R
import com.musclesOS.adil.databinding.FragmentGoalBinding


class GoalFragment : Fragment(R.layout.fragment_goal) {
 private var _binding : FragmentGoalBinding? = null
    private val binding get() = _binding!!



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGoalBinding.bind(view)
        binding.header.progress.text = "5 of 5"
        binding.header.backButton.setOnClickListener { findNavController().navigateUp() }
        initClicklistner()
    }
    private fun initClicklistner(){
        val options = listOf(binding.goalLose, binding.goalCoach, binding.goalBulk, binding.goalEndurance)
        options.forEach { option -> option.setOnClickListener {
            options.forEach { it.setBackgroundResource(R.drawable.bg_onboarding_choice) }
            option.setBackgroundResource(R.drawable.bg_onboarding_choice_selected)
        } }
        binding.button3.setOnClickListener {
            findNavController().navigate(R.id.action_goalFragment_to_finishFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

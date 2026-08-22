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
        binding.header.progress.text = "6 of 6"
        binding.header.backButton.setOnClickListener { findNavController().navigateUp() }
        initClicklistner()
        applyPremiumAnimations()
    }

    private fun applyPremiumAnimations() {
        // Header animation
        listOf(binding.header.backButton, binding.header.assessmentTitle, binding.header.progress).forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationX = -24f
            view.animate().alpha(1f).translationX(0f).setDuration(300).setStartDelay((index * 50).toLong()).start()
        }

        // Title animation
        binding.title.alpha = 0f
        binding.title.translationX = -30f
        binding.title.animate().alpha(1f).translationX(0f).setDuration(500).setStartDelay(150).start()

        // Goal options animation (grid items)
        listOf(binding.goalLose, binding.goalCoach, binding.goalBulk, binding.goalEndurance).forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 40f
            view.animate().alpha(1f).translationY(0f).setDuration(600).setStartDelay(300 + (index * 80).toLong()).start()
        }

        // Button animation
        binding.button3.alpha = 0f
        binding.button3.animate().alpha(1f).setDuration(500).setStartDelay(700).start()
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

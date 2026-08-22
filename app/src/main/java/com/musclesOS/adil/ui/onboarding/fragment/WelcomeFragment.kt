package com.musclesOS.adil.ui.onboarding.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.musclesOS.adil.R
import com.musclesOS.adil.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment(R.layout.fragment_welcome) {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentWelcomeBinding.bind(view)

        initClickListeners()
        applyPremiumAnimations()
    }

    private fun applyPremiumAnimations() {
        binding.button3.alpha = 0f
        binding.button3.translationY = 30f
        binding.button3.animate().alpha(1f).translationY(0f).setDuration(600).setStartDelay(300).start()
    }

    private fun initClickListeners() {

        binding.button3.setOnClickListener {

            findNavController().navigate(
                R.id.action_welcomeFragment_to_genderFragment
            )

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
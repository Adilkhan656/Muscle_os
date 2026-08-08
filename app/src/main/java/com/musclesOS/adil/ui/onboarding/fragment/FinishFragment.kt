package com.musclesOS.adil.ui.onboarding.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.musclesOS.adil.MainActivity
import com.musclesOS.adil.R
import com.musclesOS.adil.databinding.FragmentFinishBinding

class FinishFragment : Fragment() {

private var _binding : FragmentFinishBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_finish, container, false)
    }
    private fun navigateToHome() {

        val intent = Intent(requireContext(), MainActivity::class.java)

        startActivity(intent)

        requireActivity().finish()

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFinishBinding.bind(view)
        binding.button3.setOnClickListener {
            navigateToHome()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.musclesOS.adil.ui.onboarding.activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import com.musclesOS.adil.R
import com.musclesOS.adil.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        val showWave = intent.getBooleanExtra("SHOW_WAVE", false)
        if (savedInstanceState == null && showWave) {
            keepOrangeSystemBarsForReveal()
            runWaveReveal()
        } else {
            showOnboardingSystemBars()
        }
    }

    private fun runWaveReveal() {
        binding.onboardingRoot.doOnPreDraw {
            binding.waveRevealView.setBehindColor(ContextCompat.getColor(this, R.color.orange_primary))
            binding.waveRevealView.reveal(duration = 1100, isReveal = true) {
                binding.waveRevealView.visibility = View.GONE
                showOnboardingSystemBars()
            }
        }
    }

    private fun keepOrangeSystemBarsForReveal() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
    }

    private fun showOnboardingSystemBars() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
    }

    override fun onResume() {
        super.onResume()
        com.musclesOS.adil.OneSignalManager.promptNotificationPermissionIfDaily(this)
    }
}

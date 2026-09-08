package com.musclesOS.adil

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.doOnPreDraw
import com.musclesOS.adil.databinding.ActivityMainBinding
import com.musclesOS.adil.repository.AuthRepository
import com.musclesOS.adil.ui.auth.LoginActivity
import com.musclesOS.adil.ui.profile.ProfileActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null && intent.getBooleanExtra("SHOW_WAVE", false)) {
            keepOrangeSystemBarsForReveal()
            runWaveReveal()
        }

        binding.profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.button2.setOnClickListener { signOut() }
    }

    override fun onResume() {
        super.onResume()
        OneSignalManager.promptNotificationPermissionIfDaily(this)
    }

    private fun signOut() {
        AuthRepository().signOut()
        OneSignalManager.logout()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun runWaveReveal() {
        binding.main.doOnPreDraw {
            binding.waveRevealView.setBehindColor(ContextCompat.getColor(this, R.color.orange_primary))
            binding.waveRevealView.reveal(duration = 1100, isReveal = true) {
                binding.waveRevealView.visibility = View.GONE
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = true
                    isAppearanceLightNavigationBars = true
                }
            }
        }
    }

    private fun keepOrangeSystemBarsForReveal() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
    }
}

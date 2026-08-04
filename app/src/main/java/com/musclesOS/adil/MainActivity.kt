package com.musclesOS.adil

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.doOnPreDraw
import com.musclesOS.adil.ui.auth.LoginActivity
import com.musclesOS.adil.databinding.ActivityMainBinding
import com.musclesOS.adil.repository.AuthRepository

/**
 * The main dashboard activity for the application.
 * Handles the authenticated user experience and overall app navigation.
 */
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

        binding.button2.setOnClickListener {
            AuthRepository().signOut()

            startActivity(
                Intent(
                    this,
                    LoginActivity::class.java
                )
            )

            finish()
        }
    }

    /**
     * Executes the wave reveal animation when navigating from the Splash screen.
     */
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

    /**
     * Maintains the orange theme for system bars during the reveal transition.
     */
    private fun keepOrangeSystemBarsForReveal() {
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
    }
}

package com.musclesOS.adil.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.content.ContextCompat
import com.musclesOS.adil.MainActivity
import com.musclesOS.adil.R
import com.musclesOS.adil.core.AppDestination
import com.musclesOS.adil.core.AppInitializer
import com.musclesOS.adil.ui.auth.LoginActivity
import com.musclesOS.adil.ui.auth.RegisterScreen
import com.musclesOS.adil.ui.onboarding.activity.OnboardingActivity
import com.musclesOS.adil.databinding.ActivityWelcomeScreenBinding
import com.musclesOS.adil.repository.AuthRepository
import com.musclesOS.adil.ui.auth.viewmodel.AuthViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/**
 * The initial screen shown to unauthenticated users.
 * Provides options to Register, Login, or continue as a guest.
 * Handles the entry wave animation from the splash screen.
 */
class WelcomeScreen : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivityWelcomeScreenBinding
    private val repository by lazy {
        AuthRepository()
    }

    private val viewModel by lazy {
        AuthViewModel(repository)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWelcomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val showWave = intent.getBooleanExtra("SHOW_WAVE", false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.welcomeContent) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Do not pad the full-screen root: with edge-to-edge enabled that would expose its
            // white background beneath the transparent status bar while the orange wave runs.
            // The logo already has ample top spacing; only the bottom controls need the inset.
            binding.bottomLayout.setPadding(
                binding.bottomLayout.paddingLeft,
                binding.bottomLayout.paddingTop,
                binding.bottomLayout.paddingRight,
                systemBars.bottom
            )
            insets
        }

        if (savedInstanceState == null && showWave) {
            keepOrangeSystemBarsForReveal()
            runWaveReveal()
        }else{
            showWelcomeSystemBars()
        }

        binding.btnGetStarted.setOnClickListener(this)
        binding.tvSignIn.setOnClickListener(this)
    }

    /**
     * Triggers the wave reveal animation that sweeps away the orange splash color.
     */
    private fun runWaveReveal() {
        // Start before the first welcome frame is drawn. Posting this work can allow the
        // white welcome background to be visible for one frame before the orange wave appears.
        binding.welcomeContent.doOnPreDraw {
            binding.waveRevealView.setBehindColor(ContextCompat.getColor(this, R.color.orange_primary))
            binding.waveRevealView.reveal(duration = 1100, isReveal = true) {
                binding.waveRevealView.visibility = View.GONE
                showWelcomeSystemBars()
            }
        }
    }

    /**
     * configures the system bars to remain orange/dark during the transition reveal.
     */
    private fun keepOrangeSystemBarsForReveal() {
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
    }

    /**
     * Resets system bars to standard light mode with dark icons for the white background.
     */
    private fun showWelcomeSystemBars() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
    }

    /**
     * Navigates to the appropriate activity after successful login.
     */
    private fun openMain() {
        lifecycleScope.launch {
            val destination = AppInitializer(this@WelcomeScreen).initialize()
            val intent = when (destination) {
                AppDestination.Main -> Intent(this@WelcomeScreen, MainActivity::class.java)
                AppDestination.Onboarding -> Intent(this@WelcomeScreen, OnboardingActivity::class.java)
                else -> Intent(this@WelcomeScreen, OnboardingActivity::class.java)
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onStart() {

        super.onStart()

        if (viewModel.isLoggedIn()) {

            openMain()

        }

    }
    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btnGetStarted -> {
                startActivity(Intent(
                    this@WelcomeScreen,
                    RegisterScreen::class.java
                ))
                finish()
            }
            R.id.tvSignIn ->{
                startActivity(Intent(
                    this@WelcomeScreen,
                    LoginActivity::class.java
                ))
                finish()
            }
        }
    }
}

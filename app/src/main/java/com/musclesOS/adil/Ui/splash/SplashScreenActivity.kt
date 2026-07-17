package com.musclesOS.adil.Ui.splash

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.musclesOS.adil.MainActivity
import com.musclesOS.adil.Ui.WelcomeScreen
import com.musclesOS.adil.core.AppDestination
import com.musclesOS.adil.databinding.ActivitySplashScreenBinding
import kotlinx.coroutines.launch

/**
 * The entry point of the application.
 * Displays branding animations and determines the initial destination (Welcome or Main).
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private val viewModel: SplashViewModel by viewModels()
    private var navigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, com.musclesOS.adil.R.color.orange_primary)
        window.navigationBarColor = ContextCompat.getColor(this, com.musclesOS.adil.R.color.orange_primary)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playEntranceAnimation()
        observeState()
    }

    /**
     * Plays the initial scale and fade animations for the logo and tagline.
     */
    private fun playEntranceAnimation() {
        binding.ivLogo.animate()
            .alpha(1f).scaleX(1f).scaleY(1f)
            .setDuration(650)
            .setInterpolator(OvershootInterpolator(1.1f))
            .setStartDelay(150)
            .start()

        binding.tvTagline.animate()
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(550)
            .start()

        binding.tvBrand.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(450)
            .setStartDelay(420)
            .start()
    }

    /**
     * Observes the ViewModel state to trigger navigation once loading/auth check is complete.
     */
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is SplashState.Ready -> if (!navigated) startExitSequence(state.destination)
                        is SplashState.Error -> if (!navigated) startExitSequence(AppDestination.Welcome)
                        SplashState.Loading -> Unit
                    }
                }
            }
        }
    }

    /**
     * Starts the transition to the next activity with a seamless handoff for the wave animation.
     */
    private fun startExitSequence(destination: AppDestination) {
        navigated = true

        binding.contentContainer.animate()
            .alpha(0f).scaleX(0.92f).scaleY(0.92f)
            .setDuration(400)
            .withEndAction {
                val intent = when (destination) {
                    AppDestination.Main -> Intent(this, MainActivity::class.java).apply {
                        putExtra("SHOW_WAVE", true)
                    }
                    AppDestination.Welcome -> Intent(this, WelcomeScreen::class.java).apply {
                        putExtra("SHOW_WAVE", true)
                    }
                }
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()
            }
            .start()
    }
}

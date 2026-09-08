package com.musclesOS.adil

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.doOnPreDraw
import com.musclesOS.adil.databinding.ActivityMainBinding
import com.musclesOS.adil.ui.main.GoalsFragment
import com.musclesOS.adil.ui.main.HomeFragment
import com.musclesOS.adil.ui.main.TrackingFragment
import com.musclesOS.adil.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null && intent.getBooleanExtra("SHOW_WAVE", false)) {
            runWaveReveal()
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            showDestination(item.itemId)
            true
        }

        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.navigation_profile
        }
    }

    override fun onResume() {
        super.onResume()
        OneSignalManager.promptNotificationPermissionIfDaily(this)
    }

    private fun runWaveReveal() {
        binding.main.doOnPreDraw {
            binding.waveRevealView.setBehindColor(ContextCompat.getColor(this, R.color.orange_primary))
            binding.waveRevealView.reveal(duration = 1100, isReveal = true) {
                binding.waveRevealView.visibility = View.GONE
            }
        }
    }

    private fun showDestination(itemId: Int) {
        val fragment = when (itemId) {
            R.id.navigation_home -> HomeFragment()
            R.id.navigation_tracking -> TrackingFragment()
            R.id.navigation_goals -> GoalsFragment()
            else -> ProfileFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainContent, fragment)
            .commit()
    }
}

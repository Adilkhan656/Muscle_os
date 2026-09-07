package com.musclesOS.adil.ui.onboarding.fragment

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.musclesOS.adil.MainActivity
import com.musclesOS.adil.OneSignalManager
import com.musclesOS.adil.R
import com.musclesOS.adil.databinding.FragmentGoalBinding
import com.musclesOS.adil.ui.onboarding.viewmodel.OnboardingViewModel
import com.musclesOS.adil.ui.onboarding.viewmodel.OnboardingViewModelProvider
import com.musclesOS.adil.utils.animation.OnboardingAnimations

class GoalFragment : Fragment(R.layout.fragment_goal) {

    private var _binding: FragmentGoalBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OnboardingViewModel by activityViewModels { OnboardingViewModelProvider.provideFactory(requireContext()) }
    private val selectedGoalIds = mutableListOf<String>()
    private val floatingAnimators = mutableMapOf<String, ObjectAnimator>()
    private lateinit var goalItems: List<View>

    private val goalData = listOf(
        GoalData("fat_loss", "Fat Loss", R.drawable.goal_fat_loss),
        GoalData("muscle_gain", "Muscle Gain", R.drawable.goal_muscle_gain),
        GoalData("strength", "Strength", R.drawable.goal_strength),
        GoalData("endurance", "Endurance", R.drawable.goal_endurance),
        GoalData("recomposition", "Recomposition", R.drawable.goal_recomposition),
        GoalData("fitness", "Stay Fit", R.drawable.goal_fitness)
    )

    data class GoalData(val id: String, val title: String, val imageRes: Int)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGoalBinding.bind(view)
        setupHeader(); setupGoals(); restoreSelection(); setupContinueButton(); handleKeyboardVisibility()
        binding.goalRoot.post { applyEntranceAnimations() }
    }

    private fun setupHeader() {
        binding.header.progressTag.text = "6 of 6"
        binding.header.progressBar.progress = 600
        binding.header.backButton.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            findNavController().navigateUp()
        }
    }

    private fun setupGoals() {
        goalItems = listOf(binding.goalFatLoss.root, binding.goalMuscleGain.root, binding.goalStrength.root, binding.goalEndurance.root, binding.goalRecomposition.root, binding.goalFitness.root)
        goalItems.forEachIndexed { index, itemView ->
            val data = goalData[index]
            val title = itemView.findViewById<TextView>(R.id.goalTitle)
            val image = itemView.findViewById<ImageView>(R.id.goalImage)
            val minus = itemView.findViewById<View>(R.id.minusContainer)
            val border = itemView.findViewById<View>(R.id.selectionBorder)
            title.text = data.title; image.setImageResource(data.imageRes); border.alpha = 0f; minus.alpha = 0f
            itemView.setOnClickListener { it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY); toggleGoal(data.id, itemView) }
            minus.apply {
                isClickable = true; isFocusable = true
                setOnClickListener {
                    it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    if (selectedGoalIds.contains(data.id)) deselectGoal(data.id, itemView)
                }
            }
        }
    }

    private fun toggleGoal(id: String, itemView: View) {
        if (selectedGoalIds.contains(id)) deselectGoal(id, itemView)
        else { if (selectedGoalIds.size >= 3) return; selectGoal(id, itemView) }
    }

    private fun selectGoal(id: String, itemView: View) { selectedGoalIds.add(id); moveGoalUp(id, itemView) }

    private fun deselectGoal(id: String, itemView: View) {
        selectedGoalIds.remove(id); stopFloating(id); OnboardingAnimations.animateGoalDeselection(itemView)
    }

    private fun moveGoalUp(id: String, itemView: View) {
        stopFloating(id)
        binding.featuredRow.post {
            val itemLocation = IntArray(2); itemView.getLocationInWindow(itemLocation)
            val targetLocation = IntArray(2); binding.featuredRow.getLocationInWindow(targetLocation)
            val itemCenterY = itemLocation[1] + itemView.height / 2f
            val targetCenterY = targetLocation[1] + binding.featuredRow.height / 2f
            val moveY = targetCenterY - itemCenterY
            OnboardingAnimations.animateGoalSelection(itemView, moveY) { startFloating(id, itemView, moveY) }
        }
    }

    private fun startFloating(id: String, view: View, baseY: Float) {
        stopFloating(id); floatingAnimators[id] = OnboardingAnimations.createFloatingAnimator(view, baseY)
    }

    private fun stopFloating(id: String) { floatingAnimators[id]?.cancel(); floatingAnimators.remove(id) }

    private fun setupContinueButton() {
        binding.button3.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            if (selectedGoalIds.isEmpty()) return@setOnClickListener
            viewModel.updateGoals(selectedGoalIds.toList())
            viewModel.updateCustomGoal(binding.etGoalDescription.text.toString())
            saveAndNavigateToHome()
        }
    }

    private fun saveAndNavigateToHome() {
        binding.button3.isEnabled = false

        // Do not mark onboarding complete until the profile is actually persisted.
        // The OneSignal tag is updated only after the Firestore save succeeds.
        viewModel.saveOnboarding(
            onSuccess = {
                OneSignalManager.addTag("onboarding_completed", "true")
                val intent = android.content.Intent(requireContext(), MainActivity::class.java).apply {
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                requireActivity().finish()
            },
            onError = { e ->
                binding.button3.isEnabled = true
                Toast.makeText(requireContext(), "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun restoreSelection() {
        val savedGoals = viewModel.userProfile.value.goals
        val savedCustomGoal = viewModel.userProfile.value.customGoal
        if (savedGoals.isNotEmpty()) savedGoals.forEach { goalId ->
            val index = goalData.indexOfFirst { it.id == goalId }
            if (index != -1) selectGoal(goalId, goalItems[index])
        }
        if (savedCustomGoal.isNotEmpty()) binding.etGoalDescription.setText(savedCustomGoal)
    }

    private fun handleKeyboardVisibility() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.goalRoot) { _, insets ->
            val keyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val keyboardHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            binding.bottomBar.animate().translationY(if (keyboardVisible) -keyboardHeight.toFloat() + 40f else 0f).setDuration(250).start()
            insets
        }
    }

    private fun applyEntranceAnimations() {
        OnboardingAnimations.applyGoalEntranceAnimations(
            headers = listOf(binding.header.backButton, binding.header.progressTag, binding.header.progressBar),
            subtitle = binding.subtitle, goalItems = goalItems, bottomBar = binding.bottomBar
        )
    }

    override fun onDestroyView() {
        floatingAnimators.values.forEach { it.cancel() }; floatingAnimators.clear()
        super.onDestroyView(); _binding = null
    }
}

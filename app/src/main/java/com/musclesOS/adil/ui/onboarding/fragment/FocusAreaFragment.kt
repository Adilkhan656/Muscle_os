package com.musclesOS.adil.ui.onboarding.fragment

import android.graphics.PointF
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.musclesOS.adil.R
import com.musclesOS.adil.data.MuscleOption
import com.musclesOS.adil.data.MuscleOptions
import com.musclesOS.adil.databinding.FragmentFocusAreaBinding
import com.musclesOS.adil.utils.animation.OnboardingAnimations

class FocusAreaFragment : Fragment(R.layout.fragment_focus_area) {

    private var _binding: FragmentFocusAreaBinding? = null
    private val binding get() = _binding!!

    private val selectedIds = mutableSetOf<String>()
    private val rowViews = mutableMapOf<String, View>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_focus_area, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFocusAreaBinding.bind(view)

        setupHeader()
        binding.header.backButton.setOnClickListener { findNavController().navigateUp() }

        buildChecklist()

        binding.bodyImageContainer.post { configureBodyOverlay() }

        binding.button3.setOnClickListener {
            findNavController().navigate(R.id.action_focusAreaFragment_to_goalFragment)
        }
        applyPremiumAnimations()
    }

    private fun setupHeader() {
        binding.header.progressTag.text = "5 of 6"
        binding.header.progressBar.progress = 500
    }

    private fun buildChecklist() {
        val inflater = LayoutInflater.from(requireContext())
        MuscleOptions.ALL.forEach { option ->
            val row = inflater.inflate(R.layout.item_muscle_option, binding.optionsList, false)
            row.findViewById<TextView>(R.id.optionLabel).text = option.displayName
            row.setOnClickListener { 
                it.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                toggle(option) 
            }

            binding.optionsList.addView(row)
            rowViews[option.id] = row
        }
    }

    private fun configureBodyOverlay() {
        val container = binding.bodyImageContainer
        val drawable = binding.bodyImage.drawable ?: return
        val scale = maxOf(container.width.toFloat() / drawable.intrinsicWidth, container.height.toFloat() / drawable.intrinsicHeight)
        val displayedWidth = drawable.intrinsicWidth * scale
        val displayedHeight = drawable.intrinsicHeight * scale
        
        val left = (container.width - displayedWidth) / 2f
        val top = (container.height - displayedHeight) / 2f
        
        binding.bodyFocusOverlay.setImageBounds(left, top, displayedWidth, displayedHeight)

        // Clear existing hotspots before adding new ones (prevents duplicates on re-layout)
        container.removeAllViews()
        container.addView(binding.bodyImage)
        container.addView(binding.bodyFocusOverlay)

        // Add invisible clickable hotspots over the body image
        MuscleOptions.ALL.filter { it.hasBodyDot }.forEach { option ->
            val hotspot = View(requireContext()).apply {
                // Base the hotspot size on the container width, not the giant cropped image width
                val baseSize = container.width * 0.15f
                val size = (baseSize * option.glowScale).toInt()
                
                layoutParams = FrameLayout.LayoutParams(size, size)
                x = left + option.xBias * displayedWidth - size / 2f
                y = top + option.yBias * displayedHeight - size / 2f
                
                val outValue = android.util.TypedValue()
                context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
                setBackgroundResource(outValue.resourceId)
                
                setOnClickListener { 
                    it.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    toggle(option) 
                }
            }
            container.addView(hotspot)
        }
    }

    private fun toggle(option: MuscleOption) {
        if (option.id == "full_body") {
            if (selectedIds.contains("full_body")) {
                MuscleOptions.ALL.forEach(::clearSelection)
            } else {
                MuscleOptions.ALL.forEach(::select)
            }
            return
        }
        if (selectedIds.contains("full_body")) {
            MuscleOptions.ALL.forEach(::clearSelection)
        }
        if (selectedIds.contains(option.id)) clearSelection(option) else select(option)
    }

    private fun select(option: MuscleOption) {
        val row = rowViews[option.id] ?: return
        val checkIcon = row.findViewById<TextView>(R.id.checkIcon)
        selectedIds.add(option.id)
        
        row.setBackgroundResource(R.drawable.bg_option_row_selected)
        checkIcon.text = "✓"
        checkIcon.setBackgroundResource(R.drawable.bg_checkbox_checked)
        
        binding.bodyFocusOverlay.setHighlights(MuscleOptions.ALL.filter { selectedIds.contains(it.id) })
        
        if (option.hasBodyDot) {
            val from = pointOnOverlayFor(option)
            val to = pointOnOverlayFor(row, useCenter = false)
            binding.calloutOverlay.showLine(option.id, from, to)
        }
    }

    private fun clearSelection(option: MuscleOption) {
        selectedIds.remove(option.id)
        rowViews[option.id]?.apply {
            setBackgroundResource(R.drawable.bg_option_row_unselected)
            findViewById<TextView>(R.id.checkIcon).apply {
                text = ""
                setBackgroundResource(R.drawable.bg_checkbox_unchecked)
            }
        }
        binding.calloutOverlay.hideLine(option.id)
        binding.bodyFocusOverlay.setHighlights(MuscleOptions.ALL.filter { selectedIds.contains(it.id) })
    }

    private fun pointOnOverlayFor(target: View, useCenter: Boolean): PointF {
        val targetLocation = IntArray(2)
        target.getLocationOnScreen(targetLocation)
        val overlayLocation = IntArray(2)
        binding.calloutOverlay.getLocationOnScreen(overlayLocation)
        val x = if (useCenter) targetLocation[0] - overlayLocation[0] + target.width / 2f else targetLocation[0] - overlayLocation[0].toFloat()
        val y = targetLocation[1] - overlayLocation[1] + target.height / 2f
        return PointF(x, y)
    }

    private fun pointOnOverlayFor(option: MuscleOption): PointF {
        val container = binding.bodyImageContainer
        val drawable = binding.bodyImage.drawable ?: return PointF(0f, 0f)
        val scale = maxOf(container.width.toFloat() / drawable.intrinsicWidth, container.height.toFloat() / drawable.intrinsicHeight)
        val dw = drawable.intrinsicWidth * scale
        val dh = drawable.intrinsicHeight * scale
        val containerLoc = IntArray(2).also(container::getLocationOnScreen)
        val overlayLoc = IntArray(2).also(binding.calloutOverlay::getLocationOnScreen)
        return PointF(
            containerLoc[0] - overlayLoc[0] + (container.width - dw) / 2f + option.xBias * dw,
            containerLoc[1] - overlayLoc[1] + (container.height - dh) / 2f + option.yBias * dh
        )
    }

    private fun applyPremiumAnimations() {
        listOf(binding.header.backButton, binding.header.progressTag, binding.header.progressBar).forEachIndexed { index, view ->
            OnboardingAnimations.fadeInSlideIn(view, index)
        }
        binding.title.alpha = 0f; binding.title.translationX = -30f
        binding.title.animate().alpha(1f).translationX(0f).setDuration(500).setStartDelay(150).start()
        binding.bodyImageContainer.alpha = 0f; binding.bodyImageContainer.scaleX = 0.95f; binding.bodyImageContainer.scaleY = 0.95f
        binding.bodyImageContainer.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(800).setStartDelay(300).start()
        binding.optionsScroll.alpha = 0f; binding.optionsScroll.translationY = 40f
        binding.optionsScroll.animate().alpha(1f).translationY(0f).setDuration(600).setStartDelay(450).start()
        binding.button3.alpha = 0f; binding.button3.animate().alpha(1f).setDuration(500).setStartDelay(700).start()
    }

    override fun onDestroyView() {
        binding.calloutOverlay.clearAll()
        super.onDestroyView()
        _binding = null
    }
}

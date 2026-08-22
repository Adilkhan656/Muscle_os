package com.musclesOS.adil.ui.onboarding.fragment

import android.graphics.PointF
import android.os.Bundle
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

class FocusAreaFragment : Fragment(R.layout.fragment_focus_area) {

    private var _binding: FragmentFocusAreaBinding? = null
    private val binding get() = _binding!!

    private val selectedIds = mutableSetOf<String>()

    // Keep references so we can update visuals and compute line
    // positions later, keyed by MuscleOption.id.
    private val dotViews = mutableMapOf<String, View>()
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

        binding.header.progress.text = "5 of 6"
        binding.header.backButton.setOnClickListener { findNavController().navigateUp() }

        buildChecklist()

        // Dots need the photo's final on-screen size to compute pixel
        // positions from bias values, so we wait for the first layout
        // pass to finish before placing them.
        binding.bodyImageContainer.post { buildDots() }

        binding.button3.setOnClickListener {
            // selectedIds now holds whatever the user picked, e.g. ["chest", "abs"]
            findNavController().navigate(R.id.action_focusAreaFragment_to_goalFragment)
        }
    }

    /** Creates one row per MuscleOption inside optionsList. */
    private fun buildChecklist() {
        val inflater = LayoutInflater.from(requireContext())
        MuscleOptions.ALL.forEach { option ->
            val row = inflater.inflate(R.layout.item_muscle_option, binding.optionsList, false)
            row.findViewById<TextView>(R.id.optionLabel).text = option.displayName
            row.setOnClickListener { toggle(option) }

            binding.optionsList.addView(row)
            rowViews[option.id] = row
        }
    }

    /** Creates one small dot View per MuscleOption that has hasBodyDot = true. */
    private fun buildDots() {
        val container = binding.bodyImageContainer
        val containerWidth = container.width
        val containerHeight = container.height
        val dotSizePx = (18 * resources.displayMetrics.density).toInt()

        MuscleOptions.ALL.filter { it.hasBodyDot }.forEach { option ->
            val dot = View(requireContext()).apply {
                background = androidx.core.content.ContextCompat.getDrawable(
                    requireContext(), R.drawable.bg_hotspot_marker
                )
                layoutParams = FrameLayout.LayoutParams(dotSizePx, dotSizePx).apply {
                    leftMargin = (option.xBias * containerWidth - dotSizePx / 2f).toInt()
                    topMargin = (option.yBias * containerHeight - dotSizePx / 2f).toInt()
                }
                setOnClickListener { toggle(option) }
            }
            container.addView(dot)
            dotViews[option.id] = dot
        }
    }

    /** Shared toggle used by both the dot and the row for a given option. */
    private fun toggle(option: MuscleOption) {
        // Full body is an all-or-nothing choice. Selecting a specific area removes it;
        // selecting it clears every individual area and their connector lines.
        if (option.id == "full_body" && !selectedIds.contains(option.id)) {
            selectedIds.toList().forEach { selectedId -> MuscleOptions.ALL.find { it.id == selectedId }?.let(::clearSelection) }
        } else if (option.id != "full_body" && selectedIds.contains("full_body")) {
            MuscleOptions.ALL.find { it.id == "full_body" }?.let(::clearSelection)
        }
        val row = rowViews[option.id] ?: return
        val checkIcon = row.findViewById<TextView>(R.id.checkIcon)
        val dot = dotViews[option.id]

        if (selectedIds.contains(option.id)) {
            clearSelection(option)
        } else {
            selectedIds.add(option.id)
            row.setBackgroundResource(R.drawable.bg_option_row_selected)
            checkIcon.text = "✓"
            checkIcon.setBackgroundResource(R.drawable.bg_checkbox_checked)
            dot?.setBackgroundResource(R.drawable.bg_hotspot_marker_selected)

            if (dot != null) {
                val from = pointOnOverlayFor(dot, useCenter = true)
                val to = pointOnOverlayFor(row, useCenter = false)
                binding.calloutOverlay.showLine(option.id, from, to)
            }
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
        dotViews[option.id]?.setBackgroundResource(R.drawable.bg_hotspot_marker)
        binding.calloutOverlay.hideLine(option.id)
    }

    /**
     * Converts a view's on-screen position into a point in the overlay's
     * coordinate space. useCenter = true targets the view's center (for
     * the small round dots); false targets its left edge, vertically
     * centered (a natural "arrow tip" spot for a wide row).
     */
    private fun pointOnOverlayFor(target: View, useCenter: Boolean): PointF {
        val targetLocation = IntArray(2)
        target.getLocationOnScreen(targetLocation)

        val overlayLocation = IntArray(2)
        binding.calloutOverlay.getLocationOnScreen(overlayLocation)

        val x = if (useCenter) {
            targetLocation[0] - overlayLocation[0] + target.width / 2f
        } else {
            targetLocation[0] - overlayLocation[0].toFloat()
        }
        val y = targetLocation[1] - overlayLocation[1] + target.height / 2f
        return PointF(x, y)
    }

    override fun onDestroyView() {
        binding.calloutOverlay.clearAll()
        super.onDestroyView()
        _binding = null
    }
}

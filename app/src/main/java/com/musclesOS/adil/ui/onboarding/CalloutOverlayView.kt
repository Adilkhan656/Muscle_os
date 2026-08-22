package com.musclesOS.adil.ui.onboarding

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator

/**
 * CalloutOverlayView.kt
 *
 * Transparent overlay that draws an animated dot + dashed line from a
 * point on the photo to another point on screen (the matching list row).
 * Place as the LAST child in your ConstraintLayout, match_parent, with
 * clickable="false" so taps pass through to whatever is underneath.
 *
 * Usage:
 *   overlay.showLine("chest", dotAnchorPoint, rowTargetPoint)
 *   overlay.hideLine("chest")
 *
 * Both points must already be converted into THIS view's coordinate
 * space (see anchorPointFor() / targetPointFor() in the fragment).
 */
class CalloutOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private data class ActiveLine(
        val id: String,
        val from: PointF,
        val to: PointF,
        var progress: Float = 0f
    )

    private val activeLines = mutableMapOf<String, ActiveLine>()
    private val runningAnimators = mutableMapOf<String, ValueAnimator>()

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF8A00")
        style = Paint.Style.FILL
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF8A00")
        strokeWidth = 3f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(14f, 10f), 0f) // dash length, gap length
    }

    /** Call when an option is selected. from = dot position on the photo, to = target point on the matching list row. */
    fun showLine(id: String, from: PointF, to: PointF) {
        if (activeLines.containsKey(id)) return

        val line = ActiveLine(id, from, to, progress = 0f)
        activeLines[id] = line

        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 350
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                line.progress = it.animatedValue as Float
                invalidate()
            }
        }
        runningAnimators[id]?.cancel()
        runningAnimators[id] = animator
        animator.start()
    }

    /** Call when an option is deselected. */
    fun hideLine(id: String) {
        runningAnimators.remove(id)?.cancel()
        activeLines.remove(id)
        invalidate()
    }

    fun clearAll() {
        runningAnimators.values.forEach { it.cancel() }
        runningAnimators.clear()
        activeLines.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        activeLines.values.forEach { line ->
            val progress = line.progress.coerceIn(0f, 1f)

            dotPaint.alpha = (progress * 255).toInt()
            canvas.drawCircle(line.from.x, line.from.y, 8f, dotPaint)

            val curX = line.from.x + (line.to.x - line.from.x) * progress
            val curY = line.from.y + (line.to.y - line.from.y) * progress
            linePaint.alpha = (progress * 255).toInt()
            canvas.drawLine(line.from.x, line.from.y, curX, curY, linePaint)
        }
    }
}
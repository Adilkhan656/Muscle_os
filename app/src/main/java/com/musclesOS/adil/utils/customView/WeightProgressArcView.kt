package com.musclesOS.adil.utils.customView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs

/** A lightweight semicircle target-weight visual for the profile screen. */
class WeightProgressArcView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; color = Color.rgb(98, 232, 155) }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER; typeface = android.graphics.Typeface.DEFAULT_BOLD }
    private val rect = RectF()
    private var currentWeight = 0.0
    private var targetWeight = 0.0

    fun setWeights(current: Double, target: Double) {
        currentWeight = current
        targetWeight = target
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val stroke = 18f * resources.displayMetrics.density
        val padding = stroke * 1.2f
        rect.set(padding, padding, width - padding, height * 1.65f)
        trackPaint.strokeWidth = stroke
        trackPaint.color = Color.rgb(55, 63, 70)
        canvas.drawArc(rect, 180f, 180f, false, trackPaint)

        val distance = abs(currentWeight - targetWeight)
        val completion = when {
            currentWeight <= 0.0 || targetWeight <= 0.0 -> 0.0
            distance <= 0.05 -> 1.0
            else -> (1.0 - distance / currentWeight).coerceIn(0.0, 1.0)
        }
        progressPaint.strokeWidth = stroke
        canvas.drawArc(rect, 180f, 180f * completion.toFloat(), false, progressPaint)

        textPaint.color = Color.WHITE
        textPaint.textSize = 30f * resources.displayMetrics.density
        val centerX = width / 2f
        canvas.drawText(if (distance <= 0.05) "Target reached" else String.format("%.1f kg", distance), centerX, height * 0.72f, textPaint)

        textPaint.color = Color.rgb(166, 170, 182)
        textPaint.textSize = 13f * resources.displayMetrics.density
        canvas.drawText(if (distance <= 0.05) "You are at your target" else "to target", centerX, height * 0.88f, textPaint)
    }
}

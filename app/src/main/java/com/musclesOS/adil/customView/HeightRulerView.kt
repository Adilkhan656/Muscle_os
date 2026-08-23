package com.musclesOS.adil.customView

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.roundToInt
import java.util.Locale

/** A vertical height ruler with a fixed centre marker and an always-live value readout. */
class HeightRulerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    var minValue = 120.0
    var maxValue = 220.0
    var step = .1
    var value = 175.0
        set(raw) {
            val newValue = ((raw / step).roundToInt() * step).coerceIn(minValue, maxValue)
            if (abs(field - newValue) < .00001) return
            field = newValue
            onValueChanged?.invoke(newValue)
            performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
            invalidate()
        }
    
    var isCm: Boolean = true
        set(newValue) {
            field = newValue
            invalidate()
        }

    var onValueChanged: ((Double) -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeCap = Paint.Cap.ROUND }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    
    private val majorTypeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    private val boldTypeface = Typeface.create("sans-serif", Typeface.BOLD)
    private val regularTypeface = Typeface.create("sans-serif", Typeface.NORMAL)

    private var downY = 0f
    private var downValue = value

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerY = height / 2f
        val accent = Color.rgb(249, 115, 22)
        val isDark = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val primary = if (isDark) Color.rgb(242, 243, 248) else Color.rgb(18, 19, 23)
        val secondary = if (isDark) Color.rgb(190, 193, 204) else Color.rgb(103, 108, 117)
        val spacing = dp(11f)

        // Draw ticks and labels
        for (offset in -32..32) {
            val candidate = value - offset * step
            if (candidate !in minValue..maxValue) continue
            val y = centerY + offset * spacing
            if (y !in -dp(10f)..height + dp(10f)) continue

            val isSelected = offset == 0
            val whole = abs(candidate - candidate.roundToInt()) < .001
            val isMajor = if (isCm) {
                abs(candidate % 5.0) < .001
            } else {
                // In feet mode, major ticks every 1 inch
                whole
            }

            paint.color = when {
                isSelected -> accent
                isMajor -> Color.rgb(210, 213, 222)
                whole -> Color.rgb(145, 149, 160)
                else -> Color.rgb(100, 104, 114)
            }
            
            paint.strokeWidth = dp(if (isSelected) 2.5f else if (isMajor) 2f else 1f)
            val length = when {
                isSelected -> 62f
                isMajor -> 48f
                whole -> 30f
                else -> 18f
            }
            
            canvas.drawLine(dp(2f), y, dp(length), y, paint)

            if (isMajor && !isSelected) {
                textPaint.color = secondary
                textPaint.textSize = dp(14f)
                textPaint.typeface = majorTypeface
                
                val labelText = if (isCm) {
                    candidate.roundToInt().toString()
                } else {
                    val feet = (candidate / 12).toInt()
                    val inches = (candidate % 12).roundToInt()
                    if (inches == 12) {
                        "${feet + 1}' 0\""
                    } else {
                        "${feet}' ${inches}\""
                    }
                }
                
                canvas.drawText(labelText, dp(length + 20f), y + dp(5f), textPaint)
            }
        }

        // Fixed laser line
        paint.color = accent
        paint.strokeWidth = dp(2f)
        canvas.drawLine(0f, centerY, width.toFloat(), centerY, paint)

        // Large live display - POSITIONED ABOVE THE LINE
        val displayStr = if (isCm) {
            String.format(Locale.US, "%.1f cm", value)
        } else {
            val feet = (value / 12).toInt()
            val inches = value - (feet * 12)
            String.format(Locale.US, "%d' %.1f\"", feet, inches)
        }

        val number = displayStr.substringBeforeLast(' ')
        val unit = displayStr.substringAfterLast(' ', "")

        textPaint.color = primary
        textPaint.textSize = dp(54f)
        textPaint.typeface = boldTypeface
        textPaint.textAlign = Paint.Align.LEFT
        
        val numberWidth = textPaint.measureText(number)
        val unitPaint = Paint(textPaint).apply {
            textSize = dp(22f)
            typeface = regularTypeface
            color = secondary
        }
        val unitWidth = unitPaint.measureText(unit)
        val totalWidth = numberWidth + (if (unit.isNotEmpty()) dp(8f) + unitWidth else 0f)
        
        val startX = width / 2f - totalWidth / 2f
        // Move y position up to be "above the line"
        val textY = centerY - dp(24f)
        
        canvas.drawText(number, startX, textY, textPaint)
        if (unit.isNotEmpty()) {
            canvas.drawText(unit, startX + numberWidth + dp(8f), textY, unitPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean = when (event.actionMasked) {
        MotionEvent.ACTION_DOWN -> { downY = event.y; downValue = value; parent.requestDisallowInterceptTouchEvent(true); true }
        MotionEvent.ACTION_MOVE -> { value = downValue - (event.y - downY) / dp(10f) * step; true }
        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { parent.requestDisallowInterceptTouchEvent(false); performClick(); true }
        else -> true
    }
    override fun performClick(): Boolean { super.performClick(); return true }
    private fun dp(v: Float) = v * resources.displayMetrics.density
}

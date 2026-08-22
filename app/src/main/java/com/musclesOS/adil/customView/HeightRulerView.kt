package com.musclesOS.adil.customView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.roundToInt

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
    var displayText = "175 cm"
        set(newValue) { field = newValue; invalidate() }
    var onValueChanged: ((Double) -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeCap = Paint.Cap.ROUND }
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private var downY = 0f
    private var downValue = value

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerY = height / 2f
        val accent = Color.rgb(249, 115, 22)
        val railX = width - dp(16f)
        val spacing = dp(11f)
        // Right-side tick rail. It is centred on the selected value, so its labels move with the value.
        for (offset in -32..32) {
            val candidate = value - offset * step
            if (candidate !in minValue..maxValue) continue
            val y = centerY + offset * spacing
            if (y !in -dp(10f)..height + dp(10f)) continue
            val whole = abs(candidate - candidate.roundToInt()) < .001
            val five = abs(candidate / 5.0 - (candidate / 5.0).roundToInt()) < .001
            paint.color = when { offset == 0 -> accent; whole || five -> Color.rgb(170, 174, 180); else -> Color.rgb(215, 217, 220) }
            paint.strokeWidth = dp(if (offset == 0) 2.5f else if (whole) 2f else 1f)
            val length = when { offset == 0 -> 62f; whole -> 42f; five -> 28f; else -> 16f }
            canvas.drawLine(railX - dp(length), y, railX, y, paint)
            if (whole && offset != 0) {
                text.color = Color.rgb(150, 153, 160)
                text.textSize = dp(14f)
                canvas.drawText(candidate.roundToInt().toString(), railX - dp(length + 24f), y + dp(5f), text)
            }
        }
        // Fixed laser line and large live display.
        paint.color = accent
        paint.strokeWidth = dp(2f)
        canvas.drawLine(0f, centerY, width.toFloat(), centerY, paint)
        text.color = Color.rgb(20, 21, 24)
        text.textSize = dp(50f)
        text.typeface = android.graphics.Typeface.create("sans-serif", 1)
        canvas.drawText(displayText, width / 2f, centerY + dp(16f), text)
        // A subtle left glass marker creates the physical ruler feel without obscuring the value.
        paint.color = Color.argb(32, 249, 115, 22)
        canvas.drawCircle(dp(58f), centerY, dp(42f), paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1.5f)
        paint.color = Color.argb(90, 120, 120, 125)
        canvas.drawCircle(dp(58f), centerY, dp(40f), paint)
        paint.style = Paint.Style.FILL
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

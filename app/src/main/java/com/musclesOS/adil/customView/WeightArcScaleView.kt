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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/** Curved, touch-driven weight dial used on the onboarding weight screen. */
class WeightArcScaleView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    var minValue = 35.0
    var maxValue = 160.0
    var step = 0.1
    var value = 50.0
        set(newValue) {
            val rounded = (newValue / step).roundToInt() * step
            val bounded = rounded.coerceIn(minValue, maxValue)
            if (abs(field - bounded) < .00001) return
            field = bounded
            onValueChanged?.invoke(field)
            performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
            invalidate()
        }
    var unit = "kg"
        set(newValue) { field = newValue; invalidate() }
    var onValueChanged: ((Double) -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeCap = Paint.Cap.ROUND }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private var downX = 0f
    private var downValue = value

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val centerX = w / 2f
        val centerY = h * 1.08f
        val radius = w * .78f
        val accent = Color.rgb(249, 115, 22)
        val arcRect = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1.5f)
        paint.color = Color.argb(75, 190, 190, 200)
        canvas.drawArc(arcRect, 202f, 136f, false, paint)
        val innerRect = RectF(centerX - radius * .83f, centerY - radius * .83f, centerX + radius * .83f, centerY + radius * .83f)
        canvas.drawArc(innerRect, 207f, 126f, false, paint)

        val visibleRange = 20.0
        val tickStep = if (step <= .1) .5 else step
        var tick = value - visibleRange
        while (tick <= value + visibleRange) {
            if (tick >= minValue && tick <= maxValue) {
                val offset = tick - value
                val angle = 270.0 + offset * 2.7
                if (angle in 202.0..338.0) {
                    val major = abs(tick % 10.0) < .02 || abs(tick % 10.0 - 10) < .02
                    val selected = abs(offset) < tickStep / 2
                    val length = when { selected -> 78f; major -> 55f; else -> 29f }
                    paint.strokeWidth = dp(if (major || selected) 3.4f else 2f)
                    paint.color = when { selected -> accent; major -> Color.rgb(224, 224, 232); else -> Color.rgb(130, 130, 145) }
                    val rad = angle * PI / 180.0
                    val rOuter = radius * .93f
                    val x1 = centerX + cos(rad).toFloat() * rOuter
                    val y1 = centerY + sin(rad).toFloat() * rOuter
                    val x2 = centerX + cos(rad).toFloat() * (rOuter - dp(length))
                    val y2 = centerY + sin(rad).toFloat() * (rOuter - dp(length))
                    canvas.drawLine(x1, y1, x2, y2, paint)
                    if (major) {
                        textPaint.color = Color.rgb(190, 190, 202)
                        textPaint.textSize = dp(17f)
                        textPaint.typeface = android.graphics.Typeface.create("sans-serif-medium", 0)
                        val labelR = rOuter - dp(length + 23f)
                        canvas.drawText(tick.roundToInt().toString(), centerX + cos(rad).toFloat() * labelR, centerY + sin(rad).toFloat() * labelR + dp(6f), textPaint)
                    }
                }
            }
            tick += tickStep
        }

        // Selected marker and readout plate.
        paint.color = accent
        paint.strokeWidth = dp(3f)
        canvas.drawLine(centerX, h * .26f, centerX, h * .53f, paint)
        paint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, h * .26f, dp(5f), paint)
        val plate = RectF(centerX - dp(112f), h * .62f, centerX + dp(112f), h * .80f)
        paint.color = Color.argb(235, 25, 25, 30)
        canvas.drawRoundRect(plate, dp(30f), dp(30f), paint)
        textPaint.typeface = android.graphics.Typeface.create("sans-serif", 1)
        textPaint.textSize = dp(45f)
        textPaint.color = Color.WHITE
        canvas.drawText(String.format(java.util.Locale.US, "%.1f", value), centerX - dp(15f), h * .725f, textPaint)
        textPaint.typeface = android.graphics.Typeface.create("sans-serif", 0)
        textPaint.textSize = dp(20f)
        textPaint.color = Color.rgb(175, 175, 190)
        canvas.drawText(unit, centerX + dp(72f), h * .725f, textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> { downX = event.x; downValue = value; parent.requestDisallowInterceptTouchEvent(true); return true }
            MotionEvent.ACTION_MOVE -> { value = downValue + ((event.x - downX) / dp(8f)) * step; return true }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { parent.requestDisallowInterceptTouchEvent(false); performClick(); return true }
        }
        return true
    }
    override fun performClick(): Boolean { super.performClick(); return true }
    private fun dp(value: Float) = value * resources.displayMetrics.density
}

package com.musclesOS.adil.customView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.roundToInt

/** A touch-driven ruler used by the assessment screens. */
class AssessmentScaleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var minValue = 120
    var maxValue = 220
    var value = 175
        set(newValue) {
            field = newValue.coerceIn(minValue, maxValue)
            onValueChanged?.invoke(field)
            invalidate()
        }
    var vertical = false
        set(newValue) { field = newValue; invalidate() }
    /** Use white tick marks on a dark or coloured background. */
    var inverted = false
        set(newValue) { field = newValue; invalidate() }
    var onValueChanged: ((Int) -> Unit)? = null

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeCap = Paint.Cap.ROUND }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
    }
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var downCoordinate = 0f
    private var downValue = value

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val center = if (vertical) height / 2f else width / 2f
        val spacing = resources.displayMetrics.density * 10f
        val orange = Color.rgb(249, 115, 22)
        val foreground = if (inverted) Color.WHITE else Color.rgb(17, 18, 20)
        val secondary = if (inverted) 0x99FFFFFF.toInt() else Color.rgb(158, 160, 165)
        for (offset in -30..30) {
            val candidate = value + offset
            if (candidate !in minValue..maxValue) continue
            val major = candidate % 5 == 0
            val selected = offset == 0
            tickPaint.color = if (selected) foreground else secondary
            tickPaint.strokeWidth = if (selected) dp(4f) else dp(if (major) 2f else 1.3f)
            val length = if (selected) dp(62f) else dp(if (major) 42f else 25f)
            if (vertical) {
                val y = center - offset * spacing
                canvas.drawLine(width - length, y, width.toFloat(), y, tickPaint)
                if (major && !selected) drawLabel(canvas, candidate.toString(), width - length - dp(12f), y + dp(5f), secondary, Paint.Align.RIGHT)
            } else {
                val x = center + offset * spacing
                canvas.drawLine(x, 0f, x, length, tickPaint)
                if (major && !selected) drawLabel(canvas, candidate.toString(), x, length + dp(22f), secondary, Paint.Align.CENTER)
            }
        }

        indicatorPaint.color = orange
        if (vertical) {
            canvas.drawRoundRect(dp(4f), center - dp(42f), dp(92f), center + dp(42f), dp(18f), dp(18f), indicatorPaint)
            drawLabel(canvas, value.toString(), dp(48f), center + dp(13f), Color.WHITE, Paint.Align.CENTER, 42f)
        } else {
            canvas.drawRoundRect(center - dp(48f), height - dp(58f), center + dp(48f), height.toFloat(), dp(18f), dp(18f), indicatorPaint)
            drawLabel(canvas, value.toString(), center, height - dp(17f), Color.WHITE, Paint.Align.CENTER, 38f)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val coordinate = if (vertical) event.y else event.x
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> { downCoordinate = coordinate; downValue = value; parent.requestDisallowInterceptTouchEvent(true); return true }
            MotionEvent.ACTION_MOVE -> {
                // Six dp per unit makes the control precise without requiring a long drag.
                val spacing = dp(6f)
                val delta = ((coordinate - downCoordinate) / spacing).roundToInt()
                value = if (vertical) downValue - delta else downValue + delta
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { performClick(); return true }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean = super.performClick()

    private fun drawLabel(canvas: Canvas, text: String, x: Float, y: Float, color: Int, align: Paint.Align, size: Float = 13f) {
        labelPaint.color = color
        labelPaint.textSize = dp(size)
        labelPaint.textAlign = align
        canvas.drawText(text, x, y, labelPaint)
    }

    private fun dp(value: Float) = value * resources.displayMetrics.density
}

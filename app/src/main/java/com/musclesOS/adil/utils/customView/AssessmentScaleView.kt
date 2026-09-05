package com.musclesOS.adil.utils.customView

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.roundToInt
import java.util.Locale

/**
 * Premium touch-driven ruler used by MuscleOS assessment screens.
 *
 * Supports:
 * - Decimal values
 * - Vertical / horizontal orientation
 * - Major / medium / minor ticks
 * - Minimalist / Magnifier selection modes
 * - Haptic feedback on every value step
 */
class AssessmentScaleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    init {
        isHapticFeedbackEnabled = true
        // Enable hardware acceleration for BlurMaskFilter
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    // ---------------------------------------------------------
    // VALUE CONFIGURATION
    // ---------------------------------------------------------

    var minValue = 0.0
        set(newValue) {
            field = newValue
            if (maxValue >= field) {
                value = value.coerceIn(field, maxValue)
            }
            invalidate()
        }

    var maxValue = 200.0
        set(newValue) {
            field = newValue
            if (field >= minValue) {
                value = value.coerceIn(minValue, field)
            }
            invalidate()
        }

    var step = 0.1

    var value = 175.0
        set(newValue) {
            val roundedValue = (newValue / step).roundToInt() * step
            val cleanValue = roundedValue.coerceIn(minValue, maxValue)

            if (abs(field - cleanValue) < 0.00001) return
            field = cleanValue
            onValueChanged?.invoke(field)
            triggerHaptic()
            invalidate()
        }

    var vertical = false
        set(newValue) {
            field = newValue
            invalidate()
        }

    var inverted = false
        set(newValue) {
            field = newValue
            invalidate()
        }

    var onValueChanged: ((Double) -> Unit)? = null

    /**
     * Whether to show numerical labels next to the major ticks.
     */
    var showLabels = true
        set(newValue) {
            field = newValue
            invalidate()
        }

    /**
     * Whether to show the premium glassy magnifier bubble.
     */
    var showMagnifier = true
        set(newValue) {
            field = newValue
            invalidate()
        }

    /**
     * Optional formatter for the values displayed on labels and magnifier.
     */
    var valueFormatter: ((Double) -> String)? = null

    // ---------------------------------------------------------
    // PAINTS
    // ---------------------------------------------------------

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }

    private val selectedValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif", Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val glossPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val bubbleStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val centerIndicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
    }

    private val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        // This DST_IN mode with a white->transparent gradient will create a vignette fade
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        maskFilter = BlurMaskFilter(dp(16f), BlurMaskFilter.Blur.NORMAL)
    }

    // ---------------------------------------------------------
    // TOUCH STATE
    // ---------------------------------------------------------

    private var downCoordinate = 0f
    private var downValue = value
    private var lastHapticStep = Int.MIN_VALUE
    private val rulerSpacingDp = 10f

    // ---------------------------------------------------------
    // HAPTIC
    // ---------------------------------------------------------

    private fun triggerHaptic() {
        val currentStep = (value / step).roundToInt()
        if (currentStep == lastHapticStep) return
        lastHapticStep = currentStep
        performHapticFeedback(
            HapticFeedbackConstants.CLOCK_TICK,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }

    // ---------------------------------------------------------
    // DRAW
    // ---------------------------------------------------------

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Using a layer for DST_IN xfermode (vignette)
        val saveCount = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)

        val center = if (vertical) height / 2f else width / 2f
        val spacing = dp(rulerSpacingDp)
        val foreground = if (inverted) Color.WHITE else Color.rgb(25, 26, 29)
        val secondary = if (inverted) 0x70FFFFFF else Color.rgb(170, 172, 178)
        val tertiary = if (inverted) 0x35FFFFFF else Color.rgb(215, 216, 220)

        val maxOffset = 45
        for (offset in -maxOffset..maxOffset) {
            val candidate = value + offset * step
            if (candidate < minValue || candidate > maxValue) continue

            val isSelected = offset == 0
            val isMajor = isWholeValue(candidate)
            val isMedium = isHalfValue(candidate)

            // Focus Effect
            val distanceFactor = 1f - (abs(offset).toFloat() / maxOffset).coerceIn(0f, 1f)
            val focusAlpha = (distanceFactor * 255).toInt()

            tickPaint.color = if (isSelected) foreground else if (isMajor || isMedium) secondary else tertiary
            tickPaint.alpha = focusAlpha

            val tickLength = when {
                isSelected -> if (showMagnifier) dp(70f) else dp(50f)
                isMajor -> dp(48f) * (0.6f + 0.4f * distanceFactor)
                isMedium -> dp(36f) * (0.6f + 0.4f * distanceFactor)
                else -> dp(24f) * (0.6f + 0.4f * distanceFactor)
            }

            tickPaint.strokeWidth = when {
                isSelected -> dp(3.5f)
                isMajor -> dp(2f)
                isMedium -> dp(1.5f)
                else -> dp(1f)
            }

            if (vertical) {
                val y = center - offset * spacing
                canvas.drawLine(width - tickLength, y, width.toFloat(), y, tickPaint)
                if (showLabels && isMajor && !isSelected) {
                    drawLabel(canvas, formatValue(candidate), width - tickLength - dp(12f), y + dp(5f), secondary, Paint.Align.RIGHT)
                }
            } else {
                val x = center + offset * spacing
                canvas.drawLine(x, 0f, x, tickLength, tickPaint)
                if (showLabels && isMajor && !isSelected) {
                    drawLabel(canvas, formatValue(candidate), x, tickLength + dp(22f), secondary, Paint.Align.CENTER)
                }
            }
        }

        if (showMagnifier) {
            drawSelectedMagnifier(canvas, center, foreground)
        } else {
            drawMinimalistIndicator(canvas, center)
        }

        drawVignette(canvas)
        canvas.restoreToCount(saveCount)
    }

    private fun drawSelectedMagnifier(canvas: Canvas, center: Float, foreground: Int) {
        val orange = Color.rgb(249, 115, 22)
        val bubbleRadius = dp(44f)
        val bubbleCenterX: Float
        val bubbleCenterY: Float

        if (vertical) {
            bubbleCenterX = dp(64f)
            bubbleCenterY = center
        } else {
            bubbleCenterX = center
            bubbleCenterY = height / 2f
        }

        // Needle
        centerIndicatorPaint.color = orange
        centerIndicatorPaint.strokeWidth = dp(2f)
        if (vertical) {
            canvas.drawLine(dp(4f), center, width.toFloat(), center, centerIndicatorPaint)
        } else {
            canvas.drawLine(center, 0f, center, dp(85f), centerIndicatorPaint)
        }

        // Liquid Glass Shadow
        bubblePaint.shader = null
        bubblePaint.maskFilter = BlurMaskFilter(dp(8f), BlurMaskFilter.Blur.NORMAL)
        bubblePaint.color = if (inverted) 0x60000000.toInt() else 0x25000000
        canvas.drawCircle(bubbleCenterX, bubbleCenterY + dp(4f), bubbleRadius, bubblePaint)
        bubblePaint.maskFilter = null

        // Body
        val glassGradient = LinearGradient(
            bubbleCenterX - bubbleRadius, bubbleCenterY - bubbleRadius,
            bubbleCenterX + bubbleRadius, bubbleCenterY + bubbleRadius,
            intArrayOf(
                if (inverted) 0x44FFFFFF.toInt() else 0x99FFFFFF.toInt(),
                if (inverted) 0x11FFFFFF.toInt() else 0x33FFFFFF.toInt(),
                if (inverted) 0x22FFFFFF.toInt() else 0x66FFFFFF.toInt()
            ),
            floatArrayOf(0f, 0.5f, 1f), Shader.TileMode.CLAMP
        )
        bubblePaint.shader = glassGradient
        canvas.drawCircle(bubbleCenterX, bubbleCenterY, bubbleRadius, bubblePaint)

        // Highlights
        val glossPath = Path()
        val glossRect = RectF(bubbleCenterX - bubbleRadius * 0.75f, bubbleCenterY - bubbleRadius * 0.85f, bubbleCenterX + bubbleRadius * 0.75f, bubbleCenterY + bubbleRadius * 0.2f)
        glossPath.addOval(glossRect, Path.Direction.CW)
        glossPaint.shader = LinearGradient(bubbleCenterX, bubbleCenterY - bubbleRadius * 0.85f, bubbleCenterX, bubbleCenterY, if (inverted) 0x66FFFFFF.toInt() else 0xCCFFFFFF.toInt(), 0x00FFFFFF.toInt(), Shader.TileMode.CLAMP)
        canvas.drawPath(glossPath, glossPaint)

        // Rim
        bubbleStrokePaint.strokeWidth = dp(2f)
        bubbleStrokePaint.shader = LinearGradient(bubbleCenterX, bubbleCenterY - bubbleRadius, bubbleCenterX, bubbleCenterY + bubbleRadius, intArrayOf(0xBBFFFFFF.toInt(), 0x22FFFFFF.toInt(), 0x55FFFFFF.toInt()), null, Shader.TileMode.CLAMP)
        canvas.drawCircle(bubbleCenterX, bubbleCenterY, bubbleRadius - dp(1f), bubbleStrokePaint)
        bubbleStrokePaint.shader = null

        // Spark
        bubblePaint.shader = null
        bubblePaint.color = if (inverted) 0xAAFFFFFF.toInt() else 0xDDFFFFFF.toInt()
        canvas.drawCircle(bubbleCenterX - bubbleRadius * 0.45f, bubbleCenterY - bubbleRadius * 0.45f, dp(3f), bubblePaint)

        // Text
        selectedValuePaint.color = foreground
        selectedValuePaint.textSize = dp(30f)
        val textStr = formatValue(value)
        val textBounds = Rect()
        selectedValuePaint.getTextBounds(textStr, 0, textStr.length, textBounds)
        canvas.drawText(textStr, bubbleCenterX, bubbleCenterY + (textBounds.height() / 2f), selectedValuePaint)
    }

    private fun drawMinimalistIndicator(canvas: Canvas, center: Float) {
        val orange = Color.rgb(249, 115, 22)
        
        // High-Tech Glow
        glowPaint.color = 0x30F97316
        if (vertical) {
            canvas.drawCircle(width.toFloat(), center, dp(45f), glowPaint)
        } else {
            canvas.drawCircle(center, 0f, dp(45f), glowPaint)
        }

        // Laser Needle
        centerIndicatorPaint.strokeWidth = dp(2.5f)
        if (vertical) {
            centerIndicatorPaint.shader = LinearGradient(0f, center, width.toFloat(), center, intArrayOf(0x00F97316.toInt(), 0xAAF97316.toInt(), orange), floatArrayOf(0f, 0.7f, 1f), Shader.TileMode.CLAMP)
            canvas.drawLine(dp(24f), center, width.toFloat(), center, centerIndicatorPaint)
            
            bubblePaint.shader = null
            bubblePaint.color = orange
            canvas.drawCircle(width.toFloat(), center, dp(4f), bubblePaint)
            bubblePaint.color = Color.WHITE
            canvas.drawCircle(width.toFloat() - dp(1.5f), center - dp(1.5f), dp(1.2f), bubblePaint)
        } else {
            centerIndicatorPaint.shader = LinearGradient(center, 0f, center, dp(85f), intArrayOf(orange, 0xAAF97316.toInt(), 0x00F97316.toInt()), floatArrayOf(0f, 0.3f, 1f), Shader.TileMode.CLAMP)
            canvas.drawLine(center, 0f, center, dp(85f), centerIndicatorPaint)
            
            bubblePaint.shader = null
            bubblePaint.color = orange
            canvas.drawCircle(center, 0f, dp(4f), bubblePaint)
            bubblePaint.color = Color.WHITE
            canvas.drawCircle(center - dp(1.5f), dp(1.5f), dp(1.2f), bubblePaint)
        }
        centerIndicatorPaint.shader = null
    }

    private fun drawVignette(canvas: Canvas) {
        val colors = intArrayOf(0x00FFFFFF.toInt(), 0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt(), 0x00FFFFFF.toInt())
        val stops = floatArrayOf(0f, 0.15f, 0.85f, 1f)
        if (vertical) {
            vignettePaint.shader = LinearGradient(0f, 0f, 0f, height.toFloat(), colors, stops, Shader.TileMode.CLAMP)
        } else {
            vignettePaint.shader = LinearGradient(0f, 0f, width.toFloat(), 0f, colors, stops, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), vignettePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val coordinate = if (vertical) event.y else event.x
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downCoordinate = coordinate
                downValue = value
                parent.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val delta = (coordinate - downCoordinate) / dp(rulerSpacingDp)
                val valueDelta = delta * step
                value = if (vertical) downValue - valueDelta else downValue + valueDelta
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun isWholeValue(v: Double) = abs(v - v.roundToInt()) < 0.0001
    private fun isHalfValue(v: Double) = abs(v * 2.0 - (v * 2.0).roundToInt()) < 0.0001 && !isWholeValue(v)

    private fun drawLabel(canvas: Canvas, text: String, x: Float, y: Float, color: Int, align: Paint.Align) {
        labelPaint.color = color
        labelPaint.textSize = dp(13f)
        labelPaint.textAlign = align
        canvas.drawText(text, x, y, labelPaint)
    }

    private fun formatValue(v: Double) = valueFormatter?.invoke(v) ?: String.format(Locale.US, "%.1f", v)
    private fun dp(v: Float) = v * resources.displayMetrics.density
}

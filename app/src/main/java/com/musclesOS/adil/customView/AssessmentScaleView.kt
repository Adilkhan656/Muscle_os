package com.musclesOS.adil.customView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.BlurMaskFilter
import android.graphics.Path
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
 * - Selected-value magnifier
 * - Haptic feedback on every value step
 *
 * Unit conversion is intentionally NOT handled here.
 * HeightFragment / WeightFragment handle units.
 */
class AssessmentScaleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    init {
        isHapticFeedbackEnabled = true
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

    /**
     * One selectable step.
     *
     * Example:
     *
     * 80.0
     * 80.1
     * 80.2
     * 80.3
     */
    var step = 0.1

    var value = 175.0
        set(newValue) {

            val roundedValue =
                (newValue / step).roundToInt() * step

            val cleanValue =
                roundedValue.coerceIn(
                    minValue,
                    maxValue
                )

            if (abs(field - cleanValue) < 0.00001) {
                return
            }

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
     * Optional formatter for the values displayed on labels and magnifier.
     * Useful for things like converting total inches to 5' 11".
     */
    var valueFormatter: ((Double) -> String)? = null

    // ---------------------------------------------------------
    // PAINTS
    // ---------------------------------------------------------

    private val tickPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeCap = Paint.Cap.ROUND
        }

    private val labelPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(
                "sans-serif-medium",
                Typeface.NORMAL
            )
        }

    private val selectedValuePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(
                "sans-serif",
                Typeface.BOLD
            )
            textAlign = Paint.Align.CENTER
        }

    private val bubblePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

    private val glossPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

    private val bubbleStrokePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }

    private val centerIndicatorPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeCap = Paint.Cap.ROUND
        }

    // ---------------------------------------------------------
    // TOUCH STATE
    // ---------------------------------------------------------

    private var downCoordinate = 0f
    private var downValue = value

    private var lastHapticStep = Int.MIN_VALUE

    // ---------------------------------------------------------
    // DIMENSIONS
    // ---------------------------------------------------------

    private val rulerSpacingDp = 10f

    // ---------------------------------------------------------
    // HAPTIC
    // ---------------------------------------------------------

    private fun triggerHaptic() {

        val currentStep =
            (value / step).roundToInt()

        if (currentStep == lastHapticStep) {
            return
        }

        lastHapticStep = currentStep

        // Use a more noticeable tick for moving the scale
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

        val center =
            if (vertical) {
                height / 2f
            } else {
                width / 2f
            }

        val spacing = dp(rulerSpacingDp)

        val foreground =
            if (inverted) {
                Color.WHITE
            } else {
                Color.rgb(25, 26, 29)
            }

        val secondary =
            if (inverted) {
                0x70FFFFFF
            } else {
                Color.rgb(170, 172, 178)
            }

        val tertiary =
            if (inverted) {
                0x35FFFFFF
            } else {
                Color.rgb(215, 216, 220)
            }

        /*
         * We show ±40 ticks around the selected value.
         *
         * With step = 0.1:
         *
         * 40 × 0.1 = 4.0 units
         */
        for (offset in -40..40) {

            val candidate =
                value + offset * step

            if (
                candidate < minValue ||
                candidate > maxValue
            ) {
                continue
            }

            val isSelected =
                offset == 0

            /*
             * Every 1.0 is a major tick.
             */
            val isMajor =
                isWholeValue(candidate)

            /*
             * Every 0.5 is a medium tick.
             */
            val isMedium =
                isHalfValue(candidate)

            val tickColor =
                when {
                    isSelected -> foreground
                    isMajor -> secondary
                    isMedium -> secondary
                    else -> tertiary
                }

            tickPaint.color = tickColor

            val tickLength =
                when {
                    isSelected -> dp(70f)
                    isMajor -> dp(48f)
                    isMedium -> dp(36f)
                    else -> dp(24f)
                }

            tickPaint.strokeWidth =
                when {
                    isSelected -> dp(3.5f)
                    isMajor -> dp(2f)
                    isMedium -> dp(1.5f)
                    else -> dp(1f)
                }

            if (vertical) {

                val y =
                    center - offset * spacing

                canvas.drawLine(
                    width - tickLength,
                    y,
                    width.toFloat(),
                    y,
                    tickPaint
                )

                /*
                 * Major labels.
                 *
                 * We deliberately don't label
                 * every 0.1 value.
                 */
                if (
                    isMajor &&
                    !isSelected
                ) {

                    drawLabel(
                        canvas = canvas,
                        text = formatValue(candidate),
                        x = width - tickLength - dp(12f),
                        y = y + dp(5f),
                        color = secondary,
                        align = Paint.Align.RIGHT,
                        size = 13f
                    )
                }

            } else {

                val x =
                    center + offset * spacing

                canvas.drawLine(
                    x,
                    0f,
                    x,
                    tickLength,
                    tickPaint
                )

                if (
                    isMajor &&
                    !isSelected
                ) {

                    drawLabel(
                        canvas = canvas,
                        text = formatValue(candidate),
                        x = x,
                        y = tickLength + dp(22f),
                        color = secondary,
                        align = Paint.Align.CENTER,
                        size = 13f
                    )
                }
            }
        }

        /*
         * Draw the selected indicator.
         *
         * This replaces the old large orange rectangle
         * with a more premium magnifier-style element.
         */
        drawSelectedMagnifier(
            canvas = canvas,
            center = center,
            foreground = foreground
        )
    }

    // ---------------------------------------------------------
    // SELECTED MAGNIFIER
    // ---------------------------------------------------------

    private fun drawSelectedMagnifier(
        canvas: Canvas,
        center: Float,
        foreground: Int
    ) {
        val orange = Color.rgb(249, 115, 22)
        val bubbleRadius = dp(44f)

        // Adjust position to avoid clipping
        val bubbleCenterX: Float
        val bubbleCenterY: Float

        if (vertical) {
            // Increase from 42f to 56f to prevent clipping on the left
            bubbleCenterX = dp(56f)
            bubbleCenterY = center
        } else {
            bubbleCenterX = center
            // Centered vertically in the view height for horizontal mode
            bubbleCenterY = height / 2f
        }

        // 1. Precision Line (The Needle)
        // Make it slightly more subtle and precise
        centerIndicatorPaint.color = orange
        centerIndicatorPaint.strokeWidth = dp(2f)

        if (vertical) {
            canvas.drawLine(dp(4f), center, width.toFloat(), center, centerIndicatorPaint)
        } else {
            canvas.drawLine(center, 0f, center, dp(85f), centerIndicatorPaint)
        }

        // 2. Liquid Glass Shadow (Soft & Deep)
        bubblePaint.shader = null
        bubblePaint.maskFilter = BlurMaskFilter(dp(8f), BlurMaskFilter.Blur.NORMAL)
        bubblePaint.color = if (inverted) 0x60000000.toInt() else 0x25000000
        canvas.drawCircle(bubbleCenterX, bubbleCenterY + dp(4f), bubbleRadius, bubblePaint)
        bubblePaint.maskFilter = null

        // 3. Main Liquid Glass Body (Neutral translucent gradient)
        // Using very subtle grays/whites to simulate clear glass
        val glassGradient = LinearGradient(
            bubbleCenterX - bubbleRadius, bubbleCenterY - bubbleRadius,
            bubbleCenterX + bubbleRadius, bubbleCenterY + bubbleRadius,
            intArrayOf(
                if (inverted) 0x44FFFFFF.toInt() else 0x99FFFFFF.toInt(),
                if (inverted) 0x11FFFFFF.toInt() else 0x33FFFFFF.toInt(),
                if (inverted) 0x22FFFFFF.toInt() else 0x66FFFFFF.toInt()
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        bubblePaint.shader = glassGradient
        canvas.drawCircle(bubbleCenterX, bubbleCenterY, bubbleRadius, bubblePaint)

        // 4. Highlight Arc (Simulating "Liquid" reflection)
        val glossPath = Path()
        val glossRect = RectF(
            bubbleCenterX - bubbleRadius * 0.75f,
            bubbleCenterY - bubbleRadius * 0.85f,
            bubbleCenterX + bubbleRadius * 0.75f,
            bubbleCenterY + bubbleRadius * 0.2f
        )
        glossPath.addOval(glossRect, Path.Direction.CW)
        
        glossPaint.shader = LinearGradient(
            bubbleCenterX, bubbleCenterY - bubbleRadius * 0.85f,
            bubbleCenterX, bubbleCenterY,
            if (inverted) 0x66FFFFFF.toInt() else 0xCCFFFFFF.toInt(),
            0x00FFFFFF.toInt(),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(glossPath, glossPaint)

        // 5. Liquid Rim (Inner Glow)
        bubbleStrokePaint.strokeWidth = dp(2f)
        bubbleStrokePaint.shader = LinearGradient(
            bubbleCenterX, bubbleCenterY - bubbleRadius,
            bubbleCenterX, bubbleCenterY + bubbleRadius,
            intArrayOf(0xBBFFFFFF.toInt(), 0x22FFFFFF.toInt(), 0x55FFFFFF.toInt()),
            null, Shader.TileMode.CLAMP
        )
        canvas.drawCircle(bubbleCenterX, bubbleCenterY, bubbleRadius - dp(1f), bubbleStrokePaint)
        bubbleStrokePaint.shader = null

        // 5b. High-intensity liquid spark (Upper left)
        bubblePaint.shader = null
        bubblePaint.color = if (inverted) 0xAAFFFFFF.toInt() else 0xDDFFFFFF.toInt()
        canvas.drawCircle(bubbleCenterX - bubbleRadius * 0.45f, bubbleCenterY - bubbleRadius * 0.45f, dp(3f), bubblePaint)

        // 6. Selected Value Text
        // Use a high-contrast neutral or the brand orange if it looks premium
        // Let's use the foreground color (Dark in light, White in dark) for a cleaner look
        selectedValuePaint.color = foreground
        selectedValuePaint.textSize = dp(30f)
        selectedValuePaint.alpha = 255
        
        // Center text vertically
        val textBounds = android.graphics.Rect()
        val textStr = formatValue(value)
        selectedValuePaint.getTextBounds(textStr, 0, textStr.length, textBounds)
        val textY = bubbleCenterY + (textBounds.height() / 2f)
        
        canvas.drawText(textStr, bubbleCenterX, textY, selectedValuePaint)
    }

    // ---------------------------------------------------------
    // TOUCH
    // ---------------------------------------------------------

    override fun onTouchEvent(
        event: MotionEvent
    ): Boolean {

        val coordinate =
            if (vertical) {
                event.y
            } else {
                event.x
            }

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {

                downCoordinate =
                    coordinate

                downValue =
                    value

                parent.requestDisallowInterceptTouchEvent(
                    true
                )

                return true
            }

            MotionEvent.ACTION_MOVE -> {

                val spacing =
                    dp(rulerSpacingDp)

                val delta =
                    (coordinate -
                            downCoordinate) /
                            spacing

                val valueDelta =
                    delta * step

                value =
                    if (vertical) {
                        downValue -
                                valueDelta
                    } else {
                        downValue +
                                valueDelta
                    }

                return true
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {

                parent.requestDisallowInterceptTouchEvent(
                    false
                )

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

    // ---------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------

    private fun isWholeValue(
        value: Double
    ): Boolean {

        return abs(
            value -
                    value.roundToInt()
        ) < 0.0001
    }

    private fun isHalfValue(
        value: Double
    ): Boolean {

        val doubled =
            value * 2.0

        return abs(
            doubled -
                    doubled.roundToInt()
        ) < 0.0001 &&
                !isWholeValue(value)
    }

    private fun drawLabel(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        color: Int,
        align: Paint.Align,
        size: Float = 13f
    ) {

        labelPaint.color =
            color

        labelPaint.textSize =
            dp(size)

        labelPaint.textAlign =
            align

        canvas.drawText(
            text,
            x,
            y,
            labelPaint
        )
    }

    private fun formatValue(
        value: Double
    ): String {

        return valueFormatter?.invoke(value) ?: String.format(
            Locale.US,
            "%.1f",
            value
        )
    }

    private fun dp(
        value: Float
    ): Float {

        return value *
                resources.displayMetrics.density
    }
}
package com.musclesOS.adil.customView

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import kotlin.math.*
import java.util.Locale

/** Curved, touch-driven weight dial used on the onboarding weight screen. */
class WeightArcScaleView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs) {
    
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
            
            // Precise haptic feedback
            val currentHapticStep = (field / 0.5).roundToInt()
            if (currentHapticStep != lastHapticStep) {
                lastHapticStep = currentHapticStep
                performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
            }
            invalidate()
        }
        
    var unit = "kg"
        set(newValue) {
            field = newValue; invalidate()
        }
    var onValueChanged: ((Double) -> Unit)? = null
    var onReadoutClicked: (() -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeCap = Paint.Cap.ROUND }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    
    private val majorTypeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    private val boldTypeface = Typeface.create("sans-serif", Typeface.BOLD)
    private val regularTypeface = Typeface.create("sans-serif", Typeface.NORMAL)

    private var downX = 0f
    private var downValue = value
    private var lastHapticStep = Int.MIN_VALUE

    // Pre-allocated objects for smooth performance
    private val arc1Rect = RectF()
    private val arc2Rect = RectF()
    private val plateRect = RectF()
    private val chevronPath = Path()
    private val accentColor = Color.rgb(249, 115, 22)

    init {
        isHapticFeedbackEnabled = true
        // Enabled for soft shadows and smooth gradients
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val centerX = w / 2f
        val centerY = h * .80f
        val radius = w * .78f
        
        val arc1Radius = radius * 1.0f
        arc1Rect.set(centerX - arc1Radius, centerY - arc1Radius, centerX + arc1Radius, centerY + arc1Radius)
        
        val arc2Radius = radius * 1.07f
        arc2Rect.set(centerX - arc2Radius, centerY - arc2Radius, centerX + arc2Radius, centerY + arc2Radius)
        
        plateRect.set(centerX - dp(112f), h * .52f, centerX + dp(112f), h * .70f)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val centerX = w / 2f
        val centerY = h * .80f
        val radius = w * .78f
        val rOuter = radius * .93f

        // 1. Draw Background Halos
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(1.4f)
        paint.color = Color.argb(80, 170, 174, 185)
        canvas.drawArc(arc1Rect, 204f, 132f, false, paint)

        paint.strokeWidth = dp(1.1f)
        paint.color = Color.argb(50, 170, 174, 185)
        canvas.drawArc(arc2Rect, 210f, 120f, false, paint)

        // 2. Draw Unified Tick Fan (Integer steps: 1kg per line)
        val visibleRange = 25.0
        val tickStep = 1.0 // Lines at 60, 61, 62...
        
        val lenMajor = dp(56f)   // 60, 70, 80...
        val lenMedium = dp(42f)  // 65, 75, 85...
        val lenMinor = dp(24f)   // 61, 62, 63...
        
        var tick = ((value - visibleRange) / tickStep).roundToInt() * tickStep
        while (tick <= value + visibleRange) {
            if (tick >= minValue && tick <= maxValue) {
                val offset = tick - value
                val angle = 270.0 + offset * 2.7
                
                if (angle in 202.0..338.0) {
                    val isMajor = abs(tick % 10.0) < .001
                    val isFive = abs(tick % 5.0) < .001 && !isMajor
                    
                    val length = when {
                        isMajor -> lenMajor
                        isFive -> lenMedium
                        else -> lenMinor
                    }
                    
                    paint.strokeWidth = dp(if (isMajor) 3.5f else if (isFive) 2.2f else 1.2f)
                    paint.color = when {
                        isMajor -> Color.rgb(230, 231, 239)
                        isFive -> Color.rgb(200, 200, 215)
                        else -> Color.rgb(130, 130, 145)
                    }
                    
                    val rad = angle * PI / 180.0
                    val cosA = cos(rad).toFloat()
                    val sinA = sin(rad).toFloat()
                    
                    canvas.drawLine(
                        centerX + cosA * rOuter,
                        centerY + sinA * rOuter,
                        centerX + cosA * (rOuter - length),
                        centerY + sinA * (rOuter - length),
                        paint
                    )

                    // Draw Labels only for major (10s)
                    if (isMajor) {
                        textPaint.color = Color.rgb(205, 207, 218)
                        textPaint.textSize = dp(18f)
                        textPaint.typeface = majorTypeface
                        val labelR = rOuter - dp(80f)
                        canvas.drawText(
                            tick.roundToInt().toString(),
                            centerX + cosA * labelR,
                            centerY + sinA * labelR + dp(7f),
                            textPaint
                        )
                    }
                }
            }
            tick += tickStep
        }

        // 3. Draw Center Indicator (Chevron)
        val chevronVertexY = centerY - (radius * 1.07f) - dp(18f)
        val topOfTicks = centerY - rOuter
        
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(2.6f)
        paint.color = accentColor
        canvas.drawLine(centerX, chevronVertexY, centerX, topOfTicks, paint)

        val chevronHalfWidth = dp(9f)
        val chevronHeight = dp(8f)
        chevronPath.reset()
        chevronPath.moveTo(centerX - chevronHalfWidth, chevronVertexY - chevronHeight)
        chevronPath.lineTo(centerX, chevronVertexY)
        chevronPath.lineTo(centerX + chevronHalfWidth, chevronVertexY - chevronHeight)
        canvas.drawPath(chevronPath, paint)

        // 4. Draw Readout Plate
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(24, 25, 31)
        paint.setShadowLayer(dp(14f), 0f, dp(6f), Color.argb(90, 0, 0, 0))
        canvas.drawRoundRect(plateRect, dp(30f), dp(30f), paint)
        paint.clearShadowLayer()

        textPaint.typeface = boldTypeface
        textPaint.textSize = dp(45f)
        textPaint.color = Color.WHITE
        canvas.drawText(
            String.format(Locale.US, "%.1f", value),
            centerX - dp(15f),
            h * .625f,
            textPaint
        )
        
        textPaint.typeface = regularTypeface
        textPaint.textSize = dp(20f)
        textPaint.color = Color.rgb(175, 175, 190)
        canvas.drawText(unit, centerX + dp(72f), h * .625f, textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downValue = value
                parent.requestDisallowInterceptTouchEvent(true)
                return true
            }
            // Handled Scroll Speed: 15dp per 1.0kg movement
            // This makes it zip from 60 to 70 with a natural swipe.
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.x - downX
                value = downValue + (deltaX / dp(15f))
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val upX = event.x
                val upY = event.y
                
                // If it was a tap on the readout plate, trigger callback
                if (abs(upX - downX) < dp(5f) && plateRect.contains(upX, upY)) {
                    onReadoutClicked?.invoke()
                }

                parent.requestDisallowInterceptTouchEvent(false)
                performClick()
                return true
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun dp(value: Float) = value * resources.displayMetrics.density
}

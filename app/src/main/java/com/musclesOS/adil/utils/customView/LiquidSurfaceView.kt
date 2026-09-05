package com.musclesOS.adil.utils.customView

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.view.animation.PathInterpolatorCompat
import com.musclesOS.adil.R
import kotlin.math.sin

/**
 * A custom view that renders a sine-wave animation to reveal or cover screen content.
 * Supports a "reveal mode" where it acts as a curtain being pulled up.
 */
class WaveRevealView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val wavePath = Path()

    private var waveLevel = 0f   // y-position of the wave crest baseline; animates upward
    private var phase = 0f       // drives the horizontal ripple motion

    private val amplitude = 40f
    private val frequency = 1.6f // number of ripples across the width

    private var levelAnimator: ValueAnimator? = null
    private var phaseAnimator: ValueAnimator? = null
    private var isRevealMode = false

    init {
        paint.color = Color.WHITE
        backgroundPaint.color = Color.TRANSPARENT
        visibility = INVISIBLE
    }

    /**
     * Sets the color of the wave line/fill.
     */
    fun setWaveColor(@ColorInt color: Int) {
        paint.color = color
    }

    /**
     * Sets the background color drawn behind/above the wave in reveal mode.
     */
    fun setBehindColor(@ColorInt color: Int) {
        backgroundPaint.color = color
    }

    /** Kicks off the wave rising to fully cover the screen, then calls [onEnd]. */
    fun reveal(duration: Long = 1100L, isReveal: Boolean = false, onEnd: (() -> Unit)? = null) {
        this.isRevealMode = isReveal
        visibility = VISIBLE
        waveLevel = height.toFloat() + amplitude

        levelAnimator?.cancel()
        levelAnimator = ValueAnimator.ofFloat(height.toFloat() + amplitude, -amplitude).apply {
            this.duration = duration
            interpolator = PathInterpolatorCompat.create(0.22f, 0.61f, 0.36f, 1f) // smooth decelerate
            addUpdateListener {
                waveLevel = it.animatedValue as Float
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) = onEnd?.invoke() ?: Unit
            })
            start()
        }

        phaseAnimator?.cancel()
        phaseAnimator = ValueAnimator.ofFloat(0f, (2 * Math.PI).toFloat()).apply {
            this.duration = 900
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                phase = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.color = ContextCompat.getColor(context, R.color.wave_color)
        if (visibility != VISIBLE || width == 0) return

        val step = 8

        if (isRevealMode && backgroundPaint.color != Color.TRANSPARENT) {
            // Fill the area ABOVE the wave with the background color (e.g., Splash Orange)
            val bgPath = Path()
            bgPath.moveTo(0f, 0f)
            var x = 0f
            while (x <= width) {
                val y = waveLevel + amplitude * sin((x / width) * frequency * 2 * Math.PI + phase).toFloat()
                bgPath.lineTo(x, y)
                x += step
            }
            bgPath.lineTo(width.toFloat(), 0f)
            bgPath.close()
            canvas.drawPath(bgPath, backgroundPaint)

            // Draw a white "crest" band that follows the wave line
            wavePath.reset()
            x = 0f
            while (x <= width) {
                val y = waveLevel + amplitude * sin((x / width) * frequency * 2 * Math.PI + phase).toFloat()
                if (x == 0f) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
                x += step
            }
            // Thickness to the wave
            x = width.toFloat()
            while (x >= 0) {
                val y = waveLevel + 1f + amplitude * sin((x / width) * frequency * 2 * Math.PI + phase).toFloat()
                wavePath.lineTo(x, y)
                x -= step
            }
            wavePath.close()
            canvas.drawPath(wavePath, paint)
        } else {
            // Default behavior fill everything below the wave
            wavePath.reset()
            wavePath.moveTo(0f, height.toFloat())
            var x = 0f
            while (x <= width) {
                val y = waveLevel + amplitude * sin((x / width) * frequency * 2 * Math.PI + phase).toFloat()
                wavePath.lineTo(x, y)
                x += step
            }
            wavePath.lineTo(width.toFloat(), height.toFloat())
            wavePath.close()
            canvas.drawPath(wavePath, paint)
        }
    }

    /**
     * Stops all active animations (level and phase).
     */
    fun stopAnimations() {
        levelAnimator?.cancel()
        phaseAnimator?.cancel()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimations()
    }
}
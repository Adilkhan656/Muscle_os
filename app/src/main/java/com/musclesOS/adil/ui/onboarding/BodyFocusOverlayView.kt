package com.musclesOS.adil.ui.onboarding

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import com.musclesOS.adil.data.MuscleOption

/** Soft region highlights layered over the body image (no marker circles). */
class BodyFocusOverlayView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var selected: List<MuscleOption> = emptyList()
    private var imageLeft = 0f
    private var imageTop = 0f
    private var imageWidth = 0f
    private var imageHeight = 0f

    fun setImageBounds(left: Float, top: Float, width: Float, height: Float) {
        imageLeft = left; imageTop = top; imageWidth = width; imageHeight = height; invalidate()
    }

    fun setHighlights(options: Collection<MuscleOption>) {
        selected = options.filter { it.id != "full_body" }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        selected.forEach { option ->
            val x = imageLeft + option.xBias * imageWidth
            val y = imageTop + option.yBias * imageHeight
            
            // Use canvas width (container) instead of imageWidth to keep glow size constant
            val baseRadius = width * 0.12f
            val radius = baseRadius * option.glowScale
            
            val rectLeft = x - radius * option.hScale
            val rectTop = y - radius * option.vScale
            val rectRight = x + radius * option.hScale
            val rectBottom = y + radius * option.vScale

            // Softer "Vapor" glow gradient
            paint.shader = RadialGradient(
                x, y, radius * maxOf(option.hScale, option.vScale), 
                intArrayOf(0x88F97316.toInt(), 0x1AF97316.toInt(), 0x00F97316),
                floatArrayOf(0f, .55f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawOval(rectLeft, rectTop, rectRight, rectBottom, paint)
        }
        paint.shader = null
    }
}

/**
 * BodyMapView.kt
 *
 * A custom View that shows a body diagram and lets the user tap a muscle
 * group. On tap, it highlights the shape and draws a callout (dot + line +
 * label) similar to the reference "Muscle Groups" infographic.
 *
 * Drop this file into your Kotlin sources and use it in XML like:
 *
 *   <com.muscleos.ui.BodyMapView
 *       android:id="@+id/bodyMap"
 *       android:layout_width="match_parent"
 *       android:layout_height="match_parent"
 *       app:bodyBackground="@drawable/body_silhouette" />
 *
 * and listen for selection:
 *
 *   bodyMap.onMuscleSelected = { muscle -> viewModel.selectMuscle(muscle.id) }
 */

package com.musclesOS.adil.ui.onboarding

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.PathParser // AndroidX has a public PathParser you can use safely

/** One tappable muscle group. */
data class MuscleRegion(
    val id: String,
    val displayName: String,
    val pathData: String,      // SVG "d" attribute string for this muscle's outline
    val anchor: PointF,        // point on the body where the leader-line starts (e.g. centroid)
    val labelSide: LabelSide,  // which side of the screen the label box should sit on
    val labelSlot: Int         // vertical ordering (0 = topmost label on that side)
) {
    lateinit var path: Path
    lateinit var region: Region
    var isSelected: Boolean = false
}

enum class LabelSide { LEFT, RIGHT }

class BodyMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var onMuscleSelected: ((MuscleRegion) -> Unit)? = null

    // ---- Paints ----
    private val highlightFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF8A00") // orange, matches your app accent
        alpha = 140
        style = Paint.Style.FILL
    }
    private val lineePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E5484D")
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E5484D")
        style = Paint.Style.FILL
    }
    private val labelBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2B2B45")
        style = Paint.Style.FILL
    }
    private val labelTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 34f
        textAlign = Paint.Align.LEFT
    }

    private var bodyBitmap: Bitmap? = null
    private val regions = mutableListOf<MuscleRegion>()

    /** Call once you know the view's real pixel size (paths are authored against a fixed viewBox, e.g. 660x980). */
    private val viewBoxWidth = 660f
    private val viewBoxHeight = 980f
    private var scaleMatrix = Matrix()

    fun setBodyBackground(bitmap: Bitmap) {
        bodyBitmap = bitmap
        invalidate()
    }

    /** Load your muscle definitions (normally parsed once from a JSON/SVG asset). */
    fun setRegions(defs: List<MuscleRegion>) {
        regions.clear()
        defs.forEach { def ->
            def.path = PathParser.createPathFromPathData(def.pathData)
            val bounds = RectF()
            def.path.computeBounds(bounds, true)
            def.region = Region().apply {
                setPath(
                    def.path,
                    Region(bounds.left.toInt(), bounds.top.toInt(), bounds.right.toInt(), bounds.bottom.toInt())
                )
            }
            regions.add(def)
        }
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val scale = minOf(w / viewBoxWidth, h / viewBoxHeight)
        scaleMatrix = Matrix().apply {
            setScale(scale, scale)
            postTranslate((w - viewBoxWidth * scale) / 2f, (h - viewBoxHeight * scale) / 2f)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        bodyBitmap?.let { bmp ->
            canvas.drawBitmap(bmp, null, RectF(0f, 0f, width.toFloat(), height.toFloat()), null)
        }

        canvas.save()
        canvas.concat(scaleMatrix)

        // Highlight fill for the selected region, drawn in view-box space
        regions.firstOrNull { it.isSelected }?.let { muscle ->
            canvas.drawPath(muscle.path, highlightFillPaint)
        }
        canvas.restore()

        // Callout (dot + line + label) drawn in *screen* space so the label box
        // doesn't get squashed by the scale matrix.
        regions.firstOrNull { it.isSelected }?.let { muscle ->
            drawCallout(canvas, muscle)
        }
    }

    private fun drawCallout(canvas: Canvas, muscle: MuscleRegion) {
        val pts = floatArrayOf(muscle.anchor.x, muscle.anchor.y)
        scaleMatrix.mapPoints(pts)
        val anchorX = pts[0]
        val anchorY = pts[1]

        canvas.drawCircle(anchorX, anchorY, 8f, dotPaint)

        val labelText = muscle.displayName.uppercase()
        val textWidth = labelTextPaint.measureText(labelText)
        val boxPadding = 24f
        val boxHeight = 70f

        val boxLeft = if (muscle.labelSide == LabelSide.LEFT)
            40f else width - textWidth - boxPadding * 2 - 40f
        val boxTop = 60f + muscle.labelSlot * (boxHeight + 24f)

        val elbowX = if (muscle.labelSide == LabelSide.LEFT)
            boxLeft + textWidth + boxPadding * 2 else boxLeft
        val elbowY = boxTop + boxHeight / 2f

        // leader line: anchor -> elbow -> box edge
        canvas.drawLine(anchorX, anchorY, elbowX, elbowY, lineePaint)

        val boxRect = RectF(boxLeft, boxTop, boxLeft + textWidth + boxPadding * 2, boxTop + boxHeight)
        canvas.drawRoundRect(boxRect, 16f, 16f, labelBgPaint)
        canvas.drawText(labelText, boxLeft + boxPadding, boxTop + boxHeight / 2f + 12f, labelTextPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            // Map the tap point from screen space into view-box space (inverse of scaleMatrix)
            val inverse = Matrix()
            scaleMatrix.invert(inverse)
            val pts = floatArrayOf(event.x, event.y)
            inverse.mapPoints(pts)
            val vx = pts[0].toInt()
            val vy = pts[1].toInt()

            val hit = regions.firstOrNull { it.region.contains(vx, vy) }
            if (hit != null) {
                regions.forEach { it.isSelected = (it.id == hit.id) }
                invalidate()
                onMuscleSelected?.invoke(hit)
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}

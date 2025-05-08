package com.education.quran8

import android.content.Context
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max
import kotlin.math.min

class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private val scaleDetector: ScaleGestureDetector
    private val matrix = Matrix()
    private var scaleFactor = 1f
    private var mode = NONE

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var activePointerId = -1

    private var isPortraitMode = true

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
    }

    init {
        scaleType = ScaleType.MATRIX
        scaleDetector = ScaleGestureDetector(context, ScaleListener())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                mode = DRAG
                lastTouchX = event.x
                lastTouchY = event.y
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                mode = ZOOM
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode == DRAG) {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY
                    matrix.postTranslate(dx, dy)
                    lastTouchX = event.x
                    lastTouchY = event.y
                    setImageMatrix(matrix)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
            }
        }
        return true
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = max(1f, min(scaleFactor, 3f)) // Limit zoom between 1x and 3x

            // Restrict scaling based on orientation
            if (isPortraitMode) {
                scaleFactor = min(scaleFactor, height.toFloat() / drawable.intrinsicHeight)
            } else {
                scaleFactor = min(scaleFactor, width.toFloat() / drawable.intrinsicWidth)
            }

            matrix.setScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
            setImageMatrix(matrix)
            return true
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        isPortraitMode = w < h // Determine orientation based on width and height
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        resetZoom()
    }

    private fun resetZoom() {
        matrix.reset()
        scaleFactor = 1f
        setImageMatrix(matrix)
    }
}

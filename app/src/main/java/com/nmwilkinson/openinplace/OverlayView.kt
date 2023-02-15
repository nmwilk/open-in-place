package com.nmwilkinson.openinplace

import android.content.Context
import android.graphics.*
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import kotlin.random.Random

class OverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var drawAtPoint: PointF? = null

    var closeListener: (() -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x70000000.or(0x00FFFFFF.and(Random(SystemClock.currentThreadTimeMillis()).nextInt()))
        style = Paint.Style.FILL
    }

    init {
        isClickable = true
        setWillNotDraw(false)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        drawAtPoint?.let { canvas.drawCircle(it.x, it.y, width * 0.05f, paint) }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            drawAtPoint = PointF(event.x, event.y)
            invalidate()
            closeListener?.invoke()
            closeListener = null
        }
        return true
    }
}
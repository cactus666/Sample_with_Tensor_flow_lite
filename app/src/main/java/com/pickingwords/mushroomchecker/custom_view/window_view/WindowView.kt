package com.pickingwords.mushroomchecker.custom_view.window_view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import com.pickingwords.mushroomchecker.R


class WindowView(context: Context, attributeSet: AttributeSet): View(context, attributeSet){

    private var marginLeftAndRight = 40
    private var size = 0
    private var centerOnVertical = 0
    private val radius = 50f

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        marginLeftAndRight = width / 20
    }

    fun initializeSizeLimitation(topLimitation: Int, bottomLimitation: Int) {
        centerOnVertical = (bottomLimitation + topLimitation) / 2
        size = (bottomLimitation - topLimitation - marginLeftAndRight * 2).coerceAtMost((right - left - marginLeftAndRight * 2))
        invalidate()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paint = Paint(ANTI_ALIAS_FLAG)
        paint.color = resources.getColor(R.color.color_camera_shadow)
        paint.style = Paint.Style.FILL

        val paintStroke = Paint(ANTI_ALIAS_FLAG)
        paintStroke.color = resources.getColor(android.R.color.holo_red_light)
        paintStroke.style = Paint.Style.STROKE
        paintStroke.strokeWidth = 2f

        canvas.drawPaint(paint)

        val circleRect = RectF(
            ((width - size) / 2).toFloat(),
            (centerOnVertical - size / 2).toFloat(),
            ((width + size) / 2).toFloat(),
            (centerOnVertical + size / 2).toFloat()
        )

        //Draw transparent shape
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.drawRoundRect(circleRect, radius, radius, paint)

        canvas.drawRoundRect(circleRect, radius, radius, paintStroke)

    }

    fun getTopLimitPercent(): Float {
        return (centerOnVertical - size / 2).toFloat() / height
    }

    fun getBottomLimitPercent(): Float {
        return (centerOnVertical + size / 2).toFloat() / height
    }

    fun getLeftLimitPercent(): Float {
        return ((width - size) / 2).toFloat() / width
    }

    fun getRightLimitPercent(): Float {
        return ((width + size) / 2).toFloat() / width
    }

}
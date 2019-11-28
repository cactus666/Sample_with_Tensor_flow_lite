package com.pickingwords.mushroomchecker.custom_view.window_view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import androidx.core.content.ContextCompat
import com.pickingwords.mushroomchecker.R


class WindowView(context: Context, attributeSet: AttributeSet): View(context, attributeSet){

    private var marginLeftAndRight = 40
    private var size = 0
    private var centerOnVertical = 0
    private val radius = 50f
    private var stateWindowView: State = State.NORMAL
    private val paintStroke: Paint = Paint(ANTI_ALIAS_FLAG)

    init {
        paintStroke.color = ContextCompat.getColor(context, R.color.color_stroke_normal)
        paintStroke.style = Paint.Style.STROKE
        paintStroke.strokeWidth = 3f
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        marginLeftAndRight = width / 20
    }

    fun initializeSizeLimitation(topLimitation: Int, bottomLimitation: Int) {
        centerOnVertical = (bottomLimitation + topLimitation) / 2
        size = (bottomLimitation - topLimitation - marginLeftAndRight * 2).coerceAtMost((right - left - marginLeftAndRight * 2))
        invalidate()
    }




    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paintLid = Paint(ANTI_ALIAS_FLAG)
        paintLid.color = ContextCompat.getColor(context, R.color.color_camera_shadow)
        paintLid.style = Paint.Style.FILL

        canvas.drawPaint(paintLid)

        val circleRect = RectF(
            ((width - size) / 2).toFloat(),
            (centerOnVertical - size / 2).toFloat(),
            ((width + size) / 2).toFloat(),
            (centerOnVertical + size / 2).toFloat()
        )

        //Draw transparent shape
        paintLid.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        canvas.drawRoundRect(circleRect, radius, radius, paintLid)

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

    fun onHarmful() {
        stateWindowView = State.HARMFUL
        setColorForStroke(stateWindowView, paintStroke)
    }

    fun onFail() {
        stateWindowView = State.NORMAL
        setColorForStroke(stateWindowView, paintStroke)
    }

    fun onWin() {
        stateWindowView = State.WIN
        setColorForStroke(stateWindowView, paintStroke)
    }

    fun onRestart() {
        stateWindowView = State.NORMAL
        setColorForStroke(stateWindowView, paintStroke)
    }

    private fun setColorForStroke(state: State, paintStroke: Paint) {
        when (state) {
            State.NORMAL -> paintStroke.color = ContextCompat.getColor(context, R.color.color_stroke_normal)
            State.WIN -> paintStroke.color = ContextCompat.getColor(context, R.color.color_stroke_win)
            State.HARMFUL -> paintStroke.color = ContextCompat.getColor(context, R.color.color_stroke_harmful)
        }
        invalidate()
    }

}
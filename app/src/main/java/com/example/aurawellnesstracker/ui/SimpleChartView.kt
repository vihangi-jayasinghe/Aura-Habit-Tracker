package com.example.aurawellnesstracker.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class SimpleChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val dataPoints = mutableListOf<Float>()
    private val paint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 4f
        isAntiAlias = true
    }
    private val pointPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 8f
        isAntiAlias = true
    }

    fun setData(data: List<Float>) {
        dataPoints.clear()
        dataPoints.addAll(data)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (dataPoints.size < 2) return

        val width = width.toFloat()
        val height = height.toFloat()
        val maxValue = dataPoints.maxOrNull() ?: 1f

        // Draw lines between points
        for (i in 0 until dataPoints.size - 1) {
            val x1 = (i * width / (dataPoints.size - 1))
            val y1 = height - (dataPoints[i] / maxValue * height)
            val x2 = ((i + 1) * width / (dataPoints.size - 1))
            val y2 = height - (dataPoints[i + 1] / maxValue * height)

            canvas.drawLine(x1, y1, x2, y2, paint)
        }

        // Draw points
        for (i in dataPoints.indices) {
            val x = (i * width / (dataPoints.size - 1))
            val y = height - (dataPoints[i] / maxValue * height)
            canvas.drawCircle(x, y, 8f, pointPaint)
        }
    }
}
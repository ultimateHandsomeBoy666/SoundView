package com.taobao.android.soundwave

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs

class WaveformView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val barPaint = Paint().apply {
        color = Color.parseColor("#8A2BE2") // BlueViolet
        style = Paint.Style.FILL
    }

    private var audioData = ShortArray(0)
    private val numBars = 50

    fun updateData(data: ShortArray) {
        this.audioData = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerY = height / 2f

        if (audioData.isEmpty()) {
            canvas.drawLine(0f, centerY, width, centerY, barPaint)
            return
        }

        val barWidth = width / numBars
        val barPadding = barWidth * 0.2f // Add some space between bars
        val samplesPerBar = audioData.size / numBars

        if (samplesPerBar == 0) return

        for (i in 0 until numBars) {
            val startSample = i * samplesPerBar
            val endSample = (i + 1) * samplesPerBar

            var maxAmplitude = 0
            for (j in startSample until endSample) {
                if (abs(audioData[j].toInt()) > maxAmplitude) {
                    maxAmplitude = abs(audioData[j].toInt())
                }
            }

            val barHeight = (maxAmplitude / 32767f) * centerY

            val left = i * barWidth + barPadding / 2
            val top = centerY - barHeight
            val right = (i + 1) * barWidth - barPadding / 2
            val bottom = centerY + barHeight

            canvas.drawRect(left, top, right, bottom, barPaint)
        }
    }
}
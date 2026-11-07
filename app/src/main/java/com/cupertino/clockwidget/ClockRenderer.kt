package com.cupertino.clockwidget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-fidelity Canvas renderer for authentic iOS (Cupertino) Clock dials.
 * Follows Apple Human Interface Guidelines:
 * - Minimalist tick marks (bold quarters at 12, 3, 6, 9)
 * - Rounded pill hour and minute hands
 * - Signature Cupertino orange (#FF9500) second hand and center pivot
 */
object ClockRenderer {

    fun renderDial(
        size: Int = 512,
        timeZone: TimeZone = TimeZone.getDefault(),
        isDarkTheme: Boolean = true,
        showSecondHand: Boolean = true
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val center = size / 2f
        val radius = size * 0.44f

        val dialBgColor = if (isDarkTheme) Color.parseColor("#121214") else Color.parseColor("#F5F5F7")
        val tickColor = if (isDarkTheme) Color.parseColor("#AEAEB2") else Color.parseColor("#8E8E93")
        val handColor = if (isDarkTheme) Color.WHITE else Color.BLACK
        val orangeColor = Color.parseColor("#FF9500")

        // 1. Draw Dial Circle Background
        val dialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = dialBgColor
            style = Paint.Style.FILL
        }
        canvas.drawCircle(center, center, radius, dialPaint)

        // 2. Subtle Outer Border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isDarkTheme) Color.parseColor("#2C2C2E") else Color.parseColor("#E5E5EA")
            style = Paint.Style.STROKE
            strokeWidth = size * 0.012f
        }
        canvas.drawCircle(center, center, radius, borderPaint)

        // 3. Draw Hour & Minute Ticks
        val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeCap = Paint.Cap.ROUND
        }

        for (i in 0 until 60) {
            val angle = Math.toRadians((i * 6 - 90).toDouble())
            val isHour = i % 5 == 0
            val isMajorQuarter = i % 15 == 0

            val tickLength = when {
                isMajorQuarter -> size * 0.065f
                isHour -> size * 0.045f
                else -> size * 0.02f
            }

            tickPaint.strokeWidth = when {
                isMajorQuarter -> size * 0.024f
                isHour -> size * 0.016f
                else -> size * 0.007f
            }

            tickPaint.color = when {
                isMajorQuarter -> handColor
                isHour -> tickColor
                else -> if (isDarkTheme) Color.parseColor("#3A3A3C") else Color.parseColor("#D1D1D6")
            }

            val startX = (center + (radius - tickLength - size * 0.015f) * cos(angle)).toFloat()
            val startY = (center + (radius - tickLength - size * 0.015f) * sin(angle)).toFloat()
            val endX = (center + (radius - size * 0.015f) * cos(angle)).toFloat()
            val endY = (center + (radius - size * 0.015f) * sin(angle)).toFloat()

            canvas.drawLine(startX, startY, endX, endY, tickPaint)
        }

        // 4. Calculate Time
        val calendar = Calendar.getInstance(timeZone)
        val hours = calendar.get(Calendar.HOUR)
        val minutes = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)

        val hourAngle = Math.toRadians(((hours + minutes / 60f) * 30 - 90).toDouble())
        val minuteAngle = Math.toRadians(((minutes + seconds / 60f) * 6 - 90).toDouble())
        val secondAngle = Math.toRadians((seconds * 6 - 90).toDouble())

        // 5. Draw Hour Hand (Rounded Pill)
        val hourPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = handColor
            strokeCap = Paint.Cap.ROUND
            strokeWidth = size * 0.038f
        }
        val hourLength = radius * 0.52f
        val hourTail = radius * 0.1f
        canvas.drawLine(
            (center - hourTail * cos(hourAngle)).toFloat(),
            (center - hourTail * sin(hourAngle)).toFloat(),
            (center + hourLength * cos(hourAngle)).toFloat(),
            (center + hourLength * sin(hourAngle)).toFloat(),
            hourPaint
        )

        // 6. Draw Minute Hand (Longer Sleek Pill)
        val minutePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = handColor
            strokeCap = Paint.Cap.ROUND
            strokeWidth = size * 0.026f
        }
        val minuteLength = radius * 0.76f
        val minuteTail = radius * 0.12f
        canvas.drawLine(
            (center - minuteTail * cos(minuteAngle)).toFloat(),
            (center - minuteTail * sin(minuteAngle)).toFloat(),
            (center + minuteLength * cos(minuteAngle)).toFloat(),
            (center + minuteLength * sin(minuteAngle)).toFloat(),
            minutePaint
        )

        // 7. Draw Signature Cupertino Orange Second Hand
        if (showSecondHand) {
            val secondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = orangeColor
                strokeCap = Paint.Cap.ROUND
                strokeWidth = size * 0.014f
            }
            val secondLength = radius * 0.85f
            val secondTail = radius * 0.22f
            canvas.drawLine(
                (center - secondTail * cos(secondAngle)).toFloat(),
                (center - secondTail * sin(secondAngle)).toFloat(),
                (center + secondLength * cos(secondAngle)).toFloat(),
                (center + secondLength * sin(secondAngle)).toFloat(),
                secondPaint
            )

            // Orange Center Hub Pin
            val hubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = orangeColor
                style = Paint.Style.FILL
            }
            canvas.drawCircle(center, center, size * 0.024f, hubPaint)

            // Inner Tiny Black Dot in Hub
            val innerDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                style = Paint.Style.FILL
            }
            canvas.drawCircle(center, center, size * 0.008f, innerDotPaint)
        }

        return bitmap
    }
}

package com.cupertino.clockwidget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-fidelity Canvas renderer for iOS-style Clock widgets.
 * Renders the iconic Squircle Clock with a Dynamic Seconds Counter:
 * - 60 perimeter ticks that dynamically fill as a seconds counter (0 to 59s)
 * - Trailing elapsed seconds are filled solid
 * - The active current second is highlighted in signature Cupertino Orange (#FF9500)
 * - Remaining seconds are displayed as subtle inactive dots/dashes
 */
object ClockRenderer {

    /**
     * Renders the Squircle Clock with Dynamic Seconds Counter
     */
    fun renderSquircleClock(
        size: Int = 512,
        timeZone: TimeZone = TimeZone.getDefault(),
        is24Hour: Boolean = false,
        isDarkTheme: Boolean = false,
        forcedSecond: Int? = null
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val cx = size / 2f
        val cy = size / 2f

        // Get Current Time & Seconds
        val calendar = Calendar.getInstance(timeZone)
        val currentSecond = forcedSecond ?: calendar.get(Calendar.SECOND)

        // Theme Palettes
        val cardBgColor = if (isDarkTheme) Color.parseColor("#1C1C1E") else Color.parseColor("#FFFFFF")
        val textColor = if (isDarkTheme) Color.WHITE else Color.parseColor("#0A0A0A")
        val activeTickColor = if (isDarkTheme) Color.WHITE else Color.parseColor("#0A0A0A")
        val inactiveTickColor = if (isDarkTheme) Color.parseColor("#2C2C2E") else Color.parseColor("#E5E5EA")
        val currentSecondColor = Color.parseColor("#FF9500") // Cupertino orange runner
        val shadowColor = if (isDarkTheme) Color.parseColor("#30000000") else Color.parseColor("#1F000000")

        // 1. Soft Ambient Drop Shadow
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = shadowColor
            style = Paint.Style.FILL
        }
        val shadowOffset = size * 0.025f
        val cardMargin = size * 0.055f
        val cardRadius = size * 0.22f
        val shadowRect = RectF(
            cardMargin,
            cardMargin + shadowOffset,
            size - cardMargin,
            size - cardMargin + shadowOffset
        )
        canvas.drawRoundRect(shadowRect, cardRadius, cardRadius, shadowPaint)

        // 2. Main Squircle Card Background
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = cardBgColor
            style = Paint.Style.FILL
        }
        val cardRect = RectF(cardMargin, cardMargin, size - cardMargin, size - cardMargin)
        canvas.drawRoundRect(cardRect, cardRadius, cardRadius, cardPaint)

        // Subtle Card Border / Rim
        val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isDarkTheme) Color.parseColor("#2C2C2E") else Color.parseColor("#EFEFF4")
            style = Paint.Style.STROKE
            strokeWidth = size * 0.008f
        }
        canvas.drawRoundRect(cardRect, cardRadius, cardRadius, rimPaint)

        // 3. Draw 60 Perimeter Ticks as Dynamic Seconds Counter
        val tickMargin = cardMargin + size * 0.052f
        val tickW = size - 2 * tickMargin
        val tickH = size - 2 * tickMargin
        val tickRadius = cardRadius * 0.76f

        val baseTickLength = size * 0.045f
        val baseTickStroke = size * 0.013f

        val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeCap = Paint.Cap.ROUND
        }

        // Generate 60 points along rounded rectangle perimeter (0 = 12 o'clock, 59 = 11:59 position)
        val ticks = getRoundedRectPerimeterPoints(
            cx = cx,
            cy = cy,
            width = tickW,
            height = tickH,
            radius = tickRadius,
            count = 60
        )

        for (i in ticks.indices) {
            val tick = ticks[i]
            val isCurrent = (i == currentSecond)
            val isElapsed = (i <= currentSecond)

            // Dynamic Second State:
            // - Current second: vibrant Cupertino orange runner with slight accent length
            // - Elapsed seconds: solid active black/white dots
            // - Upcoming seconds: subtle muted inactive dots
            val tickColor = when {
                isCurrent -> currentSecondColor
                isElapsed -> activeTickColor
                else -> inactiveTickColor
            }

            val tickLength = when {
                isCurrent -> baseTickLength * 1.35f
                isElapsed -> baseTickLength
                else -> baseTickLength * 0.85f
            }

            val tickStroke = when {
                isCurrent -> baseTickStroke * 1.35f
                isElapsed -> baseTickStroke
                else -> baseTickStroke * 0.85f
            }

            tickPaint.color = tickColor
            tickPaint.strokeWidth = tickStroke

            val startX = tick.x
            val startY = tick.y
            val endX = tick.x + tick.nx * tickLength
            val endY = tick.y + tick.ny * tickLength

            canvas.drawLine(startX, startY, endX, endY, tickPaint)
        }

        // 4. Draw Bold Condensed Digital Time (e.g., "10:29")
        val pattern = if (is24Hour) "HH:mm" else "h:mm"
        val timeFormat = SimpleDateFormat(pattern, Locale.US).apply {
            this.timeZone = timeZone
        }
        val timeText = timeFormat.format(calendar.time)

        val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = size * 0.355f
            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = -0.02f
        }

        // Perfect Vertical Centering
        val bounds = Rect()
        timePaint.getTextBounds(timeText, 0, timeText.length, bounds)
        val textY = cy - bounds.exactCenterY()

        canvas.drawText(timeText, cx, textY, timePaint)

        return bitmap
    }

    /**
     * Classic iOS Analog Dial (preserved for multi-style widgets)
     */
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

        val dialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = dialBgColor
            style = Paint.Style.FILL
        }
        canvas.drawCircle(center, center, radius, dialPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isDarkTheme) Color.parseColor("#2C2C2E") else Color.parseColor("#E5E5EA")
            style = Paint.Style.STROKE
            strokeWidth = size * 0.012f
        }
        canvas.drawCircle(center, center, radius, borderPaint)

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

        val calendar = Calendar.getInstance(timeZone)
        val hours = calendar.get(Calendar.HOUR)
        val minutes = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)

        val hourAngle = Math.toRadians(((hours + minutes / 60f) * 30 - 90).toDouble())
        val minuteAngle = Math.toRadians(((minutes + seconds / 60f) * 6 - 90).toDouble())
        val secondAngle = Math.toRadians((seconds * 6 - 90).toDouble())

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

            val hubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = orangeColor
                style = Paint.Style.FILL
            }
            canvas.drawCircle(center, center, size * 0.024f, hubPaint)

            val innerDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                style = Paint.Style.FILL
            }
            canvas.drawCircle(center, center, size * 0.008f, innerDotPaint)
        }

        return bitmap
    }

    data class PerimeterPoint(val x: Float, val y: Float, val nx: Float, val ny: Float)

    /**
     * Calculates evenly-spaced points and inward normal vectors along a rounded rectangle perimeter.
     */
    private fun getRoundedRectPerimeterPoints(
        cx: Float,
        cy: Float,
        width: Float,
        height: Float,
        radius: Float,
        count: Int
    ): List<PerimeterPoint> {
        val left = cx - width / 2f
        val right = cx + width / 2f
        val top = cy - height / 2f
        val bottom = cy + height / 2f

        val hStraight = width - 2 * radius
        val vStraight = height - 2 * radius
        val arcLen = (0.5 * Math.PI * radius).toFloat()

        val totalPerimeter = 2 * hStraight + 2 * vStraight + 4 * arcLen
        val step = totalPerimeter / count

        val points = mutableListOf<PerimeterPoint>()

        for (i in 0 until count) {
            var d = (i * step) % totalPerimeter

            // Section 1: Top edge (right half, from center to top-right corner)
            val topHalf = hStraight / 2f
            if (d < topHalf) {
                val px = cx + d
                val py = top
                points.add(PerimeterPoint(px, py, 0f, 1f))
                continue
            }
            d -= topHalf

            // Section 2: Top-right corner arc (from -PI/2 to 0)
            if (d < arcLen) {
                val theta = (-Math.PI / 2.0 + (d / radius)).toDouble()
                val px = (right - radius + radius * cos(theta)).toFloat()
                val py = (top + radius + radius * sin(theta)).toFloat()
                points.add(PerimeterPoint(px, py, -cos(theta).toFloat(), -sin(theta).toFloat()))
                continue
            }
            d -= arcLen

            // Section 3: Right edge (top to bottom)
            if (d < vStraight) {
                val px = right
                val py = top + radius + d
                points.add(PerimeterPoint(px, py, -1f, 0f))
                continue
            }
            d -= vStraight

            // Section 4: Bottom-right corner arc (from 0 to PI/2)
            if (d < arcLen) {
                val theta = (d / radius).toDouble()
                val px = (right - radius + radius * cos(theta)).toFloat()
                val py = (bottom - radius + radius * sin(theta)).toFloat()
                points.add(PerimeterPoint(px, py, -cos(theta).toFloat(), -sin(theta).toFloat()))
                continue
            }
            d -= arcLen

            // Section 5: Bottom edge (right to left)
            if (d < hStraight) {
                val px = (right - radius) - d
                val py = bottom
                points.add(PerimeterPoint(px, py, 0f, -1f))
                continue
            }
            d -= hStraight

            // Section 6: Bottom-left corner arc (from PI/2 to PI)
            if (d < arcLen) {
                val theta = (Math.PI / 2.0 + (d / radius)).toDouble()
                val px = (left + radius + radius * cos(theta)).toFloat()
                val py = (bottom - radius + radius * sin(theta)).toFloat()
                points.add(PerimeterPoint(px, py, -cos(theta).toFloat(), -sin(theta).toFloat()))
                continue
            }
            d -= arcLen

            // Section 7: Left edge (bottom to top)
            if (d < vStraight) {
                val px = left
                val py = (bottom - radius) - d
                points.add(PerimeterPoint(px, py, 1f, 0f))
                continue
            }
            d -= vStraight

            // Section 8: Top-left corner arc (from PI to 3*PI/2)
            if (d < arcLen) {
                val theta = (Math.PI + (d / radius)).toDouble()
                val px = (left + radius + radius * cos(theta)).toFloat()
                val py = (top + radius + radius * sin(theta)).toFloat()
                points.add(PerimeterPoint(px, py, -cos(theta).toFloat(), -sin(theta).toFloat()))
                continue
            }
            d -= arcLen

            // Section 9: Top edge (left half, back to center)
            val px = (left + radius) + d
            val py = top
            points.add(PerimeterPoint(px, py, 0f, 1f))
        }

        return points
    }
}

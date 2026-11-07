package com.cupertino.clockwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.util.Calendar
import java.util.TimeZone

/**
 * AppWidgetProvider for the 2x2 iOS Analog World Clock Widget.
 */
class IosAnalogWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_analog_clock)

            // Render Cupertino Analog Dial
            val timeZone = TimeZone.getDefault()
            val clockBitmap = ClockRenderer.renderDial(
                size = 380,
                timeZone = timeZone,
                isDarkTheme = true,
                showSecondHand = false // Widgets conserve battery by refreshing on minute intervals
            )
            views.setImageViewBitmap(R.id.iv_clock_face, clockBitmap)

            // Set City & Offset Info
            val city = context.getSharedPreferences("cupertino_prefs", Context.MODE_PRIVATE)
                .getString("selected_city", "CUPERTINO") ?: "CUPERTINO"
            views.setTextViewText(R.id.tv_city_name, city.uppercase())
            views.setTextViewText(R.id.tv_time_offset, calculateOffsetString(timeZone))

            // Open App on Click
            val clickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.analog_widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun calculateOffsetString(tz: TimeZone): String {
            val offsetHours = tz.rawOffset / (1000 * 60 * 60)
            val sign = if (offsetHours >= 0) "+" else ""
            return "Today, ${sign}${offsetHours}HRS"
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, IosAnalogWidgetProvider::class.java)
            )
            for (id in ids) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }
    }
}

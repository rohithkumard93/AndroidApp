package com.cupertino.clockwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.util.TimeZone

/**
 * AppWidgetProvider for the Squircle Perimeter-Tick Clock Widget (as in the reference image).
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

            val prefs = context.getSharedPreferences("cupertino_prefs", Context.MODE_PRIVATE)
            val tzId = prefs.getString("selected_tz", TimeZone.getDefault().id)
            val timeZone = if (tzId != null) TimeZone.getTimeZone(tzId) else TimeZone.getDefault()
            val isDark = prefs.getBoolean("is_dark_theme", false)

            // Render exact Squircle Perimeter-Tick Clock (Light White Card with Black Ticks & Bold Time)
            val clockBitmap = ClockRenderer.renderSquircleClock(
                size = 512,
                timeZone = timeZone,
                is24Hour = false,
                isDarkTheme = isDark
            )
            views.setImageViewBitmap(R.id.iv_clock_face, clockBitmap)

            // Open Companion App on Tap
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

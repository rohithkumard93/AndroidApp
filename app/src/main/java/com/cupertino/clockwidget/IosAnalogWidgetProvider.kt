package com.cupertino.clockwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import java.util.TimeZone

/**
 * AppWidgetProvider for the Squircle Clock Widget with Dynamic Seconds Counter.
 */
class IosAnalogWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        startSecondsService(context)
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        startSecondsService(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        val serviceIntent = Intent(context, WidgetSecondsService::class.java)
        context.stopService(serviceIntent)
    }

    private fun startSecondsService(context: Context) {
        try {
            val serviceIntent = Intent(context, WidgetSecondsService::class.java)
            context.startService(serviceIntent)
        } catch (_: Exception) {}
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

            // Render Squircle Clock with live seconds progress
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

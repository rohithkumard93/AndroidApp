package com.cupertino.clockwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * AppWidgetProvider for the 4x2 iOS Lockscreen Digital Clock Widget.
 */
class IosDigitalWidgetProvider : AppWidgetProvider() {

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
            val views = RemoteViews(context.packageName, R.layout.widget_digital_clock)

            // Current Date Pill (e.g., "WED, NOV 7")
            val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.US)
            views.setTextViewText(R.id.tv_digital_date, dateFormat.format(Date()).uppercase())

            // City Tag
            val city = context.getSharedPreferences("cupertino_prefs", Context.MODE_PRIVATE)
                .getString("selected_city", "Cupertino") ?: "Cupertino"
            views.setTextViewText(R.id.tv_digital_city, "$city • Local Time")

            // Mini Accent Analog Dial on Right
            val miniDial = ClockRenderer.renderDial(
                size = 180,
                timeZone = TimeZone.getDefault(),
                isDarkTheme = true,
                showSecondHand = false
            )
            views.setImageViewBitmap(R.id.iv_mini_dial, miniDial)

            // Launch Activity on Click
            val clickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.digital_widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, IosDigitalWidgetProvider::class.java)
            )
            for (id in ids) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }
    }
}

package com.cupertino.clockwidget

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.IBinder
import android.os.Looper

/**
 * Lightweight service that keeps the Squircle Seconds Counter ticking every second
 * when the screen is active, and automatically pauses when the screen turns off
 * to preserve battery life.
 */
class WidgetSecondsService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var isScreenOn = true

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (isScreenOn) {
                IosAnalogWidgetProvider.updateAllWidgets(this@WidgetSecondsService)
            }
            handler.postDelayed(this, 1000)
        }
    }

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    isScreenOn = false
                    handler.removeCallbacks(tickRunnable)
                }
                Intent.ACTION_SCREEN_ON -> {
                    isScreenOn = true
                    handler.post(tickRunnable)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenStateReceiver, filter)
        handler.post(tickRunnable)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(tickRunnable)
        try {
            unregisterReceiver(screenStateReceiver)
        } catch (_: Exception) {}
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

package com.cupertino.clockwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cupertino.clockwidget.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper())
    private var selectedTimeZone: TimeZone = TimeZone.getDefault()
    private var selectedCityName: String = "Cupertino"
    private var isDarkMode: Boolean = false

    private val cities = listOf(
        "Local Device Time" to TimeZone.getDefault().id,
        "Cupertino" to "America/Los_Angeles",
        "New York" to "America/New_York",
        "London" to "Europe/London",
        "Paris" to "Europe/Paris",
        "Tokyo" to "Asia/Tokyo",
        "Dubai" to "Asia/Dubai",
        "Sydney" to "Australia/Sydney"
    )

    private val tickerRunnable = object : Runnable {
        override fun run() {
            updatePreviews()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("cupertino_prefs", Context.MODE_PRIVATE)
        isDarkMode = prefs.getBoolean("is_dark_theme", false)

        setupCitySpinner()
        setupPinWidgetButtons()
    }

    override fun onResume() {
        super.onResume()
        handler.post(tickerRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(tickerRunnable)
    }

    private fun setupCitySpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            cities.map { it.first }
        )
        binding.spinnerCities.adapter = adapter

        binding.spinnerCities.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val (cityName, tzId) = cities[position]
                selectedCityName = cityName
                selectedTimeZone = TimeZone.getTimeZone(tzId)

                getSharedPreferences("cupertino_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("selected_city", cityName)
                    .putString("selected_tz", tzId)
                    .apply()

                updatePreviews()
                IosAnalogWidgetProvider.updateAllWidgets(this@MainActivity)
                IosDigitalWidgetProvider.updateAllWidgets(this@MainActivity)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updatePreviews() {
        // 1. Exact Squircle Perimeter-Tick Clock (As in uploaded image)
        val squircleBitmap = ClockRenderer.renderSquircleClock(
            size = 480,
            timeZone = selectedTimeZone,
            is24Hour = false,
            isDarkTheme = isDarkMode
        )
        binding.previewAnalogClock.setImageBitmap(squircleBitmap)

        // 2. Mini Accent Dial
        val miniBitmap = ClockRenderer.renderDial(
            size = 140,
            timeZone = selectedTimeZone,
            isDarkTheme = true,
            showSecondHand = false
        )
        binding.previewMiniDial.setImageBitmap(miniBitmap)

        // 3. Date Text
        val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.US).apply {
            timeZone = selectedTimeZone
        }
        binding.previewDigitalDate.text = dateFormat.format(Date()).uppercase()
    }

    private fun setupPinWidgetButtons() {
        binding.btnAddAnalog.text = "Pin Squircle Clock to Home"
        binding.btnAddAnalog.setOnClickListener {
            pinWidget(IosAnalogWidgetProvider::class.java)
        }

        binding.btnAddDigital.setOnClickListener {
            pinWidget(IosDigitalWidgetProvider::class.java)
        }
    }

    private fun pinWidget(providerClass: Class<*>) {
        val appWidgetManager = getSystemService(AppWidgetManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                val componentName = ComponentName(this, providerClass)
                val successCallback = PendingIntent.getBroadcast(
                    this,
                    0,
                    Intent(this, providerClass),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                appWidgetManager.requestPinAppWidget(componentName, null, successCallback)
                Toast.makeText(this, "Adding widget to home screen...", Toast.LENGTH_SHORT).show()
                return
            }
        }
        Toast.makeText(this, "Long-press your home screen to place widget", Toast.LENGTH_LONG).show()
    }
}

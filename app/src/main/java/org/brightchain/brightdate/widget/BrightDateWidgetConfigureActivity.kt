package org.brightchain.brightdate.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import androidx.core.content.ContextCompat

class BrightDateWidgetConfigureActivity : Activity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var currentColor = Color.parseColor("#F5C518")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        setContentView(R.layout.brightdate_widget_configure)

        intent.extras?.let { extras ->
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val colorPreview = findViewById<View>(R.id.colorPreview)
        val seekRed = findViewById<SeekBar>(R.id.seekRed)
        val seekGreen = findViewById<SeekBar>(R.id.seekGreen)
        val seekBlue = findViewById<SeekBar>(R.id.seekBlue)

        // Initialize seekbars with current color
        seekRed.progress = Color.red(currentColor)
        seekGreen.progress = Color.green(currentColor)
        seekBlue.progress = Color.blue(currentColor)

        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentColor = Color.rgb(seekRed.progress, seekGreen.progress, seekBlue.progress)
                colorPreview.setBackgroundColor(currentColor)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }

        seekRed.setOnSeekBarChangeListener(listener)
        seekGreen.setOnSeekBarChangeListener(listener)
        seekBlue.setOnSeekBarChangeListener(listener)

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            saveColorPref(this, appWidgetId, currentColor)

            val appWidgetManager = AppWidgetManager.getInstance(this)
            BrightDateWidgetProvider.renderWidget(this, appWidgetManager, appWidgetId)
            BrightDateWidgetProvider.scheduleNextTick(this)

            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }

    companion object {
        private const val PREFS_NAME = "org.brightchain.brightdate.widget.BrightDateWidgetProvider"
        private const val PREF_PREFIX_KEY = "appwidget_"

        internal fun saveColorPref(context: Context, appWidgetId: Int, color: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.putInt(PREF_PREFIX_KEY + appWidgetId, color)
            prefs.apply()
        }

        internal fun loadColorPref(context: Context, appWidgetId: Int): Int {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            return prefs.getInt(PREF_PREFIX_KEY + appWidgetId, ContextCompat.getColor(context, R.color.widget_value))
        }

        internal fun deleteColorPref(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.remove(PREF_PREFIX_KEY + appWidgetId)
            prefs.apply()
        }
    }
}

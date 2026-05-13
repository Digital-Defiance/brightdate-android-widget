package org.brightchain.brightdate.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews

/**
 * Home-screen widget that renders the current BrightDate value.
 *
 * Android's built-in [android.appwidget.AppWidgetProviderInfo.updatePeriodMillis]
 * enforces a 30-minute minimum, which is far too slow for a live clock.
 * We schedule our own repeating [AlarmManager] tick (every [UPDATE_INTERVAL_MS])
 * while at least one widget instance exists.
 */
class BrightDateWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            renderWidget(context, appWidgetManager, id)
        }
        scheduleNextTick(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            BrightDateWidgetConfigureActivity.deleteColorPref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        scheduleNextTick(context)
    }

    override fun onDisabled(context: Context) {
        cancelTick(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TICK) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, javaClass))
            for (id in ids) {
                renderWidget(context, mgr, id)
            }
            scheduleNextTick(context)
        }
    }

    companion object {
        const val ACTION_TICK = "org.brightchain.brightdate.widget.ACTION_TICK"

        /**
         * Update cadence. BrightDate's 5th decimal place ticks every ~0.864 s,
         * but waking the CPU that often is hostile to battery. 30 s is a
         * reasonable compromise — the last 1–2 digits will visibly jump,
         * which is honest behavior for a low-power widget.
         */
        private const val UPDATE_INTERVAL_MS = 30_000L

        fun renderWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val color = BrightDateWidgetConfigureActivity.loadColorPref(context, appWidgetId)
            val views = RemoteViews(context.packageName, R.layout.brightdate_widget)
            views.setTextViewText(R.id.brightdate_value, BrightDate.format(BrightDate.now()))
            views.setTextColor(R.id.brightdate_value, color)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun scheduleNextTick(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pi = tickPendingIntent(context)
            // Inexact repeating: no special permission needed, batched by system.
            alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + UPDATE_INTERVAL_MS,
                UPDATE_INTERVAL_MS,
                pi
            )
        }

        fun cancelTick(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(tickPendingIntent(context))
        }

        private fun tickPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, BrightDateWidgetProvider::class.java).apply {
                action = ACTION_TICK
            }
            return PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}

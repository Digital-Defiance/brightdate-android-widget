package org.brightchain.brightdate.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/**
 * Re-arms the widget tick alarm after boot, app upgrade, or time/zone changes.
 * AlarmManager schedules do not survive these events.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val mgr = AppWidgetManager.getInstance(context)
        val ids = mgr.getAppWidgetIds(
            ComponentName(context, BrightDateWidgetProvider::class.java)
        )
        if (ids.isNotEmpty()) {
            BrightDateWidgetProvider.scheduleNextTick(context)
        }
    }
}

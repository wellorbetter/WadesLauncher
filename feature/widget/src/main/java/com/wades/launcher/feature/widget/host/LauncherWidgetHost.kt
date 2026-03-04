package com.wades.launcher.feature.widget.host

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context

class LauncherWidgetHost(
    context: Context,
    hostId: Int,
) : AppWidgetHost(context, hostId) {

    override fun onCreateView(
        context: Context,
        appWidgetId: Int,
        appWidget: AppWidgetProviderInfo?,
    ): AppWidgetHostView {
        return LauncherWidgetHostView(context)
    }

    companion object {
        const val HOST_ID = 1024
    }
}

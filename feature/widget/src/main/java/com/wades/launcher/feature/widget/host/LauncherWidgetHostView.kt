package com.wades.launcher.feature.widget.host

import android.appwidget.AppWidgetHostView
import android.content.Context
import android.view.MotionEvent
import android.view.ViewGroup

class LauncherWidgetHostView(context: Context) : AppWidgetHostView(context) {

    private var initialX = 0f
    private var initialY = 0f

    init {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = ev.x
                initialY = ev.y
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = Math.abs(ev.x - initialX)
                val dy = Math.abs(ev.y - initialY)
                if (dx > dy) {
                    parent?.requestDisallowInterceptTouchEvent(false)
                    return false
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }
}

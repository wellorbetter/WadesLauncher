package com.wades.launcher.core.domain.model

data class WidgetInfo(
    val id: Int,
    val appWidgetId: Int,
    val packageName: String,
    val label: String,
    val spanX: Int = 4,
    val spanY: Int = 2,
    val sortOrder: Int = 0,
)

package com.wades.launcher.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val sortOrder: Int,
    val appPackageNames: String, // Comma-separated package names
    val type: String,
    val isExpanded: Boolean = true,
)

@Entity(tableName = "widgets")
data class WidgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val appWidgetId: Int,
    val packageName: String,
    val label: String,
    val spanX: Int,
    val spanY: Int,
    val sortOrder: Int,
)

@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey val packageName: String,
    val usageCount: Int = 0,
    val lastUsedTimestamp: Long = 0L,
    val isHidden: Boolean = false,
)

@Entity(tableName = "builtin_widgets")
data class BuiltInWidgetEntity(
    @PrimaryKey val id: String,
    val type: String,
    val sortOrder: Int,
    val isVisible: Boolean = true,
    val config: String = "{}",
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val text: String,
    val isCompleted: Boolean = false,
    val sortOrder: Int,
    val createdAt: Long,
    val completedAt: Long? = null,
)

package com.wades.launcher.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        GroupEntity::class,
        WidgetEntity::class,
        AppUsageEntity::class,
        BuiltInWidgetEntity::class,
        TaskEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class LauncherDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDao
    abstract fun widgetDao(): WidgetDao
    abstract fun appUsageDao(): AppUsageDao
    abstract fun builtInWidgetDao(): BuiltInWidgetDao
    abstract fun taskDao(): TaskDao
}

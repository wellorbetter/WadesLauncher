package com.wades.launcher.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query("SELECT * FROM `groups` ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<GroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: GroupEntity)

    @Update
    suspend fun update(group: GroupEntity)

    @Query("DELETE FROM `groups` WHERE id = :groupId")
    suspend fun deleteById(groupId: String)

    @Query("SELECT * FROM `groups` WHERE id = :groupId")
    suspend fun getById(groupId: String): GroupEntity?

    @Query("UPDATE `groups` SET sortOrder = :sortOrder WHERE id = :groupId")
    suspend fun updateSortOrder(groupId: String, sortOrder: Int)

    @Query("UPDATE `groups` SET name = :name WHERE id = :groupId")
    suspend fun updateName(groupId: String, name: String)

    @Transaction
    suspend fun reorderAll(groupIds: List<String>) {
        groupIds.forEachIndexed { index, id ->
            updateSortOrder(id, index)
        }
    }
}

@Dao
interface WidgetDao {
    @Query("SELECT * FROM widgets ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<WidgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(widget: WidgetEntity)

    @Query("DELETE FROM widgets WHERE id = :widgetId")
    suspend fun deleteById(widgetId: Int)

    @Update
    suspend fun update(widget: WidgetEntity)
}

@Dao
interface AppUsageDao {
    @Query("SELECT * FROM app_usage")
    fun observeAll(): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage WHERE packageName = :packageName")
    suspend fun getByPackageName(packageName: String): AppUsageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AppUsageEntity)

    @Query("""
        INSERT OR REPLACE INTO app_usage (packageName, usageCount, lastUsedTimestamp, isHidden)
        VALUES (
            :packageName,
            COALESCE((SELECT usageCount FROM app_usage WHERE packageName = :packageName), 0) + 1,
            :timestamp,
            COALESCE((SELECT isHidden FROM app_usage WHERE packageName = :packageName), 0)
        )
    """)
    suspend fun upsertUsage(packageName: String, timestamp: Long)

    @Query("""
        INSERT OR REPLACE INTO app_usage (packageName, usageCount, lastUsedTimestamp, isHidden)
        VALUES (
            :packageName,
            COALESCE((SELECT usageCount FROM app_usage WHERE packageName = :packageName), 0),
            COALESCE((SELECT lastUsedTimestamp FROM app_usage WHERE packageName = :packageName), 0),
            :hidden
        )
    """)
    suspend fun upsertHidden(packageName: String, hidden: Boolean)
}

@Dao
interface BuiltInWidgetDao {
    @Query("SELECT * FROM builtin_widgets ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<BuiltInWidgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BuiltInWidgetEntity)

    @Update
    suspend fun update(entity: BuiltInWidgetEntity)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, sortOrder ASC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteById(taskId: String)

    @Update
    suspend fun update(task: TaskEntity)

    @Query("UPDATE tasks SET isCompleted = :completed, completedAt = :at WHERE id = :taskId")
    suspend fun setCompleted(taskId: String, completed: Boolean, at: Long?)
}

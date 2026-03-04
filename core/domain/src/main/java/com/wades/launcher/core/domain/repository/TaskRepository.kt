package com.wades.launcher.core.domain.repository

import com.wades.launcher.core.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeAll(): Flow<List<Task>>
    suspend fun add(task: Task)
    suspend fun update(task: Task)
    suspend fun delete(taskId: String)
    suspend fun setCompleted(taskId: String, completed: Boolean)
}

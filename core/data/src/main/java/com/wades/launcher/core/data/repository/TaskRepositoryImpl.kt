package com.wades.launcher.core.data.repository

import com.wades.launcher.core.data.local.TaskDao
import com.wades.launcher.core.data.local.TaskEntity
import com.wades.launcher.core.domain.model.Task
import com.wades.launcher.core.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
) : TaskRepository {

    override fun observeAll(): Flow<List<Task>> =
        taskDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun add(task: Task) {
        taskDao.insert(task.toEntity())
    }

    override suspend fun update(task: Task) {
        taskDao.update(task.toEntity())
    }

    override suspend fun delete(taskId: String) {
        taskDao.deleteById(taskId)
    }

    override suspend fun setCompleted(taskId: String, completed: Boolean) {
        val at = if (completed) System.currentTimeMillis() else null
        taskDao.setCompleted(taskId, completed, at)
    }

    private fun TaskEntity.toDomain() = Task(
        id = id, text = text, isCompleted = isCompleted,
        sortOrder = sortOrder, createdAt = createdAt, completedAt = completedAt,
    )

    private fun Task.toEntity() = TaskEntity(
        id = id, text = text, isCompleted = isCompleted,
        sortOrder = sortOrder, createdAt = createdAt, completedAt = completedAt,
    )
}

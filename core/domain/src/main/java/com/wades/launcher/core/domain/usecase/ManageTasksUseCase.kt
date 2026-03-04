package com.wades.launcher.core.domain.usecase

import com.wades.launcher.core.domain.model.Task
import com.wades.launcher.core.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ManageTasksUseCase(
    private val taskRepository: TaskRepository,
) {
    fun observeAll(): Flow<List<Task>> = taskRepository.observeAll()

    suspend fun add(text: String) {
        val now = System.currentTimeMillis()
        val task = Task(
            id = UUID.randomUUID().toString(),
            text = text,
            sortOrder = now.toInt(),
            createdAt = now,
        )
        taskRepository.add(task)
    }

    suspend fun toggleComplete(taskId: String, currentlyCompleted: Boolean) {
        taskRepository.setCompleted(taskId, !currentlyCompleted)
    }

    suspend fun delete(taskId: String) {
        taskRepository.delete(taskId)
    }

    suspend fun update(task: Task) {
        taskRepository.update(task)
    }
}

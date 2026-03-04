package com.wades.launcher.core.domain.model

data class Task(
    val id: String,
    val text: String,
    val isCompleted: Boolean = false,
    val sortOrder: Int,
    val createdAt: Long,
    val completedAt: Long? = null,
)

package com.wades.launcher.core.domain.model

data class AppShortcut(
    val id: String,
    val packageName: String,
    val label: String,
    val intentUri: String,
)

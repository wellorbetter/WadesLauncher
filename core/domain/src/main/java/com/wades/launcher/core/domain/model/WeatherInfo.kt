package com.wades.launcher.core.domain.model

data class WeatherInfo(
    val temperature: Int,
    val condition: String,
    val humidity: Int,
    val location: String,
    val updatedAt: Long,
)

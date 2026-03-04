package com.wades.launcher.core.domain.repository

import com.wades.launcher.core.domain.model.WeatherInfo

interface WeatherRepository {
    suspend fun getWeather(city: String): WeatherInfo
    fun getCachedWeather(): WeatherInfo?
}

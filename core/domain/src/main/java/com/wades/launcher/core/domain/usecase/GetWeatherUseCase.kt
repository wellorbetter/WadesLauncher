package com.wades.launcher.core.domain.usecase

import com.wades.launcher.core.domain.model.WeatherInfo
import com.wades.launcher.core.domain.repository.WeatherRepository

class GetWeatherUseCase(
    private val weatherRepository: WeatherRepository,
) {
    suspend fun fetch(city: String): WeatherInfo =
        weatherRepository.getWeather(city)

    fun getCached(): WeatherInfo? =
        weatherRepository.getCachedWeather()
}

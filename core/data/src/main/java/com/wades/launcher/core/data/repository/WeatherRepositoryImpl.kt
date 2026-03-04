package com.wades.launcher.core.data.repository

import com.wades.launcher.core.data.source.WeatherDataSource
import com.wades.launcher.core.domain.model.WeatherInfo
import com.wades.launcher.core.domain.repository.WeatherRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val dataSource: WeatherDataSource,
) : WeatherRepository {

    override suspend fun getWeather(city: String): WeatherInfo =
        dataSource.fetch(city)

    override fun getCachedWeather(): WeatherInfo? =
        dataSource.getCached()
}

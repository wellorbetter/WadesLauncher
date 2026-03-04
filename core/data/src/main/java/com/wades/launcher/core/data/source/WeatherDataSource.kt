package com.wades.launcher.core.data.source

import android.content.Context
import com.wades.launcher.core.domain.model.WeatherInfo
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs by lazy {
        context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)
    }

    companion object {
        private const val CACHE_TTL_MS = 30 * 60 * 1000L // 30 min
        private const val KEY_TEMP = "temp"
        private const val KEY_CONDITION = "condition"
        private const val KEY_HUMIDITY = "humidity"
        private const val KEY_LOCATION = "location"
        private const val KEY_UPDATED = "updated_at"
    }

    fun getCached(): WeatherInfo? {
        val updatedAt = prefs.getLong(KEY_UPDATED, 0L)
        if (updatedAt == 0L) return null
        if (System.currentTimeMillis() - updatedAt > CACHE_TTL_MS) return null
        return WeatherInfo(
            temperature = prefs.getInt(KEY_TEMP, 0),
            condition = prefs.getString(KEY_CONDITION, "") ?: "",
            humidity = prefs.getInt(KEY_HUMIDITY, 0),
            location = prefs.getString(KEY_LOCATION, "") ?: "",
            updatedAt = updatedAt,
        )
    }

    suspend fun fetch(city: String): WeatherInfo {
        val url = URL("https://wttr.in/$city?format=j1")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        conn.setRequestProperty("User-Agent", "WadesLauncher/1.0")
        try {
            val body = conn.inputStream.bufferedReader().readText()
            val json = JSONObject(body)
            val current = json.getJSONArray("current_condition").getJSONObject(0)
            val info = WeatherInfo(
                temperature = current.optInt("temp_C", 0),
                condition = parseCondition(current.optString("weatherCode", "113")),
                humidity = current.optInt("humidity", 0),
                location = city,
                updatedAt = System.currentTimeMillis(),
            )
            saveCache(info)
            return info
        } finally {
            conn.disconnect()
        }
    }

    private fun saveCache(info: WeatherInfo) {
        prefs.edit()
            .putInt(KEY_TEMP, info.temperature)
            .putString(KEY_CONDITION, info.condition)
            .putInt(KEY_HUMIDITY, info.humidity)
            .putString(KEY_LOCATION, info.location)
            .putLong(KEY_UPDATED, info.updatedAt)
            .apply()
    }

    private fun parseCondition(code: String): String = when (code) {
        "113" -> "晴"
        "116" -> "多云"
        "119", "122" -> "阴"
        "143", "248", "260" -> "雾"
        "176", "263", "266", "293", "296", "299", "302", "305", "308" -> "雨"
        "179", "182", "185", "227", "230", "320", "323", "326", "329", "332",
        "335", "338", "350", "368", "371", "374", "377", "392", "395" -> "雪"
        "200", "386", "389" -> "雷"
        else -> "未知"
    }
}

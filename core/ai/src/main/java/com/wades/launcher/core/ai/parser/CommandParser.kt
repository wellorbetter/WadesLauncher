package com.wades.launcher.core.ai.parser

import com.wades.launcher.core.ai.model.AiCommand
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

class CommandParser {

    private val json = Json { ignoreUnknownKeys = true }

    fun parse(text: String): AiCommand? {
        // Look for JSON command block in the response
        val jsonMatch = Regex("""\{[^{}]*"action"\s*:\s*"[^"]+?"[^{}]*}""").find(text)
            ?: return null

        return try {
            val obj = json.decodeFromString<JsonObject>(jsonMatch.value)
            val action = obj["action"]?.jsonPrimitive?.content ?: return null
            when (action) {
                "launch_app" -> {
                    val pkg = obj["package"]?.jsonPrimitive?.content ?: return null
                    AiCommand.LaunchApp(pkg)
                }
                "search_app" -> {
                    val query = obj["query"]?.jsonPrimitive?.content ?: return null
                    AiCommand.SearchApp(query)
                }
                "open_settings" -> AiCommand.OpenSettings
                "open_url" -> {
                    val url = obj["url"]?.jsonPrimitive?.content ?: return null
                    AiCommand.OpenUrl(url)
                }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }
}

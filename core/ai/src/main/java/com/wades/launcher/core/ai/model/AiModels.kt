package com.wades.launcher.core.ai.model

import kotlinx.serialization.Serializable

@Serializable
data class AiMessage(
    val role: Role,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
) {
    enum class Role { USER, ASSISTANT, SYSTEM }
}

@Serializable
data class AiConversation(
    val id: String,
    val title: String = "",
    val messages: List<AiMessage> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
)

sealed class AiCommand {
    data class LaunchApp(val packageName: String) : AiCommand()
    data class SearchApp(val query: String) : AiCommand()
    data object OpenSettings : AiCommand()
    data class SetAlarm(val hour: Int, val minute: Int, val label: String = "") : AiCommand()
    data class OpenUrl(val url: String) : AiCommand()
    data class TextReply(val text: String) : AiCommand()
}

enum class AiProvider {
    OPENAI,
    CLAUDE,
    DOUBAO,
    CUSTOM,
}

data class AiConfig(
    val provider: AiProvider = AiProvider.OPENAI,
    val apiKey: String = "",
    val baseUrl: String = "",
    val modelName: String = "",
)

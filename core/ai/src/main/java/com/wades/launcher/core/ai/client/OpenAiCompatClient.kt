package com.wades.launcher.core.ai.client

import com.wades.launcher.core.ai.model.AiConfig
import com.wades.launcher.core.ai.model.AiMessage
import com.wades.launcher.core.ai.model.AiProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class OpenAiCompatClient(
    private val config: AiConfig,
) : AiClient {

    private val json = Json { ignoreUnknownKeys = true }

    private val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    private val baseUrl: String
        get() = when (config.provider) {
            AiProvider.OPENAI -> config.baseUrl.ifEmpty { "https://api.openai.com/v1" }
            AiProvider.CLAUDE -> config.baseUrl.ifEmpty { "https://api.anthropic.com/v1" }
            AiProvider.DOUBAO -> config.baseUrl.ifEmpty { "https://ark.cn-beijing.volces.com/api/v3" }
            AiProvider.CUSTOM -> config.baseUrl
        }

    private val model: String
        get() = config.modelName.ifEmpty {
            when (config.provider) {
                AiProvider.OPENAI -> "gpt-4o-mini"
                AiProvider.CLAUDE -> "claude-sonnet-4-20250514"
                AiProvider.DOUBAO -> "doubao-pro-32k"
                AiProvider.CUSTOM -> "default"
            }
        }

    override suspend fun chat(messages: List<AiMessage>): AiMessage {
        val response = httpClient.post("$baseUrl/chat/completions") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer ${config.apiKey}")
            setBody(
                ChatRequest(
                    model = model,
                    messages = messages.map {
                        ChatMessage(role = it.role.name.lowercase(), content = it.content)
                    },
                ),
            )
        }
        val body = response.body<ChatResponse>()
        val content = body.choices.firstOrNull()?.message?.content ?: ""
        return AiMessage(role = AiMessage.Role.ASSISTANT, content = content)
    }

    override fun chatStream(messages: List<AiMessage>): Flow<String> = flow {
        // Simplified: non-streaming fallback
        val reply = chat(messages)
        emit(reply.content)
    }

    override fun isConfigured(): Boolean = config.apiKey.isNotBlank()
}

@Serializable
private data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
)

@Serializable
private data class ChatMessage(
    val role: String,
    val content: String,
)

@Serializable
private data class ChatResponse(
    val choices: List<Choice> = emptyList(),
)

@Serializable
private data class Choice(
    val message: ChatMessage? = null,
)

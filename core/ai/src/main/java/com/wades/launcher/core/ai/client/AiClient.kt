package com.wades.launcher.core.ai.client

import com.wades.launcher.core.ai.model.AiMessage
import kotlinx.coroutines.flow.Flow

interface AiClient {
    suspend fun chat(messages: List<AiMessage>): AiMessage
    fun chatStream(messages: List<AiMessage>): Flow<String>
    fun isConfigured(): Boolean
}

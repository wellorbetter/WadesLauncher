package com.wades.launcher.feature.assistant

import com.wades.launcher.core.ai.client.AiClient
import com.wades.launcher.core.ai.client.OpenAiCompatClient
import com.wades.launcher.core.ai.model.AiConfig
import com.wades.launcher.core.ai.model.AiMessage
import com.wades.launcher.core.ai.parser.CommandParser
import com.wades.launcher.core.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val commandParser: CommandParser,
) : MviViewModel<AssistantIntent, AssistantState, AssistantSideEffect>(AssistantState()) {

    private var aiClient: AiClient? = null

    fun configure(config: AiConfig) {
        aiClient = OpenAiCompatClient(config)
        updateState { copy(providerConfigured = aiClient?.isConfigured() == true) }
    }

    override suspend fun handleIntent(intent: AssistantIntent) {
        when (intent) {
            is AssistantIntent.SendMessage -> sendMessage(intent.text)
            is AssistantIntent.ConfirmCommand -> confirmCommand()
            is AssistantIntent.DismissCommand -> updateState { copy(pendingCommand = null) }
            is AssistantIntent.ClearConversation -> updateState { copy(messages = emptyList(), pendingCommand = null) }
        }
    }

    private suspend fun sendMessage(text: String) {
        val client = aiClient
        if (client == null || !client.isConfigured()) {
            emitSideEffect(AssistantSideEffect.ShowError("AI provider not configured"))
            return
        }

        val userMessage = AiMessage(role = AiMessage.Role.USER, content = text)
        updateState { copy(messages = messages + userMessage, isLoading = true) }

        try {
            val reply = client.chat(state.value.messages)
            updateState { copy(messages = messages + reply, isLoading = false) }

            // Check if reply contains a command
            val command = commandParser.parse(reply.content)
            if (command != null) {
                updateState { copy(pendingCommand = command) }
            }
        } catch (e: Exception) {
            updateState { copy(isLoading = false) }
            emitSideEffect(AssistantSideEffect.ShowError(e.message ?: "Unknown error"))
        }
    }

    private fun confirmCommand() {
        val command = state.value.pendingCommand ?: return
        updateState { copy(pendingCommand = null) }
        emitSideEffect(AssistantSideEffect.ExecuteCommand(command))
    }
}

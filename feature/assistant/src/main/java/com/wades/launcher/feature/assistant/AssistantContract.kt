package com.wades.launcher.feature.assistant

import com.wades.launcher.core.ai.model.AiCommand
import com.wades.launcher.core.ai.model.AiMessage
import com.wades.launcher.core.ui.mvi.MviIntent
import com.wades.launcher.core.ui.mvi.MviSideEffect
import com.wades.launcher.core.ui.mvi.MviState

sealed interface AssistantIntent : MviIntent {
    data class SendMessage(val text: String) : AssistantIntent
    data object ConfirmCommand : AssistantIntent
    data object DismissCommand : AssistantIntent
    data object ClearConversation : AssistantIntent
}

data class AssistantState(
    val messages: List<AiMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isListening: Boolean = false,
    val pendingCommand: AiCommand? = null,
    val providerConfigured: Boolean = false,
) : MviState

sealed interface AssistantSideEffect : MviSideEffect {
    data class ExecuteCommand(val command: AiCommand) : AssistantSideEffect
    data class ShowError(val message: String) : AssistantSideEffect
}

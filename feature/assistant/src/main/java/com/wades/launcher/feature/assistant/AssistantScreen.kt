package com.wades.launcher.feature.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wades.launcher.core.ai.model.AiCommand
import com.wades.launcher.core.ai.model.AiMessage
import com.wades.launcher.feature.assistant.R as AssistantR

@Composable
fun AssistantScreen(
    onExecuteCommand: (AiCommand) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: AssistantViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
    val imePadding = WindowInsets.ime.asPaddingValues()
    val bottomPadding = maxOf(
        navBarPadding.calculateBottomPadding(),
        imePadding.calculateBottomPadding(),
    )

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is AssistantSideEffect.ExecuteCommand -> onExecuteCommand(effect.command)
                is AssistantSideEffect.ShowError -> { /* TODO: show snackbar */ }
            }
        }
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = statusBarPadding.calculateTopPadding() + 8.dp,
                    bottom = bottomPadding + 8.dp,
                ),
        ) {
            // Title
            Text(
                text = stringResource(AssistantR.string.assistant_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            // Chat messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.messages, key = { it.timestamp }) { message ->
                    ChatBubble(message = message)
                }

                if (state.isLoading) {
                    item(key = "loading") {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White.copy(alpha = 0.6f),
                            )
                            Text(
                                text = stringResource(AssistantR.string.assistant_thinking),
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }
            }

            // Command confirmation card
            val pending = state.pendingCommand
            if (pending != null) {
                CommandConfirmCard(
                    command = pending,
                    onConfirm = { viewModel.dispatch(AssistantIntent.ConfirmCommand) },
                    onDismiss = { viewModel.dispatch(AssistantIntent.DismissCommand) },
                )
            }

            // Input bar
            ChatInputBar(
                enabled = !state.isLoading,
           onSend = { viewModel.dispatch(AssistantIntent.SendMessage(it)) },
            )
        }

        // Not configured overlay
        if (!state.providerConfigured) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(AssistantR.string.assistant_not_configured),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(message: AiMessage) {
    val isUser = message.role == AiMessage.Role.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isUser) 12.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 12.dp,
                    ),
                )
                .background(
                    if (isUser) Color.White.copy(alpha = 0.15f)
                    else Color.White.copy(alpha = 0.08f),
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = message.content,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun CommandConfirmCard(
    command: AiCommand,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f),
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(AssistantR.string.assistant_confirm_action),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.7f),
            )
            Text(
                text = commandDescription(command),
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.padding(top = 4.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        stringResource(AssistantR.string.assistant_cancel),
                        color = Color.White.copy(alpha = 0.5f),
                    )
                }
                TextButton(onClick = onConfirm) {
                    Text(
                        stringResource(AssistantR.string.assistant_confirm),
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }
            }
        }
    }
}

@Composable
private fun commandDescription(command: AiCommand): String = when (command) {
    is AiCommand.LaunchApp -> stringResource(AssistantR.string.assistant_cmd_launch, command.packageName)
    is AiCommand.SearchApp -> stringResource(AssistantR.string.assistant_cmd_search, command.query)
    is AiCommand.OpenSettings -> stringResource(AssistantR.string.assistant_cmd_settings)
    is AiCommand.OpenUrl -> stringResource(AssistantR.string.assistant_cmd_url, command.url)
    is AiCommand.SetAlarm -> stringResource(AssistantR.string.assistant_cmd_alarm, command.hour, command.minute)
    is AiCommand.TextReply -> command.text
}

@Composable
private fun ChatInputBar(
    enabled: Boolean,
    onSend: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp)),
            placeholder = {
                Text(
                    stringResource(AssistantR.string.assistant_input_hint),
                    color = Color.White.copy(alpha = 0.35f),
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.08f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                focusedTextColor = Color.White.copy(alpha = 0.85f),
                unfocusedTextColor = Color.White.copy(alpha = 0.85f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (text.isNotBlank() && enabled) {
                        onSend(text.trim())
                        text = ""
                    }
                },
            ),
            singleLine = true,
        )

        IconButton(
            onClick = {
                if (text.isNotBlank() && enabled) {
                    onSend(text.trim())
                    text = ""
                }
            },
            enabled = text.isNotBlank() && enabled,
            modifier = Modifier
                .padding(start = 8.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (text.isNotBlank() && enabled) Color.White.copy(alpha = 0.15f)
                    else Color.Transparent,
                ),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = if (text.isNotBlank() && enabled) Color.White.copy(alpha = 0.8f)
                else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

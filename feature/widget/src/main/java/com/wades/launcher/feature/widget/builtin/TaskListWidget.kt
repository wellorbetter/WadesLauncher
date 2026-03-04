package com.wades.launcher.feature.widget.builtin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wades.launcher.core.domain.model.Task
import com.wades.launcher.core.ui.component.GlassCard

private const val DEFAULT_VISIBLE_COUNT = 5

@Composable
fun TaskListWidget(
    tasks: List<Task>,
    onAdd: (String) -> Unit,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isInputVisible by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val incomplete = tasks.filter { !it.isCompleted }
    val completed = tasks.filter { it.isCompleted }
    val allOrdered = incomplete + completed
    val visibleTasks = if (isExpanded) allOrdered else allOrdered.take(DEFAULT_VISIBLE_COUNT)
    val hasMore = allOrdered.size > DEFAULT_VISIBLE_COUNT && !isExpanded

    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "待办",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 2.sp,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = { isInputVisible = true },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "添加待办",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            if (tasks.isEmpty()) {
                Text(
                    text = "点击 + 添加第一个待办",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                )
            } else {
                visibleTasks.forEach { task ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TaskItem(
                            task = task,
                            onToggle = { onToggle(task.id) },
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = { onDelete(task.id) },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "删除",
                                tint = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }

                if (hasMore) {
                    Text(
                        text = "展开更多 (${allOrdered.size - DEFAULT_VISIBLE_COUNT})",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier
                            .clickable { isExpanded = true }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            // Inline input
            AnimatedVisibility(
                visible = isInputVisible,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f),
                            ),
                            cursorBrush = SolidColor(Color.White.copy(alpha = 0.6f)),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (inputText.isNotBlank()) {
                                        onAdd(inputText.trim())
                                        inputText = ""
                                        isInputVisible = false
                                    }
                                },
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            decorationBox = { inner ->
                                if (inputText.isEmpty()) {
                                    Text(
                                        text = "输入待办内容...",
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.3f),
                                    )
                                }
                                inner()
                            },
                        )
                    }

                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

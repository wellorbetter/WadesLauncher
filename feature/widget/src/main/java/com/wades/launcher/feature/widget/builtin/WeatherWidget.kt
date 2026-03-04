package com.wades.launcher.feature.widget.builtin

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wades.launcher.core.domain.model.WeatherInfo
import com.wades.launcher.core.ui.component.GlassCard
import kotlinx.coroutines.launch

@Composable
fun WeatherWidget(
    weather: WeatherInfo?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    GlassCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    scope.launch {
                        rotation.animateTo(
                            rotation.value + 360f,
                            animationSpec = tween(600, easing = LinearEasing),
                        )
                    }
                    onRefresh()
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (weather != null) {
                // Weather emoji
                Text(
                    text = conditionEmoji(weather.condition),
                    fontSize = 28.sp,
                )
                Spacer(modifier = Modifier.width(12.dp))

                // Temperature + condition
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${weather.temperature}°",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Thin,
                        color = Color.White,
                    )
                    Text(
                        text = weather.condition,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f),
                    )
                }

                // Humidity + location
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "💧 ${weather.humidity}%",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f),
                    )
                    Text(
                        text = weather.location,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.4f),
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "刷新",
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(rotation.value),
                )
            } else {
                Text(
                    text = "🌤",
                    fontSize = 28.sp,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "点击加载天气",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "刷新",
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(rotation.value),
                )
            }
        }
    }
}

private fun conditionEmoji(condition: String): String = when (condition) {
    "晴" -> "☀️"
    "多云" -> "⛅"
    "阴" -> "☁️"
    "雾" -> "🌫️"
    "雨" -> "🌧️"
    "雪" -> "❄️"
    "雷" -> "⛈️"
    else -> "🌤"
}

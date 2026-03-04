package com.wades.launcher.feature.widget.builtin

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wades.launcher.core.ui.component.GlassCard
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BuiltInClockWidget(modifier: Modifier = Modifier) {
    var timeText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFmt = SimpleDateFormat("M月d日 EEEE", Locale.CHINESE)
        while (true) {
            val now = Date()
            timeText = timeFmt.format(now)
            dateText = dateFmt.format(now)
            delay(10_000L)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "clock_shimmer")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_angle",
    )

    GlassCard(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawWithContent {
                    drawContent()
                    val rad = Math.toRadians(angle.toDouble())
                    val cx = size.width / 2
                    val cy = size.height / 2
                    val r = size.width * 0.6f
                    val start = Offset(
                        cx + (r * cos(rad)).toFloat(),
                        cy + (r * sin(rad)).toFloat(),
                    )
                    val end = Offset(
                        cx - (r * cos(rad)).toFloat(),
                        cy - (r * sin(rad)).toFloat(),
                    )
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.0f),
                                Color.White.copy(alpha = 0.06f),
                                Color.White.copy(alpha = 0.0f),
                            ),
                            start = start,
                            end = end,
                        ),
                    )
                },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = timeText,
                    style = TextStyle(
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Thin,
                        color = Color.White,
                        letterSpacing = 4.sp,
                    ),
                )
                Text(
                    text = dateText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 2.sp,
                )
            }
        }
    }
}

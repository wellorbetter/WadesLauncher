package com.wades.launcher.feature.widget.builtin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wades.launcher.core.ui.component.GlassCard
import java.util.Calendar
import java.util.Locale

@Composable
fun BuiltInCalendarWidget(modifier: Modifier = Modifier) {
    val cal = remember { Calendar.getInstance() }
    val today = remember { cal.get(Calendar.DAY_OF_MONTH) }
    val year = remember { cal.get(Calendar.YEAR) }
    val month = remember { cal.get(Calendar.MONTH) } // 0-based

    // First day of month: which weekday (Mon=0 .. Sun=6)
    val firstDayCal = remember {
        Calendar.getInstance().apply {
            set(year, month, 1)
        }
    }
    // Calendar.MONDAY=2, SUNDAY=1; convert to Mon=0..Sun=6
    val firstDayOfWeek = remember {
        val dow = firstDayCal.get(Calendar.DAY_OF_WEEK)
        if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
    }
    val daysInMonth = remember { firstDayCal.getActualMaximum(Calendar.DAY_OF_MONTH) }

    val weekdays = remember { listOf("一", "二", "三", "四", "五", "六", "日") }
    val monthName = remember {
        val names = arrayOf("1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月")
        names[month]
    }

    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            // Header: month + year
            Text(
                text = "${year}年${monthName}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 2.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
            )

            // Weekday headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                weekdays.forEachIndexed { index, day ->
                    val isWeekend = index >= 5
                    Text(
                        text = day,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isWeekend) Color(0xFFE57373).copy(alpha = 0.6f)
                        else Color.White.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Date grid
            var dayCounter = 1
            val totalSlots = firstDayOfWeek + daysInMonth
            val rows = (totalSlots + 6) / 7

            for (row in 0 until rows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    for (col in 0..6) {
                        val slotIndex = row * 7 + col
                        if (slotIndex < firstDayOfWeek || dayCounter > daysInMonth) {
                            // Empty slot
                            Box(modifier = Modifier.weight(1f).size(28.dp))
                  } else {
                            val day = dayCounter
                            val isToday = day == today
                            val isWeekend = col >= 5
                            dayCounter++

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .size(28.dp)
                                    .then(
                                        if (isToday) Modifier
                                            .clip(CircleShape)
                                            .background(Color(0xFF4A90D9).copy(alpha = 0.7f))
                                        else Modifier
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = day.toString(),
                                    fontSize = 11.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isToday -> Color.White
                                        isWeekend -> Color(0xFFE57373).copy(alpha = 0.5f)
                                        else -> Color.White.copy(alpha = 0.7f)
                                    },
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.animation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.engine.animation.ClipData
import com.example.ui.theme.*

enum class TimeManipulationTab {
    INSERT_TIME,
    DELETE_RANGE,
    REMOVE_GAPS,
    EXTEND_DURATION
}

@Composable
fun TimeManipulationDialog(
    clip: ClipData,
    currentFrame: Int,
    rangeStart: Int,
    rangeEnd: Int,
    onDismiss: () -> Unit,
    onInsertTime: (atFrame: Int, count: Int) -> Unit,
    onDeleteRange: (start: Int, end: Int) -> Unit,
    onRemoveGaps: (gap: Int) -> Unit,
    onSetDuration: (Int) -> Unit
) {
    var selectedTab by remember { mutableStateOf(TimeManipulationTab.INSERT_TIME) }

    // Insert Time State
    var insertAtFrameText by remember { mutableStateOf(currentFrame.toString()) }
    var insertCountText by remember { mutableStateOf("12") }

    // Delete Range State
    var deleteStartText by remember { mutableStateOf(rangeStart.toString()) }
    var deleteEndText by remember { mutableStateOf(rangeEnd.toString()) }

    // Remove Gaps State
    var fixedGapText by remember { mutableStateOf("2") }

    // Duration State
    var durationText by remember { mutableStateOf(clip.durationFrames.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.width(340.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = EngineSurface),
            border = BorderStroke(1.dp, StudioPurpleBorder)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HourglassTop, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("العمليات الزمنية للتايم لاين", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted, modifier = Modifier.size(14.dp))
                    }
                }

                HorizontalDivider(color = StudioBorder)

                // Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(EngineBackground)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    listOf(
                        TimeManipulationTab.INSERT_TIME to "إدراج زمن",
                        TimeManipulationTab.DELETE_RANGE to "حذف نطاق",
                        TimeManipulationTab.REMOVE_GAPS to "إزالة الفراغات",
                        TimeManipulationTab.EXTEND_DURATION to "تعديل المدة"
                    ).forEach { (tab, label) ->
                        val isSelected = selectedTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) StudioPurple else Color.Transparent)
                                .clickable { selectedTab = tab }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else TextSecondary,
                                fontSize = 8.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Tab Content
                when (selectedTab) {
                    TimeManipulationTab.INSERT_TIME -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("إدراج فريمات فارغة وإزاحة كل الحركات اللاحقة تلقائياً:", color = TextSecondary, fontSize = 9.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = insertAtFrameText,
                                    onValueChange = { insertAtFrameText = it },
                                    label = { Text("عند الإطار", fontSize = 8.5.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = insertCountText,
                                    onValueChange = { insertCountText = it },
                                    label = { Text("عدد الفريمات (+)", fontSize = 8.5.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Quick Insert Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(6, 12, 24, 48).forEach { count ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(EngineCardBg)
                                            .border(0.5.dp, StudioBorder, RoundedCornerShape(3.dp))
                                            .clickable { insertCountText = count.toString() }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("+$count f", color = StudioGreen, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }

                    TimeManipulationTab.DELETE_RANGE -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("حذف الفريمات داخل النطاق وإزاحة كل الحركات اللاحقة لليسار:", color = TextSecondary, fontSize = 9.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = deleteStartText,
                                    onValueChange = { deleteStartText = it },
                                    label = { Text("بداية النطاق (F)", fontSize = 8.5.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = deleteEndText,
                                    onValueChange = { deleteEndText = it },
                                    label = { Text("نهاية النطاق (F)", fontSize = 8.5.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            val dStart = deleteStartText.toIntOrNull() ?: 0
                            val dEnd = deleteEndText.toIntOrNull() ?: 0
                            val span = (dEnd - dStart + 1).coerceAtLeast(0)
                            Text("سيتم إزالة $span فريم من المخطط الزمني بالكامل", color = StudioRed, fontSize = 8.5.sp)
                        }
                    }

                    TimeManipulationTab.REMOVE_GAPS -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("ضغط المسافات الفارغة بين الإطارات المفتاحية المتتالية:", color = TextSecondary, fontSize = 9.sp)
                            OutlinedTextField(
                                value = fixedGapText,
                                onValueChange = { fixedGapText = it },
                                label = { Text("المسافة القياسية بين المفاتيح (فريم)", fontSize = 8.5.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(1 to "متصلة (1f)", 2 to "طبيعية (2f)", 4 to "متباعدة (4f)").forEach { (gap, lbl) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(EngineCardBg)
                                            .border(0.5.dp, StudioBorder, RoundedCornerShape(3.dp))
                                            .clickable { fixedGapText = gap.toString() }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(lbl, color = TextSecondary, fontSize = 8.sp)
                                    }
                                }
                            }
                        }
                    }

                    TimeManipulationTab.EXTEND_DURATION -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("تحديد إجمالي عدد الفريمات في مقطع الحركة الحالي:", color = TextSecondary, fontSize = 9.sp)
                            OutlinedTextField(
                                value = durationText,
                                onValueChange = { durationText = it },
                                label = { Text("المدة الإجمالية (فريم)", fontSize = 8.5.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(24 to "1 ثانية (24f)", 48 to "2 ثانية (48f)", 72 to "3 ثوانٍ (72f)", 120 to "5 ثوانٍ (120f)").forEach { (f, lbl) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(EngineCardBg)
                                            .border(0.5.dp, StudioBorder, RoundedCornerShape(3.dp))
                                            .clickable { durationText = f.toString() }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(lbl, color = TextSecondary, fontSize = 7.5.sp, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(0.5.dp, StudioBorder)
                    ) {
                        Text("إلغاء", color = TextSecondary, fontSize = 10.sp)
                    }

                    Button(
                        onClick = {
                            when (selectedTab) {
                                TimeManipulationTab.INSERT_TIME -> {
                                    val atF = insertAtFrameText.toIntOrNull() ?: currentFrame
                                    val count = insertCountText.toIntOrNull() ?: 12
                                    onInsertTime(atF, count)
                                }
                                TimeManipulationTab.DELETE_RANGE -> {
                                    val s = deleteStartText.toIntOrNull() ?: rangeStart
                                    val e = deleteEndText.toIntOrNull() ?: rangeEnd
                                    onDeleteRange(s, e)
                                }
                                TimeManipulationTab.REMOVE_GAPS -> {
                                    val gap = fixedGapText.toIntOrNull() ?: 2
                                    onRemoveGaps(gap)
                                }
                                TimeManipulationTab.EXTEND_DURATION -> {
                                    val d = durationText.toIntOrNull() ?: clip.durationFrames
                                    onSetDuration(d)
                                }
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == TimeManipulationTab.DELETE_RANGE) StudioRed else StudioPurple
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (selectedTab == TimeManipulationTab.DELETE_RANGE) "تنفيذ الحذف" else "تطبيق العملية",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

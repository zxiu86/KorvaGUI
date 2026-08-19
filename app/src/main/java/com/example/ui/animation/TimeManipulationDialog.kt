package com.example.ui.animation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.animation.ClipData
import com.example.ui.components.KorvaDangerButton
import com.example.ui.components.KorvaDialog
import com.example.ui.components.KorvaOutlinedButton
import com.example.ui.components.KorvaPrimaryButton
import com.example.ui.theme.EngineBackground
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.KorvaGreen
import com.example.ui.theme.KorvaPurple
import com.example.ui.theme.KorvaPurpleLight
import com.example.ui.theme.KorvaRed
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

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

    KorvaDialog(
        onDismissRequest = onDismiss,
        title = "العمليات الزمنية للتايم لاين",
        subtitle = "إدراج، حذف، تقليص، وتعديل الفترات الزمنية للمقطع",
        icon = Icons.Default.HourglassTop,
        iconTint = KorvaPurpleLight,
        maxWidth = 420.dp,
        buttons = {
            KorvaOutlinedButton(
                text = "إلغاء",
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )

            if (selectedTab == TimeManipulationTab.DELETE_RANGE) {
                KorvaDangerButton(
                    text = "تنفيذ الحذف",
                    onClick = {
                        val s = deleteStartText.toIntOrNull() ?: rangeStart
                        val e = deleteEndText.toIntOrNull() ?: rangeEnd
                        onDeleteRange(s, e)
                        onDismiss()
                    },
                    icon = Icons.Default.DeleteSweep,
                    modifier = Modifier.weight(1.2f)
                )
            } else {
                KorvaPrimaryButton(
                    text = "تطبيق العملية",
                    onClick = {
                        when (selectedTab) {
                            TimeManipulationTab.INSERT_TIME -> {
                                val atF = insertAtFrameText.toIntOrNull() ?: currentFrame
                                val count = insertCountText.toIntOrNull() ?: 12
                                onInsertTime(atF, count)
                            }
                            TimeManipulationTab.REMOVE_GAPS -> {
                                val gap = fixedGapText.toIntOrNull() ?: 2
                                onRemoveGaps(gap)
                            }
                            TimeManipulationTab.EXTEND_DURATION -> {
                                val d = durationText.toIntOrNull() ?: clip.durationFrames
                                onSetDuration(d)
                            }
                            else -> {}
                        }
                        onDismiss()
                    },
                    icon = Icons.Default.Check,
                    modifier = Modifier.weight(1.2f)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Tabs Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(EngineBackground)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                listOf(
                    TimeManipulationTab.INSERT_TIME to "إدراج زمن",
                    TimeManipulationTab.DELETE_RANGE to "حذف نطاق",
                    TimeManipulationTab.REMOVE_GAPS to "ضغط الفراغات",
                    TimeManipulationTab.EXTEND_DURATION to "المدة الكلية"
                ).forEach { (tab, label) ->
                    val isSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) KorvaPurple else Color.Transparent)
                            .clickable { selectedTab = tab }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = 9.5.sp,
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
                        Text("إدراج فريمات فارغة وإزاحة الحركات اللاحقة تلقائياً:", color = TextSecondary, fontSize = 10.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = insertAtFrameText,
                                onValueChange = { insertAtFrameText = it },
                                label = { Text("عند الإطار", fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = KorvaPurpleLight,
                                    unfocusedBorderColor = StudioBorder,
                                    focusedContainerColor = EngineCardBg,
                                    unfocusedContainerColor = EngineCardBg,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = insertCountText,
                                onValueChange = { insertCountText = it },
                                label = { Text("عدد الفريمات (+)", fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = KorvaGreen,
                                    unfocusedBorderColor = StudioBorder,
                                    focusedContainerColor = EngineCardBg,
                                    unfocusedContainerColor = EngineCardBg,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(8.dp),
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
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(EngineCardBg)
                                        .border(0.8.dp, StudioBorder, RoundedCornerShape(6.dp))
                                        .clickable { insertCountText = count.toString() }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+$count f", color = KorvaGreen, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                TimeManipulationTab.DELETE_RANGE -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("حذف الفريمات داخل النطاق وإزاحة كل الحركات اللاحقة:", color = TextSecondary, fontSize = 10.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = deleteStartText,
                                onValueChange = { deleteStartText = it },
                                label = { Text("بداية النطاق (F)", fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = KorvaRed,
                                    unfocusedBorderColor = StudioBorder,
                                    focusedContainerColor = EngineCardBg,
                                    unfocusedContainerColor = EngineCardBg,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = deleteEndText,
                                onValueChange = { deleteEndText = it },
                                label = { Text("نهاية النطاق (F)", fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = KorvaRed,
                                    unfocusedBorderColor = StudioBorder,
                                    focusedContainerColor = EngineCardBg,
                                    unfocusedContainerColor = EngineCardBg,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        val dStart = deleteStartText.toIntOrNull() ?: 0
                        val dEnd = deleteEndText.toIntOrNull() ?: 0
                        val span = (dEnd - dStart + 1).coerceAtLeast(0)
                        Text("سيتم حذف $span فريم من المخطط الزمني بالكامل", color = KorvaRed, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                TimeManipulationTab.REMOVE_GAPS -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("ضغط المسافات الفارغة بين الإطارات المفتاحية المتتالية:", color = TextSecondary, fontSize = 10.sp)
                        OutlinedTextField(
                            value = fixedGapText,
                            onValueChange = { fixedGapText = it },
                            label = { Text("المسافة القياسية بين المفاتيح (فريم)", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KorvaPurpleLight,
                                unfocusedBorderColor = StudioBorder,
                                focusedContainerColor = EngineCardBg,
                                unfocusedContainerColor = EngineCardBg,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
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
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(EngineCardBg)
                                        .border(0.8.dp, StudioBorder, RoundedCornerShape(6.dp))
                                        .clickable { fixedGapText = gap.toString() }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(lbl, color = TextSecondary, fontSize = 9.5.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                TimeManipulationTab.EXTEND_DURATION -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("تحديد إجمالي عدد الفريمات في مقطع الحركة الحالي:", color = TextSecondary, fontSize = 10.sp)
                        OutlinedTextField(
                            value = durationText,
                            onValueChange = { durationText = it },
                            label = { Text("المدة الإجمالية (فريم)", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KorvaPurpleLight,
                                unfocusedBorderColor = StudioBorder,
                                focusedContainerColor = EngineCardBg,
                                unfocusedContainerColor = EngineCardBg,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
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
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(EngineCardBg)
                                        .border(0.8.dp, StudioBorder, RoundedCornerShape(6.dp))
                                        .clickable { durationText = f.toString() }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(lbl, color = TextSecondary, fontSize = 8.5.sp, maxLines = 1, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

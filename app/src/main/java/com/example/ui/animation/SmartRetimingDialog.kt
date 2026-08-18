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

@Composable
fun SmartRetimingDialog(
    clip: ClipData,
    selectedKeyframeIds: Set<String>,
    onDismiss: () -> Unit,
    onScaleTiming: (Float) -> Unit,
    onSetDuration: (Int) -> Unit,
    onDistributeEvenly: () -> Unit,
    onReverseTiming: () -> Unit,
    onHoldKeyframes: () -> Unit
) {
    // Gather selected keyframes
    val selectedKeys = remember(clip, selectedKeyframeIds) {
        clip.tracks.flatMap { track ->
            track.keyframes.filter { selectedKeyframeIds.contains(it.id) }
        }
    }

    val minFrame = remember(selectedKeys) { selectedKeys.minOfOrNull { it.frame } ?: 0 }
    val maxFrame = remember(selectedKeys) { selectedKeys.maxOfOrNull { it.frame } ?: 0 }
    val originalDuration = remember(minFrame, maxFrame) { (maxFrame - minFrame).coerceAtLeast(1) }

    var targetDurationText by remember(originalDuration) { mutableStateOf(originalDuration.toString()) }
    var scalePercentText by remember { mutableStateOf("100") }

    val targetDuration = targetDurationText.toIntOrNull() ?: originalDuration
    val calculatedScale = if (originalDuration > 0) targetDuration.toFloat() / originalDuration.toFloat() else 1.0f
    val newEndFrame = minFrame + targetDuration

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
                        Icon(Icons.Default.Speed, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("التحكم الذكي بالتوقيت (Smart Retiming)", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted, modifier = Modifier.size(14.dp))
                    }
                }

                HorizontalDivider(color = StudioBorder)

                // Info Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(EngineBackground)
                        .border(0.5.dp, StudioBorder, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الإطارات المحددة: ${selectedKeys.size}", color = TextSecondary, fontSize = 9.5.sp)
                            Text("النطاق: F$minFrame ➔ F$maxFrame", color = StudioPurpleLight, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("المدة الحالية: $originalDuration فريم", color = TextMuted, fontSize = 9.sp)
                            Text("${String.format(java.util.Locale.US, "%.3f", if (clip.fps > 0) originalDuration.toFloat() / clip.fps else 0f)}s", color = StudioBlue, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                // Live Timing Preview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(StudioPurpleDark.copy(alpha = 0.5f))
                        .border(0.6.dp, StudioPurpleLight.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("المعاينة الجديدة:", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("F$minFrame ➔ F$newEndFrame (${targetDuration}f)", color = StudioYellow, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(StudioPurple)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "معدل: ${String.format(java.util.Locale.US, "%.2f", calculatedScale)}x",
                                color = Color.White,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // 1. Duration / Scale Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = targetDurationText,
                        onValueChange = {
                            targetDurationText = it
                            val d = it.toIntOrNull()
                            if (d != null && originalDuration > 0) {
                                val s = (d.toFloat() / originalDuration.toFloat()) * 100f
                                scalePercentText = s.toInt().toString()
                            }
                        },
                        label = { Text("المدة المستهدفة (فريم)", fontSize = 9.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = scalePercentText,
                        onValueChange = {
                            scalePercentText = it
                            val p = it.toFloatOrNull()
                            if (p != null) {
                                val d = ((p / 100f) * originalDuration).toInt().coerceAtLeast(1)
                                targetDurationText = d.toString()
                            }
                        },
                        label = { Text("النسبة المئوية %", fontSize = 9.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 2. Quick Multiplier Chips (50%, 75%, 125%, 150%, 200%)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    listOf(
                        0.5f to "50% (2x أسرع)",
                        0.75f to "75%",
                        1.25f to "125%",
                        1.5f to "150%",
                        2.0f to "200% (نصف السرعة)"
                    ).forEach { (scale, label) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(EngineCardBg)
                                .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                                .clickable {
                                    val d = (originalDuration * scale).toInt().coerceAtLeast(1)
                                    targetDurationText = d.toString()
                                    scalePercentText = (scale * 100).toInt().toString()
                                }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, color = TextSecondary, fontSize = 7.5.sp, maxLines = 1)
                        }
                    }
                }

                // 3. Timing Presets (10f, 12f, 15f, 24f, 30f, 60f)
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("قوالب توقيت قياسية (Presets):", color = TextMuted, fontSize = 8.5.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        listOf(10, 12, 15, 24, 30, 60).forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (targetDuration == preset) StudioPurple else EngineBackground)
                                    .border(0.5.dp, if (targetDuration == preset) StudioPurpleLight else StudioBorder, RoundedCornerShape(3.dp))
                                    .clickable {
                                        targetDurationText = preset.toString()
                                        if (originalDuration > 0) {
                                            scalePercentText = ((preset.toFloat() / originalDuration.toFloat()) * 100).toInt().toString()
                                        }
                                    }
                                    .padding(vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${preset}f", color = if (targetDuration == preset) Color.White else TextSecondary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                // 4. Quick Timing Operations: Distribute, Reverse, Hold
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Distribute Evenly
                    OutlinedButton(
                        onClick = {
                            onDistributeEvenly()
                            onDismiss()
                        },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        modifier = Modifier.weight(1f).height(30.dp),
                        border = BorderStroke(0.5.dp, StudioBorder)
                    ) {
                        Icon(Icons.Default.SpaceBar, contentDescription = null, tint = StudioGreen, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("توزيع متساوٍ", color = StudioGreen, fontSize = 8.sp)
                    }

                    // Reverse
                    OutlinedButton(
                        onClick = {
                            onReverseTiming()
                            onDismiss()
                        },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        modifier = Modifier.weight(1f).height(30.dp),
                        border = BorderStroke(0.5.dp, StudioBorder)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = StudioOrange, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("عكس التوقيت", color = StudioOrange, fontSize = 8.sp)
                    }

                    // Hold
                    OutlinedButton(
                        onClick = {
                            onHoldKeyframes()
                            onDismiss()
                        },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                        modifier = Modifier.weight(1f).height(30.dp),
                        border = BorderStroke(0.5.dp, StudioBorder)
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null, tint = StudioBlue, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("تثبيت (Hold)", color = StudioBlue, fontSize = 8.sp)
                    }
                }

                // Apply / Cancel
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
                            if (targetDuration != originalDuration) {
                                onSetDuration(targetDuration)
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("تطبيق التوقيت (Apply)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

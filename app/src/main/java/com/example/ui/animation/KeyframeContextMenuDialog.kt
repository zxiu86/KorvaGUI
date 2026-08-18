package com.example.ui.animation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.engine.animation.InterpolationType
import com.example.engine.animation.KeyframeData
import com.example.engine.animation.TrackData
import com.example.ui.theme.*

@Composable
fun KeyframeContextMenuDialog(
    track: TrackData,
    keyframe: KeyframeData,
    onDismiss: () -> Unit,
    onSetValue: (Float) -> Unit,
    onSetInterpolation: (InterpolationType) -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onOpenCurveEditor: () -> Unit
) {
    var valueText by remember(keyframe) { mutableStateOf(keyframe.value.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.width(300.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = EngineSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioPurpleBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = track.displayColor, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إطار ${track.name} (Frame ${keyframe.frame})", color = TextPrimary, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted, modifier = Modifier.size(13.dp))
                    }
                }

                HorizontalDivider(color = StudioBorder)

                // 1. Value Input & Stepper
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("القيمة (Value):", color = TextSecondary, fontSize = 9.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val cur = valueText.toFloatOrNull() ?: keyframe.value
                                val next = cur - 1f
                                valueText = next.toString()
                                onSetValue(next)
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "إنقاص", tint = TextSecondary, modifier = Modifier.size(13.dp))
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(EngineBackground)
                                .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.text.BasicTextField(
                                value = valueText,
                                onValueChange = {
                                    valueText = it
                                    it.toFloatOrNull()?.let { v -> onSetValue(v) }
                                },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = StudioPurpleLight,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        IconButton(
                            onClick = {
                                val cur = valueText.toFloatOrNull() ?: keyframe.value
                                val next = cur + 1f
                                valueText = next.toString()
                                onSetValue(next)
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "زيادة", tint = TextSecondary, modifier = Modifier.size(13.dp))
                        }
                    }
                }

                // 2. Interpolation Mode Dropdown / Chips
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("نوع المنحنى (Interpolation):", color = TextSecondary, fontSize = 9.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(EngineBackground)
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        listOf(
                            InterpolationType.LINEAR to "Linear",
                            InterpolationType.EASE_IN_OUT to "Ease",
                            InterpolationType.CONSTANT to "Hold",
                            InterpolationType.BEZIER to "Bezier"
                        ).forEach { (type, label) ->
                            val isChosen = keyframe.interpolation == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isChosen) StudioPurple else Color.Transparent)
                                    .clickable { onSetInterpolation(type) }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isChosen) Color.White else TextSecondary,
                                    fontSize = 8.5.sp,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // 3. Quick Actions (Duplicate, Open Curve Editor, Delete)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Open Curve Editor
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(EngineCardBg)
                            .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                            .clickable {
                                onDismiss()
                                onOpenCurveEditor()
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ShowChart, contentDescription = null, tint = StudioYellow, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("فتح محرر المنحنيات الكامل (Curve Editor)", color = TextPrimary, fontSize = 9.5.sp)
                        }
                    }

                    // Duplicate Keyframe
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(EngineCardBg)
                            .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                            .clickable {
                                onDuplicate()
                                onDismiss()
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = StudioBlue, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("نسخ وتكرار الإطار (Duplicate)", color = TextPrimary, fontSize = 9.5.sp)
                        }
                    }

                    // Delete Keyframe
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(StudioRed.copy(alpha = 0.15f))
                            .border(0.5.dp, StudioRed.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .clickable {
                                onDelete()
                                onDismiss()
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = StudioRed, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("حذف الإطار المفتاحي (Delete)", color = StudioRed, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

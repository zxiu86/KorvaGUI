package com.example.ui.animation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.animation.InterpolationType
import com.example.engine.animation.KeyframeData
import com.example.engine.animation.TrackData
import com.example.ui.components.KorvaDangerButton
import com.example.ui.components.KorvaDialog
import com.example.ui.components.KorvaOutlinedButton
import com.example.ui.theme.EngineBackground
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.KorvaBlue
import com.example.ui.theme.KorvaPurple
import com.example.ui.theme.KorvaPurpleLight
import com.example.ui.theme.KorvaRed
import com.example.ui.theme.KorvaYellow
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

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

    KorvaDialog(
        onDismissRequest = onDismiss,
        title = "تحكم الإطار المفتاحي",
        subtitle = "${track.name} • Frame ${keyframe.frame}",
        icon = Icons.Default.Tune,
        iconTint = track.displayColor,
        maxWidth = 360.dp,
        buttons = {
            KorvaOutlinedButton(
                text = "إغلاق",
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )

            KorvaDangerButton(
                text = "حذف الإطار",
                onClick = {
                    onDelete()
                    onDismiss()
                },
                icon = Icons.Default.Delete,
                modifier = Modifier.weight(1f)
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Value Input & Stepper
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("القيمة (Value):", color = TextSecondary, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = {
                            val cur = valueText.toFloatOrNull() ?: keyframe.value
                            val next = cur - 1f
                            valueText = next.toString()
                            onSetValue(next)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EngineCardBg)
                            .border(0.8.dp, StudioBorder, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "إنقاص", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EngineBackground)
                            .border(0.8.dp, KorvaPurpleLight.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = valueText,
                            onValueChange = {
                                valueText = it
                                it.toFloatOrNull()?.let { v -> onSetValue(v) }
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = KorvaPurpleLight,
                                fontSize = 13.sp,
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
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EngineCardBg)
                            .border(0.8.dp, StudioBorder, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "زيادة", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // 2. Interpolation Mode Chips
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("نوع المنحنى (Interpolation):", color = TextSecondary, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(EngineBackground)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
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
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isChosen) KorvaPurple else Color.Transparent)
                                .clickable { onSetInterpolation(type) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isChosen) Color.White else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // 3. Quick Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Open Curve Editor
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(EngineCardBg)
                        .border(0.8.dp, StudioBorder, RoundedCornerShape(8.dp))
                        .clickable {
                            onDismiss()
                            onOpenCurveEditor()
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShowChart, contentDescription = null, tint = KorvaYellow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("فتح محرر المنحنيات الكامل (Curve Editor)", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // Duplicate Keyframe
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(EngineCardBg)
                        .border(0.8.dp, StudioBorder, RoundedCornerShape(8.dp))
                        .clickable {
                            onDuplicate()
                            onDismiss()
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = KorvaBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("نسخ وتكرار الإطار (Duplicate)", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

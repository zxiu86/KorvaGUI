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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
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
import com.example.ui.components.KorvaDialog
import com.example.ui.components.KorvaOutlinedButton
import com.example.ui.components.KorvaPrimaryButton
import com.example.ui.theme.EngineBackground
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.KorvaBlue
import com.example.ui.theme.KorvaGreen
import com.example.ui.theme.KorvaPurple
import com.example.ui.theme.KorvaPurpleDark
import com.example.ui.theme.KorvaPurpleLight
import com.example.ui.theme.KorvaYellow
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

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
    val scrollState = rememberScrollState()

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

    KorvaDialog(
        onDismissRequest = onDismiss,
        title = "التحكم الذكي بالتوقيت (Smart Retiming)",
        subtitle = "تمديد وضغط وتوزيع الإطارات المفتاحية",
        icon = Icons.Default.Speed,
        maxWidth = 420.dp,
        buttons = {
            KorvaOutlinedButton(
                text = "إلغاء",
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )

            KorvaPrimaryButton(
                text = "تطبيق التوقيت",
                onClick = {
                    if (targetDuration != originalDuration) {
                        onSetDuration(targetDuration)
                    }
                    onDismiss()
                },
                icon = Icons.Default.Check,
                modifier = Modifier.weight(1.2f)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Info Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(EngineCardBg)
                    .border(0.8.dp, StudioBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("الإطارات المحددة: ${selectedKeys.size}", color = TextSecondary, fontSize = 11.sp)
                        Text("النطاق: F$minFrame ➔ F$maxFrame", color = KorvaPurpleLight, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("المدة الحالية: $originalDuration فريم", color = TextMuted, fontSize = 10.sp)
                        Text("${String.format(java.util.Locale.US, "%.3f", if (clip.fps > 0) originalDuration.toFloat() / clip.fps else 0f)}s", color = KorvaBlue, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            // Live Timing Preview Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(KorvaPurpleDark.copy(alpha = 0.5f))
                    .border(0.8.dp, KorvaPurpleLight.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("المعاينة الجديدة:", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("F$minFrame ➔ F$newEndFrame (${targetDuration}f)", color = KorvaYellow, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(KorvaPurple)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "معدل: ${String.format(java.util.Locale.US, "%.2f", calculatedScale)}x",
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // 1. Duration / Scale Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                    label = { Text("المدة المستهدفة (فريم)", fontSize = 10.sp) },
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
                    value = scalePercentText,
                    onValueChange = {
                        scalePercentText = it
                        val p = it.toFloatOrNull()
                        if (p != null) {
                            val d = ((p / 100f) * originalDuration).toInt().coerceAtLeast(1)
                            targetDurationText = d.toString()
                        }
                    },
                    label = { Text("النسبة المئوية %", fontSize = 10.sp) },
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
            }

            // 2. Quick Multiplier Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    0.5f to "50% (2x)",
                    0.75f to "75%",
                    1.25f to "125%",
                    1.5f to "150%",
                    2.0f to "200% (0.5x)"
                ).forEach { (scale, label) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(EngineCardBg)
                            .border(0.8.dp, StudioBorder, RoundedCornerShape(6.dp))
                            .clickable {
                                val d = (originalDuration * scale).toInt().coerceAtLeast(1)
                                targetDurationText = d.toString()
                                scalePercentText = (scale * 100).toInt().toString()
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = TextSecondary, fontSize = 9.sp, maxLines = 1, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // 3. Timing Operations: Distribute, Reverse, Hold
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Distribute Evenly
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(EngineCardBg)
                        .border(0.8.dp, StudioBorder, RoundedCornerShape(6.dp))
                        .clickable {
                            onDistributeEvenly()
                            onDismiss()
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SpaceBar, contentDescription = null, tint = KorvaGreen, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("توزيع متساوٍ", color = KorvaGreen, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Reverse
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(EngineCardBg)
                        .border(0.8.dp, StudioBorder, RoundedCornerShape(6.dp))
                        .clickable {
                            onReverseTiming()
                            onDismiss()
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = StudioOrange, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("عكس التوقيت", color = StudioOrange, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Hold
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(EngineCardBg)
                        .border(0.8.dp, StudioBorder, RoundedCornerShape(6.dp))
                        .clickable {
                            onHoldKeyframes()
                            onDismiss()
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Pause, contentDescription = null, tint = KorvaBlue, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تثبيت (Hold)", color = KorvaBlue, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

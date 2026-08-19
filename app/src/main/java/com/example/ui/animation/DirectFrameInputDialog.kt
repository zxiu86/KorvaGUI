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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.KorvaDialog
import com.example.ui.components.KorvaOutlinedButton
import com.example.ui.components.KorvaPrimaryButton
import com.example.ui.theme.EngineBackground
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.KorvaBlue
import com.example.ui.theme.KorvaPurpleLight
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DirectFrameInputDialog(
    currentFrame: Int,
    fps: Int,
    maxFrames: Int,
    onDismiss: () -> Unit,
    onJumpToFrame: (Int) -> Unit
) {
    var frameInput by remember { mutableStateOf(currentFrame.toString()) }
    var timeInput by remember {
        val sec = if (fps > 0) currentFrame.toFloat() / fps else 0f
        mutableStateOf(String.format(java.util.Locale.US, "%.3f", sec))
    }

    KorvaDialog(
        onDismissRequest = onDismiss,
        title = "الانتقال المباشر للإطار / الوقت",
        subtitle = "تحديد موقع مؤشر الرأس بدقة",
        icon = Icons.Default.Navigation,
        maxWidth = 380.dp,
        buttons = {
            KorvaOutlinedButton(
                text = "إلغاء",
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )

            KorvaPrimaryButton(
                text = "انتقال (Go)",
                onClick = {
                    val target = frameInput.toIntOrNull()?.coerceIn(0, maxFrames) ?: currentFrame
                    onJumpToFrame(target)
                    onDismiss()
                },
                icon = Icons.Default.PlayArrow,
                modifier = Modifier.weight(1f)
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Frame Input
            OutlinedTextField(
                value = frameInput,
                onValueChange = { input ->
                    frameInput = input
                    val parsed = input.toIntOrNull()
                    if (parsed != null && fps > 0) {
                        timeInput = String.format(java.util.Locale.US, "%.3f", parsed.toFloat() / fps)
                    }
                },
                label = { Text("رقم الإطار (Frame 0..$maxFrames)", fontSize = 11.sp) },
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

            // Seconds Input
            OutlinedTextField(
                value = timeInput,
                onValueChange = { input ->
                    timeInput = input
                    val parsedSec = input.toFloatOrNull()
                    if (parsedSec != null && fps > 0) {
                        frameInput = (parsedSec * fps).toInt().coerceIn(0, maxFrames).toString()
                    }
                },
                label = { Text("الوقت بالثواني (Seconds)", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, tint = KorvaBlue, modifier = Modifier.size(16.dp)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KorvaBlue,
                    unfocusedBorderColor = StudioBorder,
                    focusedContainerColor = EngineCardBg,
                    unfocusedContainerColor = EngineCardBg,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Quick Jump Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(0 to "البداية (0)", (maxFrames / 2) to "المنتصف", maxFrames to "النهاية").forEach { (f, label) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(EngineBackground)
                            .border(0.8.dp, StudioBorder, RoundedCornerShape(6.dp))
                            .clickable {
                                frameInput = f.toString()
                                if (fps > 0) {
                                    timeInput = String.format(java.util.Locale.US, "%.3f", f.toFloat() / fps)
                                }
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

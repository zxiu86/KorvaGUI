package com.example.ui.animation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

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

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.width(300.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = EngineSurface),
            border = BorderStroke(1.dp, StudioPurpleBorder)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Navigation, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("الانتقال المباشر للإطار / الوقت", color = TextPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(22.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted, modifier = Modifier.size(14.dp))
                    }
                }

                HorizontalDivider(color = StudioBorder)

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
                    label = { Text("رقم الإطار (Frame 0..$maxFrames)", fontSize = 10.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioPurpleLight,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
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
                    label = { Text("الوقت بالثواني (Seconds)", fontSize = 10.sp) },
                    leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, tint = StudioBlue, modifier = Modifier.size(14.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioBlue,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Jump Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(0 to "البداية", (maxFrames / 2) to "المنتصف", maxFrames to "النهاية").forEach { (f, label) ->
                        OutlinedButton(
                            onClick = {
                                frameInput = f.toString()
                                if (fps > 0) {
                                    timeInput = String.format(java.util.Locale.US, "%.3f", f.toFloat() / fps)
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.weight(1f).height(28.dp),
                            border = BorderStroke(0.5.dp, StudioBorder)
                        ) {
                            Text(label, fontSize = 8.5.sp, color = TextSecondary)
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
                            val target = frameInput.toIntOrNull()?.coerceIn(0, maxFrames) ?: currentFrame
                            onJumpToFrame(target)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("انتقال (Go)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

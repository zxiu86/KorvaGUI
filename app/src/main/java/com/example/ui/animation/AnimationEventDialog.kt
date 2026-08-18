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
import com.example.ui.theme.*

@Composable
fun AnimationEventDialog(
    initialFrame: Int,
    maxFrame: Int,
    onDismiss: () -> Unit,
    onAddEvent: (frame: Int, name: String, funcName: String, params: String) -> Unit
) {
    var frame by remember { mutableIntStateOf(initialFrame) }
    var eventName by remember { mutableStateOf("PlaySound") }
    var functionName by remember { mutableStateOf("PlaySound") }
    var parameters by remember { mutableStateOf("footstep_grass_01") }

    val presetEvents = listOf(
        "PlaySound" to "footstep_01",
        "SpawnParticle" to "dust_puff",
        "EnableHitbox" to "damage:35,radius:50",
        "DisableHitbox" to "",
        "ScreenShake" to "intensity:5,duration:0.1"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.width(320.dp),
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
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = StudioYellow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة حدث أنيميشن (Animation Event)", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted, modifier = Modifier.size(13.dp))
                    }
                }

                HorizontalDivider(color = StudioBorder)

                // Frame Stepper
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("الإطار المستهدف (Target Frame):", color = TextSecondary, fontSize = 9.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { frame = (frame - 1).coerceAtLeast(0) }, modifier = Modifier.size(22.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(11.dp))
                        }
                        Text("$frame", color = StudioPurpleLight, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { frame = (frame + 1).coerceAtMost(maxFrame) }, modifier = Modifier.size(22.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(11.dp))
                        }
                    }
                }

                // Presets
                Text("قوالب سريعة للأحداث:", color = TextMuted, fontSize = 8.5.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presetEvents.take(3).forEach { (func, param) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(EngineBackground)
                                .border(0.5.dp, StudioBorder, RoundedCornerShape(3.dp))
                                .clickable {
                                    eventName = func
                                    functionName = func
                                    parameters = param
                                }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(func, color = StudioYellow, fontSize = 8.sp, maxLines = 1)
                        }
                    }
                }

                // Event Name Input
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("اسم الحدث (Event Identifier):", color = TextSecondary, fontSize = 8.5.sp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(EngineBackground)
                            .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = eventName,
                            onValueChange = { eventName = it },
                            textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 9.5.sp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Parameters Input
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("المعاملات (Parameters):", color = TextSecondary, fontSize = 8.5.sp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(EngineBackground)
                            .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = parameters,
                            onValueChange = { parameters = it },
                            textStyle = androidx.compose.ui.text.TextStyle(color = StudioGreen, fontSize = 9.sp, fontFamily = FontFamily.Monospace),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = EngineCardBg),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f).height(30.dp)
                    ) {
                        Text("إلغاء", color = TextSecondary, fontSize = 9.5.sp)
                    }

                    Button(
                        onClick = {
                            if (eventName.isNotBlank()) {
                                onAddEvent(frame, eventName, functionName, parameters)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1.2f).height(30.dp)
                    ) {
                        Text("إضافة الحدث", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AnimationMarkerDialog(
    initialFrame: Int,
    maxFrame: Int,
    onDismiss: () -> Unit,
    onAddMarker: (frame: Int, label: String, colorHex: Long) -> Unit
) {
    var frame by remember { mutableIntStateOf(initialFrame) }
    var label by remember { mutableStateOf("Impact") }
    var selectedColor by remember { mutableLongStateOf(0xFFFACC15) }

    val presetColors = listOf(
        0xFFFACC15 to "أصفر",
        0xFFEF4444 to "أحمر",
        0xFF22C55E to "أخضر",
        0xFF38BDF8 to "أزرق",
        0xFFA855F7 to "بنفسجي"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.width(280.dp),
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color(selectedColor), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة علامة زمنية (Timeline Marker)", color = TextPrimary, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted, modifier = Modifier.size(13.dp))
                    }
                }

                HorizontalDivider(color = StudioBorder)

                // Target Frame
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("الإطار (Frame):", color = TextSecondary, fontSize = 9.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { frame = (frame - 1).coerceAtLeast(0) }, modifier = Modifier.size(22.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(11.dp))
                        }
                        Text("$frame", color = StudioPurpleLight, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { frame = (frame + 1).coerceAtMost(maxFrame) }, modifier = Modifier.size(22.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(11.dp))
                        }
                    }
                }

                // Label
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("نص العلامة (Label):", color = TextSecondary, fontSize = 8.5.sp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(EngineBackground)
                            .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = label,
                            onValueChange = { label = it },
                            textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 9.5.sp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Color Chooser
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetColors.forEach { (colorHex, _) ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(colorHex))
                                .border(
                                    if (selectedColor == colorHex) 1.5.dp else 0.dp,
                                    Color.White,
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable { selectedColor = colorHex }
                        )
                    }
                }

                // Submit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = EngineCardBg),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f).height(30.dp)
                    ) {
                        Text("إلغاء", color = TextSecondary, fontSize = 9.5.sp)
                    }

                    Button(
                        onClick = {
                            if (label.isNotBlank()) {
                                onAddMarker(frame, label, selectedColor)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1.2f).height(30.dp)
                    ) {
                        Text("حفظ العلامة", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

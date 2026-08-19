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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.ui.components.KorvaDialog
import com.example.ui.components.KorvaOutlinedButton
import com.example.ui.components.KorvaPrimaryButton
import com.example.ui.theme.EngineBackground
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.KorvaGreen
import com.example.ui.theme.KorvaPurpleLight
import com.example.ui.theme.KorvaYellow
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

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

    KorvaDialog(
        onDismissRequest = onDismiss,
        title = "إضافة حدث أنيميشن",
        subtitle = "ربط الدوال والأصوات والمؤثرات بالتايم لاين",
        icon = Icons.Default.Bolt,
        iconTint = KorvaYellow,
        maxWidth = 400.dp,
        buttons = {
            KorvaOutlinedButton(
                text = "إلغاء",
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )

            KorvaPrimaryButton(
                text = "إضافة الحدث",
                onClick = {
                    if (eventName.isNotBlank()) {
                        onAddEvent(frame, eventName.trim(), functionName.trim(), parameters.trim())
                        onDismiss()
                    }
                },
                icon = Icons.Default.Check,
                modifier = Modifier.weight(1.2f)
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Frame Stepper
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(EngineCardBg)
                    .border(0.8.dp, StudioBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("الإطار المستهدف:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { frame = (frame - 1).coerceAtLeast(0) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = "$frame",
                        color = KorvaPurpleLight,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = { frame = (frame + 1).coerceAtMost(maxFrame) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Quick Presets
            Text("قوالب سريعة للأحداث:", color = TextMuted, fontSize = 10.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                presetEvents.take(3).forEach { (func, param) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(EngineBackground)
                            .border(0.8.dp, StudioBorder, RoundedCornerShape(6.dp))
                            .clickable {
                                eventName = func
                                functionName = func
                                parameters = param
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(func, color = KorvaYellow, fontSize = 9.5.sp, maxLines = 1, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Event Name Input
            OutlinedTextField(
                value = eventName,
                onValueChange = {
                    eventName = it
                    functionName = it
                },
                label = { Text("اسم الحدث (Event Identifier)", fontSize = 11.sp) },
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

            // Parameters Input
            OutlinedTextField(
                value = parameters,
                onValueChange = { parameters = it },
                label = { Text("المعاملات (Parameters)", fontSize = 11.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KorvaGreen,
                    unfocusedBorderColor = StudioBorder,
                    focusedContainerColor = EngineCardBg,
                    unfocusedContainerColor = EngineCardBg,
                    focusedTextColor = KorvaGreen,
                    unfocusedTextColor = KorvaGreen
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            )
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

    KorvaDialog(
        onDismissRequest = onDismiss,
        title = "إضافة علامة زمنية (Marker)",
        subtitle = "وضع إشارة مرجعية مميزة على شريط الإطارات",
        icon = Icons.Default.Bookmark,
        iconTint = Color(selectedColor),
        maxWidth = 380.dp,
        buttons = {
            KorvaOutlinedButton(
                text = "إلغاء",
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )

            KorvaPrimaryButton(
                text = "حفظ العلامة",
                onClick = {
                    if (label.isNotBlank()) {
                        onAddMarker(frame, label.trim(), selectedColor)
                        onDismiss()
                    }
                },
                icon = Icons.Default.Check,
                modifier = Modifier.weight(1.2f)
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Target Frame Stepper
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(EngineCardBg)
                    .border(0.8.dp, StudioBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("الإطار المستهدف:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { frame = (frame - 1).coerceAtLeast(0) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = "$frame",
                        color = KorvaPurpleLight,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = { frame = (frame + 1).coerceAtMost(maxFrame) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Marker Label Field
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("نص العلامة (Label)", fontSize = 11.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(selectedColor),
                    unfocusedBorderColor = StudioBorder,
                    focusedContainerColor = EngineCardBg,
                    unfocusedContainerColor = EngineCardBg,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Color Palette Selector
            Text("لون العلامة:", color = TextSecondary, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetColors.forEach { (colorHex, _) ->
                    val isChosen = selectedColor == colorHex
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(colorHex))
                            .border(
                                width = if (isChosen) 2.dp else 0.dp,
                                color = if (isChosen) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedColor = colorHex }
                    )
                }
            }
        }
    }
}

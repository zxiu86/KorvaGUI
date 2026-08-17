package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.example.engine.interfaces.IProperty
import com.example.engine.interfaces.PropertyType
import com.example.engine.interfaces.PropertyValue
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun DynamicPropertyWidget(
    property: IProperty,
    onValueChanged: (PropertyValue) -> Unit,
    onOpenTexturePreview: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Property Label & Type Tag
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = property.name,
                color = TextSecondary,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = property.type.name.lowercase(),
                color = TextMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Property Editor according to Type
        when (property.type) {
            PropertyType.BOOL -> {
                val boolVal = (property.value as? PropertyValue.BoolValue)?.value ?: false
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (boolVal) "ON (مفعل)" else "OFF (معطل)",
                        color = if (boolVal) StudioGreen else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = boolVal,
                        onCheckedChange = { onValueChanged(PropertyValue.BoolValue(it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = StudioPurple,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = EngineCardBg
                        ),
                        modifier = Modifier.size(38.dp, 24.dp)
                    )
                }
            }

            PropertyType.FLOAT -> {
                val currentVal = (property.value as? PropertyValue.FloatValue)?.value ?: 0f
                val min = property.min ?: 0f
                val max = property.max ?: 100f
                val step = property.step ?: 0.1f

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Slider(
                        value = currentVal.coerceIn(min, max),
                        onValueChange = {
                            val rounded = (it / step).roundToInt() * step
                            onValueChanged(PropertyValue.FloatValue(rounded))
                        },
                        valueRange = min..max,
                        colors = SliderDefaults.colors(
                            thumbColor = StudioPurpleLight,
                            activeTrackColor = StudioPurple,
                            inactiveTrackColor = EngineCardBg
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = String.format("%.2f", currentVal),
                        color = TextPrimary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .background(EngineCardBg, RoundedCornerShape(4.dp))
                            .border(0.6.dp, StudioBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            PropertyType.INT -> {
                val currentVal = (property.value as? PropertyValue.IntValue)?.value ?: 0
                val min = (property.min ?: 0f).toInt()
                val max = (property.max ?: 100f).toInt()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = { onValueChanged(PropertyValue.IntValue((currentVal - 1).coerceAtLeast(min))) },
                        modifier = Modifier.size(28.dp).background(EngineCardBg, RoundedCornerShape(4.dp))
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TextPrimary, modifier = Modifier.size(14.dp))
                    }

                    Text(
                        text = currentVal.toString(),
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .weight(1f)
                            .background(EngineCardBg, RoundedCornerShape(4.dp))
                            .border(0.6.dp, StudioBorder, RoundedCornerShape(4.dp))
                            .padding(vertical = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    IconButton(
                        onClick = { onValueChanged(PropertyValue.IntValue((currentVal + 1).coerceAtMost(max))) },
                        modifier = Modifier.size(28.dp).background(EngineCardBg, RoundedCornerShape(4.dp))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = TextPrimary, modifier = Modifier.size(14.dp))
                    }
                }
            }

            PropertyType.STRING -> {
                val currentVal = (property.value as? PropertyValue.StringValue)?.value ?: ""
                OutlinedTextField(
                    value = currentVal,
                    onValueChange = { onValueChanged(PropertyValue.StringValue(it)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioPurpleLight,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = StudioPurpleLight,
                        focusedContainerColor = EngineCardBg,
                        unfocusedContainerColor = EngineCardBg
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                )
            }

            PropertyType.ENUM -> {
                val enumVal = property.value as? PropertyValue.EnumValue
                val selected = enumVal?.selected ?: ""
                val options = enumVal?.options ?: emptyList()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    options.forEach { option ->
                        val isSelected = option == selected
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) StudioPurple else EngineCardBg)
                                .border(0.6.dp, if (isSelected) StudioPurpleLight else StudioBorder, RoundedCornerShape(6.dp))
                                .clickable { onValueChanged(PropertyValue.EnumValue(option, options)) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option,
                                color = if (isSelected) Color.White else TextSecondary,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            PropertyType.VECTOR2 -> {
                val vec = (property.value as? PropertyValue.Vector2Value) ?: PropertyValue.Vector2Value(0f, 0f)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // X Axis Input
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(EngineCardBg)
                            .border(0.6.dp, StudioBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("X", color = StudioRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = String.format("%.1f", vec.x),
                            color = TextPrimary,
                            fontSize = 11.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Y Axis Input
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(EngineCardBg)
                            .border(0.6.dp, StudioBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Y", color = StudioGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = String.format("%.1f", vec.y),
                            color = TextPrimary,
                            fontSize = 11.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            PropertyType.COLOR -> {
                val colVal = (property.value as? PropertyValue.ColorValue)?.hex ?: "#8B5CF6"
                val parsedColor = try {
                    Color(android.graphics.Color.parseColor(colVal))
                } catch (e: Exception) {
                    StudioPurple
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(EngineCardBg)
                        .border(0.6.dp, StudioBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(parsedColor)
                                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(colVal, color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }

                    // Quick Palette Preset Chooser
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("#8B5CF6", "#EF4444", "#22C55E", "#38BDF8", "#FBBF24").forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .clickable { onValueChanged(PropertyValue.ColorValue(hex)) }
                            )
                        }
                    }
                }
            }

            PropertyType.TEXTURE -> {
                val texPath = (property.value as? PropertyValue.TextureValue)?.assetPath ?: "none.png"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(EngineCardBg)
                        .border(0.6.dp, StudioBorder, RoundedCornerShape(6.dp))
                        .clickable { onOpenTexturePreview?.invoke(texPath) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(texPath, color = TextPrimary, fontSize = 11.sp, maxLines = 1)
                    }
                    Text("معاينة", color = StudioBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            PropertyType.AUDIO -> {
                val audPath = (property.value as? PropertyValue.AudioValue)?.assetPath ?: "none.wav"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(EngineCardBg)
                        .border(0.6.dp, StudioBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = StudioOrange, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(audPath, color = TextPrimary, fontSize = 11.sp, maxLines = 1)
                    }
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = StudioGreen, modifier = Modifier.size(18.dp))
                }
            }

            else -> {
                // Fallback for other property types
                Text(
                    text = property.value.toString(),
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EngineCardBg, RoundedCornerShape(4.dp))
                        .padding(6.dp)
                )
            }
        }
    }
}

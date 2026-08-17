package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
    isCompact: Boolean = false,
    onOpenTexturePreview: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var isExpandedDial by remember { mutableStateOf(false) }

    when (property.type) {
        PropertyType.VECTOR2 -> {
            val vec = (property.value as? PropertyValue.Vector2Value) ?: PropertyValue.Vector2Value(0f, 0f)
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = property.name,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(68.dp)
                )

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Compact X
                    CompactAxisInput(
                        label = "X",
                        value = vec.x,
                        color = StudioRed,
                        onValueChange = { onValueChanged(PropertyValue.Vector2Value(it, vec.y)) },
                        modifier = Modifier.weight(1f)
                    )

                    // Compact Y
                    CompactAxisInput(
                        label = "Y",
                        value = vec.y,
                        color = StudioGreen,
                        onValueChange = { onValueChanged(PropertyValue.Vector2Value(vec.x, it)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        PropertyType.VECTOR2I -> {
            val vec = (property.value as? PropertyValue.Vector2iValue) ?: PropertyValue.Vector2iValue(0, 0)
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = property.name,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(68.dp)
                )

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompactAxisInput(
                        label = "X",
                        value = vec.x.toFloat(),
                        color = StudioRed,
                        onValueChange = { onValueChanged(PropertyValue.Vector2iValue(it.toInt(), vec.y)) },
                        modifier = Modifier.weight(1f)
                    )
                    CompactAxisInput(
                        label = "Y",
                        value = vec.y.toFloat(),
                        color = StudioGreen,
                        onValueChange = { onValueChanged(PropertyValue.Vector2iValue(vec.x, it.toInt())) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        PropertyType.FLOAT -> {
            val currentVal = (property.value as? PropertyValue.FloatValue)?.value ?: 0f
            val min = property.min ?: 0f
            val max = property.max ?: 100f
            val step = property.step ?: 0.1f
            val isRotation = property.name.contains("rot", true) || property.name.contains("angle", true)

            Column(modifier = modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = property.name,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Quick Stepper / Number pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EngineCardBg)
                                .border(0.6.dp, StudioBorder, RoundedCornerShape(4.dp))
                                .clickable { isExpandedDial = !isExpandedDial }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isRotation) "${currentVal.roundToInt()}°" else String.format("%.1f", currentVal),
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isRotation) {
                            IconButton(
                                onClick = {
                                    val nextRot = ((currentVal + 45f) % 360f)
                                    onValueChanged(PropertyValue.FloatValue(nextRot))
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.RotateRight, contentDescription = "Rotate 45", tint = StudioPurpleLight, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                // Expandable Precision Slider on tap
                if (isExpandedDial || !isCompact) {
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
                        modifier = Modifier.fillMaxWidth().height(26.dp)
                    )
                }
            }
        }

        PropertyType.INT -> {
            val currentVal = (property.value as? PropertyValue.IntValue)?.value ?: 0
            val min = (property.min ?: 0f).toInt()
            val max = (property.max ?: 100f).toInt()

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = property.name,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { onValueChanged(PropertyValue.IntValue((currentVal - 1).coerceAtLeast(min))) },
                        modifier = Modifier.size(22.dp).background(EngineCardBg, RoundedCornerShape(4.dp))
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TextPrimary, modifier = Modifier.size(12.dp))
                    }

                    Text(
                        text = currentVal.toString(),
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .widthIn(min = 28.dp)
                            .background(EngineCardBg, RoundedCornerShape(4.dp))
                            .border(0.6.dp, StudioBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = { onValueChanged(PropertyValue.IntValue((currentVal + 1).coerceAtMost(max))) },
                        modifier = Modifier.size(22.dp).background(EngineCardBg, RoundedCornerShape(4.dp))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = TextPrimary, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }

        PropertyType.BOOL -> {
            val boolVal = (property.value as? PropertyValue.BoolValue)?.value ?: false
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = property.name,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (boolVal) "ON" else "OFF",
                        color = if (boolVal) StudioGreen else TextMuted,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
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
                        modifier = Modifier.size(34.dp, 20.dp)
                    )
                }
            }
        }

        PropertyType.ENUM -> {
            val enumVal = property.value as? PropertyValue.EnumValue
            val selected = enumVal?.selected ?: ""
            val options = enumVal?.options ?: emptyList()

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = property.name,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    options.forEach { option ->
                        val isSelected = option.equals(selected, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) StudioPurple else EngineCardBg)
                                .border(0.6.dp, if (isSelected) StudioPurpleLight else StudioBorder, RoundedCornerShape(4.dp))
                                .clickable { onValueChanged(PropertyValue.EnumValue(option, options)) }
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option,
                                color = if (isSelected) Color.White else TextSecondary,
                                fontSize = 9.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
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
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = property.name,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(parsedColor)
                            .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    )
                    listOf("#8B5CF6", "#EF4444", "#22C55E", "#38BDF8", "#FBBF24").forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(12.dp)
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
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(EngineCardBg)
                    .clickable { onOpenTexturePreview?.invoke(texPath) }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(texPath, color = TextPrimary, fontSize = 10.5.sp, maxLines = 1)
                }
                Text("معاينة", color = StudioBlue, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
            }
        }

        PropertyType.STRING -> {
            val currentVal = (property.value as? PropertyValue.StringValue)?.value ?: ""
            var editVal by remember(currentVal) { mutableStateOf(currentVal) }

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = property.name,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                OutlinedTextField(
                    value = editVal,
                    onValueChange = {
                        editVal = it
                        onValueChanged(PropertyValue.StringValue(it))
                    },
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
                        .width(120.dp)
                        .height(34.dp)
                )
            }
        }

        else -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = property.name, color = TextSecondary, fontSize = 11.sp)
                Text(text = property.value.toString(), color = TextPrimary, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
private fun CompactAxisInput(
    label: String,
    value: Float,
    color: Color,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var textValue by remember(value) { mutableStateOf(String.format("%.0f", value)) }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(EngineCardBg)
            .border(0.6.dp, StudioBorder, RoundedCornerShape(4.dp))
            .clickable { isEditing = !isEditing }
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(label, color = color, fontWeight = FontWeight.Bold, fontSize = 10.sp)
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = String.format("%.0f", value),
            color = TextPrimary,
            fontSize = 10.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}


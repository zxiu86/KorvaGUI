package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.interfaces.IProperty
import com.example.engine.interfaces.ISection
import com.example.engine.interfaces.PropertyValue
import com.example.ui.theme.*

@Composable
fun DynamicSectionCard(
    section: ISection,
    onPropertyValueChanged: (propertyId: String, newValue: PropertyValue) -> Unit,
    onToggleSectionEnabled: (Boolean) -> Unit,
    onRemoveSection: () -> Unit,
    isInitiallyExpanded: Boolean = false,
    isCompactMode: Boolean = false,
    onOpenTexturePreview: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember(section.id, isInitiallyExpanded) { mutableStateOf(isInitiallyExpanded) }
    var showAdvanced by remember { mutableStateOf(false) }

    val icon: ImageVector = when (section.name.lowercase()) {
        "transform" -> Icons.Default.OpenWith
        "sprite" -> Icons.Default.Image
        "physics" -> Icons.Default.Science
        "animation" -> Icons.Default.Movie
        "logic (brain)", "logic", "brain" -> Icons.Default.Psychology
        "light 2d", "light" -> Icons.Default.Lightbulb
        "camera 2d", "camera" -> Icons.Default.Videocam
        "particles 2d", "particles" -> Icons.Default.AutoAwesome
        "audio source", "audio" -> Icons.Default.VolumeUp
        "ui element", "ui" -> Icons.Default.DashboardCustomize
        else -> Icons.Default.Layers
    }

    // Build a compact summary text for collapsed preview
    val summaryText = remember(section.properties) {
        when (section.name.lowercase()) {
            "transform" -> {
                val pos = section.properties.find { it.name.contains("pos", true) }?.value as? PropertyValue.Vector2Value
                val rot = section.properties.find { it.name.contains("rot", true) }?.value as? PropertyValue.FloatValue
                if (pos != null && rot != null) "(${pos.x.toInt()}, ${pos.y.toInt()}) ${rot.value.toInt()}°" else ""
            }
            "physics" -> {
                val body = (section.properties.find { it.name.contains("body", true) }?.value as? PropertyValue.EnumValue)?.selected ?: "Dynamic"
                "Body: $body"
            }
            "sprite" -> {
                val col = (section.properties.find { it.name.contains("color", true) || it.name.contains("tint", true) }?.value as? PropertyValue.ColorValue)?.hex
                col ?: "Visible"
            }
            else -> ""
        }
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = EngineSurface),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .border(
                width = 0.8.dp,
                color = if (isExpanded) StudioPurpleLight.copy(alpha = 0.6f) else StudioBorder,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Card Header Accordion: [ ✥ Transform ˅ ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(if (isExpanded) StudioPurpleDark.copy(alpha = 0.4f) else EngineBackground)
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Chevron Indicator
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = if (isExpanded) StudioPurpleLight else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (section.isEnabled) StudioPurpleLight else TextMuted,
                        modifier = Modifier.size(15.dp)
                    )

                    Text(
                        text = section.name,
                        color = if (section.isEnabled) TextPrimary else TextMuted,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (!isExpanded && summaryText.isNotBlank()) {
                        Text(
                            text = summaryText,
                            color = TextMuted,
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Enable/Disable Toggle
                    Switch(
                        checked = section.isEnabled,
                        onCheckedChange = { onToggleSectionEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = StudioPurple,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = EngineCardBg
                        ),
                        modifier = Modifier.size(30.dp, 18.dp)
                    )

                    // Remove Section Button
                    if (section.isRemovable) {
                        IconButton(
                            onClick = onRemoveSection,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Section",
                                tint = StudioRed.copy(alpha = 0.8f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            // Card Body (Collapsible Properties with Progressive Disclosure)
            AnimatedVisibility(
                visible = isExpanded && section.isEnabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Split properties into primary & advanced
                    val primaryCount = if (section.name.equals("Physics", true)) 3 else 5
                    val primaryProperties = section.properties.take(primaryCount)
                    val advancedProperties = section.properties.drop(primaryCount)

                    primaryProperties.forEach { property ->
                        DynamicPropertyWidget(
                            property = property,
                            onValueChanged = { newVal ->
                                onPropertyValueChanged(property.id, newVal)
                            },
                            isCompact = isCompactMode,
                            onOpenTexturePreview = onOpenTexturePreview
                        )
                    }

                    if (advancedProperties.isNotEmpty()) {
                        // Advanced Progressive Disclosure Toggle: Advanced ▸ / ▾
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { showAdvanced = !showAdvanced }
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (showAdvanced) "خيارات متقدمة ▾" else "خيارات متقدمة ▸",
                                color = StudioPurpleLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "(${advancedProperties.size})",
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }

                        if (showAdvanced) {
                            advancedProperties.forEach { property ->
                                DynamicPropertyWidget(
                                    property = property,
                                    onValueChanged = { newVal ->
                                        onPropertyValueChanged(property.id, newVal)
                                    },
                                    isCompact = isCompactMode,
                                    onOpenTexturePreview = onOpenTexturePreview
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


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
    onOpenTexturePreview: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }

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

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = EngineSurface),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(
                width = 0.8.dp,
                color = if (section.isEnabled) StudioBorder else StudioBorder.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Card Header (Touch-first collapsible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    .background(if (section.isEnabled) StudioPurpleGlass else EngineBackground)
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                        contentDescription = "Expand/Collapse",
                        tint = StudioPurpleLight,
                        modifier = Modifier.size(18.dp)
                    )

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (section.isEnabled) StudioPurpleLight else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = section.name,
                        color = if (section.isEnabled) TextPrimary else TextMuted,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                        modifier = Modifier.size(34.dp, 20.dp)
                    )

                    // Remove Section Button
                    if (section.isRemovable) {
                        IconButton(
                            onClick = onRemoveSection,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Section",
                                tint = StudioRed.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Card Body (Collapsible Properties List)
            AnimatedVisibility(
                visible = isExpanded && section.isEnabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    section.properties.forEach { property ->
                        DynamicPropertyWidget(
                            property = property,
                            onValueChanged = { newVal ->
                                onPropertyValueChanged(property.id, newVal)
                            },
                            onOpenTexturePreview = onOpenTexturePreview
                        )
                    }
                }
            }
        }
    }
}

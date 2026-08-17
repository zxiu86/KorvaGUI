package com.example.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.interfaces.IObject
import com.example.engine.interfaces.ISection
import com.example.engine.interfaces.PropertyValue
import com.example.ui.components.DynamicSectionCard
import com.example.ui.theme.*

@Composable
fun ObjectInspectorPanel(
    selectedObject: IObject?,
    onPropertyValueChanged: (sectionId: String, propertyId: String, newValue: PropertyValue) -> Unit,
    onToggleSectionEnabled: (sectionId: String, enabled: Boolean) -> Unit,
    onRemoveSection: (sectionId: String) -> Unit,
    onAddSection: (sectionType: String) -> Unit,
    onRenameObject: (newName: String) -> Unit,
    onToggleObjectVisibility: () -> Unit,
    onToggleObjectLock: () -> Unit,
    onOpenTexturePreview: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showAddSectionMenu by remember { mutableStateOf(false) }
    var isEditingName by remember { mutableStateOf(false) }
    var tempName by remember(selectedObject?.name) { mutableStateOf(selectedObject?.name ?: "") }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(EngineSurface)
            .border(width = 0.8.dp, color = StudioBorder)
    ) {
        if (selectedObject == null) {
            // Empty State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "لم يتم تحديد كائن",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "اختر كائناً من المشهد أو قائمة الطبقات لعرض بطاقات الخصائص",
                    color = TextMuted,
                    fontSize = 11.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header: Object Title, Layer Badge, Rename & Visibility
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EngineBackground)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .border(0.6.dp, StudioBorder, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(StudioPurpleLight)
                            )
                            Spacer(modifier = Modifier.width(6.dp))

                            if (isEditingName) {
                                OutlinedTextField(
                                    value = tempName,
                                    onValueChange = { tempName = it },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = StudioPurpleLight,
                                        unfocusedBorderColor = StudioBorder
                                    )
                                )
                                IconButton(
                                    onClick = {
                                        onRenameObject(tempName)
                                        isEditingName = false
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Save", tint = StudioGreen, modifier = Modifier.size(16.dp))
                                }
                            } else {
                                Text(
                                    text = selectedObject.name,
                                    color = TextPrimary,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                IconButton(
                                    onClick = {
                                        tempName = selectedObject.name
                                        isEditingName = true
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Rename", tint = TextMuted, modifier = Modifier.size(12.dp))
                                }
                            }
                        }

                        // Visibility & Lock Quick Toggles
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onToggleObjectVisibility,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = if (selectedObject.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Visibility",
                                    tint = if (selectedObject.isVisible) TextSecondary else StudioRed,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            IconButton(
                                onClick = onToggleObjectLock,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = if (selectedObject.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = "Lock",
                                    tint = if (selectedObject.isLocked) StudioYellow else TextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Layer: ${selectedObject.layerId}",
                        color = StudioPurpleLight,
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Cards List (Sections)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(selectedObject.sections, key = { it.id }) { section ->
                        DynamicSectionCard(
                            section = section,
                            onPropertyValueChanged = { propId, newVal ->
                                onPropertyValueChanged(section.id, propId, newVal)
                            },
                            onToggleSectionEnabled = { enabled ->
                                onToggleSectionEnabled(section.id, enabled)
                            },
                            onRemoveSection = {
                                onRemoveSection(section.id)
                            },
                            onOpenTexturePreview = onOpenTexturePreview
                        )
                    }

                    // Add Section Button Card at Bottom
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Button(
                                onClick = { showAddSectionMenu = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StudioPurpleDark
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, StudioPurpleLight.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("إضافة قسم جديد (Add Section)", color = TextPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }

                            DropdownMenu(
                                expanded = showAddSectionMenu,
                                onDismissRequest = { showAddSectionMenu = false },
                                modifier = Modifier.background(EngineSurface)
                            ) {
                                listOf(
                                    "Sprite" to Icons.Default.Image,
                                    "Physics" to Icons.Default.Science,
                                    "Animation" to Icons.Default.Movie,
                                    "Logic (Brain)" to Icons.Default.Psychology,
                                    "Light 2D" to Icons.Default.Lightbulb,
                                    "Camera 2D" to Icons.Default.Videocam,
                                    "Particles 2D" to Icons.Default.AutoAwesome,
                                    "Audio Source" to Icons.Default.VolumeUp,
                                    "UI Element" to Icons.Default.DashboardCustomize
                                ).forEach { (secTitle, secIcon) ->
                                    DropdownMenuItem(
                                        text = { Text(secTitle, color = TextPrimary, fontSize = 12.sp) },
                                        leadingIcon = { Icon(secIcon, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(16.dp)) },
                                        onClick = {
                                            onAddSection(secTitle)
                                            showAddSectionMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

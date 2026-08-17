package com.example.ui.panels

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
    layerName: String = "World",
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
    var searchQuery by remember { mutableStateOf("") }
    var isCompactMode by remember { mutableStateOf(false) }

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
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "لم يتم تحديد كائن",
                    color = TextPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "انقر على أي كائن في المشهد لعرض وتعديل خصائصه",
                    color = TextMuted,
                    fontSize = 10.5.sp,
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
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .border(0.6.dp, StudioBorder, RoundedCornerShape(6.dp))
                        .padding(6.dp)
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
                            val emoji = when {
                                selectedObject.name.contains("player", true) -> "👤"
                                selectedObject.name.contains("enemy", true) -> "👹"
                                selectedObject.name.contains("tree", true) -> "🌳"
                                selectedObject.name.contains("camera", true) -> "📷"
                                selectedObject.name.contains("light", true) -> "💡"
                                selectedObject.name.contains("coin", true) -> "🪙"
                                else -> "📦"
                            }

                            Text(emoji, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(5.dp))

                            if (isEditingName) {
                                OutlinedTextField(
                                    value = tempName,
                                    onValueChange = { tempName = it },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).height(36.dp),
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
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Save", tint = StudioGreen, modifier = Modifier.size(15.dp))
                                }
                            } else {
                                Text(
                                    text = selectedObject.name,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        tempName = selectedObject.name
                                        isEditingName = true
                                    },
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Rename", tint = TextMuted, modifier = Modifier.size(12.dp))
                                }
                            }
                        }

                        // Visibility & Lock Quick Toggles
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onToggleObjectVisibility,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (selectedObject.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Visibility",
                                    tint = if (selectedObject.isVisible) TextSecondary else StudioRed,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            IconButton(
                                onClick = onToggleObjectLock,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (selectedObject.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = "Lock",
                                    tint = if (selectedObject.isLocked) StudioYellow else TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Clean Layer Info & Compact Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Layers, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = layerName.ifBlank { "World" },
                                color = StudioPurpleLight,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Compact Mode Toggle
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isCompactMode) StudioPurpleDark else Color.Transparent)
                                .clickable { isCompactMode = !isCompactMode }
                                .padding(horizontal = 4.dp, vertical = 1.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isCompactMode) Icons.Default.ViewAgenda else Icons.Default.ViewCompact,
                                contentDescription = null,
                                tint = if (isCompactMode) StudioPurpleLight else TextMuted,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Compact",
                                color = if (isCompactMode) StudioPurpleLight else TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Property Search Bar (Requirement 18)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("🔍 بحث في الخصائص...", color = TextMuted, fontSize = 10.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                        .height(36.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioPurpleLight,
                        unfocusedBorderColor = StudioBorder,
                        focusedContainerColor = EngineCardBg,
                        unfocusedContainerColor = EngineCardBg
                    )
                )

                // Scrollable Dynamic Cards List
                val filteredSections = remember(selectedObject.sections, searchQuery) {
                    if (searchQuery.isBlank()) {
                        selectedObject.sections
                    } else {
                        selectedObject.sections.filter { sec ->
                            sec.name.contains(searchQuery, ignoreCase = true) ||
                                    sec.properties.any { it.name.contains(searchQuery, ignoreCase = true) }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredSections, key = { it.id }) { section ->
                        val shouldAutoExpand = searchQuery.isNotBlank() || section.name.equals("Transform", true)
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
                            isInitiallyExpanded = shouldAutoExpand,
                            isCompactMode = isCompactMode,
                            onOpenTexturePreview = onOpenTexturePreview
                        )
                    }

                    // Add Section Button Card at Bottom
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Button(
                                onClick = { showAddSectionMenu = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StudioPurpleDark
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                                    .border(0.8.dp, StudioPurpleLight.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ إضافة قسم (Section)", color = TextPrimary, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
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
                                        text = { Text(secTitle, color = TextPrimary, fontSize = 11.5.sp) },
                                        leadingIcon = { Icon(secIcon, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(15.dp)) },
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


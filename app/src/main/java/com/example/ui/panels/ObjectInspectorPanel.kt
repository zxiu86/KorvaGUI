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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.interfaces.IObject
import com.example.engine.interfaces.ISection
import com.example.engine.interfaces.PropertyValue
import com.example.ui.components.DynamicSectionCard
import com.example.ui.components.KorvaDropdownMenu
import com.example.ui.components.KorvaDropdownMenuItem
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
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(StudioPurpleDark.copy(alpha = 0.5f))
                        .border(1.dp, StudioBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = KorvaPurpleLight,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "لم يتم تحديد أي كائن",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "انقر على أي كائن من المشهد أو شجرة الكائنات لتعديل خصائصه ومكوناته",
                    color = TextMuted,
                    fontSize = 9.5.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 14.sp
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
                                selectedObject.name.contains("tree", true) || selectedObject.name.contains("ground", true) -> "🧱"
                                selectedObject.name.contains("camera", true) -> "📷"
                                selectedObject.name.contains("light", true) -> "💡"
                                selectedObject.name.contains("coin", true) -> "🪙"
                                selectedObject.name.contains("particle", true) -> "✨"
                                else -> "📦"
                            }

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(StudioPurpleDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(6.dp))

                            if (isEditingName) {
                                OutlinedTextField(
                                    value = tempName,
                                    onValueChange = { tempName = it },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = KorvaPurpleLight,
                                        unfocusedBorderColor = StudioBorder,
                                        focusedContainerColor = EngineCardBg,
                                        unfocusedContainerColor = EngineCardBg,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        if (tempName.isNotBlank()) {
                                            onRenameObject(tempName.trim())
                                        }
                                        isEditingName = false
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "حفظ",
                                        tint = KorvaGreen,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = selectedObject.name,
                                    color = TextPrimary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        tempName = selectedObject.name
                                        isEditingName = true
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "إعادة تسمية",
                                        tint = TextMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        // Visibility & Lock Quick Toggles
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            IconButton(
                                onClick = onToggleObjectVisibility,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (selectedObject.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "الرؤية",
                                    tint = if (selectedObject.isVisible) KorvaGreen else TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            IconButton(
                                onClick = onToggleObjectLock,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (selectedObject.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = "القفل",
                                    tint = if (selectedObject.isLocked) StudioYellow else TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Layer Info & Compact Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(StudioPurpleDark.copy(alpha = 0.5f))
                                .border(0.6.dp, StudioBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = KorvaPurpleLight,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "الطبقة: ${layerName.ifBlank { "World" }}",
                                    color = KorvaPurpleLight,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Compact Mode Toggle
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isCompactMode) StudioPurpleDark else EngineCardBg)
                                .border(0.6.dp, if (isCompactMode) KorvaPurpleLight else StudioBorder, RoundedCornerShape(4.dp))
                                .clickable { isCompactMode = !isCompactMode }
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isCompactMode) Icons.Default.ViewAgenda else Icons.Default.ViewCompact,
                                contentDescription = null,
                                tint = if (isCompactMode) Color.White else TextSecondary,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "عرض مضغوط",
                                color = if (isCompactMode) Color.White else TextSecondary,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Property Search Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("بحث في الخصائص والمكونات...", color = TextMuted, fontSize = 9.5.sp) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = if (searchQuery.isNotBlank()) KorvaPurpleLight else TextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "مسح",
                                        tint = TextMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KorvaPurpleLight,
                            unfocusedBorderColor = StudioBorder,
                            focusedContainerColor = EngineCardBg,
                            unfocusedContainerColor = EngineCardBg,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

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
                                    .height(36.dp)
                                    .border(0.8.dp, KorvaPurpleLight.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .testTag("add_component_button"),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = KorvaPurpleLight,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "+ إضافة مكون (Component / Section)",
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            KorvaDropdownMenu(
                                expanded = showAddSectionMenu,
                                onDismissRequest = { showAddSectionMenu = false }
                            ) {
                                listOf(
                                    Triple("Sprite", Icons.Default.Image, KorvaPurpleLight),
                                    Triple("Physics", Icons.Default.Science, StudioYellow),
                                    Triple("Animation", Icons.Default.Movie, KorvaBlue),
                                    Triple("Logic (Brain)", Icons.Default.Psychology, KorvaGreen),
                                    Triple("Light 2D", Icons.Default.Lightbulb, StudioYellow),
                                    Triple("Camera 2D", Icons.Default.Videocam, KorvaBlue),
                                    Triple("Particles 2D", Icons.Default.AutoAwesome, KorvaPurpleLight),
                                    Triple("Audio Source", Icons.Default.VolumeUp, KorvaBlue),
                                    Triple("UI Element", Icons.Default.DashboardCustomize, KorvaGreen)
                                ).forEach { (secTitle, secIcon, iconTint) ->
                                    KorvaDropdownMenuItem(
                                        text = secTitle,
                                        icon = secIcon,
                                        iconTint = iconTint,
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

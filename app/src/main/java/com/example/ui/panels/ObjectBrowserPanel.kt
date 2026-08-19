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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.interfaces.ILayer
import com.example.engine.interfaces.IObject
import com.example.ui.components.KorvaDialog
import com.example.ui.components.KorvaDropdownMenu
import com.example.ui.components.KorvaDropdownMenuItem
import com.example.ui.components.KorvaOutlinedButton
import com.example.ui.components.KorvaPrimaryButton
import com.example.ui.theme.*

@Composable
fun ObjectBrowserPanel(
    layers: List<ILayer>,
    selectedObjectId: String?,
    onSelectObject: (objectId: String) -> Unit,
    onCreateObject: (name: String, layerId: String) -> Unit,
    onDuplicateObject: (objectId: String) -> Unit,
    onDeleteObject: (objectId: String) -> Unit,
    onToggleObjectVisibility: (objectId: String) -> Unit,
    onToggleObjectLock: (objectId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newObjectName by remember { mutableStateOf("NewNode_01") }
    var selectedTargetLayerId by remember { mutableStateOf(layers.firstOrNull()?.id ?: "") }
    var selectedObjectTypePreset by remember { mutableStateOf("Sprite 2D") }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(EngineSurface)
            .border(width = 0.8.dp, color = StudioBorder)
    ) {
        // Top Header with Add Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(EngineBackground)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(StudioPurpleDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = null,
                        tint = KorvaPurpleLight,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "شجرة الكائنات (Hierarchy)",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Quick Add Object Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(StudioPurple)
                    .clickable {
                        selectedTargetLayerId = layers.firstOrNull()?.id ?: ""
                        showCreateDialog = true
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("add_object_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "إضافة كائن",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "كائن +",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Search Field with Clear Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث في الكائنات...", color = TextMuted, fontSize = 10.sp) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = if (searchQuery.isNotBlank()) KorvaPurpleLight else TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(22.dp)
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
                    .height(38.dp),
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

        // Hierarchical Object List by Layers
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            layers.forEach { layer ->
                val filteredObjects = layer.objects.filter {
                    searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
                }

                if (filteredObjects.isNotEmpty() || searchQuery.isBlank()) {
                    // Layer Group Header
                    item(key = "layer_header_${layer.id}") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(StudioPurpleDark.copy(alpha = 0.4f))
                                .border(0.6.dp, StudioBorder.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = KorvaPurpleLight,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = layer.name,
                                    color = KorvaPurpleLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${filteredObjects.size} كائن",
                                color = TextMuted,
                                fontSize = 8.5.sp
                            )
                        }
                    }

                    // Object Items inside Layer
                    items(filteredObjects, key = { it.id }) { obj ->
                        val isSelected = selectedObjectId == obj.id
                        var showMenu by remember { mutableStateOf(false) }

                        val (objIcon, badgeBg, badgeTint) = when {
                            obj.name.contains("player", true) -> Triple("👤", KorvaBlue.copy(alpha = 0.2f), KorvaBlue)
                            obj.name.contains("enemy", true) -> Triple("👹", KorvaRed.copy(alpha = 0.2f), KorvaRed)
                            obj.name.contains("tree", true) || obj.name.contains("ground", true) || obj.name.contains("platform", true) -> Triple("🧱", KorvaGreen.copy(alpha = 0.2f), KorvaGreen)
                            obj.name.contains("camera", true) -> Triple("📷", StudioYellow.copy(alpha = 0.2f), StudioYellow)
                            obj.name.contains("light", true) || obj.name.contains("torch", true) -> Triple("💡", StudioYellow.copy(alpha = 0.2f), StudioYellow)
                            obj.name.contains("particle", true) -> Triple("✨", KorvaPurpleLight.copy(alpha = 0.2f), KorvaPurpleLight)
                            obj.name.contains("coin", true) -> Triple("🪙", StudioYellow.copy(alpha = 0.2f), StudioYellow)
                            obj.name.contains("ui", true) || obj.name.contains("health", true) -> Triple("📊", KorvaBlue.copy(alpha = 0.2f), KorvaBlue)
                            else -> Triple("📦", EngineBackground, TextSecondary)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 34.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) Brush.horizontalGradient(
                                        listOf(StudioPurpleDark, StudioPurple.copy(alpha = 0.6f))
                                    ) else Brush.horizontalGradient(
                                        listOf(EngineCardBg, EngineCardBg)
                                    )
                                )
                                .border(
                                    width = if (isSelected) 1.dp else 0.6.dp,
                                    color = if (isSelected) KorvaPurpleLight else StudioBorder,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { onSelectObject(obj.id) }
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left section: Visibility toggle + Icon Badge + Name
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Visibility Toggle Button (Generous touch target)
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .clickable { onToggleObjectVisibility(obj.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (obj.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "الرؤية",
                                        tint = if (obj.isVisible) KorvaGreen else TextMuted,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(3.dp))

                                // Category Emoji Badge
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(badgeBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(objIcon, fontSize = 10.sp)
                                }

                                Spacer(modifier = Modifier.width(5.dp))

                                Text(
                                    text = obj.name,
                                    color = if (isSelected) Color.White else TextPrimary,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Right section: Lock state & Context Menu
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (obj.isLocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "مقفل",
                                        tint = StudioYellow,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                }

                                Box {
                                    IconButton(
                                        onClick = { showMenu = true },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "خيارات الكائن",
                                            tint = if (isSelected) Color.White else TextSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    KorvaDropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false }
                                    ) {
                                        KorvaDropdownMenuItem(
                                            text = "استنساخ (Duplicate)",
                                            icon = Icons.Default.ContentCopy,
                                            iconTint = KorvaPurpleLight,
                                            onClick = {
                                                onDuplicateObject(obj.id)
                                                showMenu = false
                                            }
                                        )
                                        KorvaDropdownMenuItem(
                                            text = if (obj.isLocked) "إلغاء القفل (Unlock)" else "قفل الكائن (Lock)",
                                            icon = if (obj.isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                            iconTint = StudioYellow,
                                            onClick = {
                                                onToggleObjectLock(obj.id)
                                                showMenu = false
                                            }
                                        )
                                        KorvaDropdownMenuItem(
                                            text = if (obj.isVisible) "إخفاء الكائن (Hide)" else "إظهار الكائن (Show)",
                                            icon = if (obj.isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            iconTint = KorvaBlue,
                                            onClick = {
                                                onToggleObjectVisibility(obj.id)
                                                showMenu = false
                                            }
                                        )
                                        KorvaDropdownMenuItem(
                                            text = "حذف الكائن (Delete)",
                                            icon = Icons.Default.Delete,
                                            iconTint = KorvaRed,
                                            isDanger = true,
                                            onClick = {
                                                onDeleteObject(obj.id)
                                                showMenu = false
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

    // Modern Create New Object Dialog
    if (showCreateDialog) {
        KorvaDialog(
            onDismissRequest = { showCreateDialog = false },
            title = "إضافة كائن جديد للمشهد",
            subtitle = "تحديد اسم الكائن والنوع والطبقة المستهدفة",
            icon = Icons.Default.AddBox,
            maxWidth = 420.dp,
            buttons = {
                KorvaOutlinedButton(
                    text = "إلغاء",
                    onClick = { showCreateDialog = false },
                    modifier = Modifier.weight(1f)
                )

                KorvaPrimaryButton(
                    text = "إنشاء الكائن",
                    onClick = {
                        if (newObjectName.isNotBlank()) {
                            onCreateObject(newObjectName.trim(), selectedTargetLayerId)
                            newObjectName = "NewNode_${System.currentTimeMillis() % 1000}"
                            showCreateDialog = false
                        }
                    },
                    icon = Icons.Default.Check,
                    modifier = Modifier.weight(1.3f)
                )
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Object Type Presets
                Text(
                    text = "نوع الكائن (Preset):",
                    color = TextSecondary,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )

                val presets = listOf(
                    "Sprite 2D" to "Player_Hero",
                    "Enemy" to "Monster_01",
                    "Ground/Tile" to "Platform_Ground",
                    "Light 2D" to "Light_Torch",
                    "Particles" to "Particle_Spark",
                    "Camera 2D" to "Main_Camera"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presets.take(3).forEach { (preset, defaultName) ->
                        val isChosen = selectedObjectTypePreset == preset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isChosen) StudioPurple else EngineCardBg)
                                .border(0.6.dp, if (isChosen) KorvaPurpleLight else StudioBorder, RoundedCornerShape(6.dp))
                                .clickable {
                                    selectedObjectTypePreset = preset
                                    newObjectName = defaultName
                                }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset,
                                color = if (isChosen) Color.White else TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    presets.drop(3).forEach { (preset, defaultName) ->
                        val isChosen = selectedObjectTypePreset == preset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isChosen) StudioPurple else EngineCardBg)
                                .border(0.6.dp, if (isChosen) KorvaPurpleLight else StudioBorder, RoundedCornerShape(6.dp))
                                .clickable {
                                    selectedObjectTypePreset = preset
                                    newObjectName = defaultName
                                }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset,
                                color = if (isChosen) Color.White else TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Name Input
                Text(
                    text = "اسم الكائن (Object Name) *",
                    color = TextPrimary,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = newObjectName,
                    onValueChange = { newObjectName = it },
                    placeholder = { Text("أدخل اسم الكائن...", color = TextMuted, fontSize = 11.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(6.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KorvaPurpleLight,
                        unfocusedBorderColor = StudioBorder,
                        focusedContainerColor = EngineCardBg,
                        unfocusedContainerColor = EngineCardBg,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Target Layer Selection
                Text(
                    text = "الطبقة الحاضنة (Layer):",
                    color = TextSecondary,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    layers.forEach { l ->
                        val isChosen = selectedTargetLayerId == l.id
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isChosen) StudioPurpleDark else EngineCardBg)
                                .border(0.8.dp, if (isChosen) KorvaPurpleLight else StudioBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedTargetLayerId = l.id }
                                .padding(horizontal = 6.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = l.name,
                                color = if (isChosen) Color.White else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

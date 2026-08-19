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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    var newObjectName by remember { mutableStateOf("") }
    var selectedTargetLayerId by remember { mutableStateOf(layers.firstOrNull()?.id ?: "") }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(EngineSurface)
            .border(width = 0.8.dp, color = StudioBorder)
    ) {
        // Top Header & Create Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(EngineBackground)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = null,
                    tint = StudioPurpleLight,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "الكائنات (Objects)",
                    color = TextPrimary,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = {
                    selectedTargetLayerId = layers.firstOrNull()?.id ?: ""
                    showCreateDialog = true
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Object",
                    tint = StudioPurpleLight,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث عن كائن...", color = TextMuted, fontSize = 10.sp) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp)) },
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

        // Compact Hierarchical Object List (Requirement 9: single row per object)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            layers.forEach { layer ->
                val filteredObjects = layer.objects.filter {
                    searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
                }

                if (filteredObjects.isNotEmpty() || searchQuery.isBlank()) {
                    item(key = "layer_header_${layer.id}") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(StudioPurpleDark.copy(alpha = 0.35f))
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = StudioPurpleLight,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(layer.name, color = StudioPurpleLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("(${filteredObjects.size})", color = TextMuted, fontSize = 9.sp)
                        }
                    }

                    items(filteredObjects, key = { it.id }) { obj ->
                        val isSelected = selectedObjectId == obj.id
                        var showMenu by remember { mutableStateOf(false) }

                        val objIcon = when {
                            obj.name.contains("player", true) -> "👤"
                            obj.name.contains("enemy", true) -> "👹"
                            obj.name.contains("tree", true) -> "🌳"
                            obj.name.contains("ground", true) || obj.name.contains("platform", true) -> "🧱"
                            obj.name.contains("camera", true) -> "📷"
                            obj.name.contains("light", true) || obj.name.contains("torch", true) -> "💡"
                            obj.name.contains("particle", true) -> "✨"
                            obj.name.contains("coin", true) -> "🪙"
                            obj.name.contains("ui", true) || obj.name.contains("health", true) -> "📊"
                            else -> "📦"
                        }

                        // Compact Single Row: 👁 SkyGradient  ⋮
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) StudioPurpleDark else EngineCardBg)
                                .border(
                                    0.6.dp,
                                    if (isSelected) StudioPurpleLight else StudioBorder,
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable { onSelectObject(obj.id) }
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                IconButton(
                                    onClick = { onToggleObjectVisibility(obj.id) },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = if (obj.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Visibility",
                                        tint = if (obj.isVisible) TextMuted else StudioRed,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }

                                Text(objIcon, fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = obj.name,
                                    color = if (isSelected) Color.White else TextPrimary,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }

                            // Clean single ⋮ Menu
                            Box {
                                IconButton(
                                    onClick = { showMenu = true },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Actions",
                                        tint = if (isSelected) StudioPurpleLight else TextMuted,
                                        modifier = Modifier.size(13.dp)
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
                                        text = if (obj.isLocked) "إلغاء القفل (Unlock)" else "قفل (Lock)",
                                        icon = if (obj.isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                        iconTint = StudioYellow,
                                        onClick = {
                                            onToggleObjectLock(obj.id)
                                            showMenu = false
                                        }
                                    )
                                    KorvaDropdownMenuItem(
                                        text = "حذف (Delete)",
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

    // New Object Dialog
    if (showCreateDialog) {
        KorvaDialog(
            onDismissRequest = { showCreateDialog = false },
            title = "إنشاء كائن جديد (New Object)",
            subtitle = "إضافة كائن إلى مشهد اللعبة وتعيين طبقته",
            icon = Icons.Default.AddBox,
            maxWidth = 380.dp,
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
                            newObjectName = ""
                            showCreateDialog = false
                        }
                    },
                    icon = Icons.Default.Check,
                    modifier = Modifier.weight(1.2f)
                )
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = newObjectName,
                    onValueChange = { newObjectName = it },
                    placeholder = { Text("اسم الكائن (مثال: Coin_Bonus)", color = TextMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioPurpleLight,
                        unfocusedBorderColor = StudioBorder,
                        focusedContainerColor = EngineCardBg,
                        unfocusedContainerColor = EngineCardBg,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("اختر الطبقة:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    layers.forEach { l ->
                        val isChosen = selectedTargetLayerId == l.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isChosen) StudioPurple else EngineCardBg)
                                .border(0.8.dp, if (isChosen) StudioPurpleLight else StudioBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedTargetLayerId = l.id }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(l.name, color = if (isChosen) Color.White else TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

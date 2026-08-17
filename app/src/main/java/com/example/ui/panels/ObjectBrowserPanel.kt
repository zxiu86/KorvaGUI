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
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = null,
                    tint = StudioPurpleLight,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "مستعرض الكائنات (Objects)",
                    color = TextPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = {
                    selectedTargetLayerId = layers.firstOrNull()?.id ?: ""
                    showCreateDialog = true
                },
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Object",
                    tint = StudioPurpleLight,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث عن كائن...", color = TextMuted, fontSize = 11.sp) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(15.dp)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .height(40.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = StudioPurpleLight,
                unfocusedBorderColor = StudioBorder,
                focusedContainerColor = EngineCardBg,
                unfocusedContainerColor = EngineCardBg
            )
        )

        // Grouped Objects by Layer
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                .background(StudioPurpleDark.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Layers, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(layer.name, color = StudioPurpleLight, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("(${filteredObjects.size})", color = TextMuted, fontSize = 9.5.sp)
                        }
                    }

                    items(filteredObjects, key = { it.id }) { obj ->
                        val isSelected = selectedObjectId == obj.id
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

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) StudioPurpleDark else EngineCardBg)
                                .border(
                                    0.8.dp,
                                    if (isSelected) StudioPurpleLight else StudioBorder,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { onSelectObject(obj.id) }
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(objIcon, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = obj.name,
                                    color = if (isSelected) Color.White else TextPrimary,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1
                                )
                            }

                            // Quick Action Icons
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Duplicate
                                IconButton(
                                    onClick = { onDuplicateObject(obj.id) },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = TextMuted, modifier = Modifier.size(11.dp))
                                }

                                // Visibility
                                IconButton(
                                    onClick = { onToggleObjectVisibility(obj.id) },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = if (obj.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Visibility",
                                        tint = if (obj.isVisible) TextSecondary else StudioRed,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }

                                // Delete
                                IconButton(
                                    onClick = { onDeleteObject(obj.id) },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StudioRed.copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
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
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("إنشاء كائن جديد", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newObjectName,
                        onValueChange = { newObjectName = it },
                        placeholder = { Text("اسم الكائن (مثال: Coin_Bonus)", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioPurpleLight,
                            unfocusedBorderColor = StudioBorder
                        )
                    )

                    Text("اختر الطبقة:", color = TextSecondary, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        layers.forEach { l ->
                            val isChosen = selectedTargetLayerId == l.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isChosen) StudioPurple else EngineCardBg)
                                    .border(0.6.dp, if (isChosen) StudioPurpleLight else StudioBorder, RoundedCornerShape(4.dp))
                                    .clickable { selectedTargetLayerId = l.id }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(l.name, color = if (isChosen) Color.White else TextMuted, fontSize = 9.5.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newObjectName.isNotBlank()) {
                            onCreateObject(newObjectName.trim(), selectedTargetLayerId)
                            newObjectName = ""
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPurple)
                ) {
                    Text("إنشاء", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("إلغاء", color = TextMuted)
                }
            },
            containerColor = EngineSurface
        )
    }
}

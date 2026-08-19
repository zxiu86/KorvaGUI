package com.example.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.ui.components.KorvaDialog
import com.example.ui.components.KorvaOutlinedButton
import com.example.ui.components.KorvaPrimaryButton
import com.example.ui.theme.*

@Composable
fun LayerManagerPanel(
    layers: List<ILayer>,
    selectedLayerId: String?,
    onSelectLayer: (layerId: String) -> Unit,
    onCreateLayer: (name: String) -> Unit,
    onDeleteLayer: (layerId: String) -> Unit,
    onToggleLayerVisibility: (layerId: String) -> Unit,
    onToggleLayerLock: (layerId: String) -> Unit,
    onMoveLayerUp: (layerId: String) -> Unit,
    onMoveLayerDown: (layerId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNewLayerDialog by remember { mutableStateOf(false) }
    var newLayerName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(EngineSurface)
            .border(width = 0.8.dp, color = StudioBorder)
    ) {
        // Panel Header
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
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = StudioPurpleLight,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "مدير الطبقات (Layers)",
                    color = TextPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = { showNewLayerDialog = true },
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Layer",
                    tint = StudioPurpleLight,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Layers List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(layers, key = { _, layer -> layer.id }) { index, layer ->
                val isSelected = selectedLayerId == layer.id

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) StudioPurpleDark else EngineCardBg)
                        .border(
                            0.8.dp,
                            if (isSelected) StudioPurpleLight else StudioBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectLayer(layer.id) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = layer.name,
                            color = if (isSelected) Color.White else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${layer.objects.size})",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }

                    // Action Controls
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Move Up / Down
                        if (index > 0) {
                            IconButton(
                                onClick = { onMoveLayerUp(layer.id) },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = TextSecondary, modifier = Modifier.size(14.dp))
                            }
                        }
                        if (index < layers.size - 1) {
                            IconButton(
                                onClick = { onMoveLayerDown(layer.id) },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = TextSecondary, modifier = Modifier.size(14.dp))
                            }
                        }

                        // Visibility Toggle
                        IconButton(
                            onClick = { onToggleLayerVisibility(layer.id) },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Visibility",
                                tint = if (layer.isVisible) TextSecondary else StudioRed,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        // Lock Toggle
                        IconButton(
                            onClick = { onToggleLayerLock(layer.id) },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Lock",
                                tint = if (layer.isLocked) StudioYellow else TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        // Delete Layer (if more than 1)
                        if (layers.size > 1) {
                            IconButton(
                                onClick = { onDeleteLayer(layer.id) },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = StudioRed.copy(alpha = 0.7f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // New Layer Dialog
    if (showNewLayerDialog) {
        KorvaDialog(
            onDismissRequest = { showNewLayerDialog = false },
            title = "إنشاء طبقة جديدة (New Layer)",
            subtitle = "إضافة طبقة ترتيب لمشهد اللعبة",
            icon = Icons.Default.Layers,
            maxWidth = 360.dp,
            buttons = {
                KorvaOutlinedButton(
                    text = "إلغاء",
                    onClick = { showNewLayerDialog = false },
                    modifier = Modifier.weight(1f)
                )

                KorvaPrimaryButton(
                    text = "إنشاء",
                    onClick = {
                        if (newLayerName.isNotBlank()) {
                            onCreateLayer(newLayerName.trim())
                            newLayerName = ""
                            showNewLayerDialog = false
                        }
                    },
                    icon = Icons.Default.Check,
                    modifier = Modifier.weight(1.2f)
                )
            }
        ) {
            OutlinedTextField(
                value = newLayerName,
                onValueChange = { newLayerName = it },
                placeholder = { Text("اسم الطبقة (مثال: Foreground)", color = TextMuted) },
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
        }
    }
}

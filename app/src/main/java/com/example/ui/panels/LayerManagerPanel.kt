package com.example.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
    var newLayerName by remember { mutableStateOf("Foreground_01") }

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
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = KorvaPurpleLight,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "مدير الطبقات (Layers)",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(StudioPurple)
                    .clickable { showNewLayerDialog = true }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("add_layer_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "إضافة طبقة",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "طبقة +",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Layers List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(layers, key = { _, layer -> layer.id }) { index, layer ->
                val isSelected = selectedLayerId == layer.id

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 38.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isSelected) Brush.horizontalGradient(
                                listOf(StudioPurpleDark, StudioPurple.copy(alpha = 0.5f))
                            ) else Brush.horizontalGradient(
                                listOf(EngineCardBg, EngineCardBg)
                            )
                        )
                        .border(
                            width = if (isSelected) 1.dp else 0.6.dp,
                            color = if (isSelected) KorvaPurpleLight else StudioBorder,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onSelectLayer(layer.id) }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left info: Order index, Name, Object count
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) KorvaPurple else EngineBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = if (isSelected) Color.White else TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column {
                            Text(
                                text = layer.name,
                                color = if (isSelected) Color.White else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${layer.objects.size} كائنات",
                                color = if (isSelected) KorvaPurpleLight else TextMuted,
                                fontSize = 8.5.sp
                            )
                        }
                    }

                    // Action Controls with comfortable touch targets
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Move Up
                        IconButton(
                            onClick = { onMoveLayerUp(layer.id) },
                            enabled = index > 0,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "أعلى",
                                tint = if (index > 0) (if (isSelected) Color.White else TextSecondary) else TextMuted.copy(alpha = 0.3f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Move Down
                        IconButton(
                            onClick = { onMoveLayerDown(layer.id) },
                            enabled = index < layers.size - 1,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "أسفل",
                                tint = if (index < layers.size - 1) (if (isSelected) Color.White else TextSecondary) else TextMuted.copy(alpha = 0.3f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Visibility Toggle
                        IconButton(
                            onClick = { onToggleLayerVisibility(layer.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "الرؤية",
                                tint = if (layer.isVisible) KorvaGreen else TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        // Lock Toggle
                        IconButton(
                            onClick = { onToggleLayerLock(layer.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "القفل",
                                tint = if (layer.isLocked) StudioYellow else TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        // Delete Layer (if more than 1)
                        if (layers.size > 1) {
                            IconButton(
                                onClick = { onDeleteLayer(layer.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف الطبقة",
                                    tint = KorvaRed.copy(alpha = 0.8f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modern Create Layer Dialog
    if (showNewLayerDialog) {
        KorvaDialog(
            onDismissRequest = { showNewLayerDialog = false },
            title = "إنشاء طبقة جديدة (New Layer)",
            subtitle = "إضافة مستوى ترتيب جديد في المشهد",
            icon = Icons.Default.Layers,
            maxWidth = 380.dp,
            buttons = {
                KorvaOutlinedButton(
                    text = "إلغاء",
                    onClick = { showNewLayerDialog = false },
                    modifier = Modifier.weight(1f)
                )

                KorvaPrimaryButton(
                    text = "إنشاء الطبقة",
                    onClick = {
                        if (newLayerName.isNotBlank()) {
                            onCreateLayer(newLayerName.trim())
                            newLayerName = "Layer_${System.currentTimeMillis() % 1000}"
                            showNewLayerDialog = false
                        }
                    },
                    icon = Icons.Default.Check,
                    modifier = Modifier.weight(1.2f)
                )
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "اقتراحات سريعة:",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Background", "Characters", "Foreground", "UI_HUD").forEach { preset ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(EngineBackground)
                                .border(0.6.dp, StudioBorder, RoundedCornerShape(4.dp))
                                .clickable { newLayerName = preset }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset,
                                color = TextSecondary,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = "اسم الطبقة *",
                    color = TextPrimary,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = newLayerName,
                    onValueChange = { newLayerName = it },
                    placeholder = { Text("أدخل اسم الطبقة...", color = TextMuted, fontSize = 11.sp) },
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
            }
        }
    }
}

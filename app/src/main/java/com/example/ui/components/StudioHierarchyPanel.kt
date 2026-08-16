package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Token
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NodeType
import com.example.model.SceneNode
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.EngineSurface
import com.example.ui.theme.StudioBlue
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioOrange
import com.example.ui.theme.StudioPink
import com.example.ui.theme.StudioPurple
import com.example.ui.theme.StudioPurpleBg
import com.example.ui.theme.StudioPurpleBorder
import com.example.ui.theme.StudioPurpleDark
import com.example.ui.theme.StudioPurpleGlass
import com.example.ui.theme.StudioPurpleLight
import com.example.ui.theme.StudioRed
import com.example.ui.theme.StudioYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class StudioLayer(
    val id: String,
    val name: String,
    val color: Color,
    var isVisible: Boolean = true,
    var isLocked: Boolean = false
)

@Composable
fun StudioHierarchyPanel(
    sceneNodes: List<SceneNode>,
    selectedNodeId: String?,
    onSelectNode: (String) -> Unit,
    onAddNode: (name: String, type: NodeType) -> Unit,
    onDeleteNode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddMenu by remember { mutableStateOf(false) }
    var selectedSubtab by remember { mutableStateOf(0) }

    val layers = remember {
        mutableStateMapOf(
            "ui" to StudioLayer("ui", "UI Layer", StudioBlue, isVisible = true),
            "objects" to StudioLayer("objects", "Objects", StudioPurple, isVisible = true),
            "collisions" to StudioLayer("collisions", "Collisions", StudioRed, isVisible = true),
            "tilemap" to StudioLayer("tilemap", "Tilemap", StudioGreen, isVisible = true),
            "background" to StudioLayer("background", "Background", Color(0xFF94A3B8), isVisible = true)
        )
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(200.dp)
            .background(EngineSurface)
            .border(0.8.dp, StudioBorder)
            .padding(8.dp)
    ) {
        // ========================================================
        // 1. OBJECTS Header & Add Button
        // ========================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "OBJECTS",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Box {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(EngineCardBg)
                        .border(0.6.dp, StudioBorder, RoundedCornerShape(4.dp))
                        .clickable { showAddMenu = true }
                        .testTag("add_object_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Object",
                        tint = TextPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                DropdownMenu(
                    expanded = showAddMenu,
                    onDismissRequest = { showAddMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Player Character (لاعب)") },
                        onClick = {
                            onAddNode("Player", NodeType.PLAYER)
                            showAddMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Enemy Creature (عدو)") },
                        onClick = {
                            onAddNode("Enemy", NodeType.ENEMY)
                            showAddMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Tree / Prop (شجرة / بيئة)") },
                        onClick = {
                            onAddNode("Tree", NodeType.SPRITE_OBJECT)
                            showAddMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Coin / Collectible (عملة)") },
                        onClick = {
                            onAddNode("Coin", NodeType.PARTICLE_SYSTEM)
                            showAddMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Camera (كاميرا)") },
                        onClick = {
                            onAddNode("Camera", NodeType.CAMERA)
                            showAddMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Light 2D (إضاءة)") },
                        onClick = {
                            onAddNode("Light", NodeType.LIGHT)
                            showAddMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ========================================================
        // 2. Objects List
        // ========================================================
        LazyColumn(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            items(sceneNodes, key = { it.id }) { node ->
                val isSelected = node.id == selectedNodeId
                val (icon, iconTint) = when (node.name.lowercase()) {
                    "player" -> Icons.Default.Person to StudioPurpleLight
                    "enemy" -> Icons.Default.Warning to StudioRed
                    "tree" -> Icons.Default.Park to StudioGreen
                    "coin" -> Icons.Default.Token to StudioYellow
                    "camera" -> Icons.Default.Videocam to StudioBlue
                    "canvas" -> Icons.Default.Widgets to StudioPink
                    "light" -> Icons.Default.WbSunny to StudioOrange
                    else -> when (node.type) {
                        NodeType.PLAYER -> Icons.Default.Person to StudioPurpleLight
                        NodeType.ENEMY -> Icons.Default.Warning to StudioRed
                        NodeType.CAMERA -> Icons.Default.Videocam to StudioBlue
                        NodeType.LIGHT -> Icons.Default.WbSunny to StudioOrange
                        else -> Icons.Default.Widgets to StudioGreen
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isSelected) StudioPurpleBg else Color.Transparent
                        )
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) StudioPurpleBorder else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onSelectNode(node.id) }
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = node.name,
                            color = if (isSelected) Color.White else TextPrimary,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "خيارات",
                        tint = TextMuted,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { /* options */ }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // ========================================================
        // 3. Subtabs Bar (Folder, Cube, Layers, Document)
        // ========================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(EngineCardBg)
                .border(0.8.dp, StudioBorder, RoundedCornerShape(6.dp))
                .padding(2.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                Icons.Default.Folder,
                Icons.Default.ViewInAr,
                Icons.Default.Layers,
                Icons.Default.Description
            ).forEachIndexed { index, icon ->
                val isSelected = selectedSubtab == index
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) StudioPurple.copy(alpha = 0.25f) else Color.Transparent)
                        .clickable { selectedSubtab = index },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) StudioPurpleLight else TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ========================================================
        // 4. LAYERS Header & List
        // ========================================================
        Text(
            text = "LAYERS",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            items(layers.values.toList()) { layer ->
                val isLayerSelected = layer.id == "objects"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isLayerSelected) StudioPurpleBg else Color.Transparent)
                        .border(
                            width = if (isLayerSelected) 0.8.dp else 0.dp,
                            color = if (isLayerSelected) StudioPurpleBorder else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { }
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(layer.color)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = layer.name,
                            color = if (isLayerSelected) Color.White else TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = if (isLayerSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "رؤية الطبقة",
                            tint = if (layer.isVisible) TextSecondary else TextMuted,
                            modifier = Modifier
                                .size(13.dp)
                                .clickable {
                                    layers[layer.id] = layer.copy(isVisible = !layer.isVisible)
                                }
                        )

                        Icon(
                            imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "قفل الطبقة",
                            tint = TextMuted,
                            modifier = Modifier
                                .size(13.dp)
                                .clickable {
                                    layers[layer.id] = layer.copy(isLocked = !layer.isLocked)
                                }
                        )
                    }
                }
            }
        }
    }
}

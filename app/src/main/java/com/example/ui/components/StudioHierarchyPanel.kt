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
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Token
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import com.example.ui.theme.StudioPurpleDark
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
            "ui" to StudioLayer("ui", "UI", StudioBlue, isVisible = true),
            "objects" to StudioLayer("objects", "Objects", StudioPurple, isVisible = true),
            "collisions" to StudioLayer("collisions", "Collisions", StudioRed, isVisible = true),
            "tilemap" to StudioLayer("tilemap", "Tilemap", StudioGreen, isVisible = true),
            "background" to StudioLayer("background", "Background", Color(0xFF94A3B8), isVisible = true)
        )
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(120.dp)
            .background(EngineSurface)
            .border(0.6.dp, StudioBorder)
            .padding(4.dp)
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
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Box {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(EngineCardBg)
                        .border(0.5.dp, StudioBorder, RoundedCornerShape(3.dp))
                        .clickable { showAddMenu = true }
                        .testTag("add_object_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Object",
                        tint = TextPrimary,
                        modifier = Modifier.size(11.dp)
                    )
                }

                DropdownMenu(
                    expanded = showAddMenu,
                    onDismissRequest = { showAddMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Player Character", fontSize = 9.5.sp) },
                        onClick = {
                            onAddNode("Player", NodeType.PLAYER)
                            showAddMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Enemy NPC", fontSize = 9.5.sp) },
                        onClick = {
                            onAddNode("Enemy", NodeType.ENEMY)
                            showAddMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Coin Pickup", fontSize = 9.5.sp) },
                        onClick = {
                            onAddNode("Coin", NodeType.SPRITE_OBJECT)
                            showAddMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Camera 2D", fontSize = 9.5.sp) },
                        onClick = {
                            onAddNode("Main Camera", NodeType.CAMERA)
                            showAddMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Light Source", fontSize = 9.5.sp) },
                        onClick = {
                            onAddNode("Point Light", NodeType.LIGHT)
                            showAddMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ========================================================
        // 2. Sub-tab Icons (Folder, 3D Cube, Layers)
        // ========================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(EngineCardBg)
                .border(0.4.dp, StudioBorder, RoundedCornerShape(3.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                Icons.Default.Folder,
                Icons.Default.ViewInAr,
                Icons.Default.Layers
            ).forEachIndexed { index, icon ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(if (selectedSubtab == index) StudioPurpleDark else Color.Transparent)
                        .clickable { selectedSubtab = index },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selectedSubtab == index) StudioPurpleLight else TextMuted,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ========================================================
        // 3. Scene Objects Hierarchy List
        // ========================================================
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(sceneNodes, key = { it.id }) { node ->
                val isSelected = node.id == selectedNodeId
                val (nodeIcon, iconColor) = getNodeVisuals(node.type)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isSelected) StudioPurpleDark else Color.Transparent)
                        .border(
                            width = if (isSelected) 0.6.dp else 0.dp,
                            color = if (isSelected) StudioPurpleLight else Color.Transparent,
                            shape = RoundedCornerShape(3.dp)
                        )
                        .clickable { onSelectNode(node.id) }
                        .padding(horizontal = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = nodeIcon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(11.dp)
                    )

                    Spacer(modifier = Modifier.width(3.dp))

                    Text(
                        text = node.name,
                        color = if (isSelected) Color.White else TextPrimary,
                        fontSize = 8.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    if (!node.isVisible) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(9.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ========================================================
        // 4. LAYERS Section (Bottom of Left Panel)
        // ========================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(3.dp))
                .background(EngineCardBg)
                .border(0.4.dp, StudioBorder, RoundedCornerShape(3.dp))
                .padding(3.dp)
        ) {
            Text(
                text = "LAYERS",
                color = TextSecondary,
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            layers.values.forEach { layer ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .padding(vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(layer.color)
                        )

                        Spacer(modifier = Modifier.width(3.dp))

                        Text(
                            text = layer.name,
                            color = TextPrimary,
                            fontSize = 7.5.sp,
                            maxLines = 1
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = if (layer.isVisible) TextSecondary else TextMuted,
                            modifier = Modifier
                                .size(9.dp)
                                .clickable {
                                    layers[layer.id] = layer.copy(isVisible = !layer.isVisible)
                                }
                        )

                        Icon(
                            imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = if (layer.isLocked) StudioYellow else TextMuted,
                            modifier = Modifier
                                .size(9.dp)
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

private fun getNodeVisuals(type: NodeType): Pair<ImageVector, Color> {
    return when (type) {
        NodeType.PLAYER -> Icons.Default.Person to StudioPurpleLight
        NodeType.ENEMY -> Icons.Default.Widgets to StudioRed
        NodeType.PLATFORM -> Icons.Default.Description to StudioGreen
        NodeType.SPRITE_OBJECT -> Icons.Default.Token to StudioYellow
        NodeType.CAMERA -> Icons.Default.Videocam to StudioBlue
        NodeType.LIGHT -> Icons.Default.WbSunny to StudioOrange
        NodeType.PARTICLE_SYSTEM -> Icons.Default.Park to StudioPink
        else -> Icons.Default.Token to StudioPurpleLight
    }
}

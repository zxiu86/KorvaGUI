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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
    onToggleNodeVisibility: (String) -> Unit = {},
    onCollapse: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAddMenu by remember { mutableStateOf(false) }
    var selectedSubtab by remember { mutableStateOf(0) } // 0: Tree, 1: Layers
    var searchQuery by remember { mutableStateOf("") }

    val layers = remember {
        mutableStateMapOf(
            "ui" to StudioLayer("ui", "UI", StudioBlue, isVisible = true),
            "objects" to StudioLayer("objects", "Objects", StudioPurple, isVisible = true),
            "collisions" to StudioLayer("collisions", "Collisions", StudioRed, isVisible = true),
            "tilemap" to StudioLayer("tilemap", "Tilemap", StudioGreen, isVisible = true),
            "background" to StudioLayer("background", "Background", Color(0xFF94A3B8), isVisible = true)
        )
    }

    val filteredNodes = remember(sceneNodes, searchQuery) {
        if (searchQuery.isBlank()) sceneNodes
        else sceneNodes.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(125.dp)
            .background(EngineSurface)
            .border(0.6.dp, StudioBorder)
            .padding(4.dp)
    ) {
        // ========================================================
        // 1. OBJECTS Header & Actions (Add + Collapse)
        // ========================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "HIERARCHY",
                    color = TextSecondary,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "(${sceneNodes.size})",
                    color = StudioPurpleLight,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Add Object Button
                Box {
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(EngineCardBg)
                            .border(0.5.dp, StudioBorder, RoundedCornerShape(3.dp))
                            .clickable { showAddMenu = true }
                            .testTag("add_object_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "إضافة عنصر",
                            tint = TextPrimary,
                            modifier = Modifier.size(10.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showAddMenu,
                        onDismissRequest = { showAddMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Player Sprite", fontSize = 9.5.sp) },
                            onClick = {
                                onAddNode("Player Hero", NodeType.PLAYER)
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Platform Tile", fontSize = 9.5.sp) },
                            onClick = {
                                onAddNode("Grass Platform", NodeType.PLATFORM)
                                showAddMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Enemy NPC", fontSize = 9.5.sp) },
                            onClick = {
                                onAddNode("Goblin Enemy", NodeType.ENEMY)
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
                        DropdownMenuItem(
                            text = { Text("Particle Emitter", fontSize = 9.5.sp) },
                            onClick = {
                                onAddNode("Magic Sparks", NodeType.PARTICLE_SYSTEM)
                                showAddMenu = false
                            }
                        )
                    }
                }

                // Collapse Panel Button
                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(EngineCardBg)
                        .border(0.5.dp, StudioBorder, RoundedCornerShape(3.dp))
                        .clickable { onCollapse() }
                        .testTag("collapse_hierarchy_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "إغلاق لوحة العناصر",
                        tint = TextSecondary,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ========================================================
        // 2. Tab Selector (Tree vs Layers)
        // ========================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(EngineCardBg)
                .padding(1.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (selectedSubtab == 0) StudioPurple else Color.Transparent)
                    .clickable { selectedSubtab = 0 },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tree",
                    color = if (selectedSubtab == 0) Color.White else TextSecondary,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (selectedSubtab == 1) StudioPurple else Color.Transparent)
                    .clickable { selectedSubtab = 1 },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Layers",
                    color = if (selectedSubtab == 1) Color.White else TextSecondary,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ========================================================
        // 3. Tab Content
        // ========================================================
        if (selectedSubtab == 0) {
            // Search Input (Compact)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث...", fontSize = 7.5.sp, color = TextMuted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(10.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StudioPurpleLight,
                    unfocusedBorderColor = StudioBorder,
                    focusedContainerColor = EngineCardBg,
                    unfocusedContainerColor = EngineCardBg,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(4.dp)
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Scene Node Tree List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(items = filteredNodes, key = { it.id }) { node ->
                    val isSelected = node.id == selectedNodeId
                    val (icon, tint) = getNodeIconAndColor(node.type)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isSelected) StudioPurpleDark else EngineCardBg)
                            .border(
                                width = if (isSelected) 0.8.dp else 0.4.dp,
                                color = if (isSelected) StudioPurpleLight else StudioBorder,
                                shape = RoundedCornerShape(3.dp)
                            )
                            .clickable { onSelectNode(node.id) }
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = node.name,
                                color = if (isSelected) Color.White else TextPrimary,
                                fontSize = 7.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }

                        // Visibility Toggle
                        Icon(
                            imageVector = if (node.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "إظهار/إخفاء",
                            tint = if (node.isVisible) TextSecondary else TextMuted,
                            modifier = Modifier
                                .size(9.dp)
                                .clickable {
                                    onToggleNodeVisibility(node.id)
                                }
                        )
                    }
                }
            }
        } else {
            // Layers List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(items = layers.values.toList(), key = { it.id }) { layer ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(EngineCardBg)
                            .border(0.4.dp, StudioBorder, RoundedCornerShape(3.dp))
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(layer.color)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = layer.name,
                                color = TextPrimary,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Medium
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
                                modifier = Modifier.size(9.dp)
                            )
                            Icon(
                                imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = if (layer.isLocked) StudioRed else TextMuted,
                                modifier = Modifier.size(9.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getNodeIconAndColor(type: NodeType): Pair<ImageVector, Color> {
    return when (type) {
        NodeType.PLAYER -> Icons.Default.Person to StudioPurpleLight
        NodeType.ENEMY -> Icons.Default.Token to StudioRed
        NodeType.PLATFORM -> Icons.Default.Description to StudioGreen
        NodeType.SPRITE_OBJECT -> Icons.Default.Token to StudioYellow
        NodeType.CAMERA -> Icons.Default.Videocam to StudioBlue
        NodeType.LIGHT -> Icons.Default.WbSunny to StudioOrange
        NodeType.PARTICLE_SYSTEM -> Icons.Default.Park to StudioPink
        else -> Icons.Default.Token to StudioPurpleLight
    }
}

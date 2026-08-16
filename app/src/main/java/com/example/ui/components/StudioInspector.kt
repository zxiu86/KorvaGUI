package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SceneNode
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.EngineSurface
import com.example.ui.theme.StudioBlue
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioOrange
import com.example.ui.theme.StudioPink
import com.example.ui.theme.StudioPurple
import com.example.ui.theme.StudioPurpleBorder
import com.example.ui.theme.StudioPurpleDark
import com.example.ui.theme.StudioPurpleLight
import com.example.ui.theme.StudioRed
import com.example.ui.theme.StudioYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StudioInspector(
    selectedNode: SceneNode?,
    modifier: Modifier = Modifier
) {
    var isPhysicsExpanded by remember { mutableStateOf(true) }
    var isBrainExpanded by remember { mutableStateOf(false) }
    var isSpriteExpanded by remember { mutableStateOf(false) }
    var isAnimationExpanded by remember { mutableStateOf(false) }
    var isAudioExpanded by remember { mutableStateOf(false) }
    var isHealthExpanded by remember { mutableStateOf(false) }

    // Physics Parameters State
    var gravityEnabled by remember { mutableStateOf(true) }
    var gravityX by remember { mutableStateOf("0") }
    var gravityY by remember { mutableStateOf("980") }
    var massValue by remember { mutableStateOf(1.0f) }
    var frictionValue by remember { mutableStateOf(0.2f) }
    var bodyType by remember { mutableStateOf("Dynamic") }
    var showBodyTypeMenu by remember { mutableStateOf(false) }
    var collisionLayers by remember { mutableStateOf(setOf(1, 2)) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(135.dp)
            .background(EngineSurface)
            .border(0.6.dp, StudioBorder)
            .padding(4.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // 1. Brain Card
            item {
                CompactInspectorCard(
                    title = "Brain",
                    subtitle = "AI Agent",
                    icon = Icons.Default.Psychology,
                    iconTint = StudioBlue,
                    isExpanded = isBrainExpanded,
                    onToggle = { isBrainExpanded = !isBrainExpanded }
                ) {
                    Text(
                        text = "BehaviorTree:\nPlayerAgent.bt",
                        color = TextSecondary,
                        fontSize = 7.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // 2. Sprite Card
            item {
                CompactInspectorCard(
                    title = "Sprite",
                    subtitle = "Appearance",
                    icon = Icons.Default.Image,
                    iconTint = StudioPurpleLight,
                    isExpanded = isSpriteExpanded,
                    onToggle = { isSpriteExpanded = !isSpriteExpanded }
                ) {
                    Text(
                        text = "Sheet: hero_knight\nFilter: PixelArt",
                        color = TextSecondary,
                        fontSize = 7.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // 3. Physics Card (Expanded by default)
            item {
                CompactInspectorCard(
                    title = "Physics",
                    subtitle = "Collision/Rigid",
                    icon = Icons.Default.ViewInAr,
                    iconTint = StudioGreen,
                    isExpanded = isPhysicsExpanded,
                    onToggle = { isPhysicsExpanded = !isPhysicsExpanded }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        // Gravity Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Gravity",
                                color = TextPrimary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Switch(
                                checked = gravityEnabled,
                                onCheckedChange = { gravityEnabled = it },
                                modifier = Modifier.scale(0.55f),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = StudioPurple
                                )
                            )
                        }

                        // Gravity X/Y
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            CompactInputField(label = "GX", value = gravityX, modifier = Modifier.weight(1f))
                            CompactInputField(label = "GY", value = gravityY, modifier = Modifier.weight(1f))
                        }

                        // Mass Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Mass", color = TextSecondary, fontSize = 7.5.sp)
                                Text("${String.format("%.1f", massValue)}kg", color = StudioPurpleLight, fontSize = 7.5.sp, fontFamily = FontFamily.Monospace)
                            }
                            Slider(
                                value = massValue,
                                onValueChange = { massValue = it },
                                valueRange = 0.1f..10f,
                                modifier = Modifier.height(14.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = StudioPurpleLight,
                                    activeTrackColor = StudioPurple
                                )
                            )
                        }

                        // Friction Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Friction", color = TextSecondary, fontSize = 7.5.sp)
                                Text("${(frictionValue * 100).toInt()}%", color = StudioPurpleLight, fontSize = 7.5.sp, fontFamily = FontFamily.Monospace)
                            }
                            Slider(
                                value = frictionValue,
                                onValueChange = { frictionValue = it },
                                valueRange = 0f..1f,
                                modifier = Modifier.height(14.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = StudioPurpleLight,
                                    activeTrackColor = StudioPurple
                                )
                            )
                        }

                        // Body Type Selector
                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(EngineSurface)
                                    .border(0.4.dp, StudioBorder, RoundedCornerShape(3.dp))
                                    .clickable { showBodyTypeMenu = true }
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = bodyType,
                                    color = StudioPurpleLight,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(10.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showBodyTypeMenu,
                                onDismissRequest = { showBodyTypeMenu = false }
                            ) {
                                listOf("Dynamic", "Kinematic", "Static").forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type, fontSize = 9.sp) },
                                        onClick = {
                                            bodyType = type
                                            showBodyTypeMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Collision Layers (1, 2, 3, 4)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Layers:", color = TextMuted, fontSize = 7.sp)
                            (1..4).forEach { layer ->
                                val active = collisionLayers.contains(layer)
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (active) StudioPurple else EngineSurface)
                                        .border(0.4.dp, if (active) StudioPurpleLight else StudioBorder, RoundedCornerShape(2.dp))
                                        .clickable {
                                            collisionLayers = if (active) collisionLayers - layer else collisionLayers + layer
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$layer",
                                        color = if (active) Color.White else TextMuted,
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Animation Card
            item {
                CompactInspectorCard(
                    title = "Animation",
                    subtitle = "State Machine",
                    icon = Icons.Default.DirectionsRun,
                    iconTint = StudioOrange,
                    isExpanded = isAnimationExpanded,
                    onToggle = { isAnimationExpanded = !isAnimationExpanded }
                ) {
                    Text(
                        text = "Clip: Idle (Loop)\nSpeed: 1.0x",
                        color = TextSecondary,
                        fontSize = 7.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // 5. Audio Card
            item {
                CompactInspectorCard(
                    title = "Audio",
                    subtitle = "2D Sound",
                    icon = Icons.Default.MusicNote,
                    iconTint = StudioPink,
                    isExpanded = isAudioExpanded,
                    onToggle = { isAudioExpanded = !isAudioExpanded }
                ) {
                    Text(
                        text = "SFX: footstep_grass\nVolume: 80%",
                        color = TextSecondary,
                        fontSize = 7.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // 6. Health Card
            item {
                CompactInspectorCard(
                    title = "Health",
                    subtitle = "Stats",
                    icon = Icons.Default.Favorite,
                    iconTint = StudioRed,
                    isExpanded = isHealthExpanded,
                    onToggle = { isHealthExpanded = !isHealthExpanded }
                ) {
                    Text(
                        text = "HP: 100/100\nArmor: 15",
                        color = TextSecondary,
                        fontSize = 7.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Bottom Add Component Button (Compact: 22dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(StudioPurpleDark, StudioPurple)
                    )
                )
                .border(0.6.dp, StudioPurpleBorder, RoundedCornerShape(4.dp))
                .clickable { /* Add new component */ }
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "+ Add Component",
                    color = Color.White,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CompactInspectorCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(3.dp))
            .background(EngineCardBg)
            .border(0.4.dp, StudioBorder, RoundedCornerShape(3.dp))
            .padding(3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() },
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
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Column {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(10.dp)
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.4.dp)
                        .background(StudioBorder)
                )
                Spacer(modifier = Modifier.height(3.dp))
                content()
            }
        }
    }
}

@Composable
private fun CompactInputField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(16.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(EngineSurface)
            .border(0.4.dp, StudioBorder, RoundedCornerShape(2.dp))
            .padding(horizontal = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            color = TextMuted,
            fontSize = 7.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 7.5.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

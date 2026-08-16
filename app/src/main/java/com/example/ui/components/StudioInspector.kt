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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.theme.StudioPurpleBg
import com.example.ui.theme.StudioPurpleBorder
import com.example.ui.theme.StudioPurpleDark
import com.example.ui.theme.StudioPurpleLight
import com.example.ui.theme.StudioRed
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
    var velX by remember { mutableStateOf("0") }
    var velY by remember { mutableStateOf("0") }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(220.dp)
            .background(EngineSurface)
            .border(0.8.dp, StudioBorder)
            .padding(8.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // ========================================================
            // 1. Brain (AI & Behavior)
            // ========================================================
            item {
                InspectorComponentCard(
                    title = "Brain",
                    subtitle = "AI & Behavior",
                    icon = Icons.Default.Psychology,
                    iconTint = StudioBlue,
                    isExpanded = isBrainExpanded,
                    onToggle = { isBrainExpanded = !isBrainExpanded }
                ) {
                    Text(
                        text = "Behavior Tree: PlayerAgent.bt\nState: Patrol & Follow Target",
                        color = TextSecondary,
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // ========================================================
            // 2. Sprite (Appearance)
            // ========================================================
            item {
                InspectorComponentCard(
                    title = "Sprite",
                    subtitle = "Appearance",
                    icon = Icons.Default.Image,
                    iconTint = StudioPurpleLight,
                    isExpanded = isSpriteExpanded,
                    onToggle = { isSpriteExpanded = !isSpriteExpanded }
                ) {
                    Text(
                        text = "Texture: hero_knight_sheet.png\nFilter: PixelArt (Nearest)\nSorting: Layer 2",
                        color = TextSecondary,
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // ========================================================
            // 3. Physics (Collision & Movement) - Expanded
            // ========================================================
            item {
                InspectorComponentCard(
                    title = "Physics",
                    subtitle = "Collision & Movement",
                    icon = Icons.Default.ViewInAr,
                    iconTint = StudioGreen,
                    isExpanded = isPhysicsExpanded,
                    onToggle = { isPhysicsExpanded = !isPhysicsExpanded }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Gravity Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Gravity", color = TextSecondary, fontSize = 9.5.sp)
                            Switch(
                                checked = gravityEnabled,
                                onCheckedChange = { gravityEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = StudioPurple
                                ),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // X / Y Gravity inputs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "X", color = TextMuted, fontSize = 9.sp)
                            OutlinedTextField(
                                value = gravityX,
                                onValueChange = { gravityX = it },
                                modifier = Modifier.weight(1f).height(30.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = StudioPurple,
                                    unfocusedBorderColor = StudioBorder,
                                    focusedContainerColor = EngineSurface,
                                    unfocusedContainerColor = EngineSurface,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(4.dp)
                            )

                            Text(text = "Y", color = TextMuted, fontSize = 9.sp)
                            OutlinedTextField(
                                value = gravityY,
                                onValueChange = { gravityY = it },
                                modifier = Modifier.weight(1f).height(30.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = StudioPurple,
                                    unfocusedBorderColor = StudioBorder,
                                    focusedContainerColor = EngineSurface,
                                    unfocusedContainerColor = EngineSurface,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(4.dp)
                            )
                        }

                        // Mass Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Mass", color = TextSecondary, fontSize = 9.5.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = String.format("%.1f", massValue), color = TextPrimary, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace)
                                Slider(
                                    value = massValue,
                                    onValueChange = { massValue = it },
                                    valueRange = 0.1f..5.0f,
                                    colors = SliderDefaults.colors(thumbColor = StudioPurpleLight, activeTrackColor = StudioPurple),
                                    modifier = Modifier.width(70.dp).height(20.dp)
                                )
                            }
                        }

                        // Friction Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Friction", color = TextSecondary, fontSize = 9.5.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = String.format("%.1f", frictionValue), color = TextPrimary, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace)
                                Slider(
                                    value = frictionValue,
                                    onValueChange = { frictionValue = it },
                                    valueRange = 0.0f..1.0f,
                                    colors = SliderDefaults.colors(thumbColor = StudioPurpleLight, activeTrackColor = StudioPurple),
                                    modifier = Modifier.width(70.dp).height(20.dp)
                                )
                            }
                        }

                        // Body Type Dropdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Body Type", color = TextSecondary, fontSize = 9.5.sp)
                            Box {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(EngineSurface)
                                        .border(0.6.dp, StudioBorder, RoundedCornerShape(4.dp))
                                        .clickable { showBodyTypeMenu = true }
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = bodyType, color = TextPrimary, fontSize = 9.sp)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextMuted, modifier = Modifier.size(11.dp))
                                }

                                DropdownMenu(
                                    expanded = showBodyTypeMenu,
                                    onDismissRequest = { showBodyTypeMenu = false }
                                ) {
                                    listOf("Dynamic", "Static", "Kinematic").forEach { type ->
                                        DropdownMenuItem(
                                            text = { Text(type, fontSize = 10.sp) },
                                            onClick = {
                                                bodyType = type
                                                showBodyTypeMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Collision Layer chips (1, 2, 3, 4)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Collision Layer", color = TextSecondary, fontSize = 9.5.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                listOf(1, 2, 3, 4).forEach { layer ->
                                    val isSelected = collisionLayers.contains(layer)
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (isSelected) StudioPurple else EngineSurface)
                                            .border(0.6.dp, if (isSelected) StudioPurpleLight else StudioBorder, RoundedCornerShape(3.dp))
                                            .clickable {
                                                collisionLayers = if (isSelected) collisionLayers - layer else collisionLayers + layer
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = layer.toString(),
                                            color = if (isSelected) Color.White else TextMuted,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Velocity X / Y
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Velocity", color = TextSecondary, fontSize = 9.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(text = "X: $velX", color = TextPrimary, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Y: $velY", color = TextPrimary, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // ========================================================
            // 4. Animation (Frames & Transitions)
            // ========================================================
            item {
                InspectorComponentCard(
                    title = "Animation",
                    subtitle = "Frames & Transitions",
                    icon = Icons.Default.DirectionsRun,
                    iconTint = StudioOrange,
                    isExpanded = isAnimationExpanded,
                    onToggle = { isAnimationExpanded = !isAnimationExpanded }
                ) {
                    Text(text = "Active Controller: HeroAnimator\nTransitions: Idle -> Run -> Jump", color = TextSecondary, fontSize = 9.sp)
                }
            }

            // ========================================================
            // 5. Audio (Sounds)
            // ========================================================
            item {
                InspectorComponentCard(
                    title = "Audio",
                    subtitle = "Sounds",
                    icon = Icons.Default.MusicNote,
                    iconTint = StudioRed,
                    isExpanded = isAudioExpanded,
                    onToggle = { isAudioExpanded = !isAudioExpanded }
                ) {
                    Text(text = "Audio Clips: Footsteps.wav, Slash.wav", color = TextSecondary, fontSize = 9.sp)
                }
            }

            // ========================================================
            // 6. Health (Hit Points & Damage)
            // ========================================================
            item {
                InspectorComponentCard(
                    title = "Health",
                    subtitle = "Hit Points & Damage",
                    icon = Icons.Default.Favorite,
                    iconTint = StudioPink,
                    isExpanded = isHealthExpanded,
                    onToggle = { isHealthExpanded = !isHealthExpanded }
                ) {
                    Text(text = "Max HP: 100\nArmor: 15\nInvulnerability Frames: 0.5s", color = TextSecondary, fontSize = 9.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ========================================================
        // Bottom "+ Add Component" Button (Purple pill button)
        // ========================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(StudioPurpleDark, StudioPurple)
                    )
                )
                .border(0.8.dp, StudioPurpleBorder, RoundedCornerShape(10.dp))
                .clickable { /* Add component */ }
                .testTag("add_component_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add Component",
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun InspectorComponentCard(
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
            .clip(RoundedCornerShape(8.dp))
            .background(EngineCardBg)
            .border(0.8.dp, StudioBorder, RoundedCornerShape(8.dp))
            .padding(6.dp)
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
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        color = TextMuted,
                        fontSize = 7.5.sp
                    )
                }
            }

            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(14.dp)
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(top = 6.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.6.dp)
                        .background(StudioBorder)
                )
                Spacer(modifier = Modifier.height(6.dp))
                content()
            }
        }
    }
}

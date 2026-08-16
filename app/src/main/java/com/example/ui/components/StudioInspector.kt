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
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import androidx.compose.runtime.mutableIntStateOf
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
    onCollapse: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var inspectorTab by remember { mutableIntStateOf(0) } // 0: Transform/Phys, 1: Visual/Anim, 2: Script/Audio

    // Physics Parameters State
    var gravityEnabled by remember { mutableStateOf(true) }
    var massValue by remember { mutableStateOf(1.0f) }
    var frictionValue by remember { mutableStateOf(0.2f) }
    var bounceValue by remember { mutableStateOf(0.1f) }
    var bodyType by remember { mutableStateOf("Dynamic") }
    var showBodyTypeMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(140.dp)
            .background(EngineSurface)
            .border(0.6.dp, StudioBorder)
            .padding(4.dp)
    ) {
        // ========================================================
        // 1. Inspector Header + Collapse Chevron
        // ========================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "INSPECTOR",
                    color = TextSecondary,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(3.dp))
                selectedNode?.let {
                    Text(
                        text = it.name,
                        color = StudioPurpleLight,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(15.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(EngineCardBg)
                    .border(0.5.dp, StudioBorder, RoundedCornerShape(3.dp))
                    .clickable { onCollapse() }
                    .testTag("collapse_inspector_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "إغلاق لوحة الخصائص",
                    tint = TextSecondary,
                    modifier = Modifier.size(11.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ========================================================
        // 2. Compact Tab Selector (Transform, Visual, Script)
        // ========================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(EngineCardBg)
                .padding(1.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            listOf("Physics", "Visual", "Scripts").forEachIndexed { index, title ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (inspectorTab == index) StudioPurple else Color.Transparent)
                        .clickable { inspectorTab = index },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (inspectorTab == index) Color.White else TextSecondary,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ========================================================
        // 3. Tab Specific Properties (Clean, Minimal, High-Performance)
        // ========================================================
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (inspectorTab == 0) {
                // --- TRANSFORM & PHYSICS TAB ---
                item {
                    // Transform Section
                    CompactInspectorCard(
                        title = "Transform 2D",
                        subtitle = "Position & Scale",
                        icon = Icons.Default.ViewInAr,
                        iconTint = StudioPurpleLight,
                        isExpanded = true,
                        onToggle = {}
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            // X & Y Position
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                MiniCoordBadge("X", "${selectedNode?.posX?.toInt() ?: 0} px", StudioRed, Modifier.weight(1f))
                                MiniCoordBadge("Y", "${selectedNode?.posY?.toInt() ?: 0} px", StudioGreen, Modifier.weight(1f))
                            }
                            // Scale & Rotation
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                MiniCoordBadge("S", "${selectedNode?.scale ?: 1.0}x", StudioBlue, Modifier.weight(1f))
                                MiniCoordBadge("R", "${selectedNode?.rotation?.toInt() ?: 0}°", StudioOrange, Modifier.weight(1f))
                            }
                        }
                    }
                }

                item {
                    // Rigidbody 2D Section
                    CompactInspectorCard(
                        title = "Rigidbody 2D",
                        subtitle = "Physics Engine",
                        icon = Icons.Default.DirectionsRun,
                        iconTint = StudioOrange,
                        isExpanded = true,
                        onToggle = {}
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            // Body Type selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Type:", color = TextMuted, fontSize = 7.sp)
                                Box {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(EngineSurface)
                                            .border(0.4.dp, StudioBorder, RoundedCornerShape(3.dp))
                                            .clickable { showBodyTypeMenu = true }
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(bodyType, color = TextPrimary, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                    DropdownMenu(
                                        expanded = showBodyTypeMenu,
                                        onDismissRequest = { showBodyTypeMenu = false }
                                    ) {
                                        listOf("Dynamic", "Static", "Kinematic").forEach { type ->
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
                            }

                            // Gravity Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Simulate Gravity", color = TextSecondary, fontSize = 7.sp)
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

                            // Mass Slider
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Mass (kg):", color = TextMuted, fontSize = 7.sp)
                                    Text(String.format("%.1f", massValue), color = StudioPurpleLight, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                                }
                                Slider(
                                    value = massValue,
                                    onValueChange = { massValue = it },
                                    valueRange = 0.1f..10.0f,
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
                                    Text("Friction:", color = TextMuted, fontSize = 7.sp)
                                    Text(String.format("%.2f", frictionValue), color = StudioPurpleLight, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                                }
                                Slider(
                                    value = frictionValue,
                                    onValueChange = { frictionValue = it },
                                    valueRange = 0.0f..1.0f,
                                    modifier = Modifier.height(14.dp),
                                    colors = SliderDefaults.colors(
                                        thumbColor = StudioPurpleLight,
                                        activeTrackColor = StudioPurple
                                    )
                                )
                            }
                        }
                    }
                }
            } else if (inspectorTab == 1) {
                // --- VISUAL & ANIMATION TAB ---
                item {
                    CompactInspectorCard(
                        title = "Sprite Renderer",
                        subtitle = "Shader & Texture",
                        icon = Icons.Default.Image,
                        iconTint = StudioPink,
                        isExpanded = true,
                        onToggle = {}
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Texture: atlas_hero_v1.png", color = TextPrimary, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                            Text("Shader: Lit/Diffuse2D", color = TextMuted, fontSize = 6.5.sp)
                            Text("Sorting Layer: Default [0]", color = StudioBlue, fontSize = 6.5.sp)
                        }
                    }
                }

                item {
                    CompactInspectorCard(
                        title = "Animator 2D",
                        subtitle = "State Machine",
                        icon = Icons.Default.DirectionsRun,
                        iconTint = StudioYellow,
                        isExpanded = true,
                        onToggle = {}
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Controller: HeroAnimTree", color = TextPrimary, fontSize = 7.sp)
                            Text("Current State: IDLE_LOOP", color = StudioGreen, fontSize = 6.5.sp, fontWeight = FontWeight.Bold)
                            Text("FPS Rate: 24 frames/sec", color = TextMuted, fontSize = 6.5.sp)
                        }
                    }
                }
            } else {
                // --- SCRIPTS & AUDIO TAB ---
                item {
                    CompactInspectorCard(
                        title = "AI Behavior Tree",
                        subtitle = "Logic & Events",
                        icon = Icons.Default.Psychology,
                        iconTint = StudioBlue,
                        isExpanded = true,
                        onToggle = {}
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Script: PlayerController.kt", color = TextPrimary, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                            Text("Speed: 180.0 px/s", color = TextMuted, fontSize = 6.5.sp)
                            Text("Jump Force: 420.0 N", color = TextMuted, fontSize = 6.5.sp)
                        }
                    }
                }

                item {
                    CompactInspectorCard(
                        title = "Audio Source",
                        subtitle = "SFX & Ambience",
                        icon = Icons.Default.MusicNote,
                        iconTint = StudioGreen,
                        isExpanded = true,
                        onToggle = {}
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Clip: sfx_jump_01.ogg", color = TextPrimary, fontSize = 7.sp)
                            Text("Volume: 0.85 (Spatial 2D)", color = TextMuted, fontSize = 6.5.sp)
                        }
                    }
                }
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(EngineCardBg)
            .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
            .padding(3.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
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
                        tint = iconTint,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(9.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 3.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun MiniCoordBadge(
    axis: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(18.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(EngineSurface)
            .border(0.4.dp, StudioBorder, RoundedCornerShape(3.dp))
            .padding(horizontal = 3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$axis:",
                color = accentColor,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 6.5.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

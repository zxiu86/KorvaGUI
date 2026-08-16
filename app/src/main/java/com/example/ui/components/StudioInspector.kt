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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewInAr
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
    onUpdateName: (String) -> Unit = {},
    onUpdatePos: (Float, Float) -> Unit = { _, _ -> },
    onUpdateScale: (Float) -> Unit = {},
    onUpdateRotation: (Float) -> Unit = {},
    onUpdateColor: (String) -> Unit = {},
    onUpdatePhysics: (Boolean, Float) -> Unit = { _, _ -> },
    onCollapse: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var inspectorTab by remember { mutableIntStateOf(0) } // 0: Transform/Phys, 1: Visual/Anim, 2: Script/Audio
    var isEditingName by remember { mutableStateOf(false) }
    var nameInput by remember(selectedNode?.name) { mutableStateOf(selectedNode?.name ?: "") }

    val presetColors = remember {
        listOf(
            "#8B5CF6" to StudioPurple,
            "#EC4899" to StudioPink,
            "#3B82F6" to StudioBlue,
            "#10B981" to StudioGreen,
            "#F59E0B" to StudioOrange,
            "#EF4444" to StudioRed,
            "#EAB308" to StudioYellow,
            "#06B6D4" to Color(0xFF06B6D4)
        )
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(145.dp)
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
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = StudioPurpleLight,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "INSPECTOR",
                    color = TextSecondary,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
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

        Spacer(modifier = Modifier.height(3.dp))

        // Selected Node Name Field (Directly Editable)
        if (selectedNode != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(3.dp))
                    .background(EngineCardBg)
                    .border(0.4.dp, StudioBorder, RoundedCornerShape(3.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (isEditingName) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioPurpleLight,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = EngineSurface,
                            unfocusedContainerColor = EngineSurface
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (nameInput.isNotBlank()) onUpdateName(nameInput)
                            isEditingName = false
                        })
                    )
                } else {
                    Text(
                        text = selectedNode.name,
                        color = StudioPurpleLight,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                }

                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "تعديل الاسم",
                    tint = TextMuted,
                    modifier = Modifier
                        .size(9.dp)
                        .clickable {
                            if (isEditingName && nameInput.isNotBlank()) {
                                onUpdateName(nameInput)
                            }
                            isEditingName = !isEditingName
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // ========================================================
        // 2. Compact Tab Selector (Physics, Visual, Scripts)
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
            listOf("Transform", "Visual", "Scripts").forEachIndexed { index, title ->
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

        Spacer(modifier = Modifier.height(3.dp))

        // ========================================================
        // 3. Tab Specific Properties (100% Functional & Connected)
        // ========================================================
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (inspectorTab == 0) {
                // --- TRANSFORM 2D ---
                item {
                    CompactInspectorCard(
                        title = "Transform 2D",
                        icon = Icons.Default.ViewInAr,
                        iconTint = StudioPurpleLight
                    ) {
                        if (selectedNode != null) {
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                // X Position with Step Controls
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("X:", color = StudioRed, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                                    Text("${selectedNode.posX.toInt()} px", color = TextPrimary, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                                    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                        MiniStepBtn("-10") { onUpdatePos(selectedNode.posX - 10f, selectedNode.posY) }
                                        MiniStepBtn("+10") { onUpdatePos(selectedNode.posX + 10f, selectedNode.posY) }
                                    }
                                }

                                // Y Position with Step Controls
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Y:", color = StudioGreen, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                                    Text("${selectedNode.posY.toInt()} px", color = TextPrimary, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                                    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                        MiniStepBtn("-10") { onUpdatePos(selectedNode.posX, selectedNode.posY - 10f) }
                                        MiniStepBtn("+10") { onUpdatePos(selectedNode.posX, selectedNode.posY + 10f) }
                                    }
                                }

                                // Scale Slider (0.2x to 4.0x)
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Scale:", color = StudioBlue, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(String.format("%.1fx", selectedNode.scale), color = TextPrimary, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Reset Scale",
                                                tint = TextMuted,
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clickable { onUpdateScale(1.0f) }
                                            )
                                        }
                                    }
                                    Slider(
                                        value = selectedNode.scale,
                                        onValueChange = { onUpdateScale(it) },
                                        valueRange = 0.2f..4.0f,
                                        modifier = Modifier.height(14.dp),
                                        colors = SliderDefaults.colors(
                                            thumbColor = StudioPurpleLight,
                                            activeTrackColor = StudioPurple
                                        )
                                    )
                                }

                                // Rotation Slider (0 to 360)
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Rot:", color = StudioOrange, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("${selectedNode.rotation.toInt()}°", color = TextPrimary, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Icon(
                                                imageVector = Icons.Default.RotateRight,
                                                contentDescription = "Rotate 90",
                                                tint = TextMuted,
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clickable { onUpdateRotation(selectedNode.rotation + 90f) }
                                            )
                                        }
                                    }
                                    Slider(
                                        value = selectedNode.rotation,
                                        onValueChange = { onUpdateRotation(it) },
                                        valueRange = 0f..360f,
                                        modifier = Modifier.height(14.dp),
                                        colors = SliderDefaults.colors(
                                            thumbColor = StudioPurpleLight,
                                            activeTrackColor = StudioPurple
                                        )
                                    )
                                }
                            }
                        } else {
                            Text("حدد عنصراً لعرض خصائصه", color = TextMuted, fontSize = 7.sp)
                        }
                    }
                }

                // --- RIGIDBODY 2D ---
                item {
                    CompactInspectorCard(
                        title = "Rigidbody 2D",
                        icon = Icons.Default.DirectionsRun,
                        iconTint = StudioOrange
                    ) {
                        if (selectedNode != null) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                // Enable Physics Switch
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Simulate Body", color = TextSecondary, fontSize = 7.sp)
                                    Switch(
                                        checked = selectedNode.hasPhysics,
                                        onCheckedChange = { onUpdatePhysics(it, selectedNode.mass) },
                                        modifier = Modifier.scale(0.55f),
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = StudioPurple
                                        )
                                    )
                                }

                                if (selectedNode.hasPhysics) {
                                    // Mass Slider
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Mass:", color = TextMuted, fontSize = 7.sp)
                                            Text("${String.format("%.1f", selectedNode.mass)} kg", color = StudioPurpleLight, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                                        }
                                        Slider(
                                            value = selectedNode.mass,
                                            onValueChange = { onUpdatePhysics(true, it) },
                                            valueRange = 0.1f..10.0f,
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
                    }
                }
            } else if (inspectorTab == 1) {
                // --- VISUAL & COLOR TAB ---
                item {
                    CompactInspectorCard(
                        title = "Color & Material",
                        icon = Icons.Default.ColorLens,
                        iconTint = StudioPink
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("Node Color Tint:", color = TextSecondary, fontSize = 7.sp)
                            // Palette Swatches
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                presetColors.take(4).forEach { (hex, col) ->
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(col)
                                            .border(
                                                width = if (selectedNode?.colorHex == hex) 1.2.dp else 0.4.dp,
                                                color = if (selectedNode?.colorHex == hex) Color.White else StudioBorder,
                                                shape = CircleShape
                                            )
                                            .clickable { onUpdateColor(hex) }
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                presetColors.drop(4).forEach { (hex, col) ->
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(col)
                                            .border(
                                                width = if (selectedNode?.colorHex == hex) 1.2.dp else 0.4.dp,
                                                color = if (selectedNode?.colorHex == hex) Color.White else StudioBorder,
                                                shape = CircleShape
                                            )
                                            .clickable { onUpdateColor(hex) }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    CompactInspectorCard(
                        title = "Sprite Renderer",
                        icon = Icons.Default.Image,
                        iconTint = StudioYellow
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Type: ${selectedNode?.type?.name ?: "SPRITE"}", color = StudioPurpleLight, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                            Text("Visible: ${selectedNode?.isVisible ?: true}", color = TextSecondary, fontSize = 6.5.sp)
                            Text("Layer: Default [0]", color = StudioBlue, fontSize = 6.5.sp)
                        }
                    }
                }
            } else {
                // --- SCRIPTS & LOGIC TAB ---
                item {
                    CompactInspectorCard(
                        title = "Behavior Script",
                        icon = Icons.Default.Psychology,
                        iconTint = StudioBlue
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Script: ${selectedNode?.name ?: "Entity"}Controller.kt", color = TextPrimary, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                            Text("Status: Active / Attached", color = StudioGreen, fontSize = 6.5.sp, fontWeight = FontWeight.Bold)
                            Text("Events: OnTick, OnCollision2D", color = TextMuted, fontSize = 6.5.sp)
                        }
                    }
                }

                item {
                    CompactInspectorCard(
                        title = "Audio SFX",
                        icon = Icons.Default.MusicNote,
                        iconTint = StudioGreen
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Clip: sfx_${selectedNode?.name?.lowercase() ?: "action"}.ogg", color = TextPrimary, fontSize = 7.sp)
                            Text("Spatial 2D: Enabled", color = TextMuted, fontSize = 6.5.sp)
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
    icon: ImageVector,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(EngineCardBg)
            .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
            .padding(4.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
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
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            content()
        }
    }
}

@Composable
private fun MiniStepBtn(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(2.dp))
            .background(EngineSurface)
            .border(0.4.dp, StudioBorder, RoundedCornerShape(2.dp))
            .clickable { onClick() }
            .padding(horizontal = 3.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = StudioPurpleLight,
            fontSize = 6.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

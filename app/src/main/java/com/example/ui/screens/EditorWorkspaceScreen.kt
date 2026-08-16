package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectEntity
import com.example.model.EditorTab
import com.example.model.LogLevel
import com.example.model.NodeType
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.components.KorvaStatusBar
import com.example.ui.theme.EngineBackground
import com.example.ui.theme.EngineBorder
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.EngineSurface
import com.example.ui.theme.EngineWhiteBorder
import com.example.ui.theme.EngineWhiteGlass
import com.example.ui.theme.EngineWhiteMuted
import com.example.ui.theme.EngineWhitePrimary
import com.example.ui.theme.EngineWhiteTranslucent
import com.example.ui.theme.KorvaAmber
import com.example.ui.theme.KorvaEmerald
import com.example.ui.theme.KorvaPurple
import com.example.ui.theme.KorvaRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun EditorWorkspaceScreen(
    project: ProjectEntity,
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var showAddNodeMenu by remember { mutableStateOf(false) }
    var zoomLevel by remember { mutableStateOf(1.0f) }
    val selectedNode = uiState.sceneNodes.find { it.id == uiState.selectedNodeId }

    // Simulation animation loop
    LaunchedEffect(uiState.isSimulationPlaying) {
        if (uiState.isSimulationPlaying) {
            var step = 0
            while (uiState.isSimulationPlaying) {
                delay(16)
                step++
                val angle = step * 0.05f
                val dx = kotlin.math.sin(angle) * 1.5f
                val dy = kotlin.math.cos(angle) * 0.8f
                viewModel.updateSelectedNodePos(dx.toFloat(), dy.toFloat())
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EngineBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ========================================================
            // 1. Top Editor Header Bar
            // ========================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(EngineSurface)
                    .border(0.8.dp, EngineBorder)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Back button + Project Name + Mode Switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EngineCardBg)
                            .border(0.8.dp, EngineBorder, RoundedCornerShape(6.dp))
                            .clickable { viewModel.closeEditor() }
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                            .testTag("back_to_hub_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "العودة للرئيسية",
                                tint = EngineWhitePrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "الرئيسية",
                                color = EngineWhitePrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(20.dp)
                            .width(1.dp)
                            .background(EngineBorder)
                    )

                    // Project Name & Type Badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = project.name,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EngineWhiteGlass)
                                .border(0.6.dp, EngineWhiteBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.5.dp)
                        ) {
                            Text(
                                text = project.templateType,
                                color = EngineWhiteTranslucent,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Tab Switcher (Scene / Code / Console)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EngineCardBg)
                            .border(0.8.dp, EngineBorder, RoundedCornerShape(6.dp))
                            .padding(2.dp)
                    ) {
                        listOf(
                            EditorTab.SCENE_VIEW to "المشهد الرسومي",
                            EditorTab.CODE_EDITOR to "البرمجيات (Code)",
                            EditorTab.CONSOLE to "شاشة الأوامر (Log)"
                        ).forEach { (tab, label) ->
                            val isSelected = uiState.editorTab == tab
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) EngineWhiteGlass else Color.Transparent)
                                    .border(if (isSelected) 0.6.dp else 0.dp, if (isSelected) EngineWhiteBorder else Color.Transparent, RoundedCornerShape(4.dp))
                                    .clickable { viewModel.setEditorTab(tab) }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) EngineWhitePrimary else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Right: Simulation Controls & Save Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Play / Pause Simulation
                    Button(
                        onClick = { viewModel.toggleSimulation() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isSimulationPlaying) KorvaRed else EngineWhiteGlass,
                            contentColor = if (uiState.isSimulationPlaying) Color.White else EngineWhitePrimary
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("toggle_simulation_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (uiState.isSimulationPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (uiState.isSimulationPlaying) "إيقاف المحاكاة" else "تشغيل المحرك",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Save Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EngineWhitePrimary)
                            .clickable { viewModel.saveCurrentProject() }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                            .testTag("save_project_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "حفظ",
                                tint = EngineBackground,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "حفظ",
                                color = EngineBackground,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ========================================================
            // 2. Main Studio 3-Panel Layout
            // ========================================================
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // ----------------------------------------------------
                // A. Left Panel: Hierarchy / Scene Tree (1/4 Width)
                // ----------------------------------------------------
                Column(
                    modifier = Modifier
                        .width(200.dp)
                        .fillMaxHeight()
                        .background(EngineSurface)
                        .border(width = 0.8.dp, color = EngineBorder)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = EngineWhiteTranslucent,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "شجرة المشهد (Scene)",
                                color = TextPrimary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box {
                            IconButton(
                                onClick = { showAddNodeMenu = true },
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(EngineWhiteGlass)
                                    .testTag("add_node_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "إضافة كائن", tint = EngineWhitePrimary, modifier = Modifier.size(14.dp))
                            }

                            DropdownMenu(
                                expanded = showAddNodeMenu,
                                onDismissRequest = { showAddNodeMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Player Character (لاعب)", fontSize = 11.sp) },
                                    onClick = {
                                        viewModel.addSceneNode("Hero Player", NodeType.PLAYER)
                                        showAddNodeMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Enemy Object (عدو)", fontSize = 11.sp) },
                                    onClick = {
                                        viewModel.addSceneNode("Enemy Drone", NodeType.ENEMY)
                                        showAddNodeMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Physics Platform (منصة)", fontSize = 11.sp) },
                                    onClick = {
                                        viewModel.addSceneNode("Tile Platform", NodeType.PLATFORM)
                                        showAddNodeMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Particle Emitter (مؤثرات)", fontSize = 11.sp) },
                                    onClick = {
                                        viewModel.addSceneNode("Sparks Emitter", NodeType.PARTICLE_SYSTEM)
                                        showAddNodeMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(uiState.sceneNodes, key = { it.id }) { node ->
                            val isSelected = node.id == uiState.selectedNodeId
                            val nodeColor = try {
                                Color(android.graphics.Color.parseColor(node.colorHex))
                            } catch (e: Exception) {
                                EngineWhitePrimary
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(if (isSelected) EngineWhiteGlass else EngineCardBg)
                                    .border(
                                        width = if (isSelected) 0.8.dp else 0.4.dp,
                                        color = if (isSelected) EngineWhiteBorder else EngineBorder,
                                        shape = RoundedCornerShape(5.dp)
                                    )
                                    .clickable { viewModel.selectSceneNode(node.id) }
                                    .padding(horizontal = 7.dp, vertical = 5.dp),
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
                                            .background(nodeColor)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = node.name,
                                        color = if (isSelected) EngineWhitePrimary else TextPrimary,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = node.type.name.take(3),
                                    color = TextMuted,
                                    fontSize = 8.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // ----------------------------------------------------
                // B. Center Viewport / Canvas / Code Tab (Flexible Width)
                // ----------------------------------------------------
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(EngineBackground)
                ) {
                    when (uiState.editorTab) {
                        EditorTab.SCENE_VIEW -> {
                            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                val canvasWidth = constraints.maxWidth.toFloat()
                                val canvasHeight = constraints.maxHeight.toFloat()
                                val centerX = canvasWidth / 2f
                                val centerY = canvasHeight / 2f

                                Canvas(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .pointerInput(Unit) {
                                            detectDragGestures { change, dragAmount ->
                                                change.consume()
                                                viewModel.updateSelectedNodePos(
                                                    dragAmount.x / zoomLevel,
                                                    dragAmount.y / zoomLevel
                                                )
                                            }
                                        }
                                        .testTag("scene_viewport_canvas")
                                ) {
                                    // Draw Grid
                                    val gridSize = 40f * zoomLevel
                                    val gridColor = Color(0xFF1B2028)
                                    var x = 0f
                                    while (x < size.width) {
                                        drawLine(
                                            color = gridColor,
                                            start = Offset(x, 0f),
                                            end = Offset(x, size.height),
                                            strokeWidth = 0.8f
                                        )
                                        x += gridSize
                                    }
                                    var y = 0f
                                    while (y < size.height) {
                                        drawLine(
                                            color = gridColor,
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = 0.8f
                                        )
                                        y += gridSize
                                    }

                                    // Center Axes in subtle white
                                    drawLine(
                                        color = Color.White.copy(alpha = 0.15f),
                                        start = Offset(centerX, 0f),
                                        end = Offset(centerX, size.height),
                                        strokeWidth = 1f,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                                    )
                                    drawLine(
                                        color = Color.White.copy(alpha = 0.15f),
                                        start = Offset(0f, centerY),
                                        end = Offset(size.width, centerY),
                                        strokeWidth = 1f,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                                    )

                                    // Draw Scene Nodes
                                    uiState.sceneNodes.forEach { node ->
                                        val nodeColor = try {
                                            Color(android.graphics.Color.parseColor(node.colorHex))
                                        } catch (e: Exception) {
                                            EngineWhitePrimary
                                        }
                                        val isCurrent = node.id == uiState.selectedNodeId
                                        val drawX = centerX + (node.posX * zoomLevel)
                                        val drawY = centerY + (node.posY * zoomLevel)
                                        val radius = 22f * node.scale * zoomLevel

                                        when (node.type) {
                                            NodeType.PLAYER -> {
                                                drawRect(
                                                    color = nodeColor.copy(alpha = 0.2f),
                                                    topLeft = Offset(drawX - radius, drawY - radius),
                                                    size = Size(radius * 2, radius * 2)
                                                )
                                                drawRect(
                                                    color = nodeColor,
                                                    topLeft = Offset(drawX - radius, drawY - radius),
                                                    size = Size(radius * 2, radius * 2),
                                                    style = Stroke(width = if (isCurrent) 2.5f else 1.2f)
                                                )
                                                drawCircle(
                                                    color = Color.White,
                                                    radius = radius * 0.3f,
                                                    center = Offset(drawX, drawY)
                                                )
                                            }
                                            NodeType.ENEMY -> {
                                                drawCircle(
                                                    color = nodeColor.copy(alpha = 0.25f),
                                                    radius = radius,
                                                    center = Offset(drawX, drawY)
                                                )
                                                drawCircle(
                                                    color = nodeColor,
                                                    radius = radius,
                                                    center = Offset(drawX, drawY),
                                                    style = Stroke(width = if (isCurrent) 2.5f else 1.2f)
                                                )
                                            }
                                            NodeType.PLATFORM -> {
                                                val pWidth = radius * 4f
                                                val pHeight = radius * 0.8f
                                                drawRoundRect(
                                                    color = nodeColor.copy(alpha = 0.35f),
                                                    topLeft = Offset(drawX - pWidth / 2f, drawY - pHeight / 2f),
                                                    size = Size(pWidth, pHeight),
                                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                                                )
                                                drawRoundRect(
                                                    color = nodeColor,
                                                    topLeft = Offset(drawX - pWidth / 2f, drawY - pHeight / 2f),
                                                    size = Size(pWidth, pHeight),
                                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                                                    style = Stroke(width = if (isCurrent) 2.5f else 1.2f)
                                                )
                                            }
                                            else -> {
                                                drawCircle(
                                                    color = nodeColor,
                                                    radius = radius,
                                                    center = Offset(drawX, drawY)
                                                )
                                            }
                                        }

                                        if (isCurrent) {
                                            drawCircle(
                                                color = Color.White.copy(alpha = 0.6f),
                                                radius = radius * 1.4f,
                                                center = Offset(drawX, drawY),
                                                style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f))
                                            )
                                        }
                                    }
                                }

                                // Canvas overlay controls
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(10.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(EngineSurface.copy(alpha = 0.9f))
                                        .border(0.8.dp, EngineBorder, RoundedCornerShape(6.dp))
                                        .padding(3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { zoomLevel = (zoomLevel * 1.2f).coerceAtMost(3.0f) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.ZoomIn, contentDescription = "تكبير", tint = EngineWhitePrimary, modifier = Modifier.size(15.dp))
                                    }
                                    Text(
                                        text = "${(zoomLevel * 100).toInt()}%",
                                        color = TextSecondary,
                                        fontSize = 9.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    IconButton(
                                        onClick = { zoomLevel = (zoomLevel / 1.2f).coerceAtLeast(0.4f) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.ZoomOut, contentDescription = "تصغير", tint = EngineWhitePrimary, modifier = Modifier.size(15.dp))
                                    }
                                }
                            }
                        }
                        EditorTab.CODE_EDITOR -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0C0E12))
                                    .border(0.8.dp, EngineBorder, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "scripts/PlayerController.ks",
                                        color = EngineWhitePrimary,
                                        fontSize = 11.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "KorvaScript v1.0",
                                        color = TextMuted,
                                        fontSize = 9.5.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = """
                                    // Korva Engine Scripting API
                                    class PlayerController : KorvaBehavior() {
                                        var speed: Float = 240.0f
                                        var jumpForce: Float = 480.0f
                                        
                                        override fun onUpdate(deltaTime: Float) {
                                            val moveX = Input.getAxis("Horizontal")
                                            transform.position.x += moveX * speed * deltaTime
                                            
                                            if (Input.isActionJustPressed("Jump") && isGrounded()) {
                                                rigidbody.applyImpulse(Vector2(0f, -jumpForce))
                                                emitParticles("Sparks")
                                            }
                                        }
                                    }
                                    """.trimIndent(),
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                        EditorTab.CONSOLE -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF080A0E))
                                    .border(0.8.dp, EngineBorder, RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(uiState.engineLogs) { log ->
                                    val logColor = when (log.level) {
                                        LogLevel.SUCCESS -> KorvaEmerald
                                        LogLevel.WARN -> KorvaAmber
                                        LogLevel.ERROR -> KorvaRed
                                        else -> EngineWhiteTranslucent
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "[${log.timestamp}]",
                                            color = TextMuted,
                                            fontSize = 9.5.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "[${log.level.name}]",
                                            color = logColor,
                                            fontSize = 9.5.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = log.message,
                                            color = TextPrimary,
                                            fontSize = 10.5.sp,
                                            fontFamily = FontFamily.SansSerif
                                        )
                                    }
                                }
                            }
                        }
                        else -> Unit
                    }
                }

                // ----------------------------------------------------
                // C. Right Panel: Inspector / Properties (1/4 Width)
                // ----------------------------------------------------
                Column(
                    modifier = Modifier
                        .width(220.dp)
                        .fillMaxHeight()
                        .background(EngineSurface)
                        .border(width = 0.8.dp, color = EngineBorder)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "الخصائص (Inspector)",
                            color = TextPrimary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (selectedNode != null) {
                            IconButton(
                                onClick = { viewModel.deleteSelectedNode() },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = KorvaRed.copy(alpha = 0.7f), modifier = Modifier.size(15.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedNode == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "حدد كائناً من المشهد لعرض الخصائص",
                                color = TextMuted,
                                fontSize = 10.5.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(EngineCardBg)
                                        .padding(7.dp)
                                ) {
                                    Column {
                                        Text(text = "اسم الكائن: ${selectedNode.name}", color = EngineWhitePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "النوع: ${selectedNode.type.name}", color = TextSecondary, fontSize = 9.5.sp)
                                    }
                                }
                            }

                            item {
                                Text(text = "الموقع (Transform X / Y)", color = TextSecondary, fontSize = 10.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(EngineCardBg)
                                            .padding(5.dp)
                                    ) {
                                        Text(text = "X: ${selectedNode.posX.toInt()} px", color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(EngineCardBg)
                                            .padding(5.dp)
                                    ) {
                                        Text(text = "Y: ${selectedNode.posY.toInt()} px", color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }

                            item {
                                Text(text = "الحجم والقياس (Scale): ${String.format("%.1f", selectedNode.scale)}x", color = TextSecondary, fontSize = 10.sp)
                                Slider(
                                    value = selectedNode.scale,
                                    onValueChange = { targetScale ->
                                        viewModel.updateSelectedNodeScale(targetScale / selectedNode.scale)
                                    },
                                    valueRange = 0.5f..4.0f,
                                    colors = SliderDefaults.colors(thumbColor = EngineWhitePrimary, activeTrackColor = EngineWhitePrimary)
                                )
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "الفيزياء الصلبة (Rigidbody)", color = TextPrimary, fontSize = 10.5.sp)
                                    Switch(
                                        checked = selectedNode.hasPhysics,
                                        onCheckedChange = { },
                                        colors = SwitchDefaults.colors(checkedThumbColor = EngineWhitePrimary, checkedTrackColor = EngineWhiteGlass)
                                    )
                                }
                            }

                            item {
                                Text(text = "التحكم اليدوي بالموضع:", color = TextSecondary, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(3.dp))
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(EngineCardBg)
                                            .clickable { viewModel.updateSelectedNodePos(0f, -10f) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("▲", color = EngineWhitePrimary, fontSize = 9.5.sp)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(EngineCardBg)
                                                .clickable { viewModel.updateSelectedNodePos(-10f, 0f) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("◀", color = EngineWhitePrimary, fontSize = 9.5.sp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(EngineCardBg)
                                                .clickable { viewModel.updateSelectedNodePos(10f, 0f) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("▶", color = EngineWhitePrimary, fontSize = 9.5.sp)
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(EngineCardBg)
                                            .clickable { viewModel.updateSelectedNodePos(0f, 10f) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("▼", color = EngineWhitePrimary, fontSize = 9.5.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ========================================================
            // 3. Bottom Status Bar in Editor
            // ========================================================
            KorvaStatusBar(
                defaultPath = project.path,
                onChangePathClick = { viewModel.openChangePathDialog() }
            )
        }
    }
}

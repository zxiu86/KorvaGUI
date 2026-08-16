package com.example.ui.components

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NodeType
import com.example.model.SceneNode
import com.example.ui.theme.EngineBackground
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.StudioBlue
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioOrange
import com.example.ui.theme.StudioPink
import com.example.ui.theme.StudioPurple
import com.example.ui.theme.StudioPurpleBorder
import com.example.ui.theme.StudioPurpleDark
import com.example.ui.theme.StudioPurpleGlass
import com.example.ui.theme.StudioPurpleLight
import com.example.ui.theme.StudioRed
import com.example.ui.theme.StudioYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StudioViewport(
    selectedNode: SceneNode?,
    allNodes: List<SceneNode> = emptyList(),
    isHierarchyVisible: Boolean = true,
    isInspectorVisible: Boolean = true,
    isTimelineVisible: Boolean = true,
    onRestoreHierarchy: () -> Unit = {},
    onRestoreInspector: () -> Unit = {},
    onRestoreTimeline: () -> Unit = {},
    onNodeDrag: (dx: Float, dy: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTool by remember { mutableIntStateOf(1) } // 0: Pan, 1: Select, 2: Move, 3: Rotate, 4: Scale, 5: Grid Snap
    var showGrid by remember { mutableStateOf(true) }
    var showSafeFrame by remember { mutableStateOf(true) }
    var showLighting by remember { mutableStateOf(true) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EngineBackground)
    ) {
        // ========================================================
        // 1. Top Ruler (Ultra-Compact: 11dp)
        // ========================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(11.dp)
                .background(EngineCardBg)
                .border(0.4.dp, StudioBorder)
                .padding(start = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("-640", "-480", "-320", "-160", "0", "+160", "+320", "+480", "+640").forEach { mark ->
                Text(
                    text = mark,
                    color = if (mark == "0") StudioPurpleLight else TextMuted,
                    fontSize = 6.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (mark == "0") FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        // ========================================================
        // 2. Left Ruler (Ultra-Compact: 14dp)
        // ========================================================
        Column(
            modifier = Modifier
                .padding(top = 11.dp)
                .width(14.dp)
                .fillMaxHeight()
                .background(EngineCardBg)
                .border(0.4.dp, StudioBorder)
                .padding(vertical = 2.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            listOf("-360", "-240", "-120", "0", "+120", "+240", "+360").forEach { mark ->
                Text(
                    text = mark,
                    color = if (mark == "0") StudioPurpleLight else TextMuted,
                    fontSize = 5.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (mark == "0") FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        // ========================================================
        // 3. Central Procedural Game Canvas Viewport
        // ========================================================
        BoxWithConstraints(
            modifier = Modifier
                .padding(start = 14.dp, top = 11.dp)
                .fillMaxSize()
        ) {
            val canvasW = constraints.maxWidth.toFloat()
            val canvasH = constraints.maxHeight.toFloat()
            val centerX = canvasW / 2f + panOffsetX
            val centerY = canvasH / 2f + panOffsetY

            // Pure Procedural Vector Engine Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(activeTool) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            if (activeTool == 0) {
                                // Pan tool
                                panOffsetX += dragAmount.x
                                panOffsetY += dragAmount.y
                            } else {
                                // Node drag
                                onNodeDrag(dragAmount.x, dragAmount.y)
                            }
                        }
                    }
            ) {
                // A. Engine Dark Radial Background
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF0A0E1A),
                            Color(0xFF05070D)
                        ),
                        center = Offset(centerX, centerY),
                        radius = (canvasW.coerceAtLeast(canvasH)) * 0.8f
                    ),
                    size = size
                )

                // B. Procedural Grid System (Minor + Major)
                if (showGrid) {
                    val minorStep = 20.dp.toPx() * zoomScale
                    val majorStep = 100.dp.toPx() * zoomScale

                    // Minor grid lines
                    val xStartMinor = (centerX % minorStep)
                    var x = xStartMinor
                    while (x < size.width) {
                        drawLine(
                            color = Color(0x0C94A3B8),
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 0.5f
                        )
                        x += minorStep
                    }

                    val yStartMinor = (centerY % minorStep)
                    var y = yStartMinor
                    while (y < size.height) {
                        drawLine(
                            color = Color(0x0C94A3B8),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 0.5f
                        )
                        y += minorStep
                    }

                    // Major grid lines
                    val xStartMajor = (centerX % majorStep)
                    x = xStartMajor
                    while (x < size.width) {
                        drawLine(
                            color = Color(0x1F94A3B8),
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1f
                        )
                        x += majorStep
                    }

                    val yStartMajor = (centerY % majorStep)
                    y = yStartMajor
                    while (y < size.height) {
                        drawLine(
                            color = Color(0x1F94A3B8),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f
                        )
                        y += majorStep
                    }
                }

                // C. Safe Game Canvas Boundary Frame (16:9 Game World)
                val safeFrameW = 380.dp.toPx() * zoomScale
                val safeFrameH = (380.dp.toPx() * (9f / 16f)) * zoomScale
                val frameLeft = centerX - safeFrameW / 2f
                val frameTop = centerY - safeFrameH / 2f

                if (showSafeFrame) {
                    // Outer Mask Shading (Focus attention to active design canvas)
                    drawRect(
                        color = Color(0x35000000),
                        size = size
                    )

                    // Active Game Box Inner Light
                    drawRoundRect(
                        color = Color(0x188B5CF6),
                        topLeft = Offset(frameLeft, frameTop),
                        size = Size(safeFrameW, safeFrameH),
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )

                    // Safe Design Border Frame (Glow neon border)
                    drawRoundRect(
                        color = StudioPurple.copy(alpha = 0.6f),
                        topLeft = Offset(frameLeft, frameTop),
                        size = Size(safeFrameW, safeFrameH),
                        cornerRadius = CornerRadius(6.dp.toPx()),
                        style = Stroke(
                            width = 1.2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f)
                        )
                    )

                    // Corner Accent Brackets
                    val bracketLen = 14.dp.toPx()
                    // Top-Left
                    drawLine(StudioPurpleLight, Offset(frameLeft, frameTop), Offset(frameLeft + bracketLen, frameTop), 2.5f)
                    drawLine(StudioPurpleLight, Offset(frameLeft, frameTop), Offset(frameLeft, frameTop + bracketLen), 2.5f)
                    // Top-Right
                    drawLine(StudioPurpleLight, Offset(frameLeft + safeFrameW, frameTop), Offset(frameLeft + safeFrameW - bracketLen, frameTop), 2.5f)
                    drawLine(StudioPurpleLight, Offset(frameLeft + safeFrameW, frameTop), Offset(frameLeft + safeFrameW, frameTop + bracketLen), 2.5f)
                    // Bottom-Left
                    drawLine(StudioPurpleLight, Offset(frameLeft, frameTop + safeFrameH), Offset(frameLeft + bracketLen, frameTop + safeFrameH), 2.5f)
                    drawLine(StudioPurpleLight, Offset(frameLeft, frameTop + safeFrameH), Offset(frameLeft, frameTop + safeFrameH - bracketLen), 2.5f)
                    // Bottom-Right
                    drawLine(StudioPurpleLight, Offset(frameLeft + safeFrameW, frameTop + safeFrameH), Offset(frameLeft + safeFrameW - bracketLen, frameTop + safeFrameH), 2.5f)
                    drawLine(StudioPurpleLight, Offset(frameLeft + safeFrameW, frameTop + safeFrameH), Offset(frameLeft + safeFrameW, frameTop + safeFrameH - bracketLen), 2.5f)
                }

                // D. Primary Coordinate Origin Axes (X: Red, Y: Green)
                // X Axis
                drawLine(
                    color = StudioRed.copy(alpha = 0.7f),
                    start = Offset(0f, centerY),
                    end = Offset(size.width, centerY),
                    strokeWidth = 1.2f
                )
                // Y Axis
                drawLine(
                    color = StudioGreen.copy(alpha = 0.7f),
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, size.height),
                    strokeWidth = 1.2f
                )

                // Origin Circle Marker
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = StudioPurple,
                    radius = 1.5.dp.toPx(),
                    center = Offset(centerX, centerY)
                )

                // E. Procedural Lighting 2D (Ambient Point Light Glow)
                if (showLighting) {
                    val lightPos = Offset(centerX + 80.dp.toPx(), centerY - 45.dp.toPx())
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                StudioOrange.copy(alpha = 0.35f),
                                StudioOrange.copy(alpha = 0.15f),
                                StudioYellow.copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            center = lightPos,
                            radius = 90.dp.toPx()
                        ),
                        radius = 90.dp.toPx(),
                        center = lightPos
                    )
                }

                // F. Procedural Game Entities Render
                // 1. Static Platforms (Tilemap Ground)
                drawProceduralPlatform(
                    topLeft = Offset(centerX - 140.dp.toPx(), centerY + 55.dp.toPx()),
                    width = 280.dp.toPx(),
                    height = 24.dp.toPx()
                )

                drawProceduralPlatform(
                    topLeft = Offset(centerX - 110.dp.toPx(), centerY + 5.dp.toPx()),
                    width = 80.dp.toPx(),
                    height = 14.dp.toPx()
                )

                drawProceduralPlatform(
                    topLeft = Offset(centerX + 30.dp.toPx(), centerY - 25.dp.toPx()),
                    width = 90.dp.toPx(),
                    height = 14.dp.toPx()
                )

                // 2. Enemy / Demon Patrol Token
                val enemyX = centerX - 70.dp.toPx()
                val enemyY = centerY + 36.dp.toPx()
                // Aggro radius circle
                drawCircle(
                    color = StudioRed.copy(alpha = 0.12f),
                    radius = 45.dp.toPx(),
                    center = Offset(enemyX, enemyY)
                )
                drawCircle(
                    color = StudioRed.copy(alpha = 0.4f),
                    radius = 45.dp.toPx(),
                    center = Offset(enemyX, enemyY),
                    style = Stroke(width = 0.8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f))
                )
                // Enemy Core
                drawCircle(
                    color = StudioRed,
                    radius = 8.dp.toPx(),
                    center = Offset(enemyX, enemyY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(enemyX, enemyY)
                )

                // 3. Camera Frustum (2D View Cone)
                val camX = centerX
                val camY = centerY - 10.dp.toPx()
                drawCircle(
                    color = StudioBlue.copy(alpha = 0.8f),
                    radius = 6.dp.toPx(),
                    center = Offset(camX, camY)
                )

                // G. Selected Object Transform Box & Gizmo
                selectedNode?.let { node ->
                    val objX = centerX + node.posX
                    val objY = centerY + node.posY
                    val boxW = 38.dp.toPx() * node.scale
                    val boxH = 38.dp.toPx() * node.scale

                    // Transform Bounding Box
                    drawRoundRect(
                        color = StudioPurpleLight,
                        topLeft = Offset(objX - boxW / 2f, objY - boxH / 2f),
                        size = Size(boxW, boxH),
                        cornerRadius = CornerRadius(3.dp.toPx()),
                        style = Stroke(width = 1.2f)
                    )

                    // Corner Resize Handles
                    val hSize = 4.dp.toPx()
                    listOf(
                        Offset(objX - boxW / 2f, objY - boxH / 2f),
                        Offset(objX + boxW / 2f, objY - boxH / 2f),
                        Offset(objX - boxW / 2f, objY + boxH / 2f),
                        Offset(objX + boxW / 2f, objY + boxH / 2f)
                    ).forEach { hPos ->
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(hPos.x - hSize / 2f, hPos.y - hSize / 2f),
                            size = Size(hSize, hSize)
                        )
                        drawRect(
                            color = StudioPurpleDark,
                            topLeft = Offset(hPos.x - hSize / 2f, hPos.y - hSize / 2f),
                            size = Size(hSize, hSize),
                            style = Stroke(0.8f)
                        )
                    }

                    // Move Gizmo Arrows (Red X Arrow & Green Y Arrow)
                    if (activeTool == 2 || activeTool == 1) {
                        val arrowLen = 32.dp.toPx()
                        // X Axis Arrow (Red)
                        drawLine(
                            color = StudioRed,
                            start = Offset(objX, objY),
                            end = Offset(objX + arrowLen, objY),
                            strokeWidth = 2.5f,
                            cap = StrokeCap.Round
                        )
                        drawCircle(StudioRed, 3.5.dp.toPx(), Offset(objX + arrowLen, objY))

                        // Y Axis Arrow (Green)
                        drawLine(
                            color = StudioGreen,
                            start = Offset(objX, objY),
                            end = Offset(objX, objY - arrowLen),
                            strokeWidth = 2.5f,
                            cap = StrokeCap.Round
                        )
                        drawCircle(StudioGreen, 3.5.dp.toPx(), Offset(objX, objY - arrowLen))
                    }
                }
            }

            // ========================================================
            // 4. Procedural Player Character Token
            // ========================================================
            selectedNode?.let { node ->
                val objX = centerX + node.posX
                val objY = centerY + node.posY

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (objX - 16.dp.toPx()).toInt(),
                                (objY - 16.dp.toPx()).toInt()
                            )
                        }
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(StudioPurpleLight, StudioPurple, StudioPurpleDark)
                            )
                        )
                        .border(1.2.dp, Color.White, CircleShape)
                        .shadow(6.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SelectAll,
                        contentDescription = "Player Character",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Floating Coordinates Tooltip above Selected Object
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (objX - 35.dp.toPx()).toInt(),
                                (objY - 34.dp.toPx()).toInt()
                            )
                        }
                        .clip(RoundedCornerShape(4.dp))
                        .background(EngineCardBg.copy(alpha = 0.92f))
                        .border(0.6.dp, StudioPurpleBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "${node.name} (${node.posX.toInt()}, ${node.posY.toInt()})",
                        color = StudioPurpleLight,
                        fontSize = 6.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ========================================================
            // 5. Floating Viewport Tool Palette (Top-Left overlay)
            // ========================================================
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(EngineCardBg.copy(alpha = 0.92f))
                    .border(0.5.dp, StudioBorder, RoundedCornerShape(6.dp))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pan Tool
                ViewportToolButton(
                    icon = Icons.Default.PanTool,
                    isSelected = activeTool == 0,
                    onClick = { activeTool = 0 },
                    testTag = "tool_pan"
                )
                // Select Tool
                ViewportToolButton(
                    icon = Icons.Default.CropFree,
                    isSelected = activeTool == 1,
                    onClick = { activeTool = 1 },
                    testTag = "tool_select"
                )
                // Move Gizmo
                ViewportToolButton(
                    icon = Icons.Default.OpenWith,
                    isSelected = activeTool == 2,
                    onClick = { activeTool = 2 },
                    testTag = "tool_move"
                )
                // Rotate Tool
                ViewportToolButton(
                    icon = Icons.Default.Refresh,
                    isSelected = activeTool == 3,
                    onClick = { activeTool = 3 },
                    testTag = "tool_rotate"
                )
                // Scale Tool
                ViewportToolButton(
                    icon = Icons.Default.Transform,
                    isSelected = activeTool == 4,
                    onClick = { activeTool = 4 },
                    testTag = "tool_scale"
                )

                Box(modifier = Modifier.width(0.6.dp).height(12.dp).background(StudioBorder))

                // Toggle Grid
                ViewportToolButton(
                    icon = Icons.Default.GridOn,
                    isSelected = showGrid,
                    onClick = { showGrid = !showGrid },
                    testTag = "tool_grid"
                )

                // Toggle Lighting
                ViewportToolButton(
                    icon = Icons.Default.WbSunny,
                    isSelected = showLighting,
                    onClick = { showLighting = !showLighting },
                    testTag = "tool_lighting"
                )
            }

            // ========================================================
            // 6. Viewport Telemetry Badge (Top-Right overlay)
            // ========================================================
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(EngineCardBg.copy(alpha = 0.9f))
                    .border(0.5.dp, StudioBorder, RoundedCornerShape(5.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "1920×1080 (16:9)",
                    color = TextPrimary,
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "60 FPS",
                    color = StudioGreen,
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Zoom: ${(zoomScale * 100).toInt()}%",
                    color = TextSecondary,
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // ========================================================
            // 7. Floating Restore Buttons for Collapsed Panels
            // ========================================================
            // Left Edge: Open Hierarchy Button
            if (!isHierarchyVisible) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 2.dp)
                        .clip(RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
                        .background(StudioPurpleDark)
                        .border(0.6.dp, StudioPurpleBorder, RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
                        .clickable { onRestoreHierarchy() }
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                        .testTag("restore_hierarchy_dock"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "فتح لوحة العناصر",
                            tint = StudioPurpleLight,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "TREE",
                            color = Color.White,
                            fontSize = 5.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Right Edge: Open Inspector Button
            if (!isInspectorVisible) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 2.dp)
                        .clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
                        .background(StudioPurpleDark)
                        .border(0.6.dp, StudioPurpleBorder, RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
                        .clickable { onRestoreInspector() }
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                        .testTag("restore_inspector_dock"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "فتح لوحة الخصائص",
                            tint = StudioPurpleLight,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "PROP",
                            color = Color.White,
                            fontSize = 5.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bottom Edge: Open Timeline Button
            if (!isTimelineVisible) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 2.dp)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(StudioPurpleDark)
                        .border(0.6.dp, StudioPurpleBorder, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .clickable { onRestoreTimeline() }
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                        .testTag("restore_timeline_dock"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = "فتح شريط الحركة",
                            tint = StudioPurpleLight,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "TIMELINE",
                            color = Color.White,
                            fontSize = 6.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawProceduralPlatform(
    topLeft: Offset,
    width: Float,
    height: Float
) {
    // Platform Solid Base
    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = topLeft,
        size = Size(width, height),
        cornerRadius = CornerRadius(2.dp.toPx())
    )

    // Platform Top Neon Grass/Ledge Highlight
    drawLine(
        color = StudioGreen,
        start = Offset(topLeft.x, topLeft.y),
        end = Offset(topLeft.x + width, topLeft.y),
        strokeWidth = 2.dp.toPx()
    )

    // Collision Box Outline
    drawRoundRect(
        color = StudioGreen.copy(alpha = 0.4f),
        topLeft = topLeft,
        size = Size(width, height),
        cornerRadius = CornerRadius(2.dp.toPx()),
        style = Stroke(width = 0.8f)
    )
}

@Composable
private fun ViewportToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(if (isSelected) StudioPurple else Color.Transparent)
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color.White else TextMuted,
            modifier = Modifier.size(11.dp)
        )
    }
}

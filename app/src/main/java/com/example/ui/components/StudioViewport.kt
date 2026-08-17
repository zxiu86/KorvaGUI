package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.FlipToFront
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.theme.EngineSurface
import com.example.ui.theme.StudioBlue
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioOrange
import com.example.ui.theme.StudioPink
import com.example.ui.theme.StudioPurple
import com.example.ui.theme.StudioPurpleDark
import com.example.ui.theme.StudioPurpleGlass
import com.example.ui.theme.StudioPurpleLight
import com.example.ui.theme.StudioRed
import com.example.ui.theme.StudioYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

enum class ViewportTool(val label: String, val shortcut: String) {
    SELECT("تحديد", "Q"),
    MOVE("تحريك", "W"),
    ROTATE("تدوير", "E"),
    SCALE("تكبير/تصغير", "R"),
    PAN("سحب المشهد", "H")
}

@Composable
fun StudioViewport(
    selectedNode: SceneNode?,
    allNodes: List<SceneNode> = emptyList(),
    isHierarchyVisible: Boolean = true,
    isInspectorVisible: Boolean = true,
    isTimelineVisible: Boolean = true,
    isPlaying: Boolean = false,
    onRestoreHierarchy: () -> Unit = {},
    onRestoreInspector: () -> Unit = {},
    onRestoreTimeline: () -> Unit = {},
    onNodeSelect: (nodeId: String) -> Unit = {},
    onNodeDrag: (dx: Float, dy: Float) -> Unit = { _, _ -> },
    onNodeExactPosChange: (x: Float, y: Float) -> Unit = { _, _ -> },
    onNodeExactScaleChange: (scale: Float) -> Unit = {},
    onNodeExactRotationChange: (rot: Float) -> Unit = {},
    onNodeDuplicate: (nodeId: String) -> Unit = {},
    onNodeDelete: (nodeId: String) -> Unit = {},
    onNodeBringToFront: (nodeId: String) -> Unit = {},
    onNodeSendToBack: (nodeId: String) -> Unit = {},
    onNodeCenter: (nodeId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Navigation & Camera State
    var cameraPanX by remember { mutableFloatStateOf(0f) }
    var cameraPanY by remember { mutableFloatStateOf(0f) }
    var cameraZoom by remember { mutableFloatStateOf(1.0f) }

    // Tools & Options
    var activeTool by remember { mutableStateOf(ViewportTool.MOVE) }
    var isGridVisible by remember { mutableStateOf(true) }
    var isGridSnapEnabled by remember { mutableStateOf(true) }
    var gridSizePx by remember { mutableIntStateOf(32) }
    var showColliders by remember { mutableStateOf(true) }
    var showLightingAmbiance by remember { mutableStateOf(true) }
    var showCameraSafeFrame by remember { mutableStateOf(true) }
    var showRulers by remember { mutableStateOf(true) }

    // Live Interaction & Telemetry
    var cursorWorldX by remember { mutableFloatStateOf(0f) }
    var cursorWorldY by remember { mutableFloatStateOf(0f) }
    var isDraggingNode by remember { mutableStateOf(false) }
    var simFrameTick by remember { mutableIntStateOf(0) }

    // Pulse / Simulation Tick Animation
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                delay(16) // ~60fps tick
                simFrameTick = (simFrameTick + 1) % 360
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(EngineBackground)
            .border(width = 1.dp, color = StudioBorder)
    ) {
        val viewportWidth = constraints.maxWidth.toFloat()
        val viewportHeight = constraints.maxHeight.toFloat()
        val centerX = viewportWidth / 2f + cameraPanX
        val centerY = viewportHeight / 2f + cameraPanY

        // Coordinate Conversion Helpers
        fun worldToScreen(worldX: Float, worldY: Float): Offset {
            return Offset(
                x = centerX + (worldX * cameraZoom),
                y = centerY + (worldY * cameraZoom)
            )
        }

        fun screenToWorld(screenX: Float, screenY: Float): Offset {
            val wx = (screenX - centerX) / cameraZoom
            val wy = (screenY - centerY) / cameraZoom
            return Offset(wx, wy)
        }

        // =========================================================================
        // 1. High Performance 2D Engine Canvas (Grid, Nodes, Gizmos, Lighting)
        // =========================================================================
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(activeTool, cameraZoom, cameraPanX, cameraPanY, selectedNode, allNodes, isGridSnapEnabled, gridSizePx) {
                    // Tap to Select / Inspect
                    detectTapGestures { tapOffset ->
                        val worldPos = screenToWorld(tapOffset.x, tapOffset.y)
                        cursorWorldX = worldPos.x
                        cursorWorldY = worldPos.y

                        // Hit-test nodes in reverse order (topmost layer first)
                        var clickedNode: SceneNode? = null
                        for (node in allNodes.reversed()) {
                            if (!node.isVisible) continue
                            val nodeScreen = worldToScreen(node.posX, node.posY)
                            val baseRadius = 26f * node.scale * cameraZoom
                            val touchRadius = baseRadius.coerceAtLeast(32f)

                            val distSq = (tapOffset.x - nodeScreen.x) * (tapOffset.x - nodeScreen.x) +
                                    (tapOffset.y - nodeScreen.y) * (tapOffset.y - nodeScreen.y)

                            if (distSq <= touchRadius * touchRadius) {
                                clickedNode = node
                                break
                            }
                        }

                        if (clickedNode != null) {
                            onNodeSelect(clickedNode.id)
                        }
                    }
                }
                .pointerInput(activeTool, cameraZoom, cameraPanX, cameraPanY, selectedNode, isGridSnapEnabled, gridSizePx) {
                    // Interactive Dragging / Rotating / Scaling / Panning
                    detectDragGestures(
                        onDragStart = { startOffset ->
                            val worldPos = screenToWorld(startOffset.x, startOffset.y)
                            cursorWorldX = worldPos.x
                            cursorWorldY = worldPos.y
                            isDraggingNode = true
                        },
                        onDragEnd = {
                            isDraggingNode = false
                        },
                        onDragCancel = {
                            isDraggingNode = false
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val curWorld = screenToWorld(change.position.x, change.position.y)
                            cursorWorldX = curWorld.x
                            cursorWorldY = curWorld.y

                            when (activeTool) {
                                ViewportTool.PAN -> {
                                    cameraPanX += dragAmount.x
                                    cameraPanY += dragAmount.y
                                }
                                ViewportTool.MOVE, ViewportTool.SELECT -> {
                                    if (selectedNode != null) {
                                        val deltaWorldX = dragAmount.x / cameraZoom
                                        val deltaWorldY = dragAmount.y / cameraZoom

                                        if (isGridSnapEnabled) {
                                            val targetX = selectedNode.posX + deltaWorldX
                                            val targetY = selectedNode.posY + deltaWorldY
                                            val snappedX = (targetX / gridSizePx).roundToInt() * gridSizePx.toFloat()
                                            val snappedY = (targetY / gridSizePx).roundToInt() * gridSizePx.toFloat()
                                            onNodeExactPosChange(snappedX, snappedY)
                                        } else {
                                            onNodeDrag(deltaWorldX, deltaWorldY)
                                        }
                                    } else {
                                        // If no node selected, pan canvas
                                        cameraPanX += dragAmount.x
                                        cameraPanY += dragAmount.y
                                    }
                                }
                                ViewportTool.ROTATE -> {
                                    if (selectedNode != null) {
                                        val nodeScreen = worldToScreen(selectedNode.posX, selectedNode.posY)
                                        val prevAngle = atan2(
                                            change.previousPosition.y - nodeScreen.y,
                                            change.previousPosition.x - nodeScreen.x
                                        ) * (180f / PI.toFloat())
                                        val curAngle = atan2(
                                            change.position.y - nodeScreen.y,
                                            change.position.x - nodeScreen.x
                                        ) * (180f / PI.toFloat())
                                        val diff = curAngle - prevAngle
                                        val newRot = (selectedNode.rotation + diff) % 360f
                                        onNodeExactRotationChange(newRot)
                                    }
                                }
                                ViewportTool.SCALE -> {
                                    if (selectedNode != null) {
                                        val nodeScreen = worldToScreen(selectedNode.posX, selectedNode.posY)
                                        val prevDist = sqrt(
                                            (change.previousPosition.x - nodeScreen.x) * (change.previousPosition.x - nodeScreen.x) +
                                                    (change.previousPosition.y - nodeScreen.y) * (change.previousPosition.y - nodeScreen.y)
                                        )
                                        val curDist = sqrt(
                                            (change.position.x - nodeScreen.x) * (change.position.x - nodeScreen.x) +
                                                    (change.position.y - nodeScreen.y) * (change.position.y - nodeScreen.y)
                                        )
                                        if (prevDist > 1f) {
                                            val scaleFactor = curDist / prevDist
                                            val newScale = (selectedNode.scale * scaleFactor).coerceIn(0.2f, 6.0f)
                                            onNodeExactScaleChange(newScale)
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            // Draw 2D Infinite Cartesian Grid
            if (isGridVisible) {
                draw2DGrid(
                    centerX = centerX,
                    centerY = centerY,
                    zoom = cameraZoom,
                    gridSizePx = gridSizePx,
                    viewportWidth = size.width,
                    viewportHeight = size.height
                )
            }

            // Draw World Coordinate Axes (X in Red, Y in Green)
            draw2DWorldAxes(centerX, centerY, size.width, size.height, cameraZoom)

            // Draw 2D Camera Safe Frame (16:9 Ratio boundary)
            if (showCameraSafeFrame) {
                draw2DCameraSafeFrame(centerX, centerY, cameraZoom)
            }

            // Draw 2D Ambient Shadows / Vignette preview if enabled
            if (showLightingAmbiance && !isPlaying) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color(0x33000000), Color(0x66000000)),
                        center = Offset(centerX, centerY),
                        radius = (size.width * 0.7f) * cameraZoom
                    ),
                    size = size
                )
            }

            // Render all 2D Scene Nodes
            allNodes.forEach { node ->
                if (node.isVisible) {
                    val isSelected = selectedNode?.id == node.id
                    draw2DSceneNode(
                        node = node,
                        centerX = centerX,
                        centerY = centerY,
                        zoom = cameraZoom,
                        isSelected = isSelected,
                        isPlaying = isPlaying,
                        simTick = simFrameTick,
                        showColliders = showColliders
                    )
                }
            }

            // Draw Active Transform Gizmo over Selected Node
            if (selectedNode != null && selectedNode.isVisible && !isPlaying) {
                val nodePos = worldToScreen(selectedNode.posX, selectedNode.posY)
                draw2DTransformGizmo(
                    node = selectedNode,
                    screenPos = nodePos,
                    tool = activeTool,
                    zoom = cameraZoom,
                    isDragging = isDraggingNode
                )
            }

            // Draw Top and Left Pixel Measurement Rulers
            if (showRulers) {
                draw2DRulers(
                    centerX = centerX,
                    centerY = centerY,
                    zoom = cameraZoom,
                    width = size.width,
                    height = size.height
                )
            }
        }

        // =========================================================================
        // 2. Top-Left: Modern 2D Engine Toolbar (Tools, Grid, Snapping, Camera)
        // =========================================================================
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 10.dp, start = 12.dp)
                .shadow(12.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(EngineSurface.copy(alpha = 0.94f))
                .border(0.8.dp, StudioBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Tool 1: Pointer / Select (Q)
            ViewportToolButton(
                icon = Icons.Default.NearMe,
                tooltip = "تحديد (Select)",
                isActive = activeTool == ViewportTool.SELECT,
                shortcut = "Q",
                onClick = { activeTool = ViewportTool.SELECT }
            )

            // Tool 2: Move (W)
            ViewportToolButton(
                icon = Icons.Default.OpenWith,
                tooltip = "تحريك (Move)",
                isActive = activeTool == ViewportTool.MOVE,
                shortcut = "W",
                onClick = { activeTool = ViewportTool.MOVE }
            )

            // Tool 3: Rotate (E)
            ViewportToolButton(
                icon = Icons.Default.RotateRight,
                tooltip = "تدوير (Rotate)",
                isActive = activeTool == ViewportTool.ROTATE,
                shortcut = "E",
                onClick = { activeTool = ViewportTool.ROTATE }
            )

            // Tool 4: Scale (R)
            ViewportToolButton(
                icon = Icons.Default.Transform,
                tooltip = "تغيير الحجم (Scale)",
                isActive = activeTool == ViewportTool.SCALE,
                shortcut = "R",
                onClick = { activeTool = ViewportTool.SCALE }
            )

            // Tool 5: Pan (H)
            ViewportToolButton(
                icon = Icons.Default.PanTool,
                tooltip = "تحريك المشهد (Hand)",
                isActive = activeTool == ViewportTool.PAN,
                shortcut = "H",
                onClick = { activeTool = ViewportTool.PAN }
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(StudioBorder)
                    .padding(horizontal = 2.dp)
            )

            // Grid Toggle
            ViewportToggleButton(
                icon = Icons.Default.GridOn,
                tooltip = "الشبكة",
                isActive = isGridVisible,
                onClick = { isGridVisible = !isGridVisible }
            )

            // Grid Snap Toggle & Size Cycle (16 -> 32 -> 64)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isGridSnapEnabled) StudioPurpleDark else Color.Transparent)
                    .border(
                        0.6.dp,
                        if (isGridSnapEnabled) StudioPurpleLight else Color.Transparent,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable {
                        if (isGridSnapEnabled) {
                            gridSizePx = when (gridSizePx) {
                                16 -> 32
                                32 -> 64
                                else -> 16
                            }
                        } else {
                            isGridSnapEnabled = true
                        }
                    }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "المحاذاة للشبكة",
                        tint = if (isGridSnapEnabled) StudioPurpleLight else TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isGridSnapEnabled) "${gridSizePx}px" else "حر",
                        color = if (isGridSnapEnabled) TextPrimary else TextMuted,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // 2D Colliders Toggle
            ViewportToggleButton(
                icon = Icons.Default.Science,
                tooltip = "حدود التصادم 2D (Colliders)",
                isActive = showColliders,
                onClick = { showColliders = !showColliders }
            )

            // 2D Lighting Ambiance Toggle
            ViewportToggleButton(
                icon = Icons.Default.Lightbulb,
                tooltip = "إضاءة وتظليل 2D",
                isActive = showLightingAmbiance,
                onClick = { showLightingAmbiance = !showLightingAmbiance }
            )

            // 2D Safe Frame Toggle
            ViewportToggleButton(
                icon = Icons.Default.CropFree,
                tooltip = "إطار الكاميرا (16:9)",
                isActive = showCameraSafeFrame,
                onClick = { showCameraSafeFrame = !showCameraSafeFrame }
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(StudioBorder)
            )

            // Reset Camera Pan & Zoom to Origin (0,0)
            IconButton(
                onClick = {
                    cameraPanX = 0f
                    cameraPanY = 0f
                    cameraZoom = 1.0f
                },
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "إعادة ضبط الكاميرا",
                    tint = TextSecondary,
                    modifier = Modifier.size(15.dp)
                )
            }

            // Center on Selected Object
            if (selectedNode != null) {
                IconButton(
                    onClick = {
                        cameraPanX = -selectedNode.posX * cameraZoom
                        cameraPanY = -selectedNode.posY * cameraZoom
                    },
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CenterFocusStrong,
                        contentDescription = "التركيز على الكائن المحدد",
                        tint = StudioPurpleLight,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        // =========================================================================
        // 3. Top-Right: Live 2D Engine Telemetry & Cursor HUD
        // =========================================================================
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 12.dp)
                .shadow(12.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(EngineSurface.copy(alpha = 0.94f))
                .border(0.8.dp, StudioBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Engine 2D Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(StudioPurpleDark)
                    .border(0.6.dp, StudioPurpleLight.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "PURE 2D SCENE",
                    color = StudioPurpleLight,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Cursor World Coordinates
            Text(
                text = "X: ${cursorWorldX.roundToInt()}  Y: ${cursorWorldY.roundToInt()}",
                color = TextSecondary,
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )

            // Zoom Badge
            Text(
                text = "${(cameraZoom * 100).roundToInt()}%",
                color = StudioBlue,
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            // FPS Counter / Simulation Indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) StudioGreen else StudioOrange)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isPlaying) "60 FPS" else "EDIT 2D",
                    color = if (isPlaying) StudioGreen else TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // =========================================================================
        // 4. Bottom-Start: Zoom Control Stepper (- / +)
        // =========================================================================
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 12.dp, start = 12.dp)
                .shadow(10.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(EngineSurface.copy(alpha = 0.94f))
                .border(0.8.dp, StudioBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { cameraZoom = (cameraZoom - 0.15f).coerceIn(0.3f, 4.0f) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "تصغير", tint = TextPrimary, modifier = Modifier.size(13.dp))
            }

            Text(
                text = "${(cameraZoom * 100).roundToInt()}%",
                color = TextPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clickable { cameraZoom = 1.0f }
            )

            IconButton(
                onClick = { cameraZoom = (cameraZoom + 0.15f).coerceIn(0.3f, 4.0f) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "تكبير", tint = TextPrimary, modifier = Modifier.size(13.dp))
            }
        }

        // =========================================================================
        // 5. Bottom-Center: Sleek Direct On-Canvas Floating Action Bar for Selected Node
        // =========================================================================
        AnimatedVisibility(
            visible = selectedNode != null && !isPlaying,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        ) {
            selectedNode?.let { node ->
                Row(
                    modifier = Modifier
                        .shadow(16.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(EngineSurface.copy(alpha = 0.96f))
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                listOf(StudioPurpleLight.copy(alpha = 0.7f), StudioBorder)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Node Type & Name Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StudioPurpleDark)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(StudioPurpleLight)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = node.name,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Duplicate Node
                    QuickActionButton(
                        icon = Icons.Default.ContentCopy,
                        tooltip = "استنساخ (Duplicate)",
                        tint = StudioPurpleLight,
                        onClick = { onNodeDuplicate(node.id) }
                    )

                    // Center to (0,0)
                    QuickActionButton(
                        icon = Icons.Default.LocationSearching,
                        tooltip = "وضع في المركز (0,0)",
                        tint = StudioBlue,
                        onClick = { onNodeCenter(node.id) }
                    )

                    // Bring to Front
                    QuickActionButton(
                        icon = Icons.Default.FlipToFront,
                        tooltip = "تقديم للأمام",
                        tint = TextSecondary,
                        onClick = { onNodeBringToFront(node.id) }
                    )

                    // Send to Back
                    QuickActionButton(
                        icon = Icons.Default.FlipToBack,
                        tooltip = "إرجاع للخلف",
                        tint = TextSecondary,
                        onClick = { onNodeSendToBack(node.id) }
                    )

                    // Delete Node
                    QuickActionButton(
                        icon = Icons.Default.Delete,
                        tooltip = "حذف الكائن",
                        tint = StudioRed,
                        onClick = { onNodeDelete(node.id) }
                    )
                }
            }
        }

        // =========================================================================
        // 6. Floating Panel Restore Chips (When Hierarchy/Inspector/Timeline are Hidden)
        // =========================================================================
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (!isHierarchyVisible) {
                RestorePanelChip(
                    icon = Icons.Default.Layers,
                    label = "العناصر",
                    onClick = onRestoreHierarchy
                )
            }

            if (!isInspectorVisible) {
                RestorePanelChip(
                    icon = Icons.Default.Tune,
                    label = "الخصائص",
                    onClick = onRestoreInspector
                )
            }

            if (!isTimelineVisible) {
                RestorePanelChip(
                    icon = Icons.Default.Timeline,
                    label = "الأنيميشن",
                    onClick = onRestoreTimeline
                )
            }
        }
    }
}

// =============================================================================
// Helper Composables for StudioViewport
// =============================================================================

@Composable
private fun ViewportToolButton(
    icon: ImageVector,
    tooltip: String,
    isActive: Boolean,
    shortcut: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) StudioPurple else Color.Transparent)
            .border(
                0.6.dp,
                if (isActive) StudioPurpleLight else Color.Transparent,
                RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .testTag("viewport_tool_${shortcut.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tooltip,
            tint = if (isActive) Color.White else TextMuted,
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
private fun ViewportToggleButton(
    icon: ImageVector,
    tooltip: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) StudioPurpleDark.copy(alpha = 0.7f) else Color.Transparent)
            .border(
                0.6.dp,
                if (isActive) StudioPurpleLight.copy(alpha = 0.5f) else Color.Transparent,
                RoundedCornerShape(6.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tooltip,
            tint = if (isActive) StudioPurpleLight else TextMuted,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    tooltip: String,
    tint: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(EngineCardBg)
            .border(0.6.dp, StudioBorder, RoundedCornerShape(6.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tooltip,
            tint = tint,
            modifier = Modifier.size(13.dp)
        )
    }
}

@Composable
private fun RestorePanelChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .shadow(8.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(EngineSurface.copy(alpha = 0.95f))
            .border(0.8.dp, StudioPurpleLight.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// =============================================================================
// Custom 2D Graphics Drawing Functions
// =============================================================================

private fun DrawScope.draw2DGrid(
    centerX: Float,
    centerY: Float,
    zoom: Float,
    gridSizePx: Int,
    viewportWidth: Float,
    viewportHeight: Float
) {
    val scaledGrid = gridSizePx * zoom
    if (scaledGrid < 8f) return

    val minorColor = Color(0x1A8B5CF6)
    val majorColor = Color(0x338B5CF6)

    // Vertical Lines
    val startX = (centerX % scaledGrid)
    var x = startX
    var colIndex = 0
    while (x < viewportWidth) {
        val isMajor = (colIndex % 4 == 0)
        drawLine(
            color = if (isMajor) majorColor else minorColor,
            start = Offset(x, 0f),
            end = Offset(x, viewportHeight),
            strokeWidth = if (isMajor) 1.0f else 0.5f
        )
        x += scaledGrid
        colIndex++
    }

    // Horizontal Lines
    val startY = (centerY % scaledGrid)
    var y = startY
    var rowIndex = 0
    while (y < viewportHeight) {
        val isMajor = (rowIndex % 4 == 0)
        drawLine(
            color = if (isMajor) majorColor else minorColor,
            start = Offset(0f, y),
            end = Offset(viewportWidth, y),
            strokeWidth = if (isMajor) 1.0f else 0.5f
        )
        y += scaledGrid
        rowIndex++
    }
}

private fun DrawScope.draw2DWorldAxes(
    centerX: Float,
    centerY: Float,
    viewportWidth: Float,
    viewportHeight: Float,
    zoom: Float
) {
    val xColor = Color(0xFFEF4444) // Red X-Axis
    val yColor = Color(0xFF22C55E) // Green Y-Axis

    // X-Axis (Horizontal)
    drawLine(
        color = xColor.copy(alpha = 0.7f),
        start = Offset(0f, centerY),
        end = Offset(viewportWidth, centerY),
        strokeWidth = 1.5f
    )

    // Y-Axis (Vertical)
    drawLine(
        color = yColor.copy(alpha = 0.7f),
        start = Offset(centerX, 0f),
        end = Offset(centerX, viewportHeight),
        strokeWidth = 1.5f
    )

    // Origin Circle (0,0)
    drawCircle(
        color = StudioPurpleLight,
        radius = 4f * zoom.coerceIn(0.8f, 1.5f),
        center = Offset(centerX, centerY)
    )
    drawCircle(
        color = Color.White,
        radius = 2f,
        center = Offset(centerX, centerY)
    )
}

private fun DrawScope.draw2DCameraSafeFrame(
    centerX: Float,
    centerY: Float,
    zoom: Float
) {
    // 16:9 Frame Representation (e.g. 640x360 scaled)
    val baseW = 540f * zoom
    val baseH = 304f * zoom
    val left = centerX - baseW / 2f
    val top = centerY - baseH / 2f

    // Dotted Border
    drawRoundRect(
        color = StudioPurpleLight.copy(alpha = 0.5f),
        topLeft = Offset(left, top),
        size = Size(baseW, baseH),
        cornerRadius = CornerRadius(4f, 4f),
        style = Stroke(
            width = 1.2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
        )
    )

    // Corner L-Marks
    val cornerLen = 14f * zoom
    val cornerColor = StudioPurpleLight

    // Top-Left
    drawLine(cornerColor, Offset(left, top), Offset(left + cornerLen, top), 2f)
    drawLine(cornerColor, Offset(left, top), Offset(left, top + cornerLen), 2f)

    // Top-Right
    drawLine(cornerColor, Offset(left + baseW, top), Offset(left + baseW - cornerLen, top), 2f)
    drawLine(cornerColor, Offset(left + baseW, top), Offset(left + baseW, top + cornerLen), 2f)

    // Bottom-Left
    drawLine(cornerColor, Offset(left, top + baseH), Offset(left + cornerLen, top + baseH), 2f)
    drawLine(cornerColor, Offset(left, top + baseH), Offset(left, top + baseH - cornerLen), 2f)

    // Bottom-Right
    drawLine(cornerColor, Offset(left + baseW, top + baseH), Offset(left + baseW - cornerLen, top + baseH), 2f)
    drawLine(cornerColor, Offset(left + baseW, top + baseH), Offset(left + baseW, top + baseH - cornerLen), 2f)
}

private fun DrawScope.draw2DSceneNode(
    node: SceneNode,
    centerX: Float,
    centerY: Float,
    zoom: Float,
    isSelected: Boolean,
    isPlaying: Boolean,
    simTick: Int,
    showColliders: Boolean
) {
    // Simulation pulse offset
    var simYOffset = 0f
    if (isPlaying && node.hasPhysics) {
        simYOffset = sin(simTick * 0.1f + node.posX.toDouble() * 0.05).toFloat() * 6f * zoom
    }

    val screenX = centerX + (node.posX * zoom)
    val screenY = centerY + (node.posY * zoom) + simYOffset
    val baseScale = node.scale * zoom
    val nodeRadius = 24f * baseScale

    rotate(degrees = node.rotation, pivot = Offset(screenX, screenY)) {
        when (node.type) {
            NodeType.PLAYER -> {
                draw2DPlayerSprite(screenX, screenY, baseScale, isPlaying, simTick)
            }
            NodeType.ENEMY -> {
                draw2DEnemySprite(screenX, screenY, baseScale, isPlaying, simTick)
            }
            NodeType.PLATFORM -> {
                draw2DPlatformTile(screenX, screenY, baseScale)
            }
            NodeType.LIGHT -> {
                draw2DPointLight(screenX, screenY, baseScale, simTick)
            }
            NodeType.PARTICLE_SYSTEM -> {
                draw2DParticleEmitter(screenX, screenY, baseScale, simTick)
            }
            NodeType.CAMERA -> {
                draw2DCameraIcon(screenX, screenY, baseScale)
            }
            NodeType.SPRITE_OBJECT, NodeType.UI_CANVAS, NodeType.AUDIO_SOURCE -> {
                draw2DGenericSprite(screenX, screenY, baseScale, node.name, node.colorHex)
            }
        }
    }

    // 2D Physics Collider Outline
    if (showColliders) {
        drawRoundRect(
            color = if (node.hasPhysics) StudioGreen.copy(alpha = 0.7f) else StudioBlue.copy(alpha = 0.4f),
            topLeft = Offset(screenX - nodeRadius * 1.1f, screenY - nodeRadius * 1.1f),
            size = Size(nodeRadius * 2.2f, nodeRadius * 2.2f),
            cornerRadius = CornerRadius(4f, 4f),
            style = Stroke(
                width = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
            )
        )
    }

    // Selection Halo & Bounding Box
    if (isSelected) {
        drawRoundRect(
            color = StudioPurpleLight,
            topLeft = Offset(screenX - nodeRadius * 1.35f, screenY - nodeRadius * 1.35f),
            size = Size(nodeRadius * 2.7f, nodeRadius * 2.7f),
            cornerRadius = CornerRadius(6f, 6f),
            style = Stroke(width = 1.6f)
        )

        // Corner Resize Handles
        val handleSize = 6f * zoom.coerceIn(0.8f, 1.4f)
        val bLeft = screenX - nodeRadius * 1.35f
        val bTop = screenY - nodeRadius * 1.35f
        val bRight = bLeft + nodeRadius * 2.7f
        val bBottom = bTop + nodeRadius * 2.7f

        val handlePoints = listOf(
            Offset(bLeft, bTop),
            Offset(bRight, bTop),
            Offset(bLeft, bBottom),
            Offset(bRight, bBottom)
        )
        handlePoints.forEach { pt ->
            drawRect(
                color = Color.White,
                topLeft = Offset(pt.x - handleSize / 2f, pt.y - handleSize / 2f),
                size = Size(handleSize, handleSize)
            )
            drawRect(
                color = StudioPurpleDark,
                topLeft = Offset(pt.x - handleSize / 2f, pt.y - handleSize / 2f),
                size = Size(handleSize, handleSize),
                style = Stroke(1f)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// Detailed 2D Procedural Sprites
// -----------------------------------------------------------------------------

private fun DrawScope.draw2DPlayerSprite(
    x: Float,
    y: Float,
    scale: Float,
    isPlaying: Boolean,
    simTick: Int
) {
    val bodyW = 26f * scale
    val bodyH = 34f * scale

    // Drop Shadow on Floor
    drawOval(
        color = Color(0x44000000),
        topLeft = Offset(x - bodyW * 0.7f, y + bodyH * 0.4f),
        size = Size(bodyW * 1.4f, bodyH * 0.35f)
    )

    // Knight Cape (Purple Gradient)
    val capePath = Path().apply {
        moveTo(x - bodyW * 0.4f, y - bodyH * 0.2f)
        lineTo(x - bodyW * 0.8f, y + bodyH * 0.45f)
        lineTo(x, y + bodyH * 0.4f)
        close()
    }
    drawPath(capePath, color = Color(0xFF6D28D9))

    // Armor Body (Studio Purple / Titanium)
    drawRoundRect(
        color = StudioPurple,
        topLeft = Offset(x - bodyW / 2f, y - bodyH / 2f),
        size = Size(bodyW, bodyH),
        cornerRadius = CornerRadius(6f * scale, 6f * scale)
    )
    drawRoundRect(
        color = StudioPurpleLight,
        topLeft = Offset(x - bodyW / 2f, y - bodyH / 2f),
        size = Size(bodyW, bodyH),
        cornerRadius = CornerRadius(6f * scale, 6f * scale),
        style = Stroke(1.2f)
    )

    // Golden Crest / Belt
    drawRect(
        color = StudioYellow,
        topLeft = Offset(x - bodyW * 0.4f, y),
        size = Size(bodyW * 0.8f, 4f * scale)
    )

    // Knight Visor / Eyes (Cyan Glow)
    drawRoundRect(
        color = StudioBlue,
        topLeft = Offset(x - bodyW * 0.3f, y - bodyH * 0.35f),
        size = Size(bodyW * 0.6f, 5f * scale),
        cornerRadius = CornerRadius(2f, 2f)
    )

    // Glowing Sword (Held on Right)
    drawLine(
        color = StudioBlue,
        start = Offset(x + bodyW * 0.6f, y - bodyH * 0.4f),
        end = Offset(x + bodyW * 0.6f, y + bodyH * 0.2f),
        strokeWidth = 3f * scale,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.draw2DEnemySprite(
    x: Float,
    y: Float,
    scale: Float,
    isPlaying: Boolean,
    simTick: Int
) {
    val bodyW = 28f * scale
    val bodyH = 28f * scale

    // Drop Shadow
    drawOval(
        color = Color(0x44000000),
        topLeft = Offset(x - bodyW * 0.6f, y + bodyH * 0.4f),
        size = Size(bodyW * 1.2f, bodyH * 0.3f)
    )

    // Hostile Body (Fiery Red)
    drawRoundRect(
        color = StudioRed,
        topLeft = Offset(x - bodyW / 2f, y - bodyH / 2f),
        size = Size(bodyW, bodyH),
        cornerRadius = CornerRadius(8f * scale, 8f * scale)
    )
    drawRoundRect(
        color = Color(0xFFFCA5A5),
        topLeft = Offset(x - bodyW / 2f, y - bodyH / 2f),
        size = Size(bodyW, bodyH),
        cornerRadius = CornerRadius(8f * scale, 8f * scale),
        style = Stroke(1.2f)
    )

    // Horns
    val hornL = Path().apply {
        moveTo(x - bodyW * 0.3f, y - bodyH / 2f)
        lineTo(x - bodyW * 0.45f, y - bodyH * 0.8f)
        lineTo(x - bodyW * 0.15f, y - bodyH / 2f)
        close()
    }
    drawPath(hornL, color = Color(0xFF991B1B))

    val hornR = Path().apply {
        moveTo(x + bodyW * 0.3f, y - bodyH / 2f)
        lineTo(x + bodyW * 0.45f, y - bodyH * 0.8f)
        lineTo(x + bodyW * 0.15f, y - bodyH / 2f)
        close()
    }
    drawPath(hornR, color = Color(0xFF991B1B))

    // Angry Glowing Yellow Eyes
    drawCircle(StudioYellow, radius = 2.5f * scale, center = Offset(x - bodyW * 0.2f, y - bodyH * 0.1f))
    drawCircle(StudioYellow, radius = 2.5f * scale, center = Offset(x + bodyW * 0.2f, y - bodyH * 0.1f))
}

private fun DrawScope.draw2DPlatformTile(
    x: Float,
    y: Float,
    scale: Float
) {
    val tileW = 90f * scale
    val tileH = 24f * scale

    // Stone Base
    drawRoundRect(
        color = Color(0xFF334155),
        topLeft = Offset(x - tileW / 2f, y - tileH / 2f),
        size = Size(tileW, tileH),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // Lush Green Top Grass Layer
    drawRoundRect(
        color = StudioGreen,
        topLeft = Offset(x - tileW / 2f, y - tileH / 2f),
        size = Size(tileW, 6f * scale),
        cornerRadius = CornerRadius(3f, 3f)
    )

    // Stone Brick Seams
    drawLine(
        color = Color(0xFF1E293B),
        start = Offset(x - tileW * 0.2f, y - tileH / 2f + 6f * scale),
        end = Offset(x - tileW * 0.2f, y + tileH / 2f),
        strokeWidth = 1.2f
    )
    drawLine(
        color = Color(0xFF1E293B),
        start = Offset(x + tileW * 0.25f, y - tileH / 2f + 6f * scale),
        end = Offset(x + tileW * 0.25f, y + tileH / 2f),
        strokeWidth = 1.2f
    )
}

private fun DrawScope.draw2DPointLight(
    x: Float,
    y: Float,
    scale: Float,
    simTick: Int
) {
    val radius = 60f * scale
    // Radial Glow Aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(StudioYellow.copy(alpha = 0.5f), StudioOrange.copy(alpha = 0.2f), Color.Transparent),
            center = Offset(x, y),
            radius = radius
        ),
        radius = radius,
        center = Offset(x, y)
    )

    // Bulb Icon Center
    drawCircle(StudioYellow, radius = 7f * scale, center = Offset(x, y))
    drawCircle(Color.White, radius = 4f * scale, center = Offset(x, y))
}

private fun DrawScope.draw2DParticleEmitter(
    x: Float,
    y: Float,
    scale: Float,
    simTick: Int
) {
    // Center Emitter
    drawCircle(StudioPink, radius = 6f * scale, center = Offset(x, y))

    // Orbiting Glowing Sparks
    for (i in 0..4) {
        val angle = (simTick * 3f + i * 72f) * (PI.toFloat() / 180f)
        val dist = (16f + (i % 2) * 8f) * scale
        val px = x + cos(angle) * dist
        val py = y + sin(angle) * dist
        drawCircle(StudioPurpleLight, radius = 2.5f * scale, center = Offset(px, py))
    }
}

private fun DrawScope.draw2DCameraIcon(
    x: Float,
    y: Float,
    scale: Float
) {
    val camW = 28f * scale
    val camH = 18f * scale

    drawRoundRect(
        color = StudioBlue,
        topLeft = Offset(x - camW / 2f, y - camH / 2f),
        size = Size(camW, camH),
        cornerRadius = CornerRadius(3f, 3f)
    )

    // Lens Projection Cone
    val lensPath = Path().apply {
        moveTo(x + camW / 2f, y - camH * 0.3f)
        lineTo(x + camW * 0.85f, y - camH * 0.6f)
        lineTo(x + camW * 0.85f, y + camH * 0.6f)
        lineTo(x + camW / 2f, y + camH * 0.3f)
        close()
    }
    drawPath(lensPath, color = StudioBlue)
}

private fun DrawScope.draw2DGenericSprite(
    x: Float,
    y: Float,
    scale: Float,
    name: String,
    colorHex: String
) {
    val sizePx = 28f * scale
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        StudioPurpleLight
    }

    drawRoundRect(
        color = parsedColor,
        topLeft = Offset(x - sizePx / 2f, y - sizePx / 2f),
        size = Size(sizePx, sizePx),
        cornerRadius = CornerRadius(6f * scale, 6f * scale)
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.5f),
        topLeft = Offset(x - sizePx / 2f, y - sizePx / 2f),
        size = Size(sizePx, sizePx),
        cornerRadius = CornerRadius(6f * scale, 6f * scale),
        style = Stroke(1f)
    )
}

// -----------------------------------------------------------------------------
// Transform Gizmo System (Move, Rotate, Scale)
// -----------------------------------------------------------------------------

private fun DrawScope.draw2DTransformGizmo(
    node: SceneNode,
    screenPos: Offset,
    tool: ViewportTool,
    zoom: Float,
    isDragging: Boolean
) {
    val gizmoLen = 50f * zoom.coerceIn(0.9f, 1.3f)
    val redX = Color(0xFFEF4444)
    val greenY = Color(0xFF22C55E)

    when (tool) {
        ViewportTool.MOVE, ViewportTool.SELECT -> {
            // X Arrow (Red - Right)
            drawLine(
                color = redX,
                start = screenPos,
                end = Offset(screenPos.x + gizmoLen, screenPos.y),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
            // X Arrowhead
            val xHead = Path().apply {
                moveTo(screenPos.x + gizmoLen + 6f, screenPos.y)
                lineTo(screenPos.x + gizmoLen - 4f, screenPos.y - 4f)
                lineTo(screenPos.x + gizmoLen - 4f, screenPos.y + 4f)
                close()
            }
            drawPath(xHead, color = redX)

            // Y Arrow (Green - Up/Down)
            drawLine(
                color = greenY,
                start = screenPos,
                end = Offset(screenPos.x, screenPos.y + gizmoLen),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
            // Y Arrowhead
            val yHead = Path().apply {
                moveTo(screenPos.x, screenPos.y + gizmoLen + 6f)
                lineTo(screenPos.x - 4f, screenPos.y + gizmoLen - 4f)
                lineTo(screenPos.x + 4f, screenPos.y + gizmoLen - 4f)
                close()
            }
            drawPath(yHead, color = greenY)

            // Free Move Center Box
            drawRect(
                color = StudioYellow,
                topLeft = Offset(screenPos.x + 4f, screenPos.y + 4f),
                size = Size(10f, 10f)
            )
        }
        ViewportTool.ROTATE -> {
            val ringRadius = 45f * zoom.coerceIn(0.9f, 1.3f)
            drawCircle(
                color = StudioPurpleLight,
                radius = ringRadius,
                center = screenPos,
                style = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                )
            )
            // Active Handle dot
            val rad = node.rotation * (PI.toFloat() / 180f)
            val hx = screenPos.x + cos(rad) * ringRadius
            val hy = screenPos.y + sin(rad) * ringRadius
            drawCircle(Color.White, radius = 5f, center = Offset(hx, hy))
            drawCircle(StudioPurple, radius = 3.5f, center = Offset(hx, hy))
        }
        ViewportTool.SCALE -> {
            // Scale X Axis with Box Handle
            drawLine(
                color = redX,
                start = screenPos,
                end = Offset(screenPos.x + gizmoLen, screenPos.y),
                strokeWidth = 2f
            )
            drawRect(
                color = redX,
                topLeft = Offset(screenPos.x + gizmoLen - 4f, screenPos.y - 4f),
                size = Size(8f, 8f)
            )

            // Scale Y Axis with Box Handle
            drawLine(
                color = greenY,
                start = screenPos,
                end = Offset(screenPos.x, screenPos.y + gizmoLen),
                strokeWidth = 2f
            )
            drawRect(
                color = greenY,
                topLeft = Offset(screenPos.x - 4f, screenPos.y + gizmoLen - 4f),
                size = Size(8f, 8f)
            )
        }
        ViewportTool.PAN -> {
            // No gizmo overlay in pan mode
        }
    }
}

// -----------------------------------------------------------------------------
// Top and Left Measurement Pixel Rulers
// -----------------------------------------------------------------------------

private fun DrawScope.draw2DRulers(
    centerX: Float,
    centerY: Float,
    zoom: Float,
    width: Float,
    height: Float
) {
    val rulerColor = Color(0x18FFFFFF)
    val tickColor = Color(0x44FFFFFF)
    val rulerThickness = 14f

    // Top Ruler Background
    drawRect(color = rulerColor, topLeft = Offset(0f, 0f), size = Size(width, rulerThickness))

    // Top Ruler Ticks
    val step = 50f * zoom
    if (step > 15f) {
        val startX = centerX % step
        var x = startX
        while (x < width) {
            drawLine(
                color = tickColor,
                start = Offset(x, 0f),
                end = Offset(x, rulerThickness),
                strokeWidth = 0.8f
            )
            x += step
        }
    }

    // Left Ruler Background
    drawRect(color = rulerColor, topLeft = Offset(0f, 0f), size = Size(rulerThickness, height))

    // Left Ruler Ticks
    if (step > 15f) {
        val startY = centerY % step
        var y = startY
        while (y < height) {
            drawLine(
                color = tickColor,
                start = Offset(0f, y),
                end = Offset(rulerThickness, y),
                strokeWidth = 0.8f
            )
            y += step
        }
    }
}

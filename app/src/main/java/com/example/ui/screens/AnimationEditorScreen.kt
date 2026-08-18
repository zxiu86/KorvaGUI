package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SceneNode
import com.example.ui.theme.*
import kotlinx.coroutines.delay

data class AnimationKeyframe(
    val frame: Int,
    var value: Float,
    var easing: String = "Linear"
)

data class AnimationTrack(
    val id: String,
    val propertyName: String,
    val iconVector: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val keyframes: MutableList<AnimationKeyframe>
)

data class AnimationClipData(
    val name: String,
    val iconEmoji: String,
    var durationFrames: Int = 30,
    var fps: Int = 24,
    var isLooping: Boolean = true,
    val tracks: MutableList<AnimationTrack>
)

@Composable
fun AnimationEditorScreen(
    projectName: String,
    selectedNode: SceneNode?,
    onBackToEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    // -------------------------------------------------------------------------
    // Animation Clips Data & Presets
    // -------------------------------------------------------------------------
    val clips = remember {
        mutableStateListOf(
            AnimationClipData(
                name = "Idle",
                iconEmoji = "🧍",
                durationFrames = 30,
                fps = 24,
                isLooping = true,
                tracks = mutableListOf(
                    AnimationTrack("t1", "Pos X", Icons.Default.OpenWith, StudioRed, mutableListOf(AnimationKeyframe(0, 0f), AnimationKeyframe(15, 4f), AnimationKeyframe(30, 0f))),
                    AnimationTrack("t2", "Pos Y", Icons.Default.OpenWith, StudioGreen, mutableListOf(AnimationKeyframe(0, 0f), AnimationKeyframe(15, -14f), AnimationKeyframe(30, 0f))),
                    AnimationTrack("t3", "Rotation", Icons.Default.RotateRight, StudioPurpleLight, mutableListOf(AnimationKeyframe(0, 0f), AnimationKeyframe(15, 8f), AnimationKeyframe(30, 0f))),
                    AnimationTrack("t4", "Scale Y", Icons.Default.AspectRatio, StudioOrange, mutableListOf(AnimationKeyframe(0, 1f), AnimationKeyframe(15, 1.12f), AnimationKeyframe(30, 1f))),
                    AnimationTrack("t5", "Sprite", Icons.Default.Image, StudioBlue, mutableListOf(AnimationKeyframe(0, 0f), AnimationKeyframe(8, 1f), AnimationKeyframe(16, 2f), AnimationKeyframe(24, 3f)))
                )
            ),
            AnimationClipData(
                name = "Walk",
                iconEmoji = "🏃",
                durationFrames = 24,
                fps = 24,
                isLooping = true,
                tracks = mutableListOf(
                    AnimationTrack("t1", "Pos X", Icons.Default.OpenWith, StudioRed, mutableListOf(AnimationKeyframe(0, -12f), AnimationKeyframe(12, 12f), AnimationKeyframe(24, -12f))),
                    AnimationTrack("t2", "Pos Y", Icons.Default.OpenWith, StudioGreen, mutableListOf(AnimationKeyframe(0, 0f), AnimationKeyframe(6, -10f), AnimationKeyframe(12, 0f), AnimationKeyframe(18, -10f), AnimationKeyframe(24, 0f))),
                    AnimationTrack("t3", "Rotation", Icons.Default.RotateRight, StudioPurpleLight, mutableListOf(AnimationKeyframe(0, -10f), AnimationKeyframe(12, 10f), AnimationKeyframe(24, -10f))),
                    AnimationTrack("t5", "Sprite", Icons.Default.Image, StudioBlue, mutableListOf(AnimationKeyframe(0, 0f), AnimationKeyframe(6, 1f), AnimationKeyframe(12, 2f), AnimationKeyframe(18, 3f)))
                )
            ),
            AnimationClipData(
                name = "Attack",
                iconEmoji = "⚔️",
                durationFrames = 20,
                fps = 30,
                isLooping = false,
                tracks = mutableListOf(
                    AnimationTrack("t1", "Pos X", Icons.Default.OpenWith, StudioRed, mutableListOf(AnimationKeyframe(0, 0f), AnimationKeyframe(5, -18f), AnimationKeyframe(10, 32f), AnimationKeyframe(20, 0f))),
                    AnimationTrack("t3", "Rotation", Icons.Default.RotateRight, StudioPurpleLight, mutableListOf(AnimationKeyframe(0, 0f), AnimationKeyframe(5, -30f), AnimationKeyframe(10, 50f), AnimationKeyframe(20, 0f))),
                    AnimationTrack("t4", "Scale X", Icons.Default.AspectRatio, StudioOrange, mutableListOf(AnimationKeyframe(0, 1f), AnimationKeyframe(10, 1.45f), AnimationKeyframe(20, 1f)))
                )
            ),
            AnimationClipData(
                name = "Jump",
                iconEmoji = "🦘",
                durationFrames = 24,
                fps = 24,
                isLooping = false,
                tracks = mutableListOf(
                    AnimationTrack("t2", "Pos Y", Icons.Default.OpenWith, StudioGreen, mutableListOf(AnimationKeyframe(0, 0f), AnimationKeyframe(4, 10f), AnimationKeyframe(12, -60f), AnimationKeyframe(20, -10f), AnimationKeyframe(24, 0f))),
                    AnimationTrack("t4", "Scale Y", Icons.Default.AspectRatio, StudioOrange, mutableListOf(AnimationKeyframe(0, 1f), AnimationKeyframe(4, 0.7f), AnimationKeyframe(12, 1.3f), AnimationKeyframe(24, 1f)))
                )
            )
        )
    }

    var selectedClipIndex by remember { mutableIntStateOf(0) }
    val currentClip = clips.getOrNull(selectedClipIndex) ?: clips.first()

    // Playback state
    var isPlaying by remember { mutableStateOf(false) }
    var currentFrameFloat by remember { mutableFloatStateOf(0f) }
    var selectedTrackId by remember { mutableStateOf<String?>("t1") }

    // Collapsible Panels Toggle States (Requested by user)
    var isTimelineOpen by remember { mutableStateOf(false) }
    var isInspectorOpen by remember { mutableStateOf(false) }

    // Canvas Visual Toggles
    var showOnionSkin by remember { mutableStateOf(true) }
    var showGrid by remember { mutableStateOf(true) }
    var showMotionPath by remember { mutableStateOf(true) }
    var showBones by remember { mutableStateOf(false) }
    var previewZoom by remember { mutableFloatStateOf(1.0f) }

    // Dialog State
    var showNewClipDialog by remember { mutableStateOf(false) }
    var newClipName by remember { mutableStateOf("") }
    var newClipPreset by remember { mutableStateOf("Idle") }

    // Toast / Feedback message
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(feedbackMessage) {
        if (feedbackMessage != null) {
            delay(1800)
            feedbackMessage = null
        }
    }

    // Animation Playback Engine
    LaunchedEffect(isPlaying, currentClip.fps, currentClip.durationFrames) {
        if (isPlaying) {
            val frameDelayMs = (1000f / currentClip.fps).toLong().coerceAtLeast(8L)
            while (isPlaying) {
                delay(frameDelayMs)
                currentFrameFloat += 1f
                if (currentFrameFloat >= currentClip.durationFrames) {
                    if (currentClip.isLooping) {
                        currentFrameFloat = 0f
                    } else {
                        currentFrameFloat = (currentClip.durationFrames - 1).toFloat()
                        isPlaying = false
                    }
                }
            }
        }
    }

    val currentFrameInt = currentFrameFloat.toInt().coerceIn(0, currentClip.durationFrames)

    // Live property interpolation
    val currentPosX = remember(currentFrameFloat, currentClip) {
        interpolateTrack(currentClip.tracks.find { it.propertyName.contains("Pos X") || it.propertyName.contains("Position X") }, currentFrameFloat)
    }
    val currentPosY = remember(currentFrameFloat, currentClip) {
        interpolateTrack(currentClip.tracks.find { it.propertyName.contains("Pos Y") || it.propertyName.contains("Position Y") }, currentFrameFloat)
    }
    val currentRotation = remember(currentFrameFloat, currentClip) {
        interpolateTrack(currentClip.tracks.find { it.propertyName.contains("Rotation") }, currentFrameFloat)
    }
    val currentScaleX = remember(currentFrameFloat, currentClip) {
        val s = interpolateTrack(currentClip.tracks.find { it.propertyName.contains("Scale X") }, currentFrameFloat)
        if (s == 0f) 1f else s
    }
    val currentScaleY = remember(currentFrameFloat, currentClip) {
        val s = interpolateTrack(currentClip.tracks.find { it.propertyName.contains("Scale Y") }, currentFrameFloat)
        if (s == 0f) 1f else s
    }
    val currentSpriteFrame = remember(currentFrameFloat, currentClip) {
        interpolateTrack(currentClip.tracks.find { it.propertyName.contains("Sprite") }, currentFrameFloat).toInt().coerceAtLeast(0)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EngineBackground)
    ) {
        // =========================================================================
        // 1. Ultra-Compact Studio Header (32dp height, ~40% smaller)
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .background(EngineSurface)
                .border(0.5.dp, StudioBorder)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Back button + Compact Title
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(EngineCardBg)
                        .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                        .clickable { onBackToEditor() }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "العودة", tint = StudioPurpleLight, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("عودة", color = TextPrimary, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Movie, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "الأنيميشن: ${selectedNode?.name ?: "Player"}",
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right: Clips Chips + New Clip
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Clips horizontal selector
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(EngineCardBg)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    clips.forEachIndexed { idx, clip ->
                        val isSel = idx == selectedClipIndex
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isSel) StudioPurple else Color.Transparent)
                                .clickable {
                                    selectedClipIndex = idx
                                    currentFrameFloat = 0f
                                    isPlaying = false
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(clip.iconEmoji, fontSize = 9.sp)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = clip.name,
                                    color = if (isSel) Color.White else TextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Add Clip mini button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StudioPurpleDark)
                        .border(0.5.dp, StudioPurpleLight, RoundedCornerShape(4.dp))
                        .clickable { showNewClipDialog = true }
                        .padding(horizontal = 5.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(10.dp))
                        Text("+ مقطع", color = StudioPurpleLight, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // =========================================================================
        // 2. Main Stage & Floating Tool Overlays
        // =========================================================================
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Main 2D Stage Canvas (Full Size Hero Canvas)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(EngineBackground)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f

                    // 1. Grid
                    if (showGrid) {
                        val gridSize = 28f * previewZoom
                        var x = centerX % gridSize
                        while (x < size.width) {
                            drawLine(StudioBorder.copy(alpha = 0.2f), Offset(x, 0f), Offset(x, size.height), 0.5f)
                            x += gridSize
                        }
                        var y = centerY % gridSize
                        while (y < size.height) {
                            drawLine(StudioBorder.copy(alpha = 0.2f), Offset(0f, y), Offset(size.width, y), 0.5f)
                            y += gridSize
                        }
                    }

                    // 2. Axes Crosshair
                    drawLine(StudioRed.copy(alpha = 0.4f), Offset(0f, centerY), Offset(size.width, centerY), 0.8f)
                    drawLine(StudioGreen.copy(alpha = 0.4f), Offset(centerX, 0f), Offset(centerX, size.height), 0.8f)

                    // 3. Motion Trajectory Path
                    if (showMotionPath) {
                        val path = Path()
                        val numSteps = currentClip.durationFrames
                        for (f in 0..numSteps) {
                            val px = interpolateTrack(currentClip.tracks.find { it.propertyName.contains("Pos X") || it.propertyName.contains("Position X") }, f.toFloat())
                            val py = interpolateTrack(currentClip.tracks.find { it.propertyName.contains("Pos Y") || it.propertyName.contains("Position Y") }, f.toFloat())
                            val pt = Offset(centerX + (px * previewZoom), centerY + (py * previewZoom))
                            if (f == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                        }
                        drawPath(
                            path = path,
                            color = StudioYellow.copy(alpha = 0.5f),
                            style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f))
                        )
                    }

                    // 4. Ghosting / Onion Skinning
                    if (showOnionSkin && currentFrameFloat > 1f) {
                        val prevFrame = currentFrameFloat - 4f
                        val prevX = interpolateTrack(currentClip.tracks.find { it.propertyName.contains("Pos X") || it.propertyName.contains("Position X") }, prevFrame)
                        val prevY = interpolateTrack(currentClip.tracks.find { it.propertyName.contains("Pos Y") || it.propertyName.contains("Position Y") }, prevFrame)
                        val prevRot = interpolateTrack(currentClip.tracks.find { it.propertyName.contains("Rotation") }, prevFrame)

                        val ghostX = centerX + (prevX * previewZoom)
                        val ghostY = centerY + (prevY * previewZoom)
                        rotate(degrees = prevRot, pivot = Offset(ghostX, ghostY)) {
                            drawRoundRect(
                                color = StudioPurpleLight.copy(alpha = 0.2f),
                                topLeft = Offset(ghostX - 22f * previewZoom, ghostY - 22f * previewZoom),
                                size = Size(44f * previewZoom, 44f * previewZoom),
                                cornerRadius = CornerRadius(6f, 6f),
                                style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 3f), 0f))
                            )
                        }
                    }

                    // 5. Active Node Character Rendering
                    val animCenterX = centerX + (currentPosX * previewZoom)
                    val animCenterY = centerY + (currentPosY * previewZoom)
                    val spriteWidth = 48f * currentScaleX * previewZoom
                    val spriteHeight = 48f * currentScaleY * previewZoom

                    // Shadow
                    drawOval(
                        color = Color.Black.copy(alpha = 0.35f),
                        topLeft = Offset(animCenterX - 24f * previewZoom, centerY + 26f * previewZoom),
                        size = Size(48f * previewZoom, 10f * previewZoom)
                    )

                    rotate(degrees = currentRotation, pivot = Offset(animCenterX, animCenterY)) {
                        val bodyGradient = when (currentSpriteFrame % 4) {
                            1 -> listOf(StudioGreen.copy(alpha = 0.9f), StudioPurpleDark)
                            2 -> listOf(StudioOrange.copy(alpha = 0.9f), StudioPurpleDark)
                            3 -> listOf(StudioRed.copy(alpha = 0.9f), StudioPurpleDark)
                            else -> listOf(StudioPurpleDark, StudioPurple)
                        }

                        drawRoundRect(
                            brush = Brush.linearGradient(bodyGradient),
                            topLeft = Offset(animCenterX - spriteWidth / 2f, animCenterY - spriteHeight / 2f),
                            size = Size(spriteWidth, spriteHeight),
                            cornerRadius = CornerRadius(8f, 8f)
                        )

                        // Outer Glow Border
                        drawRoundRect(
                            color = StudioPurpleLight,
                            topLeft = Offset(animCenterX - spriteWidth / 2f, animCenterY - spriteHeight / 2f),
                            size = Size(spriteWidth, spriteHeight),
                            cornerRadius = CornerRadius(8f, 8f),
                            style = Stroke(width = 1.8f)
                        )

                        // Face Details
                        val eyeOffset = 8f * previewZoom
                        drawCircle(Color.White, radius = 3.5f * previewZoom, center = Offset(animCenterX - eyeOffset, animCenterY - eyeOffset))
                        drawCircle(Color.White, radius = 3.5f * previewZoom, center = Offset(animCenterX + eyeOffset, animCenterY - eyeOffset))
                        drawCircle(Color.Black, radius = 2f * previewZoom, center = Offset(animCenterX - eyeOffset + 0.8f, animCenterY - eyeOffset))
                        drawCircle(Color.Black, radius = 2f * previewZoom, center = Offset(animCenterX + eyeOffset + 0.8f, animCenterY - eyeOffset))

                        // Bones Rig
                        if (showBones) {
                            drawLine(StudioYellow, Offset(animCenterX, animCenterY), Offset(animCenterX, animCenterY - 16f * previewZoom), 1.5f)
                            drawCircle(StudioYellow, radius = 2.5f * previewZoom, center = Offset(animCenterX, animCenterY))
                        }
                    }
                }

                // Toast Feedback message
                feedbackMessage?.let { msg ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(StudioPurpleDark)
                            .border(0.8.dp, StudioGreen, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(msg, color = StudioGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Top Floating Toolbar (View Toggles + Zoom, ~40% smaller)
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(EngineSurface.copy(alpha = 0.92f))
                    .border(0.5.dp, StudioBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Onion Skin Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (showOnionSkin) StudioPurpleDark else Color.Transparent)
                        .clickable { showOnionSkin = !showOnionSkin }
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text("Onion", color = if (showOnionSkin) StudioPurpleLight else TextMuted, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                }

                // Motion Path Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (showMotionPath) StudioPurpleDark else Color.Transparent)
                        .clickable { showMotionPath = !showMotionPath }
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text("Path", color = if (showMotionPath) StudioPurpleLight else TextMuted, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                }

                // Grid Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (showGrid) StudioPurpleDark else Color.Transparent)
                        .clickable { showGrid = !showGrid }
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text("Grid", color = if (showGrid) StudioPurpleLight else TextMuted, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                }

                // Bones Rig Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (showBones) StudioPurpleDark else Color.Transparent)
                        .clickable { showBones = !showBones }
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text("Rig", color = if (showBones) StudioPurpleLight else TextMuted, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                }

                // Zoom Out / In
                IconButton(onClick = { previewZoom = (previewZoom - 0.2f).coerceAtLeast(0.6f) }, modifier = Modifier.size(18.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "تصغير", tint = TextSecondary, modifier = Modifier.size(10.dp))
                }
                Text("${(previewZoom * 100).toInt()}%", color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                IconButton(onClick = { previewZoom = (previewZoom + 0.2f).coerceAtMost(2.5f) }, modifier = Modifier.size(18.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "تكبير", tint = TextSecondary, modifier = Modifier.size(10.dp))
                }
            }

            // Top Right Floating Action Toggles (Toggle Timeline & Inspector on demand!)
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Toggle Keyframe Inspector Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(if (isInspectorOpen) StudioPurpleDark else EngineSurface.copy(alpha = 0.92f))
                        .border(0.6.dp, if (isInspectorOpen) StudioPurpleLight else StudioBorder, RoundedCornerShape(5.dp))
                        .clickable { isInspectorOpen = !isInspectorOpen }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = if (isInspectorOpen) StudioPurpleLight else TextMuted, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isInspectorOpen) "إخفاء الخصائص" else "الخصائص",
                            color = if (isInspectorOpen) Color.White else TextSecondary,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Toggle Timeline Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(if (isTimelineOpen) StudioPurpleDark else EngineSurface.copy(alpha = 0.92f))
                        .border(0.6.dp, if (isTimelineOpen) StudioPurpleLight else StudioBorder, RoundedCornerShape(5.dp))
                        .clickable { isTimelineOpen = !isTimelineOpen }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timeline, contentDescription = null, tint = if (isTimelineOpen) StudioPurpleLight else TextMuted, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isTimelineOpen) "إخفاء التايم لاين" else "التايم لاين",
                            color = if (isTimelineOpen) Color.White else TextSecondary,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Collapsible Right Keyframe Inspector Panel (Floating Compact Drawer ~175dp, 40% smaller)
            if (isInspectorOpen) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 36.dp, end = 6.dp, bottom = 6.dp)
                        .width(175.dp)
                        .fillMaxHeight(0.85f),
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(containerColor = EngineSurface.copy(alpha = 0.96f)),
                    border = androidx.compose.foundation.BorderStroke(0.6.dp, StudioPurpleBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Header + Close (X)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("الخصائص (Inspector)", color = TextPrimary, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { isInspectorOpen = false }, modifier = Modifier.size(16.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted, modifier = Modifier.size(11.dp))
                            }
                        }

                        val activeTrack = currentClip.tracks.find { it.id == selectedTrackId }
                        if (activeTrack != null) {
                            // Active Track Card
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EngineCardBg)
                                    .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                                    .padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(activeTrack.iconVector, contentDescription = null, tint = activeTrack.color, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(activeTrack.propertyName, color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }

                            // Add Keyframe Compact Button
                            Button(
                                onClick = {
                                    val existing = activeTrack.keyframes.find { it.frame == currentFrameInt }
                                    if (existing != null) {
                                        existing.value += 5f
                                    } else {
                                        activeTrack.keyframes.add(AnimationKeyframe(currentFrameInt, 10f))
                                    }
                                    activeTrack.keyframes.sortBy { it.frame }
                                    feedbackMessage = "تم إضافة Keyframe لإطار $currentFrameInt"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                modifier = Modifier.fillMaxWidth().height(24.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("+ Keyframe فريم $currentFrameInt", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                            }

                            // Keyframes Mini List
                            Text("Keyframes (${activeTrack.keyframes.size}):", color = TextSecondary, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)

                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .background(EngineBackground, RoundedCornerShape(4.dp))
                                    .padding(3.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                itemsIndexed(activeTrack.keyframes) { idx, kf ->
                                    val isCurrentKf = kf.frame == currentFrameInt
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (isCurrentKf) StudioPurpleDark else EngineCardBg)
                                            .clickable { currentFrameFloat = kf.frame.toFloat() }
                                            .padding(horizontal = 4.dp, vertical = 3.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("F${kf.frame}", color = TextPrimary, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)

                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                            IconButton(onClick = { kf.value -= 2f }, modifier = Modifier.size(16.dp)) {
                                                Icon(Icons.Default.Remove, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(9.dp))
                                            }

                                            Text("${kf.value.toInt()}", color = StudioPurpleLight, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace)

                                            IconButton(onClick = { kf.value += 2f }, modifier = Modifier.size(16.dp)) {
                                                Icon(Icons.Default.Add, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(9.dp))
                                            }

                                            IconButton(onClick = { activeTrack.keyframes.removeAt(idx) }, modifier = Modifier.size(16.dp)) {
                                                Icon(Icons.Default.Delete, contentDescription = null, tint = StudioRed, modifier = Modifier.size(10.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Text("حدد مساراً لتعديل الإطارات", color = TextMuted, fontSize = 8.5.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            // Bottom Floating Transform Specs
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(EngineSurface.copy(alpha = 0.88f))
                    .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "X:${currentPosX.toInt()} Y:${currentPosY.toInt()} Rot:${currentRotation.toInt()}° S:(${String.format("%.1f", currentScaleX)})",
                    color = TextSecondary,
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // =========================================================================
        // 3. Compact Transport Bar (Height 28dp, 40% smaller)
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .background(EngineSurface)
                .border(0.5.dp, StudioBorder)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Transport Controls
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                IconButton(onClick = { currentFrameFloat = 0f }, modifier = Modifier.size(22.dp)) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "البداية", tint = TextSecondary, modifier = Modifier.size(13.dp))
                }

                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(StudioPurple)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "تشغيل",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }

                IconButton(onClick = { currentFrameFloat = (currentClip.durationFrames - 1).toFloat() }, modifier = Modifier.size(22.dp)) {
                    Icon(Icons.Default.SkipNext, contentDescription = "النهاية", tint = TextSecondary, modifier = Modifier.size(13.dp))
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Frame Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(StudioPurpleDark)
                        .border(0.5.dp, StudioPurpleLight, RoundedCornerShape(3.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "F $currentFrameInt / ${currentClip.durationFrames}",
                        color = StudioPurpleLight,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Quick Playback Options (Loop, FPS)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Loop
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (currentClip.isLooping) StudioPurpleDark else EngineCardBg)
                        .border(0.5.dp, if (currentClip.isLooping) StudioPurpleLight else StudioBorder, RoundedCornerShape(3.dp))
                        .clickable { currentClip.isLooping = !currentClip.isLooping }
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Repeat, contentDescription = null, tint = if (currentClip.isLooping) StudioPurpleLight else TextMuted, modifier = Modifier.size(9.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Loop", color = if (currentClip.isLooping) TextPrimary else TextMuted, fontSize = 8.5.sp)
                    }
                }

                // FPS
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(EngineCardBg)
                        .border(0.5.dp, StudioBorder, RoundedCornerShape(3.dp))
                        .clickable {
                            currentClip.fps = when (currentClip.fps) {
                                12 -> 24
                                24 -> 30
                                30 -> 60
                                else -> 12
                            }
                        }
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text("${currentClip.fps} FPS", color = StudioBlue, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }

                // Quick Toggle Timeline Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isTimelineOpen) StudioPurpleDark else EngineCardBg)
                        .border(0.5.dp, if (isTimelineOpen) StudioPurpleLight else StudioBorder, RoundedCornerShape(3.dp))
                        .clickable { isTimelineOpen = !isTimelineOpen }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isTimelineOpen) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = null,
                            tint = StudioPurpleLight,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("التايم لاين", color = TextPrimary, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // =========================================================================
        // 4. Collapsible Timeline Dopesheet Panel (Height 120dp, ~40% smaller)
        // =========================================================================
        AnimatedVisibility(
            visible = isTimelineOpen,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp)
                    .background(EngineSurface)
                    .border(0.5.dp, StudioBorder)
            ) {
                // Header of Timeline with Add Track & Close
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(22.dp)
                        .background(EngineBackground)
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timeline, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("مسارات التحريك (Dopesheet)", color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Add Track
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(StudioPurpleDark)
                                .clickable {
                                    val newTrackName = "Track_${currentClip.tracks.size + 1}"
                                    currentClip.tracks.add(
                                        AnimationTrack("t_${System.currentTimeMillis()}", newTrackName, Icons.Default.Tune, StudioYellow, mutableListOf(AnimationKeyframe(0, 0f)))
                                    )
                                }
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("+ مسار", color = StudioPurpleLight, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }

                        // Close Timeline
                        IconButton(onClick = { isTimelineOpen = false }, modifier = Modifier.size(16.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted, modifier = Modifier.size(10.dp))
                        }
                    }
                }

                // Timeline Tracks + Scrubber Canvas
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left: Track Names (Width ~90dp, compact)
                    Column(
                        modifier = Modifier
                            .width(90.dp)
                            .fillMaxHeight()
                            .background(EngineCardBg)
                            .border(0.5.dp, StudioBorder)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(currentClip.tracks) { track ->
                                val isSel = track.id == selectedTrackId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(20.dp)
                                        .background(if (isSel) StudioPurpleDark else Color.Transparent)
                                        .clickable {
                                            selectedTrackId = track.id
                                            isInspectorOpen = true
                                        }
                                        .padding(horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(track.iconVector, contentDescription = null, tint = track.color, modifier = Modifier.size(9.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = track.propertyName,
                                        color = if (isSel) TextPrimary else TextSecondary,
                                        fontSize = 8.5.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Right: Scrubber Canvas Grid
                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(EngineBackground)
                            .pointerInput(currentClip.durationFrames) {
                                detectTapGestures { tapOffset ->
                                    val frameW = size.width / currentClip.durationFrames.toFloat()
                                    currentFrameFloat = (tapOffset.x / frameW).coerceIn(0f, (currentClip.durationFrames - 1).toFloat())
                                }
                            }
                            .pointerInput(currentClip.durationFrames) {
                                detectDragGestures { change, _ ->
                                    change.consume()
                                    val frameW = size.width / currentClip.durationFrames.toFloat()
                                    currentFrameFloat = (change.position.x / frameW).coerceIn(0f, (currentClip.durationFrames - 1).toFloat())
                                }
                            }
                    ) {
                        val totalW = constraints.maxWidth.toFloat()
                        val totalH = constraints.maxHeight.toFloat()
                        val frameW = totalW / currentClip.durationFrames.toFloat()

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Frame Ticks
                            for (f in 0..currentClip.durationFrames) {
                                val x = f * frameW
                                val isMajor = f % 5 == 0
                                drawLine(
                                    color = if (isMajor) StudioPurpleLight.copy(alpha = 0.7f) else StudioBorder.copy(alpha = 0.5f),
                                    start = Offset(x, 0f),
                                    end = Offset(x, if (isMajor) 14f else 8f),
                                    strokeWidth = if (isMajor) 1f else 0.5f
                                )
                            }

                            // Track Row Dividers + Keyframe Dots
                            currentClip.tracks.forEachIndexed { rowIdx, track ->
                                val rowY = (rowIdx * 20f) + 10f

                                drawLine(
                                    color = StudioBorder.copy(alpha = 0.25f),
                                    start = Offset(0f, rowY + 10f),
                                    end = Offset(totalW, rowY + 10f),
                                    strokeWidth = 0.5f
                                )

                                track.keyframes.forEach { kf ->
                                    val kfX = kf.frame * frameW
                                    drawCircle(
                                        color = StudioYellow,
                                        radius = 3.5f,
                                        center = Offset(kfX, rowY)
                                    )
                                    drawCircle(
                                        color = EngineSurface,
                                        radius = 1.5f,
                                        center = Offset(kfX, rowY)
                                    )
                                }
                            }

                            // Playhead Red Cursor Line
                            val playheadX = currentFrameFloat * frameW
                            drawLine(
                                color = Color(0xFFEF4444),
                                start = Offset(playheadX, 0f),
                                end = Offset(playheadX, totalH),
                                strokeWidth = 1.5f
                            )
                            drawRect(
                                color = Color(0xFFEF4444),
                                topLeft = Offset(playheadX - 4f, 0f),
                                size = Size(8f, 8f)
                            )
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // Dialog for Adding New Animation Clip (Compact)
    // =========================================================================
    if (showNewClipDialog) {
        AlertDialog(
            onDismissRequest = { showNewClipDialog = false },
            title = { Text("مقطع أنيميشن جديد", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("اسم المقطع:", fontSize = 10.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = newClipName,
                        onValueChange = { newClipName = it },
                        singleLine = true,
                        placeholder = { Text("مثال: Roll, Attack2", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("اختر قالب جاهز:", fontSize = 10.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        listOf("Idle" to "🧍", "Walk" to "🏃", "Attack" to "⚔️", "Jump" to "🦘").forEach { (preset, emoji) ->
                            val isSelected = newClipPreset == preset
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) StudioPurple else EngineCardBg)
                                    .border(0.5.dp, if (isSelected) StudioPurpleLight else StudioBorder, RoundedCornerShape(4.dp))
                                    .clickable { newClipPreset = preset }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text("$emoji $preset", color = if (isSelected) Color.White else TextSecondary, fontSize = 9.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newClipName.ifBlank { newClipPreset }
                        val emoji = when (newClipPreset) {
                            "Walk" -> "🏃"
                            "Attack" -> "⚔️"
                            "Jump" -> "🦘"
                            else -> "🧍"
                        }
                        clips.add(
                            AnimationClipData(
                                name = name.trim(),
                                iconEmoji = emoji,
                                durationFrames = 24,
                                fps = 24,
                                isLooping = true,
                                tracks = mutableListOf(
                                    AnimationTrack("t1", "Pos X", Icons.Default.OpenWith, StudioRed, mutableListOf(AnimationKeyframe(0, 0f), AnimationKeyframe(12, 10f), AnimationKeyframe(24, 0f))),
                                    AnimationTrack("t3", "Rotation", Icons.Default.RotateRight, StudioPurpleLight, mutableListOf(AnimationKeyframe(0, 0f), AnimationKeyframe(12, 15f), AnimationKeyframe(24, 0f)))
                                )
                            )
                        )
                        selectedClipIndex = clips.size - 1
                        newClipName = ""
                        showNewClipDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("إنشاء", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewClipDialog = false }) {
                    Text("إلغاء", color = TextMuted, fontSize = 10.sp)
                }
            },
            containerColor = EngineSurface,
            titleContentColor = TextPrimary
        )
    }
}

/**
 * Keyframe interpolation helper
 */
private fun interpolateTrack(track: AnimationTrack?, currentFrame: Float): Float {
    if (track == null || track.keyframes.isEmpty()) return 0f
    val keyframes = track.keyframes.sortedBy { it.frame }
    if (currentFrame <= keyframes.first().frame) return keyframes.first().value
    if (currentFrame >= keyframes.last().frame) return keyframes.last().value

    for (i in 0 until keyframes.size - 1) {
        val kf1 = keyframes[i]
        val kf2 = keyframes[i + 1]
        if (currentFrame >= kf1.frame && currentFrame <= kf2.frame) {
            val progress = (currentFrame - kf1.frame) / (kf2.frame - kf1.frame).toFloat()
            return kf1.value + (progress * (kf2.value - kf1.value))
        }
    }
    return keyframes.first().value
}

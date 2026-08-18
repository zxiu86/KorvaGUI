package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SceneNode
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

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
                    AnimationTrack("t1", "Position X", Icons.Default.OpenWith, StudioRed, mutableListOf(AnimationKeyframe(0, 0f), AnimationKeyframe(15, 4f), AnimationKeyframe(30, 0f))),
                    AnimationTrack("t2", "Position Y", Icons.Default.OpenWith, StudioGreen, mutableListOf(AnimationKeyframe(0, 0f), AnimationKeyframe(15, -14f), AnimationKeyframe(30, 0f))),
                    AnimationTrack("t3", "Rotation", Icons.Default.RotateRight, StudioPurpleLight, mutableListOf(AnimationKeyframe(0, 0f), AnimationKeyframe(15, 8f), AnimationKeyframe(30, 0f))),
                    AnimationTrack("t4", "Scale Y", Icons.Default.AspectRatio, StudioOrange, mutableListOf(AnimationKeyframe(0, 1f), AnimationKeyframe(15, 1.12f), AnimationKeyframe(30, 1f))),
                    AnimationTrack("t5", "Sprite Frame", Icons.Default.Image, StudioBlue, mutableListOf(AnimationKeyframe(0, 0f), AnimationKeyframe(8, 1f), AnimationKeyframe(16, 2f), AnimationKeyframe(24, 3f)))
                )
            ),
            AnimationClipData(
                name = "Walk",
                iconEmoji = "🏃",
                durationFrames = 24,
                fps = 24,
                isLooping = true,
                tracks = mutableListOf(
                    AnimationTrack("t1", "Position X", Icons.Default.OpenWith, StudioRed, mutableListOf(AnimationKeyframe(0, -12f), AnimationKeyframe(12, 12f), AnimationKeyframe(24, -12f))),
                    AnimationTrack("t2", "Position Y", Icons.Default.OpenWith, StudioGreen, mutableListOf(AnimationKeyframe(0, 0f), AnimationKeyframe(6, -10f), AnimationKeyframe(12, 0f), AnimationKeyframe(18, -10f), AnimationKeyframe(24, 0f))),
                    AnimationTrack("t3", "Rotation", Icons.Default.RotateRight, StudioPurpleLight, mutableListOf(AnimationKeyframe(0, -10f), AnimationKeyframe(12, 10f), AnimationKeyframe(24, -10f))),
                    AnimationTrack("t5", "Sprite Frame", Icons.Default.Image, StudioBlue, mutableListOf(AnimationKeyframe(0, 0f), AnimationKeyframe(6, 1f), AnimationKeyframe(12, 2f), AnimationKeyframe(18, 3f)))
                )
            ),
            AnimationClipData(
                name = "Attack",
                iconEmoji = "⚔️",
                durationFrames = 20,
                fps = 30,
                isLooping = false,
                tracks = mutableListOf(
                    AnimationTrack("t1", "Position X", Icons.Default.OpenWith, StudioRed, mutableListOf(AnimationKeyframe(0, 0f), AnimationKeyframe(5, -18f), AnimationKeyframe(10, 32f), AnimationKeyframe(20, 0f))),
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
                    AnimationTrack("t2", "Position Y", Icons.Default.OpenWith, StudioGreen, mutableListOf(AnimationKeyframe(0, 0f), AnimationKeyframe(4, 10f), AnimationKeyframe(12, -60f), AnimationKeyframe(20, -10f), AnimationKeyframe(24, 0f))),
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

    // Canvas Options
    var showOnionSkin by remember { mutableStateOf(true) }
    var showGrid by remember { mutableStateOf(true) }
    var showMotionPath by remember { mutableStateOf(true) }
    var showBones by remember { mutableStateOf(true) }
    var previewZoom by remember { mutableFloatStateOf(1.25f) }

    // Dialog State
    var showNewClipDialog by remember { mutableStateOf(false) }
    var newClipName by remember { mutableStateOf("") }
    var newClipPreset by remember { mutableStateOf("Idle") }

    // Toast / Feedback message
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(feedbackMessage) {
        if (feedbackMessage != null) {
            delay(2000)
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

    // Interpolate live properties
    val currentPosX = remember(currentFrameFloat, currentClip) {
        interpolateTrack(currentClip.tracks.find { it.propertyName.contains("Position X") }, currentFrameFloat)
    }
    val currentPosY = remember(currentFrameFloat, currentClip) {
        interpolateTrack(currentClip.tracks.find { it.propertyName.contains("Position Y") }, currentFrameFloat)
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
        interpolateTrack(currentClip.tracks.find { it.propertyName.contains("Sprite Frame") }, currentFrameFloat).toInt().coerceAtLeast(0)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EngineBackground)
    ) {
        // =========================================================================
        // 1. Studio Header Top Bar (Pro Game Studio Layout)
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(EngineSurface)
                .border(0.6.dp, StudioBorder)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Back button + Title & Object Badge
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(EngineCardBg)
                        .border(0.8.dp, StudioBorder, RoundedCornerShape(6.dp))
                        .clickable { onBackToEditor() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "العودة", tint = StudioPurpleLight, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("العودة للمحرر", color = TextPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Box(modifier = Modifier.width(1.dp).height(20.dp).background(StudioBorder))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Brush.linearGradient(listOf(StudioPurpleDark, StudioPurple)))
                            .border(0.6.dp, StudioPurpleLight, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "استوديو الرسوم المتحركة (Animation Studio)",
                            color = TextPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "العنصر المستهدف: ${selectedNode?.name ?: "Player_Hero"}",
                            color = StudioPurpleLight,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Right: Clip Selector Chips & Action Buttons
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Clips Tabs
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(EngineCardBg)
                        .border(0.6.dp, StudioBorder, RoundedCornerShape(8.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    clips.forEachIndexed { idx, clip ->
                        val isSel = idx == selectedClipIndex
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) StudioPurple else Color.Transparent)
                                .border(0.6.dp, if (isSel) StudioPurpleLight else Color.Transparent, RoundedCornerShape(6.dp))
                                .clickable {
                                    selectedClipIndex = idx
                                    currentFrameFloat = 0f
                                    isPlaying = false
                                }
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(clip.iconEmoji, fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = clip.name,
                                    color = if (isSel) Color.White else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // New Clip Button
                Button(
                    onClick = { showNewClipDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPurpleDark),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مقطع جديد", color = StudioPurpleLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Save Animation Button
                Button(
                    onClick = { feedbackMessage = "✓ تم حفظ مقاطع الأنيميشن بنجاح!" },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حفظ", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // =========================================================================
        // 2. Main Studio Split Area (Left Canvas Stage + Right Inspector Panel)
        // =========================================================================
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // ----------------------------------------------------
            // Left Column: Interactive 2D Stage Canvas + Transport Toolbar
            // ----------------------------------------------------
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(EngineBackground)
            ) {
                // Viewport Controls Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EngineSurface.copy(alpha = 0.95f))
                        .border(0.6.dp, StudioBorder)
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Transport Buttons
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { currentFrameFloat = 0f },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "البداية", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = { isPlaying = !isPlaying },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(StudioPurple)
                                .shadow(4.dp, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "تشغيل",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { currentFrameFloat = (currentClip.durationFrames - 1).toFloat() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = "النهاية", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Current Frame Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(StudioPurpleDark)
                                .border(0.8.dp, StudioPurpleLight, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Frame $currentFrameInt / ${currentClip.durationFrames}",
                                color = StudioPurpleLight,
                                fontSize = 11.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Viewport Rendering Toggles
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Looping Switcher
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (currentClip.isLooping) StudioPurpleDark else EngineCardBg)
                                .border(0.6.dp, if (currentClip.isLooping) StudioPurpleLight else StudioBorder, RoundedCornerShape(6.dp))
                                .clickable { currentClip.isLooping = !currentClip.isLooping }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Repeat, contentDescription = null, tint = if (currentClip.isLooping) StudioPurpleLight else TextMuted, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تكرار Loop", color = if (currentClip.isLooping) TextPrimary else TextMuted, fontSize = 10.5.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        // FPS Selector
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EngineCardBg)
                                .border(0.6.dp, StudioBorder, RoundedCornerShape(6.dp))
                                .clickable {
                                    currentClip.fps = when (currentClip.fps) {
                                        12 -> 24
                                        24 -> 30
                                        30 -> 60
                                        else -> 12
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("${currentClip.fps} FPS", color = StudioBlue, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        // Motion Trail Toggle
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (showMotionPath) StudioPurpleDark else EngineCardBg)
                                .border(0.6.dp, if (showMotionPath) StudioPurpleLight else StudioBorder, RoundedCornerShape(6.dp))
                                .clickable { showMotionPath = !showMotionPath }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("مسار الحركة", color = if (showMotionPath) TextPrimary else TextMuted, fontSize = 10.5.sp)
                        }

                        // Onion Skin Toggle
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (showOnionSkin) StudioPurpleDark else EngineCardBg)
                                .border(0.6.dp, if (showOnionSkin) StudioPurpleLight else StudioBorder, RoundedCornerShape(6.dp))
                                .clickable { showOnionSkin = !showOnionSkin }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Onion Skin", color = if (showOnionSkin) TextPrimary else TextMuted, fontSize = 10.5.sp)
                        }

                        // Bones Rig Toggle
                        IconButton(
                            onClick = { showBones = !showBones },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.Default.DeviceHub, contentDescription = "العظام", tint = if (showBones) StudioPurpleLight else TextMuted, modifier = Modifier.size(16.dp))
                        }

                        // Grid Toggle
                        IconButton(
                            onClick = { showGrid = !showGrid },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.Default.GridOn, contentDescription = "الشبكة", tint = if (showGrid) StudioPurpleLight else TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Main Interactive Animation Stage Viewport Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(EngineBackground)
                        .border(0.6.dp, StudioBorder),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f

                        // 1. Grid Background
                        if (showGrid) {
                            val gridSize = 36f * previewZoom
                            var x = centerX % gridSize
                            while (x < size.width) {
                                drawLine(StudioBorder.copy(alpha = 0.25f), Offset(x, 0f), Offset(x, size.height), 0.5f)
                                x += gridSize
                            }
                            var y = centerY % gridSize
                            while (y < size.height) {
                                drawLine(StudioBorder.copy(alpha = 0.25f), Offset(0f, y), Offset(size.width, y), 0.5f)
                                y += gridSize
                            }
                        }

                        // 2. Axes Crosshair
                        drawLine(StudioRed.copy(alpha = 0.5f), Offset(0f, centerY), Offset(size.width, centerY), 1f)
                        drawLine(StudioGreen.copy(alpha = 0.5f), Offset(centerX, 0f), Offset(centerX, size.height), 1f)

                        // 3. Motion Trajectory Path Visualization
                        if (showMotionPath) {
                            val path = Path()
                            val numSteps = currentClip.durationFrames
                            val pathPoints = mutableListOf<Offset>()
                            for (f in 0..numSteps) {
                                val px = interpolateTrack(currentClip.tracks.find { it.propertyName.contains("Position X") }, f.toFloat())
                                val py = interpolateTrack(currentClip.tracks.find { it.propertyName.contains("Position Y") }, f.toFloat())
                                val pt = Offset(centerX + (px * previewZoom), centerY + (py * previewZoom))
                                pathPoints.add(pt)
                                if (f == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                            }
                            drawPath(
                                path = path,
                                color = StudioYellow.copy(alpha = 0.6f),
                                style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
                            )
                            pathPoints.forEach { pt ->
                                drawCircle(StudioYellow, radius = 3f, center = pt)
                            }
                        }

                        // 4. Ghosting / Onion Skinning
                        if (showOnionSkin && currentFrameFloat > 1f) {
                            val prevFrame = currentFrameFloat - 4f
                            val prevX = interpolateTrack(currentClip.tracks.find { it.propertyName.contains("Position X") }, prevFrame)
                            val prevY = interpolateTrack(currentClip.tracks.find { it.propertyName.contains("Position Y") }, prevFrame)
                            val prevRot = interpolateTrack(currentClip.tracks.find { it.propertyName.contains("Rotation") }, prevFrame)

                            val ghostX = centerX + (prevX * previewZoom)
                            val ghostY = centerY + (prevY * previewZoom)
                            rotate(degrees = prevRot, pivot = Offset(ghostX, ghostY)) {
                                drawRoundRect(
                                    color = StudioPurpleLight.copy(alpha = 0.25f),
                                    topLeft = Offset(ghostX - 28f * previewZoom, ghostY - 28f * previewZoom),
                                    size = Size(56f * previewZoom, 56f * previewZoom),
                                    cornerRadius = CornerRadius(8f, 8f),
                                    style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f))
                                )
                            }
                        }

                        // 5. Active Node Character Rendering
                        val animCenterX = centerX + (currentPosX * previewZoom)
                        val animCenterY = centerY + (currentPosY * previewZoom)
                        val spriteWidth = 60f * currentScaleX * previewZoom
                        val spriteHeight = 60f * currentScaleY * previewZoom

                        // Shadow
                        drawOval(
                            color = Color.Black.copy(alpha = 0.4f),
                            topLeft = Offset(animCenterX - 30f * previewZoom, centerY + 32f * previewZoom),
                            size = Size(60f * previewZoom, 14f * previewZoom)
                        )

                        rotate(degrees = currentRotation, pivot = Offset(animCenterX, animCenterY)) {
                            // Body Box with Gradient
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
                                cornerRadius = CornerRadius(12f, 12f)
                            )

                            // Outer Glow Border
                            drawRoundRect(
                                color = StudioPurpleLight,
                                topLeft = Offset(animCenterX - spriteWidth / 2f, animCenterY - spriteHeight / 2f),
                                size = Size(spriteWidth, spriteHeight),
                                cornerRadius = CornerRadius(12f, 12f),
                                style = Stroke(width = 2.5f)
                            )

                            // Character Face Details (Eyes & Smile)
                            val eyeOffset = 11f * previewZoom
                            drawCircle(Color.White, radius = 4.5f * previewZoom, center = Offset(animCenterX - eyeOffset, animCenterY - eyeOffset))
                            drawCircle(Color.White, radius = 4.5f * previewZoom, center = Offset(animCenterX + eyeOffset, animCenterY - eyeOffset))
                            drawCircle(Color.Black, radius = 2.5f * previewZoom, center = Offset(animCenterX - eyeOffset + 1f, animCenterY - eyeOffset))
                            drawCircle(Color.Black, radius = 2.5f * previewZoom, center = Offset(animCenterX + eyeOffset + 1f, animCenterY - eyeOffset))

                            // Skeleton Rig Joints
                            if (showBones) {
                                drawLine(StudioYellow, Offset(animCenterX, animCenterY), Offset(animCenterX, animCenterY - 20f * previewZoom), 2f)
                                drawLine(StudioYellow, Offset(animCenterX, animCenterY), Offset(animCenterX - 20f * previewZoom, animCenterY + 10f * previewZoom), 2f)
                                drawLine(StudioYellow, Offset(animCenterX, animCenterY), Offset(animCenterX + 20f * previewZoom, animCenterY + 10f * previewZoom), 2f)
                                drawCircle(StudioYellow, radius = 3.5f * previewZoom, center = Offset(animCenterX, animCenterY))
                                drawCircle(StudioYellow, radius = 3.5f * previewZoom, center = Offset(animCenterX, animCenterY - 20f * previewZoom))
                            }
                        }
                    }

                    // Feedback Toast Overlay
                    feedbackMessage?.let { msg ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(StudioPurpleDark)
                                .border(1.dp, StudioGreen, RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(msg, color = StudioGreen, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Live Transform Specs Overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(EngineSurface.copy(alpha = 0.9f))
                            .border(0.6.dp, StudioBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Pos: (${currentPosX.toInt()}, ${currentPosY.toInt()})  •  Rot: ${currentRotation.toInt()}°  •  Scale: (${String.format("%.1f", currentScaleX)}, ${String.format("%.1f", currentScaleY)})  •  Frame: $currentSpriteFrame",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // ----------------------------------------------------
            // Right Column: Keyframe Inspector & Property Fine-Tuning
            // ----------------------------------------------------
            Column(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .background(EngineSurface)
                    .border(0.6.dp, StudioBorder)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("خصائص الإطار المسجل (Keyframe Inspector)", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                val activeTrack = currentClip.tracks.find { it.id == selectedTrackId }
                if (activeTrack != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = EngineCardBg),
                        border = androidx.compose.foundation.BorderStroke(0.6.dp, StudioPurpleBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(activeTrack.iconVector, contentDescription = null, tint = activeTrack.color, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(activeTrack.propertyName, color = TextPrimary, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("الإطار المفتاحي الحالي: $currentFrameInt", color = TextSecondary, fontSize = 10.5.sp)
                        }
                    }

                    // Add Keyframe Button
                    Button(
                        onClick = {
                            val existing = activeTrack.keyframes.find { it.frame == currentFrameInt }
                            if (existing != null) {
                                existing.value += 5f
                            } else {
                                activeTrack.keyframes.add(AnimationKeyframe(currentFrameInt, 10f))
                            }
                            activeTrack.keyframes.sortBy { it.frame }
                            feedbackMessage = "تمت إضافة Keyframe لإطار $currentFrameInt"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(34.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة Keyframe لإطار $currentFrameInt", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Keyframe List for Selected Track
                    Text("قائمة الإطارات المفتاحية (Keyframes):", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(EngineBackground, RoundedCornerShape(8.dp))
                            .border(0.6.dp, StudioBorder, RoundedCornerShape(8.dp))
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(activeTrack.keyframes) { idx, kf ->
                            val isCurrentKf = kf.frame == currentFrameInt
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isCurrentKf) StudioPurpleDark else EngineCardBg)
                                    .border(0.6.dp, if (isCurrentKf) StudioPurpleLight else StudioBorder, RoundedCornerShape(6.dp))
                                    .clickable {
                                        currentFrameFloat = kf.frame.toFloat()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(StudioYellow)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Frame ${kf.frame}", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Value adjuster (- / +)
                                    IconButton(
                                        onClick = { kf.value -= 2f },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                                    }

                                    Text("${kf.value.toInt()}", color = StudioPurpleLight, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                                    IconButton(
                                        onClick = { kf.value += 2f },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                                    }

                                    IconButton(
                                        onClick = {
                                            activeTrack.keyframes.removeAt(idx)
                                        },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = StudioRed, modifier = Modifier.size(13.dp))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("اختر مساراً من التايم لاين لعرض الإطارات والخصائص", color = TextMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        // =========================================================================
        // 3. Bottom Section: Visual Timeline Dopesheet & Sprite Strip
        // =========================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(EngineSurface)
                .border(0.8.dp, StudioBorder)
        ) {
            // Header bar of Timeline
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EngineBackground)
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timeline, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("شريط المسارات والتايم لاين (Timeline Dopesheet Grid)", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Add Track Button
                Button(
                    onClick = {
                        val newTrackName = "Property_${currentClip.tracks.size + 1}"
                        currentClip.tracks.add(
                            AnimationTrack("t_${System.currentTimeMillis()}", newTrackName, Icons.Default.Tune, StudioYellow, mutableListOf(AnimationKeyframe(0, 0f)))
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPurpleDark),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة مسار", color = StudioPurpleLight, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Timeline Tracks + Scrubber Grid
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Track Names Sidebar
                Column(
                    modifier = Modifier
                        .width(150.dp)
                        .fillMaxHeight()
                        .background(EngineCardBg)
                        .border(0.6.dp, StudioBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                            .background(EngineBackground)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text("مسار الخصائص", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(currentClip.tracks) { track ->
                            val isSel = track.id == selectedTrackId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(30.dp)
                                    .background(if (isSel) StudioPurpleDark else Color.Transparent)
                                    .clickable { selectedTrackId = track.id }
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(track.iconVector, contentDescription = null, tint = track.color, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = track.propertyName,
                                    color = if (isSel) TextPrimary else TextSecondary,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Main Scrubber Canvas
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
                        // Frame Ticks Header
                        drawRect(EngineBackground, Offset(0f, 0f), Size(totalW, 26f))
                        for (f in 0..currentClip.durationFrames) {
                            val x = f * frameW
                            val isMajor = f % 5 == 0
                            drawLine(
                                color = if (isMajor) StudioPurpleLight.copy(alpha = 0.8f) else StudioBorder,
                                start = Offset(x, 0f),
                                end = Offset(x, if (isMajor) 26f else 14f),
                                strokeWidth = if (isMajor) 1f else 0.5f
                            )
                        }

                        // Track Row Lines & Keyframe Diamonds
                        currentClip.tracks.forEachIndexed { rowIdx, track ->
                            val rowY = 26f + (rowIdx * 30f) + 15f

                            // Row divider
                            drawLine(
                                color = StudioBorder.copy(alpha = 0.35f),
                                start = Offset(0f, rowY + 15f),
                                end = Offset(totalW, rowY + 15f),
                                strokeWidth = 0.5f
                            )

                            // Keyframe Diamonds
                            track.keyframes.forEach { kf ->
                                val kfX = kf.frame * frameW
                                drawCircle(
                                    color = StudioYellow,
                                    radius = 5.5f,
                                    center = Offset(kfX, rowY)
                                )
                                drawCircle(
                                    color = EngineSurface,
                                    radius = 2.5f,
                                    center = Offset(kfX, rowY)
                                )
                            }
                        }

                        // Red Playhead Cursor Line
                        val playheadX = currentFrameFloat * frameW
                        drawLine(
                            color = Color(0xFFEF4444),
                            start = Offset(playheadX, 0f),
                            end = Offset(playheadX, totalH),
                            strokeWidth = 2f
                        )
                        drawRect(
                            color = Color(0xFFEF4444),
                            topLeft = Offset(playheadX - 6f, 0f),
                            size = Size(12f, 12f)
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    // Dialog for Adding New Animation Clip
    // =========================================================================
    if (showNewClipDialog) {
        AlertDialog(
            onDismissRequest = { showNewClipDialog = false },
            title = { Text("إنشاء مقطع أنيميشن جديد", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("أدخل اسم المقطع ورسمه المستهدف:", fontSize = 11.5.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = newClipName,
                        onValueChange = { newClipName = it },
                        singleLine = true,
                        placeholder = { Text("مثال: Roll, Slide, Victory", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("اختر المظهر مسبق الإعداد (Preset):", fontSize = 11.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Idle" to "🧍", "Walk" to "🏃", "Attack" to "⚔️", "Jump" to "🦘").forEach { (preset, emoji) ->
                            val isSelected = newClipPreset == preset
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) StudioPurple else EngineCardBg)
                                    .border(0.6.dp, if (isSelected) StudioPurpleLight else StudioBorder, RoundedCornerShape(6.dp))
                                    .clickable { newClipPreset = preset }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("$emoji $preset", color = if (isSelected) Color.White else TextSecondary, fontSize = 10.5.sp)
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
                                    AnimationTrack("t1", "Position X", Icons.Default.OpenWith, StudioRed, mutableListOf(AnimationKeyframe(0, 0f), AnimationKeyframe(12, 10f), AnimationKeyframe(24, 0f))),
                                    AnimationTrack("t3", "Rotation", Icons.Default.RotateRight, StudioPurpleLight, mutableListOf(AnimationKeyframe(0, 0f), AnimationKeyframe(12, 15f), AnimationKeyframe(24, 0f)))
                                )
                            )
                        )
                        selectedClipIndex = clips.size - 1
                        newClipName = ""
                        showNewClipDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPurple)
                ) {
                    Text("إنشاء المقطع", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewClipDialog = false }) {
                    Text("إلغاء", color = TextMuted, fontSize = 11.sp)
                }
            },
            containerColor = EngineSurface,
            titleContentColor = TextPrimary
        )
    }
}

/**
 * Linear keyframe interpolation helper.
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

package com.example.ui.animation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.animation.ClipData
import com.example.engine.animation.IAnimationController
import com.example.ui.theme.*

@Composable
fun AnimationViewportPreview(
    activeClip: ClipData,
    currentFrame: Float,
    backend: IAnimationController,
    showOnionSkin: Boolean,
    showGrid: Boolean,
    showMotionPath: Boolean,
    showCrosshair: Boolean,
    onToggleOnionSkin: () -> Unit,
    onToggleGrid: () -> Unit,
    onToggleMotionPath: () -> Unit,
    onToggleCrosshair: () -> Unit,
    triggeredEvent: String?,
    modifier: Modifier = Modifier
) {
    var zoom by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Evaluate dynamic properties for current frame
    val posX = remember(currentFrame, activeClip) {
        val trk = activeClip.tracks.find { it.name.contains("Pos X", ignoreCase = true) || it.propertyPath.contains("position.x") }
        if (trk != null) backend.evaluateTrackAt(trk, currentFrame) else 0f
    }
    val posY = remember(currentFrame, activeClip) {
        val trk = activeClip.tracks.find { it.name.contains("Pos Y", ignoreCase = true) || it.propertyPath.contains("position.y") }
        if (trk != null) backend.evaluateTrackAt(trk, currentFrame) else 0f
    }
    val rot = remember(currentFrame, activeClip) {
        val trk = activeClip.tracks.find { it.name.contains("Rotation", ignoreCase = true) || it.propertyPath.contains("rotation") }
        if (trk != null) backend.evaluateTrackAt(trk, currentFrame) else 0f
    }
    val scaleX = remember(currentFrame, activeClip) {
        val trk = activeClip.tracks.find { it.name.contains("Scale X", ignoreCase = true) || it.propertyPath.contains("scale.x") }
        val v = if (trk != null) backend.evaluateTrackAt(trk, currentFrame) else 1f
        if (v == 0f) 1f else v
    }
    val scaleY = remember(currentFrame, activeClip) {
        val trk = activeClip.tracks.find { it.name.contains("Scale Y", ignoreCase = true) || it.name.contains("Scale", ignoreCase = true) || it.propertyPath.contains("scale.y") }
        val v = if (trk != null) backend.evaluateTrackAt(trk, currentFrame) else 1f
        if (v == 0f) 1f else v
    }
    val spriteFrame = remember(currentFrame, activeClip) {
        val trk = activeClip.tracks.find { it.name.contains("Sprite", ignoreCase = true) }
        if (trk != null) backend.evaluateTrackAt(trk, currentFrame).toInt().coerceAtLeast(0) else 0
    }
    val opacity = remember(currentFrame, activeClip) {
        val trk = activeClip.tracks.find { it.name.contains("Opacity", ignoreCase = true) }
        val v = if (trk != null) backend.evaluateTrackAt(trk, currentFrame) else 1f
        v.coerceIn(0f, 1f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EngineBackground)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    zoom = (zoom * gestureZoom).coerceIn(0.4f, 3.5f)
                    panOffset += pan
                }
            }
    ) {
        // Main 2D Drawing Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2f + panOffset.x
            val centerY = size.height / 2f + panOffset.y

            // 1. Grid
            if (showGrid) {
                val gridSize = 24f * zoom
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

            // 2. Center Crosshair Axes
            if (showCrosshair) {
                drawLine(StudioRed.copy(alpha = 0.35f), Offset(0f, centerY), Offset(size.width, centerY), 0.8f)
                drawLine(StudioGreen.copy(alpha = 0.35f), Offset(centerX, 0f), Offset(centerX, size.height), 0.8f)
            }

            // 3. Motion Trajectory Path (Trail)
            if (showMotionPath) {
                val path = Path()
                val steps = activeClip.durationFrames
                val trkX = activeClip.tracks.find { it.name.contains("Pos X", ignoreCase = true) }
                val trkY = activeClip.tracks.find { it.name.contains("Pos Y", ignoreCase = true) }

                for (f in 0..steps) {
                    val px = if (trkX != null) backend.evaluateTrackAt(trkX, f.toFloat()) else 0f
                    val py = if (trkY != null) backend.evaluateTrackAt(trkY, f.toFloat()) else 0f
                    val pt = Offset(centerX + px * zoom, centerY + py * zoom)
                    if (f == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                }

                drawPath(
                    path = path,
                    color = StudioYellow.copy(alpha = 0.45f),
                    style = Stroke(width = 1.6f * zoom, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f))
                )
            }

            // 4. 2D Onion Skinning (Ghost Previous and Next Frames)
            if (showOnionSkin) {
                val trkX = activeClip.tracks.find { it.name.contains("Pos X", ignoreCase = true) }
                val trkY = activeClip.tracks.find { it.name.contains("Pos Y", ignoreCase = true) }
                val trkRot = activeClip.tracks.find { it.name.contains("Rotation", ignoreCase = true) }

                // Previous Ghost
                if (currentFrame >= 3f) {
                    val prevF = currentFrame - 3f
                    val px = if (trkX != null) backend.evaluateTrackAt(trkX, prevF) else 0f
                    val py = if (trkY != null) backend.evaluateTrackAt(trkY, prevF) else 0f
                    val prot = if (trkRot != null) backend.evaluateTrackAt(trkRot, prevF) else 0f
                    val ghostPos = Offset(centerX + px * zoom, centerY + py * zoom)

                    rotate(degrees = prot, pivot = ghostPos) {
                        drawRoundRect(
                            color = StudioRed.copy(alpha = 0.22f),
                            topLeft = Offset(ghostPos.x - 22f * zoom, ghostPos.y - 22f * zoom),
                            size = Size(44f * zoom, 44f * zoom),
                            cornerRadius = CornerRadius(6f * zoom, 6f * zoom),
                            style = Stroke(width = 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 3f), 0f))
                        )
                    }
                }

                // Next Ghost
                if (currentFrame + 3f <= activeClip.durationFrames) {
                    val nextF = currentFrame + 3f
                    val nx = if (trkX != null) backend.evaluateTrackAt(trkX, nextF) else 0f
                    val ny = if (trkY != null) backend.evaluateTrackAt(trkY, nextF) else 0f
                    val nrot = if (trkRot != null) backend.evaluateTrackAt(trkRot, nextF) else 0f
                    val nextPos = Offset(centerX + nx * zoom, centerY + ny * zoom)

                    rotate(degrees = nrot, pivot = nextPos) {
                        drawRoundRect(
                            color = StudioGreen.copy(alpha = 0.22f),
                            topLeft = Offset(nextPos.x - 22f * zoom, nextPos.y - 22f * zoom),
                            size = Size(44f * zoom, 44f * zoom),
                            cornerRadius = CornerRadius(6f * zoom, 6f * zoom),
                            style = Stroke(width = 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 3f), 0f))
                        )
                    }
                }
            }

            // 5. Active Character Node Rendering
            val charCenterX = centerX + posX * zoom
            val charCenterY = centerY + posY * zoom
            val baseWidth = 52f * scaleX * zoom
            val baseHeight = 52f * scaleY * zoom

            // Ground Shadow
            drawOval(
                color = Color.Black.copy(alpha = 0.38f * opacity),
                topLeft = Offset(charCenterX - 24f * zoom, centerY + 28f * zoom),
                size = Size(48f * zoom, 10f * zoom)
            )

            rotate(degrees = rot, pivot = Offset(charCenterX, charCenterY)) {
                val spriteColorPalette = when (spriteFrame % 4) {
                    1 -> listOf(StudioGreen.copy(alpha = 0.9f * opacity), StudioPurpleDark.copy(alpha = opacity))
                    2 -> listOf(StudioOrange.copy(alpha = 0.9f * opacity), StudioPurpleDark.copy(alpha = opacity))
                    3 -> listOf(StudioRed.copy(alpha = 0.9f * opacity), StudioPurpleDark.copy(alpha = opacity))
                    else -> listOf(StudioPurpleDark.copy(alpha = opacity), StudioPurple.copy(alpha = opacity))
                }

                // Character Body Box
                drawRoundRect(
                    brush = Brush.linearGradient(spriteColorPalette),
                    topLeft = Offset(charCenterX - baseWidth / 2f, charCenterY - baseHeight / 2f),
                    size = Size(baseWidth, baseHeight),
                    cornerRadius = CornerRadius(8f * zoom, 8f * zoom)
                )

                // Outer Highlight Border
                drawRoundRect(
                    color = StudioPurpleLight.copy(alpha = opacity),
                    topLeft = Offset(charCenterX - baseWidth / 2f, charCenterY - baseHeight / 2f),
                    size = Size(baseWidth, baseHeight),
                    cornerRadius = CornerRadius(8f * zoom, 8f * zoom),
                    style = Stroke(width = 1.8f)
                )

                // Face Eyes
                val eyeOffset = 8f * zoom
                val eyeRadius = 3.5f * zoom
                val pupilRadius = 2f * zoom
                drawCircle(Color.White.copy(alpha = opacity), radius = eyeRadius, center = Offset(charCenterX - eyeOffset, charCenterY - eyeOffset))
                drawCircle(Color.White.copy(alpha = opacity), radius = eyeRadius, center = Offset(charCenterX + eyeOffset, charCenterY - eyeOffset))
                drawCircle(Color.Black.copy(alpha = opacity), radius = pupilRadius, center = Offset(charCenterX - eyeOffset + 0.8f, charCenterY - eyeOffset))
                drawCircle(Color.Black.copy(alpha = opacity), radius = pupilRadius, center = Offset(charCenterX + eyeOffset + 0.8f, charCenterY - eyeOffset))
            }
        }

        // =====================================================================
        // Viewport Top-Left Overlay: Quick Display Toggles
        // =====================================================================
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(6.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(EngineSurface.copy(alpha = 0.85f))
                .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Onion Skin Toggle
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (showOnionSkin) StudioPurpleDark else Color.Transparent)
                    .clickable { onToggleOnionSkin() }
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text("Onion", color = if (showOnionSkin) StudioPurpleLight else TextMuted, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
            }

            // Motion Path Toggle
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (showMotionPath) StudioPurpleDark else Color.Transparent)
                    .clickable { onToggleMotionPath() }
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text("Path", color = if (showMotionPath) StudioPurpleLight else TextMuted, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
            }

            // Grid Toggle
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (showGrid) StudioPurpleDark else Color.Transparent)
                    .clickable { onToggleGrid() }
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text("Grid", color = if (showGrid) StudioPurpleLight else TextMuted, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
            }

            // Crosshair Toggle
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (showCrosshair) StudioPurpleDark else Color.Transparent)
                    .clickable { onToggleCrosshair() }
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text("Axis", color = if (showCrosshair) StudioPurpleLight else TextMuted, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
            }
        }

        // =====================================================================
        // Viewport Top-Right Overlay: Zoom Controls & Center Button
        // =====================================================================
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(EngineSurface.copy(alpha = 0.85f))
                .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                .padding(horizontal = 3.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            IconButton(onClick = { zoom = (zoom - 0.2f).coerceAtLeast(0.4f) }, modifier = Modifier.size(18.dp)) {
                Icon(Icons.Default.Remove, contentDescription = "تصغير", tint = TextSecondary, modifier = Modifier.size(11.dp))
            }

            Text("${(zoom * 100).toInt()}%", color = TextSecondary, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace)

            IconButton(onClick = { zoom = (zoom + 0.2f).coerceAtMost(3.0f) }, modifier = Modifier.size(18.dp)) {
                Icon(Icons.Default.Add, contentDescription = "تكبير", tint = TextSecondary, modifier = Modifier.size(11.dp))
            }

            // Center / Reset View
            IconButton(
                onClick = {
                    zoom = 1.0f
                    panOffset = Offset.Zero
                },
                modifier = Modifier.size(18.dp)
            ) {
                Icon(Icons.Default.CenterFocusStrong, contentDescription = "توسيط الكائن", tint = StudioPurpleLight, modifier = Modifier.size(11.dp))
            }
        }

        // =====================================================================
        // Live Event Triggered Notification Bubble (Audio / Particle / Hitbox)
        // =====================================================================
        triggeredEvent?.let { evName ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(StudioPurpleDark)
                    .border(0.8.dp, StudioYellow, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = StudioYellow, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("حدث مشغل: $evName", color = StudioYellow, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Bottom Left Transform Specs Overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(EngineSurface.copy(alpha = 0.88f))
                .border(0.5.dp, StudioBorder, RoundedCornerShape(3.dp))
                .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Text(
                text = "X:${posX.toInt()} Y:${posY.toInt()} Rot:${rot.toInt()}° S:(${String.format("%.2f", scaleX)}, ${String.format("%.2f", scaleY)}) Opacity:${(opacity * 100).toInt()}%",
                color = TextSecondary,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

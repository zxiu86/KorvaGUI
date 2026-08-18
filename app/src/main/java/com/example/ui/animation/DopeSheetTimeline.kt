package com.example.ui.animation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.animation.*
import com.example.ui.theme.*

@Composable
fun DopeSheetTimeline(
    activeClip: ClipData,
    currentFrame: Float,
    selectedTrackId: String?,
    selectedKeyframeIds: Set<String>,
    snapEnabled: Boolean,
    onFrameSelected: (Float) -> Unit,
    onSelectTrack: (String) -> Unit,
    onToggleTrackVisibility: (String) -> Unit,
    onToggleTrackLock: (String) -> Unit,
    onAddKeyframeToTrack: (TrackData, Int) -> Unit,
    onSelectKeyframe: (String, Boolean) -> Unit,
    onMoveKeyframe: (TrackData, KeyframeData, Int) -> Unit,
    onKeyframeContextMenu: (TrackData, KeyframeData) -> Unit,
    onAddTrack: () -> Unit,
    onAddEvent: () -> Unit,
    onAddMarker: () -> Unit,
    modifier: Modifier = Modifier
) {
    var frameWidthDp by remember { mutableFloatStateOf(24f) }
    val hScrollState = rememberScrollState()
    val vScrollState = rememberScrollState()

    val totalFrames = activeClip.durationFrames.coerceAtLeast(1)
    val timelineWidthDp = (totalFrames + 5) * frameWidthDp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(EngineSurface)
            .border(width = 0.5.dp, color = StudioBorder)
    ) {
        // =====================================================================
        // 1. Timeline Toolbar Strip (Add Track, Add Event, Marker, Zoom In/Out)
        // =====================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .background(EngineBackground)
                .border(0.5.dp, StudioBorder)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Title & Track Actions
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Timeline, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("Dope Sheet", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                // Add Track Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(StudioPurpleDark)
                        .clickable { onAddTrack() }
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(10.dp))
                        Text("مسار", color = StudioPurpleLight, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Add Event Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(EngineCardBg)
                        .border(0.5.dp, StudioBorder, RoundedCornerShape(3.dp))
                        .clickable { onAddEvent() }
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = StudioYellow, modifier = Modifier.size(10.dp))
                        Text("حدث (Event)", color = StudioYellow, fontSize = 8.5.sp)
                    }
                }

                // Add Marker Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(EngineCardBg)
                        .border(0.5.dp, StudioBorder, RoundedCornerShape(3.dp))
                        .clickable { onAddMarker() }
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = StudioGreen, modifier = Modifier.size(10.dp))
                        Text("علامة", color = StudioGreen, fontSize = 8.5.sp)
                    }
                }
            }

            // Right: Timeline Zoom Controls
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = { frameWidthDp = (frameWidthDp - 4f).coerceAtLeast(14f) }, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.ZoomOut, contentDescription = "تصغير التايم لاين", tint = TextSecondary, modifier = Modifier.size(12.dp))
                }
                Text("${frameWidthDp.toInt()}px", color = TextMuted, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace)
                IconButton(onClick = { frameWidthDp = (frameWidthDp + 4f).coerceAtMost(48f) }, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "تكبير التايم لاين", tint = TextSecondary, modifier = Modifier.size(12.dp))
                }
            }
        }

        // =====================================================================
        // 2. Main Dope Sheet Grid (Split: Left Track Headers | Right Frames Grid)
        // =====================================================================
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Column: Track Headers (Fixed width 120dp)
            Column(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .background(EngineSurface)
                    .border(width = 0.5.dp, color = StudioBorder)
            ) {
                // Ruler Corner Blank
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(EngineBackground)
                        .border(0.5.dp, StudioBorder)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text("المسارات / الخصائص", color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }

                // Scrollable Track Headers
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(vScrollState)
                ) {
                    // Events Header Row
                    if (activeClip.events.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                                .background(StudioPurpleBg.copy(alpha = 0.4f))
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = StudioYellow, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("الأحداث (${activeClip.events.size})", color = StudioYellow, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Properties Tracks
                    activeClip.tracks.forEach { track ->
                        val isSelected = track.id == selectedTrackId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                                .background(if (isSelected) StudioPurpleDark.copy(alpha = 0.8f) else Color.Transparent)
                                .border(0.3.dp, if (isSelected) StudioPurpleLight else StudioBorder.copy(alpha = 0.4f))
                                .clickable { onSelectTrack(track.id) }
                                .padding(horizontal = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(track.iconVector, contentDescription = null, tint = track.displayColor, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = track.name,
                                    color = if (isSelected) Color.White else TextPrimary,
                                    fontSize = 8.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Quick Add Keyframe on Track Button
                            IconButton(
                                onClick = { onAddKeyframeToTrack(track, currentFrame.toInt()) },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = "إضافة فريم", tint = track.displayColor, modifier = Modifier.size(11.dp))
                            }
                        }
                    }
                }
            }

            // Right Column: Clickable/Draggable Ruler & Keyframes Grid Canvas
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .horizontalScroll(hScrollState)
            ) {
                // -------------------------------------------------------------
                // A. Frame Numbers Ruler Strip (Touch Draggable Scrubber)
                // -------------------------------------------------------------
                Box(
                    modifier = Modifier
                        .width(timelineWidthDp.dp)
                        .height(24.dp)
                        .background(EngineBackground)
                        .border(0.5.dp, StudioBorder)
                        .pointerInput(frameWidthDp, totalFrames, snapEnabled) {
                            detectTapGestures { offset ->
                                val clickedFrame = (offset.x / (frameWidthDp * density)).coerceIn(0f, totalFrames.toFloat())
                                onFrameSelected(if (snapEnabled) kotlin.math.round(clickedFrame) else clickedFrame)
                            }
                        }
                        .pointerInput(frameWidthDp, totalFrames, snapEnabled) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val draggedFrame = (change.position.x / (frameWidthDp * density)).coerceIn(0f, totalFrames.toFloat())
                                onFrameSelected(if (snapEnabled) kotlin.math.round(draggedFrame) else draggedFrame)
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val pxPerFrame = frameWidthDp.dp.toPx()

                        // Draw ticks & numbers
                        for (f in 0..totalFrames) {
                            val x = f * pxPerFrame
                            val isMajor = f % 5 == 0

                            drawLine(
                                color = if (isMajor) TextSecondary else StudioBorder,
                                start = Offset(x, if (isMajor) 10f else 16f),
                                end = Offset(x, size.height),
                                strokeWidth = if (isMajor) 1.2f else 0.6f
                            )
                        }
                    }

                    // Render Frame Numbers & Markers Text
                    Row(modifier = Modifier.fillMaxSize()) {
                        for (f in 0..totalFrames) {
                            val isMajor = f % 5 == 0
                            val marker = activeClip.markers.find { it.frame == f }
                            Box(
                                modifier = Modifier
                                    .width(frameWidthDp.dp)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                if (marker != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color(marker.colorHex))
                                            .padding(horizontal = 2.dp, vertical = 1.dp)
                                    ) {
                                        Text(marker.label, color = Color.Black, fontSize = 6.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                } else if (isMajor) {
                                    Text("$f", color = TextMuted, fontSize = 7.5.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // B. Tracks & Keyframes Canvas Area
                // -------------------------------------------------------------
                Box(
                    modifier = Modifier
                        .width(timelineWidthDp.dp)
                        .weight(1f)
                        .background(EngineCardBg)
                        .verticalScroll(vScrollState)
                ) {
                    // Vertical Grid Lines for each frame
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val pxPerFrame = frameWidthDp.dp.toPx()
                        for (f in 0..totalFrames) {
                            val x = f * pxPerFrame
                            val isMajor = f % 5 == 0
                            drawLine(
                                color = if (isMajor) StudioBorder.copy(alpha = 0.35f) else StudioBorder.copy(alpha = 0.15f),
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = 0.5f
                            )
                        }
                    }

                    // Track Rows Container
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Events Strip
                        if (activeClip.events.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(22.dp)
                                    .background(StudioPurpleBg.copy(alpha = 0.2f))
                            ) {
                                activeClip.events.forEach { ev ->
                                    val leftOffset = (ev.frame * frameWidthDp).dp
                                    Box(
                                        modifier = Modifier
                                            .offset(x = leftOffset - 5.dp)
                                            .align(Alignment.CenterStart)
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(StudioYellow)
                                            .border(0.5.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Bolt, contentDescription = ev.name, tint = Color.Black, modifier = Modifier.size(10.dp))
                                    }
                                }
                            }
                        }

                        // Property Tracks Rows
                        activeClip.tracks.forEach { track ->
                            val isSelectedTrack = track.id == selectedTrackId
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .background(if (isSelectedTrack) StudioPurpleGlass else Color.Transparent)
                                    .border(0.3.dp, StudioBorder.copy(alpha = 0.3f))
                            ) {
                                // Draw connection lines between keyframes
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val pxPerFrame = frameWidthDp.dp.toPx()
                                    val centerY = size.height / 2f
                                    val sortedKf = track.keyframes.sortedBy { it.frame }
                                    if (sortedKf.size >= 2) {
                                        for (i in 0 until sortedKf.size - 1) {
                                            val x1 = sortedKf[i].frame * pxPerFrame
                                            val x2 = sortedKf[i + 1].frame * pxPerFrame
                                            drawLine(
                                                color = track.displayColor.copy(alpha = 0.5f),
                                                start = Offset(x1, centerY),
                                                end = Offset(x2, centerY),
                                                strokeWidth = 1.5f
                                            )
                                        }
                                    }
                                }

                                // Render Keyframe Diamonds (◆) with Touch Interaction & Context Menu
                                track.keyframes.forEach { kf ->
                                    val isSelectedKf = selectedKeyframeIds.contains(kf.id)
                                    val leftOffset = (kf.frame * frameWidthDp).dp

                                    Box(
                                        modifier = Modifier
                                            .offset(x = leftOffset - 7.dp)
                                            .align(Alignment.CenterStart)
                                            .size(16.dp)
                                            .pointerInput(kf.id, track.id, snapEnabled) {
                                                detectTapGestures(
                                                    onTap = {
                                                        onSelectKeyframe(kf.id, false)
                                                        onFrameSelected(kf.frame.toFloat())
                                                    },
                                                    onLongPress = {
                                                        onSelectKeyframe(kf.id, false)
                                                        onKeyframeContextMenu(track, kf)
                                                    }
                                                )
                                            }
                                            .pointerInput(kf.id, track.id, snapEnabled) {
                                                detectDragGestures { change, dragAmount ->
                                                    change.consume()
                                                    val deltaFrames = dragAmount.x / (frameWidthDp * density)
                                                    val newTargetFrame = (kf.frame + deltaFrames).toInt().coerceIn(0, totalFrames)
                                                    if (newTargetFrame != kf.frame) {
                                                        onMoveKeyframe(track, kf, newTargetFrame)
                                                    }
                                                }
                                            }
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val path = Path().apply {
                                                moveTo(size.width / 2f, 1f)
                                                lineTo(size.width - 1f, size.height / 2f)
                                                lineTo(size.width / 2f, size.height - 1f)
                                                lineTo(1f, size.height / 2f)
                                                close()
                                            }

                                            // Diamond fill
                                            drawPath(
                                                path = path,
                                                color = if (isSelectedKf) Color.White else track.displayColor,
                                                style = Fill
                                            )

                                            // Outer border highlight
                                            drawPath(
                                                path = path,
                                                color = if (isSelectedKf) StudioPurple else Color.Black.copy(alpha = 0.5f),
                                                style = Stroke(width = if (isSelectedKf) 1.8f else 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // =========================================================
                    // 3. Draggable Playhead Scrubber Cursor Line
                    // =========================================================
                    val playheadOffset = (currentFrame * frameWidthDp).dp
                    Box(
                        modifier = Modifier
                            .offset(x = playheadOffset - 1.dp)
                            .fillMaxHeight()
                            .width(2.dp)
                            .background(StudioRed)
                    )

                    // Scrubber Arrow Top Marker (▼)
                    Box(
                        modifier = Modifier
                            .offset(x = playheadOffset - 5.dp, y = 0.dp)
                            .size(10.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val path = Path().apply {
                                moveTo(0f, 0f)
                                lineTo(size.width, 0f)
                                lineTo(size.width / 2f, size.height)
                                close()
                            }
                            drawPath(path = path, color = StudioRed, style = Fill)
                        }
                    }
                }
            }
        }
    }
}

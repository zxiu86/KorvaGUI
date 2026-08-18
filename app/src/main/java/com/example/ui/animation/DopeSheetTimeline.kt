package com.example.ui.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.animation.*
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun DopeSheetTimeline(
    activeClip: ClipData,
    currentFrame: Float,
    selectedTrackId: String?,
    selectedKeyframeIds: Set<String>,
    snapMode: SnapMode,
    loopRangeStart: Int?,
    loopRangeEnd: Int?,
    onFrameSelected: (Float) -> Unit,
    onSelectTrack: (String) -> Unit,
    onToggleTrackVisibility: (String) -> Unit,
    onToggleTrackLock: (String) -> Unit,
    onToggleTrackSolo: (String) -> Unit,
    onToggleTrackMute: (String) -> Unit,
    onToggleTrackFocus: (String) -> Unit,
    onAddKeyframeToTrack: (TrackData, Int) -> Unit,
    onSelectKeyframe: (String, Boolean) -> Unit,
    onSelectAllKeyframes: () -> Unit,
    onClearKeyframeSelection: () -> Unit,
    onMoveKeyframesBatch: (deltaFrames: Int) -> Unit,
    onMoveKeyframe: (TrackData, KeyframeData, Int) -> Unit,
    onKeyframeContextMenu: (TrackData, KeyframeData) -> Unit,
    onKeyframeStackSelected: (Int, List<Pair<TrackData, KeyframeData>>) -> Unit,
    onCopySelectedKeyframes: () -> Unit,
    onPasteKeyframes: () -> Unit,
    onDuplicateSelectedKeyframes: () -> Unit,
    onDeleteSelectedKeyframes: () -> Unit,
    onOpenSmartRetiming: () -> Unit,
    onStretchTiming: (Int) -> Unit,
    onCompressTiming: (Int) -> Unit,
    onDistributeEvenly: () -> Unit,
    onReverseTiming: () -> Unit,
    onHoldKeyframes: () -> Unit,
    onSetLoopIn: (Int) -> Unit,
    onSetLoopOut: (Int) -> Unit,
    onClearLoopRange: () -> Unit,
    onAddTrack: () -> Unit,
    onAddEvent: () -> Unit,
    onAddMarker: () -> Unit,
    modifier: Modifier = Modifier
) {
    var frameWidthDp by remember { mutableFloatStateOf(24f) }
    var isMultiSelectMode by remember { mutableStateOf(false) }
    var showMiniOverview by remember { mutableStateOf(true) }
    var isTransformGroupExpanded by remember { mutableStateOf(true) }

    // Real-time drag feedback HUD state
    var isDraggingKeyframe by remember { mutableStateOf(false) }
    var dragOriginalFrame by remember { mutableIntStateOf(0) }
    var dragCurrentFrame by remember { mutableIntStateOf(0) }
    var dragTrackName by remember { mutableStateOf("") }

    val hScrollState = rememberScrollState()
    val vScrollState = rememberScrollState()
    val density = LocalDensity.current

    val totalFrames = activeClip.durationFrames.coerceAtLeast(1)
    val timelineWidthDp = (totalFrames + 8) * frameWidthDp

    // Pinch-to-zoom gesture state
    val transformableState = rememberTransformableState { zoomChange, _, _ ->
        val newWidth = (frameWidthDp * zoomChange).coerceIn(12f, 72f)
        frameWidthDp = newWidth
    }

    // Helper for snapping
    fun calculateSnappedFrame(rawFrame: Float): Float {
        return when (snapMode) {
            SnapMode.SNAP_FRAME -> rawFrame.roundToInt().toFloat()
            SnapMode.SNAP_KEYFRAME -> {
                val allKfFrames = activeClip.tracks.flatMap { it.keyframes }.map { it.frame.toFloat() }
                val closest = allKfFrames.minByOrNull { kotlin.math.abs(it - rawFrame) }
                if (closest != null && kotlin.math.abs(closest - rawFrame) < 0.6f) closest else rawFrame.roundToInt().toFloat()
            }
            SnapMode.SNAP_MARKER -> {
                val allMarkerFrames = activeClip.markers.map { it.frame.toFloat() }
                val closest = allMarkerFrames.minByOrNull { kotlin.math.abs(it - rawFrame) }
                if (closest != null && kotlin.math.abs(closest - rawFrame) < 0.8f) closest else rawFrame.roundToInt().toFloat()
            }
            SnapMode.FREE_MOVE -> rawFrame
        }
    }

    // Separate transform tracks and other tracks for hierarchical grouping
    val transformTracks = remember(activeClip.tracks) {
        activeClip.tracks.filter { it.category == TrackCategory.TRANSFORM }
    }
    val otherTracks = remember(activeClip.tracks) {
        activeClip.tracks.filter { it.category != TrackCategory.TRANSFORM }
    }

    // Aggregated transform keyframes for collapsed group row
    val transformAggregatedKeyframes = remember(transformTracks) {
        transformTracks.flatMap { it.keyframes }.groupBy { it.frame }
    }

    val anyTrackFocused = remember(activeClip.tracks) {
        activeClip.tracks.any { it.isFocused }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(EngineSurface)
            .border(width = 0.5.dp, color = StudioBorder)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // =================================================================
            // 1. Dope Sheet Control Bar (Add Tracks, Multi-Select Toggle, Zoom)
            // =================================================================
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
                            .border(0.5.dp, StudioPurpleLight.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                            .clickable { onAddTrack() }
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(2.dp))
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
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("حدث", color = StudioYellow, fontSize = 8.5.sp)
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
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("علامة", color = StudioGreen, fontSize = 8.5.sp)
                        }
                    }

                    // Multi-Select Mode Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isMultiSelectMode) StudioPurple else EngineCardBg)
                            .border(0.5.dp, if (isMultiSelectMode) StudioPurpleLight else StudioBorder, RoundedCornerShape(3.dp))
                            .clickable { isMultiSelectMode = !isMultiSelectMode }
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SelectAll, contentDescription = null, tint = if (isMultiSelectMode) Color.White else TextSecondary, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("تحديد متعدد", color = if (isMultiSelectMode) Color.White else TextSecondary, fontSize = 8.5.sp, fontWeight = if (isMultiSelectMode) FontWeight.Bold else FontWeight.Normal)
                        }
                    }

                    // Mini Overview Strip Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (showMiniOverview) StudioPurpleDark else EngineCardBg)
                            .border(0.5.dp, if (showMiniOverview) StudioPurpleLight else StudioBorder, RoundedCornerShape(3.dp))
                            .clickable { showMiniOverview = !showMiniOverview }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = "خريطة التايم لاين", tint = if (showMiniOverview) StudioPurpleLight else TextMuted, modifier = Modifier.size(11.dp))
                    }
                }

                // Right: Zoom Controls & Presets
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    // Zoom Presets
                    listOf(14f to "S", 24f to "M", 40f to "L").forEach { (width, lbl) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (frameWidthDp.toInt() == width.toInt()) StudioPurple else Color.Transparent)
                                .clickable { frameWidthDp = width }
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(lbl, color = if (frameWidthDp.toInt() == width.toInt()) Color.White else TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(onClick = { frameWidthDp = (frameWidthDp - 4f).coerceAtLeast(12f) }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.ZoomOut, contentDescription = "تصغير", tint = TextSecondary, modifier = Modifier.size(12.dp))
                    }
                    Text("${frameWidthDp.toInt()}px", color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    IconButton(onClick = { frameWidthDp = (frameWidthDp + 4f).coerceAtMost(72f) }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.ZoomIn, contentDescription = "تكبير", tint = TextSecondary, modifier = Modifier.size(12.dp))
                    }
                }
            }

            // =================================================================
            // 2. Mini Timeline Overview Navigator Strip
            // =================================================================
            if (showMiniOverview) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .background(Color(0xFF0F0F14))
                        .border(0.5.dp, StudioBorder)
                        .pointerInput(totalFrames) {
                            detectTapGestures { offset ->
                                val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                                val targetF = fraction * totalFrames
                                onFrameSelected(targetF)
                            }
                        }
                        .pointerInput(totalFrames) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                                val targetF = fraction * totalFrames
                                onFrameSelected(targetF)
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Draw track keyframe mini dots
                        activeClip.tracks.forEachIndexed { tIdx, track ->
                            val y = 3f + (tIdx * 2.5f) % (h - 6f)
                            track.keyframes.forEach { kf ->
                                val x = (kf.frame.toFloat() / totalFrames) * w
                                drawCircle(
                                    color = track.displayColor.copy(alpha = 0.7f),
                                    radius = 1.5f,
                                    center = Offset(x, y)
                                )
                            }
                        }

                        // Draw Loop Range in Mini Overview
                        if (loopRangeStart != null && loopRangeEnd != null) {
                            val loopX1 = (loopRangeStart.toFloat() / totalFrames) * w
                            val loopX2 = (loopRangeEnd.toFloat() / totalFrames) * w
                            drawRect(
                                color = StudioPurple.copy(alpha = 0.25f),
                                topLeft = Offset(loopX1, 0f),
                                size = Size(loopX2 - loopX1, h)
                            )
                        }

                        // Draw Current Playhead Line in Mini Overview
                        val playheadMiniX = (currentFrame / totalFrames) * w
                        drawLine(
                            color = StudioRed,
                            start = Offset(playheadMiniX, 0f),
                            end = Offset(playheadMiniX, h),
                            strokeWidth = 1.5f
                        )
                    }

                    // Loop In/Out Buttons overlay
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "نظرة عامة (Overview 0..${totalFrames}f)",
                            color = TextMuted,
                            fontSize = 7.5.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(EngineCardBg)
                                    .clickable { onSetLoopIn(currentFrame.toInt()) }
                                    .padding(horizontal = 3.dp, vertical = 1.dp)
                            ) {
                                Text("[ In", color = StudioPurpleLight, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(EngineCardBg)
                                    .clickable { onSetLoopOut(currentFrame.toInt()) }
                                    .padding(horizontal = 3.dp, vertical = 1.dp)
                            ) {
                                Text("Out ]", color = StudioPurpleLight, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                            }

                            if (loopRangeStart != null || loopRangeEnd != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(StudioRed.copy(alpha = 0.2f))
                                        .clickable { onClearLoopRange() }
                                        .padding(horizontal = 3.dp, vertical = 1.dp)
                                ) {
                                    Text("✕", color = StudioRed, fontSize = 7.5.sp)
                                }
                            }
                        }
                    }
                }
            }

            // =================================================================
            // 3. Main Dope Sheet Grid (Left: Headers | Right: Frames & Ruler)
            // =================================================================
            Row(modifier = Modifier.fillMaxSize()) {
                // -------------------------------------------------------------
                // Left Column: Track Headers with Hierarchy, Focus, Solo, Mute, Lock
                // -------------------------------------------------------------
                Column(
                    modifier = Modifier
                        .width(136.dp)
                        .fillMaxHeight()
                        .background(EngineSurface)
                        .border(width = 0.5.dp, color = StudioBorder)
                ) {
                    // Corner Ruler Label
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .background(EngineBackground)
                            .border(0.5.dp, StudioBorder)
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("المسار / الخاصية", color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            if (selectedKeyframeIds.isNotEmpty()) {
                                Text("${selectedKeyframeIds.size} محدد", color = StudioPurpleLight, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
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

                        // -----------------------------------------------------
                        // Transform Parent Group Header (Collapsible)
                        // -----------------------------------------------------
                        if (transformTracks.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(22.dp)
                                    .background(EngineCardBg)
                                    .border(0.3.dp, StudioBorder)
                                    .clickable { isTransformGroupExpanded = !isTransformGroupExpanded }
                                    .padding(horizontal = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isTransformGroupExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = StudioPurpleLight,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("التحول (Transform)", color = TextPrimary, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("${transformTracks.size}", color = TextMuted, fontSize = 7.5.sp, fontFamily = FontFamily.Monospace)
                            }

                            if (isTransformGroupExpanded) {
                                transformTracks.forEach { track ->
                                    TrackHeaderItem(
                                        track = track,
                                        isSelected = track.id == selectedTrackId,
                                        isDimmed = anyTrackFocused && !track.isFocused,
                                        currentFrame = currentFrame.toInt(),
                                        onSelectTrack = { onSelectTrack(track.id) },
                                        onAddKeyframe = { onAddKeyframeToTrack(track, currentFrame.toInt()) },
                                        onToggleSolo = { onToggleTrackSolo(track.id) },
                                        onToggleMute = { onToggleTrackMute(track.id) },
                                        onToggleLock = { onToggleTrackLock(track.id) },
                                        onToggleFocus = { onToggleTrackFocus(track.id) }
                                    )
                                }
                            }
                        }

                        // Other Tracks
                        otherTracks.forEach { track ->
                            TrackHeaderItem(
                                track = track,
                                isSelected = track.id == selectedTrackId,
                                isDimmed = anyTrackFocused && !track.isFocused,
                                currentFrame = currentFrame.toInt(),
                                onSelectTrack = { onSelectTrack(track.id) },
                                onAddKeyframe = { onAddKeyframeToTrack(track, currentFrame.toInt()) },
                                onToggleSolo = { onToggleTrackSolo(track.id) },
                                onToggleMute = { onToggleTrackMute(track.id) },
                                onToggleLock = { onToggleTrackLock(track.id) },
                                onToggleFocus = { onToggleTrackFocus(track.id) }
                            )
                        }
                    }
                }

                // -------------------------------------------------------------
                // Right Column: Clickable/Draggable Ruler & Keyframes Grid Canvas
                // -------------------------------------------------------------
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .horizontalScroll(hScrollState)
                        .transformable(state = transformableState)
                ) {
                    // ---------------------------------------------------------
                    // A. Frame Numbers Ruler Strip (Touch Draggable Scrubber)
                    // ---------------------------------------------------------
                    Box(
                        modifier = Modifier
                            .width(timelineWidthDp.dp)
                            .height(24.dp)
                            .background(EngineBackground)
                            .border(0.5.dp, StudioBorder)
                            .pointerInput(frameWidthDp, totalFrames, snapMode) {
                                detectTapGestures { offset ->
                                    val rawF = (offset.x / (frameWidthDp * density.density)).coerceIn(0f, totalFrames.toFloat())
                                    onFrameSelected(calculateSnappedFrame(rawF))
                                }
                            }
                            .pointerInput(frameWidthDp, totalFrames, snapMode) {
                                detectDragGestures { change, _ ->
                                    change.consume()
                                    val rawF = (change.position.x / (frameWidthDp * density.density)).coerceIn(0f, totalFrames.toFloat())
                                    onFrameSelected(calculateSnappedFrame(rawF))
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val pxPerFrame = frameWidthDp.dp.toPx()

                            // Draw Loop Range Highlight
                            if (loopRangeStart != null && loopRangeEnd != null) {
                                val x1 = loopRangeStart * pxPerFrame
                                val x2 = loopRangeEnd * pxPerFrame
                                drawRect(
                                    color = StudioPurple.copy(alpha = 0.2f),
                                    topLeft = Offset(x1, 0f),
                                    size = Size(x2 - x1, size.height)
                                )
                            }

                            // Draw ticks & numbers
                            for (f in 0..totalFrames) {
                                val x = f * pxPerFrame
                                val isMajor = f % 5 == 0
                                val isSecond = activeClip.fps > 0 && f % activeClip.fps == 0

                                drawLine(
                                    color = if (isSecond) StudioPurpleLight else if (isMajor) TextSecondary else StudioBorder,
                                    start = Offset(x, if (isSecond) 6f else if (isMajor) 10f else 16f),
                                    end = Offset(x, size.height),
                                    strokeWidth = if (isSecond) 1.5f else if (isMajor) 1.0f else 0.5f
                                )
                            }
                        }

                        // Render Frame Numbers & Markers Text
                        Row(modifier = Modifier.fillMaxSize()) {
                            for (f in 0..totalFrames) {
                                val isMajor = f % 5 == 0
                                val isSecond = activeClip.fps > 0 && f % activeClip.fps == 0
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
                                    } else if (isSecond) {
                                        Text("${f / activeClip.fps}s", color = StudioPurpleLight, fontSize = 7.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    } else if (isMajor) {
                                        Text("$f", color = TextMuted, fontSize = 7.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }

                    // ---------------------------------------------------------
                    // B. Tracks & Keyframes Canvas Area
                    // ---------------------------------------------------------
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

                            // Draw Loop Region in grid
                            if (loopRangeStart != null && loopRangeEnd != null) {
                                val x1 = loopRangeStart * pxPerFrame
                                val x2 = loopRangeEnd * pxPerFrame
                                drawRect(
                                    color = StudioPurple.copy(alpha = 0.08f),
                                    topLeft = Offset(x1, 0f),
                                    size = Size(x2 - x1, size.height)
                                )
                            }

                            for (f in 0..totalFrames) {
                                val x = f * pxPerFrame
                                val isMajor = f % 5 == 0
                                val isSecond = activeClip.fps > 0 && f % activeClip.fps == 0
                                drawLine(
                                    color = if (isSecond) StudioPurpleLight.copy(alpha = 0.3f) else if (isMajor) StudioBorder.copy(alpha = 0.35f) else StudioBorder.copy(alpha = 0.12f),
                                    start = Offset(x, 0f),
                                    end = Offset(x, size.height),
                                    strokeWidth = if (isSecond) 0.8f else 0.5f
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
                                                .offset(x = leftOffset - 6.dp)
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

                            // -------------------------------------------------
                            // Transform Group Keyframes Row (Collapsed summary or full)
                            // -------------------------------------------------
                            if (transformTracks.isNotEmpty()) {
                                if (!isTransformGroupExpanded) {
                                    // Render Collapsed Transform Summary Row
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(22.dp)
                                            .background(StudioPurpleDark.copy(alpha = 0.3f))
                                            .border(0.3.dp, StudioBorder)
                                    ) {
                                        transformAggregatedKeyframes.forEach { (frame, kfList) ->
                                            val leftOffset = (frame * frameWidthDp).dp
                                            Box(
                                                modifier = Modifier
                                                    .offset(x = leftOffset - 10.dp)
                                                    .align(Alignment.CenterStart)
                                                    .size(24.dp)
                                                    .clickable {
                                                        val items = kfList.mapNotNull { kf ->
                                                            val tr = transformTracks.find { it.keyframes.any { k -> k.id == kf.id } }
                                                            if (tr != null) Pair(tr, kf) else null
                                                        }
                                                        if (items.size == 1) {
                                                            onSelectKeyframe(items[0].second.id, isMultiSelectMode)
                                                        } else if (items.size > 1) {
                                                            onKeyframeStackSelected(frame, items)
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clip(RoundedCornerShape(3.dp))
                                                        .background(StudioPurple)
                                                        .border(0.5.dp, Color.White, RoundedCornerShape(3.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("${kfList.size}", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Expanded Transform Rows
                                    transformTracks.forEach { track ->
                                        TrackGridRow(
                                            track = track,
                                            totalFrames = totalFrames,
                                            frameWidthDp = frameWidthDp,
                                            isSelectedTrack = track.id == selectedTrackId,
                                            isDimmed = anyTrackFocused && !track.isFocused,
                                            selectedKeyframeIds = selectedKeyframeIds,
                                            isMultiSelectMode = isMultiSelectMode,
                                            onSelectTrack = { onSelectTrack(track.id) },
                                            onSelectKeyframe = onSelectKeyframe,
                                            onFrameSelected = onFrameSelected,
                                            onMoveKeyframe = onMoveKeyframe,
                                            onKeyframeContextMenu = onKeyframeContextMenu,
                                            onDragFeedback = { dragging, orig, curr, name ->
                                                isDraggingKeyframe = dragging
                                                dragOriginalFrame = orig
                                                dragCurrentFrame = curr
                                                dragTrackName = name
                                            }
                                        )
                                    }
                                }
                            }

                            // Other Tracks Rows
                            otherTracks.forEach { track ->
                                TrackGridRow(
                                    track = track,
                                    totalFrames = totalFrames,
                                    frameWidthDp = frameWidthDp,
                                    isSelectedTrack = track.id == selectedTrackId,
                                    isDimmed = anyTrackFocused && !track.isFocused,
                                    selectedKeyframeIds = selectedKeyframeIds,
                                    isMultiSelectMode = isMultiSelectMode,
                                    onSelectTrack = { onSelectTrack(track.id) },
                                    onSelectKeyframe = onSelectKeyframe,
                                    onFrameSelected = onFrameSelected,
                                    onMoveKeyframe = onMoveKeyframe,
                                    onKeyframeContextMenu = onKeyframeContextMenu,
                                    onDragFeedback = { dragging, orig, curr, name ->
                                        isDraggingKeyframe = dragging
                                        dragOriginalFrame = orig
                                        dragCurrentFrame = curr
                                        dragTrackName = name
                                    }
                                )
                            }
                        }

                        // =====================================================
                        // 4. Draggable Playhead Scrubber Cursor Line
                        // =====================================================
                        val playheadOffset = (currentFrame * frameWidthDp).dp
                        Box(
                            modifier = Modifier
                                .offset(x = playheadOffset - 1.dp)
                                .fillMaxHeight()
                                .width(2.dp)
                                .background(StudioRed)
                        )

                        // Big Touch Scrubber Top Marker (▼) with Expanded Touch Target (44dp x 44dp hit area)
                        Box(
                            modifier = Modifier
                                .offset(x = playheadOffset - 22.dp, y = 0.dp)
                                .size(44.dp)
                                .pointerInput(frameWidthDp, totalFrames, snapMode) {
                                    detectDragGestures { change, _ ->
                                        change.consume()
                                        val rawF = (change.position.x / (frameWidthDp * density.density)).coerceIn(0f, totalFrames.toFloat())
                                        onFrameSelected(calculateSnappedFrame(rawF))
                                    }
                                },
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Canvas(modifier = Modifier.size(14.dp, 10.dp)) {
                                val path = Path().apply {
                                    moveTo(0f, 0f)
                                    lineTo(size.width, 0f)
                                    lineTo(size.width / 2f, size.height)
                                    close()
                                }
                                drawPath(path = path, color = StudioRed, style = Fill)
                                drawPath(path = path, color = Color.White, style = Stroke(width = 0.8f))
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 5. Floating Real-Time Drag Timing HUD
        // =====================================================================
        if (isDraggingKeyframe) {
            val delta = dragCurrentFrame - dragOriginalFrame
            val deltaSign = if (delta >= 0) "+$delta" else "$delta"
            val sec = if (activeClip.fps > 0) dragCurrentFrame.toFloat() / activeClip.fps else 0f

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xE61E1E2E))
                    .border(1.dp, StudioPurpleLight, RoundedCornerShape(6.dp))
                    .shadow(8.dp)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(14.dp))
                    Text(
                        text = "$dragTrackName: F$dragOriginalFrame ➔ F$dragCurrentFrame (Δ $deltaSign frames | ${String.format(java.util.Locale.US, "%.3f", sec)}s)",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // =====================================================================
        // 6. Contextual Quick Action Floating Bar (When Keyframes Selected)
        // =====================================================================
        AnimatedVisibility(
            visible = selectedKeyframeIds.isNotEmpty(),
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xF2181825)),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioPurpleLight.copy(alpha = 0.8f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Selected Count Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(StudioPurple)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${selectedKeyframeIds.size} محدد",
                            color = Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Copy
                    IconButton(onClick = onCopySelectedKeyframes, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = StudioBlue, modifier = Modifier.size(13.dp))
                    }

                    // Duplicate
                    IconButton(onClick = onDuplicateSelectedKeyframes, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Layers, contentDescription = "تكرار", tint = StudioGreen, modifier = Modifier.size(13.dp))
                    }

                    // Retime / Scale Timing
                    IconButton(onClick = onOpenSmartRetiming, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Speed, contentDescription = "إعادة التوقيت", tint = StudioYellow, modifier = Modifier.size(13.dp))
                    }

                    // Stretch (+1f)
                    IconButton(onClick = { onStretchTiming(1) }, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "تمديد (+1f)", tint = TextSecondary, modifier = Modifier.size(13.dp))
                    }

                    // Compress (-1f)
                    IconButton(onClick = { onCompressTiming(1) }, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Remove, contentDescription = "ضغط (-1f)", tint = TextSecondary, modifier = Modifier.size(13.dp))
                    }

                    // Distribute Evenly
                    IconButton(onClick = onDistributeEvenly, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.SpaceBar, contentDescription = "توزيع متساوٍ", tint = StudioGreen, modifier = Modifier.size(13.dp))
                    }

                    // Reverse Timing
                    IconButton(onClick = onReverseTiming, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "عكس التوقيت", tint = StudioOrange, modifier = Modifier.size(13.dp))
                    }

                    // Hold Frame
                    IconButton(onClick = onHoldKeyframes, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Pause, contentDescription = "تثبيت (Hold)", tint = StudioBlue, modifier = Modifier.size(13.dp))
                    }

                    // Delete
                    IconButton(onClick = onDeleteSelectedKeyframes, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = StudioRed, modifier = Modifier.size(13.dp))
                    }

                    // Clear Selection
                    IconButton(onClick = onClearKeyframeSelection, modifier = Modifier.size(22.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إلغاء التحديد", tint = TextMuted, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

// =============================================================================
// Helper Sub-composables: Track Header Item & Track Grid Row
// =============================================================================

@Composable
private fun TrackHeaderItem(
    track: TrackData,
    isSelected: Boolean,
    isDimmed: Boolean,
    currentFrame: Int,
    onSelectTrack: () -> Unit,
    onAddKeyframe: () -> Unit,
    onToggleSolo: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleLock: () -> Unit,
    onToggleFocus: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(if (isSelected) StudioPurpleDark.copy(alpha = 0.8f) else Color.Transparent)
            .border(0.3.dp, if (isSelected) StudioPurpleLight else StudioBorder.copy(alpha = 0.3f))
            .clickable { onSelectTrack() }
            .padding(horizontal = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Track Icon & Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                track.iconVector,
                contentDescription = null,
                tint = if (isDimmed) TextMuted else track.displayColor,
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = track.name,
                color = if (isDimmed) TextMuted else if (isSelected) Color.White else TextPrimary,
                fontSize = 8.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Action Icons: Focus, Mute, Lock, Add Keyframe
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            // Focus Button 🎯
            IconButton(onClick = onToggleFocus, modifier = Modifier.size(16.dp)) {
                Icon(
                    imageVector = if (track.isFocused) Icons.Default.FilterCenterFocus else Icons.Default.CenterFocusWeak,
                    contentDescription = "تركيز",
                    tint = if (track.isFocused) StudioYellow else TextMuted.copy(alpha = 0.4f),
                    modifier = Modifier.size(9.dp)
                )
            }

            // Mute Button 👁 / 🔇
            IconButton(onClick = onToggleMute, modifier = Modifier.size(16.dp)) {
                Icon(
                    imageVector = if (track.isMuted) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "كتم",
                    tint = if (track.isMuted) StudioRed else TextMuted.copy(alpha = 0.4f),
                    modifier = Modifier.size(9.dp)
                )
            }

            // Add Keyframe on Track Button
            IconButton(onClick = onAddKeyframe, modifier = Modifier.size(16.dp)) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = "إضافة فريم", tint = track.displayColor, modifier = Modifier.size(10.dp))
            }
        }
    }
}

@Composable
private fun TrackGridRow(
    track: TrackData,
    totalFrames: Int,
    frameWidthDp: Float,
    isSelectedTrack: Boolean,
    isDimmed: Boolean,
    selectedKeyframeIds: Set<String>,
    isMultiSelectMode: Boolean,
    onSelectTrack: () -> Unit,
    onSelectKeyframe: (String, Boolean) -> Unit,
    onFrameSelected: (Float) -> Unit,
    onMoveKeyframe: (TrackData, KeyframeData, Int) -> Unit,
    onKeyframeContextMenu: (TrackData, KeyframeData) -> Unit,
    onDragFeedback: (isDragging: Boolean, originalFrame: Int, currentFrame: Int, trackName: String) -> Unit
) {
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(
                if (isSelectedTrack) StudioPurpleGlass
                else if (isDimmed) Color.Black.copy(alpha = 0.25f)
                else Color.Transparent
            )
            .border(0.3.dp, StudioBorder.copy(alpha = 0.25f))
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
                    val isHold = sortedKf[i].interpolation == InterpolationType.CONSTANT
                    drawLine(
                        color = (if (isDimmed) TextMuted else track.displayColor).copy(alpha = 0.45f),
                        start = Offset(x1, centerY),
                        end = Offset(x2, centerY),
                        strokeWidth = if (isHold) 2.0f else 1.2f
                    )
                }
            }
        }

        // Render Keyframes with Large Touch Hitbox (44dp target) & Visual Feedback
        track.keyframes.forEach { kf ->
            val isSelectedKf = selectedKeyframeIds.contains(kf.id)
            val isHoldKf = kf.interpolation == InterpolationType.CONSTANT
            val leftOffset = (kf.frame * frameWidthDp).dp

            var dragCurrentF by remember(kf.frame) { mutableIntStateOf(kf.frame) }

            Box(
                modifier = Modifier
                    .offset(x = leftOffset - 22.dp)
                    .align(Alignment.CenterStart)
                    .size(44.dp) // Touch-First 44dp hitbox!
                    .pointerInput(kf.id, track.id, isMultiSelectMode) {
                        detectTapGestures(
                            onTap = {
                                onSelectKeyframe(kf.id, isMultiSelectMode)
                                onFrameSelected(kf.frame.toFloat())
                            },
                            onLongPress = {
                                onSelectKeyframe(kf.id, false)
                                onKeyframeContextMenu(track, kf)
                            }
                        )
                    }
                    .pointerInput(kf.id, track.id) {
                        detectDragGestures(
                            onDragStart = {
                                dragCurrentF = kf.frame
                                onDragFeedback(true, kf.frame, kf.frame, track.name)
                            },
                            onDragEnd = {
                                onDragFeedback(false, kf.frame, dragCurrentF, track.name)
                                if (dragCurrentF != kf.frame) {
                                    onMoveKeyframe(track, kf, dragCurrentF)
                                }
                            },
                            onDragCancel = {
                                onDragFeedback(false, kf.frame, kf.frame, track.name)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val deltaFrames = dragAmount.x / (frameWidthDp * density.density)
                                val newTarget = (dragCurrentF + deltaFrames.roundToInt()).coerceIn(0, totalFrames)
                                if (newTarget != dragCurrentF) {
                                    dragCurrentF = newTarget
                                    onDragFeedback(true, kf.frame, dragCurrentF, track.name)
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Visual Diamond / Square / Sprite
                Canvas(modifier = Modifier.size(if (isSelectedKf) 14.dp else 11.dp)) {
                    val w = size.width
                    val h = size.height

                    if (isHoldKf) {
                        // Square for Hold Keyframe
                        drawRect(
                            color = if (isSelectedKf) Color.White else track.displayColor,
                            topLeft = Offset(1f, 1f),
                            size = Size(w - 2f, h - 2f),
                            style = Fill
                        )
                        drawRect(
                            color = if (isSelectedKf) StudioPurple else Color.Black.copy(alpha = 0.6f),
                            topLeft = Offset(1f, 1f),
                            size = Size(w - 2f, h - 2f),
                            style = Stroke(width = if (isSelectedKf) 2.0f else 0.8f)
                        )
                    } else {
                        // Diamond for Normal/Ease Keyframe
                        val path = Path().apply {
                            moveTo(w / 2f, 1f)
                            lineTo(w - 1f, h / 2f)
                            lineTo(w / 2f, h - 1f)
                            lineTo(1f, h / 2f)
                            close()
                        }

                        // Diamond fill
                        drawPath(
                            path = path,
                            color = if (isSelectedKf) Color.White else track.displayColor,
                            style = Fill
                        )

                        // Diamond stroke
                        drawPath(
                            path = path,
                            color = if (isSelectedKf) StudioPurple else Color.Black.copy(alpha = 0.6f),
                            style = Stroke(width = if (isSelectedKf) 2.0f else 0.8f)
                        )
                    }
                }
            }
        }
    }
}

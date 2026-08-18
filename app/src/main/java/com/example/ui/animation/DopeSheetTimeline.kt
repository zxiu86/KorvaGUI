package com.example.ui.animation

import androidx.compose.animation.*
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.engine.animation.*
import com.example.ui.theme.*
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * KORVA ENGINE — REDESIGNED TOUCH-FIRST DOPE SHEET TIMELINE
 * 
 * Rebuilt from zero for fast, simple, precise, and extremely comfortable
 * keyframe, track, playhead, and timing editing on mobile.
 */
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
    // -------------------------------------------------------------------------
    // Viewport Zoom & Sizing State
    // -------------------------------------------------------------------------
    var frameWidthDp by remember { mutableFloatStateOf(24f) }
    var isMultiSelectMode by remember { mutableStateOf(false) }
    var isTransformGroupExpanded by remember { mutableStateOf(true) }

    // Direct Frame Range & Retiming Modals internal to DopeSheet
    var isQuickNumericEditOpen by remember { mutableStateOf(false) }
    var isQuickInsertTimeOpen by remember { mutableStateOf(false) }
    var isQuickRangeScaleOpen by remember { mutableStateOf(false) }

    // Live Drag HUD State
    var isDraggingKeyframe by remember { mutableStateOf(false) }
    var dragDeltaFrames by remember { mutableIntStateOf(0) }
    var dragDisplayFrame by remember { mutableIntStateOf(0) }
    var dragTargetTrackName by remember { mutableStateOf("") }

    val hScrollState = rememberScrollState()
    val vScrollState = rememberScrollState()
    val density = LocalDensity.current

    val totalFrames = activeClip.durationFrames.coerceAtLeast(1)
    val timelineWidthDp = (totalFrames + 12) * frameWidthDp

    // Pinch-to-zoom gesture state
    val transformableState = rememberTransformableState { zoomChange, _, _ ->
        val newWidth = (frameWidthDp * zoomChange).coerceIn(12f, 64f)
        frameWidthDp = newWidth
    }

    // Helper for frame snapping
    fun calculateSnappedFrame(rawFrame: Float): Float {
        return when (snapMode) {
            SnapMode.SNAP_FRAME -> rawFrame.roundToInt().toFloat()
            SnapMode.SNAP_KEYFRAME -> {
                val allKfFrames = activeClip.tracks.flatMap { it.keyframes }.map { it.frame.toFloat() }
                val closest = allKfFrames.minByOrNull { abs(it - rawFrame) }
                if (closest != null && abs(closest - rawFrame) < 0.6f) closest else rawFrame.roundToInt().toFloat()
            }
            SnapMode.SNAP_MARKER -> {
                val allMarkerFrames = activeClip.markers.map { it.frame.toFloat() }
                val closest = allMarkerFrames.minByOrNull { abs(it - rawFrame) }
                if (closest != null && abs(closest - rawFrame) < 0.8f) closest else rawFrame.roundToInt().toFloat()
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

    // Visible tracks list based on expansion & focus
    val activeTrack = remember(selectedTrackId, activeClip.tracks) {
        activeClip.tracks.find { it.id == selectedTrackId }
    }

    // Selected keyframes list
    val allSelectedKeyframes = remember(selectedKeyframeIds, activeClip.tracks) {
        activeClip.tracks.flatMap { track ->
            track.keyframes.filter { selectedKeyframeIds.contains(it.id) }.map { Pair(track, it) }
        }
    }

    // Contextual button label computation
    val contextualAddKeyLabel = remember(activeTrack, selectedKeyframeIds) {
        if (activeTrack != null) {
            when (activeTrack.category) {
                TrackCategory.TRANSFORM -> "Add ${activeTrack.name} Key"
                TrackCategory.SPRITE -> "Add Sprite Key"
                TrackCategory.VISUAL -> "Add Visual Key"
                TrackCategory.EVENTS -> "Add Event Key"
                TrackCategory.AUDIO -> "Add Audio Key"
                else -> "Add ${activeTrack.name} Key"
            }
        } else {
            "Add Keyframe"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EngineBackground)
    ) {
        // =====================================================================
        // SECTION 1: TOP DYNAMIC CONTEXTUAL ACTION & RANGE BAR
        // =====================================================================
        Surface(
            color = EngineSurface,
            tonalElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = EngineBorder, shape = RoundedCornerShape(0.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Range & Current Frame Information Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Frame Summary Pill
                    Surface(
                        color = EngineCardBg,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioPurpleBorder),
                        modifier = Modifier.clickable { isQuickNumericEditOpen = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = StudioPurple,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Start: 0",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "|",
                                color = EngineBorder,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "Cur: ${currentFrame.toInt()}",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "(${String.format("%.2fs", currentFrame / activeClip.fps.coerceAtLeast(1))})",
                                color = StudioCyan,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "|",
                                color = EngineBorder,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "End: ${activeClip.durationFrames}",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Loop Range Tag if active
                    if (loopRangeStart != null || loopRangeEnd != null) {
                        Surface(
                            color = StudioPurple.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioPurple)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Repeat, contentDescription = null, tint = StudioPurple, modifier = Modifier.size(12.dp))
                                Text("In: ${loopRangeStart ?: 0} Out: ${loopRangeEnd ?: activeClip.durationFrames}", color = StudioPurple, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear Range",
                                    tint = TextSecondary,
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clickable { onClearLoopRange() }
                                )
                            }
                        }
                    }
                }

                // Center/Right: Contextual Action Buttons depending on selection state
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    when {
                        // -----------------------------------------------------
                        // STATE A: Multiple Keyframes Selected
                        // -----------------------------------------------------
                        allSelectedKeyframes.size > 1 -> {
                            val minF = allSelectedKeyframes.minOf { it.second.frame }
                            val maxF = allSelectedKeyframes.maxOf { it.second.frame }
                            val span = maxF - minF

                            // Selection Count Tag
                            Surface(
                                color = StudioPurple.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StudioPurple)
                            ) {
                                Text(
                                    text = "Selected: ${allSelectedKeyframes.size} (${span}f span)",
                                    color = StudioPurple,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            // Stretch / Compress / Smart Retiming
                            IconButton(
                                onClick = { onOpenSmartRetiming() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Speed, contentDescription = "Smart Retiming", tint = StudioCyan, modifier = Modifier.size(16.dp))
                            }

                            // Distribute Evenly
                            IconButton(
                                onClick = { onDistributeEvenly() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.FormatAlignJustify, contentDescription = "Distribute Evenly", tint = TextPrimary, modifier = Modifier.size(16.dp))
                            }

                            // Reverse Timing
                            IconButton(
                                onClick = { onReverseTiming() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = "Reverse Timing", tint = TextPrimary, modifier = Modifier.size(16.dp))
                            }

                            // Copy
                            IconButton(
                                onClick = { onCopySelectedKeyframes() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextPrimary, modifier = Modifier.size(16.dp))
                            }

                            // Duplicate
                            IconButton(
                                onClick = { onDuplicateSelectedKeyframes() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ControlPointDuplicate, contentDescription = "Duplicate", tint = StudioGreen, modifier = Modifier.size(16.dp))
                            }

                            // Delete
                            IconButton(
                                onClick = { onDeleteSelectedKeyframes() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Keys", tint = StudioRed, modifier = Modifier.size(16.dp))
                            }

                            // Clear Selection
                            IconButton(
                                onClick = { onClearKeyframeSelection() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear Selection", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }

                        // -----------------------------------------------------
                        // STATE B: Single Keyframe Selected
                        // -----------------------------------------------------
                        allSelectedKeyframes.size == 1 -> {
                            val (trk, kf) = allSelectedKeyframes.first()

                            // Keyframe Info Tag
                            Surface(
                                color = EngineCardBg,
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCyan)
                            ) {
                                Text(
                                    text = "${trk.name} @ F${kf.frame}: ${String.format("%.1f", kf.value)}",
                                    color = StudioCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            // Direct Edit / Context Menu
                            IconButton(
                                onClick = { onKeyframeContextMenu(trk, kf) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = "Edit Keyframe", tint = TextPrimary, modifier = Modifier.size(16.dp))
                            }

                            // Duplicate
                            IconButton(
                                onClick = { onDuplicateSelectedKeyframes() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ControlPointDuplicate, contentDescription = "Duplicate", tint = StudioGreen, modifier = Modifier.size(16.dp))
                            }

                            // Copy
                            IconButton(
                                onClick = { onCopySelectedKeyframes() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextPrimary, modifier = Modifier.size(16.dp))
                            }

                            // Delete
                            IconButton(
                                onClick = { onDeleteSelectedKeyframes() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = StudioRed, modifier = Modifier.size(16.dp))
                            }

                            IconButton(
                                onClick = { onClearKeyframeSelection() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Deselect", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }

                        // -----------------------------------------------------
                        // STATE C: Nothing Selected (Primary Action Bar)
                        // -----------------------------------------------------
                        else -> {
                            // Primary Contextual Add Keyframe Button
                            Button(
                                onClick = {
                                    val targetTrack = activeTrack ?: activeClip.tracks.firstOrNull()
                                    if (targetTrack != null) {
                                        onAddKeyframeToTrack(targetTrack, currentFrame.toInt())
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StudioPurple,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = contextualAddKeyLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Paste (if clipboard active)
                            IconButton(
                                onClick = { onPasteKeyframes() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Paste Keys", tint = TextPrimary, modifier = Modifier.size(15.dp))
                            }

                            // Insert Time Action
                            IconButton(
                                onClick = { isQuickInsertTimeOpen = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.SpaceBar, contentDescription = "Insert Time", tint = StudioCyan, modifier = Modifier.size(15.dp))
                            }

                            // Select All Keys
                            IconButton(
                                onClick = { onSelectAllKeyframes() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.SelectAll, contentDescription = "Select All", tint = TextSecondary, modifier = Modifier.size(15.dp))
                            }

                            // Multi-Select Mode Toggle
                            FilterChip(
                                selected = isMultiSelectMode,
                                onClick = { isMultiSelectMode = !isMultiSelectMode },
                                label = { Text("Multi", fontSize = 9.sp) },
                                leadingIcon = {
                                    Icon(
                                        if (isMultiSelectMode) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
                                    )
                                },
                                modifier = Modifier.height(26.dp)
                            )
                        }
                    }

                    // Zoom Fit / Zoom Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .background(EngineCardBg, RoundedCornerShape(4.dp))
                            .padding(2.dp)
                    ) {
                        IconButton(
                            onClick = { frameWidthDp = (frameWidthDp - 4f).coerceAtLeast(12f) },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = TextSecondary, modifier = Modifier.size(12.dp))
                        }
                        Text(
                            text = "${frameWidthDp.toInt()}p",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.clickable { frameWidthDp = 24f }
                        )
                        IconButton(
                            onClick = { frameWidthDp = (frameWidthDp + 4f).coerceAtMost(64f) },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = TextSecondary, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }

        // =====================================================================
        // SECTION 2: LIVE DRAG HUD BANNER (Floats when moving keyframes)
        // =====================================================================
        AnimatedVisibility(
            visible = isDraggingKeyframe,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Surface(
                color = StudioPurple.copy(alpha = 0.95f),
                contentColor = Color.White,
                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(14.dp))
                        Text(
                            text = if (allSelectedKeyframes.size > 1) "Moving ${allSelectedKeyframes.size} Keyframes" else "Moving $dragTargetTrackName",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Frame: $dragDisplayFrame (${if (dragDeltaFrames >= 0) "+$dragDeltaFrames" else "$dragDeltaFrames"} frames)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // =====================================================================
        // SECTION 3: MAIN SPLIT TIMELINE VIEW (TRACK HEADERS + TIMELINE BODY)
        // =====================================================================
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // -----------------------------------------------------------------
            // LEFT COLUMN: HIERARCHICAL COMPACT TRACK HEADERS (Fixed Width)
            // -----------------------------------------------------------------
            Column(
                modifier = Modifier
                    .width(135.dp)
                    .fillMaxHeight()
                    .background(EngineSurface)
                    .border(width = 1.dp, color = EngineBorder)
            ) {
                // Header Top (Tracks Title & Add Track)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .background(EngineCardBg)
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "TRACKS (${activeClip.tracks.size})",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onAddTrack() },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Track", tint = StudioCyan, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Divider(color = EngineBorder, thickness = 1.dp)

                // Track List Rows (Vertical Scroll synced)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(vScrollState)
                ) {
                    // 1. TRANSFORM GROUP HEADER
                    if (transformTracks.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .background(if (activeTrack?.category == TrackCategory.TRANSFORM) StudioPurple.copy(alpha = 0.12f) else EngineSurface)
                                .clickable { isTransformGroupExpanded = !isTransformGroupExpanded }
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(
                                    imageVector = if (isTransformGroupExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.Transform,
                                    contentDescription = null,
                                    tint = StudioPurple,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Transform",
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }

                            // Quick Add Key for Transform Group
                            IconButton(
                                onClick = {
                                    transformTracks.firstOrNull()?.let { onAddKeyframeToTrack(it, currentFrame.toInt()) }
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = "Add Transform Key", tint = StudioPurple, modifier = Modifier.size(12.dp))
                            }
                        }

                        Divider(color = EngineBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                        // Transform Child Tracks
                        if (isTransformGroupExpanded) {
                            transformTracks.forEach { track ->
                                TrackHeaderItemRow(
                                    track = track,
                                    isSelected = selectedTrackId == track.id,
                                    onSelect = { onSelectTrack(track.id) },
                                    onAddKey = { onAddKeyframeToTrack(track, currentFrame.toInt()) },
                                    onToggleLock = { onToggleTrackLock(track.id) },
                                    onToggleSolo = { onToggleTrackSolo(track.id) },
                                    isChild = true
                                )
                                Divider(color = EngineBorder.copy(alpha = 0.3f), thickness = 0.5.dp)
                            }
                        }
                    }

                    // 2. OTHER TRACKS (Sprite, Events, Custom Properties)
                    otherTracks.forEach { track ->
                        TrackHeaderItemRow(
                            track = track,
                            isSelected = selectedTrackId == track.id,
                            onSelect = { onSelectTrack(track.id) },
                            onAddKey = { onAddKeyframeToTrack(track, currentFrame.toInt()) },
                            onToggleLock = { onToggleTrackLock(track.id) },
                            onToggleSolo = { onToggleTrackSolo(track.id) },
                            isChild = false
                        )
                        Divider(color = EngineBorder.copy(alpha = 0.3f), thickness = 0.5.dp)
                    }

                    // Bottom Spacer for smooth scrolling
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }

            // -----------------------------------------------------------------
            // RIGHT COLUMN: TIMELINE RULER & KEYFRAME LANES CANVAS
            // -----------------------------------------------------------------
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFF0F1117))
                    .transformable(state = transformableState)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(hScrollState)
                ) {
                    // A. TOP TIME RULER (Frame numbers, markers, loop handles)
                    Box(
                        modifier = Modifier
                            .width(timelineWidthDp.dp)
                            .height(30.dp)
                            .background(EngineCardBg)
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val targetFrame = (offset.x / (frameWidthDp * density.density)).coerceIn(0f, activeClip.durationFrames.toFloat())
                                    onFrameSelected(calculateSnappedFrame(targetFrame))
                                }
                            }
                            .pointerInput(Unit) {
                                detectDragGestures { change, _ ->
                                    change.consume()
                                    val targetFrame = (change.position.x / (frameWidthDp * density.density)).coerceIn(0f, activeClip.durationFrames.toFloat())
                                    onFrameSelected(calculateSnappedFrame(targetFrame))
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val fwPx = frameWidthDp.dp.toPx()
                            val totalF = activeClip.durationFrames + 8

                            // Draw loop range shading if active
                            if (loopRangeStart != null || loopRangeEnd != null) {
                                val rStartPx = (loopRangeStart ?: 0) * fwPx
                                val rEndPx = (loopRangeEnd ?: activeClip.durationFrames) * fwPx
                                drawRect(
                                    color = Color(0x33A855F7),
                                    topLeft = Offset(rStartPx, 0f),
                                    size = Size(rEndPx - rStartPx, size.height)
                                )
                            }

                            // Frame Ticks
                            val step = if (frameWidthDp < 16f) 10 else if (frameWidthDp < 30f) 5 else 1
                            for (f in 0..totalF) {
                                val x = f * fwPx
                                val isMajor = f % 5 == 0
                                val isTen = f % 10 == 0

                                val tickHeight = if (isTen) size.height * 0.7f else if (isMajor) size.height * 0.45f else size.height * 0.25f
                                drawLine(
                                    color = if (isTen) Color(0xFF6B7280) else if (isMajor) Color(0xFF4B5563) else Color(0xFF27272A),
                                    start = Offset(x, size.height - tickHeight),
                                    end = Offset(x, size.height),
                                    strokeWidth = if (isTen) 1.5f else 1f
                                )
                            }
                        }

                        // Frame number labels row
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            val step = if (frameWidthDp < 18f) 10 else 5
                            val totalF = activeClip.durationFrames + 8
                            for (f in 0..totalF step step) {
                                Text(
                                    text = "$f",
                                    color = if (f == currentFrame.toInt()) StudioPurple else TextMuted,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (f == currentFrame.toInt()) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier
                                        .width((frameWidthDp * step).dp)
                                        .padding(start = 2.dp, top = 2.dp)
                                )
                            }
                        }

                        // Timeline Markers Pins along ruler
                        activeClip.markers.forEach { marker ->
                            val markerXPx = marker.frame * frameWidthDp
                            Surface(
                                color = StudioYellow,
                                shape = RoundedCornerShape(2.dp),
                                modifier = Modifier
                                    .offset(x = (markerXPx - 4).dp, y = 2.dp)
                                    .size(width = 8.dp, height = 12.dp)
                            ) {}
                        }
                    }

                    Divider(color = EngineBorder, thickness = 1.dp)

                    // B. TRACK LANES & KEYFRAME NODES (Vertical scroll synced)
                    Column(
                        modifier = Modifier
                            .width(timelineWidthDp.dp)
                            .weight(1f)
                            .verticalScroll(vScrollState)
                            .pointerInput(Unit) {
                                // Tap on blank area of timeline moves playhead
                                detectTapGestures { offset ->
                                    val targetFrame = (offset.x / (frameWidthDp * density.density)).coerceIn(0f, activeClip.durationFrames.toFloat())
                                    onFrameSelected(calculateSnappedFrame(targetFrame))
                                }
                            }
                    ) {
                        // 1. TRANSFORM GROUP ROW (When present)
                        if (transformTracks.isNotEmpty()) {
                            // Aggregated summary row
                            Box(
                                modifier = Modifier
                                    .width(timelineWidthDp.dp)
                                    .height(28.dp)
                                    .background(if (activeTrack?.category == TrackCategory.TRANSFORM) StudioPurple.copy(alpha = 0.05f) else Color.Transparent)
                            ) {
                                // Render Background Grid Lines
                                TimelineGridLines(
                                    widthDp = timelineWidthDp,
                                    frameWidthDp = frameWidthDp,
                                    totalFrames = totalFrames
                                )

                                // Aggregated keyframes on group row
                                val allTransformKeys = transformTracks.flatMap { it.keyframes }.groupBy { it.frame }
                                allTransformKeys.forEach { (frame, keys) ->
                                    KeyframeNodeItem(
                                        frame = frame,
                                        frameWidthDp = frameWidthDp,
                                        trackCategory = TrackCategory.TRANSFORM,
                                        interpolation = keys.firstOrNull()?.interpolation ?: InterpolationType.LINEAR,
                                        isSelected = keys.any { selectedKeyframeIds.contains(it.id) },
                                        isMultipleStacked = keys.size > 1,
                                        stackedCount = keys.size,
                                        onSelect = {
                                            if (keys.size == 1) {
                                                onSelectKeyframe(keys.first().id, isMultiSelectMode)
                                            } else {
                                                onKeyframeStackSelected(frame, keys.map { k -> Pair(transformTracks.first { it.keyframes.contains(k) }, k) })
                                            }
                                        },
                                        onDragStart = {
                                            isDraggingKeyframe = true
                                            dragDisplayFrame = frame
                                            dragDeltaFrames = 0
                                            dragTargetTrackName = "Transform"
                                        },
                                        onDragDelta = { deltaF ->
                                            dragDeltaFrames = deltaF
                                            dragDisplayFrame = (frame + deltaF).coerceIn(0, activeClip.durationFrames)
                                        },
                                        onDragEnd = { deltaF ->
                                            isDraggingKeyframe = false
                                            if (deltaF != 0) {
                                                if (selectedKeyframeIds.isNotEmpty()) {
                                                    onMoveKeyframesBatch(deltaF)
                                                } else {
                                                    keys.forEach { k ->
                                                        val trk = transformTracks.first { it.keyframes.contains(k) }
                                                        onMoveKeyframe(trk, k, (k.frame + deltaF).coerceIn(0, activeClip.durationFrames))
                                                    }
                                                }
                                            }
                                        },
                                        onLongPress = {
                                            if (keys.size == 1) {
                                                val trk = transformTracks.first { it.keyframes.contains(keys.first()) }
                                                onKeyframeContextMenu(trk, keys.first())
                                            }
                                        }
                                    )
                                }
                            }

                            Divider(color = EngineBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                            // Transform Child Lanes
                            if (isTransformGroupExpanded) {
                                transformTracks.forEach { track ->
                                    TrackLaneItemRow(
                                        track = track,
                                        timelineWidthDp = timelineWidthDp,
                                        frameWidthDp = frameWidthDp,
                                        totalFrames = totalFrames,
                                        isSelected = selectedTrackId == track.id,
                                        selectedKeyframeIds = selectedKeyframeIds,
                                        isMultiSelectMode = isMultiSelectMode,
                                        onSelectKeyframe = onSelectKeyframe,
                                        onKeyframeContextMenu = onKeyframeContextMenu,
                                        onKeyframeStackSelected = onKeyframeStackSelected,
                                        onMoveKeyframe = onMoveKeyframe,
                                        onMoveKeyframesBatch = onMoveKeyframesBatch,
                                        onDragStateChange = { dragging, delta, targetFrame, trackName ->
                                            isDraggingKeyframe = dragging
                                            dragDeltaFrames = delta
                                            dragDisplayFrame = targetFrame
                                            dragTargetTrackName = trackName
                                        }
                                    )
                                    Divider(color = EngineBorder.copy(alpha = 0.3f), thickness = 0.5.dp)
                                }
                            }
                        }

                        // 2. OTHER TRACK LANES (Sprite, Events, Custom)
                        otherTracks.forEach { track ->
                            TrackLaneItemRow(
                                track = track,
                                timelineWidthDp = timelineWidthDp,
                                frameWidthDp = frameWidthDp,
                                totalFrames = totalFrames,
                                isSelected = selectedTrackId == track.id,
                                selectedKeyframeIds = selectedKeyframeIds,
                                isMultiSelectMode = isMultiSelectMode,
                                onSelectKeyframe = onSelectKeyframe,
                                onKeyframeContextMenu = onKeyframeContextMenu,
                                onKeyframeStackSelected = onKeyframeStackSelected,
                                onMoveKeyframe = onMoveKeyframe,
                                onMoveKeyframesBatch = onMoveKeyframesBatch,
                                onDragStateChange = { dragging, delta, targetFrame, trackName ->
                                    isDraggingKeyframe = dragging
                                    dragDeltaFrames = delta
                                    dragDisplayFrame = targetFrame
                                    dragTargetTrackName = trackName
                                }
                            )
                            Divider(color = EngineBorder.copy(alpha = 0.3f), thickness = 0.5.dp)
                        }

                        // Bottom padding for scroll clearance
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }

                // =============================================================
                // C. PROMINENT SCRUBBABLE PLAYHEAD (Overlay traversing full height)
                // =============================================================
                val playheadXPx = currentFrame * frameWidthDp
                val playheadOffsetDp = playheadXPx.dp - hScrollState.value.dp / density.density

                if (playheadOffsetDp >= 0.dp) {
                    Box(
                        modifier = Modifier
                            .offset(x = playheadOffsetDp)
                            .width(24.dp)
                            .fillMaxHeight()
                            .pointerInput(Unit) {
                                detectDragGestures { change, _ ->
                                    change.consume()
                                    val rawScrollX = hScrollState.value.toFloat() / density.density
                                    val newFrame = ((change.position.x + playheadOffsetDp.toPx() + rawScrollX) / (frameWidthDp * density.density))
                                        .coerceIn(0f, activeClip.durationFrames.toFloat())
                                    onFrameSelected(calculateSnappedFrame(newFrame))
                                }
                            }
                    ) {
                        // Playhead Vertical Line
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .width(2.dp)
                                .fillMaxHeight()
                                .background(StudioRed)
                        )

                        // Playhead Top Pointer Head & Badge
                        Surface(
                            color = StudioRed,
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 2.dp, bottomEnd = 2.dp),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .size(width = 16.dp, height = 18.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${currentFrame.toInt()}",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // SECTION 4: INTEGRATED DOPE SHEET QUICK MODALS
    // =========================================================================

    // Quick Numeric Frame Jump & Range Edit
    if (isQuickNumericEditOpen) {
        DirectFrameInputDialog(
            currentFrame = currentFrame.toInt(),
            fps = activeClip.fps,
            maxFrames = activeClip.durationFrames,
            onDismiss = { isQuickNumericEditOpen = false },
            onJumpToFrame = { f ->
                onFrameSelected(f.toFloat())
            }
        )
    }

    // Quick Insert Time Dialog
    if (isQuickInsertTimeOpen) {
        var framesToInsert by remember { mutableIntStateOf(10) }
        Dialog(onDismissRequest = { isQuickInsertTimeOpen = false }) {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = EngineSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCyan),
                modifier = Modifier.width(260.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Insert Empty Time", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Insert frames at current frame ${currentFrame.toInt()} (shifts downstream keys)", color = TextMuted, fontSize = 9.sp)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(5, 10, 15, 24).forEach { count ->
                            FilterChip(
                                selected = framesToInsert == count,
                                onClick = { framesToInsert = count },
                                label = { Text("+$count f", fontSize = 9.sp) }
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(onClick = { isQuickInsertTimeOpen = false }, colors = ButtonDefaults.buttonColors(containerColor = EngineCardBg), modifier = Modifier.weight(1f)) {
                            Text("Cancel", color = TextSecondary, fontSize = 10.sp)
                        }
                        Button(
                            onClick = {
                                onStretchTiming(framesToInsert)
                                isQuickInsertTimeOpen = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioCyan),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Insert", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// HELPER COMPOSABLE: TRACK HEADER ITEM ROW
// -----------------------------------------------------------------------------
@Composable
private fun TrackHeaderItemRow(
    track: TrackData,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onAddKey: () -> Unit,
    onToggleLock: () -> Unit,
    onToggleSolo: () -> Unit,
    isChild: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(if (isSelected) StudioPurple.copy(alpha = 0.18f) else Color.Transparent)
            .clickable { onSelect() }
            .padding(start = if (isChild) 14.dp else 6.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Track Type Icon
            val icon = when (track.category) {
                TrackCategory.TRANSFORM -> if (track.name.contains("Pos", true)) Icons.Default.OpenWith else if (track.name.contains("Rot", true)) Icons.Default.RotateRight else Icons.Default.AspectRatio
                TrackCategory.SPRITE -> Icons.Default.Image
                TrackCategory.VISUAL -> Icons.Default.Palette
                TrackCategory.AUDIO -> Icons.Default.VolumeUp
                TrackCategory.EVENTS -> Icons.Default.Bolt
                else -> Icons.Default.Tune
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) StudioPurple else TextSecondary,
                modifier = Modifier.size(12.dp)
            )

            // Track Name
            Text(
                text = track.name,
                color = if (isSelected) TextPrimary else TextSecondary,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Action Icons (Add Key & Lock)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onAddKey,
                modifier = Modifier.size(18.dp)
            ) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = "Add Key", tint = if (isSelected) StudioPurple else TextMuted, modifier = Modifier.size(11.dp))
            }
            IconButton(
                onClick = onToggleLock,
                modifier = Modifier.size(18.dp)
            ) {
                Icon(
                    imageVector = if (track.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = "Lock",
                    tint = if (track.isLocked) StudioYellow else TextMuted.copy(alpha = 0.5f),
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// HELPER COMPOSABLE: TRACK LANE ITEM ROW (Grid + Keyframes)
// -----------------------------------------------------------------------------
@Composable
private fun TrackLaneItemRow(
    track: TrackData,
    timelineWidthDp: Float,
    frameWidthDp: Float,
    totalFrames: Int,
    isSelected: Boolean,
    selectedKeyframeIds: Set<String>,
    isMultiSelectMode: Boolean,
    onSelectKeyframe: (String, Boolean) -> Unit,
    onKeyframeContextMenu: (TrackData, KeyframeData) -> Unit,
    onKeyframeStackSelected: (Int, List<Pair<TrackData, KeyframeData>>) -> Unit,
    onMoveKeyframe: (TrackData, KeyframeData, Int) -> Unit,
    onMoveKeyframesBatch: (Int) -> Unit,
    onDragStateChange: (Boolean, Int, Int, String) -> Unit
) {
    Box(
        modifier = Modifier
            .width(timelineWidthDp.dp)
            .height(28.dp)
            .background(if (isSelected) StudioPurple.copy(alpha = 0.05f) else Color.Transparent)
    ) {
        // Grid Lines
        TimelineGridLines(
            widthDp = timelineWidthDp,
            frameWidthDp = frameWidthDp,
            totalFrames = totalFrames
        )

        // Hold Bars / Interpolation Lines connecting keyframes
        if (track.keyframes.size > 1) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val sortedKeys = track.keyframes.sortedBy { it.frame }
                val fwPx = frameWidthDp.dp.toPx()
                for (i in 0 until sortedKeys.size - 1) {
                    val k1 = sortedKeys[i]
                    val k2 = sortedKeys[i + 1]
                    val x1 = k1.frame * fwPx
                    val x2 = k2.frame * fwPx
                    val y = size.height / 2f

                    if (k1.interpolation == InterpolationType.CONSTANT || track.category == TrackCategory.SPRITE) {
                        // Solid Hold bar for stepped/sprite frames
                        drawLine(
                            color = StudioCyan.copy(alpha = 0.5f),
                            start = Offset(x1, y),
                            end = Offset(x2, y),
                            strokeWidth = 2.dp.toPx()
                        )
                    } else {
                        // Subtle dashed or thin curve line
                        drawLine(
                            color = StudioPurple.copy(alpha = 0.35f),
                            start = Offset(x1, y),
                            end = Offset(x2, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
            }
        }

        // Keyframe Nodes on this Track
        val groupedKeys = track.keyframes.groupBy { it.frame }
        groupedKeys.forEach { (frame, keys) ->
            val kf = keys.first()
            KeyframeNodeItem(
                frame = frame,
                frameWidthDp = frameWidthDp,
                trackCategory = track.category,
                interpolation = kf.interpolation,
                isSelected = keys.any { selectedKeyframeIds.contains(it.id) },
                isMultipleStacked = keys.size > 1,
                stackedCount = keys.size,
                onSelect = {
                    if (keys.size == 1) {
                        onSelectKeyframe(kf.id, isMultiSelectMode)
                    } else {
                        onKeyframeStackSelected(frame, keys.map { Pair(track, it) })
                    }
                },
                onDragStart = {
                    onDragStateChange(true, 0, frame, track.name)
                },
                onDragDelta = { deltaF ->
                    onDragStateChange(true, deltaF, (frame + deltaF).coerceIn(0, totalFrames), track.name)
                },
                onDragEnd = { deltaF ->
                    onDragStateChange(false, 0, 0, "")
                    if (deltaF != 0) {
                        if (selectedKeyframeIds.contains(kf.id) && selectedKeyframeIds.size > 1) {
                            onMoveKeyframesBatch(deltaF)
                        } else {
                            onMoveKeyframe(track, kf, (kf.frame + deltaF).coerceIn(0, totalFrames))
                        }
                    }
                },
                onLongPress = {
                    onKeyframeContextMenu(track, kf)
                }
            )
        }
    }
}

// -----------------------------------------------------------------------------
// HELPER COMPOSABLE: BACKGROUND GRID LINES
// -----------------------------------------------------------------------------
@Composable
private fun TimelineGridLines(
    widthDp: Float,
    frameWidthDp: Float,
    totalFrames: Int
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val fwPx = frameWidthDp.dp.toPx()
        val totalF = totalFrames + 8
        for (f in 0..totalF) {
            val x = f * fwPx
            val isMajor = f % 5 == 0
            val isTen = f % 10 == 0
            drawLine(
                color = if (isTen) Color(0xFF2D3748) else if (isMajor) Color(0xFF212631) else Color(0xFF161922),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = if (isTen) 1f else 0.5f
            )
        }
    }
}

// -----------------------------------------------------------------------------
// HELPER COMPOSABLE: TOUCH-FIRST SHAPE-CODED KEYFRAME NODE
// -----------------------------------------------------------------------------
@Composable
private fun KeyframeNodeItem(
    frame: Int,
    frameWidthDp: Float,
    trackCategory: TrackCategory,
    interpolation: InterpolationType,
    isSelected: Boolean,
    isMultipleStacked: Boolean,
    stackedCount: Int,
    onSelect: () -> Unit,
    onDragStart: () -> Unit,
    onDragDelta: (Int) -> Unit,
    onDragEnd: (Int) -> Unit,
    onLongPress: () -> Unit
) {
    val density = LocalDensity.current
    var dragAccumulatedPx by remember { mutableFloatStateOf(0f) }

    // Generous 36dp invisible touch target container centered exactly on frame
    Box(
        modifier = Modifier
            .offset(x = (frame * frameWidthDp - 18).dp, y = 0.dp)
            .size(width = 36.dp, height = 28.dp)
            .pointerInput(frame, isSelected) {
                detectTapGestures(
                    onTap = { onSelect() },
                    onLongPress = { onLongPress() }
                )
            }
            .pointerInput(frame, isSelected) {
                detectDragGestures(
                    onDragStart = {
                        dragAccumulatedPx = 0f
                        onDragStart()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragAccumulatedPx += dragAmount.x
                        val frameStepPx = frameWidthDp * density.density
                        val deltaFrames = (dragAccumulatedPx / frameStepPx).roundToInt()
                        onDragDelta(deltaFrames)
                    },
                    onDragEnd = {
                        val frameStepPx = frameWidthDp * density.density
                        val deltaFrames = (dragAccumulatedPx / frameStepPx).roundToInt()
                        onDragEnd(deltaFrames)
                    },
                    onDragCancel = {
                        onDragEnd(0)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Visual Shape representation
        Canvas(modifier = Modifier.size(16.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseColor = when (trackCategory) {
                TrackCategory.TRANSFORM -> Color(0xFFA855F7) // Purple
                TrackCategory.SPRITE -> Color(0xFF38BDF8)    // Cyan
                TrackCategory.VISUAL -> Color(0xFFFBBF24)    // Amber
                TrackCategory.AUDIO -> Color(0xFF10B981)     // Emerald
                TrackCategory.EVENTS -> Color(0xFFEC4899)    // Pink
                else -> Color(0xFF10B981)                   // Emerald
            }

            val strokeColor = if (isSelected) Color.White else Color(0xFF0F1117)
            val strokeWidth = if (isSelected) 2.dp.toPx() else 1.dp.toPx()

            // Glow Halo when selected
            if (isSelected) {
                drawCircle(
                    color = StudioPurple.copy(alpha = 0.35f),
                    radius = size.width * 0.9f,
                    center = center
                )
            }

            when (trackCategory) {
                // Circular Visual / Audio keyframe
                TrackCategory.VISUAL, TrackCategory.AUDIO -> {
                    drawCircle(color = baseColor, radius = 5.dp.toPx(), center = center)
                    drawCircle(color = strokeColor, radius = 5.dp.toPx(), center = center, style = Stroke(strokeWidth))
                }
                // Square Sprite frame
                TrackCategory.SPRITE -> {
                    drawRect(
                        color = baseColor,
                        topLeft = Offset(center.x - 4.dp.toPx(), center.y - 4.dp.toPx()),
                        size = Size(8.dp.toPx(), 8.dp.toPx())
                    )
                    drawRect(
                        color = strokeColor,
                        topLeft = Offset(center.x - 4.dp.toPx(), center.y - 4.dp.toPx()),
                        size = Size(8.dp.toPx(), 8.dp.toPx()),
                        style = Stroke(strokeWidth)
                    )
                }
                // Triangle Event keyframe
                TrackCategory.EVENTS -> {
                    val path = Path().apply {
                        moveTo(center.x, center.y - 5.dp.toPx())
                        lineTo(center.x + 5.dp.toPx(), center.y + 4.dp.toPx())
                        lineTo(center.x - 5.dp.toPx(), center.y + 4.dp.toPx())
                        close()
                    }
                    drawPath(path, color = baseColor, style = Fill)
                    drawPath(path, color = strokeColor, style = Stroke(strokeWidth))
                }
                // Diamond Rotation & General Transform keyframe
                else -> {
                    val path = Path().apply {
                        moveTo(center.x, center.y - 5.5.dp.toPx())
                        lineTo(center.x + 5.5.dp.toPx(), center.y)
                        lineTo(center.x, center.y + 5.5.dp.toPx())
                        lineTo(center.x - 5.5.dp.toPx(), center.y)
                        close()
                    }
                    drawPath(path, color = baseColor, style = Fill)
                    drawPath(path, color = strokeColor, style = Stroke(strokeWidth))
                }
            }
        }

        // Multiple stacked keyframe counter badge
        if (isMultipleStacked) {
            Surface(
                color = StudioYellow,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 2.dp)
                    .size(11.dp)
            ) {
                Text(
                    text = "$stackedCount",
                    color = Color.Black,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

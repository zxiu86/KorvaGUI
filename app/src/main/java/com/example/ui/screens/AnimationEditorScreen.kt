package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.engine.animation.*
import com.example.model.SceneNode
import com.example.ui.animation.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun AnimationEditorScreen(
    projectName: String,
    selectedNode: SceneNode?,
    onBackToEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    // -------------------------------------------------------------------------
    // Backend & Controller Instance
    // -------------------------------------------------------------------------
    val backend = remember { MockAnimationBackend() }

    // Reactive State
    var clipsVersion by remember { mutableIntStateOf(0) }
    val clips = remember(clipsVersion) { backend.getClips() }
    var activeClipId by remember { mutableStateOf(backend.getActiveClip()?.id ?: "") }
    val activeClip = remember(activeClipId, clipsVersion) {
        backend.getActiveClip() ?: backend.getClips().first()
    }

    // Playback State
    var isPlaying by remember { mutableStateOf(false) }
    var currentFrameFloat by remember { mutableFloatStateOf(0f) }
    var playbackDirection by remember { mutableIntStateOf(1) } // 1: forward, -1: reverse (for ping-pong)

    // Selection & Editing State
    var selectedTrackId by remember { mutableStateOf<String?>(activeClip.tracks.firstOrNull()?.id) }
    var selectedKeyframeIds by remember { mutableStateOf(setOf<String>()) }
    var autoKeyEnabled by remember { mutableStateOf(false) }
    var snapEnabled by remember { mutableStateOf(true) }

    // UI Panes & Dialog Toggles
    var isLibraryOpen by remember { mutableStateOf(false) }
    var showOnionSkin by remember { mutableStateOf(true) }
    var showGrid by remember { mutableStateOf(true) }
    var showMotionPath by remember { mutableStateOf(true) }
    var showCrosshair by remember { mutableStateOf(true) }

    // Modals
    var isCurveEditorOpen by remember { mutableStateOf(false) }
    var isSpriteSheetOpen by remember { mutableStateOf(false) }
    var isAddEventOpen by remember { mutableStateOf(false) }
    var isAddMarkerOpen by remember { mutableStateOf(false) }
    var isNewClipDialogOpen by remember { mutableStateOf(false) }
    var isRenameClipDialogOpen by remember { mutableStateOf(false) }
    var newClipName by remember { mutableStateOf("NewAnimation") }

    // Keyframe Context Menu Dialog Target
    var contextMenuKeyframe by remember { mutableStateOf<Pair<TrackData, KeyframeData>?>(null) }

    // Active Triggered Event notification
    var activeTriggeredEvent by remember { mutableStateOf<String?>(null) }

    // -------------------------------------------------------------------------
    // Playback Coroutine Loop
    // -------------------------------------------------------------------------
    LaunchedEffect(isPlaying, activeClip.fps, activeClip.durationFrames, activeClip.loopMode, playbackDirection) {
        if (!isPlaying) return@LaunchedEffect

        val frameDelayMs = if (activeClip.fps > 0) (1000L / activeClip.fps).coerceAtLeast(10L) else 40L

        while (isActive && isPlaying) {
            delay(frameDelayMs)

            val total = activeClip.durationFrames.toFloat()
            var nextFrame = currentFrameFloat + playbackDirection * 1.0f

            when (activeClip.loopMode) {
                LoopMode.OFF -> {
                    if (nextFrame >= total) {
                        currentFrameFloat = total
                        isPlaying = false
                    } else if (nextFrame < 0f) {
                        currentFrameFloat = 0f
                        isPlaying = false
                    } else {
                        currentFrameFloat = nextFrame
                    }
                }
                LoopMode.LOOP -> {
                    if (nextFrame > total) {
                        currentFrameFloat = 0f
                    } else {
                        currentFrameFloat = nextFrame
                    }
                }
                LoopMode.PING_PONG -> {
                    if (nextFrame >= total) {
                        currentFrameFloat = total
                        playbackDirection = -1
                    } else if (nextFrame <= 0f) {
                        currentFrameFloat = 0f
                        playbackDirection = 1
                    } else {
                        currentFrameFloat = nextFrame
                    }
                }
                LoopMode.REVERSE -> {
                    if (nextFrame < 0f) {
                        currentFrameFloat = total
                    } else {
                        currentFrameFloat = nextFrame
                    }
                }
                LoopMode.RANGE -> {
                    if (nextFrame > activeClip.rangeEnd) {
                        currentFrameFloat = activeClip.rangeStart.toFloat()
                    } else {
                        currentFrameFloat = nextFrame
                    }
                }
            }

            // Check if any event fires on current integer frame
            val currentIntFrame = currentFrameFloat.toInt()
            val firedEvent = activeClip.events.find { it.frame == currentIntFrame }
            if (firedEvent != null) {
                activeTriggeredEvent = "${firedEvent.name} (${firedEvent.parameters})"
            } else {
                activeTriggeredEvent = null
            }
        }
    }

    // -------------------------------------------------------------------------
    // Main UI Layout Container
    // -------------------------------------------------------------------------
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EngineBackground)
    ) {
        // =====================================================================
        // 1. Studio Header Toolbar
        // =====================================================================
        AnimationHeaderBar(
            activeClip = activeClip,
            clips = clips,
            isPlaying = isPlaying,
            currentFrame = currentFrameFloat.toInt(),
            autoKeyEnabled = autoKeyEnabled,
            snapEnabled = snapEnabled,
            canUndo = backend.commandHistory.canUndo,
            canRedo = backend.commandHistory.canRedo,
            isLibraryOpen = isLibraryOpen,
            onToggleLibrary = { isLibraryOpen = !isLibraryOpen },
            onSelectClip = { clipId ->
                backend.setActiveClip(clipId)
                activeClipId = clipId
                currentFrameFloat = 0f
                selectedTrackId = backend.getActiveClip()?.tracks?.firstOrNull()?.id
                selectedKeyframeIds = emptySet()
            },
            onNewClip = {
                newClipName = "NewAnimation_${clips.size + 1}"
                isNewClipDialogOpen = true
            },
            onDuplicateClip = {
                val dup = backend.duplicateClip(activeClip.id)
                if (dup != null) {
                    activeClipId = dup.id
                    clipsVersion++
                }
            },
            onRenameClip = {
                newClipName = activeClip.name
                isRenameClipDialogOpen = true
            },
            onDeleteClip = {
                if (backend.deleteClip(activeClip.id)) {
                    activeClipId = backend.getActiveClip()?.id ?: ""
                    clipsVersion++
                }
            },
            onPlayToggle = { isPlaying = !isPlaying },
            onStop = {
                isPlaying = false
                currentFrameFloat = 0f
            },
            onPrevFrame = {
                currentFrameFloat = (currentFrameFloat - 1f).coerceAtLeast(0f)
            },
            onNextFrame = {
                currentFrameFloat = (currentFrameFloat + 1f).coerceAtMost(activeClip.durationFrames.toFloat())
            },
            onFirstFrame = { currentFrameFloat = 0f },
            onLastFrame = { currentFrameFloat = activeClip.durationFrames.toFloat() },
            onLoopModeChange = { mode ->
                activeClip.loopMode = mode
                clipsVersion++
            },
            onFpsChange = { fps ->
                activeClip.fps = fps
                clipsVersion++
            },
            onAutoKeyToggle = { autoKeyEnabled = !autoKeyEnabled },
            onSnapToggle = { snapEnabled = !snapEnabled },
            onUndo = {
                backend.commandHistory.undo()
                clipsVersion++
            },
            onRedo = {
                backend.commandHistory.redo()
                clipsVersion++
            },
            onOpenCurveEditor = { isCurveEditorOpen = true },
            onOpenSpriteSheet = { isSpriteSheetOpen = true },
            onSaveClip = {
                clipsVersion++
            },
            onBackToEditor = onBackToEditor
        )

        // =====================================================================
        // 2. Main Content Split: (Library Drawer) + (Viewport & Dope Sheet)
        // =====================================================================
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Left Collapsible Animation Library Drawer
            AnimatedVisibility(
                visible = isLibraryOpen,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut()
            ) {
                AnimationLibraryDrawer(
                    clips = clips,
                    selectedClipId = activeClip.id,
                    onSelectClip = { clipId ->
                        backend.setActiveClip(clipId)
                        activeClipId = clipId
                        currentFrameFloat = 0f
                        selectedTrackId = backend.getActiveClip()?.tracks?.firstOrNull()?.id
                        selectedKeyframeIds = emptySet()
                    },
                    onNewClip = {
                        newClipName = "Clip_${clips.size + 1}"
                        isNewClipDialogOpen = true
                    },
                    onDuplicateClip = { clipId ->
                        backend.duplicateClip(clipId)?.let {
                            activeClipId = it.id
                            clipsVersion++
                        }
                    },
                    onRenameClip = {
                        newClipName = activeClip.name
                        isRenameClipDialogOpen = true
                    },
                    onDeleteClip = { clipId ->
                        if (backend.deleteClip(clipId)) {
                            activeClipId = backend.getActiveClip()?.id ?: ""
                            clipsVersion++
                        }
                    },
                    onToggleFavorite = { clipId ->
                        clips.find { it.id == clipId }?.let {
                            it.isFavorite = !it.isFavorite
                            clipsVersion++
                        }
                    },
                    onClose = { isLibraryOpen = false },
                    modifier = Modifier.width(200.dp)
                )
            }

            // Right Split: Viewport (Top 55%) + Dope Sheet (Bottom 45%)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Viewport Preview Canvas (Top)
                AnimationViewportPreview(
                    activeClip = activeClip,
                    currentFrame = currentFrameFloat,
                    backend = backend,
                    showOnionSkin = showOnionSkin,
                    showGrid = showGrid,
                    showMotionPath = showMotionPath,
                    showCrosshair = showCrosshair,
                    onToggleOnionSkin = { showOnionSkin = !showOnionSkin },
                    onToggleGrid = { showGrid = !showGrid },
                    onToggleMotionPath = { showMotionPath = !showMotionPath },
                    onToggleCrosshair = { showCrosshair = !showCrosshair },
                    triggeredEvent = activeTriggeredEvent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.1f)
                )

                // Dope Sheet Timeline (Bottom)
                DopeSheetTimeline(
                    activeClip = activeClip,
                    currentFrame = currentFrameFloat,
                    selectedTrackId = selectedTrackId,
                    selectedKeyframeIds = selectedKeyframeIds,
                    snapEnabled = snapEnabled,
                    onFrameSelected = { f ->
                        currentFrameFloat = f
                    },
                    onSelectTrack = { trkId ->
                        selectedTrackId = trkId
                    },
                    onToggleTrackVisibility = { trkId ->
                        activeClip.tracks.find { it.id == trkId }?.let {
                            it.isVisible = !it.isVisible
                            clipsVersion++
                        }
                    },
                    onToggleTrackLock = { trkId ->
                        activeClip.tracks.find { it.id == trkId }?.let {
                            it.isLocked = !it.isLocked
                            clipsVersion++
                        }
                    },
                    onAddKeyframeToTrack = { track, frame ->
                        val currentVal = backend.evaluateTrackAt(track, frame.toFloat())
                        backend.addKeyframe(activeClip.id, track.id, frame, currentVal)
                        clipsVersion++
                    },
                    onSelectKeyframe = { kfId, multiSelect ->
                        selectedKeyframeIds = if (multiSelect) {
                            if (selectedKeyframeIds.contains(kfId)) selectedKeyframeIds - kfId else selectedKeyframeIds + kfId
                        } else {
                            setOf(kfId)
                        }
                    },
                    onMoveKeyframe = { track, kf, newFrame ->
                        backend.moveKeyframe(activeClip.id, track.id, kf.id, newFrame)
                        clipsVersion++
                    },
                    onKeyframeContextMenu = { track, kf ->
                        contextMenuKeyframe = Pair(track, kf)
                    },
                    onAddTrack = {
                        backend.addTrack(activeClip.id, TrackCategory.TRANSFORM, "New Track ${activeClip.tracks.size + 1}", "custom.prop")
                        clipsVersion++
                    },
                    onAddEvent = {
                        isAddEventOpen = true
                    },
                    onAddMarker = {
                        isAddMarkerOpen = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.9f)
                )
            }
        }
    }

    // =========================================================================
    // 3. Modal Dialogs (Curve Editor, Sprite Sheet, Events, Keyframe Context)
    // =========================================================================

    // A. Keyframe Context Menu Dialog
    contextMenuKeyframe?.let { (track, kf) ->
        KeyframeContextMenuDialog(
            track = track,
            keyframe = kf,
            onDismiss = { contextMenuKeyframe = null },
            onSetValue = { newVal ->
                backend.setKeyframeValue(activeClip.id, track.id, kf.id, newVal)
                clipsVersion++
            },
            onSetInterpolation = { interp ->
                backend.setInterpolation(activeClip.id, track.id, kf.id, interp)
                clipsVersion++
            },
            onDuplicate = {
                val nextFrame = (kf.frame + 2).coerceAtMost(activeClip.durationFrames)
                backend.addKeyframe(activeClip.id, track.id, nextFrame, kf.value, kf.interpolation)
                clipsVersion++
            },
            onDelete = {
                backend.removeKeyframe(activeClip.id, track.id, kf.id)
                clipsVersion++
            },
            onOpenCurveEditor = {
                isCurveEditorOpen = true
            }
        )
    }

    // B. Curve Editor Dialog
    if (isCurveEditorOpen) {
        val selectedTrk = activeClip.tracks.find { it.id == selectedTrackId } ?: activeClip.tracks.firstOrNull()
        val selectedKf = selectedTrk?.keyframes?.find { selectedKeyframeIds.contains(it.id) } ?: selectedTrk?.keyframes?.firstOrNull()

        AnimationCurveEditorDialog(
            track = selectedTrk,
            keyframe = selectedKf,
            onDismiss = { isCurveEditorOpen = false },
            onApplyInterpolation = { interp, hIn, hOut ->
                if (selectedTrk != null && selectedKf != null) {
                    selectedKf.handleIn = hIn
                    selectedKf.handleOut = hOut
                    backend.setInterpolation(activeClip.id, selectedTrk.id, selectedKf.id, interp)
                    clipsVersion++
                }
            }
        )
    }

    // C. Sprite Sheet Animator Dialog
    if (isSpriteSheetOpen) {
        SpriteSheetAnimatorDialog(
            onDismiss = { isSpriteSheetOpen = false },
            onCreateSpriteAnimation = { cfg ->
                val spriteTrack = activeClip.tracks.find { it.name.contains("Sprite", ignoreCase = true) }
                    ?: backend.addTrack(activeClip.id, TrackCategory.SPRITE, "Sprite Frame", "sprite.frame")

                if (spriteTrack != null) {
                    spriteTrack.keyframes.clear()
                    val frameStep = 1
                    var f = 0
                    for (idx in cfg.startFrame..cfg.endFrame) {
                        spriteTrack.keyframes.add(
                            KeyframeData(
                                frame = f,
                                value = idx.toFloat(),
                                interpolation = InterpolationType.CONSTANT,
                                type = KeyframeType.SPRITE_FRAME
                            )
                        )
                        f += frameStep
                    }
                    activeClip.durationFrames = f
                    activeClip.fps = cfg.fps
                    clipsVersion++
                }
            }
        )
    }

    // D. Animation Event Dialog
    if (isAddEventOpen) {
        AnimationEventDialog(
            initialFrame = currentFrameFloat.toInt(),
            maxFrame = activeClip.durationFrames,
            onDismiss = { isAddEventOpen = false },
            onAddEvent = { frame, name, func, params ->
                backend.addEvent(activeClip.id, frame, name, func, params)
                clipsVersion++
            }
        )
    }

    // E. Animation Marker Dialog
    if (isAddMarkerOpen) {
        AnimationMarkerDialog(
            initialFrame = currentFrameFloat.toInt(),
            maxFrame = activeClip.durationFrames,
            onDismiss = { isAddMarkerOpen = false },
            onAddMarker = { frame, label, colorHex ->
                backend.addMarker(activeClip.id, frame, label, colorHex)
                clipsVersion++
            }
        )
    }

    // F. New Clip Dialog
    if (isNewClipDialogOpen) {
        Dialog(onDismissRequest = { isNewClipDialogOpen = false }) {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = EngineSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioPurpleBorder),
                modifier = Modifier.width(280.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("إنشاء مقطع أنيميشن جديد", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = newClipName,
                        onValueChange = { newClipName = it },
                        label = { Text("اسم المقطع") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(onClick = { isNewClipDialogOpen = false }, colors = ButtonDefaults.buttonColors(containerColor = EngineCardBg), modifier = Modifier.weight(1f)) {
                            Text("إلغاء", color = TextSecondary, fontSize = 10.sp)
                        }
                        Button(
                            onClick = {
                                if (newClipName.isNotBlank()) {
                                    val created = backend.createClip(newClipName, 24, 24)
                                    activeClipId = created.id
                                    clipsVersion++
                                    isNewClipDialogOpen = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إنشاء", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // G. Rename Clip Dialog
    if (isRenameClipDialogOpen) {
        Dialog(onDismissRequest = { isRenameClipDialogOpen = false }) {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = EngineSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioPurpleBorder),
                modifier = Modifier.width(280.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("إعادة تسمية المقطع", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = newClipName,
                        onValueChange = { newClipName = it },
                        label = { Text("الاسم الجديد") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(onClick = { isRenameClipDialogOpen = false }, colors = ButtonDefaults.buttonColors(containerColor = EngineCardBg), modifier = Modifier.weight(1f)) {
                            Text("إلغاء", color = TextSecondary, fontSize = 10.sp)
                        }
                        Button(
                            onClick = {
                                if (newClipName.isNotBlank()) {
                                    backend.renameClip(activeClip.id, newClipName)
                                    clipsVersion++
                                    isRenameClipDialogOpen = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

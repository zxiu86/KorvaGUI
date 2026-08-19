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
import com.example.ui.components.KorvaDialog
import com.example.ui.components.KorvaOutlinedButton
import com.example.ui.components.KorvaPrimaryButton
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
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }

    // Selection & Editing State
    var selectedTrackId by remember { mutableStateOf<String?>(activeClip.tracks.firstOrNull()?.id) }
    var selectedKeyframeIds by remember { mutableStateOf(setOf<String>()) }
    var autoKeyEnabled by remember { mutableStateOf(false) }
    var snapMode by remember { mutableStateOf(SnapMode.SNAP_FRAME) }

    // Loop Range (In / Out)
    var loopRangeStart by remember { mutableStateOf<Int?>(null) }
    var loopRangeEnd by remember { mutableStateOf<Int?>(null) }

    // UI Panes & Viewport Toggles
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

    // Advanced Animation UX Dialogs
    var isDirectFrameInputOpen by remember { mutableStateOf(false) }
    var isSmartRetimingOpen by remember { mutableStateOf(false) }
    var isTimeManipulationOpen by remember { mutableStateOf(false) }
    var isJumpToOpen by remember { mutableStateOf(false) }
    var keyframeStackData by remember { mutableStateOf<Pair<Int, List<Pair<TrackData, KeyframeData>>>?>(null) }

    // Keyframe Context Menu Dialog Target
    var contextMenuKeyframe by remember { mutableStateOf<Pair<TrackData, KeyframeData>?>(null) }

    // Active Triggered Event notification
    var activeTriggeredEvent by remember { mutableStateOf<String?>(null) }

    // -------------------------------------------------------------------------
    // Playback Coroutine Loop (Speed & Loop Range aware)
    // -------------------------------------------------------------------------
    LaunchedEffect(isPlaying, activeClip.fps, activeClip.durationFrames, activeClip.loopMode, playbackDirection, playbackSpeed, loopRangeStart, loopRangeEnd) {
        if (!isPlaying) return@LaunchedEffect

        val baseDelay = if (activeClip.fps > 0) 1000L / activeClip.fps else 40L
        val frameDelayMs = (baseDelay / playbackSpeed).toLong().coerceAtLeast(8L)

        while (isActive && isPlaying) {
            delay(frameDelayMs)

            val minF = (loopRangeStart?.toFloat() ?: 0f)
            val maxF = (loopRangeEnd?.toFloat() ?: activeClip.durationFrames.toFloat())
            var nextFrame = currentFrameFloat + playbackDirection * 1.0f

            when (activeClip.loopMode) {
                LoopMode.OFF -> {
                    if (nextFrame >= maxF) {
                        currentFrameFloat = maxF
                        isPlaying = false
                    } else if (nextFrame < minF) {
                        currentFrameFloat = minF
                        isPlaying = false
                    } else {
                        currentFrameFloat = nextFrame
                    }
                }
                LoopMode.LOOP -> {
                    if (nextFrame > maxF) {
                        currentFrameFloat = minF
                    } else {
                        currentFrameFloat = nextFrame
                    }
                }
                LoopMode.PING_PONG -> {
                    if (nextFrame >= maxF) {
                        currentFrameFloat = maxF
                        playbackDirection = -1
                    } else if (nextFrame <= minF) {
                        currentFrameFloat = minF
                        playbackDirection = 1
                    } else {
                        currentFrameFloat = nextFrame
                    }
                }
                LoopMode.REVERSE -> {
                    if (nextFrame < minF) {
                        currentFrameFloat = maxF
                    } else {
                        currentFrameFloat = nextFrame
                    }
                }
                LoopMode.RANGE -> {
                    val rStart = activeClip.rangeStart.toFloat()
                    val rEnd = activeClip.rangeEnd.toFloat()
                    if (nextFrame > rEnd) {
                        currentFrameFloat = rStart
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
            snapMode = snapMode,
            playbackSpeed = playbackSpeed,
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
            onPrevKeyframe = {
                val allFrames = activeClip.tracks.flatMap { it.keyframes }.map { it.frame }.distinct().sorted()
                val prev = allFrames.filter { it < currentFrameFloat.toInt() }.maxOrNull()
                if (prev != null) currentFrameFloat = prev.toFloat()
            },
            onNextKeyframe = {
                val allFrames = activeClip.tracks.flatMap { it.keyframes }.map { it.frame }.distinct().sorted()
                val next = allFrames.filter { it > currentFrameFloat.toInt() }.minOrNull()
                if (next != null) currentFrameFloat = next.toFloat()
            },
            onOpenDirectFrameInput = { isDirectFrameInputOpen = true },
            onOpenJumpTo = { isJumpToOpen = true },
            onOpenTimeTools = { isTimeManipulationOpen = true },
            onLoopModeChange = { mode ->
                activeClip.loopMode = mode
                clipsVersion++
            },
            onFpsChange = { fps ->
                activeClip.fps = fps
                clipsVersion++
            },
            onPlaybackSpeedChange = { spd -> playbackSpeed = spd },
            onAutoKeyToggle = { autoKeyEnabled = !autoKeyEnabled },
            onSnapModeChange = { mode -> snapMode = mode },
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

            // Right Split: Viewport (Top 52%) + Dope Sheet (Bottom 48%)
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
                        .weight(1.05f)
                )

                // Dope Sheet Timeline (Bottom)
                DopeSheetTimeline(
                    activeClip = activeClip,
                    currentFrame = currentFrameFloat,
                    selectedTrackId = selectedTrackId,
                    selectedKeyframeIds = selectedKeyframeIds,
                    snapMode = snapMode,
                    loopRangeStart = loopRangeStart,
                    loopRangeEnd = loopRangeEnd,
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
                    onToggleTrackSolo = { trkId ->
                        backend.toggleTrackSolo(activeClip.id, trkId)
                        clipsVersion++
                    },
                    onToggleTrackMute = { trkId ->
                        backend.toggleTrackMute(activeClip.id, trkId)
                        clipsVersion++
                    },
                    onToggleTrackFocus = { trkId ->
                        backend.toggleTrackFocus(activeClip.id, trkId)
                        clipsVersion++
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
                    onSelectAllKeyframes = {
                        selectedKeyframeIds = activeClip.tracks.flatMap { it.keyframes }.map { it.id }.toSet()
                    },
                    onClearKeyframeSelection = {
                        selectedKeyframeIds = emptySet()
                    },
                    onMoveKeyframesBatch = { delta ->
                        backend.moveSelectedKeyframes(activeClip.id, selectedKeyframeIds, delta)
                        clipsVersion++
                    },
                    onMoveKeyframe = { track, kf, newFrame ->
                        if (selectedKeyframeIds.contains(kf.id) && selectedKeyframeIds.size > 1) {
                            val delta = newFrame - kf.frame
                            backend.moveSelectedKeyframes(activeClip.id, selectedKeyframeIds, delta)
                        } else {
                            backend.moveKeyframe(activeClip.id, track.id, kf.id, newFrame)
                        }
                        clipsVersion++
                    },
                    onKeyframeContextMenu = { track, kf ->
                        contextMenuKeyframe = Pair(track, kf)
                    },
                    onKeyframeStackSelected = { frame, items ->
                        keyframeStackData = Pair(frame, items)
                    },
                    onCopySelectedKeyframes = {
                        backend.copySelectedKeyframes(activeClip.id, selectedKeyframeIds)
                    },
                    onPasteKeyframes = {
                        val pasted = backend.pasteKeyframesAt(activeClip.id, currentFrameFloat.toInt())
                        selectedKeyframeIds = pasted
                        clipsVersion++
                    },
                    onDuplicateSelectedKeyframes = {
                        val dups = backend.duplicateSelectedKeyframes(activeClip.id, selectedKeyframeIds, 2)
                        selectedKeyframeIds = dups
                        clipsVersion++
                    },
                    onDeleteSelectedKeyframes = {
                        backend.deleteSelectedKeyframes(activeClip.id, selectedKeyframeIds)
                        selectedKeyframeIds = emptySet()
                        clipsVersion++
                    },
                    onOpenSmartRetiming = { isSmartRetimingOpen = true },
                    onStretchTiming = { amount ->
                        backend.stretchSelectedKeyframes(activeClip.id, selectedKeyframeIds, amount)
                        clipsVersion++
                    },
                    onCompressTiming = { amount ->
                        backend.compressSelectedKeyframes(activeClip.id, selectedKeyframeIds, amount)
                        clipsVersion++
                    },
                    onDistributeEvenly = {
                        backend.distributeKeyframesEvenly(activeClip.id, selectedKeyframeIds)
                        clipsVersion++
                    },
                    onReverseTiming = {
                        backend.reverseKeyframesTiming(activeClip.id, selectedKeyframeIds)
                        clipsVersion++
                    },
                    onHoldKeyframes = {
                        backend.holdSelectedKeyframes(activeClip.id, selectedKeyframeIds)
                        clipsVersion++
                    },
                    onSetLoopIn = { frame -> loopRangeStart = frame },
                    onSetLoopOut = { frame -> loopRangeEnd = frame },
                    onClearLoopRange = {
                        loopRangeStart = null
                        loopRangeEnd = null
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
                        .weight(0.95f)
                )
            }
        }
    }

    // =========================================================================
    // 3. Modal Dialogs & Sheets
    // =========================================================================

    // A. Direct Numeric Frame Input Dialog
    if (isDirectFrameInputOpen) {
        DirectFrameInputDialog(
            currentFrame = currentFrameFloat.toInt(),
            fps = activeClip.fps,
            maxFrames = activeClip.durationFrames,
            onDismiss = { isDirectFrameInputOpen = false },
            onJumpToFrame = { newFrame ->
                currentFrameFloat = newFrame.toFloat()
            }
        )
    }

    // B. Smart Retiming & Timing Scale Dialog
    if (isSmartRetimingOpen) {
        SmartRetimingDialog(
            clip = activeClip,
            selectedKeyframeIds = selectedKeyframeIds,
            onDismiss = { isSmartRetimingOpen = false },
            onScaleTiming = { factor ->
                backend.scaleKeyframesTiming(activeClip.id, selectedKeyframeIds, factor)
                clipsVersion++
            },
            onSetDuration = { targetFrames ->
                backend.scaleKeyframesToDuration(activeClip.id, selectedKeyframeIds, targetFrames)
                clipsVersion++
            },
            onDistributeEvenly = {
                backend.distributeKeyframesEvenly(activeClip.id, selectedKeyframeIds)
                clipsVersion++
            },
            onReverseTiming = {
                backend.reverseKeyframesTiming(activeClip.id, selectedKeyframeIds)
                clipsVersion++
            },
            onHoldKeyframes = {
                backend.holdSelectedKeyframes(activeClip.id, selectedKeyframeIds)
                clipsVersion++
            }
        )
    }

    // C. Time Manipulation (Insert Time / Delete Range / Remove Gaps) Dialog
    if (isTimeManipulationOpen) {
        TimeManipulationDialog(
            clip = activeClip,
            currentFrame = currentFrameFloat.toInt(),
            rangeStart = loopRangeStart ?: 0,
            rangeEnd = loopRangeEnd ?: activeClip.durationFrames,
            onDismiss = { isTimeManipulationOpen = false },
            onInsertTime = { atFrame, count ->
                backend.insertTimeAt(activeClip.id, atFrame, count)
                clipsVersion++
            },
            onDeleteRange = { startF, endF ->
                backend.deleteTimeRange(activeClip.id, startF, endF)
                clipsVersion++
            },
            onRemoveGaps = { gap ->
                backend.removeGapsBetweenKeyframes(activeClip.id, gap)
                clipsVersion++
            },
            onSetDuration = { newDur ->
                activeClip.durationFrames = newDur
                clipsVersion++
            }
        )
    }

    // D. Jump to Keyframe / Search Dialog
    if (isJumpToOpen) {
        JumpToKeyframeDialog(
            clip = activeClip,
            onDismiss = { isJumpToOpen = false },
            onJumpToFrame = { targetFrame ->
                currentFrameFloat = targetFrame.toFloat()
            }
        )
    }

    // E. Keyframe Stack / Collision Resolver Dialog
    keyframeStackData?.let { (frame, items) ->
        KeyframeStackDialog(
            frame = frame,
            stackedItems = items,
            onDismiss = { keyframeStackData = null },
            onSelectKeyframe = { track, kf ->
                selectedTrackId = track.id
                selectedKeyframeIds = setOf(kf.id)
                currentFrameFloat = frame.toFloat()
                keyframeStackData = null
            }
        )
    }

    // F. Keyframe Context Menu Dialog
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

    // G. Curve Editor Dialog
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

    // H. Sprite Sheet Animator Dialog
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

    // I. Animation Event Dialog
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

    // J. Animation Marker Dialog
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

    // K. New Clip Dialog
    if (isNewClipDialogOpen) {
        KorvaDialog(
            onDismissRequest = { isNewClipDialogOpen = false },
            title = "إنشاء مقطع أنيميشن جديد",
            subtitle = "إضافة مقطع حركة فارغ إلى المشروع",
            icon = Icons.Default.Add,
            maxWidth = 360.dp,
            buttons = {
                KorvaOutlinedButton(
                    text = "إلغاء",
                    onClick = { isNewClipDialogOpen = false },
                    modifier = Modifier.weight(1f)
                )

                KorvaPrimaryButton(
                    text = "إنشاء المقطع",
                    onClick = {
                        if (newClipName.isNotBlank()) {
                            val created = backend.createClip(newClipName, 24, 24)
                            activeClipId = created.id
                            clipsVersion++
                            isNewClipDialogOpen = false
                        }
                    },
                    icon = Icons.Default.Check,
                    modifier = Modifier.weight(1.2f)
                )
            }
        ) {
            OutlinedTextField(
                value = newClipName,
                onValueChange = { newClipName = it },
                label = { Text("اسم المقطع (مثال: Run_Fast)", fontSize = 11.sp) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KorvaPurpleLight,
                    unfocusedBorderColor = StudioBorder,
                    focusedContainerColor = EngineCardBg,
                    unfocusedContainerColor = EngineCardBg,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // L. Rename Clip Dialog
    if (isRenameClipDialogOpen) {
        KorvaDialog(
            onDismissRequest = { isRenameClipDialogOpen = false },
            title = "إعادة تسمية مقطع الأنيميشن",
            subtitle = "تغيير اسم المقطع النشط: ${activeClip.name}",
            icon = Icons.Default.Edit,
            maxWidth = 360.dp,
            buttons = {
                KorvaOutlinedButton(
                    text = "إلغاء",
                    onClick = { isRenameClipDialogOpen = false },
                    modifier = Modifier.weight(1f)
                )

                KorvaPrimaryButton(
                    text = "حفظ التغييرات",
                    onClick = {
                        if (newClipName.isNotBlank()) {
                            backend.renameClip(activeClip.id, newClipName)
                            clipsVersion++
                            isRenameClipDialogOpen = false
                        }
                    },
                    icon = Icons.Default.Check,
                    modifier = Modifier.weight(1.2f)
                )
            }
        ) {
            OutlinedTextField(
                value = newClipName,
                onValueChange = { newClipName = it },
                label = { Text("الاسم الجديد", fontSize = 11.sp) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KorvaPurpleLight,
                    unfocusedBorderColor = StudioBorder,
                    focusedContainerColor = EngineCardBg,
                    unfocusedContainerColor = EngineCardBg,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

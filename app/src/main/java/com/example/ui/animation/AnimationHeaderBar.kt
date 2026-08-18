package com.example.ui.animation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.animation.ClipData
import com.example.engine.animation.LoopMode
import com.example.engine.animation.SnapMode
import com.example.ui.theme.*

@Composable
fun AnimationHeaderBar(
    activeClip: ClipData,
    clips: List<ClipData>,
    isPlaying: Boolean,
    currentFrame: Int,
    autoKeyEnabled: Boolean,
    snapMode: SnapMode,
    playbackSpeed: Float,
    canUndo: Boolean,
    canRedo: Boolean,
    isLibraryOpen: Boolean,
    onToggleLibrary: () -> Unit,
    onSelectClip: (String) -> Unit,
    onNewClip: () -> Unit,
    onDuplicateClip: () -> Unit,
    onRenameClip: () -> Unit,
    onDeleteClip: () -> Unit,
    onPlayToggle: () -> Unit,
    onStop: () -> Unit,
    onPrevFrame: () -> Unit,
    onNextFrame: () -> Unit,
    onFirstFrame: () -> Unit,
    onLastFrame: () -> Unit,
    onPrevKeyframe: () -> Unit,
    onNextKeyframe: () -> Unit,
    onOpenDirectFrameInput: () -> Unit,
    onOpenJumpTo: () -> Unit,
    onOpenTimeTools: () -> Unit,
    onLoopModeChange: (LoopMode) -> Unit,
    onFpsChange: (Int) -> Unit,
    onPlaybackSpeedChange: (Float) -> Unit,
    onAutoKeyToggle: () -> Unit,
    onSnapModeChange: (SnapMode) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onOpenCurveEditor: () -> Unit,
    onOpenSpriteSheet: () -> Unit,
    onSaveClip: () -> Unit,
    onBackToEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClipMenu by remember { mutableStateOf(false) }
    var showFpsMenu by remember { mutableStateOf(false) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var showLoopMenu by remember { mutableStateOf(false) }
    var showSnapMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(StudioHeaderBg)
            .border(width = 0.5.dp, color = StudioBorder)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // =====================================================================
        // Left Section: Back, Library Toggle & Animation Selector Dropdown
        // =====================================================================
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Back Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(EngineCardBg)
                    .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                    .clickable { onBackToEditor() }
                    .padding(horizontal = 6.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "العودة للمحرر", tint = StudioPurpleLight, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("المحرر", color = TextPrimary, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Toggle Library Drawer
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isLibraryOpen) StudioPurpleDark else EngineCardBg)
                    .border(0.5.dp, if (isLibraryOpen) StudioPurpleLight else StudioBorder, RoundedCornerShape(4.dp))
                    .clickable { onToggleLibrary() }
                    .padding(horizontal = 6.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Folder, contentDescription = "المكتبة", tint = if (isLibraryOpen) Color.White else TextSecondary, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("المكتبة", color = if (isLibraryOpen) Color.White else TextSecondary, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Animation Selector Dropdown
            Box {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StudioPurpleBg)
                        .border(0.6.dp, StudioPurpleLight, RoundedCornerShape(4.dp))
                        .clickable { showClipMenu = true }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(activeClip.iconEmoji, fontSize = 10.5.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = activeClip.name,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(13.dp))
                    }
                }

                DropdownMenu(
                    expanded = showClipMenu,
                    onDismissRequest = { showClipMenu = false },
                    modifier = Modifier.background(EngineSurface).border(0.8.dp, StudioBorder)
                ) {
                    Text(
                        text = "مقاطع الأنيميشن",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    HorizontalDivider(color = StudioBorder)

                    clips.forEach { clip ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(clip.iconEmoji, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = clip.name,
                                        color = if (clip.id == activeClip.id) StudioPurpleLight else TextPrimary,
                                        fontWeight = if (clip.id == activeClip.id) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 10.5.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text("${clip.durationFrames}f", color = TextMuted, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace)
                                }
                            },
                            onClick = {
                                onSelectClip(clip.id)
                                showClipMenu = false
                            }
                        )
                    }

                    HorizontalDivider(color = StudioBorder)
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = StudioGreen, modifier = Modifier.size(13.dp)) },
                        text = { Text("+ مقطع جديد (New Clip)", color = StudioGreen, fontSize = 9.5.sp, fontWeight = FontWeight.Bold) },
                        onClick = {
                            showClipMenu = false
                            onNewClip()
                        }
                    )
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = StudioBlue, modifier = Modifier.size(13.dp)) },
                        text = { Text("نسخ المقطع الحالي (Duplicate)", color = TextPrimary, fontSize = 9.5.sp) },
                        onClick = {
                            showClipMenu = false
                            onDuplicateClip()
                        }
                    )
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = StudioYellow, modifier = Modifier.size(13.dp)) },
                        text = { Text("إعادة تسمية (Rename)", color = TextPrimary, fontSize = 9.5.sp) },
                        onClick = {
                            showClipMenu = false
                            onRenameClip()
                        }
                    )
                    if (clips.size > 1) {
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = StudioRed, modifier = Modifier.size(13.dp)) },
                            text = { Text("حذف المقطع (Delete)", color = StudioRed, fontSize = 9.5.sp) },
                            onClick = {
                                showClipMenu = false
                                onDeleteClip()
                            }
                        )
                    }
                }
            }
        }

        // =====================================================================
        // Center Section: Big Touch-First Playback Transport & Navigation
        // =====================================================================
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // First Frame ⏮
            IconButton(onClick = onFirstFrame, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "أول فريم", tint = TextSecondary, modifier = Modifier.size(14.dp))
            }

            // Jump to Prev Keyframe (|◀)
            IconButton(onClick = onPrevKeyframe, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.FastRewind, contentDescription = "المفتاح السابق", tint = StudioPurpleLight, modifier = Modifier.size(14.dp))
            }

            // Step Back ◀
            IconButton(onClick = onPrevFrame, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "فريم للخلف", tint = TextSecondary, modifier = Modifier.size(15.dp))
            }

            // Big Play / Pause Button ▶ ⏸
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) StudioPurpleDark else StudioPurple)
                    .border(1.dp, StudioPurpleLight, CircleShape)
                    .clickable { onPlayToggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Stop ⏹
            IconButton(onClick = onStop, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Stop, contentDescription = "إيقاف", tint = if (isPlaying) StudioRed else TextMuted, modifier = Modifier.size(14.dp))
            }

            // Step Forward ▶
            IconButton(onClick = onNextFrame, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.ChevronRight, contentDescription = "فريم للأمام", tint = TextSecondary, modifier = Modifier.size(15.dp))
            }

            // Jump to Next Keyframe (▶|)
            IconButton(onClick = onNextKeyframe, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.FastForward, contentDescription = "المفتاح التالي", tint = StudioPurpleLight, modifier = Modifier.size(14.dp))
            }

            // Last Frame ⏭
            IconButton(onClick = onLastFrame, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.SkipNext, contentDescription = "آخر فريم", tint = TextSecondary, modifier = Modifier.size(14.dp))
            }

            // Current Frame Counter Badge (Clickable for Direct Input)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(EngineCardBg)
                    .border(0.5.dp, StudioPurpleLight.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .clickable { onOpenDirectFrameInput() }
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "F:$currentFrame/${activeClip.durationFrames}",
                        color = StudioPurpleLight,
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.2f", if (activeClip.fps > 0) currentFrame.toFloat() / activeClip.fps else 0f)}s",
                        color = StudioBlue,
                        fontSize = 8.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // =====================================================================
        // Right Section: Speed, Snap, Loop, FPS, Jump, Time Tools, Undo & Redo
        // =====================================================================
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Playback Speed Selector (0.25x, 0.5x, 1x, 2x, 4x)
            Box {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (playbackSpeed != 1.0f) StudioYellow.copy(alpha = 0.2f) else EngineCardBg)
                        .border(0.5.dp, if (playbackSpeed != 1.0f) StudioYellow else StudioBorder, RoundedCornerShape(4.dp))
                        .clickable { showSpeedMenu = true }
                        .padding(horizontal = 4.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${playbackSpeed}x",
                        color = if (playbackSpeed != 1.0f) StudioYellow else TextSecondary,
                        fontSize = 8.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                DropdownMenu(
                    expanded = showSpeedMenu,
                    onDismissRequest = { showSpeedMenu = false },
                    modifier = Modifier.background(EngineSurface).border(0.8.dp, StudioBorder)
                ) {
                    listOf(0.25f, 0.5f, 1.0f, 1.5f, 2.0f, 4.0f).forEach { spd ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${spd}x ${if (spd == 1.0f) "(عادي)" else if (spd < 1f) "(بطيء)" else "(سريع)"}",
                                    color = if (spd == playbackSpeed) StudioYellow else TextPrimary,
                                    fontSize = 9.5.sp
                                )
                            },
                            onClick = {
                                onPlaybackSpeedChange(spd)
                                showSpeedMenu = false
                            }
                        )
                    }
                }
            }

            // Snap Mode Selector (Frame, Keyframe, Marker, Free)
            Box {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (snapMode != SnapMode.FREE_MOVE) StudioPurpleDark else EngineCardBg)
                        .border(0.5.dp, if (snapMode != SnapMode.FREE_MOVE) StudioPurpleLight else StudioBorder, RoundedCornerShape(4.dp))
                        .clickable { showSnapMenu = true }
                        .padding(horizontal = 4.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (snapMode) {
                                SnapMode.SNAP_FRAME -> Icons.Default.GridOn
                                SnapMode.SNAP_KEYFRAME -> Icons.Default.Tune
                                SnapMode.SNAP_MARKER -> Icons.Default.Bookmark
                                SnapMode.FREE_MOVE -> Icons.Default.CropFree
                            },
                            contentDescription = snapMode.label,
                            tint = if (snapMode != SnapMode.FREE_MOVE) StudioPurpleLight else TextMuted,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(snapMode.iconName, color = if (snapMode != SnapMode.FREE_MOVE) Color.White else TextMuted, fontSize = 8.sp)
                    }
                }

                DropdownMenu(
                    expanded = showSnapMenu,
                    onDismissRequest = { showSnapMenu = false },
                    modifier = Modifier.background(EngineSurface).border(0.8.dp, StudioBorder)
                ) {
                    SnapMode.values().forEach { mode ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${mode.label} (${mode.iconName})",
                                    color = if (mode == snapMode) StudioPurpleLight else TextPrimary,
                                    fontSize = 9.5.sp
                                )
                            },
                            onClick = {
                                onSnapModeChange(mode)
                                showSnapMenu = false
                            }
                        )
                    }
                }
            }

            // Jump To Search Icon
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(EngineCardBg)
                    .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                    .clickable { onOpenJumpTo() }
                    .padding(horizontal = 5.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TravelExplore, contentDescription = "بحث وانتقال", tint = StudioPurpleLight, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("انتقال", color = TextSecondary, fontSize = 8.5.sp)
                }
            }

            // Time Manipulation Tools
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(EngineCardBg)
                    .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                    .clickable { onOpenTimeTools() }
                    .padding(horizontal = 5.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HourglassTop, contentDescription = "أدوات الزمن", tint = StudioYellow, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("الزمن", color = TextSecondary, fontSize = 8.5.sp)
                }
            }

            // Loop Mode Selector
            Box {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (activeClip.loopMode != LoopMode.OFF) StudioPurpleDark else EngineCardBg)
                        .border(0.5.dp, if (activeClip.loopMode != LoopMode.OFF) StudioPurpleLight else StudioBorder, RoundedCornerShape(4.dp))
                        .clickable { showLoopMenu = true }
                        .padding(horizontal = 4.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Repeat, contentDescription = null, tint = if (activeClip.loopMode != LoopMode.OFF) StudioPurpleLight else TextMuted, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(activeClip.loopMode.label, color = TextPrimary, fontSize = 8.5.sp)
                    }
                }

                DropdownMenu(
                    expanded = showLoopMenu,
                    onDismissRequest = { showLoopMenu = false },
                    modifier = Modifier.background(EngineSurface).border(0.8.dp, StudioBorder)
                ) {
                    LoopMode.values().forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.label, color = if (mode == activeClip.loopMode) StudioPurpleLight else TextPrimary, fontSize = 9.5.sp) },
                            onClick = {
                                onLoopModeChange(mode)
                                showLoopMenu = false
                            }
                        )
                    }
                }
            }

            // FPS Stepper Selector
            Box {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(EngineCardBg)
                        .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                        .clickable { showFpsMenu = true }
                        .padding(horizontal = 4.dp, vertical = 3.dp)
                ) {
                    Text("${activeClip.fps} FPS", color = StudioBlue, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }

                DropdownMenu(
                    expanded = showFpsMenu,
                    onDismissRequest = { showFpsMenu = false },
                    modifier = Modifier.background(EngineSurface).border(0.8.dp, StudioBorder)
                ) {
                    listOf(6, 8, 12, 15, 24, 30, 60).forEach { rate ->
                        DropdownMenuItem(
                            text = { Text("$rate FPS", color = if (rate == activeClip.fps) StudioBlue else TextPrimary, fontSize = 9.5.sp) },
                            onClick = {
                                onFpsChange(rate)
                                showFpsMenu = false
                            }
                        )
                    }
                }
            }

            // 🔴 Auto Key Toggle Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (autoKeyEnabled) Color(0xFF450A0A) else EngineCardBg)
                    .border(0.7.dp, if (autoKeyEnabled) StudioRed else StudioBorder, RoundedCornerShape(4.dp))
                    .clickable { onAutoKeyToggle() }
                    .padding(horizontal = 4.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (autoKeyEnabled) StudioRed else TextMuted)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("AutoKey", color = if (autoKeyEnabled) StudioRed else TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Curve Editor Modal Opener
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(EngineCardBg)
                    .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                    .clickable { onOpenCurveEditor() }
                    .padding(horizontal = 5.dp, vertical = 3.dp)
            ) {
                Icon(Icons.Default.ShowChart, contentDescription = "منحنى الحركة", tint = StudioYellow, modifier = Modifier.size(11.dp))
            }

            // Sprite Sheet Slicer Opener
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(EngineCardBg)
                    .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                    .clickable { onOpenSpriteSheet() }
                    .padding(horizontal = 5.dp, vertical = 3.dp)
            ) {
                Icon(Icons.Default.GridOn, contentDescription = "Sprite Sheet", tint = StudioGreen, modifier = Modifier.size(11.dp))
            }

            // Undo / Redo
            IconButton(onClick = onUndo, enabled = canUndo, modifier = Modifier.size(22.dp)) {
                Icon(Icons.Default.Undo, contentDescription = "تراجع", tint = if (canUndo) TextPrimary else TextMuted.copy(alpha = 0.3f), modifier = Modifier.size(12.dp))
            }

            IconButton(onClick = onRedo, enabled = canRedo, modifier = Modifier.size(22.dp)) {
                Icon(Icons.Default.Redo, contentDescription = "إعادة", tint = if (canRedo) TextPrimary else TextMuted.copy(alpha = 0.3f), modifier = Modifier.size(12.dp))
            }

            // Save Clip Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(StudioPurple)
                    .border(0.6.dp, StudioPurpleLight, RoundedCornerShape(4.dp))
                    .clickable { onSaveClip() }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Save, contentDescription = "حفظ", tint = Color.White, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("حفظ", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

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
import com.example.ui.theme.*

@Composable
fun AnimationHeaderBar(
    activeClip: ClipData,
    clips: List<ClipData>,
    isPlaying: Boolean,
    currentFrame: Int,
    autoKeyEnabled: Boolean,
    snapEnabled: Boolean,
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
    onLoopModeChange: (LoopMode) -> Unit,
    onFpsChange: (Int) -> Unit,
    onAutoKeyToggle: () -> Unit,
    onSnapToggle: () -> Unit,
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
    var showLoopMenu by remember { mutableStateOf(false) }

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
                    .padding(horizontal = 7.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "العودة للمحرر", tint = StudioPurpleLight, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("المحرر", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("المكتبة", color = if (isLibraryOpen) Color.White else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(activeClip.iconEmoji, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = activeClip.name,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(14.dp))
                    }
                }

                DropdownMenu(
                    expanded = showClipMenu,
                    onDismissRequest = { showClipMenu = false },
                    modifier = Modifier.background(EngineSurface).border(0.8.dp, StudioBorder)
                ) {
                    Text(
                        text = "اختر مقطع الأنيميشن",
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
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text("${clip.durationFrames}f", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
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
                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = StudioGreen, modifier = Modifier.size(14.dp)) },
                        text = { Text("+ مقطع جديد (New Clip)", color = StudioGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        onClick = {
                            showClipMenu = false
                            onNewClip()
                        }
                    )
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = StudioBlue, modifier = Modifier.size(14.dp)) },
                        text = { Text("نسخ المقطع الحالي (Duplicate)", color = TextPrimary, fontSize = 10.sp) },
                        onClick = {
                            showClipMenu = false
                            onDuplicateClip()
                        }
                    )
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = StudioYellow, modifier = Modifier.size(14.dp)) },
                        text = { Text("إعادة تسمية (Rename)", color = TextPrimary, fontSize = 10.sp) },
                        onClick = {
                            showClipMenu = false
                            onRenameClip()
                        }
                    )
                    if (clips.size > 1) {
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = StudioRed, modifier = Modifier.size(14.dp)) },
                            text = { Text("حذف المقطع (Delete)", color = StudioRed, fontSize = 10.sp) },
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
        // Center Section: Big Touch-First Playback Transport Controls
        // =====================================================================
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // First Frame ⏮
            IconButton(onClick = onFirstFrame, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "أول فريم", tint = TextSecondary, modifier = Modifier.size(15.dp))
            }

            // Step Back ◀
            IconButton(onClick = onPrevFrame, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "فريم للخلف", tint = TextSecondary, modifier = Modifier.size(16.dp))
            }

            // Big Play / Pause Button ▶ ⏸
            Box(
                modifier = Modifier
                    .size(34.dp)
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
                    modifier = Modifier.size(20.dp)
                )
            }

            // Stop ⏹
            IconButton(onClick = onStop, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Stop, contentDescription = "إيقاف", tint = if (isPlaying) StudioRed else TextMuted, modifier = Modifier.size(15.dp))
            }

            // Step Forward ▶
            IconButton(onClick = onNextFrame, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.ChevronRight, contentDescription = "فريم للأمام", tint = TextSecondary, modifier = Modifier.size(16.dp))
            }

            // Last Frame ⏭
            IconButton(onClick = onLastFrame, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.SkipNext, contentDescription = "آخر فريم", tint = TextSecondary, modifier = Modifier.size(15.dp))
            }

            // Current Frame Counter Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(EngineCardBg)
                    .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "F: $currentFrame / ${activeClip.durationFrames}",
                    color = StudioPurpleLight,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // =====================================================================
        // Right Section: Loop, FPS Stepper, AutoKey, Tools, Undo/Redo & Save
        // =====================================================================
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Loop Mode Selector
            Box {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (activeClip.loopMode != LoopMode.OFF) StudioPurpleDark else EngineCardBg)
                        .border(0.5.dp, if (activeClip.loopMode != LoopMode.OFF) StudioPurpleLight else StudioBorder, RoundedCornerShape(4.dp))
                        .clickable { showLoopMenu = true }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Repeat, contentDescription = null, tint = if (activeClip.loopMode != LoopMode.OFF) StudioPurpleLight else TextMuted, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(activeClip.loopMode.label, color = TextPrimary, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                DropdownMenu(
                    expanded = showLoopMenu,
                    onDismissRequest = { showLoopMenu = false },
                    modifier = Modifier.background(EngineSurface).border(0.8.dp, StudioBorder)
                ) {
                    LoopMode.values().forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.label, color = if (mode == activeClip.loopMode) StudioPurpleLight else TextPrimary, fontSize = 10.sp) },
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
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text("${activeClip.fps} FPS", color = StudioBlue, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }

                DropdownMenu(
                    expanded = showFpsMenu,
                    onDismissRequest = { showFpsMenu = false },
                    modifier = Modifier.background(EngineSurface).border(0.8.dp, StudioBorder)
                ) {
                    listOf(6, 8, 12, 15, 24, 30, 60).forEach { rate ->
                        DropdownMenuItem(
                            text = { Text("$rate FPS", color = if (rate == activeClip.fps) StudioBlue else TextPrimary, fontSize = 10.sp) },
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
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (autoKeyEnabled) StudioRed else TextMuted)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Auto Key", color = if (autoKeyEnabled) StudioRed else TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            // 🧲 Snap Toggle Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (snapEnabled) StudioPurpleDark else EngineCardBg)
                    .border(0.5.dp, if (snapEnabled) StudioPurpleLight else StudioBorder, RoundedCornerShape(4.dp))
                    .clickable { onSnapToggle() }
                    .padding(horizontal = 5.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.FilterCenterFocus, contentDescription = "Snap", tint = if (snapEnabled) StudioPurpleLight else TextMuted, modifier = Modifier.size(12.dp))
            }

            // Curve Editor Modal Opener
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(EngineCardBg)
                    .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                    .clickable { onOpenCurveEditor() }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ShowChart, contentDescription = "منحنى الحركة", tint = StudioYellow, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Curves", color = StudioYellow, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Sprite Sheet Slicer Opener
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(EngineCardBg)
                    .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                    .clickable { onOpenSpriteSheet() }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.GridOn, contentDescription = "Sprite Sheet", tint = StudioGreen, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Sprites", color = StudioGreen, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Undo / Redo
            IconButton(onClick = onUndo, enabled = canUndo, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Undo, contentDescription = "تراجع", tint = if (canUndo) TextPrimary else TextMuted.copy(alpha = 0.4f), modifier = Modifier.size(13.dp))
            }

            IconButton(onClick = onRedo, enabled = canRedo, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Redo, contentDescription = "إعادة", tint = if (canRedo) TextPrimary else TextMuted.copy(alpha = 0.4f), modifier = Modifier.size(13.dp))
            }

            // Save Clip Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(StudioPurple)
                    .border(0.6.dp, StudioPurpleLight, RoundedCornerShape(4.dp))
                    .clickable { onSaveClip() }
                    .padding(horizontal = 7.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Save, contentDescription = "حفظ", tint = Color.White, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("حفظ", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectEntity
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.EngineSurface
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioPurple
import com.example.ui.theme.StudioPurpleBorder
import com.example.ui.theme.StudioPurpleDark
import com.example.ui.theme.StudioPurpleLight
import com.example.ui.theme.StudioRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StudioHeader(
    projectName: String,
    projectType: String,
    isPlaying: Boolean,
    isPaused: Boolean,
    isHierarchyVisible: Boolean,
    isInspectorVisible: Boolean,
    isTimelineVisible: Boolean,
    isFullscreen: Boolean,
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    availableProjects: List<ProjectEntity> = emptyList(),
    onToggleHierarchy: () -> Unit,
    onToggleInspector: () -> Unit,
    onToggleTimeline: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onUndoClick: () -> Unit = {},
    onRedoClick: () -> Unit = {},
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onStopClick: () -> Unit,
    onBuildApkClick: () -> Unit,
    onSaveClick: () -> Unit,
    onProjectSwitch: (String) -> Unit,
    onBackToProjects: () -> Unit,
    onOpenAnimationPage: () -> Unit = {}
) {
    var showProjectDropdown by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(EngineSurface)
            .border(width = 0.6.dp, color = StudioBorder)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Section: Engine Logo + Project Dropdown
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Engine Logo Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onBackToProjects() }
                    .padding(vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(StudioPurpleDark, StudioPurple)
                            )
                        )
                        .border(0.6.dp, StudioPurpleLight, RoundedCornerShape(5.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "K",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(5.dp))

                Text(
                    text = "Korva",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(3.dp))

                Text(
                    text = "v1.0",
                    color = TextMuted,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(
                modifier = Modifier
                    .height(14.dp)
                    .width(0.6.dp)
                    .background(StudioBorder)
            )

            // Project Selector Dropdown
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(EngineCardBg)
                        .border(0.5.dp, StudioBorder, RoundedCornerShape(5.dp))
                        .clickable { showProjectDropdown = true }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = projectName,
                            color = TextPrimary,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = projectType,
                            color = TextMuted,
                            fontSize = 7.5.sp,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "قائمة المشاريع",
                        tint = TextSecondary,
                        modifier = Modifier.size(11.dp)
                    )
                }

                DropdownMenu(
                    expanded = showProjectDropdown,
                    onDismissRequest = { showProjectDropdown = false }
                ) {
                    if (availableProjects.isNotEmpty()) {
                        availableProjects.forEach { proj ->
                            DropdownMenuItem(
                                text = { Text("${proj.name} (${proj.templateType})", fontSize = 10.sp) },
                                onClick = {
                                    onProjectSwitch(proj.name)
                                    showProjectDropdown = false
                                }
                            )
                        }
                    } else {
                        listOf("Dark Village", "Cyber Dungeon", "Pixel Platformer").forEach { name ->
                            DropdownMenuItem(
                                text = { Text(name, fontSize = 10.sp) },
                                onClick = {
                                    onProjectSwitch(name)
                                    showProjectDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Center Section: Undo/Redo + Transport Controls (Play, Pause, Stop) + View Layout Toggles
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Undo / Redo Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(EngineCardBg)
                    .border(0.5.dp, StudioBorder, RoundedCornerShape(5.dp))
                    .padding(horizontal = 2.dp, vertical = 2.dp)
            ) {
                // Undo
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .clickable(enabled = canUndo) { onUndoClick() }
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "↩",
                        color = if (canUndo) StudioPurpleLight else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Redo
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .clickable(enabled = canRedo) { onRedoClick() }
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "↪",
                        color = if (canRedo) StudioPurpleLight else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Transport Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(EngineCardBg)
                    .border(0.5.dp, StudioBorder, RoundedCornerShape(5.dp))
                    .padding(horizontal = 3.dp, vertical = 2.dp)
            ) {
                // Play Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isPlaying && !isPaused) StudioPurple else Color.Transparent)
                        .clickable { onPlayClick() }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                        .testTag("studio_play_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "تشغيل",
                            tint = if (isPlaying && !isPaused) Color.White else StudioPurpleLight,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "Play",
                            color = if (isPlaying && !isPaused) Color.White else TextPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Pause Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isPaused) StudioPurple.copy(alpha = 0.3f) else Color.Transparent)
                        .clickable { onPauseClick() }
                        .padding(horizontal = 5.dp, vertical = 3.dp)
                        .testTag("studio_pause_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "إيقاف مؤقت",
                            tint = TextSecondary,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "Pause",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Stop Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onStopClick() }
                        .padding(horizontal = 5.dp, vertical = 3.dp)
                        .testTag("studio_stop_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(StudioRed)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Stop",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Panel Visibility Dock Toggles
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(EngineCardBg)
                    .border(0.5.dp, StudioBorder, RoundedCornerShape(5.dp))
                    .padding(horizontal = 2.dp, vertical = 2.dp)
            ) {
                // Toggle Hierarchy (Left Panel)
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isHierarchyVisible) StudioPurpleDark else Color.Transparent)
                        .clickable { onToggleHierarchy() }
                        .testTag("toggle_hierarchy_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "تبديل لوحة العناصر والطبقات",
                        tint = if (isHierarchyVisible) StudioPurpleLight else TextMuted,
                        modifier = Modifier.size(11.dp)
                    )
                }

                // Toggle Timeline (Bottom Panel)
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isTimelineVisible) StudioPurpleDark else Color.Transparent)
                        .clickable { onToggleTimeline() }
                        .testTag("toggle_timeline_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = "تبديل شريط الحركة الزمني",
                        tint = if (isTimelineVisible) StudioPurpleLight else TextMuted,
                        modifier = Modifier.size(11.dp)
                    )
                }

                // Dedicated Animations Page Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(StudioPurpleDark)
                        .border(0.5.dp, StudioPurpleLight, RoundedCornerShape(3.dp))
                        .clickable { onOpenAnimationPage() }
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                        .testTag("open_animations_page_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = "صفحة الأنيميشن",
                            tint = StudioPurpleLight,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Animations",
                            color = Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Toggle Inspector (Right Panel)
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isInspectorVisible) StudioPurpleDark else Color.Transparent)
                        .clickable { onToggleInspector() }
                        .testTag("toggle_inspector_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "تبديل لوحة الخصائص والمكونات",
                        tint = if (isInspectorVisible) StudioPurpleLight else TextMuted,
                        modifier = Modifier.size(11.dp)
                    )
                }

                // Toggle Fullscreen
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isFullscreen) StudioPurple else Color.Transparent)
                        .clickable { onToggleFullscreen() }
                        .testTag("toggle_fullscreen_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = "وضع ملء الشاشة لمنطقة التصميم",
                        tint = if (isFullscreen) Color.White else TextMuted,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }

        // Right Section: Build APK + Save
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Build APK Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(StudioPurpleDark, StudioPurple)
                        )
                    )
                    .border(0.6.dp, StudioPurpleBorder, RoundedCornerShape(5.dp))
                    .clickable { onBuildApkClick() }
                    .padding(horizontal = 7.dp, vertical = 3.dp)
                    .testTag("build_apk_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Build",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Save Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(EngineCardBg)
                    .border(0.5.dp, StudioBorder, RoundedCornerShape(5.dp))
                    .clickable { onSaveClick() }
                    .padding(horizontal = 6.dp, vertical = 3.dp)
                    .testTag("save_project_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save",
                        tint = TextPrimary,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Save",
                        color = TextPrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

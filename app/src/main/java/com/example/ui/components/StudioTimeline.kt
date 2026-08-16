package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EngineBackground
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.EngineSurface
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioPurple
import com.example.ui.theme.StudioPurpleDark
import com.example.ui.theme.StudioPurpleLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class AnimationTrack(
    val name: String,
    val framesCount: Int,
    val durationSeconds: Float,
    val color: Color
)

@Composable
fun StudioTimeline(
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onCollapse: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedClip by remember { mutableStateOf("Idle") }
    var showClipDropdown by remember { mutableStateOf(false) }
    val timelineScroll = rememberScrollState()

    val tracks = remember {
        listOf(
            AnimationTrack("Idle", 6, 0.30f, StudioPurple),
            AnimationTrack("Run", 8, 0.50f, StudioPurple.copy(alpha = 0.8f)),
            AnimationTrack("Jump", 4, 0.40f, StudioPurple.copy(alpha = 0.65f)),
            AnimationTrack("Attack", 7, 0.45f, StudioPurple.copy(alpha = 0.5f))
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(EngineSurface)
            .border(width = 0.6.dp, color = StudioBorder)
    ) {
        // ========================================================
        // 1. Timeline Header & Transport Controls (Compact: 20dp)
        // ========================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(EngineCardBg)
                .border(0.4.dp, StudioBorder)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Title & Clip Selector
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "TIMELINE",
                    color = TextSecondary,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.width(4.dp))

                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(EngineSurface)
                            .border(0.4.dp, StudioBorder, RoundedCornerShape(3.dp))
                            .clickable { showClipDropdown = true }
                            .padding(horizontal = 4.dp, vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedClip,
                            color = StudioPurpleLight,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(9.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showClipDropdown,
                        onDismissRequest = { showClipDropdown = false }
                    ) {
                        listOf("Idle", "Run", "Jump", "Attack").forEach { clip ->
                            DropdownMenuItem(
                                text = { Text(clip, fontSize = 9.sp) },
                                onClick = {
                                    selectedClip = clip
                                    showClipDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Transport: Prev, Play, Next, Timecode
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Frame",
                    tint = TextSecondary,
                    modifier = Modifier.size(10.dp)
                )

                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(StudioPurple)
                        .clickable { onTogglePlay() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play Timeline",
                        tint = Color.White,
                        modifier = Modifier.size(8.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Frame",
                    tint = TextSecondary,
                    modifier = Modifier.size(10.dp)
                )

                Text(
                    text = "00:00:12 / 60 FPS",
                    color = TextMuted,
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace
                )

                // Collapse Button
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(EngineSurface)
                        .border(0.4.dp, StudioBorder, RoundedCornerShape(2.dp))
                        .clickable { onCollapse() }
                        .testTag("collapse_timeline_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "إغلاق شريط الحركة",
                        tint = TextSecondary,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }

        // ========================================================
        // 2. Timeline Tracks and Keyframe Grid
        // ========================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Track Labels Column
            Column(
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight()
                    .background(EngineCardBg)
                    .border(0.4.dp, StudioBorder)
                    .padding(2.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                tracks.take(2).forEach { track ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (track.name == selectedClip) StudioPurpleDark else EngineSurface)
                            .padding(horizontal = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = track.name,
                            color = TextPrimary,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(8.dp)
                        )
                    }
                }
            }

            // Keyframe Grid Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .horizontalScroll(timelineScroll)
                    .background(EngineBackground)
            ) {
                Canvas(
                    modifier = Modifier
                        .width(400.dp)
                        .fillMaxHeight()
                ) {
                    val frameStep = 16.dp.toPx()
                    var f = 0
                    while (f * frameStep < size.width) {
                        val x = f * frameStep
                        val isMajor = f % 5 == 0

                        drawLine(
                            color = if (isMajor) StudioBorder else Color(0x0CFFFFFF),
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = if (isMajor) 0.8f else 0.4f
                        )

                        // Keyframe Diamond on Active Track
                        if (f == 0 || f == 4 || f == 8 || f == 12 || f == 16) {
                            val diamondPath = Path().apply {
                                val cy = 9.dp.toPx()
                                moveTo(x, cy - 3.dp.toPx())
                                lineTo(x + 3.dp.toPx(), cy)
                                lineTo(x, cy + 3.dp.toPx())
                                lineTo(x - 3.dp.toPx(), cy)
                                close()
                            }
                            drawPath(diamondPath, StudioPurpleLight)
                        }

                        f++
                    }

                    // Scrubber Playhead Line
                    val playheadX = 4 * frameStep
                    drawLine(
                        color = Color(0xFFEF4444),
                        start = Offset(playheadX, 0f),
                        end = Offset(playheadX, size.height),
                        strokeWidth = 1.2f
                    )
                }
            }
        }
    }
}

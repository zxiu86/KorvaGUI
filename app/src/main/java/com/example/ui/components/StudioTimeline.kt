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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            .height(78.dp)
            .background(EngineSurface)
            .border(width = 0.6.dp, color = StudioBorder)
    ) {
        // ========================================================
        // 1. Timeline Header & Transport Controls (Compact: 22dp)
        // ========================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
                .background(EngineCardBg)
                .border(0.4.dp, StudioBorder)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "TIMELINE",
                    color = TextSecondary,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
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
                    modifier = Modifier.size(11.dp)
                )

                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .clip(CircleShape)
                        .background(StudioPurple)
                        .clickable { onTogglePlay() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play Timeline",
                        tint = Color.White,
                        modifier = Modifier.size(9.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Frame",
                    tint = TextSecondary,
                    modifier = Modifier.size(11.dp)
                )

                Spacer(modifier = Modifier.width(3.dp))

                Text(
                    text = "00:00:00",
                    color = TextPrimary,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Clip Selector Dropdown
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(EngineSurface)
                            .border(0.5.dp, StudioBorder, RoundedCornerShape(3.dp))
                            .clickable { showClipDropdown = true }
                            .padding(horizontal = 4.dp, vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedClip,
                            color = StudioPurpleLight,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(10.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showClipDropdown,
                        onDismissRequest = { showClipDropdown = false }
                    ) {
                        listOf("Idle", "Run", "Jump", "Attack", "Hit").forEach { clip ->
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

            // Right side stats (FPS + Length)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "60 FPS",
                    color = TextMuted,
                    fontSize = 7.5.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "0.80s",
                    color = StudioPurpleLight,
                    fontSize = 7.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // ========================================================
        // 2. Timeline Tracks and Keyframe Canvas (Compact: 56dp)
        // ========================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Left Track List (Narrow: 65dp)
            Column(
                modifier = Modifier
                    .width(65.dp)
                    .fillMaxHeight()
                    .background(EngineCardBg)
                    .border(width = 0.4.dp, color = StudioBorder)
            ) {
                // Header spacer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(11.dp)
                        .background(EngineSurface)
                        .padding(horizontal = 3.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "TRACKS",
                        color = TextMuted,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Track Names
                tracks.forEach { track ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(11.dp)
                            .padding(horizontal = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(3.dp)
                                    .clip(CircleShape)
                                    .background(track.color)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = track.name,
                                color = TextPrimary,
                                fontSize = 7.5.sp,
                                maxLines = 1
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(8.dp)
                        )
                    }
                }
            }

            // Right Keyframe Area with horizontal scroll
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(EngineSurface)
                    .horizontalScroll(timelineScroll)
            ) {
                // Canvas Grid Lines and Ticks
                Canvas(
                    modifier = Modifier
                        .width(400.dp)
                        .fillMaxHeight()
                ) {
                    val step = 30.dp.toPx()
                    var x = 0f
                    while (x < size.width) {
                        drawLine(
                            color = Color(0xFF1E2433),
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 0.5f
                        )
                        x += step
                    }
                }

                Column(
                    modifier = Modifier
                        .width(400.dp)
                        .fillMaxHeight()
                ) {
                    // Time Ruler Numbers
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(11.dp)
                            .padding(start = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("0:00", "0:05", "0:10", "0:15", "0:20", "0:25", "0:30", "0:35", "0:40", "0:50").forEach { t ->
                            Text(
                                text = t,
                                color = TextMuted,
                                fontSize = 6.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Keyframe Strip Bars (Lightweight vector rendering)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        tracks.forEachIndexed { idx, track ->
                            val trackWidth = (120 + idx * 25).dp

                            Box(
                                modifier = Modifier
                                    .padding(start = 6.dp)
                                    .width(trackWidth)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(StudioPurpleDark, StudioPurple.copy(alpha = 0.6f))
                                        )
                                    )
                                    .border(0.4.dp, StudioPurpleLight.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                                    .padding(horizontal = 2.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (i in 0 until track.framesCount) {
                                        // Lightweight Vector Diamond Keyframe
                                        KeyframeDiamond(color = StudioPurpleLight)
                                    }
                                }
                            }
                        }
                    }
                }

                // Playhead red/purple indicator
                Box(
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(StudioPurpleLight)
                )
            }
        }
    }
}

@Composable
private fun KeyframeDiamond(color: Color) {
    Canvas(modifier = Modifier.size(6.dp)) {
        val path = Path().apply {
            moveTo(size.width / 2, 0f)
            lineTo(size.width, size.height / 2)
            lineTo(size.width / 2, size.height)
            lineTo(0f, size.height / 2)
            close()
        }
        drawPath(path = path, color = color)
    }
}

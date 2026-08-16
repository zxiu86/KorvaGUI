package com.example.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.EngineSurface
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioPurple
import com.example.ui.theme.StudioPurpleBg
import com.example.ui.theme.StudioPurpleDark
import com.example.ui.theme.StudioPurpleGlass
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
            .height(130.dp)
            .background(EngineSurface)
            .border(width = 0.8.dp, color = StudioBorder)
    ) {
        // ========================================================
        // 1. Timeline Header & Transport Controls
        // ========================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .background(EngineCardBg)
                .border(0.5.dp, StudioBorder)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Title
            Text(
                text = "ANIMATION TIMELINE",
                color = TextSecondary,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )

            // Transport: Prev, Play, Next, Timecode
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Frame",
                    tint = TextSecondary,
                    modifier = Modifier.size(13.dp)
                )

                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(StudioPurple)
                        .clickable { onTogglePlay() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play Timeline",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Frame",
                    tint = TextSecondary,
                    modifier = Modifier.size(13.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "00:00:00",
                    color = TextPrimary,
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Clip Selector Dropdown
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(EngineSurface)
                            .border(0.6.dp, StudioBorder, RoundedCornerShape(4.dp))
                            .clickable { showClipDropdown = true }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedClip,
                            color = StudioPurpleLight,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showClipDropdown,
                        onDismissRequest = { showClipDropdown = false }
                    ) {
                        listOf("Idle", "Run", "Jump", "Attack", "Hit", "Die").forEach { clip ->
                            DropdownMenuItem(
                                text = { Text(clip, fontSize = 11.sp) },
                                onClick = {
                                    selectedClip = clip
                                    showClipDropdown = false
                                }
                            )
                        }
                    }
                }

                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "خيارات",
                    tint = TextMuted,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        // ========================================================
        // 2. Timeline Tracks & Scrubber
        // ========================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Left Track Labels (Idle, Run, Jump, Attack)
            Column(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .background(EngineSurface)
                    .border(0.5.dp, StudioBorder)
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                tracks.forEach { track ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(StudioPurpleBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_player_sprite),
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = track.name,
                                    color = TextPrimary,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${track.framesCount} Frames",
                                    color = TextMuted,
                                    fontSize = 7.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            // Right Scrollable Time Grid & Keyframes
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFF0D0F16))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(timelineScroll)
                ) {
                    // Time Markers Bar
                    Row(
                        modifier = Modifier
                            .height(14.dp)
                            .padding(start = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(45.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("0:00", "0:05", "0:10", "0:15", "0:20", "0:25", "0:30", "0:35", "0:40", "0:50", "0:55", "1:00").forEach { t ->
                            Text(
                                text = t,
                                color = TextMuted,
                                fontSize = 7.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Keyframe Strip Bars
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        tracks.forEachIndexed { idx, track ->
                            val trackWidth = (180 + idx * 30).dp

                            Box(
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .width(trackWidth)
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(StudioPurpleDark, StudioPurple.copy(alpha = 0.7f))
                                        )
                                    )
                                    .border(0.6.dp, StudioPurpleLight.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (i in 0 until track.framesCount) {
                                        Image(
                                            painter = painterResource(id = R.drawable.img_player_sprite),
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Scrubber line at 0:00
                Box(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .width(1.5.dp)
                        .fillMaxHeight()
                        .background(StudioPurpleLight)
                )
            }
        }
    }
}

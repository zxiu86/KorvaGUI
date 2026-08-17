package com.example.ui.panels

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun AnimationTimelinePanel(
    selectedClip: String = "Idle",
    onClipChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val clips = remember { listOf("Idle", "Walk", "Run", "Jump", "Attack", "Hurt") }
    var isPlaying by remember { mutableStateOf(false) }
    var currentFrame by remember { mutableFloatStateOf(0f) }
    val totalFrames = 30
    val keyframes = remember { listOf(0, 6, 12, 18, 24, 30) }

    // Playback loop
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                delay(60) // ~16 fps
                currentFrame = (currentFrame + 1) % totalFrames
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(EngineSurface)
            .border(width = 0.8.dp, color = StudioBorder)
    ) {
        // Top Timeline Header & Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(EngineBackground)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Movie, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("محرر التحريك (Animation Timeline)", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Playback and Frame controls
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = { currentFrame = 0f },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Start", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }

                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(StudioPurple)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = { currentFrame = (totalFrames - 1).toFloat() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "End", tint = TextSecondary, modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "F: ${currentFrame.toInt()} / $totalFrames",
                    color = StudioPurpleLight,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Clips Selector Bar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(EngineCardBg)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(clips) { clip ->
                val isSelected = clip == selectedClip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) StudioPurple else Color.Transparent)
                        .border(0.6.dp, if (isSelected) StudioPurpleLight else StudioBorder, RoundedCornerShape(4.dp))
                        .clickable { onClipChange(clip) }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = clip,
                        color = if (isSelected) Color.White else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Scrubbable Keyframe Track Canvas
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(EngineBackground)
                .pointerInput(totalFrames) {
                    detectTapGestures { tapOffset ->
                        val frameWidth = size.width / totalFrames.toFloat()
                        currentFrame = (tapOffset.x / frameWidth).coerceIn(0f, (totalFrames - 1).toFloat())
                    }
                }
                .pointerInput(totalFrames) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val frameWidth = size.width / totalFrames.toFloat()
                        currentFrame = (change.position.x / frameWidth).coerceIn(0f, (totalFrames - 1).toFloat())
                    }
                }
        ) {
            val trackWidth = constraints.maxWidth.toFloat()
            val trackHeight = constraints.maxHeight.toFloat()
            val frameWidth = trackWidth / totalFrames.toFloat()

            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background Track Grid
                for (f in 0..totalFrames) {
                    val x = f * frameWidth
                    val isMajor = f % 5 == 0
                    drawLine(
                        color = if (isMajor) StudioBorder.copy(alpha = 0.8f) else StudioBorder.copy(alpha = 0.3f),
                        start = Offset(x, 0f),
                        end = Offset(x, trackHeight),
                        strokeWidth = if (isMajor) 1f else 0.5f
                    )
                }

                // Horizontal Track Line
                val trackY = trackHeight * 0.55f
                drawLine(
                    color = StudioPurpleLight.copy(alpha = 0.6f),
                    start = Offset(0f, trackY),
                    end = Offset(trackWidth, trackY),
                    strokeWidth = 2f
                )

                // Keyframe Diamonds / Dots (●───●───●)
                keyframes.forEach { kf ->
                    val kfX = kf * frameWidth
                    drawCircle(
                        color = StudioYellow,
                        radius = 4.5f,
                        center = Offset(kfX, trackY)
                    )
                    drawCircle(
                        color = EngineSurface,
                        radius = 2.5f,
                        center = Offset(kfX, trackY)
                    )
                }

                // Playhead Scrubber Cursor (Red Line)
                val cursorX = currentFrame * frameWidth
                drawLine(
                    color = Color(0xFFEF4444),
                    start = Offset(cursorX, 0f),
                    end = Offset(cursorX, trackHeight),
                    strokeWidth = 2f
                )

                // Top Playhead Handle
                drawRect(
                    color = Color(0xFFEF4444),
                    topLeft = Offset(cursorX - 5f, 0f),
                    size = Size(10f, 10f)
                )
            }
        }
    }
}

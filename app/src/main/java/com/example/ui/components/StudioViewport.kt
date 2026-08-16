package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Undo
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.SceneNode
import com.example.ui.theme.EngineBackground
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.StudioBlue
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioPurple
import com.example.ui.theme.StudioPurpleDark
import com.example.ui.theme.StudioPurpleGlass
import com.example.ui.theme.StudioPurpleLight
import com.example.ui.theme.StudioRed
import com.example.ui.theme.TextMuted

@Composable
fun StudioViewport(
    selectedNode: SceneNode?,
    onNodeDrag: (dx: Float, dy: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTool by remember { mutableStateOf(1) } // 0: Pan, 1: Select, 2: Move, 3: Rotate, 4: Scale, 5: Grid, 6: Undo

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EngineBackground)
    ) {
        // ========================================================
        // 1. Top Ruler
        // ========================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .background(EngineCardBg)
                .border(0.5.dp, StudioBorder)
                .padding(start = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("-640", "-512", "-384", "-256", "-128", "0", "128", "256", "384", "512", "640", "768", "896").forEach { mark ->
                Text(
                    text = mark,
                    color = TextMuted,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // ========================================================
        // 2. Left Ruler
        // ========================================================
        Column(
            modifier = Modifier
                .padding(top = 18.dp)
                .width(22.dp)
                .fillMaxHeight()
                .background(EngineCardBg)
                .border(0.5.dp, StudioBorder)
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            listOf("-384", "-256", "-128", "0", "128", "256", "384", "512").forEach { mark ->
                Text(
                    text = mark,
                    color = TextMuted,
                    fontSize = 7.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // ========================================================
        // 3. Central Canvas Viewport Area
        // ========================================================
        BoxWithConstraints(
            modifier = Modifier
                .padding(start = 22.dp, top = 18.dp)
                .fillMaxSize()
        ) {
            val canvasW = constraints.maxWidth.toFloat()
            val canvasH = constraints.maxHeight.toFloat()
            val centerX = canvasW / 2f
            val centerY = canvasH / 2f

            // Village Scene Artwork
            Image(
                painter = painterResource(id = R.drawable.img_dark_village),
                contentDescription = "Dark Village Scene Viewport",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gizmo and Camera Frame Overlay Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onNodeDrag(dragAmount.x, dragAmount.y)
                        }
                    }
                    .testTag("viewport_interactive_canvas")
            ) {
                // Purple Camera Frame
                val frameW = canvasW * 0.45f
                val frameH = canvasH * 0.55f
                val frameLeft = centerX - frameW / 2f
                val frameTop = centerY - frameH / 2f

                drawRect(
                    color = StudioPurple.copy(alpha = 0.55f),
                    topLeft = Offset(frameLeft, frameTop),
                    size = Size(frameW, frameH),
                    style = Stroke(width = 1.2f)
                )

                // Selected Object Gizmo
                selectedNode?.let { node ->
                    val objX = centerX + node.posX
                    val objY = centerY + node.posY
                    val boxSize = 36f * node.scale

                    // Bounding Box
                    drawRect(
                        color = StudioPurpleLight,
                        topLeft = Offset(objX - boxSize / 2f, objY - boxSize / 2f),
                        size = Size(boxSize, boxSize),
                        style = Stroke(width = 1.2f)
                    )

                    // 4 Anchor Points
                    val cornerRadius = 3f
                    drawCircle(color = StudioBlue, radius = cornerRadius, center = Offset(objX - boxSize / 2f, objY - boxSize / 2f))
                    drawCircle(color = StudioBlue, radius = cornerRadius, center = Offset(objX + boxSize / 2f, objY - boxSize / 2f))
                    drawCircle(color = StudioBlue, radius = cornerRadius, center = Offset(objX - boxSize / 2f, objY + boxSize / 2f))
                    drawCircle(color = StudioBlue, radius = cornerRadius, center = Offset(objX + boxSize / 2f, objY + boxSize / 2f))

                    // Green Up Arrow (Y-Axis Gizmo)
                    val arrowYLen = 42f
                    drawLine(
                        color = StudioGreen,
                        start = Offset(objX, objY - boxSize / 2f),
                        end = Offset(objX, objY - boxSize / 2f - arrowYLen),
                        strokeWidth = 2f
                    )
                    // Green Arrowhead
                    val pathY = Path().apply {
                        moveTo(objX, objY - boxSize / 2f - arrowYLen - 6f)
                        lineTo(objX - 4f, objY - boxSize / 2f - arrowYLen)
                        lineTo(objX + 4f, objY - boxSize / 2f - arrowYLen)
                        close()
                    }
                    drawPath(pathY, color = StudioGreen)

                    // Red Right Arrow (X-Axis Gizmo)
                    val arrowXLen = 42f
                    drawLine(
                        color = StudioRed,
                        start = Offset(objX + boxSize / 2f, objY),
                        end = Offset(objX + boxSize / 2f + arrowXLen, objY),
                        strokeWidth = 2f
                    )
                    // Red Arrowhead
                    val pathX = Path().apply {
                        moveTo(objX + boxSize / 2f + arrowXLen + 6f, objY)
                        lineTo(objX + boxSize / 2f + arrowXLen, objY - 4f)
                        lineTo(objX + boxSize / 2f + arrowXLen, objY + 4f)
                        close()
                    }
                    drawPath(pathX, color = StudioRed)
                }
            }

            // Player character sprite overlay over gizmo
            selectedNode?.let { node ->
                val objX = centerX + node.posX
                val objY = centerY + node.posY
                val spritePx = (32 * node.scale).dp

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (objX - (16 * node.scale)).toInt(),
                                (objY - (16 * node.scale)).toInt()
                            )
                        }
                        .size(spritePx)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_player_sprite),
                        contentDescription = "Player Sprite",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // ========================================================
            // 4. Floating Left Tools Palette (Pan, Select, Move, Rotate, Scale, Snap, Undo)
            // ========================================================
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(EngineCardBg.copy(alpha = 0.92f))
                    .border(0.8.dp, StudioBorder, RoundedCornerShape(8.dp))
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    Icons.Default.PanTool to "Pan",
                    Icons.Default.CropFree to "Select",
                    Icons.Default.OpenWith to "Move",
                    Icons.Default.Refresh to "Rotate",
                    Icons.Default.Transform to "Scale",
                    Icons.Default.GridOn to "Snap",
                    Icons.Default.Undo to "Reset"
                ).forEachIndexed { idx, (icon, desc) ->
                    val isSelected = activeTool == idx
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isSelected) StudioPurple else Color.Transparent
                            )
                            .clickable { activeTool = idx },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = desc,
                            tint = if (isSelected) Color.White else TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // ========================================================
            // 5. Viewport Bottom-Right Overlay Controls (Focus, Grid, Lock, Fullscreen)
            // ========================================================
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(EngineCardBg.copy(alpha = 0.92f))
                    .border(0.8.dp, StudioBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SelectAll,
                    contentDescription = "Focus",
                    tint = TextMuted,
                    modifier = Modifier.size(13.dp)
                )
                Icon(
                    imageVector = Icons.Default.CropFree,
                    contentDescription = "Frame",
                    tint = TextMuted,
                    modifier = Modifier.size(13.dp)
                )
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock",
                    tint = TextMuted,
                    modifier = Modifier.size(13.dp)
                )
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Fullscreen",
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

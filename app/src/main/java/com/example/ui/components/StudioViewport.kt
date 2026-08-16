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
        // 1. Top Ruler (Compact: 11dp)
        // ========================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(11.dp)
                .background(EngineCardBg)
                .border(0.4.dp, StudioBorder)
                .padding(start = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("-512", "-256", "-128", "0", "128", "256", "512", "768").forEach { mark ->
                Text(
                    text = mark,
                    color = TextMuted,
                    fontSize = 6.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // ========================================================
        // 2. Left Ruler (Compact: 14dp)
        // ========================================================
        Column(
            modifier = Modifier
                .padding(top = 11.dp)
                .width(14.dp)
                .fillMaxHeight()
                .background(EngineCardBg)
                .border(0.4.dp, StudioBorder)
                .padding(vertical = 2.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            listOf("-256", "-128", "0", "128", "256", "384").forEach { mark ->
                Text(
                    text = mark,
                    color = TextMuted,
                    fontSize = 6.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // ========================================================
        // 3. Central Canvas Viewport Area
        // ========================================================
        BoxWithConstraints(
            modifier = Modifier
                .padding(start = 14.dp, top = 11.dp)
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
            ) {
                // Subtle Grid
                val gridSize = 24.dp.toPx()
                var gx = 0f
                while (gx < size.width) {
                    drawLine(
                        color = Color(0x15FFFFFF),
                        start = Offset(gx, 0f),
                        end = Offset(gx, size.height),
                        strokeWidth = 0.5f
                    )
                    gx += gridSize
                }
                var gy = 0f
                while (gy < size.height) {
                    drawLine(
                        color = Color(0x15FFFFFF),
                        start = Offset(0f, gy),
                        end = Offset(size.width, gy),
                        strokeWidth = 0.5f
                    )
                    gy += gridSize
                }

                // Origin crosshair (0,0)
                drawLine(
                    color = StudioPurpleLight.copy(alpha = 0.4f),
                    start = Offset(centerX - 15f, centerY),
                    end = Offset(centerX + 15f, centerY),
                    strokeWidth = 1f
                )
                drawLine(
                    color = StudioPurpleLight.copy(alpha = 0.4f),
                    start = Offset(centerX, centerY - 15f),
                    end = Offset(centerX, centerY + 15f),
                    strokeWidth = 1f
                )

                // Selected Object Transform Box & Gizmo
                selectedNode?.let { node ->
                    val objX = centerX + node.posX
                    val objY = centerY + node.posY
                    val boxW = 40.dp.toPx() * node.scale
                    val boxH = 40.dp.toPx() * node.scale

                    // Bounding Box
                    drawRect(
                        color = StudioPurpleLight,
                        topLeft = Offset(objX - boxW / 2, objY - boxH / 2),
                        size = Size(boxW, boxH),
                        style = Stroke(
                            width = 1.2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 3f), 0f)
                        )
                    )

                    // Gizmo X Axis (Red)
                    val arrowLen = 32.dp.toPx()
                    drawLine(
                        color = StudioRed,
                        start = Offset(objX, objY),
                        end = Offset(objX + arrowLen, objY),
                        strokeWidth = 2f
                    )
                    drawPath(
                        path = Path().apply {
                            moveTo(objX + arrowLen + 6f, objY)
                            lineTo(objX + arrowLen - 2f, objY - 4f)
                            lineTo(objX + arrowLen - 2f, objY + 4f)
                            close()
                        },
                        color = StudioRed
                    )

                    // Gizmo Y Axis (Green)
                    drawLine(
                        color = StudioGreen,
                        start = Offset(objX, objY),
                        end = Offset(objX, objY - arrowLen),
                        strokeWidth = 2f
                    )
                    drawPath(
                        path = Path().apply {
                            moveTo(objX, objY - arrowLen - 6f)
                            lineTo(objX - 4f, objY - arrowLen + 2f)
                            lineTo(objX + 4f, objY - arrowLen + 2f)
                            close()
                        },
                        color = StudioGreen
                    )

                    // Center Pivot Dot
                    drawCircle(
                        color = Color.White,
                        radius = 2.5f,
                        center = Offset(objX, objY)
                    )
                }

                // Camera Frame (Dashed Blue Rect)
                val camW = size.width * 0.72f
                val camH = size.height * 0.65f
                drawRect(
                    color = StudioBlue.copy(alpha = 0.5f),
                    topLeft = Offset((size.width - camW) / 2f, (size.height - camH) / 2f),
                    size = Size(camW, camH),
                    style = Stroke(
                        width = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                    )
                )
            }

            // Player Sprite (Placed directly at selected node position)
            selectedNode?.let { node ->
                val objX = centerX + node.posX
                val objY = centerY + node.posY
                val spritePx = (24 * node.scale).dp

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (objX - (12 * node.scale)).toInt(),
                                (objY - (12 * node.scale)).toInt()
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
            // 4. Floating Left Tools Palette (Compact: 18dp icons)
            // ========================================================
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(EngineCardBg.copy(alpha = 0.94f))
                    .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                    .padding(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
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
                            .size(18.dp)
                            .clip(RoundedCornerShape(3.dp))
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
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            // ========================================================
            // 5. Viewport Bottom-Right Overlay Controls (Compact)
            // ========================================================
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(EngineCardBg.copy(alpha = 0.94f))
                    .border(0.5.dp, StudioBorder, RoundedCornerShape(3.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SelectAll,
                    contentDescription = "Focus",
                    tint = TextMuted,
                    modifier = Modifier.size(10.dp)
                )
                Icon(
                    imageVector = Icons.Default.CropFree,
                    contentDescription = "Frame",
                    tint = TextMuted,
                    modifier = Modifier.size(10.dp)
                )
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock",
                    tint = TextMuted,
                    modifier = Modifier.size(10.dp)
                )
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Fullscreen",
                    tint = TextMuted,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

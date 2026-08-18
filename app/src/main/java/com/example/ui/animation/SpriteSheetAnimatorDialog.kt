package com.example.ui.animation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.window.Dialog
import com.example.engine.animation.SpriteSheetConfig
import com.example.ui.theme.*

@Composable
fun SpriteSheetAnimatorDialog(
    onDismiss: () -> Unit,
    onCreateSpriteAnimation: (SpriteSheetConfig) -> Unit
) {
    var rows by remember { mutableIntStateOf(4) }
    var cols by remember { mutableIntStateOf(4) }
    var startFrame by remember { mutableIntStateOf(0) }
    var endFrame by remember { mutableIntStateOf(7) }
    var fps by remember { mutableIntStateOf(12) }
    var selectedTexture by remember { mutableStateOf("hero_warrior_sheet.png") }

    val totalFrames = rows * cols

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .width(440.dp)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = EngineSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioPurpleBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GridOn, contentDescription = null, tint = StudioGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مُولّد حركة الصور (Sprite Sheet Animator)", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted, modifier = Modifier.size(13.dp))
                    }
                }

                HorizontalDivider(color = StudioBorder)

                // Grid Slicing Controls (Rows, Columns, FPS)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Texture selector
                    Column(modifier = Modifier.weight(1.5f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("الصورة (Texture):", color = TextMuted, fontSize = 8.5.sp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(EngineBackground)
                                .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(selectedTexture, color = StudioGreen, fontSize = 9.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
                        }
                    }

                    // Columns
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("الأعمدة (Cols):", color = TextMuted, fontSize = 8.5.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { cols = (cols - 1).coerceAtLeast(1) }, modifier = Modifier.size(22.dp)) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(10.dp))
                            }
                            Text("$cols", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { cols = (cols + 1).coerceAtMost(16) }, modifier = Modifier.size(22.dp)) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(10.dp))
                            }
                        }
                    }

                    // Rows
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("الصفوف (Rows):", color = TextMuted, fontSize = 8.5.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { rows = (rows - 1).coerceAtLeast(1) }, modifier = Modifier.size(22.dp)) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(10.dp))
                            }
                            Text("$rows", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { rows = (rows + 1).coerceAtMost(16) }, modifier = Modifier.size(22.dp)) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(10.dp))
                            }
                        }
                    }

                    // FPS
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("السرعة (FPS):", color = TextMuted, fontSize = 8.5.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { fps = (fps - 2).coerceAtLeast(1) }, modifier = Modifier.size(22.dp)) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(10.dp))
                            }
                            Text("$fps", color = StudioBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { fps = (fps + 2).coerceAtMost(60) }, modifier = Modifier.size(22.dp)) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(10.dp))
                            }
                        }
                    }
                }

                // Interactive Grid Slicer Preview (Select Start / End Frames)
                Text(
                    text = "حدد الإطارات المراد تضمينها (من $startFrame إلى $endFrame):",
                    color = StudioPurpleLight,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(EngineBackground)
                        .border(0.8.dp, StudioBorder, RoundedCornerShape(6.dp))
                        .padding(6.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(cols),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        items((0 until totalFrames).toList()) { index ->
                            val isIncluded = index in startFrame..endFrame
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isIncluded) StudioGreen.copy(alpha = 0.25f) else EngineCardBg)
                                    .border(
                                        0.8.dp,
                                        if (isIncluded) StudioGreen else StudioBorder.copy(alpha = 0.4f),
                                        RoundedCornerShape(3.dp)
                                    )
                                    .clickable {
                                        if (index < startFrame) {
                                            startFrame = index
                                        } else {
                                            endFrame = index
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Image,
                                        contentDescription = null,
                                        tint = if (isIncluded) StudioGreen else TextMuted.copy(alpha = 0.5f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "$index",
                                        color = if (isIncluded) Color.White else TextMuted,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                // Action Buttons (Cancel, Generate Track & Clip)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = EngineCardBg),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f).height(32.dp)
                    ) {
                        Text("إلغاء", color = TextSecondary, fontSize = 10.sp)
                    }

                    Button(
                        onClick = {
                            onCreateSpriteAnimation(
                                SpriteSheetConfig(
                                    textureName = selectedTexture,
                                    rows = rows,
                                    columns = cols,
                                    startFrame = startFrame,
                                    endFrame = endFrame,
                                    fps = fps
                                )
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioGreen),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1.5f).height(32.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("توليد مقطع Sprite تلقائياً", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

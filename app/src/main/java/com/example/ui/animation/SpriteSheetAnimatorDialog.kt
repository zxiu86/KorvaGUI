package com.example.ui.animation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.KorvaDialog
import com.example.ui.components.KorvaOutlinedButton
import com.example.ui.components.KorvaPrimaryButton
import com.example.ui.theme.EngineBackground
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.KorvaBlue
import com.example.ui.theme.KorvaGreen
import com.example.ui.theme.KorvaPurpleLight
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class SpriteSheetConfig(
    val textureName: String,
    val rows: Int,
    val columns: Int,
    val startFrame: Int,
    val endFrame: Int,
    val fps: Int
)

@Composable
fun SpriteSheetAnimatorDialog(
    availableTextures: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onCreateSpriteAnimation: (SpriteSheetConfig) -> Unit
) {
    var selectedTexture by remember {
        mutableStateOf(availableTextures.firstOrNull() ?: "hero_spritesheet.png")
    }
    var rows by remember { mutableIntStateOf(4) }
    var cols by remember { mutableIntStateOf(6) }
    var fps by remember { mutableIntStateOf(12) }
    var startFrame by remember { mutableIntStateOf(0) }
    var endFrame by remember { mutableIntStateOf(5) }

    val totalFrames = rows * cols

    KorvaDialog(
        onDismissRequest = onDismiss,
        title = "مولد حركات الـ Sprite Sheet",
        subtitle = "تقطيع الصور المتحركة وتوليد مفاتيح الأنيميشن آلياً",
        icon = Icons.Default.GridOn,
        iconTint = KorvaGreen,
        maxWidth = 440.dp,
        buttons = {
            KorvaOutlinedButton(
                text = "إلغاء",
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )

            KorvaPrimaryButton(
                text = "توليد الأنيميشن",
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
                icon = Icons.Default.AutoFixHigh,
                modifier = Modifier.weight(1.3f)
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Texture Selection Row
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("الصورة أو الـ Atlas:", color = TextSecondary, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val textures = if (availableTextures.isNotEmpty()) availableTextures else listOf("hero_spritesheet.png", "monsters_sheet.png", "fx_fire.png")
                    textures.take(3).forEach { tex ->
                        val isChosen = selectedTexture == tex
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isChosen) KorvaGreen.copy(alpha = 0.2f) else EngineCardBg)
                                .border(0.8.dp, if (isChosen) KorvaGreen else StudioBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedTexture = tex }
                                .padding(horizontal = 6.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = if (isChosen) KorvaGreen else TextMuted, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(tex, color = if (isChosen) Color.White else TextMuted, fontSize = 9.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }

            // Grid Dimensions (Cols, Rows, FPS)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(EngineCardBg)
                    .border(0.8.dp, StudioBorder, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Columns
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("الأعمدة (Cols)", color = TextMuted, fontSize = 9.5.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { cols = (cols - 1).coerceAtLeast(1) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                        }
                        Text("$cols", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { cols = (cols + 1).coerceAtMost(16) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                        }
                    }
                }

                // Rows
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("الصفوف (Rows)", color = TextMuted, fontSize = 9.5.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { rows = (rows - 1).coerceAtLeast(1) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                        }
                        Text("$rows", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { rows = (rows + 1).coerceAtMost(16) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                        }
                    }
                }

                // FPS
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("السرعة (FPS)", color = TextMuted, fontSize = 9.5.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { fps = (fps - 2).coerceAtLeast(1) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                        }
                        Text("$fps", color = KorvaBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { fps = (fps + 2).coerceAtMost(60) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                        }
                    }
                }
            }

            // Slicer Preview Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("حدد نطاق الإطارات (من $startFrame إلى $endFrame):", color = KorvaPurpleLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("المجموع: ${endFrame - startFrame + 1} إطار", color = TextMuted, fontSize = 9.5.sp)
            }

            // Interactive Grid Slicer Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(EngineBackground)
                    .border(0.8.dp, StudioBorder, RoundedCornerShape(8.dp))
                    .padding(6.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(cols),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    items((0 until totalFrames).toList()) { index ->
                        val isIncluded = index in startFrame..endFrame
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isIncluded) KorvaGreen.copy(alpha = 0.25f) else EngineCardBg)
                                .border(
                                    0.8.dp,
                                    if (isIncluded) KorvaGreen else StudioBorder.copy(alpha = 0.4f),
                                    RoundedCornerShape(4.dp)
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
                                    tint = if (isIncluded) KorvaGreen else TextMuted.copy(alpha = 0.5f),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "$index",
                                    color = if (isIncluded) Color.White else TextMuted,
                                    fontSize = 7.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

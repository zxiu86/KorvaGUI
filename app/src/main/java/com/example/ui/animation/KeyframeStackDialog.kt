package com.example.ui.animation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.engine.animation.KeyframeData
import com.example.engine.animation.TrackData
import com.example.ui.theme.*

@Composable
fun KeyframeStackDialog(
    frame: Int,
    stackedItems: List<Pair<TrackData, KeyframeData>>,
    onDismiss: () -> Unit,
    onSelectKeyframe: (TrackData, KeyframeData) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.width(300.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = EngineSurface),
            border = BorderStroke(1.dp, StudioPurpleBorder)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Layers, contentDescription = null, tint = StudioYellow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مفاتيح متعددة في الإطار $frame (${stackedItems.size})", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted, modifier = Modifier.size(14.dp))
                    }
                }

                Text("اختر الإطار المفتاحي للتعديل أو التحكم المباشر:", color = TextSecondary, fontSize = 8.5.sp)

                HorizontalDivider(color = StudioBorder)

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(stackedItems) { (track, kf) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(EngineCardBg)
                                .border(0.5.dp, StudioBorder, RoundedCornerShape(6.dp))
                                .clickable {
                                    onSelectKeyframe(track, kf)
                                    onDismiss()
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(track.displayColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(track.name, color = TextPrimary, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                    Text("نوع المنحنى: ${kf.interpolation.label}", color = TextMuted, fontSize = 8.sp)
                                }
                            }

                            Text(
                                text = "Val: ${String.format(java.util.Locale.US, "%.1f", kf.value)}",
                                color = StudioPurpleLight,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

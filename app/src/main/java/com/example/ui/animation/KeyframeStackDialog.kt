package com.example.ui.animation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.animation.KeyframeData
import com.example.engine.animation.TrackData
import com.example.ui.components.KorvaDialog
import com.example.ui.components.KorvaOutlinedButton
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.KorvaPurpleLight
import com.example.ui.theme.KorvaYellow
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun KeyframeStackDialog(
    frame: Int,
    stackedItems: List<Pair<TrackData, KeyframeData>>,
    onDismiss: () -> Unit,
    onSelectKeyframe: (TrackData, KeyframeData) -> Unit
) {
    KorvaDialog(
        onDismissRequest = onDismiss,
        title = "مفاتيح متعددة في الإطار $frame",
        subtitle = "تم العثور على ${stackedItems.size} مفاتيح متزامنة",
        icon = Icons.Default.Layers,
        iconTint = KorvaYellow,
        maxWidth = 380.dp,
        buttons = {
            KorvaOutlinedButton(
                text = "إلغاء",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("اختر الإطار المفتاحي للتعديل أو التحكم المباشر:", color = TextSecondary, fontSize = 10.5.sp)

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(stackedItems) { (track, kf) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(EngineCardBg)
                            .border(0.8.dp, StudioBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                onSelectKeyframe(track, kf)
                                onDismiss()
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(track.displayColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(track.name, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("نوع المنحنى: ${kf.interpolation.label}", color = TextMuted, fontSize = 9.sp)
                            }
                        }

                        Text(
                            text = "Val: ${String.format(java.util.Locale.US, "%.1f", kf.value)}",
                            color = KorvaPurpleLight,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

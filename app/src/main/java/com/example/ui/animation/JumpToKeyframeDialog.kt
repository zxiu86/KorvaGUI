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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.engine.animation.ClipData
import com.example.ui.components.KorvaDialog
import com.example.ui.components.KorvaOutlinedButton
import com.example.ui.theme.EngineBackground
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.KorvaPurple
import com.example.ui.theme.KorvaPurpleLight
import com.example.ui.theme.KorvaYellow
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class TimelineJumpTarget(
    val frame: Int,
    val title: String,
    val subtitle: String,
    val tagColor: Color,
    val type: String
)

@Composable
fun JumpToKeyframeDialog(
    clip: ClipData,
    onDismiss: () -> Unit,
    onJumpToFrame: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("ALL") }

    val allTargets = remember(clip) {
        val list = mutableListOf<TimelineJumpTarget>()

        // 1. Keyframes from tracks
        clip.tracks.forEach { track ->
            track.keyframes.forEach { kf ->
                list.add(
                    TimelineJumpTarget(
                        frame = kf.frame,
                        title = "${track.name} (Val: ${String.format(java.util.Locale.US, "%.1f", kf.value)})",
                        subtitle = "مسار: ${track.category.label} | ${kf.interpolation.label}",
                        tagColor = track.displayColor,
                        type = "KEYFRAME"
                    )
                )
            }
        }

        // 2. Events
        clip.events.forEach { ev ->
            list.add(
                TimelineJumpTarget(
                    frame = ev.frame,
                    title = "حدث: ${ev.name} (${ev.functionName})",
                    subtitle = "معاملات: ${ev.parameters}",
                    tagColor = KorvaYellow,
                    type = "EVENT"
                )
            )
        }

        // 3. Markers
        clip.markers.forEach { mk ->
            list.add(
                TimelineJumpTarget(
                    frame = mk.frame,
                    title = "علامة: ${mk.label}",
                    subtitle = "علامة مرجعية في الإطار ${mk.frame}",
                    tagColor = Color(mk.colorHex),
                    type = "MARKER"
                )
            )
        }

        list.sortedBy { it.frame }
    }

    val filteredTargets = remember(allTargets, searchQuery, filterType) {
        allTargets.filter { target ->
            val matchesFilter = when (filterType) {
                "KEYFRAME" -> target.type == "KEYFRAME"
                "EVENT" -> target.type == "EVENT"
                "MARKER" -> target.type == "MARKER"
                else -> true
            }
            val matchesQuery = searchQuery.isBlank() ||
                target.title.contains(searchQuery, ignoreCase = true) ||
                target.subtitle.contains(searchQuery, ignoreCase = true) ||
                target.frame.toString().contains(searchQuery)
            matchesFilter && matchesQuery
        }
    }

    KorvaDialog(
        onDismissRequest = onDismiss,
        title = "الانتقال السريع للإطارات",
        subtitle = "بحث وتصفح المفاتيح والأحداث في التايم لاين",
        icon = Icons.Default.TravelExplore,
        maxWidth = 420.dp,
        buttons = {
            KorvaOutlinedButton(
                text = "إغلاق",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث باسم المسار، الإطار، أو الحدث...", fontSize = 10.sp, color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = KorvaPurpleLight, modifier = Modifier.size(16.dp)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KorvaPurpleLight,
                    unfocusedBorderColor = StudioBorder,
                    focusedContainerColor = EngineCardBg,
                    unfocusedContainerColor = EngineCardBg,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Category Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    "ALL" to "الكل (${allTargets.size})",
                    "KEYFRAME" to "مفاتيح",
                    "EVENT" to "أحداث",
                    "MARKER" to "علامات"
                ).forEach { (t, label) ->
                    val isSelected = filterType == t
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) KorvaPurple else EngineBackground)
                            .border(0.8.dp, if (isSelected) KorvaPurpleLight else StudioBorder, RoundedCornerShape(6.dp))
                            .clickable { filterType = t }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = 9.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }

            // Target Items List
            if (filteredTargets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد عناصر مطابقة", color = TextMuted, fontSize = 11.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredTargets) { target ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(EngineCardBg)
                                .border(0.8.dp, StudioBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    onJumpToFrame(target.frame)
                                    onDismiss()
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(target.tagColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(target.title, color = TextPrimary, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(target.subtitle, color = TextMuted, fontSize = 9.sp, maxLines = 1)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(KorvaPurple.copy(alpha = 0.25f))
                                    .border(0.6.dp, KorvaPurpleLight.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "F${target.frame}",
                                    color = KorvaPurpleLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
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

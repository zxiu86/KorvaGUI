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
import com.example.engine.animation.ClipData
import com.example.ui.theme.*

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
                    tagColor = StudioYellow,
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

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .width(320.dp)
                .heightIn(max = 420.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = EngineSurface),
            border = BorderStroke(1.dp, StudioPurpleBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TravelExplore, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("الانتقال السريع للإطارات (Jump)", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted, modifier = Modifier.size(14.dp))
                    }
                }

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("بحث باسم المسار، الإطار، أو الحدث...", fontSize = 9.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StudioPurpleLight,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
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
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) StudioPurple else EngineBackground)
                                .border(0.5.dp, if (isSelected) StudioPurpleLight else StudioBorder, RoundedCornerShape(4.dp))
                                .clickable { filterType = t }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, color = if (isSelected) Color.White else TextSecondary, fontSize = 8.sp, maxLines = 1)
                        }
                    }
                }

                HorizontalDivider(color = StudioBorder)

                // Target Items List
                if (filteredTargets.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("لا توجد عناصر مطابقة", color = TextMuted, fontSize = 10.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredTargets) { target ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EngineCardBg)
                                    .border(0.5.dp, StudioBorder, RoundedCornerShape(6.dp))
                                    .clickable {
                                        onJumpToFrame(target.frame)
                                        onDismiss()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(target.tagColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(target.title, color = TextPrimary, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text(target.subtitle, color = TextMuted, fontSize = 8.sp, maxLines = 1)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(StudioPurple.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "F${target.frame}",
                                        color = StudioPurpleLight,
                                        fontSize = 9.sp,
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
}

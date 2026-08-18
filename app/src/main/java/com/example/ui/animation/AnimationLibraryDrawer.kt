package com.example.ui.animation

import androidx.compose.animation.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.animation.ClipData
import com.example.ui.theme.*

@Composable
fun AnimationLibraryDrawer(
    clips: List<ClipData>,
    selectedClipId: String,
    onSelectClip: (String) -> Unit,
    onNewClip: () -> Unit,
    onDuplicateClip: (String) -> Unit,
    onRenameClip: (String) -> Unit,
    onDeleteClip: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterFavoritesOnly by remember { mutableStateOf(false) }

    val filteredClips = remember(clips, searchQuery, filterFavoritesOnly) {
        clips.filter { clip ->
            val matchesQuery = searchQuery.isBlank() || clip.name.contains(searchQuery, ignoreCase = true)
            val matchesFav = !filterFavoritesOnly || clip.isFavorite
            matchesQuery && matchesFav
        }
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(EngineSurface)
            .border(width = 0.8.dp, color = StudioBorder)
            .padding(6.dp)
    ) {
        // -------------------------------------------------------------
        // Header (Title, + New Clip, Close)
        // -------------------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Folder, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("مكتبة الأنيميشن", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Text("(${clips.size})", color = TextMuted, fontSize = 9.5.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Add New Clip Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StudioPurpleDark)
                        .border(0.5.dp, StudioPurpleLight, RoundedCornerShape(4.dp))
                        .clickable { onNewClip() }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("مقطع جديد", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                IconButton(onClick = onClose, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted, modifier = Modifier.size(12.dp))
                }
            }
        }

        // -------------------------------------------------------------
        // Search Bar + Favorites Filter Toggle
        // -------------------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Search Input
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(EngineBackground)
                    .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 9.5.sp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text("بحث بالاسم...", color = TextMuted, fontSize = 9.5.sp)
                            }
                            innerTextField()
                        }
                    )
                }
            }

            // Favorites Filter Toggle
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (filterFavoritesOnly) StudioPurpleBg else EngineCardBg)
                    .border(0.5.dp, if (filterFavoritesOnly) StudioYellow else StudioBorder, RoundedCornerShape(4.dp))
                    .clickable { filterFavoritesOnly = !filterFavoritesOnly },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (filterFavoritesOnly) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "المفضلة",
                    tint = if (filterFavoritesOnly) StudioYellow else TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // -------------------------------------------------------------
        // Clips Cards List
        // -------------------------------------------------------------
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filteredClips, key = { it.id }) { clip ->
                val isSelected = clip.id == selectedClipId
                ClipCardItem(
                    clip = clip,
                    isSelected = isSelected,
                    onSelect = { onSelectClip(clip.id) },
                    onDuplicate = { onDuplicateClip(clip.id) },
                    onRename = { onRenameClip(clip.id) },
                    onDelete = { onDeleteClip(clip.id) },
                    onToggleFavorite = { onToggleFavorite(clip.id) }
                )
            }
        }
    }
}

@Composable
private fun ClipCardItem(
    clip: ClipData,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDuplicate: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) StudioPurpleDark.copy(alpha = 0.65f) else EngineCardBg
        ),
        border = androidx.compose.foundation.BorderStroke(
            0.6.dp,
            if (isSelected) StudioPurpleLight else StudioBorder
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Name & Icon
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(clip.iconEmoji, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = clip.name,
                        color = if (isSelected) Color.White else TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Favorite Star & Context Menu
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(18.dp)) {
                        Icon(
                            imageVector = if (clip.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "مفضلة",
                            tint = if (clip.isFavorite) StudioYellow else TextMuted.copy(alpha = 0.5f),
                            modifier = Modifier.size(11.dp)
                        )
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(18.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = "خيارات", tint = TextMuted, modifier = Modifier.size(12.dp))
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(EngineSurface).border(0.8.dp, StudioBorder)
                        ) {
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = StudioBlue, modifier = Modifier.size(12.dp)) },
                                text = { Text("نسخ (Duplicate)", color = TextPrimary, fontSize = 9.5.sp) },
                                onClick = {
                                    showMenu = false
                                    onDuplicate()
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = StudioYellow, modifier = Modifier.size(12.dp)) },
                                text = { Text("إعادة تسمية (Rename)", color = TextPrimary, fontSize = 9.5.sp) },
                                onClick = {
                                    showMenu = false
                                    onRename()
                                }
                            )
                            DropdownMenuItem(
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = StudioRed, modifier = Modifier.size(12.dp)) },
                                text = { Text("حذف (Delete)", color = StudioRed, fontSize = 9.5.sp) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Metadata Chips (FPS, Frames, Duration Seconds)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // FPS
                Text(
                    text = "${clip.fps} FPS",
                    color = StudioBlue,
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text("•", color = TextMuted, fontSize = 8.sp)
                // Frames
                Text(
                    text = "${clip.durationFrames} فريم",
                    color = StudioGreen,
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text("•", color = TextMuted, fontSize = 8.sp)
                // Duration
                Text(
                    text = "${String.format("%.2f", clip.durationSeconds)}s",
                    color = StudioOrange,
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

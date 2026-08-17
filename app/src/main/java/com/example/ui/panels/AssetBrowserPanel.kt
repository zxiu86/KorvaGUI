package com.example.ui.panels

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.interfaces.AssetType
import com.example.engine.interfaces.IAsset
import com.example.ui.theme.*

@Composable
fun AssetBrowserPanel(
    assets: List<IAsset>,
    onSelectAsset: (asset: IAsset) -> Unit,
    onImportAsset: () -> Unit,
    onDeleteAsset: (assetId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<AssetType?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var previewAsset by remember { mutableStateOf<IAsset?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(EngineSurface)
            .border(width = 0.8.dp, color = StudioBorder)
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(EngineBackground)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Folder, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("مستعرض الملفات (Assets)", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Category Filter Chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AssetCategoryChip("الكل", isSelected = selectedCategory == null) { selectedCategory = null }
                    AssetCategoryChip("صور", isSelected = selectedCategory == AssetType.TEXTURE) { selectedCategory = AssetType.TEXTURE }
                    AssetCategoryChip("صوتيات", isSelected = selectedCategory == AssetType.AUDIO) { selectedCategory = AssetType.AUDIO }
                    AssetCategoryChip("سكربتات", isSelected = selectedCategory == AssetType.SCRIPT) { selectedCategory = AssetType.SCRIPT }
                }

                IconButton(onClick = onImportAsset, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Import", tint = StudioPurpleLight, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Assets Grid
        val filteredAssets = assets.filter {
            (selectedCategory == null || it.type == selectedCategory) &&
                    (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true))
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 110.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filteredAssets, key = { it.id }) { asset ->
                val icon: ImageVector = when (asset.type) {
                    AssetType.TEXTURE -> Icons.Default.Image
                    AssetType.AUDIO -> Icons.Default.VolumeUp
                    AssetType.FONT -> Icons.Default.TextFields
                    AssetType.ANIMATION -> Icons.Default.Movie
                    AssetType.SCRIPT -> Icons.Default.Code
                    AssetType.SCENE -> Icons.Default.Layers
                    AssetType.MATERIAL -> Icons.Default.Palette
                }

                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = EngineCardBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.6.dp, StudioBorder, RoundedCornerShape(8.dp))
                        .clickable { previewAsset = asset; onSelectAsset(asset) }
                ) {
                    Column(
                        modifier = Modifier.padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(StudioPurpleDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = asset.name,
                            color = TextPrimary,
                            fontSize = 10.sp,
                            maxLines = 1,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${asset.sizeBytes / 1024} KB",
                            color = TextMuted,
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }

    // Asset / Texture Preview Dialog (Specification 10)
    if (previewAsset != null) {
        val asset = previewAsset!!
        AlertDialog(
            onDismissRequest = { previewAsset = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(asset.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Preview Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioPurpleDark)
                            .border(1.dp, StudioPurpleLight.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (asset.type) {
                                AssetType.TEXTURE -> Icons.Default.Image
                                AssetType.AUDIO -> Icons.Default.VolumeUp
                                AssetType.SCRIPT -> Icons.Default.Code
                                else -> Icons.Default.Folder
                            },
                            contentDescription = null,
                            tint = StudioPurpleLight,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    // Metadata Spec Table
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EngineCardBg, RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        MetadataRow("Path", asset.relativePath)
                        MetadataRow("Size", "${asset.sizeBytes / (1024 * 1024f)} MB (${asset.sizeBytes / 1024} KB)")
                        asset.metadata.forEach { (k, v) ->
                            MetadataRow(k.replaceFirstChar { it.uppercase() }, v)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { previewAsset = null },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPurple)
                ) {
                    Text("إغلاق", color = Color.White)
                }
            },
            containerColor = EngineSurface
        )
    }
}

@Composable
private fun AssetCategoryChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) StudioPurple else EngineCardBg)
            .border(0.6.dp, if (isSelected) StudioPurpleLight else StudioBorder, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(title, color = if (isSelected) Color.White else TextMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, fontSize = 10.sp)
        Text(value, color = TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

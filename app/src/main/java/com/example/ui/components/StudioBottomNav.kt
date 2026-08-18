package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EngineSurface
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioPurple
import com.example.ui.theme.StudioPurpleLight
import com.example.ui.theme.TextMuted

enum class StudioGlobalTab {
    PROJECTS,
    ASSETS,
    EDITOR,
    ANIMATIONS,
    BUILD,
    SETTINGS
}

@Composable
fun StudioBottomNav(
    activeTab: StudioGlobalTab = StudioGlobalTab.EDITOR,
    onTabSelected: (StudioGlobalTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(22.dp)
            .background(EngineSurface)
            .border(width = 0.5.dp, color = StudioBorder)
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(
            Triple(StudioGlobalTab.PROJECTS, "Projects", Icons.Default.Folder),
            Triple(StudioGlobalTab.ASSETS, "Assets", Icons.Default.FolderSpecial),
            Triple(StudioGlobalTab.EDITOR, "Editor", Icons.Default.ViewInAr),
            Triple(StudioGlobalTab.ANIMATIONS, "Animations", Icons.Default.Movie),
            Triple(StudioGlobalTab.BUILD, "Build", Icons.Default.Build),
            Triple(StudioGlobalTab.SETTINGS, "Settings", Icons.Default.Settings)
        ).forEach { (tab, label, icon) ->
            val isSelected = activeTab == tab

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (isSelected) StudioPurple.copy(alpha = 0.35f) else Color.Transparent
                    )
                    .border(
                        width = if (isSelected) 0.5.dp else 0.dp,
                        color = if (isSelected) StudioPurpleLight else Color.Transparent,
                        shape = RoundedCornerShape(3.dp)
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .testTag("studio_tab_${tab.name.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) Color.White else TextMuted,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else TextMuted,
                        fontSize = 8.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

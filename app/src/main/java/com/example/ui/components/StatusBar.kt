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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EngineBorder
import com.example.ui.theme.EngineWhiteBorder
import com.example.ui.theme.EngineWhiteMuted
import com.example.ui.theme.EngineWhitePrimary
import com.example.ui.theme.EngineWhiteTranslucent
import com.example.ui.theme.StatusBarBg
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun KorvaStatusBar(
    defaultPath: String,
    onChangePathClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(StatusBarBg)
            .border(
                width = 0.6.dp,
                color = EngineBorder.copy(alpha = 0.5f)
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Save Path Indicator with translucent white icon & text
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f, fill = false)
                .clip(RoundedCornerShape(4.dp))
                .clickable { onChangePathClick() }
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .testTag("status_bar_path_button")
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = EngineWhiteTranslucent,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "مسار الحفظ الافتراضي:",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = defaultPath.ifBlank { "/storage/emulated/0/KorvaEngine/Projects" },
                color = EngineWhitePrimary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Left side in RTL: Engine stability & telemetry
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Engine Core Status
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(StatusGreen)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "النواة: جاهزة",
                    color = StatusGreen,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Shader Runtime Status
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = EngineWhiteMuted,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Vulkan / GLES 3.2",
                    color = TextMuted,
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Memory Status
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "RAM: 48 MB",
                    color = TextMuted,
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

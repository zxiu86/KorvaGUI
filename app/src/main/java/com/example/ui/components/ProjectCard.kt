package com.example.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectEntity
import com.example.ui.theme.EngineBorder
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.EngineCardHover
import com.example.ui.theme.KorvaAmber
import com.example.ui.theme.KorvaBlue
import com.example.ui.theme.KorvaCyan
import com.example.ui.theme.KorvaRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProjectCard(
    project: ProjectEntity,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatted = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault()).format(Date(project.lastModified))
    val accentColor = try {
        Color(android.graphics.Color.parseColor(project.colorHex))
    } catch (e: Exception) {
        KorvaCyan
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(EngineCardBg)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        accentColor.copy(alpha = 0.4f),
                        EngineBorder
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(14.dp)
            .testTag("project_card_${project.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left in RTL (Right info): Icon + Project Details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon Avatar
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.12f))
                        .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = project.name,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Template Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EngineBorder.copy(alpha = 0.5f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = project.templateType,
                                color = KorvaCyan,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Path text
                    Text(
                        text = project.path,
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Meta chips row (Date + Size)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = dateFormatted,
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = project.fileSize,
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${project.scenesCount} مشاهد • ${project.scriptsCount} برمجيات",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Open Project Quick Action Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(KorvaCyan.copy(alpha = 0.15f))
                        .border(1.dp, KorvaCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable { onClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("open_project_btn_${project.id}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "فتح المشروع",
                            tint = KorvaCyan,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "فتح",
                            color = KorvaCyan,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Delete Button
                IconButton(
                    onClick = { onDeleteClick() },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .testTag("delete_project_btn_${project.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "حذف المشروع",
                        tint = KorvaRed.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

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
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioPurple
import com.example.ui.theme.StudioPurpleDark
import com.example.ui.theme.StudioPurpleGlass
import com.example.ui.theme.StudioPurpleLight
import com.example.ui.theme.StudioRed
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
    val dateFormatted = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(project.lastModified))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(EngineCardBg)
            .border(
                width = 0.6.dp,
                color = StudioBorder,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(6.dp)
            .testTag("project_card_${project.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon + Project Details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon Avatar
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(StudioPurpleDark.copy(alpha = 0.6f))
                        .border(0.6.dp, StudioPurpleLight.copy(alpha = 0.4f), RoundedCornerShape(5.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = StudioPurpleLight,
                        modifier = Modifier.size(13.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = project.name,
                            color = TextPrimary,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        // Template Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(StudioPurpleGlass)
                                .border(0.5.dp, StudioPurpleLight.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = project.templateType,
                                color = StudioPurpleLight,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(1.dp))

                    // Path text
                    Text(
                        text = project.path,
                        color = TextMuted,
                        fontSize = 7.5.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Meta row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(8.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = dateFormatted,
                                color = TextSecondary,
                                fontSize = 7.5.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(8.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = project.fileSize,
                                color = TextSecondary,
                                fontSize = 7.5.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(8.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${project.scenesCount} scenes • ${project.scriptsCount} scripts",
                                color = TextSecondary,
                                fontSize = 7.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Open Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(StudioPurple)
                        .clickable { onClick() }
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                        .testTag("open_project_btn_${project.id}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "فتح المشروع",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "فتح",
                            color = Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Delete Button
                IconButton(
                    onClick = { onDeleteClick() },
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .testTag("delete_project_btn_${project.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "حذف المشروع",
                        tint = StudioRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

package com.example.ui.dialogs

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.EngineBorder
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.EngineSurface
import com.example.ui.theme.EngineSurfaceVariant
import com.example.ui.theme.KorvaBlue
import com.example.ui.theme.KorvaCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.io.File

@Composable
fun OpenProjectDialog(
    initialPath: String,
    onDismiss: () -> Unit,
    onProjectSelected: (folderPath: String) -> Unit
) {
    var currentPath by remember { mutableStateOf(initialPath) }
    var manualPathInput by remember { mutableStateOf(initialPath) }

    // Quick folders discovered or simulated in storage
    val discoveredFolders = remember(currentPath) {
        val root = File(currentPath)
        val list = mutableListOf<String>()
        if (root.exists() && root.isDirectory) {
            root.listFiles()?.filter { it.isDirectory }?.forEach { list.add(it.name) }
        }
        if (list.isEmpty()) {
            listOf("CyberRunner_Project", "SpaceInvaders_Korva", "Physics_Lab_v1", "NeonCity_3D")
        } else {
            list
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        Surface(
            modifier = Modifier
                .widthIn(min = 520.dp, max = 680.dp)
                .padding(16.dp)
                .shadow(24.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(EngineSurface)
                .border(
                    width = 1.2.dp,
                    brush = Brush.linearGradient(
                        listOf(KorvaBlue.copy(alpha = 0.6f), EngineBorder)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            color = EngineSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(KorvaBlue.copy(alpha = 0.15f))
                                .border(1.dp, KorvaBlue.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = KorvaBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "تحرير مشروع محفوظ (Open Project)",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "استعراض الذاكرة وفتح مجلدات ومشاريع Korva المحفوظة",
                                color = TextSecondary,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Current Path input
                OutlinedTextField(
                    value = manualPathInput,
                    onValueChange = {
                        manualPathInput = it
                        currentPath = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_project_path_input"),
                    label = { Text("المسار المستهدف", fontSize = 11.sp) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = KorvaBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KorvaBlue,
                        unfocusedBorderColor = EngineBorder,
                        focusedContainerColor = EngineCardBg,
                        unfocusedContainerColor = EngineCardBg,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "المجلدات المكتشفة في المسار:",
                    color = TextSecondary,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Folder list
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(EngineCardBg)
                        .border(1.dp, EngineBorder, RoundedCornerShape(10.dp))
                        .padding(6.dp)
                ) {
                    LazyColumn {
                        items(discoveredFolders) { folderName ->
                            val folderFullPath = "$currentPath/$folderName"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        manualPathInput = folderFullPath
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = KorvaBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = folderName,
                                        color = TextPrimary,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Text(
                                    text = "project.korva",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onDismiss() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "إلغاء",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            onProjectSelected(manualPathInput.ifBlank { initialPath })
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KorvaBlue,
                            contentColor = com.example.ui.theme.EngineBackground
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("confirm_open_project_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "فتح وتعديل المشروع",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

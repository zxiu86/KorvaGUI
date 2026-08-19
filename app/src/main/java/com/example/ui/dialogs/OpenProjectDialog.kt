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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.KorvaDialog
import com.example.ui.components.KorvaOutlinedButton
import com.example.ui.components.KorvaPrimaryButton
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioPurpleDark
import com.example.ui.theme.StudioPurpleLight
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
    val scrollState = rememberScrollState()

    val discoveredFolders = remember(currentPath) {
        val root = File(currentPath)
        val list = mutableListOf<String>()
        if (root.exists() && root.isDirectory) {
            root.listFiles()?.filter { it.isDirectory }?.forEach { list.add(it.name) }
        }
        if (list.isEmpty()) {
            listOf("CyberRunner_2D", "SpaceInvaders_Korva", "Physics_Lab_2D", "NeonCity_2D_Pixel")
        } else {
            list
        }
    }

    KorvaDialog(
        onDismissRequest = onDismiss,
        title = "تحرير مشروع محفوظ",
        subtitle = "استعراض الذاكرة وفتح مجلدات المشاريع",
        icon = Icons.Default.FolderOpen,
        maxWidth = 500.dp,
        buttons = {
            KorvaOutlinedButton(
                text = "إلغاء",
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )

            KorvaPrimaryButton(
                text = "فتح وتعديل المشروع",
                onClick = { onProjectSelected(manualPathInput.ifBlank { initialPath }) },
                icon = Icons.Default.PlayArrow,
                modifier = Modifier
                    .weight(1.2f)
                    .testTag("confirm_open_project_button")
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 280.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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
                        tint = StudioPurpleLight,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StudioPurpleLight,
                    unfocusedBorderColor = StudioBorder,
                    focusedContainerColor = EngineCardBg,
                    unfocusedContainerColor = EngineCardBg,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Text(
                text = "المشاريع المكتشفة في المسار:",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(EngineCardBg)
                    .border(0.8.dp, StudioBorder, RoundedCornerShape(8.dp))
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                discoveredFolders.forEach { folderName ->
                    val folderFullPath = "$currentPath/$folderName"
                    val isSelected = manualPathInput == folderFullPath

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) StudioPurpleDark.copy(alpha = 0.6f) else EngineCardBg)
                            .border(0.6.dp, if (isSelected) StudioPurpleLight else StudioBorder, RoundedCornerShape(6.dp))
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
                                tint = if (isSelected) StudioPurpleLight else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = folderName,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = "project.korva",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.KorvaDialog
import com.example.ui.components.KorvaOutlinedButton
import com.example.ui.components.KorvaPrimaryButton
import com.example.ui.theme.EngineBackground
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.KorvaBlue
import com.example.ui.theme.KorvaGreen
import com.example.ui.theme.KorvaPurple
import com.example.ui.theme.KorvaPurpleLight
import com.example.ui.theme.KorvaRed
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioPurpleDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class ProjectTemplate(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val defaultProjectName: String
)

@Composable
fun NewProjectDialog(
    defaultPath: String,
    onDismiss: () -> Unit,
    onCreateProject: (name: String, path: String, template: String) -> Unit,
    onChangePathRequested: () -> Unit
) {
    var projectName by remember { mutableStateOf("NewProject_01") }
    var selectedPath by remember(defaultPath) { mutableStateOf(defaultPath) }
    var selectedTemplateIndex by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val templatesScrollState = rememberScrollState()
    val settingsScrollState = rememberScrollState()

    val templates = remember {
        listOf(
            ProjectTemplate(
                title = "2D Platformer Engine",
                subtitle = "ألعاب المنصات والقفز",
                description = "محرك منصات 2D مع فيزياء وتصادمات وتجميع عملات",
                icon = Icons.Default.Gamepad,
                defaultProjectName = "Platformer_2D"
            ),
            ProjectTemplate(
                title = "2D Top-Down RPG",
                subtitle = "مغامرات وعوالم مفتوحة",
                description = "مشهد استكشاف من الأعلى مع تحكم بالبطل ونظام طبقات",
                icon = Icons.Default.Layers,
                defaultProjectName = "TopDown_RPG"
            ),
            ProjectTemplate(
                title = "2D Physics Sandbox",
                subtitle = "مختبر محاكاة الفيزياء",
                description = "بيئة محاكاة الجاذبية والأجسام الصلبة والارتدادات",
                icon = Icons.Default.Science,
                defaultProjectName = "Physics_Sandbox"
            ),
            ProjectTemplate(
                title = "2D Pixel Art World",
                subtitle = "عوالم البكسل والمؤثرات",
                description = "مشهد بكسل آرت مع إضاءة ديناميكية ونظام جزيئات",
                icon = Icons.Default.AutoFixHigh,
                defaultProjectName = "Pixel_World_2D"
            )
        )
    }

    fun validateAndSubmit() {
        val trimmed = projectName.trim()
        when {
            trimmed.isBlank() -> {
                errorMessage = "يرجى إدخال اسم المشروع أولاً"
            }
            trimmed.contains(Regex("[/\\\\:*?\"<>|]")) -> {
                errorMessage = "اسم المشروع يحتوي على رموز غير صالحة (/ \\ : * ? \" < > |)"
            }
            else -> {
                errorMessage = null
                onCreateProject(
                    trimmed,
                    selectedPath.ifBlank { defaultPath },
                    templates[selectedTemplateIndex].title
                )
            }
        }
    }

    KorvaDialog(
        onDismissRequest = onDismiss,
        title = "إنشاء مشروع جديد (New Project)",
        subtitle = "تأسيس بنية الملفات واختيار قالب ومسار العمل",
        icon = Icons.Default.CreateNewFolder,
        iconTint = KorvaPurpleLight,
        maxWidth = 600.dp,
        buttons = {
            KorvaOutlinedButton(
                text = "إلغاء",
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .testTag("cancel_new_project_button")
            )

            KorvaPrimaryButton(
                text = "إنشاء المشروع والبدء",
                onClick = { validateAndSubmit() },
                icon = Icons.Default.PlayArrow,
                modifier = Modifier
                    .weight(1.4f)
                    .testTag("save_and_create_button")
            )
        }
    ) {
        // Two-Column Landscape Layout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // -------------------------------------------------------------
            // Left Column: Templates List
            // -------------------------------------------------------------
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = "قالب المشروع (Templates)",
                    color = TextPrimary,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(templatesScrollState),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    templates.forEachIndexed { index, template ->
                        val isSelected = selectedTemplateIndex == index
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) StudioPurpleDark.copy(alpha = 0.5f) else EngineCardBg)
                                .border(
                                    width = if (isSelected) 1.2.dp else 0.6.dp,
                                    color = if (isSelected) KorvaPurpleLight else StudioBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedTemplateIndex = index
                                    if (projectName.isBlank() || projectName.startsWith("NewProject_") || templates.any { it.defaultProjectName == projectName }) {
                                        projectName = template.defaultProjectName
                                        errorMessage = null
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(if (isSelected) KorvaPurple else EngineBackground)
                                        .border(0.6.dp, if (isSelected) KorvaPurpleLight else StudioBorder, RoundedCornerShape(5.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = template.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else KorvaBlue,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = template.title,
                                        color = if (isSelected) Color.White else TextPrimary,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = template.subtitle,
                                        color = if (isSelected) KorvaPurpleLight else TextSecondary,
                                        fontSize = 8.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "تم الاختيار",
                                        tint = KorvaPurpleLight,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // Right Column: Project Settings
            // -------------------------------------------------------------
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .verticalScroll(settingsScrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Project Name Field
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "اسم المشروع (Project Name) *",
                        color = TextPrimary,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = projectName,
                        onValueChange = {
                            projectName = it
                            if (it.isNotBlank()) errorMessage = null
                        },
                        placeholder = {
                            Text("أدخل اسم المشروع (مثال: CyberRunner_2D)", color = TextMuted, fontSize = 10.sp)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("project_name_input"),
                        singleLine = true,
                        isError = errorMessage != null,
                        supportingText = if (errorMessage != null) {
                            {
                                Text(text = errorMessage ?: "", color = KorvaRed, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                            }
                        } else null,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = if (errorMessage != null) KorvaRed else KorvaPurpleLight,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        trailingIcon = {
                            if (projectName.isNotEmpty()) {
                                IconButton(
                                    onClick = { projectName = "" },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "مسح",
                                        tint = TextMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KorvaPurpleLight,
                            unfocusedBorderColor = StudioBorder,
                            focusedContainerColor = EngineCardBg,
                            unfocusedContainerColor = EngineCardBg,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = KorvaPurpleLight,
                            errorBorderColor = KorvaRed
                        ),
                        shape = RoundedCornerShape(6.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { validateAndSubmit() })
                    )

                    // Quick name preset chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("MyGame_01", "Runner_2D", "RetroQuest", "Arena_2D").forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EngineBackground)
                                    .border(0.6.dp, StudioBorder, RoundedCornerShape(4.dp))
                                    .clickable {
                                        projectName = preset
                                        errorMessage = null
                                    }
                                    .padding(vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = preset,
                                    color = TextSecondary,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // 2. Storage Path Field
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "مسار الحفظ (Save Location)",
                        color = TextPrimary,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(EngineCardBg)
                            .border(0.6.dp, StudioBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = KorvaPurpleLight,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = selectedPath.ifBlank { defaultPath },
                                        color = TextSecondary,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val previewName = projectName.trim().ifBlank { "Project" }
                                    Text(
                                        text = "↳ $previewName/",
                                        color = KorvaGreen,
                                        fontSize = 8.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(StudioPurpleDark)
                                    .border(0.6.dp, KorvaPurpleLight, RoundedCornerShape(4.dp))
                                    .clickable { onChangePathRequested() }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                    .testTag("change_path_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "تغيير",
                                        color = Color.White,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
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

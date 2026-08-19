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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Science
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
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioPurple
import com.example.ui.theme.StudioPurpleDark
import com.example.ui.theme.StudioPurpleLight
import com.example.ui.theme.StudioRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class ProjectTemplate(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
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
    var selectedTemplateIndex by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    val templates = remember {
        listOf(
            ProjectTemplate(
                title = "2D Platformer Engine",
                description = "محرك منصات ثنائي الأبعاد مع دعم فيزياء وتصادمات وتجميع العملات",
                icon = Icons.Default.Gamepad,
                color = StudioPurple
            ),
            ProjectTemplate(
                title = "2D Top-Down RPG",
                description = "مشهد مغامرة واستكشاف 2D من الأعلى مع تحكم بالبطل ونظام خرائط",
                icon = Icons.Default.Layers,
                color = StudioPurpleLight
            ),
            ProjectTemplate(
                title = "2D Physics Sandbox",
                description = "مختبر محاكاة الأجسام والجاذبية والارتدادات ثنائية الأبعاد",
                icon = Icons.Default.Science,
                color = StudioPurple
            ),
            ProjectTemplate(
                title = "2D Pixel Art World",
                description = "مشهد بكسل آرت مع إضاءة ديناميكية 2D وجزيئات وتأثيرات بصرية",
                icon = Icons.Default.AutoFixHigh,
                color = StudioPurpleLight
            )
        )
    }

    KorvaDialog(
        onDismissRequest = onDismiss,
        title = "مشروع جديد (New Project)",
        subtitle = "تأسيس بنية الملفات ومساحة العمل",
        icon = Icons.Default.AutoFixHigh,
        maxWidth = 520.dp,
        buttons = {
            KorvaOutlinedButton(
                text = "إلغاء",
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )

            KorvaPrimaryButton(
                text = "تثبيت وإنشاء المشروع",
                onClick = {
                    if (projectName.isBlank()) {
                        errorMessage = "يرجى إدخال اسم المشروع أولاً"
                    } else {
                        onCreateProject(
                            projectName.trim(),
                            selectedPath.ifBlank { defaultPath },
                            templates[selectedTemplateIndex].title
                        )
                    }
                },
                icon = Icons.Default.AutoFixHigh,
                modifier = Modifier
                    .weight(1.3f)
                    .testTag("save_and_create_button")
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 340.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Project Name Field
            Text(
                text = "اسم المشروع *",
                color = TextPrimary,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = projectName,
                onValueChange = {
                    projectName = it
                    if (it.isNotBlank()) errorMessage = null
                },
                placeholder = {
                    Text("أدخل اسم المشروع (مثال: CyberRunner_2D)", color = TextMuted, fontSize = 11.sp)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("project_name_input"),
                singleLine = true,
                isError = errorMessage != null,
                supportingText = if (errorMessage != null) {
                    {
                        Text(text = errorMessage ?: "", color = StudioRed, fontSize = 10.sp)
                    }
                } else null,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
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
                    unfocusedTextColor = TextPrimary,
                    cursorColor = StudioPurpleLight
                ),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (projectName.isBlank()) {
                        errorMessage = "يرجى إدخال اسم المشروع أولاً"
                    } else {
                        onCreateProject(projectName.trim(), selectedPath, templates[selectedTemplateIndex].title)
                    }
                })
            )

            // 2. Storage Path Field
            Text(
                text = "مسار الحفظ في الذاكرة *",
                color = TextPrimary,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(EngineCardBg)
                    .border(0.8.dp, StudioBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
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
                            tint = StudioPurpleLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedPath.ifBlank { defaultPath },
                            color = TextSecondary,
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StudioPurpleDark)
                            .border(0.8.dp, StudioPurpleLight, RoundedCornerShape(6.dp))
                            .clickable { onChangePathRequested() }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("change_path_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "تغيير المسار",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 3. Template Selection Grid
            Text(
                text = "قالب المشروع ونوع البيئة",
                color = TextPrimary,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                templates.forEachIndexed { index, template ->
                    val isSelected = selectedTemplateIndex == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) StudioPurpleDark.copy(alpha = 0.5f) else EngineCardBg)
                            .border(
                                width = if (isSelected) 1.2.dp else 0.8.dp,
                                color = if (isSelected) StudioPurpleLight else StudioBorder,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedTemplateIndex = index }
                            .padding(8.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = template.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) StudioPurpleLight else TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = template.title,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = template.description,
                                color = TextMuted,
                                fontSize = 8.5.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.Widgets
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.EngineBackground
import com.example.ui.theme.EngineBorder
import com.example.ui.theme.EngineBorderGlow
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.EngineSurface
import com.example.ui.theme.EngineSurfaceVariant
import com.example.ui.theme.KorvaAmber
import com.example.ui.theme.KorvaBlue
import com.example.ui.theme.KorvaCyan
import com.example.ui.theme.KorvaRed
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

    val templates = remember {
        listOf(
            ProjectTemplate(
                title = "2D Game Engine",
                description = "محرك ألعاب ثنائي الأبعاد مع دعم فيزياء وتصادمات",
                icon = Icons.Default.Gamepad,
                color = KorvaCyan
            ),
            ProjectTemplate(
                title = "3D Scene Studio",
                description = "مشهد ثلاثي الأبعاد مع إضاءة ديناميكية وكاميرا",
                icon = Icons.Default.ViewInAr,
                color = KorvaBlue
            ),
            ProjectTemplate(
                title = "Physics Sandbox",
                description = "مختبر محاكاة الجاذبية والأجسام التفاعلية",
                icon = Icons.Default.Science,
                color = KorvaAmber
            )
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
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
                        listOf(KorvaCyan.copy(alpha = 0.6f), EngineBorder)
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
                                .background(KorvaCyan.copy(alpha = 0.15f))
                                .border(1.dp, KorvaCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                tint = KorvaCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "إنشاء مشروع جديد (New Project)",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "تأسيس بنية الملفات ومساحة العمل التفاعلية",
                                color = TextSecondary,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("dialog_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Project Name Input Field (حقل إدخال نصي: أدخل اسم المشروع)
                Text(
                    text = "اسم المشروع *",
                    color = TextPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = projectName,
                    onValueChange = {
                        projectName = it
                        if (it.isNotBlank()) errorMessage = null
                    },
                    placeholder = {
                        Text("أدخل اسم المشروع (مثال: CyberRunner_2D)", color = TextMuted, fontSize = 12.sp)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("project_name_input"),
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage ?: "",
                                color = KorvaRed,
                                fontSize = 11.sp
                            )
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = KorvaCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KorvaCyan,
                        unfocusedBorderColor = EngineBorder,
                        focusedContainerColor = EngineCardBg,
                        unfocusedContainerColor = EngineCardBg,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = KorvaCyan
                    ),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (projectName.isBlank()) {
                            errorMessage = "يرجى إدخال اسم المشروع أولاً"
                        } else {
                            onCreateProject(projectName, selectedPath, templates[selectedTemplateIndex].title)
                        }
                    })
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Path Picker Field (حقل اختيار المسار: يعرض المسار الافتراضي مع زر تغيير المسار)
                Text(
                    text = "مسار الحفظ في الذاكرة *",
                    color = TextPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(EngineCardBg)
                        .border(1.dp, EngineBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
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
                                tint = KorvaCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedPath.ifBlank { defaultPath },
                                color = TextSecondary,
                                fontSize = 11.5.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Change Path Button (زر "تغيير المسار")
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EngineSurfaceVariant)
                                .border(1.dp, KorvaCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .clickable { onChangePathRequested() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("change_path_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = KorvaCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "تغيير المسار",
                                    color = KorvaCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Template selection
                Text(
                    text = "قالب المشروع ونوع البيئة",
                    color = TextPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    templates.forEachIndexed { index, template ->
                        val isSelected = selectedTemplateIndex == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) template.color.copy(alpha = 0.15f) else EngineCardBg)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) template.color else EngineBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedTemplateIndex = index }
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = template.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) template.color else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = template.title,
                                        color = if (isSelected) TextPrimary else TextSecondary,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = template.description,
                                    color = TextMuted,
                                    fontSize = 9.5.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Footer Buttons (Save/Create + Cancel)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onDismiss() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "إلغاء",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Save / Create Button (زر التثبيت - Save / Create)
                    Button(
                        onClick = {
                            if (projectName.isBlank()) {
                                errorMessage = "يرجى إدخال اسم المشروع"
                            } else {
                                onCreateProject(
                                    projectName,
                                    selectedPath.ifBlank { defaultPath },
                                    templates[selectedTemplateIndex].title
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KorvaCyan,
                            contentColor = EngineBackground
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_and_create_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = EngineBackground
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تثبيت وإنشاء المشروع",
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

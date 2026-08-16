package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectEntity
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.components.KorvaLogo
import com.example.ui.components.KorvaStatusBar
import com.example.ui.components.ProjectCard
import com.example.ui.theme.EngineBackground
import com.example.ui.theme.EngineBorder
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

@Composable
fun HomeScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EngineBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Main Landscape Split (1/3 Left Control + 2/3 Right Data)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // ========================================================
                // 1. الجانب الأيسر (ثلث الشاشة - التحكم الرئيسي)
                // ========================================================
                Box(
                    modifier = Modifier
                        .weight(1.0f) // 1/3 of total 3.0 ratio
                        .fillMaxHeight()
                        .background(EngineSurface)
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                listOf(EngineBorder, Color(0xFF0F172A))
                            ),
                            shape = RoundedCornerShape(0.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Section: App Logo + Explicit Exit Button
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // شعار التطبيق الفخم باسمه: في أعلى الزاوية اليسرى كعلامة بصرية ثابتة
                                KorvaLogo(compact = false)

                                // زر خروج صريح (Exit Button): يوضع في الزاوية العليا كأيقونة مصغرة
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(EngineCardBg)
                                        .border(1.dp, KorvaRed.copy(alpha = 0.4f), CircleShape)
                                        .clickable { viewModel.openExitConfirmDialog() }
                                        .testTag("explicit_exit_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PowerSettingsNew,
                                        contentDescription = "زر خروج صريح من التطبيق",
                                        tint = KorvaRed,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Separator line with cyan accent
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(KorvaCyan.copy(alpha = 0.5f), Color.Transparent)
                                        )
                                    )
                            )
                        }

                        // Middle Section: Primary and Secondary Action Buttons
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // الزر الأول والأساسي: زر "مشروع جديد" (Start Editing)
                            Button(
                                onClick = { viewModel.openNewProjectDialog() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = KorvaCyan, ambientColor = KorvaCyan)
                                    .testTag("start_editing_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = KorvaCyan,
                                    contentColor = EngineBackground
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(EngineBackground.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = EngineBackground,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "مشروع جديد (Start Editing)",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            text = "تأسيس بيئة عمل ومحرر فوري",
                                            fontSize = 10.sp,
                                            color = EngineBackground.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }

                            // الزر الثاني: زر "تحرير مشروع محفوظ" (Open Project)
                            OutlinedButton(
                                onClick = { viewModel.openOpenProjectDialog() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("open_saved_project_button"),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = EngineCardBg,
                                    contentColor = TextPrimary
                                ),
                                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                    brush = Brush.horizontalGradient(
                                        listOf(KorvaBlue.copy(alpha = 0.6f), EngineBorder)
                                    ),
                                    width = 1.2.dp
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null,
                                        tint = KorvaBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "تحرير مشروع محفوظ (Open Project)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "فتح مستعرض الملفات والاختيار اليدوي",
                                            fontSize = 9.5.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }

                        // Bottom telemetry info inside left panel
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(EngineCardBg)
                                .border(0.8.dp, EngineBorder, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Korva Runtime Engine",
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "المحرك جاهز لبيئات 2D / 3D والفيزياء",
                                        color = TextMuted,
                                        fontSize = 9.sp
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = KorvaCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // ========================================================
                // 2. الجانب الأيمن (ثلثي الشاشة - إدارة البيانات)
                // ========================================================
                Column(
                    modifier = Modifier
                        .weight(2.0f) // 2/3 of total 3.0 ratio
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    // Header of Data Management
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "قائمة المشاريع الأخيرة (Recent Projects)",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(KorvaCyan.copy(alpha = 0.15f))
                                    .border(1.dp, KorvaCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${uiState.filteredProjects.size} مشروع",
                                    color = KorvaCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Search Filter input
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("بحث في المشاريع...", fontSize = 11.sp, color = TextMuted) },
                            modifier = Modifier
                                .width(220.dp)
                                .height(44.dp)
                                .testTag("search_projects_input"),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotBlank()) {
                                    IconButton(
                                        onClick = { viewModel.setSearchQuery("") },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "مسح",
                                            tint = TextMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KorvaCyan,
                                unfocusedBorderColor = EngineBorder,
                                focusedContainerColor = EngineCardBg,
                                unfocusedContainerColor = EngineCardBg,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Content Area: Empty State OR Recent Projects List
                    if (uiState.filteredProjects.isEmpty()) {
                        // مؤشر فارغ (Empty State View): نص يظهر في الجانب الأيمن (مكان المشاريع الأخيرة)
                        // يكتب فيه "لا توجد مشاريع سابقة، ابدأ مشروعك الأول الآن"، وذلك في حال كان التطبيق يُفتح لأول مرة
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(EngineSurface.copy(alpha = 0.6f))
                                .border(1.dp, EngineBorder.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                                .padding(24.dp)
                                .testTag("empty_projects_state"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(EngineCardBg)
                                        .border(1.dp, KorvaCyan.copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LayersClear,
                                        contentDescription = null,
                                        tint = KorvaCyan,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "لا توجد مشاريع سابقة، ابدأ مشروعك الأول الآن",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "اضغط على زر 'مشروع جديد' للبدء في إنشاء لعبتك أو تطبيقك التفاعلي مع محرك korva engine",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.width(360.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { viewModel.openNewProjectDialog() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = KorvaCyan.copy(alpha = 0.2f),
                                        contentColor = KorvaCyan
                                    ),
                                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                        brush = Brush.linearGradient(listOf(KorvaCyan, KorvaBlue))
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("empty_state_create_button")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("إنشاء أول مشروع", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        // Projects Cards list ordered vertically
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .testTag("recent_projects_list"),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = uiState.filteredProjects,
                                key = { it.id }
                            ) { project ->
                                ProjectCard(
                                    project = project,
                                    onClick = { viewModel.openProjectInEditor(project) },
                                    onDeleteClick = { viewModel.promptDeleteProject(project) }
                                )
                            }
                        }
                    }
                }
            }

            // ========================================================
            // 3. شريط الحالة السفلي (Status Bar)
            // ========================================================
            KorvaStatusBar(
                defaultPath = uiState.defaultSavePath,
                onChangePathClick = { viewModel.openChangePathDialog() }
            )
        }
    }
}

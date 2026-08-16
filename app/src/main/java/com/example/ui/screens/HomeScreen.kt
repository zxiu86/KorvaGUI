package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.components.KorvaLogo
import com.example.ui.components.KorvaStatusBar
import com.example.ui.components.ProjectCard
import com.example.ui.theme.EngineBackground
import com.example.ui.theme.EngineBorder
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.EngineSurface
import com.example.ui.theme.EngineWhiteBorder
import com.example.ui.theme.EngineWhiteGlass
import com.example.ui.theme.EngineWhiteMuted
import com.example.ui.theme.EngineWhitePrimary
import com.example.ui.theme.EngineWhiteSubtle
import com.example.ui.theme.EngineWhiteTranslucent
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
    val leftScrollState = rememberScrollState()

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
                // 1. الجانب الأيسر (ثلث الشاشة - التحكم الرئيسي والتمرير الديناميكي)
                // ========================================================
                Box(
                    modifier = Modifier
                        .weight(1.0f) // 1/3 ratio
                        .fillMaxHeight()
                        .background(EngineSurface)
                        .border(
                            width = 0.8.dp,
                            color = EngineBorder
                        )
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(leftScrollState),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Section: App Logo + Explicit Exit Button
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                KorvaLogo(compact = false)

                                // Explicit Exit Button (Frosted White & Red accent)
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(EngineCardBg)
                                        .border(0.8.dp, KorvaRed.copy(alpha = 0.4f), CircleShape)
                                        .clickable { viewModel.openExitConfirmDialog() }
                                        .testTag("explicit_exit_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PowerSettingsNew,
                                        contentDescription = "زر خروج صريح من التطبيق",
                                        tint = KorvaRed.copy(alpha = 0.9f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Separator line with translucent white accent
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(EngineWhiteBorder, Color.Transparent)
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Middle Section: Primary and Secondary Action Buttons
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Primary Button: "مشروع جديد" (Translucent White frosted engine theme)
                            Button(
                                onClick = { viewModel.openNewProjectDialog() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .shadow(6.dp, RoundedCornerShape(10.dp), spotColor = Color.Black)
                                    .testTag("start_editing_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EngineWhitePrimary,
                                    contentColor = EngineBackground
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(EngineBackground.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = EngineBackground,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "مشروع جديد (Start Editing)",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "تأسيس بيئة عمل ومحرر فوري",
                                            fontSize = 9.5.sp,
                                            color = EngineBackground.copy(alpha = 0.75f)
                                        )
                                    }
                                }
                            }

                            // Secondary Button: "تحرير مشروع محفوظ" (Translucent Frosted Glass)
                            OutlinedButton(
                                onClick = { viewModel.openOpenProjectDialog() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("open_saved_project_button"),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = EngineCardBg,
                                    contentColor = TextPrimary
                                ),
                                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                    brush = Brush.horizontalGradient(
                                        listOf(EngineWhiteBorder, EngineBorder)
                                    ),
                                    width = 1.dp
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null,
                                        tint = EngineWhiteTranslucent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "تحرير مشروع محفوظ (Open Project)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "استعراض واختيار يدوي",
                                            fontSize = 9.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Bottom telemetry info inside left panel
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(EngineCardBg)
                                .border(0.8.dp, EngineBorder, RoundedCornerShape(8.dp))
                                .padding(8.dp)
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
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "المحرك جاهز لبيئات 2D / 3D والفيزياء",
                                        color = TextMuted,
                                        fontSize = 8.5.sp
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = EngineWhiteMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }

                // ========================================================
                // 2. الجانب الأيمن (ثلثي الشاشة - إدارة البيانات والتمرير)
                // ========================================================
                Column(
                    modifier = Modifier
                        .weight(2.0f) // 2/3 ratio
                        .fillMaxHeight()
                        .padding(14.dp)
                ) {
                    // Header of Data Management
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "المشاريع الأخيرة (Recent Projects)",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(EngineWhiteGlass)
                                    .border(0.8.dp, EngineWhiteBorder, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${uiState.filteredProjects.size} مشروع",
                                    color = EngineWhitePrimary,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Search Filter input
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("بحث في المشاريع...", fontSize = 10.5.sp, color = TextMuted) },
                            modifier = Modifier
                                .width(200.dp)
                                .height(40.dp)
                                .testTag("search_projects_input"),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                            },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotBlank()) {
                                    IconButton(
                                        onClick = { viewModel.setSearchQuery("") },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "مسح",
                                            tint = TextMuted,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EngineWhitePrimary,
                                unfocusedBorderColor = EngineBorder,
                                focusedContainerColor = EngineCardBg,
                                unfocusedContainerColor = EngineCardBg,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Content Area: Empty State OR Recent Projects List
                    if (uiState.filteredProjects.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(EngineSurface.copy(alpha = 0.6f))
                                .border(0.8.dp, EngineBorder, RoundedCornerShape(12.dp))
                                .padding(20.dp)
                                .testTag("empty_projects_state"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(EngineCardBg)
                                        .border(0.8.dp, EngineWhiteBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LayersClear,
                                        contentDescription = null,
                                        tint = EngineWhiteTranslucent,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "لا توجد مشاريع سابقة، ابدأ مشروعك الأول الآن",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "اضغط على زر 'مشروع جديد' للبدء في إنشاء لعبتك أو تطبيقك التفاعلي مع محرك korva engine",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.width(320.dp)
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(EngineWhiteGlass)
                                        .border(0.8.dp, EngineWhiteBorder, RoundedCornerShape(8.dp))
                                        .clickable { viewModel.openNewProjectDialog() }
                                        .padding(horizontal = 14.dp, vertical = 7.dp)
                                        .testTag("empty_state_create_button")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp), tint = EngineWhitePrimary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("إنشاء أول مشروع", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = EngineWhitePrimary)
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .testTag("recent_projects_list"),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
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

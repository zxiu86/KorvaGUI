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
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.EngineSurface
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioPurple
import com.example.ui.theme.StudioPurpleDark
import com.example.ui.theme.StudioPurpleGlass
import com.example.ui.theme.StudioPurpleLight
import com.example.ui.theme.StudioRed
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
            // Main Landscape Split
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // ========================================================
                // 1. Left Control Panel
                // ========================================================
                Box(
                    modifier = Modifier
                        .weight(1.0f)
                        .fillMaxHeight()
                        .background(EngineSurface)
                        .border(
                            width = 0.6.dp,
                            color = StudioBorder
                        )
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(leftScrollState),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Section: App Logo + Exit Button
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                KorvaLogo(compact = true)

                                // Exit Button
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(EngineCardBg)
                                        .border(0.6.dp, StudioRed.copy(alpha = 0.4f), CircleShape)
                                        .clickable { viewModel.openExitConfirmDialog() }
                                        .testTag("explicit_exit_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PowerSettingsNew,
                                        contentDescription = "زر خروج صريح من التطبيق",
                                        tint = StudioRed.copy(alpha = 0.9f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Separator line
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.6.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(StudioPurpleLight.copy(alpha = 0.3f), Color.Transparent)
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Middle Section: Primary Action Buttons
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Primary Button: "مشروع جديد"
                            Button(
                                onClick = { viewModel.openNewProjectDialog() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .shadow(4.dp, RoundedCornerShape(6.dp), spotColor = StudioPurpleDark)
                                    .testTag("start_editing_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StudioPurple,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(StudioPurpleDark),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "مشروع جديد (Start Editing)",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Secondary Button: "تحرير مشروع محفوظ"
                            OutlinedButton(
                                onClick = { viewModel.openOpenProjectDialog() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                                    .testTag("open_saved_project_button"),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = EngineCardBg,
                                    contentColor = TextPrimary
                                ),
                                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                    brush = Brush.horizontalGradient(
                                        listOf(StudioPurpleLight.copy(alpha = 0.5f), StudioBorder)
                                    ),
                                    width = 0.6.dp
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null,
                                        tint = StudioPurpleLight,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "تحرير مشروع محفوظ (Open Project)",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Bottom info inside left panel
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(5.dp))
                                .background(EngineCardBg)
                                .border(0.5.dp, StudioBorder, RoundedCornerShape(5.dp))
                                .padding(5.dp)
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
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Pure 2D Engine Ready",
                                        color = TextMuted,
                                        fontSize = 7.sp
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = StudioPurpleLight,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                // ========================================================
                // 2. Right Data & Projects Panel
                // ========================================================
                Column(
                    modifier = Modifier
                        .weight(2.0f)
                        .fillMaxHeight()
                        .padding(8.dp)
                ) {
                    // Header of Projects List
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "المشاريع الأخيرة (Recent Projects)",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StudioPurpleGlass)
                                    .border(0.5.dp, StudioPurpleLight.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "${uiState.filteredProjects.size} مشروع",
                                    color = StudioPurpleLight,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Search Filter input
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("بحث في المشاريع...", fontSize = 8.5.sp, color = TextMuted) },
                            modifier = Modifier
                                .width(150.dp)
                                .height(32.dp)
                                .testTag("search_projects_input"),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                            },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotBlank()) {
                                    IconButton(
                                        onClick = { viewModel.setSearchQuery("") },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "مسح",
                                            tint = TextMuted,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StudioPurpleLight,
                                unfocusedBorderColor = StudioBorder,
                                focusedContainerColor = EngineCardBg,
                                unfocusedContainerColor = EngineCardBg,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Content Area: Empty State OR Recent Projects List
                    if (uiState.filteredProjects.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(EngineSurface.copy(alpha = 0.6f))
                                .border(0.6.dp, StudioBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                                .testTag("empty_projects_state"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(EngineCardBg)
                                        .border(0.6.dp, StudioPurpleLight.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LayersClear,
                                        contentDescription = null,
                                        tint = StudioPurpleLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "لا توجد مشاريع سابقة، ابدأ مشروعك الأول الآن",
                                    color = TextPrimary,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "اضغط على زر 'مشروع جديد' للبدء في إنشاء لعبتك مع korva engine",
                                    color = TextSecondary,
                                    fontSize = 8.5.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.width(260.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(StudioPurple)
                                        .clickable { viewModel.openNewProjectDialog() }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                        .testTag("empty_state_create_button")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(11.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("إنشاء أول مشروع", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                            verticalArrangement = Arrangement.spacedBy(4.dp)
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
            // 3. Status Bar
            // ========================================================
            KorvaStatusBar(
                defaultPath = uiState.defaultSavePath,
                onChangePathClick = { viewModel.openChangePathDialog() }
            )
        }
    }
}

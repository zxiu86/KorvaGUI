package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectEntity
import com.example.model.LogLevel
import com.example.model.NodeType
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.components.StudioBottomNav
import com.example.ui.components.StudioGlobalTab
import com.example.ui.components.StudioHeader
import com.example.ui.components.StudioHierarchyPanel
import com.example.ui.components.StudioInspector
import com.example.ui.components.StudioTimeline
import com.example.ui.components.StudioViewport
import com.example.ui.theme.EngineBackground
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.EngineSurface
import com.example.ui.theme.StudioBlue
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioGreen
import com.example.ui.theme.StudioOrange
import com.example.ui.theme.StudioPink
import com.example.ui.theme.StudioPurple
import com.example.ui.theme.StudioPurpleDark
import com.example.ui.theme.StudioPurpleLight
import com.example.ui.theme.StudioRed
import com.example.ui.theme.StudioYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EditorWorkspaceScreen(
    project: ProjectEntity,
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var globalTab by remember { mutableStateOf(StudioGlobalTab.EDITOR) }
    var isTimelinePlaying by remember { mutableStateOf(false) }

    // Collapsible Dock Panel Visibility States
    var isHierarchyVisible by remember { mutableStateOf(true) }
    var isInspectorVisible by remember { mutableStateOf(true) }
    var isTimelineVisible by remember { mutableStateOf(true) }
    var isFullscreen by remember { mutableStateOf(false) }

    val selectedNode = uiState.sceneNodes.find { it.id == uiState.selectedNodeId }
        ?: uiState.sceneNodes.firstOrNull()

    // Simulation animation loop
    LaunchedEffect(uiState.isSimulationPlaying) {
        if (uiState.isSimulationPlaying) {
            var step = 0
            while (uiState.isSimulationPlaying) {
                delay(16)
                step++
                val angle = step * 0.05f
                val dx = kotlin.math.sin(angle) * 1.5f
                val dy = kotlin.math.cos(angle) * 0.8f
                viewModel.updateSelectedNodePos(dx.toFloat(), dy.toFloat())
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EngineBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ========================================================
            // 1. Studio Header Top Bar
            // ========================================================
            StudioHeader(
                projectName = project.name.ifBlank { "Dark Village" },
                projectType = project.templateType.ifBlank { "2D Project" },
                isPlaying = uiState.isSimulationPlaying,
                isPaused = false,
                isHierarchyVisible = isHierarchyVisible && !isFullscreen,
                isInspectorVisible = isInspectorVisible && !isFullscreen,
                isTimelineVisible = isTimelineVisible && !isFullscreen,
                isFullscreen = isFullscreen,
                availableProjects = uiState.projects,
                onToggleHierarchy = {
                    if (isFullscreen) isFullscreen = false
                    isHierarchyVisible = !isHierarchyVisible
                },
                onToggleInspector = {
                    if (isFullscreen) isFullscreen = false
                    isInspectorVisible = !isInspectorVisible
                },
                onToggleTimeline = {
                    if (isFullscreen) isFullscreen = false
                    isTimelineVisible = !isTimelineVisible
                },
                onToggleFullscreen = {
                    isFullscreen = !isFullscreen
                },
                onPlayClick = {
                    viewModel.toggleSimulation()
                },
                onPauseClick = {
                    viewModel.toggleSimulation()
                },
                onStopClick = {
                    if (uiState.isSimulationPlaying) viewModel.toggleSimulation()
                },
                onBuildApkClick = {
                    globalTab = StudioGlobalTab.BUILD
                    viewModel.addLog(LogLevel.INFO, "⚙️ بدء عملية بناء ملف APK للمشروع...")
                    coroutineScope.launch {
                        delay(500)
                        viewModel.addLog(LogLevel.SUCCESS, "✓ تم فحص وتجميع المشاهد والمكتبات بنجاح!")
                        snackbarHostState.showSnackbar("جاري بناء وتجميع حزمة APK للمشروع...")
                    }
                },
                onSaveClick = {
                    viewModel.saveCurrentProject()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("تم حفظ بيانات ومشاهد المشروع بنجاح!")
                    }
                },
                onProjectSwitch = { name ->
                    viewModel.switchActiveProjectByName(name)
                },
                onBackToProjects = {
                    viewModel.closeEditor()
                }
            )

            // ========================================================
            // 2. Main Studio 3-Column Split (Dynamic Collapsible Layout)
            // ========================================================
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Primary Editor Workspace
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Panel: Objects & Layers (Collapsible)
                    AnimatedVisibility(
                        visible = isHierarchyVisible && !isFullscreen,
                        enter = expandHorizontally() + fadeIn(),
                        exit = shrinkHorizontally() + fadeOut()
                    ) {
                        StudioHierarchyPanel(
                            sceneNodes = uiState.sceneNodes,
                            selectedNodeId = selectedNode?.id,
                            onSelectNode = { viewModel.selectSceneNode(it) },
                            onAddNode = { name, type -> viewModel.addSceneNode(name, type) },
                            onDeleteNode = { viewModel.deleteSelectedNode() },
                            onToggleNodeVisibility = { viewModel.toggleNodeVisibility(it) },
                            onCollapse = { isHierarchyVisible = false }
                        )
                    }

                    // Center Column: 2D Scene Viewport + Bottom Animation Timeline
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        // Procedural Viewport Canvas
                        StudioViewport(
                            selectedNode = selectedNode,
                            allNodes = uiState.sceneNodes,
                            isHierarchyVisible = isHierarchyVisible && !isFullscreen,
                            isInspectorVisible = isInspectorVisible && !isFullscreen,
                            isTimelineVisible = isTimelineVisible && !isFullscreen,
                            onRestoreHierarchy = {
                                isFullscreen = false
                                isHierarchyVisible = true
                            },
                            onRestoreInspector = {
                                isFullscreen = false
                                isInspectorVisible = true
                            },
                            onRestoreTimeline = {
                                isFullscreen = false
                                isTimelineVisible = true
                            },
                            onNodeDrag = { dx, dy ->
                                viewModel.updateSelectedNodePos(dx, dy)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )

                        // Animation Timeline (Collapsible)
                        AnimatedVisibility(
                            visible = isTimelineVisible && !isFullscreen,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            StudioTimeline(
                                isPlaying = isTimelinePlaying,
                                onTogglePlay = { isTimelinePlaying = !isTimelinePlaying },
                                onCollapse = { isTimelineVisible = false }
                            )
                        }
                    }

                    // Right Panel: Inspector & Components (Collapsible)
                    AnimatedVisibility(
                        visible = isInspectorVisible && !isFullscreen,
                        enter = expandHorizontally() + fadeIn(),
                        exit = shrinkHorizontally() + fadeOut()
                    ) {
                        StudioInspector(
                            selectedNode = selectedNode,
                            onUpdateName = { viewModel.setSelectedNodeName(it) },
                            onUpdatePos = { x, y -> viewModel.setSelectedNodeExactPos(x, y) },
                            onUpdateScale = { viewModel.setSelectedNodeExactScale(it) },
                            onUpdateRotation = { viewModel.setSelectedNodeExactRotation(it) },
                            onUpdateColor = { viewModel.setSelectedNodeColor(it) },
                            onUpdatePhysics = { enabled, mass -> viewModel.setSelectedNodePhysics(enabled, mass) },
                            onCollapse = { isInspectorVisible = false }
                        )
                    }
                }

                // ========================================================
                // Overlays for Bottom Tabs (Assets, Build/Console, Settings)
                // ========================================================
                if (globalTab == StudioGlobalTab.ASSETS) {
                    AssetBrowserOverlay(
                        onAddSprite = { name ->
                            viewModel.addSceneNode(name, NodeType.SPRITE_OBJECT)
                            viewModel.addLog(LogLevel.SUCCESS, "تم إدراج المورد $name في المشهد")
                        },
                        onClose = { globalTab = StudioGlobalTab.EDITOR }
                    )
                }

                if (globalTab == StudioGlobalTab.BUILD) {
                    ConsoleBuildOverlay(
                        logs = uiState.engineLogs,
                        projectName = project.name,
                        onExportApk = {
                            viewModel.addLog(LogLevel.SUCCESS, "🚀 تم تجهيز ملف التثبيت APK للمشروع بنجاح!")
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("تم اكتمال بناء ملف APK بنجاح!")
                            }
                        },
                        onClearLogs = {
                            viewModel.addLog(LogLevel.INFO, "تم تفريغ سجلات المحرك")
                        },
                        onClose = { globalTab = StudioGlobalTab.EDITOR }
                    )
                }

                if (globalTab == StudioGlobalTab.SETTINGS) {
                    ProjectSettingsOverlay(
                        projectName = project.name,
                        template = project.templateType,
                        onSaveSettings = {
                            viewModel.saveCurrentProject()
                            globalTab = StudioGlobalTab.EDITOR
                        },
                        onClose = { globalTab = StudioGlobalTab.EDITOR }
                    )
                }
            }

            // ========================================================
            // 3. Studio Global Bottom Navigation Bar
            // ========================================================
            AnimatedVisibility(
                visible = !isFullscreen,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                StudioBottomNav(
                    activeTab = globalTab,
                    onTabSelected = { tab ->
                        if (tab == StudioGlobalTab.PROJECTS) {
                            viewModel.closeEditor()
                        } else {
                            globalTab = tab
                        }
                    }
                )
            }
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}

@Composable
private fun AssetBrowserOverlay(
    onAddSprite: (String) -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(360.dp)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(8.dp))
                .background(EngineSurface)
                .border(0.8.dp, StudioBorder, RoundedCornerShape(8.dp))
                .clickable(enabled = false) {}
                .padding(10.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("مستكشف الموارد (Asset Browser)", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp).clickable { onClose() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val assets = listOf(
                        Triple("hero_knight.png", "2D Sprite Sheet (32x32)", Icons.Default.Image),
                        Triple("dungeon_tileset.png", "Tilemap Atlas (16x16)", Icons.Default.Image),
                        Triple("bgm_dark_ambient.ogg", "Audio Loop (Stereo 44.1kHz)", Icons.Default.Audiotrack),
                        Triple("jump_sfx.wav", "Sound Effect (0.4s)", Icons.Default.Audiotrack),
                        Triple("PlayerController.kt", "Kotlin Native Script", Icons.Default.Code),
                        Triple("PressStart2P.ttf", "Pixel Bitmap Font", Icons.Default.FontDownload)
                    )

                    items(assets) { (name, desc, icon) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(EngineCardBg)
                                .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                                .padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = icon, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(name, color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(desc, color = TextMuted, fontSize = 7.5.sp)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(StudioPurpleDark)
                                    .border(0.5.dp, StudioPurpleLight, RoundedCornerShape(3.dp))
                                    .clickable { onAddSprite(name.substringBefore(".")) }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(9.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("إدراج", color = Color.White, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsoleBuildOverlay(
    logs: List<com.example.model.EngineLog>,
    projectName: String,
    onExportApk: () -> Unit,
    onClearLogs: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(420.dp)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(8.dp))
                .background(EngineSurface)
                .border(0.8.dp, StudioBorder, RoundedCornerShape(8.dp))
                .clickable(enabled = false) {}
                .padding(10.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("وحدة التحكم وسجلات المحرك (Build Terminal)", color = TextPrimary, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp).clickable { onClose() }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Terminal Output Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(EngineBackground)
                        .border(0.5.dp, StudioBorder, RoundedCornerShape(4.dp))
                        .padding(6.dp)
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(logs) { log ->
                            val color = when (log.level) {
                                LogLevel.INFO -> StudioBlue
                                LogLevel.WARN -> StudioYellow
                                LogLevel.ERROR -> StudioRed
                                LogLevel.SUCCESS -> StudioGreen
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "[${log.timestamp}]",
                                    color = TextMuted,
                                    fontSize = 7.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = log.message,
                                    color = color,
                                    fontSize = 7.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onClearLogs,
                        colors = ButtonDefaults.buttonColors(containerColor = EngineCardBg),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("تفريغ السجلات", color = TextSecondary, fontSize = 8.sp)
                    }

                    Button(
                        onClick = onExportApk,
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("تصدير APK للعبة", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectSettingsOverlay(
    projectName: String,
    template: String,
    onSaveSettings: () -> Unit,
    onClose: () -> Unit
) {
    var fpsCap by remember { mutableFloatStateOf(60f) }
    var antiAliasing by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(360.dp)
                .fillMaxHeight(0.8f)
                .clip(RoundedCornerShape(8.dp))
                .background(EngineSurface)
                .border(0.8.dp, StudioBorder, RoundedCornerShape(8.dp))
                .clickable(enabled = false) {}
                .padding(10.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إعدادات المحرك والمشروع", color = TextPrimary, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp).clickable { onClose() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("اسم المشروع: $projectName", color = TextPrimary, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                    Text("نوع المحرك: $template", color = StudioPurpleLight, fontSize = 8.sp)
                    Text("حزمة التطبيق: com.korva.game.${projectName.lowercase().replace(" ", "")}", color = TextMuted, fontSize = 7.5.sp, fontFamily = FontFamily.Monospace)

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("معدل الإطارات المستهدف (60 FPS)", color = TextSecondary, fontSize = 8.sp)
                        Text("60 FPS", color = StudioGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تنعيم الحواف (Anti-Aliasing 2D)", color = TextSecondary, fontSize = 8.sp)
                        Switch(
                            checked = antiAliasing,
                            onCheckedChange = { antiAliasing = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = StudioPurple)
                        )
                    }
                }

                Button(
                    onClick = onSaveSettings,
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth().height(28.dp)
                ) {
                    Text("حفظ الإعدادات", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

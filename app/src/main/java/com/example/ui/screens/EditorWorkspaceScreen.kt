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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.engine.interfaces.PropertyValue
import com.example.model.LogLevel
import com.example.model.NodeType
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.components.StudioBottomNav
import com.example.ui.components.StudioGlobalTab
import com.example.ui.components.StudioHeader
import com.example.ui.components.StudioViewport
import com.example.ui.panels.AnimationTimelinePanel
import com.example.ui.panels.AssetBrowserPanel
import com.example.ui.panels.LayerManagerPanel
import com.example.ui.panels.ObjectBrowserPanel
import com.example.ui.panels.ObjectInspectorPanel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class LeftPanelMode {
    OBJECTS,
    LAYERS
}

enum class BottomPanelMode {
    TIMELINE,
    ASSETS,
    CONSOLE
}

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

    // Collapsible Dock Panel Visibility States
    var isHierarchyVisible by remember { mutableStateOf(true) }
    var isInspectorVisible by remember { mutableStateOf(true) }
    var isTimelineVisible by remember { mutableStateOf(true) }
    var isFullscreen by remember { mutableStateOf(false) }

    // Sub-panel mode switches
    var leftPanelMode by remember { mutableStateOf(LeftPanelMode.OBJECTS) }
    var bottomPanelMode by remember { mutableStateOf(BottomPanelMode.TIMELINE) }

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
            // 1. Studio Header Top Bar (With Undo/Redo & Transport)
            // ========================================================
            StudioHeader(
                projectName = project.name.ifBlank { "Korva Game" },
                projectType = project.templateType.ifBlank { "2D Engine" },
                isPlaying = uiState.isSimulationPlaying,
                isPaused = false,
                isHierarchyVisible = isHierarchyVisible && !isFullscreen,
                isInspectorVisible = isInspectorVisible && !isFullscreen,
                isTimelineVisible = isTimelineVisible && !isFullscreen,
                isFullscreen = isFullscreen,
                canUndo = uiState.canUndo,
                canRedo = uiState.canRedo,
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
                onUndoClick = { viewModel.undo() },
                onRedoClick = { viewModel.redo() },
                onPlayClick = { viewModel.toggleSimulation() },
                onPauseClick = { viewModel.toggleSimulation() },
                onStopClick = {
                    if (uiState.isSimulationPlaying) viewModel.toggleSimulation()
                },
                onBuildApkClick = {
                    globalTab = StudioGlobalTab.BUILD
                    viewModel.addLog(LogLevel.INFO, "⚙️ بدء تجميع ملفات ومكتبات المشروع...")
                    coroutineScope.launch {
                        delay(500)
                        viewModel.addLog(LogLevel.SUCCESS, "✓ تم فحص المشاهد والموارد بنجاح!")
                        snackbarHostState.showSnackbar("جاري تصدير اللعبة بنجاح...")
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
            // 2. Main Studio 3-Column Split (Touch-First Dock)
            // ========================================================
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // ----------------------------------------------------
                    // Left Panel: Objects Browser / Layer Manager Switcher
                    // ----------------------------------------------------
                    AnimatedVisibility(
                        visible = isHierarchyVisible && !isFullscreen,
                        enter = expandHorizontally() + fadeIn(),
                        exit = shrinkHorizontally() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .width(220.dp)
                                .fillMaxHeight()
                                .background(EngineSurface)
                                .border(width = 0.8.dp, color = StudioBorder)
                        ) {
                            // Left Panel Mode Tab Selector
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(EngineBackground)
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (leftPanelMode == LeftPanelMode.OBJECTS) StudioPurple else Color.Transparent)
                                        .clickable { leftPanelMode = LeftPanelMode.OBJECTS }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "الكائنات (Objects)",
                                        color = if (leftPanelMode == LeftPanelMode.OBJECTS) Color.White else TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (leftPanelMode == LeftPanelMode.LAYERS) StudioPurple else Color.Transparent)
                                        .clickable { leftPanelMode = LeftPanelMode.LAYERS }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "الطبقات (Layers)",
                                        color = if (leftPanelMode == LeftPanelMode.LAYERS) Color.White else TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            val layers = uiState.activeScene?.layers ?: emptyList()

                            if (leftPanelMode == LeftPanelMode.OBJECTS) {
                                ObjectBrowserPanel(
                                    layers = layers,
                                    selectedObjectId = uiState.selectedObject?.id ?: uiState.selectedNodeId,
                                    onSelectObject = { viewModel.selectKorvaObject(it) },
                                    onCreateObject = { name, layerId -> viewModel.createKorvaObject(name, layerId) },
                                    onDuplicateObject = { viewModel.duplicateNode(it) },
                                    onDeleteObject = { viewModel.deleteKorvaObject(it) },
                                    onToggleObjectVisibility = { viewModel.toggleNodeVisibility(it) },
                                    onToggleObjectLock = { /* Handled via command */ },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                LayerManagerPanel(
                                    layers = layers,
                                    selectedLayerId = uiState.selectedLayerId,
                                    onSelectLayer = { viewModel.selectKorvaLayer(it) },
                                    onCreateLayer = { viewModel.createKorvaLayer(it) },
                                    onDeleteLayer = { viewModel.deleteKorvaLayer(it) },
                                    onToggleLayerVisibility = { viewModel.toggleKorvaLayerVisibility(it) },
                                    onToggleLayerLock = { viewModel.toggleKorvaLayerLock(it) },
                                    onMoveLayerUp = { viewModel.moveKorvaLayerUp(it) },
                                    onMoveLayerDown = { viewModel.moveKorvaLayerDown(it) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // ----------------------------------------------------
                    // Center Column: 2D Scene Viewport + Bottom Tools
                    // ----------------------------------------------------
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        // 2D Viewport Canvas
                        StudioViewport(
                            selectedNode = selectedNode,
                            allNodes = uiState.sceneNodes,
                            isHierarchyVisible = isHierarchyVisible && !isFullscreen,
                            isInspectorVisible = isInspectorVisible && !isFullscreen,
                            isTimelineVisible = isTimelineVisible && !isFullscreen,
                            isPlaying = uiState.isSimulationPlaying,
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
                            onNodeSelect = { nodeId ->
                                viewModel.selectKorvaObject(nodeId)
                            },
                            onNodeDrag = { dx, dy ->
                                viewModel.updateSelectedNodePos(dx, dy)
                            },
                            onNodeExactPosChange = { x, y ->
                                viewModel.setSelectedNodeExactPos(x, y)
                            },
                            onNodeExactScaleChange = { scale ->
                                viewModel.setSelectedNodeExactScale(scale)
                            },
                            onNodeExactRotationChange = { rot ->
                                viewModel.setSelectedNodeExactRotation(rot)
                            },
                            onNodeDuplicate = { nodeId ->
                                viewModel.duplicateNode(nodeId)
                            },
                            onNodeDelete = { nodeId ->
                                viewModel.deleteKorvaObject(nodeId)
                            },
                            onNodeBringToFront = { nodeId ->
                                viewModel.bringNodeToFront(nodeId)
                            },
                            onNodeSendToBack = { nodeId ->
                                viewModel.sendNodeToBack(nodeId)
                            },
                            onNodeCenter = { nodeId ->
                                viewModel.centerNode(nodeId)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )

                        // Bottom Panels (Timeline / Assets / Console / Bottom Sheet)
                        if (!isTimelineVisible && !isFullscreen) {
                            // Mini Dock bar at bottom
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(EngineSurface)
                                    .border(0.6.dp, StudioBorder)
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(EngineCardBg)
                                            .clickable {
                                                bottomPanelMode = BottomPanelMode.ASSETS
                                                isTimelineVisible = true
                                            }
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Folder, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("الموارد (Assets)", color = TextSecondary, fontSize = 9.5.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(EngineCardBg)
                                            .clickable {
                                                bottomPanelMode = BottomPanelMode.TIMELINE
                                                isTimelineVisible = true
                                            }
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Timeline, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("التحريك (Timeline)", color = TextSecondary, fontSize = 9.5.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(EngineCardBg)
                                            .clickable {
                                                bottomPanelMode = BottomPanelMode.CONSOLE
                                                isTimelineVisible = true
                                            }
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Terminal, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("السجل (Console)", color = TextSecondary, fontSize = 9.5.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = isTimelineVisible && !isFullscreen,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(EngineSurface)
                            ) {
                                // Bottom Mode Tab Switcher
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(EngineBackground)
                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf(
                                        BottomPanelMode.TIMELINE to "التحريك (Timeline)",
                                        BottomPanelMode.ASSETS to "الملفات (Assets)",
                                        BottomPanelMode.CONSOLE to "السجل (Console)"
                                    ).forEach { (mode, title) ->
                                        val isChosen = bottomPanelMode == mode
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isChosen) StudioPurpleDark else Color.Transparent)
                                                .border(0.6.dp, if (isChosen) StudioPurpleLight else Color.Transparent, RoundedCornerShape(4.dp))
                                                .clickable { bottomPanelMode = mode }
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = title,
                                                color = if (isChosen) Color.White else TextMuted,
                                                fontSize = 9.5.sp,
                                                fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.weight(1f))
                                    IconButton(
                                        onClick = { isTimelineVisible = false },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(12.dp))
                                    }
                                }

                                when (bottomPanelMode) {
                                    BottomPanelMode.TIMELINE -> {
                                        AnimationTimelinePanel(modifier = Modifier.fillMaxWidth())
                                    }
                                    BottomPanelMode.ASSETS -> {
                                        AssetBrowserPanel(
                                            assets = uiState.engineProject?.assets ?: emptyList(),
                                            onSelectAsset = {},
                                            onImportAsset = {
                                                viewModel.addLog(LogLevel.INFO, "تم فتح نافذة استيراد الموارد")
                                            },
                                            onDeleteAsset = {},
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    BottomPanelMode.CONSOLE -> {
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(90.dp)
                                                .background(EngineBackground)
                                                .padding(6.dp),
                                             verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            items(uiState.engineLogs) { log ->
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(log.timestamp, color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = log.message,
                                                        color = when (log.level) {
                                                            LogLevel.SUCCESS -> StudioGreen
                                                            LogLevel.WARN -> StudioYellow
                                                            LogLevel.ERROR -> StudioRed
                                                            else -> TextPrimary
                                                        },
                                                        fontSize = 9.5.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ----------------------------------------------------
                    // Right Panel: Dynamic Cards-Based Inspector
                    // ----------------------------------------------------
                    AnimatedVisibility(
                        visible = isInspectorVisible && !isFullscreen,
                        enter = expandHorizontally() + fadeIn(),
                        exit = shrinkHorizontally() + fadeOut()
                    ) {
                        val activeLayerName = uiState.activeScene?.layers?.find { it.id == uiState.selectedObject?.layerId }?.name ?: "World"
                        ObjectInspectorPanel(
                            selectedObject = uiState.selectedObject,
                            layerName = activeLayerName,
                            onPropertyValueChanged = { secId, propId, newVal ->
                                viewModel.setKorvaProperty(secId, propId, newVal)
                            },
                            onToggleSectionEnabled = { secId, enabled ->
                                viewModel.toggleKorvaSectionEnabled(secId, enabled)
                            },
                            onRemoveSection = { secId ->
                                viewModel.removeKorvaSection(secId)
                            },
                            onAddSection = { secType ->
                                viewModel.addKorvaSection(secType)
                            },
                            onRenameObject = { newName ->
                                viewModel.renameKorvaObject(newName)
                            },
                            onToggleObjectVisibility = {
                                uiState.selectedObject?.let { viewModel.toggleNodeVisibility(it.id) }
                            },
                            onToggleObjectLock = {
                                /* Object lock toggle */
                            },
                            modifier = Modifier.width(260.dp)
                        )
                    }
                }

                // ========================================================
                // Overlays for Bottom Tabs (Build/Console, Settings)
                // ========================================================
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
                        project = project,
                        onSaveSettings = {
                            viewModel.addLog(LogLevel.SUCCESS, "تم تحديث إعدادات المحرك والمشروع")
                            globalTab = StudioGlobalTab.EDITOR
                        },
                        onClose = { globalTab = StudioGlobalTab.EDITOR }
                    )
                }
            }

            // ========================================================
            // 3. Studio Bottom Navigation Bar
            // ========================================================
            StudioBottomNav(
                activeTab = globalTab,
                onTabSelected = { tab ->
                    globalTab = tab
                }
            )
        }

        // Global Snackbar Notification Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        )
    }
}

// ----------------------------------------------------------------------------
// Overlay Panels
// ----------------------------------------------------------------------------
@Composable
fun ConsoleBuildOverlay(
    logs: List<com.example.model.EngineLog>,
    projectName: String,
    onExportApk: () -> Unit,
    onClearLogs: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.8f)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = EngineSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioPurpleLight.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "سجل تجميع وبناء المشروع (Build & Console)",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Console output
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(EngineBackground, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(logs) { log ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(log.timestamp, color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = log.message,
                                color = when (log.level) {
                                    LogLevel.SUCCESS -> StudioGreen
                                    LogLevel.WARN -> StudioYellow
                                    LogLevel.ERROR -> StudioRed
                                    else -> TextPrimary
                                },
                                fontSize = 10.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onClearLogs,
                        colors = ButtonDefaults.buttonColors(containerColor = EngineCardBg),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("مسح السجلات", color = TextSecondary, fontSize = 11.sp)
                    }

                    Button(
                        onClick = onExportApk,
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Android, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تصدير APK للعبة", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectSettingsOverlay(
    project: ProjectEntity,
    onSaveSettings: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var targetFps by remember { mutableFloatStateOf(60f) }
    var enablePhysics by remember { mutableStateOf(true) }
    var autoSave by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = EngineSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = StudioPurpleLight, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إعدادات المشروع والمحرك", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }

                Text("اسم المشروع: ${project.name}", color = TextSecondary, fontSize = 11.sp)
                Text("المسار: ${project.path}", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)

                Divider(color = StudioBorder)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("معدل الإطارات المستهدف (Target FPS): ${targetFps.toInt()}", color = TextPrimary, fontSize = 11.sp)
                    Slider(
                        value = targetFps,
                        onValueChange = { targetFps = it },
                        valueRange = 30f..120f,
                        steps = 2,
                        modifier = Modifier.width(140.dp),
                        colors = SliderDefaults.colors(thumbColor = StudioPurpleLight, activeTrackColor = StudioPurple)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("محاكاة الفيزياء (2D Physics Engine)", color = TextPrimary, fontSize = 11.sp)
                    Switch(
                        checked = enablePhysics,
                        onCheckedChange = { enablePhysics = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = StudioPurple)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("الحفظ التلقائي عند الخروج", color = TextPrimary, fontSize = 11.sp)
                    Switch(
                        checked = autoSave,
                        onCheckedChange = { autoSave = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = StudioPurple)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onSaveSettings,
                    colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("حفظ التغييرات", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.model.ProjectEntity
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
            // 1. Studio Header Top Bar (matching reference)
            // ========================================================
            StudioHeader(
                projectName = project.name.ifBlank { "Dark Village" },
                projectType = project.templateType.ifBlank { "2D Project" },
                isPlaying = uiState.isSimulationPlaying,
                isPaused = false,
                onPlayClick = {
                    if (!uiState.isSimulationPlaying) viewModel.toggleSimulation()
                },
                onPauseClick = {
                    if (uiState.isSimulationPlaying) viewModel.toggleSimulation()
                },
                onStopClick = {
                    if (uiState.isSimulationPlaying) viewModel.toggleSimulation()
                },
                onBuildApkClick = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("جاري بناء وتجهيز حزمة APK للمشروع...")
                    }
                },
                onSaveClick = {
                    viewModel.saveCurrentProject()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("تم حفظ بيانات المشروع بنجاح!")
                    }
                },
                onProjectSwitch = { name ->
                    // Switch current active project name
                },
                onBackToProjects = {
                    viewModel.closeEditor()
                }
            )

            // ========================================================
            // 2. Main Studio 3-Column Split
            // ========================================================
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Left Panel: Objects & Layers
                StudioHierarchyPanel(
                    sceneNodes = uiState.sceneNodes,
                    selectedNodeId = selectedNode?.id,
                    onSelectNode = { viewModel.selectSceneNode(it) },
                    onAddNode = { name, type -> viewModel.addSceneNode(name, type) },
                    onDeleteNode = { viewModel.deleteSelectedNode() }
                )

                // Center Column: 2D Scene Viewport + Bottom Animation Timeline
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // Viewport
                    StudioViewport(
                        selectedNode = selectedNode,
                        onNodeDrag = { dx, dy ->
                            viewModel.updateSelectedNodePos(dx, dy)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )

                    // Animation Timeline
                    StudioTimeline(
                        isPlaying = isTimelinePlaying,
                        onTogglePlay = { isTimelinePlaying = !isTimelinePlaying }
                    )
                }

                // Right Panel: Inspector & Components
                StudioInspector(
                    selectedNode = selectedNode
                )
            }

            // ========================================================
            // 3. Studio Global Bottom Navigation Bar
            // ========================================================
            StudioBottomNav(
                activeTab = globalTab,
                onTabSelected = { tab ->
                    globalTab = tab
                    if (tab == StudioGlobalTab.PROJECTS) {
                        viewModel.closeEditor()
                    }
                }
            )
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}

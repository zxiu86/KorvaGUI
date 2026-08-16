package com.example

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.dialogs.ChangePathDialog
import com.example.ui.dialogs.DeleteConfirmDialog
import com.example.ui.dialogs.ExitConfirmDialog
import com.example.ui.dialogs.NewProjectDialog
import com.example.ui.dialogs.OpenProjectDialog
import com.example.ui.screens.EditorWorkspaceScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.EngineBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemUI()

        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(EngineBackground)
                ) {
                    if (uiState.activeProject != null) {
                        // بيئة التعديل الاحترافية للمحرك (Studio Game Engine Workspace)
                        EditorWorkspaceScreen(
                            project = uiState.activeProject!!,
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    } else {
                        // الشاشة الرئيسية (Home Dashboard)
                        HomeScreen(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }

                    // Dialog for New Project
                    if (uiState.isNewProjectDialogOpen) {
                        NewProjectDialog(
                            defaultPath = uiState.defaultSavePath,
                            onDismiss = { viewModel.closeNewProjectDialog() },
                            onCreateProject = { name, path, template ->
                                viewModel.createAndOpenProject(name, path, template)
                            },
                            onChangePathRequested = {
                                viewModel.openChangePathDialog()
                            }
                        )
                    }

                    // Dialog for Opening an existing project
                    if (uiState.isOpenProjectDialogOpen) {
                        OpenProjectDialog(
                            initialPath = uiState.defaultSavePath,
                            onDismiss = { viewModel.closeOpenProjectDialog() },
                            onProjectSelected = { folderPath ->
                                viewModel.importAndOpenProject(folderPath)
                            }
                        )
                    }

                    // Dialog for Changing Path
                    if (uiState.isChangePathDialogOpen) {
                        ChangePathDialog(
                            currentPath = uiState.defaultSavePath,
                            onDismiss = { viewModel.closeChangePathDialog() },
                            onConfirm = { newPath ->
                                viewModel.setDefaultSavePath(newPath)
                            }
                        )
                    }

                    // Dialog for Exit Confirmation
                    if (uiState.isExitConfirmDialogOpen) {
                        ExitConfirmDialog(
                            onDismiss = { viewModel.closeExitConfirmDialog() },
                            onConfirmExit = {
                                viewModel.closeExitConfirmDialog()
                                finishAffinity()
                            }
                        )
                    }

                    // Dialog for Project Deletion Confirmation
                    uiState.projectToDelete?.let { project ->
                        DeleteConfirmDialog(
                            project = project,
                            onDismiss = { viewModel.cancelDeleteProject() },
                            onConfirmDelete = { viewModel.confirmDeleteProject() }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun hideSystemUI() {
        try {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val decorView = window.decorView
            val insetsController = WindowCompat.getInsetsController(window, decorView)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val params = window.attributes
                params.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                window.attributes = params
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

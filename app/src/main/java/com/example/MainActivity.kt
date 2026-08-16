package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(EngineBackground)
                        .safeDrawingPadding()
                ) {
                    if (uiState.activeProject != null) {
                        // بيئة التعديل (Editor Workspace)
                        EditorWorkspaceScreen(
                            project = uiState.activeProject!!,
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    } else {
                        // الشاشة الرئيسية الثابتة (Home Dashboard)
                        HomeScreen(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }

                    // Central Dialog for New Project (نافذة مشروع جديد المنبثقة)
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

                    // Dialog for Opening an existing project (نافذة تحرير مشروع محفوظ)
                    if (uiState.isOpenProjectDialogOpen) {
                        OpenProjectDialog(
                            initialPath = uiState.defaultSavePath,
                            onDismiss = { viewModel.closeOpenProjectDialog() },
                            onProjectSelected = { folderPath ->
                                viewModel.importAndOpenProject(folderPath)
                            }
                        )
                    }

                    // Dialog for Changing Path (نافذة تغيير مسار الحفظ)
                    if (uiState.isChangePathDialogOpen) {
                        ChangePathDialog(
                            currentPath = uiState.defaultSavePath,
                            onDismiss = { viewModel.closeChangePathDialog() },
                            onConfirm = { newPath ->
                                viewModel.setDefaultSavePath(newPath)
                            }
                        )
                    }

                    // Dialog for Exit Confirmation (نافذة تأكيد الخروج الصريح)
                    if (uiState.isExitConfirmDialogOpen) {
                        ExitConfirmDialog(
                            onDismiss = { viewModel.closeExitConfirmDialog() },
                            onConfirmExit = {
                                viewModel.closeExitConfirmDialog()
                                finishAffinity()
                            }
                        )
                    }

                    // Dialog for Project Deletion Confirmation (نافذة تأكيد الحذف)
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
}


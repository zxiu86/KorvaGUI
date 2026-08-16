package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.ProjectEntity
import com.example.data.repository.ProjectRepository
import com.example.model.EditorTab
import com.example.model.EngineLog
import com.example.model.LogLevel
import com.example.model.NodeType
import com.example.model.SceneNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MainUiState(
    val projects: List<ProjectEntity> = emptyList(),
    val filteredProjects: List<ProjectEntity> = emptyList(),
    val searchQuery: String = "",
    val defaultSavePath: String = "",
    val isNewProjectDialogOpen: Boolean = false,
    val isOpenProjectDialogOpen: Boolean = false,
    val isChangePathDialogOpen: Boolean = false,
    val isExitConfirmDialogOpen: Boolean = false,
    val projectToDelete: ProjectEntity? = null,
    val selectedProjectForDetails: ProjectEntity? = null,
    val activeProject: ProjectEntity? = null, // If non-null, editor workspace is open
    // Editor Workspace State
    val editorTab: EditorTab = EditorTab.SCENE_VIEW,
    val sceneNodes: List<SceneNode> = emptyList(),
    val selectedNodeId: String? = null,
    val isSimulationPlaying: Boolean = false,
    val engineLogs: List<EngineLog> = emptyList(),
    val editorFps: Int = 60,
    val isPhysicsEnabled: Boolean = true
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProjectRepository

    private val _searchQuery = MutableStateFlow("")
    private val _defaultSavePath = MutableStateFlow("")
    private val _isNewProjectDialogOpen = MutableStateFlow(false)
    private val _isOpenProjectDialogOpen = MutableStateFlow(false)
    private val _isChangePathDialogOpen = MutableStateFlow(false)
    private val _isExitConfirmDialogOpen = MutableStateFlow(false)
    private val _projectToDelete = MutableStateFlow<ProjectEntity?>(null)
    private val _selectedProjectForDetails = MutableStateFlow<ProjectEntity?>(null)
    private val _activeProject = MutableStateFlow<ProjectEntity?>(null)

    // Editor Workspace internal state
    private val _editorTab = MutableStateFlow(EditorTab.SCENE_VIEW)
    private val _sceneNodes = MutableStateFlow<List<SceneNode>>(emptyList())
    private val _selectedNodeId = MutableStateFlow<String?>(null)
    private val _isSimulationPlaying = MutableStateFlow(false)
    private val _engineLogs = MutableStateFlow<List<EngineLog>>(emptyList())
    private val _editorFps = MutableStateFlow(60)

    val uiState: StateFlow<MainUiState>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ProjectRepository(database.projectDao(), application)
        val defaultPath = repository.getDefaultProjectsDirectory()
        _defaultSavePath.value = defaultPath

        val projectsFlow = repository.allProjects

        uiState = combine(
            projectsFlow,
            _searchQuery,
            _defaultSavePath,
            _isNewProjectDialogOpen,
            _isOpenProjectDialogOpen,
            _isChangePathDialogOpen,
            _isExitConfirmDialogOpen,
            _projectToDelete,
            _selectedProjectForDetails,
            _activeProject,
            _editorTab,
            _sceneNodes,
            _selectedNodeId,
            _isSimulationPlaying,
            _engineLogs,
            _editorFps
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            val rawProjects = args[0] as List<ProjectEntity>
            val query = args[1] as String
            val savePath = args[2] as String
            val newProjectOpen = args[3] as Boolean
            val openProjectOpen = args[4] as Boolean
            val changePathOpen = args[5] as Boolean
            val exitConfirmOpen = args[6] as Boolean
            val toDelete = args[7] as ProjectEntity?
            val forDetails = args[8] as ProjectEntity?
            val currentActive = args[9] as ProjectEntity?
            val tab = args[10] as EditorTab
            val nodes = args[11] as List<SceneNode>
            val selectedNode = args[12] as String?
            val isPlaying = args[13] as Boolean
            val logs = args[14] as List<EngineLog>
            val fps = args[15] as Int

            val filtered = if (query.isBlank()) {
                rawProjects
            } else {
                rawProjects.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.path.contains(query, ignoreCase = true) ||
                            it.templateType.contains(query, ignoreCase = true)
                }
            }

            MainUiState(
                projects = rawProjects,
                filteredProjects = filtered,
                searchQuery = query,
                defaultSavePath = savePath,
                isNewProjectDialogOpen = newProjectOpen,
                isOpenProjectDialogOpen = openProjectOpen,
                isChangePathDialogOpen = changePathOpen,
                isExitConfirmDialogOpen = exitConfirmOpen,
                projectToDelete = toDelete,
                selectedProjectForDetails = forDetails,
                activeProject = currentActive,
                editorTab = tab,
                sceneNodes = nodes,
                selectedNodeId = selectedNode,
                isSimulationPlaying = isPlaying,
                engineLogs = logs,
                editorFps = fps
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MainUiState(defaultSavePath = defaultPath)
        )
    }

    // --- Dialog Controls ---
    fun openNewProjectDialog() {
        _isNewProjectDialogOpen.value = true
    }

    fun closeNewProjectDialog() {
        _isNewProjectDialogOpen.value = false
    }

    fun openOpenProjectDialog() {
        _isOpenProjectDialogOpen.value = true
    }

    fun closeOpenProjectDialog() {
        _isOpenProjectDialogOpen.value = false
    }

    fun openChangePathDialog() {
        _isChangePathDialogOpen.value = true
    }

    fun closeChangePathDialog() {
        _isChangePathDialogOpen.value = false
    }

    fun setDefaultSavePath(newPath: String) {
        if (newPath.isNotBlank()) {
            _defaultSavePath.value = newPath
        }
        _isChangePathDialogOpen.value = false
    }

    fun openExitConfirmDialog() {
        _isExitConfirmDialogOpen.value = true
    }

    fun closeExitConfirmDialog() {
        _isExitConfirmDialogOpen.value = false
    }

    fun promptDeleteProject(project: ProjectEntity) {
        _projectToDelete.value = project
    }

    fun cancelDeleteProject() {
        _projectToDelete.value = null
    }

    fun confirmDeleteProject() {
        _projectToDelete.value?.let { project ->
            viewModelScope.launch {
                repository.deleteProject(project)
                _projectToDelete.value = null
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun showProjectDetails(project: ProjectEntity?) {
        _selectedProjectForDetails.value = project
    }

    // --- Project Creation & Open Flow ---
    fun createAndOpenProject(name: String, path: String, template: String) {
        viewModelScope.launch {
            val created = repository.createNewProject(
                name = name,
                basePath = if (path.isBlank()) _defaultSavePath.value else path,
                templateType = template
            )
            _isNewProjectDialogOpen.value = false
            openProjectInEditor(created)
        }
    }

    fun importAndOpenProject(folderPath: String) {
        viewModelScope.launch {
            val project = repository.openExistingProjectFolder(folderPath)
            _isOpenProjectDialogOpen.value = false
            openProjectInEditor(project)
        }
    }

    fun openProjectInEditor(project: ProjectEntity) {
        _activeProject.value = project
        initEditorForProject(project)
    }

    fun closeEditor() {
        _activeProject.value = null
        _isSimulationPlaying.value = false
    }

    // --- Editor Workspace Logic ---
    private fun initEditorForProject(project: ProjectEntity) {
        val initialNodes = listOf(
            SceneNode(
                id = "node_cam",
                name = "Main Camera",
                type = NodeType.CAMERA,
                posX = 0f,
                posY = 0f,
                scale = 1f,
                colorHex = "#38BDF8"
            ),
            SceneNode(
                id = "node_light",
                name = "Directional Light 2D",
                type = NodeType.LIGHT,
                posX = 120f,
                posY = -100f,
                scale = 1.2f,
                colorHex = "#FBBF24"
            ),
            SceneNode(
                id = "node_player",
                name = "Player Character",
                type = NodeType.PLAYER,
                posX = 0f,
                posY = 50f,
                scale = 1.5f,
                colorHex = "#00E5C9",
                hasPhysics = true,
                mass = 1.2f
            ),
            SceneNode(
                id = "node_ground",
                name = "Floor Collider",
                type = NodeType.PLATFORM,
                posX = 0f,
                posY = 160f,
                scale = 4.0f,
                colorHex = "#64748B",
                hasPhysics = false
            ),
            SceneNode(
                id = "node_particles",
                name = "Cyber Emitter",
                type = NodeType.PARTICLE_SYSTEM,
                posX = -100f,
                posY = 20f,
                scale = 1f,
                colorHex = "#A855F7"
            )
        )
        _sceneNodes.value = initialNodes
        _selectedNodeId.value = "node_player"

        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _engineLogs.value = listOf(
            EngineLog(
                timestamp = timeStr,
                level = LogLevel.INFO,
                message = "تم تحميل محرك korva engine بنجاح (النواة v2.4.0)"
            ),
            EngineLog(
                timestamp = timeStr,
                level = LogLevel.SUCCESS,
                message = "تم فتح المشروع: ${project.name} من المسار ${project.path}"
            ),
            EngineLog(
                timestamp = timeStr,
                level = LogLevel.INFO,
                message = "تم إنشاء مسار الرندرة: 60 FPS Target - Shader Core Ready"
            )
        )
    }

    fun selectSceneNode(nodeId: String) {
        _selectedNodeId.value = nodeId
    }

    fun setEditorTab(tab: EditorTab) {
        _editorTab.value = tab
    }

    fun addSceneNode(name: String, type: NodeType) {
        val newNode = SceneNode(
            id = "node_${System.currentTimeMillis() % 100000}",
            name = name.ifBlank { "New Object" },
            type = type,
            posX = (Math.random() * 80 - 40).toFloat(),
            posY = (Math.random() * 80 - 40).toFloat(),
            scale = 1f,
            colorHex = when (type) {
                NodeType.PLAYER -> "#00E5C9"
                NodeType.ENEMY -> "#F43F5E"
                NodeType.PLATFORM -> "#64748B"
                NodeType.LIGHT -> "#FBBF24"
                NodeType.PARTICLE_SYSTEM -> "#A855F7"
                else -> "#38BDF8"
            }
        )
        _sceneNodes.update { it + newNode }
        _selectedNodeId.value = newNode.id
        addLog(LogLevel.INFO, "تمت إضافة عقدة جديدة: ${newNode.name}")
    }

    fun updateSelectedNodePos(dx: Float, dy: Float) {
        val currentId = _selectedNodeId.value ?: return
        _sceneNodes.update { list ->
            list.map { node ->
                if (node.id == currentId) {
                    node.copy(posX = node.posX + dx, posY = node.posY + dy)
                } else node
            }
        }
    }

    fun updateSelectedNodeScale(scaleMultiplier: Float) {
        val currentId = _selectedNodeId.value ?: return
        _sceneNodes.update { list ->
            list.map { node ->
                if (node.id == currentId) {
                    val newScale = (node.scale * scaleMultiplier).coerceIn(0.2f, 8.0f)
                    node.copy(scale = newScale)
                } else node
            }
        }
    }

    fun updateSelectedNodeRotation(dAngle: Float) {
        val currentId = _selectedNodeId.value ?: return
        _sceneNodes.update { list ->
            list.map { node ->
                if (node.id == currentId) {
                    node.copy(rotation = (node.rotation + dAngle) % 360f)
                } else node
            }
        }
    }

    fun deleteSelectedNode() {
        val currentId = _selectedNodeId.value ?: return
        val nodeName = _sceneNodes.value.find { it.id == currentId }?.name ?: ""
        _sceneNodes.update { it.filterNot { node -> node.id == currentId } }
        _selectedNodeId.value = _sceneNodes.value.firstOrNull()?.id
        addLog(LogLevel.WARN, "تم حذف الكائن: $nodeName")
    }

    fun toggleSimulation() {
        val newState = !_isSimulationPlaying.value
        _isSimulationPlaying.value = newState
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        if (newState) {
            addLog(LogLevel.SUCCESS, "▶ تم بدء تشغيل المحاكاة التفاعلية (Physics Simulation Running)")
        } else {
            addLog(LogLevel.INFO, "⏹ تم إيقاف المحاكاة والعودة لوضع التصميم")
        }
    }

    fun addLog(level: LogLevel, message: String) {
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _engineLogs.update {
            listOf(EngineLog(timestamp = timeStr, level = level, message = message)) + it.take(40)
        }
    }

    fun saveCurrentProject() {
        _activeProject.value?.let { project ->
            viewModelScope.launch {
                repository.updateProject(project)
                addLog(LogLevel.SUCCESS, "💾 تم حفظ ملفات المشروع ${project.name} بنجاح!")
            }
        }
    }
}

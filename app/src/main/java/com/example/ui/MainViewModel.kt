package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.ProjectEntity
import com.example.data.repository.ProjectRepository
import com.example.engine.backend.mock.*
import com.example.engine.interfaces.*
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
    // Korva Engine Interfaces
    val engineProject: IProject? = null,
    val activeScene: IScene? = null,
    val selectedObject: IObject? = null,
    val selectedLayerId: String? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
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

    // Mock Engine Instance
    val engine: MockKorvaEngine = MockKorvaEngine()
    val eventBus: IEventBus get() = engine.eventBus
    val commandHistory: ICommandHistory get() = engine.commandHistory

    private val _searchQuery = MutableStateFlow("")
    private val _defaultSavePath = MutableStateFlow("")
    private val _isNewProjectDialogOpen = MutableStateFlow(false)
    private val _isOpenProjectDialogOpen = MutableStateFlow(false)
    private val _isChangePathDialogOpen = MutableStateFlow(false)
    private val _isExitConfirmDialogOpen = MutableStateFlow(false)
    private val _projectToDelete = MutableStateFlow<ProjectEntity?>(null)
    private val _selectedProjectForDetails = MutableStateFlow<ProjectEntity?>(null)
    private val _activeProject = MutableStateFlow<ProjectEntity?>(null)

    // Korva Reactive State
    private val _selectedObjectId = MutableStateFlow<String?>("obj_player")
    private val _selectedLayerId = MutableStateFlow<String?>("layer_chars")
    private val _canUndo = MutableStateFlow(false)
    private val _canRedo = MutableStateFlow(false)
    private val _engineProjectRevision = MutableStateFlow(0) // increment on mutations to trigger reactive combine

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

        engine.initialize()

        viewModelScope.launch {
            repository.ensureDefaultProjects()
        }

        // Listen to EventBus for Engine decoupling
        viewModelScope.launch {
            engine.eventBus.events.collect { event ->
                when (event) {
                    is EngineEvent.PropertyChanged -> {
                        addLog(LogLevel.INFO, "تعديل خاصية [${event.propertyId}] في الكائن ${event.objectId}")
                        _engineProjectRevision.update { it + 1 }
                    }
                    is EngineEvent.ObjectCreated -> {
                        addLog(LogLevel.SUCCESS, "تم إنشاء كائن جديد: ${event.obj.name}")
                        _engineProjectRevision.update { it + 1 }
                    }
                    is EngineEvent.ObjectDeleted -> {
                        addLog(LogLevel.WARN, "تم حذف الكائن: ${event.objectId}")
                        _engineProjectRevision.update { it + 1 }
                    }
                    is EngineEvent.LayerCreated -> {
                        addLog(LogLevel.SUCCESS, "تم إنشاء طبقة جديدة: ${event.layer.name}")
                        _engineProjectRevision.update { it + 1 }
                    }
                    is EngineEvent.LayerDeleted -> {
                        addLog(LogLevel.WARN, "تم حذف طبقة: ${event.layerId}")
                        _engineProjectRevision.update { it + 1 }
                    }
                    is EngineEvent.SimulationStateChanged -> {
                        _isSimulationPlaying.value = event.isRunning
                    }
                    else -> {
                        _engineProjectRevision.update { it + 1 }
                    }
                }
            }
        }

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
            _editorFps,
            engine.currentProject,
            _selectedObjectId,
            _selectedLayerId,
            _engineProjectRevision
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
            val engProj = args[16] as IProject?
            val selObjId = args[17] as String?
            val selLayId = args[18] as String?

            val activeSc = engProj?.activeScene
            val currentObj = if (selObjId != null) activeSc?.findObject(selObjId) else null

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
                engineProject = engProj,
                activeScene = activeSc,
                selectedObject = currentObj,
                selectedLayerId = selLayId,
                canUndo = commandHistory.canUndo,
                canRedo = commandHistory.canRedo,
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

    // --- Korva Engine Command & Action Handlers ---

    fun executeEngineCommand(command: ICommand) {
        commandHistory.execute(command)
        _canUndo.value = commandHistory.canUndo
        _canRedo.value = commandHistory.canRedo
        _engineProjectRevision.update { it + 1 }
    }

    fun undo() {
        if (commandHistory.canUndo) {
            commandHistory.undo()
            _canUndo.value = commandHistory.canUndo
            _canRedo.value = commandHistory.canRedo
            _engineProjectRevision.update { it + 1 }
            addLog(LogLevel.INFO, "↩ تراجع (Undo)")
        }
    }

    fun redo() {
        if (commandHistory.canRedo) {
            commandHistory.redo()
            _canUndo.value = commandHistory.canUndo
            _canRedo.value = commandHistory.canRedo
            _engineProjectRevision.update { it + 1 }
            addLog(LogLevel.INFO, "↪ إعادة (Redo)")
        }
    }

    fun selectKorvaObject(objectId: String) {
        _selectedObjectId.value = objectId
        val obj = engine.currentProject.value?.activeScene?.findObject(objectId)
        if (obj != null) {
            _selectedLayerId.value = obj.layerId
            _selectedNodeId.value = objectId
        }
    }

    fun selectKorvaLayer(layerId: String) {
        _selectedLayerId.value = layerId
    }

    fun setKorvaProperty(sectionId: String, propertyId: String, newValue: PropertyValue) {
        val obj = uiState.value.selectedObject ?: return
        val cmd = SetPropertyCommand(obj, sectionId, propertyId, newValue, eventBus)
        executeEngineCommand(cmd)

        // Real-time synchronization with SceneNode in Viewport
        val selectedId = obj.id
        _sceneNodes.update { list ->
            list.map { node ->
                if (node.id == selectedId) {
                    when {
                        propertyId.contains("pos", true) && newValue is PropertyValue.Vector2Value ->
                            node.copy(posX = newValue.x, posY = newValue.y)
                        propertyId.contains("pos", true) && newValue is PropertyValue.Vector2iValue ->
                            node.copy(posX = newValue.x.toFloat(), posY = newValue.y.toFloat())
                        propertyId.contains("rot", true) && newValue is PropertyValue.FloatValue ->
                            node.copy(rotation = newValue.value)
                        propertyId.contains("scale", true) && newValue is PropertyValue.Vector2Value ->
                            node.copy(scale = newValue.x)
                        propertyId.contains("scale", true) && newValue is PropertyValue.FloatValue ->
                            node.copy(scale = newValue.value)
                        (propertyId.contains("tint", true) || propertyId.contains("color", true)) && newValue is PropertyValue.ColorValue ->
                            node.copy(colorHex = newValue.hex)
                        propertyId.contains("mass", true) && newValue is PropertyValue.FloatValue ->
                            node.copy(mass = newValue.value, hasPhysics = newValue.value > 0f)
                        propertyId.contains("gravity", true) && newValue is PropertyValue.BoolValue ->
                            node.copy(hasPhysics = newValue.value)
                        propertyId.contains("body_type", true) && newValue is PropertyValue.EnumValue ->
                            node.copy(hasPhysics = newValue.selected.equals("Dynamic", true))
                        else -> node
                    }
                } else node
            }
        }
        _engineProjectRevision.update { it + 1 }
    }

    fun toggleKorvaSectionEnabled(sectionId: String, enabled: Boolean) {
        val obj = uiState.value.selectedObject ?: return
        val sec = obj.getSection(sectionId) ?: return
        sec.toggleEnabled(enabled)
        _engineProjectRevision.update { it + 1 }
    }

    fun removeKorvaSection(sectionId: String) {
        val obj = uiState.value.selectedObject ?: return
        val cmd = RemoveSectionCommand(obj, sectionId, eventBus)
        executeEngineCommand(cmd)
    }

    fun addKorvaSection(sectionType: String) {
        val obj = uiState.value.selectedObject ?: return
        val newSection = when (sectionType.lowercase()) {
            "sprite" -> MockSection("sec_sprite_${System.currentTimeMillis() % 1000}", "Sprite", "image").apply {
                addProperty(MockProperty("tex", "Texture", PropertyType.TEXTURE, PropertyValue.TextureValue("new_sprite.png")))
                addProperty(MockProperty("opacity", "Opacity", PropertyType.FLOAT, PropertyValue.FloatValue(1.0f), min = 0f, max = 1f))
            }
            "physics" -> MockSection("sec_phys_${System.currentTimeMillis() % 1000}", "Physics", "science").apply {
                addProperty(MockProperty("body_type", "Body Type", PropertyType.ENUM, PropertyValue.EnumValue("Dynamic", listOf("Dynamic", "Static", "Kinematic"))))
                addProperty(MockProperty("mass", "Mass", PropertyType.FLOAT, PropertyValue.FloatValue(1.0f), min = 0.1f, max = 50f))
                addProperty(MockProperty("gravity", "Gravity", PropertyType.BOOL, PropertyValue.BoolValue(true)))
            }
            "animation" -> MockSection("sec_anim_${System.currentTimeMillis() % 1000}", "Animation", "movie").apply {
                addProperty(MockProperty("clip", "Clip", PropertyType.ENUM, PropertyValue.EnumValue("Idle", listOf("Idle", "Run", "Jump"))))
                addProperty(MockProperty("speed", "Speed", PropertyType.FLOAT, PropertyValue.FloatValue(1.0f), min = 0.1f, max = 3f))
            }
            "logic (brain)", "logic", "brain" -> MockSection("sec_logic_${System.currentTimeMillis() % 1000}", "Logic (Brain)", "psychology").apply {
                addProperty(MockProperty("script", "Script", PropertyType.STRING, PropertyValue.StringValue("behavior.korva")))
                addProperty(MockProperty("event", "Event", PropertyType.ENUM, PropertyValue.EnumValue("OnStart", listOf("OnStart", "OnUpdate", "OnCollision"))))
            }
            "light 2d", "light" -> MockSection("sec_light_${System.currentTimeMillis() % 1000}", "Light 2D", "lightbulb").apply {
                addProperty(MockProperty("radius", "Radius", PropertyType.FLOAT, PropertyValue.FloatValue(150f), min = 10f, max = 500f))
                addProperty(MockProperty("color", "Color", PropertyType.COLOR, PropertyValue.ColorValue("#FBBF24")))
            }
            "camera 2d", "camera" -> MockSection("sec_cam_${System.currentTimeMillis() % 1000}", "Camera 2D", "videocam").apply {
                addProperty(MockProperty("zoom", "Zoom", PropertyType.FLOAT, PropertyValue.FloatValue(1.0f), min = 0.2f, max = 4f))
            }
            "particles 2d", "particles" -> MockSection("sec_part_${System.currentTimeMillis() % 1000}", "Particles 2D", "auto_awesome").apply {
                addProperty(MockProperty("rate", "Rate", PropertyType.INT, PropertyValue.IntValue(30), min = 1f, max = 100f))
                addProperty(MockProperty("color", "Color", PropertyType.COLOR, PropertyValue.ColorValue("#8B5CF6")))
            }
            "audio source", "audio" -> MockSection("sec_aud_${System.currentTimeMillis() % 1000}", "Audio Source", "volume_up").apply {
                addProperty(MockProperty("clip", "Audio Clip", PropertyType.AUDIO, PropertyValue.AudioValue("sfx.wav")))
                addProperty(MockProperty("volume", "Volume", PropertyType.FLOAT, PropertyValue.FloatValue(0.8f), min = 0f, max = 1f))
            }
            else -> MockSection("sec_custom_${System.currentTimeMillis() % 1000}", sectionType, "layers").apply {
                addProperty(MockProperty("enabled", "Enabled", PropertyType.BOOL, PropertyValue.BoolValue(true)))
            }
        }
        val cmd = AddSectionCommand(obj, newSection, eventBus)
        executeEngineCommand(cmd)
    }

    fun createKorvaObject(name: String, layerId: String) {
        val scene = engine.currentProject.value?.activeScene ?: return
        val targetLayerId = layerId.ifBlank { scene.layers.firstOrNull()?.id ?: return }
        val cmd = CreateObjectCommand(scene, name, targetLayerId, eventBus)
        executeEngineCommand(cmd)
        addSceneNode(name, NodeType.SPRITE_OBJECT)
    }

    fun deleteKorvaObject(objectId: String) {
        val scene = engine.currentProject.value?.activeScene ?: return
        val cmd = DeleteObjectCommand(scene, objectId, eventBus)
        executeEngineCommand(cmd)
        deleteNodeById(objectId)
    }

    fun renameKorvaObject(newName: String) {
        val obj = uiState.value.selectedObject ?: return
        val cmd = RenameObjectCommand(obj, newName, eventBus)
        executeEngineCommand(cmd)
        setSelectedNodeName(newName)
    }

    fun createKorvaLayer(name: String) {
        val scene = engine.currentProject.value?.activeScene ?: return
        val cmd = CreateLayerCommand(scene, name, eventBus)
        executeEngineCommand(cmd)
    }

    fun deleteKorvaLayer(layerId: String) {
        val scene = engine.currentProject.value?.activeScene ?: return
        val cmd = DeleteLayerCommand(scene, layerId, eventBus)
        executeEngineCommand(cmd)
    }

    fun toggleKorvaLayerVisibility(layerId: String) {
        val layer = engine.currentProject.value?.activeScene?.getLayer(layerId) ?: return
        layer.isVisible = !layer.isVisible
        _engineProjectRevision.update { it + 1 }
    }

    fun toggleKorvaLayerLock(layerId: String) {
        val layer = engine.currentProject.value?.activeScene?.getLayer(layerId) ?: return
        layer.isLocked = !layer.isLocked
        _engineProjectRevision.update { it + 1 }
    }

    fun moveKorvaLayerUp(layerId: String) {
        val scene = engine.currentProject.value?.activeScene ?: return
        val layers = scene.layers.toMutableList()
        val index = layers.indexOfFirst { it.id == layerId }
        if (index > 0) {
            val temp = layers[index]
            layers[index] = layers[index - 1]
            layers[index - 1] = temp
            scene.reorderLayers(layers.map { it.id })
            _engineProjectRevision.update { it + 1 }
        }
    }

    fun moveKorvaLayerDown(layerId: String) {
        val scene = engine.currentProject.value?.activeScene ?: return
        val layers = scene.layers.toMutableList()
        val index = layers.indexOfFirst { it.id == layerId }
        if (index >= 0 && index < layers.size - 1) {
            val temp = layers[index]
            layers[index] = layers[index + 1]
            layers[index + 1] = temp
            scene.reorderLayers(layers.map { it.id })
            _engineProjectRevision.update { it + 1 }
        }
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
            engine.createProject(name, created.path, template)
            openProjectInEditor(created)
        }
    }

    fun importAndOpenProject(folderPath: String) {
        viewModelScope.launch {
            val project = repository.openExistingProjectFolder(folderPath)
            _isOpenProjectDialogOpen.value = false
            engine.loadProject(folderPath)
            openProjectInEditor(project)
        }
    }

    fun openProjectInEditor(project: ProjectEntity) {
        _activeProject.value = project
        engine.loadProject(project.path)
        initEditorForProject(project)
    }

    fun closeEditor() {
        _activeProject.value = null
        _isSimulationPlaying.value = false
        engine.stopSimulation()
    }

    // --- Editor Workspace Logic ---
    private fun initEditorForProject(project: ProjectEntity) {
        val initialNodes = listOf(
            SceneNode(
                id = "obj_player",
                name = "Player",
                type = NodeType.PLAYER,
                posX = -60f,
                posY = 60f,
                scale = 1.2f,
                colorHex = "#8B5CF6",
                hasPhysics = true,
                mass = 1.0f
            ),
            SceneNode(
                id = "obj_enemy",
                name = "Enemy_Goblin",
                type = NodeType.ENEMY,
                posX = 80f,
                posY = 70f,
                scale = 1.0f,
                colorHex = "#EF4444",
                hasPhysics = true,
                mass = 1.2f
            ),
            SceneNode(
                id = "obj_tree",
                name = "CyberTree_A",
                type = NodeType.SPRITE_OBJECT,
                posX = -140f,
                posY = 70f,
                scale = 1.2f,
                colorHex = "#22C55E",
                hasPhysics = false
            ),
            SceneNode(
                id = "obj_ground",
                name = "Ground_Platform",
                type = NodeType.SPRITE_OBJECT,
                posX = 0f,
                posY = 120f,
                scale = 3.0f,
                colorHex = "#22C55E",
                hasPhysics = false
            ),
            SceneNode(
                id = "obj_camera",
                name = "MainCamera",
                type = NodeType.CAMERA,
                posX = 0f,
                posY = 0f,
                scale = 1.0f,
                colorHex = "#38BDF8"
            ),
            SceneNode(
                id = "obj_torch",
                name = "PointLight_Torch",
                type = NodeType.LIGHT,
                posX = -140f,
                posY = 20f,
                scale = 1.0f,
                colorHex = "#FBBF24"
            )
        )
        _sceneNodes.value = initialNodes
        _selectedNodeId.value = "obj_player"
        _selectedObjectId.value = "obj_player"
        _selectedLayerId.value = "layer_chars"

        addLog(LogLevel.INFO, "🎮 تم تحميل مشروع Korva بنجاح: ${project.name}")
        addLog(LogLevel.SUCCESS, "✓ تم تهيئة واجهة المشروع والمشاهد والطبقات بنجاح!")
    }

    fun switchActiveProjectByName(name: String) {
        viewModelScope.launch {
            val proj = repository.allProjects
            val found = uiState.value.projects.find { it.name == name }
            if (found != null) {
                openProjectInEditor(found)
            }
        }
    }

    fun selectSceneNode(nodeId: String) {
        _selectedNodeId.value = nodeId
        _selectedObjectId.value = nodeId
    }

    fun addSceneNode(name: String, type: NodeType) {
        val newNode = SceneNode(
            id = "obj_${System.currentTimeMillis() % 100000}",
            name = name,
            type = type,
            posX = 0f,
            posY = 0f,
            scale = 1.0f,
            colorHex = when (type) {
                NodeType.PLAYER -> "#8B5CF6"
                NodeType.ENEMY -> "#EF4444"
                NodeType.LIGHT -> "#FBBF24"
                NodeType.CAMERA -> "#38BDF8"
                NodeType.PARTICLE_SYSTEM -> "#EC4899"
                else -> "#22C55E"
            }
        )
        _sceneNodes.update { it + newNode }
        _selectedNodeId.value = newNode.id
        _selectedObjectId.value = newNode.id
        addLog(LogLevel.INFO, "تمت إضافة الكائن ${newNode.name} إلى المشهد")
    }

    fun deleteSelectedNode() {
        val selectedId = _selectedNodeId.value ?: return
        deleteNodeById(selectedId)
    }

    fun updateSelectedNodePos(dx: Float, dy: Float) {
        val selectedId = _selectedNodeId.value ?: return
        var newX = 0f
        var newY = 0f
        _sceneNodes.update { list ->
            list.map { node ->
                if (node.id == selectedId) {
                    newX = node.posX + dx
                    newY = node.posY + dy
                    node.copy(posX = newX, posY = newY)
                } else node
            }
        }
        val obj = uiState.value.selectedObject
        if (obj != null && obj.id == selectedId) {
            val transformSec = obj.sections.find { it.name.equals("Transform", true) || it.id.contains("transform", true) }
            val posProp = transformSec?.properties?.find { it.id.contains("pos", true) }
            if (posProp?.value is PropertyValue.Vector2iValue) {
                posProp.setValue(PropertyValue.Vector2iValue(newX.toInt(), newY.toInt()))
            } else {
                posProp?.setValue(PropertyValue.Vector2Value(newX, newY))
            }
            _engineProjectRevision.update { it + 1 }
        }
    }

    fun setSelectedNodeExactPos(x: Float, y: Float) {
        val selectedId = _selectedNodeId.value ?: return
        _sceneNodes.update { list ->
            list.map { node ->
                if (node.id == selectedId) {
                    node.copy(posX = x, posY = y)
                } else node
            }
        }
        val obj = uiState.value.selectedObject
        if (obj != null && obj.id == selectedId) {
            val transformSec = obj.sections.find { it.name.equals("Transform", true) || it.id.contains("transform", true) }
            val posProp = transformSec?.properties?.find { it.id.contains("pos", true) }
            if (posProp?.value is PropertyValue.Vector2iValue) {
                posProp.setValue(PropertyValue.Vector2iValue(x.toInt(), y.toInt()))
            } else {
                posProp?.setValue(PropertyValue.Vector2Value(x, y))
            }
            _engineProjectRevision.update { it + 1 }
        }
    }

    fun setSelectedNodeExactScale(scale: Float) {
        val selectedId = _selectedNodeId.value ?: return
        val clamped = scale.coerceIn(0.1f, 10f)
        _sceneNodes.update { list ->
            list.map { node ->
                if (node.id == selectedId) {
                    node.copy(scale = clamped)
                } else node
            }
        }
        val obj = uiState.value.selectedObject
        if (obj != null && obj.id == selectedId) {
            val transformSec = obj.sections.find { it.name.equals("Transform", true) || it.id.contains("transform", true) }
            val scaleProp = transformSec?.properties?.find { it.id.contains("scale", true) }
            scaleProp?.setValue(PropertyValue.Vector2Value(clamped, clamped))
            _engineProjectRevision.update { it + 1 }
        }
    }

    fun setSelectedNodeExactRotation(rotation: Float) {
        val selectedId = _selectedNodeId.value ?: return
        _sceneNodes.update { list ->
            list.map { node ->
                if (node.id == selectedId) {
                    node.copy(rotation = rotation)
                } else node
            }
        }
        val obj = uiState.value.selectedObject
        if (obj != null && obj.id == selectedId) {
            val transformSec = obj.sections.find { it.name.equals("Transform", true) || it.id.contains("transform", true) }
            val rotProp = transformSec?.properties?.find { it.id.contains("rot", true) }
            rotProp?.setValue(PropertyValue.FloatValue(rotation))
            _engineProjectRevision.update { it + 1 }
        }
    }

    fun setSelectedNodeName(name: String) {
        val selectedId = _selectedNodeId.value ?: return
        _sceneNodes.update { list ->
            list.map { node ->
                if (node.id == selectedId) {
                    node.copy(name = name)
                } else node
            }
        }
    }

    fun setSelectedNodeColor(colorHex: String) {
        val selectedId = _selectedNodeId.value ?: return
        _sceneNodes.update { list ->
            list.map { node ->
                if (node.id == selectedId) {
                    node.copy(colorHex = colorHex)
                } else node
            }
        }
    }

    fun setSelectedNodePhysics(enabled: Boolean, mass: Float) {
        val selectedId = _selectedNodeId.value ?: return
        _sceneNodes.update { list ->
            list.map { node ->
                if (node.id == selectedId) {
                    node.copy(hasPhysics = enabled, mass = mass)
                } else node
            }
        }
    }

    fun deleteNodeById(nodeId: String) {
        val nodeName = _sceneNodes.value.find { it.id == nodeId }?.name ?: nodeId
        _sceneNodes.update { it.filterNot { node -> node.id == nodeId } }
        if (_selectedNodeId.value == nodeId) {
            _selectedNodeId.value = _sceneNodes.value.firstOrNull()?.id
            _selectedObjectId.value = _selectedNodeId.value
        }
        addLog(LogLevel.WARN, "تم حذف الكائن: $nodeName")
    }

    fun duplicateNode(nodeId: String) {
        val original = _sceneNodes.value.find { it.id == nodeId } ?: return
        val duplicated = original.copy(
            id = "obj_${System.currentTimeMillis() % 100000}",
            name = "${original.name}_Copy",
            posX = original.posX + 30f,
            posY = original.posY + 30f
        )
        _sceneNodes.update { it + duplicated }
        _selectedNodeId.value = duplicated.id
        _selectedObjectId.value = duplicated.id
        addLog(LogLevel.SUCCESS, "تم استنساخ الكائن: ${duplicated.name}")
    }

    fun bringNodeToFront(nodeId: String) {
        val node = _sceneNodes.value.find { it.id == nodeId } ?: return
        _sceneNodes.update { list ->
            list.filterNot { it.id == nodeId } + node
        }
        addLog(LogLevel.INFO, "تم رفع الكائن للواجهة: ${node.name}")
    }

    fun sendNodeToBack(nodeId: String) {
        val node = _sceneNodes.value.find { it.id == nodeId } ?: return
        _sceneNodes.update { list ->
            listOf(node) + list.filterNot { it.id == nodeId }
        }
        addLog(LogLevel.INFO, "تم إرسال الكائن للخلف: ${node.name}")
    }

    fun centerNode(nodeId: String) {
        _sceneNodes.update { list ->
            list.map { node ->
                if (node.id == nodeId) {
                    node.copy(posX = 0f, posY = 0f)
                } else node
            }
        }
        addLog(LogLevel.INFO, "تم وضع الكائن في نقطة المركز (0,0)")
    }

    fun toggleNodePhysics(nodeId: String) {
        _sceneNodes.update { list ->
            list.map { node ->
                if (node.id == nodeId) {
                    node.copy(hasPhysics = !node.hasPhysics)
                } else node
            }
        }
    }

    fun toggleNodeVisibility(nodeId: String) {
        _sceneNodes.update { list ->
            list.map { node ->
                if (node.id == nodeId) {
                    node.copy(isVisible = !node.isVisible)
                } else node
            }
        }
    }

    fun toggleSimulation() {
        val newState = !_isSimulationPlaying.value
        _isSimulationPlaying.value = newState
        if (newState) {
            engine.startSimulation()
            addLog(LogLevel.SUCCESS, "▶ تم بدء تشغيل المحاكاة التفاعلية (Physics Simulation Running)")
        } else {
            engine.pauseSimulation()
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

package com.example.engine.backend.mock

import com.example.engine.interfaces.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class MockKorvaEngine(
    val eventBus: IEventBus = MockEventBus(),
    val commandHistory: ICommandHistory = MockCommandHistory(eventBus)
) : IEngine {

    private val _currentProject = MutableStateFlow<IProject?>(null)
    override val currentProject: StateFlow<IProject?> = _currentProject.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    override val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    override fun initialize() {
        // Create default mock project if none loaded
        if (_currentProject.value == null) {
            val defaultProj = createSampleMockProject("CyberBlade_2D", "/storage/emulated/0/KorvaProjects/CyberBlade_2D")
            _currentProject.value = defaultProj
        }
    }

    override fun loadProject(path: String): Boolean {
        val projName = path.substringAfterLast("/").ifBlank { "Korva_Project" }
        val loaded = createSampleMockProject(projName, path)
        _currentProject.value = loaded
        eventBus.post(EngineEvent.ProjectLoaded(loaded))
        return true
    }

    override fun createProject(name: String, path: String, template: String): IProject {
        val proj = createSampleMockProject(name, path)
        _currentProject.value = proj
        eventBus.post(EngineEvent.ProjectLoaded(proj))
        return proj
    }

    override fun closeProject() {
        val pid = _currentProject.value?.id ?: ""
        _currentProject.value = null
        commandHistory.clear()
        eventBus.post(EngineEvent.ProjectClosed(pid))
    }

    override fun startSimulation() {
        _isRunning.value = true
        eventBus.post(EngineEvent.SimulationStateChanged(true))
    }

    override fun pauseSimulation() {
        _isRunning.value = false
        eventBus.post(EngineEvent.SimulationStateChanged(false))
    }

    override fun stopSimulation() {
        _isRunning.value = false
        eventBus.post(EngineEvent.SimulationStateChanged(false))
    }

    // =========================================================================
    // Factory method to assemble realistic Mock Project structure
    // Project -> Scenes -> Layers -> Objects -> Sections -> Properties
    // =========================================================================
    fun createSampleMockProject(name: String, rootPath: String): MockProject {
        val project = MockProject(
            id = "proj_${UUID.randomUUID().toString().take(6)}",
            name = name,
            rootPath = rootPath
        )

        // 1. Assets
        project.addAsset(MockAsset("ast_tex_player", "hero_knight.png", AssetType.TEXTURE, "textures/hero_knight.png", 2048 * 1024, mapOf("resolution" to "1024x1024", "format" to "PNG", "channels" to "RGBA8")))
        project.addAsset(MockAsset("ast_tex_tiles", "world_tileset.png", AssetType.TEXTURE, "textures/world_tileset.png", 4096 * 1024, mapOf("resolution" to "2048x2048", "format" to "PNG", "channels" to "RGBA8")))
        project.addAsset(MockAsset("ast_tex_enemy", "goblin_sprite.png", AssetType.TEXTURE, "textures/goblin_sprite.png", 1024 * 512, mapOf("resolution" to "512x512", "format" to "PNG", "channels" to "RGBA8")))
        project.addAsset(MockAsset("ast_aud_jump", "jump_sound.wav", AssetType.AUDIO, "audio/jump_sound.wav", 128 * 1024, mapOf("duration" to "0.45s", "sampleRate" to "44100Hz")))
        project.addAsset(MockAsset("ast_aud_bgm", "cyber_theme.ogg", AssetType.AUDIO, "audio/cyber_theme.ogg", 2500 * 1024, mapOf("duration" to "2m 14s", "bitrate" to "192kbps")))
        project.addAsset(MockAsset("ast_scr_player", "player_controller.korva", AssetType.SCRIPT, "scripts/player_controller.korva", 12 * 1024, mapOf("lines" to "184", "type" to "KorvaScript")))
        project.addAsset(MockAsset("ast_scr_enemy", "ai_patrol.korva", AssetType.SCRIPT, "scripts/ai_patrol.korva", 8 * 1024, mapOf("lines" to "92", "type" to "KorvaScript")))

        // 2. Main Scene
        val mainScene = project.createScene("MainScene")

        // Layers: Background -> World -> Characters -> Effects -> UI
        val layerBg = mainScene.createLayer("Background")
        val layerWorld = mainScene.createLayer("World")
        val layerChars = mainScene.createLayer("Characters")
        val layerEffects = mainScene.createLayer("Effects")
        val layerUI = mainScene.createLayer("UI")

        // 3. Populate Objects
        // Background: Sky & Mountains
        val skyObj = MockObject("obj_sky", "SkyGradient", layerBg.id)
        skyObj.addSection(createTransformSection(0f, -120f, 0f, 4f, 2f))
        skyObj.addSection(createSpriteSection("sky_gradient.png", 0.9f, false, false, "#1E1035"))
        layerBg.addObject(skyObj)

        val mountObj = MockObject("obj_mountains", "DistantMountains", layerBg.id)
        mountObj.addSection(createTransformSection(0f, -40f, 0f, 2.5f, 1.5f))
        mountObj.addSection(createSpriteSection("mountains_parallax.png", 0.7f, false, false, "#3B1E6D"))
        layerBg.addObject(mountObj)

        // World: Ground Platform & Trees
        val groundObj = MockObject("obj_ground", "Ground_Platform", layerWorld.id)
        groundObj.addSection(createTransformSection(0f, 120f, 0f, 3f, 1f))
        groundObj.addSection(createSpriteSection("world_tileset.png", 1.0f, false, false, "#22C55E"))
        groundObj.addSection(createPhysicsSection(bodyType = "Static", mass = 0f, gravity = false, friction = 0.8f, bounciness = 0.0f))
        layerWorld.addObject(groundObj)

        val treeObj = MockObject("obj_tree", "CyberTree_A", layerWorld.id)
        treeObj.addSection(createTransformSection(-140f, 70f, 0f, 1.2f, 1.2f))
        treeObj.addSection(createSpriteSection("tree_green.png", 1.0f, false, false, "#A855F7"))
        layerWorld.addObject(treeObj)

        // Characters: Player, Enemy & Camera
        val playerObj = MockObject("obj_player", "Player", layerChars.id)
        playerObj.addSection(createTransformSection(-60f, 60f, 0f, 1.2f, 1.2f))
        playerObj.addSection(createSpriteSection("hero_knight.png", 1.0f, flipX = false, flipY = false, tint = "#FFFFFF"))
        playerObj.addSection(createPhysicsSection(bodyType = "Dynamic", mass = 1.0f, gravity = true, friction = 0.4f, bounciness = 0.1f))
        playerObj.addSection(createAnimationSection(currentClip = "Idle", speed = 1.0f, isLooping = true, clips = listOf("Idle", "Run", "Jump", "Attack", "Hurt")))
        playerObj.addSection(createLogicBrainSection(script = "player_controller.korva", events = listOf("OnStart", "OnUpdate", "OnCollision", "OnJump", "OnDeath")))
        playerObj.addSection(createAudioSection(clip = "jump_sound.wav", volume = 0.85f, playOnAwake = false))
        layerChars.addObject(playerObj)

        val enemyObj = MockObject("obj_enemy", "Enemy_Goblin", layerChars.id)
        enemyObj.addSection(createTransformSection(80f, 70f, 0f, 1.0f, 1.0f))
        enemyObj.addSection(createSpriteSection("goblin_sprite.png", 1.0f, flipX = true, flipY = false, tint = "#EF4444"))
        enemyObj.addSection(createPhysicsSection(bodyType = "Dynamic", mass = 1.2f, gravity = true, friction = 0.5f, bounciness = 0.0f))
        enemyObj.addSection(createAnimationSection(currentClip = "Patrol", speed = 0.8f, isLooping = true, clips = listOf("Patrol", "Chase", "Attack", "Death")))
        enemyObj.addSection(createLogicBrainSection(script = "ai_patrol.korva", events = listOf("OnStart", "OnUpdate", "OnPlayerDetected", "OnDamage")))
        layerChars.addObject(enemyObj)

        val cameraObj = MockObject("obj_camera", "MainCamera", layerChars.id)
        cameraObj.addSection(createTransformSection(0f, 0f, 0f, 1f, 1f))
        cameraObj.addSection(createCameraSection(zoom = 1.0f, isOrthographic = true, followTarget = "Player", smoothSpeed = 5.0f))
        layerChars.addObject(cameraObj)

        // Effects: Torch Light & Particles
        val torchObj = MockObject("obj_torch", "PointLight_Torch", layerEffects.id)
        torchObj.addSection(createTransformSection(-140f, 20f, 0f, 1f, 1f))
        torchObj.addSection(createLightSection(radius = 180f, intensity = 1.5f, color = "#FBBF24"))
        layerEffects.addObject(torchObj)

        val particleObj = MockObject("obj_particles", "SparkEmitter", layerEffects.id)
        particleObj.addSection(createTransformSection(-60f, 80f, 0f, 1f, 1f))
        particleObj.addSection(createParticleSection(rate = 24, lifetime = 1.5f, speed = 80f, color = "#8B5CF6"))
        layerEffects.addObject(particleObj)

        // UI: HealthBar & CoinCounter
        val healthBarObj = MockObject("obj_healthbar", "HealthBar_UI", layerUI.id)
        healthBarObj.addSection(createTransformSection(-180f, -140f, 0f, 1f, 1f))
        healthBarObj.addSection(createUISection(width = 160, height = 24, anchor = "TopLeft", fillPercent = 0.85f))
        layerUI.addObject(healthBarObj)

        return project
    }

    // -------------------------------------------------------------------------
    // Helper Section Builders
    // -------------------------------------------------------------------------
    private fun createTransformSection(x: Float, y: Float, rot: Float, scaleX: Float, scaleY: Float): ISection {
        val sec = MockSection("sec_transform", "Transform", "open_with", isRemovable = false)
        sec.addProperty(MockProperty("pos", "Position", PropertyType.VECTOR2, PropertyValue.Vector2Value(x, y)))
        sec.addProperty(MockProperty("rot", "Rotation", PropertyType.FLOAT, PropertyValue.FloatValue(rot), min = 0f, max = 360f))
        sec.addProperty(MockProperty("scale", "Scale", PropertyType.VECTOR2, PropertyValue.Vector2Value(scaleX, scaleY), min = 0.1f, max = 10f))
        return sec
    }

    private fun createSpriteSection(texture: String, opacity: Float, flipX: Boolean, flipY: Boolean, tint: String): ISection {
        val sec = MockSection("sec_sprite", "Sprite", "image", isRemovable = true)
        sec.addProperty(MockProperty("tex", "Texture", PropertyType.TEXTURE, PropertyValue.TextureValue(texture)))
        sec.addProperty(MockProperty("flip_x", "Flip X", PropertyType.BOOL, PropertyValue.BoolValue(flipX)))
        sec.addProperty(MockProperty("flip_y", "Flip Y", PropertyType.BOOL, PropertyValue.BoolValue(flipY)))
        sec.addProperty(MockProperty("opacity", "Opacity", PropertyType.FLOAT, PropertyValue.FloatValue(opacity), min = 0f, max = 1f, step = 0.05f))
        sec.addProperty(MockProperty("tint", "Tint Color", PropertyType.COLOR, PropertyValue.ColorValue(tint)))
        return sec
    }

    private fun createPhysicsSection(bodyType: String, mass: Float, gravity: Boolean, friction: Float, bounciness: Float): ISection {
        val sec = MockSection("sec_physics", "Physics", "science", isRemovable = true)
        sec.addProperty(MockProperty("body_type", "Body Type", PropertyType.ENUM, PropertyValue.EnumValue(bodyType, listOf("Dynamic", "Static", "Kinematic"))))
        sec.addProperty(MockProperty("mass", "Mass", PropertyType.FLOAT, PropertyValue.FloatValue(mass), min = 0f, max = 100f, step = 0.1f))
        sec.addProperty(MockProperty("gravity", "Enable Gravity", PropertyType.BOOL, PropertyValue.BoolValue(gravity)))
        sec.addProperty(MockProperty("friction", "Friction", PropertyType.FLOAT, PropertyValue.FloatValue(friction), min = 0f, max = 1f, step = 0.05f))
        sec.addProperty(MockProperty("bounciness", "Bounciness", PropertyType.FLOAT, PropertyValue.FloatValue(bounciness), min = 0f, max = 1f, step = 0.05f))
        return sec
    }

    private fun createAnimationSection(currentClip: String, speed: Float, isLooping: Boolean, clips: List<String>): ISection {
        val sec = MockSection("sec_animation", "Animation", "movie", isRemovable = true)
        sec.addProperty(MockProperty("current_clip", "Current Clip", PropertyType.ENUM, PropertyValue.EnumValue(currentClip, clips)))
        sec.addProperty(MockProperty("speed", "Speed", PropertyType.FLOAT, PropertyValue.FloatValue(speed), min = 0.1f, max = 4.0f, step = 0.1f))
        sec.addProperty(MockProperty("loop", "Looping", PropertyType.BOOL, PropertyValue.BoolValue(isLooping)))
        return sec
    }

    private fun createLogicBrainSection(script: String, events: List<String>): ISection {
        val sec = MockSection("sec_logic", "Logic (Brain)", "psychology", isRemovable = true)
        sec.addProperty(MockProperty("script", "Attached Script", PropertyType.STRING, PropertyValue.StringValue(script)))
        sec.addProperty(MockProperty("event_hook", "Active Event", PropertyType.ENUM, PropertyValue.EnumValue(events.firstOrNull() ?: "OnStart", events)))
        sec.addProperty(MockProperty("auto_start", "Auto Start", PropertyType.BOOL, PropertyValue.BoolValue(true)))
        return sec
    }

    private fun createLightSection(radius: Float, intensity: Float, color: String): ISection {
        val sec = MockSection("sec_light", "Light 2D", "lightbulb", isRemovable = true)
        sec.addProperty(MockProperty("radius", "Radius", PropertyType.FLOAT, PropertyValue.FloatValue(radius), min = 10f, max = 1000f, step = 10f))
        sec.addProperty(MockProperty("intensity", "Intensity", PropertyType.FLOAT, PropertyValue.FloatValue(intensity), min = 0.1f, max = 5.0f, step = 0.1f))
        sec.addProperty(MockProperty("color", "Color", PropertyType.COLOR, PropertyValue.ColorValue(color)))
        return sec
    }

    private fun createCameraSection(zoom: Float, isOrthographic: Boolean, followTarget: String, smoothSpeed: Float): ISection {
        val sec = MockSection("sec_camera", "Camera 2D", "videocam", isRemovable = true)
        sec.addProperty(MockProperty("zoom", "Zoom", PropertyType.FLOAT, PropertyValue.FloatValue(zoom), min = 0.2f, max = 5.0f, step = 0.1f))
        sec.addProperty(MockProperty("ortho", "Orthographic", PropertyType.BOOL, PropertyValue.BoolValue(isOrthographic)))
        sec.addProperty(MockProperty("follow", "Follow Target", PropertyType.STRING, PropertyValue.StringValue(followTarget)))
        sec.addProperty(MockProperty("smooth", "Smooth Speed", PropertyType.FLOAT, PropertyValue.FloatValue(smoothSpeed), min = 1f, max = 20f))
        return sec
    }

    private fun createParticleSection(rate: Int, lifetime: Float, speed: Float, color: String): ISection {
        val sec = MockSection("sec_particles", "Particles 2D", "auto_awesome", isRemovable = true)
        sec.addProperty(MockProperty("rate", "Emission Rate", PropertyType.INT, PropertyValue.IntValue(rate), min = 1f, max = 200f))
        sec.addProperty(MockProperty("lifetime", "Lifetime (s)", PropertyType.FLOAT, PropertyValue.FloatValue(lifetime), min = 0.1f, max = 10f))
        sec.addProperty(MockProperty("speed", "Speed", PropertyType.FLOAT, PropertyValue.FloatValue(speed), min = 10f, max = 500f))
        sec.addProperty(MockProperty("color", "Color", PropertyType.COLOR, PropertyValue.ColorValue(color)))
        return sec
    }

    private fun createAudioSection(clip: String, volume: Float, playOnAwake: Boolean): ISection {
        val sec = MockSection("sec_audio", "Audio Source", "volume_up", isRemovable = true)
        sec.addProperty(MockProperty("clip", "Audio Clip", PropertyType.AUDIO, PropertyValue.AudioValue(clip)))
        sec.addProperty(MockProperty("volume", "Volume", PropertyType.FLOAT, PropertyValue.FloatValue(volume), min = 0f, max = 1f, step = 0.05f))
        sec.addProperty(MockProperty("play_on_awake", "Play On Awake", PropertyType.BOOL, PropertyValue.BoolValue(playOnAwake)))
        return sec
    }

    private fun createUISection(width: Int, height: Int, anchor: String, fillPercent: Float): ISection {
        val sec = MockSection("sec_ui", "UI Element", "dashboard_customize", isRemovable = true)
        sec.addProperty(MockProperty("width", "Width", PropertyType.INT, PropertyValue.IntValue(width), min = 10f, max = 1000f))
        sec.addProperty(MockProperty("height", "Height", PropertyType.INT, PropertyValue.IntValue(height), min = 10f, max = 1000f))
        sec.addProperty(MockProperty("anchor", "Anchor", PropertyType.ENUM, PropertyValue.EnumValue(anchor, listOf("TopLeft", "TopCenter", "TopRight", "Center", "BottomCenter", "BottomRight"))))
        sec.addProperty(MockProperty("fill", "Fill Percent", PropertyType.FLOAT, PropertyValue.FloatValue(fillPercent), min = 0f, max = 1f, step = 0.05f))
        return sec
    }
}

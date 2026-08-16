package com.example.model

data class SceneNode(
    val id: String,
    val name: String,
    val type: NodeType,
    val posX: Float = 0f,
    val posY: Float = 0f,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val colorHex: String = "#00E5C9",
    val isVisible: Boolean = true,
    val hasPhysics: Boolean = true,
    val mass: Float = 1.0f,
    val velocityX: Float = 0f,
    val velocityY: Float = 0f
)

enum class NodeType {
    CAMERA,
    LIGHT,
    PLAYER,
    ENEMY,
    PLATFORM,
    SPRITE_OBJECT,
    PARTICLE_SYSTEM,
    UI_CANVAS,
    AUDIO_SOURCE
}

data class EngineLog(
    val id: Long = System.currentTimeMillis(),
    val timestamp: String,
    val level: LogLevel,
    val message: String
)

enum class LogLevel {
    INFO,
    WARN,
    SUCCESS,
    ERROR
}

enum class EditorTab {
    SCENE_VIEW,
    CODE_EDITOR,
    ASSETS_STORE,
    CONSOLE
}

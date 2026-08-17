package com.example.engine.interfaces

import kotlinx.coroutines.flow.StateFlow

/**
 * Korva Engine - Core Property Types
 * Dynamic property definition without hardcoding engine internals in the GUI.
 */
enum class PropertyType {
    BOOL,
    INT,
    FLOAT,
    STRING,
    ENUM,
    COLOR,
    VECTOR2,
    VECTOR2I,
    TEXTURE,
    AUDIO,
    ANIMATION,
    OBJECT_REF,
    ASSET_REF
}

/**
 * Generic container for property values across types.
 */
sealed class PropertyValue {
    data class BoolValue(val value: Boolean) : PropertyValue()
    data class IntValue(val value: Int) : PropertyValue()
    data class FloatValue(val value: Float) : PropertyValue()
    data class StringValue(val value: String) : PropertyValue()
    data class EnumValue(val selected: String, val options: List<String>) : PropertyValue()
    data class ColorValue(val hex: String) : PropertyValue()
    data class Vector2Value(val x: Float, val y: Float) : PropertyValue()
    data class Vector2iValue(val x: Int, val y: Int) : PropertyValue()
    data class TextureValue(val assetPath: String, val width: Int = 0, val height: Int = 0) : PropertyValue()
    data class AudioValue(val assetPath: String, val durationMs: Long = 0) : PropertyValue()
    data class AnimationValue(val clipName: String, val fps: Int = 12) : PropertyValue()
    data class ObjectRefValue(val targetObjectId: String?, val targetObjectName: String? = null) : PropertyValue()
    data class AssetRefValue(val assetId: String, val assetName: String) : PropertyValue()
}

/**
 * Interface representing a dynamic, type-safe Property in Korva Engine.
 */
interface IProperty {
    val id: String
    val name: String
    val type: PropertyType
    val value: PropertyValue
    val defaultValue: PropertyValue
    val min: Float?
    val max: Float?
    val step: Float?
    val isReadOnly: Boolean
    val isEditable: Boolean
    val category: String?

    fun setValue(newValue: PropertyValue): Boolean
}

/**
 * Interface representing a collapsible Section / Card attached to an Object.
 * Examples: Transform, Sprite, Physics, Animation, Logic / Brain, Light, Camera, Audio.
 */
interface ISection {
    val id: String
    val name: String
    val iconName: String
    val isRemovable: Boolean
    val isEnabled: Boolean
    val properties: List<IProperty>

    fun getProperty(id: String): IProperty?
    fun setPropertyValue(propertyId: String, value: PropertyValue): Boolean
    fun toggleEnabled(enabled: Boolean)
}

/**
 * Interface representing a game Object inside a Korva Scene Layer.
 */
interface IObject {
    val id: String
    var name: String
    var layerId: String
    var isVisible: Boolean
    var isLocked: Boolean
    val sections: List<ISection>

    fun getSection(sectionId: String): ISection?
    fun addSection(section: ISection): Boolean
    fun removeSection(sectionId: String): Boolean
    fun rename(newName: String) { this.name = newName }
    fun moveToLayer(targetLayerId: String) { this.layerId = targetLayerId }
}

/**
 * Interface representing a Layer in the 2D Scene.
 * Sequence: Layer -> Object -> Section -> Property
 */
interface ILayer {
    val id: String
    var name: String
    var orderIndex: Int
    var isVisible: Boolean
    var isLocked: Boolean
    val objects: List<IObject>

    fun getObject(objectId: String): IObject?
    fun addObject(obj: IObject): Boolean
    fun removeObject(objectId: String): Boolean
    fun rename(newName: String) { this.name = newName }
}

/**
 * Interface representing an Asset (Texture, Audio, Font, Script, Scene, etc.)
 */
enum class AssetType {
    TEXTURE,
    AUDIO,
    FONT,
    ANIMATION,
    SCRIPT,
    SCENE,
    MATERIAL
}

interface IAsset {
    val id: String
    val name: String
    val type: AssetType
    val relativePath: String
    val sizeBytes: Long
    val metadata: Map<String, String> // e.g. "resolution": "2048x2048", "format": "PNG"
}

/**
 * Interface for Animation Clip in Korva Engine.
 */
interface IAnimationClip {
    val id: String
    val name: String
    val totalFrames: Int
    val fps: Int
    val isLooping: Boolean
    val keyframes: List<Int> // frame indices with keyframes
}

/**
 * Interface for Logic / Brain events and scripts.
 */
interface ILogicBrain {
    val attachedScript: String?
    val availableEvents: List<String> // OnStart, OnUpdate, OnCollision, OnDeath, etc.
    val customVariables: Map<String, PropertyValue>
}

/**
 * Interface representing a Korva Scene.
 */
interface IScene {
    val id: String
    val name: String
    val layers: List<ILayer>
    
    fun getLayer(layerId: String): ILayer?
    fun createLayer(name: String): ILayer
    fun removeLayer(layerId: String): Boolean
    fun reorderLayers(layerIdsInOrder: List<String>)
    
    fun findObject(objectId: String): IObject?
    fun createObject(name: String, layerId: String): IObject
    fun removeObject(objectId: String): Boolean
}

/**
 * Interface representing a Korva Project.
 */
interface IProject {
    val id: String
    val name: String
    val rootPath: String
    val scenes: List<IScene>
    val activeScene: IScene?
    val assets: List<IAsset>

    fun getScene(sceneId: String): IScene?
    fun createScene(name: String): IScene
    fun deleteScene(sceneId: String): Boolean
    fun setActiveScene(sceneId: String): Boolean
}

/**
 * Top-level Engine Interface (The abstract contract between GUI and Core).
 */
interface IEngine {
    val currentProject: StateFlow<IProject?>
    val isRunning: StateFlow<Boolean>

    fun initialize()
    fun loadProject(path: String): Boolean
    fun createProject(name: String, path: String, template: String): IProject
    fun closeProject()
    
    fun startSimulation()
    fun pauseSimulation()
    fun stopSimulation()
}

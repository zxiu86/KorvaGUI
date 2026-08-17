package com.example.engine.interfaces

import kotlinx.coroutines.flow.SharedFlow

/**
 * Command Pattern interface for all mutating operations in Korva Engine.
 * Allows effortless Undo / Redo without GUI coupling.
 */
interface ICommand {
    val description: String
    fun execute(): Boolean
    fun undo(): Boolean
    fun redo(): Boolean = execute()
}

/**
 * Command History Manager handling stack-based Undo and Redo.
 */
interface ICommandHistory {
    val canUndo: Boolean
    val canRedo: Boolean
    val historySize: Int
    val undoStack: List<ICommand>
    val redoStack: List<ICommand>

    fun execute(command: ICommand): Boolean
    fun undo(): Boolean
    fun redo(): Boolean
    fun clear()
}

/**
 * Engine Events for reactive, decoupled GUI updates.
 */
sealed class EngineEvent {
    data class ProjectLoaded(val project: IProject) : EngineEvent()
    data class ProjectClosed(val projectId: String) : EngineEvent()
    data class SceneChanged(val scene: IScene) : EngineEvent()
    data class LayerCreated(val layer: ILayer) : EngineEvent()
    data class LayerDeleted(val layerId: String) : EngineEvent()
    data class LayerReordered(val layers: List<ILayer>) : EngineEvent()
    data class ObjectCreated(val obj: IObject, val layerId: String) : EngineEvent()
    data class ObjectDeleted(val objectId: String) : EngineEvent()
    data class ObjectMovedToLayer(val objectId: String, val oldLayerId: String, val newLayerId: String) : EngineEvent()
    data class PropertyChanged(val objectId: String, val sectionId: String, val propertyId: String, val newValue: PropertyValue) : EngineEvent()
    data class SectionAdded(val objectId: String, val section: ISection) : EngineEvent()
    data class SectionRemoved(val objectId: String, val sectionId: String) : EngineEvent()
    data class AssetImported(val asset: IAsset) : EngineEvent()
    data class AssetDeleted(val assetId: String) : EngineEvent()
    data class SimulationStateChanged(val isRunning: Boolean) : EngineEvent()
}

/**
 * Decoupled Event Bus interface.
 */
interface IEventBus {
    val events: SharedFlow<EngineEvent>
    fun post(event: EngineEvent)
}

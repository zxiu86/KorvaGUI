package com.example.engine.backend.mock

import com.example.engine.interfaces.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

// ============================================================================
// 1. Mock Property
// ============================================================================
data class MockProperty(
    override val id: String,
    override val name: String,
    override val type: PropertyType,
    override var value: PropertyValue,
    override val defaultValue: PropertyValue = value,
    override val min: Float? = null,
    override val max: Float? = null,
    override val step: Float? = null,
    override val isReadOnly: Boolean = false,
    override val isEditable: Boolean = true,
    override val category: String? = null
) : IProperty {
    override fun setValue(newValue: PropertyValue): Boolean {
        if (isReadOnly) return false
        this.value = newValue
        return true
    }
}

// ============================================================================
// 2. Mock Section (Cards attached to Objects)
// ============================================================================
data class MockSection(
    override val id: String,
    override val name: String,
    override val iconName: String,
    override val isRemovable: Boolean = true,
    override var isEnabled: Boolean = true,
    private val _properties: MutableList<IProperty> = mutableListOf()
) : ISection {
    override val properties: List<IProperty> get() = _properties.toList()

    override fun getProperty(id: String): IProperty? = _properties.find { it.id == id }

    override fun setPropertyValue(propertyId: String, value: PropertyValue): Boolean {
        val prop = getProperty(propertyId) ?: return false
        return prop.setValue(value)
    }

    override fun toggleEnabled(enabled: Boolean) {
        this.isEnabled = enabled
    }

    fun addProperty(property: IProperty) {
        _properties.add(property)
    }
}

// ============================================================================
// 3. Mock Object
// ============================================================================
data class MockObject(
    override val id: String,
    override var name: String,
    override var layerId: String,
    override var isVisible: Boolean = true,
    override var isLocked: Boolean = false,
    private val _sections: MutableList<ISection> = mutableListOf()
) : IObject {
    override val sections: List<ISection> get() = _sections.toList()

    override fun getSection(sectionId: String): ISection? = _sections.find { it.id == sectionId }

    override fun addSection(section: ISection): Boolean {
        if (_sections.any { it.id == section.id }) return false
        _sections.add(section)
        return true
    }

    override fun removeSection(sectionId: String): Boolean {
        val sec = getSection(sectionId) ?: return false
        if (!sec.isRemovable) return false
        return _sections.remove(sec)
    }
}

// ============================================================================
// 4. Mock Layer
// ============================================================================
data class MockLayer(
    override val id: String,
    override var name: String,
    override var orderIndex: Int,
    override var isVisible: Boolean = true,
    override var isLocked: Boolean = false,
    private val _objects: MutableList<IObject> = mutableListOf()
) : ILayer {
    override val objects: List<IObject> get() = _objects.toList()

    override fun getObject(objectId: String): IObject? = _objects.find { it.id == objectId }

    override fun addObject(obj: IObject): Boolean {
        if (_objects.any { it.id == obj.id }) return false
        _objects.add(obj)
        return true
    }

    override fun removeObject(objectId: String): Boolean {
        return _objects.removeIf { it.id == objectId }
    }
}

// ============================================================================
// 5. Mock Asset
// ============================================================================
data class MockAsset(
    override val id: String,
    override val name: String,
    override val type: AssetType,
    override val relativePath: String,
    override val sizeBytes: Long,
    override val metadata: Map<String, String> = emptyMap()
) : IAsset

// ============================================================================
// 6. Mock Scene
// ============================================================================
data class MockScene(
    override val id: String,
    override var name: String,
    private val _layers: MutableList<ILayer> = mutableListOf()
) : IScene {
    override val layers: List<ILayer> get() = _layers.sortedBy { it.orderIndex }

    override fun getLayer(layerId: String): ILayer? = _layers.find { it.id == layerId }

    override fun createLayer(name: String): ILayer {
        val newLayer = MockLayer(
            id = "layer_${UUID.randomUUID().toString().take(6)}",
            name = name,
            orderIndex = _layers.size
        )
        _layers.add(newLayer)
        return newLayer
    }

    override fun removeLayer(layerId: String): Boolean {
        return _layers.removeIf { it.id == layerId }
    }

    override fun reorderLayers(layerIdsInOrder: List<String>) {
        layerIdsInOrder.forEachIndexed { index, id ->
            _layers.find { it.id == id }?.orderIndex = index
        }
    }

    override fun findObject(objectId: String): IObject? {
        for (layer in _layers) {
            val obj = layer.getObject(objectId)
            if (obj != null) return obj
        }
        return null
    }

    override fun createObject(name: String, layerId: String): IObject {
        val targetLayer = getLayer(layerId) ?: _layers.firstOrNull() ?: createLayer("Default")
        val newObj = MockObject(
            id = "obj_${UUID.randomUUID().toString().take(6)}",
            name = name,
            layerId = targetLayer.id
        )
        // Default Transform Section
        newObj.addSection(createDefaultTransformSection())
        targetLayer.addObject(newObj)
        return newObj
    }

    override fun removeObject(objectId: String): Boolean {
        for (layer in _layers) {
            if (layer.removeObject(objectId)) return true
        }
        return false
    }

    private fun createDefaultTransformSection(): ISection {
        val sec = MockSection("sec_transform", "Transform", "open_with", isRemovable = false)
        sec.addProperty(MockProperty("prop_pos", "Position", PropertyType.VECTOR2, PropertyValue.Vector2Value(0f, 0f)))
        sec.addProperty(MockProperty("prop_rot", "Rotation", PropertyType.FLOAT, PropertyValue.FloatValue(0f), min = 0f, max = 360f))
        sec.addProperty(MockProperty("prop_scale", "Scale", PropertyType.VECTOR2, PropertyValue.Vector2Value(1f, 1f), min = 0.1f, max = 10f))
        return sec
    }
}

// ============================================================================
// 7. Mock Project
// ============================================================================
data class MockProject(
    override val id: String,
    override val name: String,
    override val rootPath: String,
    private val _scenes: MutableList<IScene> = mutableListOf(),
    private var _activeScene: IScene? = null,
    private val _assets: MutableList<IAsset> = mutableListOf()
) : IProject {
    override val scenes: List<IScene> get() = _scenes.toList()
    override val activeScene: IScene? get() = _activeScene ?: _scenes.firstOrNull()
    override val assets: List<IAsset> get() = _assets.toList()

    override fun getScene(sceneId: String): IScene? = _scenes.find { it.id == sceneId }

    override fun createScene(name: String): IScene {
        val sc = MockScene(
            id = "scene_${UUID.randomUUID().toString().take(6)}",
            name = name
        )
        _scenes.add(sc)
        if (_activeScene == null) _activeScene = sc
        return sc
    }

    override fun deleteScene(sceneId: String): Boolean {
        val toRemove = getScene(sceneId) ?: return false
        _scenes.remove(toRemove)
        if (_activeScene?.id == sceneId) {
            _activeScene = _scenes.firstOrNull()
        }
        return true
    }

    override fun setActiveScene(sceneId: String): Boolean {
        val target = getScene(sceneId) ?: return false
        _activeScene = target
        return true
    }

    fun addAsset(asset: IAsset) {
        _assets.add(asset)
    }
}

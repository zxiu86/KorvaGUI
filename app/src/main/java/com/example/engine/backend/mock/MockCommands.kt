package com.example.engine.backend.mock

import com.example.engine.interfaces.*

class SetPropertyCommand(
    private val targetObject: IObject,
    private val sectionId: String,
    private val propertyId: String,
    private val newValue: PropertyValue,
    private val eventBus: IEventBus? = null
) : ICommand {
    private var oldValue: PropertyValue? = null
    override val description: String = "تعديل خاصية $propertyId في ${targetObject.name}"

    override fun execute(): Boolean {
        val section = targetObject.getSection(sectionId) ?: return false
        val prop = section.getProperty(propertyId) ?: return false
        if (oldValue == null) {
            oldValue = prop.value
        }
        val res = prop.setValue(newValue)
        if (res) {
            eventBus?.post(EngineEvent.PropertyChanged(targetObject.id, sectionId, propertyId, newValue))
        }
        return res
    }

    override fun undo(): Boolean {
        val prev = oldValue ?: return false
        val section = targetObject.getSection(sectionId) ?: return false
        val prop = section.getProperty(propertyId) ?: return false
        val res = prop.setValue(prev)
        if (res) {
            eventBus?.post(EngineEvent.PropertyChanged(targetObject.id, sectionId, propertyId, prev))
        }
        return res
    }
}

class CreateObjectCommand(
    private val scene: IScene,
    private val name: String,
    private val layerId: String,
    private val eventBus: IEventBus? = null
) : ICommand {
    private var createdObject: IObject? = null
    override val description: String = "إنشاء كائن جديد: $name"

    override fun execute(): Boolean {
        val obj = createdObject ?: scene.createObject(name, layerId)
        createdObject = obj
        val layer = scene.getLayer(layerId)
        if (layer != null && layer.getObject(obj.id) == null) {
            layer.addObject(obj)
        }
        eventBus?.post(EngineEvent.ObjectCreated(obj, layerId))
        return true
    }

    override fun undo(): Boolean {
        val obj = createdObject ?: return false
        val res = scene.removeObject(obj.id)
        if (res) {
            eventBus?.post(EngineEvent.ObjectDeleted(obj.id))
        }
        return res
    }
}

class DeleteObjectCommand(
    private val scene: IScene,
    private val objectId: String,
    private val eventBus: IEventBus? = null
) : ICommand {
    private var deletedObject: IObject? = null
    private var originalLayerId: String? = null
    override val description: String = "حذف الكائن: $objectId"

    override fun execute(): Boolean {
        val obj = scene.findObject(objectId) ?: return false
        deletedObject = obj
        originalLayerId = obj.layerId
        val res = scene.removeObject(objectId)
        if (res) {
            eventBus?.post(EngineEvent.ObjectDeleted(objectId))
        }
        return res
    }

    override fun undo(): Boolean {
        val obj = deletedObject ?: return false
        val layerId = originalLayerId ?: return false
        val layer = scene.getLayer(layerId) ?: return false
        layer.addObject(obj)
        eventBus?.post(EngineEvent.ObjectCreated(obj, layerId))
        return true
    }
}

class RenameObjectCommand(
    private val obj: IObject,
    private val newName: String,
    private val eventBus: IEventBus? = null
) : ICommand {
    private val oldName = obj.name
    override val description: String = "إعادة تسمية ${obj.name} -> $newName"

    override fun execute(): Boolean {
        obj.rename(newName)
        return true
    }

    override fun undo(): Boolean {
        obj.rename(oldName)
        return true
    }
}

class MoveObjectToLayerCommand(
    private val scene: IScene,
    private val objectId: String,
    private val targetLayerId: String,
    private val eventBus: IEventBus? = null
) : ICommand {
    private var previousLayerId: String? = null
    override val description: String = "نقل الكائن إلى طبقة أخرى"

    override fun execute(): Boolean {
        val obj = scene.findObject(objectId) ?: return false
        val currentLayer = scene.getLayer(obj.layerId) ?: return false
        val targetLayer = scene.getLayer(targetLayerId) ?: return false

        previousLayerId = currentLayer.id
        currentLayer.removeObject(objectId)
        obj.moveToLayer(targetLayerId)
        targetLayer.addObject(obj)

        eventBus?.post(EngineEvent.ObjectMovedToLayer(objectId, currentLayer.id, targetLayerId))
        return true
    }

    override fun undo(): Boolean {
        val prevLayerId = previousLayerId ?: return false
        val obj = scene.findObject(objectId) ?: return false
        val currentLayer = scene.getLayer(obj.layerId) ?: return false
        val prevLayer = scene.getLayer(prevLayerId) ?: return false

        currentLayer.removeObject(objectId)
        obj.moveToLayer(prevLayerId)
        prevLayer.addObject(obj)

        eventBus?.post(EngineEvent.ObjectMovedToLayer(objectId, currentLayer.id, prevLayerId))
        return true
    }
}

class CreateLayerCommand(
    private val scene: IScene,
    private val name: String,
    private val eventBus: IEventBus? = null
) : ICommand {
    private var createdLayer: ILayer? = null
    override val description: String = "إنشاء طبقة جديدة: $name"

    override fun execute(): Boolean {
        val layer = createdLayer ?: scene.createLayer(name)
        createdLayer = layer
        eventBus?.post(EngineEvent.LayerCreated(layer))
        return true
    }

    override fun undo(): Boolean {
        val layer = createdLayer ?: return false
        val res = scene.removeLayer(layer.id)
        if (res) {
            eventBus?.post(EngineEvent.LayerDeleted(layer.id))
        }
        return res
    }
}

class DeleteLayerCommand(
    private val scene: IScene,
    private val layerId: String,
    private val eventBus: IEventBus? = null
) : ICommand {
    private var deletedLayer: ILayer? = null
    override val description: String = "حذف طبقة: $layerId"

    override fun execute(): Boolean {
        val layer = scene.getLayer(layerId) ?: return false
        deletedLayer = layer
        val res = scene.removeLayer(layerId)
        if (res) {
            eventBus?.post(EngineEvent.LayerDeleted(layerId))
        }
        return res
    }

    override fun undo(): Boolean {
        val layer = deletedLayer ?: return false
        val sceneImpl = scene as? MockScene ?: return false
        // Re-add layer if needed
        return true
    }
}

class AddSectionCommand(
    private val obj: IObject,
    private val section: ISection,
    private val eventBus: IEventBus? = null
) : ICommand {
    override val description: String = "إضافة قسم ${section.name} إلى ${obj.name}"

    override fun execute(): Boolean {
        val res = obj.addSection(section)
        if (res) {
            eventBus?.post(EngineEvent.SectionAdded(obj.id, section))
        }
        return res
    }

    override fun undo(): Boolean {
        val res = obj.removeSection(section.id)
        if (res) {
            eventBus?.post(EngineEvent.SectionRemoved(obj.id, section.id))
        }
        return res
    }
}

class RemoveSectionCommand(
    private val obj: IObject,
    private val sectionId: String,
    private val eventBus: IEventBus? = null
) : ICommand {
    private var removedSection: ISection? = null
    override val description: String = "إزالة قسم من ${obj.name}"

    override fun execute(): Boolean {
        val sec = obj.getSection(sectionId) ?: return false
        removedSection = sec
        val res = obj.removeSection(sectionId)
        if (res) {
            eventBus?.post(EngineEvent.SectionRemoved(obj.id, sectionId))
        }
        return res
    }

    override fun undo(): Boolean {
        val sec = removedSection ?: return false
        val res = obj.addSection(sec)
        if (res) {
            eventBus?.post(EngineEvent.SectionAdded(obj.id, sec))
        }
        return res
    }
}

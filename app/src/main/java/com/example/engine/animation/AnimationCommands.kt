package com.example.engine.animation

import com.example.engine.interfaces.ICommand

/**
 * Command for adding a Keyframe with full Undo/Redo capability.
 */
class AddKeyframeCommand(
    private val track: TrackData,
    private val keyframe: KeyframeData,
    private val onExecuted: () -> Unit
) : ICommand {
    override val description: String = "إضافة إطار مفتاحي في الإطار ${keyframe.frame}"

    override fun execute(): Boolean {
        if (!track.keyframes.any { it.id == keyframe.id }) {
            track.keyframes.add(keyframe)
            track.keyframes.sortBy { it.frame }
            onExecuted()
            return true
        }
        return false
    }

    override fun undo(): Boolean {
        val removed = track.keyframes.removeAll { it.id == keyframe.id }
        if (removed) {
            onExecuted()
            return true
        }
        return false
    }
}

/**
 * Command for deleting a Keyframe.
 */
class DeleteKeyframeCommand(
    private val track: TrackData,
    private val keyframe: KeyframeData,
    private val onExecuted: () -> Unit
) : ICommand {
    override val description: String = "حذف إطار مفتاحي في الإطار ${keyframe.frame}"

    override fun execute(): Boolean {
        val removed = track.keyframes.removeAll { it.id == keyframe.id }
        if (removed) {
            onExecuted()
            return true
        }
        return false
    }

    override fun undo(): Boolean {
        track.keyframes.add(keyframe)
        track.keyframes.sortBy { it.frame }
        onExecuted()
        return true
    }
}

/**
 * Command for moving a Keyframe across the timeline.
 */
class MoveKeyframeCommand(
    private val track: TrackData,
    private val keyframeId: String,
    private val oldFrame: Int,
    private val newFrame: Int,
    private val onExecuted: () -> Unit
) : ICommand {
    override val description: String = "نقل الإطار من $oldFrame إلى $newFrame"

    override fun execute(): Boolean {
        val kf = track.keyframes.find { it.id == keyframeId } ?: return false
        kf.frame = newFrame
        track.keyframes.sortBy { it.frame }
        onExecuted()
        return true
    }

    override fun undo(): Boolean {
        val kf = track.keyframes.find { it.id == keyframeId } ?: return false
        kf.frame = oldFrame
        track.keyframes.sortBy { it.frame }
        onExecuted()
        return true
    }
}

/**
 * Command for modifying keyframe value.
 */
class ChangePropertyCommand(
    private val keyframe: KeyframeData,
    private val oldValue: Float,
    private val newValue: Float,
    private val onExecuted: () -> Unit
) : ICommand {
    override val description: String = "تعديل قيمة الإطار المفتاحي إلى $newValue"

    override fun execute(): Boolean {
        keyframe.value = newValue
        onExecuted()
        return true
    }

    override fun undo(): Boolean {
        keyframe.value = oldValue
        onExecuted()
        return true
    }
}

/**
 * Command for modifying interpolation mode (Linear, Constant, Bezier, etc.).
 */
class ChangeInterpolationCommand(
    private val keyframe: KeyframeData,
    private val oldInterp: InterpolationType,
    private val newInterp: InterpolationType,
    private val onExecuted: () -> Unit
) : ICommand {
    override val description: String = "تغيير نوع المنحنى إلى ${newInterp.label}"

    override fun execute(): Boolean {
        keyframe.interpolation = newInterp
        onExecuted()
        return true
    }

    override fun undo(): Boolean {
        keyframe.interpolation = oldInterp
        onExecuted()
        return true
    }
}

/**
 * Command for changing clip FPS.
 */
class ChangeFPSCommand(
    private val clip: ClipData,
    private val oldFps: Int,
    private val newFps: Int,
    private val onExecuted: () -> Unit
) : ICommand {
    override val description: String = "تغيير معدل الإطارات إلى $newFps FPS"

    override fun execute(): Boolean {
        clip.fps = newFps
        onExecuted()
        return true
    }

    override fun undo(): Boolean {
        clip.fps = oldFps
        onExecuted()
        return true
    }
}

/**
 * Command History Stack for Animation Workspace.
 */
class AnimationCommandHistory {
    private val _undoStack = mutableListOf<ICommand>()
    private val _redoStack = mutableListOf<ICommand>()

    val canUndo: Boolean get() = _undoStack.isNotEmpty()
    val canRedo: Boolean get() = _redoStack.isNotEmpty()

    fun execute(command: ICommand): Boolean {
        val success = command.execute()
        if (success) {
            _undoStack.add(command)
            _redoStack.clear()
            if (_undoStack.size > 50) {
                _undoStack.removeAt(0)
            }
        }
        return success
    }

    fun undo(): Boolean {
        if (_undoStack.isNotEmpty()) {
            val cmd = _undoStack.removeAt(_undoStack.size - 1)
            val success = cmd.undo()
            if (success) {
                _redoStack.add(cmd)
            }
            return success
        }
        return false
    }

    fun redo(): Boolean {
        if (_redoStack.isNotEmpty()) {
            val cmd = _redoStack.removeAt(_redoStack.size - 1)
            val success = cmd.redo()
            if (success) {
                _undoStack.add(cmd)
            }
            return success
        }
        return false
    }

    fun clear() {
        _undoStack.clear()
        _redoStack.clear()
    }
}

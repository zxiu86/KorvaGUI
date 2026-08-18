package com.example.engine.animation

import com.example.engine.interfaces.ICommand
import kotlin.math.roundToInt

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
 * Command for moving multiple keyframes simultaneously (Multi-Select Group Drag).
 */
class BatchMoveKeyframesCommand(
    private val clip: ClipData,
    private val moves: List<KeyframeMoveRecord>,
    private val onExecuted: () -> Unit
) : ICommand {
    data class KeyframeMoveRecord(
        val trackId: String,
        val keyframeId: String,
        val oldFrame: Int,
        val newFrame: Int
    )

    override val description: String = "نقل ${moves.size} إطارات مفتاحية"

    override fun execute(): Boolean {
        moves.forEach { move ->
            val track = clip.tracks.find { it.id == move.trackId }
            val kf = track?.keyframes?.find { it.id == move.keyframeId }
            kf?.frame = move.newFrame
            track?.keyframes?.sortBy { it.frame }
        }
        onExecuted()
        return true
    }

    override fun undo(): Boolean {
        moves.forEach { move ->
            val track = clip.tracks.find { it.id == move.trackId }
            val kf = track?.keyframes?.find { it.id == move.keyframeId }
            kf?.frame = move.oldFrame
            track?.keyframes?.sortBy { it.frame }
        }
        onExecuted()
        return true
    }
}

/**
 * Command for batch deleting multiple keyframes.
 */
class BatchDeleteKeyframesCommand(
    private val clip: ClipData,
    private val deletedItems: List<Pair<String, KeyframeData>>, // (TrackId, KeyframeData)
    private val onExecuted: () -> Unit
) : ICommand {
    override val description: String = "حذف ${deletedItems.size} إطارات مفتاحية"

    override fun execute(): Boolean {
        deletedItems.forEach { (trackId, kf) ->
            val track = clip.tracks.find { it.id == trackId }
            track?.keyframes?.removeAll { it.id == kf.id }
        }
        onExecuted()
        return true
    }

    override fun undo(): Boolean {
        deletedItems.forEach { (trackId, kf) ->
            val track = clip.tracks.find { it.id == trackId }
            if (track != null && !track.keyframes.any { it.id == kf.id }) {
                track.keyframes.add(kf)
                track.keyframes.sortBy { it.frame }
            }
        }
        onExecuted()
        return true
    }
}

/**
 * Command for batch adding keyframes (e.g. Paste, Duplicate).
 */
class BatchAddKeyframesCommand(
    private val clip: ClipData,
    private val itemsToAdd: List<Pair<String, KeyframeData>>, // (TrackId, KeyframeData)
    private val onExecuted: () -> Unit
) : ICommand {
    override val description: String = "إضافة ${itemsToAdd.size} إطارات مفتاحية"

    override fun execute(): Boolean {
        itemsToAdd.forEach { (trackId, kf) ->
            val track = clip.tracks.find { it.id == trackId }
            if (track != null) {
                track.keyframes.removeAll { it.frame == kf.frame }
                track.keyframes.add(kf)
                track.keyframes.sortBy { it.frame }
            }
        }
        onExecuted()
        return true
    }

    override fun undo(): Boolean {
        itemsToAdd.forEach { (trackId, kf) ->
            val track = clip.tracks.find { it.id == trackId }
            track?.keyframes?.removeAll { it.id == kf.id }
        }
        onExecuted()
        return true
    }
}

/**
 * Smart Retiming: Scale keyframes timing proportionally.
 */
class ScaleKeyframesTimingCommand(
    private val clip: ClipData,
    private val scaleRecords: List<KeyframeScaleRecord>,
    private val onExecuted: () -> Unit
) : ICommand {
    data class KeyframeScaleRecord(
        val trackId: String,
        val keyframeId: String,
        val oldFrame: Int,
        val newFrame: Int
    )

    override val description: String = "إعادة ضبط توقيت الإطارات (Scale Timing)"

    override fun execute(): Boolean {
        scaleRecords.forEach { record ->
            val track = clip.tracks.find { it.id == record.trackId }
            val kf = track?.keyframes?.find { it.id == record.keyframeId }
            kf?.frame = record.newFrame
            track?.keyframes?.sortBy { it.frame }
        }
        onExecuted()
        return true
    }

    override fun undo(): Boolean {
        scaleRecords.forEach { record ->
            val track = clip.tracks.find { it.id == record.trackId }
            val kf = track?.keyframes?.find { it.id == record.keyframeId }
            kf?.frame = record.oldFrame
            track?.keyframes?.sortBy { it.frame }
        }
        onExecuted()
        return true
    }
}

/**
 * Smart Retiming: Distribute keyframes evenly across selected frame span.
 */
class DistributeKeyframesCommand(
    private val clip: ClipData,
    private val distributionRecords: List<KeyframeDistributionRecord>,
    private val onExecuted: () -> Unit
) : ICommand {
    data class KeyframeDistributionRecord(
        val trackId: String,
        val keyframeId: String,
        val oldFrame: Int,
        val newFrame: Int
    )

    override val description: String = "توزيع الإطارات بالتساوي (Distribute Evenly)"

    override fun execute(): Boolean {
        distributionRecords.forEach { record ->
            val track = clip.tracks.find { it.id == record.trackId }
            val kf = track?.keyframes?.find { it.id == record.keyframeId }
            kf?.frame = record.newFrame
            track?.keyframes?.sortBy { it.frame }
        }
        onExecuted()
        return true
    }

    override fun undo(): Boolean {
        distributionRecords.forEach { record ->
            val track = clip.tracks.find { it.id == record.trackId }
            val kf = track?.keyframes?.find { it.id == record.keyframeId }
            kf?.frame = record.oldFrame
            track?.keyframes?.sortBy { it.frame }
        }
        onExecuted()
        return true
    }
}

/**
 * Smart Retiming: Reverse keyframes within selection.
 */
class ReverseKeyframesCommand(
    private val clip: ClipData,
    private val reverseRecords: List<KeyframeReverseRecord>,
    private val onExecuted: () -> Unit
) : ICommand {
    data class KeyframeReverseRecord(
        val trackId: String,
        val keyframeId: String,
        val oldFrame: Int,
        val newFrame: Int
    )

    override val description: String = "عكس توقيت الحركة (Reverse Timing)"

    override fun execute(): Boolean {
        reverseRecords.forEach { record ->
            val track = clip.tracks.find { it.id == record.trackId }
            val kf = track?.keyframes?.find { it.id == record.keyframeId }
            kf?.frame = record.newFrame
            track?.keyframes?.sortBy { it.frame }
        }
        onExecuted()
        return true
    }

    override fun undo(): Boolean {
        reverseRecords.forEach { record ->
            val track = clip.tracks.find { it.id == record.trackId }
            val kf = track?.keyframes?.find { it.id == record.keyframeId }
            kf?.frame = record.oldFrame
            track?.keyframes?.sortBy { it.frame }
        }
        onExecuted()
        return true
    }
}

/**
 * Timeline Manipulation: Insert Time (+N frames at given frame, shifting everything right).
 */
class InsertTimeCommand(
    private val clip: ClipData,
    private val atFrame: Int,
    private val framesToInsert: Int,
    private val onExecuted: () -> Unit
) : ICommand {
    override val description: String = "إدراج $framesToInsert فريم عند الإطار $atFrame"

    override fun execute(): Boolean {
        clip.tracks.forEach { track ->
            track.keyframes.forEach { kf ->
                if (kf.frame >= atFrame) {
                    kf.frame += framesToInsert
                }
            }
            track.keyframes.sortBy { it.frame }
        }
        clip.events.forEach { ev ->
            if (ev.frame >= atFrame) {
                ev.frame += framesToInsert
            }
        }
        clip.markers.forEach { mk ->
            if (mk.frame >= atFrame) {
                mk.frame += framesToInsert
            }
        }
        clip.durationFrames += framesToInsert
        onExecuted()
        return true
    }

    override fun undo(): Boolean {
        clip.tracks.forEach { track ->
            track.keyframes.forEach { kf ->
                if (kf.frame >= atFrame + framesToInsert) {
                    kf.frame -= framesToInsert
                }
            }
            track.keyframes.sortBy { it.frame }
        }
        clip.events.forEach { ev ->
            if (ev.frame >= atFrame + framesToInsert) {
                ev.frame -= framesToInsert
            }
        }
        clip.markers.forEach { mk ->
            if (mk.frame >= atFrame + framesToInsert) {
                mk.frame -= framesToInsert
            }
        }
        clip.durationFrames = (clip.durationFrames - framesToInsert).coerceAtLeast(1)
        onExecuted()
        return true
    }
}

/**
 * Timeline Manipulation: Delete Time Range (Delete [start..end] and shift subsequent frames back).
 */
class DeleteTimeRangeCommand(
    private val clip: ClipData,
    private val startFrame: Int,
    private val endFrame: Int,
    private val onExecuted: () -> Unit
) : ICommand {
    private val removedKeyframes = mutableListOf<Pair<String, KeyframeData>>()
    private val removedEvents = mutableListOf<AnimationEventData>()
    private val removedMarkers = mutableListOf<AnimationMarkerData>()
    private val rangeSpan = (endFrame - startFrame + 1).coerceAtLeast(1)

    override val description: String = "حذف نطاق التوقيت من $startFrame إلى $endFrame"

    override fun execute(): Boolean {
        removedKeyframes.clear()
        removedEvents.clear()
        removedMarkers.clear()

        clip.tracks.forEach { track ->
            val inRange = track.keyframes.filter { it.frame in startFrame..endFrame }
            inRange.forEach { removedKeyframes.add(Pair(track.id, it)) }
            track.keyframes.removeAll { it.frame in startFrame..endFrame }

            track.keyframes.forEach { kf ->
                if (kf.frame > endFrame) {
                    kf.frame -= rangeSpan
                }
            }
            track.keyframes.sortBy { it.frame }
        }

        val eventsInRange = clip.events.filter { it.frame in startFrame..endFrame }
        removedEvents.addAll(eventsInRange)
        clip.events.removeAll { it.frame in startFrame..endFrame }
        clip.events.forEach { if (it.frame > endFrame) it.frame -= rangeSpan }

        val markersInRange = clip.markers.filter { it.frame in startFrame..endFrame }
        removedMarkers.addAll(markersInRange)
        clip.markers.removeAll { it.frame in startFrame..endFrame }
        clip.markers.forEach { if (it.frame > endFrame) it.frame -= rangeSpan }

        clip.durationFrames = (clip.durationFrames - rangeSpan).coerceAtLeast(1)
        onExecuted()
        return true
    }

    override fun undo(): Boolean {
        // Shift back first
        clip.tracks.forEach { track ->
            track.keyframes.forEach { kf ->
                if (kf.frame >= startFrame) {
                    kf.frame += rangeSpan
                }
            }
            val toRestore = removedKeyframes.filter { it.first == track.id }.map { it.second }
            track.keyframes.addAll(toRestore)
            track.keyframes.sortBy { it.frame }
        }

        clip.events.forEach { if (it.frame >= startFrame) it.frame += rangeSpan }
        clip.events.addAll(removedEvents)
        clip.events.sortBy { it.frame }

        clip.markers.forEach { if (it.frame >= startFrame) it.frame += rangeSpan }
        clip.markers.addAll(removedMarkers)
        clip.markers.sortBy { it.frame }

        clip.durationFrames += rangeSpan
        onExecuted()
        return true
    }
}

/**
 * Remove Gaps: Collapses empty gaps between keyframes.
 */
class RemoveGapsCommand(
    private val clip: ClipData,
    private val fixedGap: Int = 2,
    private val onExecuted: () -> Unit
) : ICommand {
    private val originalFrames = mutableMapOf<String, Int>()

    override val description: String = "إزالة الفراغات وضغط التوقيت (Remove Gaps)"

    override fun execute(): Boolean {
        originalFrames.clear()
        clip.tracks.forEach { track ->
            track.keyframes.forEach { originalFrames[it.id] = it.frame }
            val sorted = track.keyframes.sortedBy { it.frame }
            var currentF = 0
            sorted.forEachIndexed { index, kf ->
                if (index == 0) {
                    currentF = kf.frame
                } else {
                    currentF += fixedGap
                    kf.frame = currentF
                }
            }
        }
        onExecuted()
        return true
    }

    override fun undo(): Boolean {
        clip.tracks.forEach { track ->
            track.keyframes.forEach { kf ->
                originalFrames[kf.id]?.let { kf.frame = it }
            }
            track.keyframes.sortBy { it.frame }
        }
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
 * Command for modifying interpolation mode (Linear, Constant, Ease, Bezier).
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
        if (newInterp == InterpolationType.CONSTANT) {
            keyframe.type = KeyframeType.HOLD
        }
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

    val lastActionDescription: String? get() = _undoStack.lastOrNull()?.description

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

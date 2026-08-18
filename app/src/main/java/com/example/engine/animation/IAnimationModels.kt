package com.example.engine.animation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.engine.interfaces.ICommand
import com.example.ui.theme.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// =============================================================================
// 1. Enums & Types
// =============================================================================

enum class KeyframeType {
    STANDARD,      // Standard diamond keyframe
    HOLD,          // Square hold keyframe (constant value until next key)
    EVENT,         // Circular trigger event
    SPRITE_FRAME   // Sprite sheet texture index
}

enum class InterpolationType(val label: String, val description: String) {
    LINEAR("Linear", "تدرج خطي مباشر بين القيمتين"),
    CONSTANT("Constant", "ثبات القيمة حتى الإطار التالي"),
    EASE_IN("Ease In", "بداية بطيئة ثم تسارع"),
    EASE_OUT("Ease Out", "بداية سريعة ثم تباطؤ"),
    EASE_IN_OUT("Ease In Out", "تسارع وسطي سلس"),
    BEZIER("Bezier", "منحنى بيزيه مخصص بالكامل")
}

enum class LoopMode(val label: String, val iconName: String) {
    OFF("Once", "مرة واحدة"),
    LOOP("Loop", "تكرار مستمر"),
    PING_PONG("Ping-Pong", "ذهاب وإياب"),
    REVERSE("Reverse", "عكسي"),
    RANGE("Range", "نطاق محدد")
}

enum class TrackCategory(val label: String, val iconColor: Color) {
    TRANSFORM("Transform", StudioPurpleLight),
    SPRITE("Sprite", StudioBlue),
    VISUAL("Visual", StudioOrange),
    AUDIO("Audio", StudioGreen),
    EVENTS("Events", StudioYellow),
    CUSTOM("Custom", StudioPink)
}

enum class SnapMode {
    SNAP_FRAME,
    SNAP_KEYFRAME,
    SNAP_MARKER,
    FREE_MOVE
}

// =============================================================================
// 2. Data Models
// =============================================================================

data class BezierHandle(
    val x: Float, // 0..1 normalized time
    val y: Float  // 0..1 normalized value
)

data class KeyframeData(
    val id: String = java.util.UUID.randomUUID().toString(),
    var frame: Int,
    var value: Float,
    var interpolation: InterpolationType = InterpolationType.LINEAR,
    var type: KeyframeType = KeyframeType.STANDARD,
    var handleIn: BezierHandle = BezierHandle(0.25f, 0.1f),
    var handleOut: BezierHandle = BezierHandle(0.25f, 1.0f),
    var eventTag: String? = null
)

data class TrackData(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String,
    val category: TrackCategory,
    val propertyPath: String,
    val defaultValue: Float = 0f,
    val minValue: Float = -500f,
    val maxValue: Float = 500f,
    var isVisible: Boolean = true,
    var isLocked: Boolean = false,
    var isExpanded: Boolean = true,
    val keyframes: MutableList<KeyframeData> = mutableListOf()
) {
    val displayColor: Color
        get() = when {
            name.contains("Pos X", ignoreCase = true) -> StudioRed
            name.contains("Pos Y", ignoreCase = true) -> StudioGreen
            name.contains("Rotation", ignoreCase = true) -> StudioPurpleLight
            name.contains("Scale", ignoreCase = true) -> StudioOrange
            name.contains("Sprite", ignoreCase = true) -> StudioBlue
            name.contains("Opacity", ignoreCase = true) -> StudioPink
            name.contains("Color", ignoreCase = true) -> StudioYellow
            name.contains("Event", ignoreCase = true) -> StudioYellow
            else -> category.iconColor
        }

    val iconVector: ImageVector
        get() = when (category) {
            TrackCategory.TRANSFORM -> if (name.contains("Rot")) Icons.Default.RotateRight else if (name.contains("Scale")) Icons.Default.AspectRatio else Icons.Default.OpenWith
            TrackCategory.SPRITE -> Icons.Default.Image
            TrackCategory.VISUAL -> Icons.Default.Palette
            TrackCategory.AUDIO -> Icons.Default.VolumeUp
            TrackCategory.EVENTS -> Icons.Default.Bolt
            TrackCategory.CUSTOM -> Icons.Default.Tune
        }
}

data class AnimationEventData(
    val id: String = java.util.UUID.randomUUID().toString(),
    var frame: Int,
    var name: String,
    var functionName: String = "PlaySound",
    var parameters: String = "footstep_01"
)

data class AnimationMarkerData(
    val id: String = java.util.UUID.randomUUID().toString(),
    var frame: Int,
    var label: String,
    var colorHex: Long = 0xFFFACC15
)

data class SpriteSheetConfig(
    var textureName: String = "player_spritesheet.png",
    var rows: Int = 4,
    var columns: Int = 4,
    var startFrame: Int = 0,
    var endFrame: Int = 11,
    var fps: Int = 12
)

data class ClipData(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String,
    var iconEmoji: String = "🎬",
    var fps: Int = 24,
    var durationFrames: Int = 24,
    var loopMode: LoopMode = LoopMode.LOOP,
    var rangeStart: Int = 0,
    var rangeEnd: Int = 24,
    var isFavorite: Boolean = false,
    val tracks: MutableList<TrackData> = mutableListOf(),
    val events: MutableList<AnimationEventData> = mutableListOf(),
    val markers: MutableList<AnimationMarkerData> = mutableListOf()
) {
    val durationSeconds: Float
        get() = if (fps > 0) durationFrames.toFloat() / fps else 0f
}

// =============================================================================
// 3. Korva Animation Interfaces (Decoupled Architecture)
// =============================================================================

interface IKeyframe {
    val id: String
    val frame: Int
    val value: Float
    val interpolation: InterpolationType
}

interface IAnimationTrack {
    val id: String
    val name: String
    val category: TrackCategory
    val keyframes: List<IKeyframe>
}

interface IAnimationClip {
    val id: String
    val name: String
    val fps: Int
    val durationFrames: Int
    val loopMode: LoopMode
    val tracks: List<IAnimationTrack>
}

interface IAnimationController {
    fun getClips(): List<ClipData>
    fun getActiveClip(): ClipData?
    fun setActiveClip(clipId: String)
    fun createClip(name: String, fps: Int, durationFrames: Int): ClipData
    fun duplicateClip(clipId: String): ClipData?
    fun deleteClip(clipId: String): Boolean
    fun renameClip(clipId: String, newName: String): Boolean

    fun addTrack(clipId: String, category: TrackCategory, name: String, propertyPath: String): TrackData?
    fun removeTrack(clipId: String, trackId: String): Boolean

    fun addKeyframe(clipId: String, trackId: String, frame: Int, value: Float, interp: InterpolationType = InterpolationType.LINEAR): KeyframeData?
    fun removeKeyframe(clipId: String, trackId: String, keyframeId: String): Boolean
    fun moveKeyframe(clipId: String, trackId: String, keyframeId: String, newFrame: Int): Boolean
    fun setKeyframeValue(clipId: String, trackId: String, keyframeId: String, newValue: Float): Boolean
    fun setInterpolation(clipId: String, trackId: String, keyframeId: String, interp: InterpolationType): Boolean

    fun addEvent(clipId: String, frame: Int, name: String, functionName: String, parameters: String): AnimationEventData?
    fun removeEvent(clipId: String, eventId: String): Boolean

    fun addMarker(clipId: String, frame: Int, label: String, colorHex: Long): AnimationMarkerData?
    fun removeMarker(clipId: String, markerId: String): Boolean

    fun evaluateTrackAt(track: TrackData, frame: Float): Float
}

// =============================================================================
// 4. Animation Event Bus System
// =============================================================================

sealed class AnimationEngineEvent {
    data class ClipCreated(val clip: ClipData) : AnimationEngineEvent()
    data class ClipDeleted(val clipId: String) : AnimationEngineEvent()
    data class ClipSwitched(val clipId: String) : AnimationEngineEvent()
    data class KeyframeAdded(val trackId: String, val keyframe: KeyframeData) : AnimationEngineEvent()
    data class KeyframeRemoved(val trackId: String, val keyframeId: String) : AnimationEngineEvent()
    data class KeyframeMoved(val trackId: String, val keyframeId: String, val newFrame: Int) : AnimationEngineEvent()
    data class CurrentFrameChanged(val frame: Float) : AnimationEngineEvent()
    data class PlaybackStateChanged(val isPlaying: Boolean) : AnimationEngineEvent()
    data class TrackChanged(val trackId: String) : AnimationEngineEvent()
    data class PropertyAnimated(val property: String, val value: Float) : AnimationEngineEvent()
    data class EventTriggered(val event: AnimationEventData) : AnimationEngineEvent()
}

class AnimationEventBus {
    private val _events = MutableSharedFlow<AnimationEngineEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<AnimationEngineEvent> = _events.asSharedFlow()

    fun emit(event: AnimationEngineEvent) {
        _events.tryEmit(event)
    }
}

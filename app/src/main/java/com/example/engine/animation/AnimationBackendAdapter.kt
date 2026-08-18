package com.example.engine.animation

import kotlin.math.pow
import kotlin.math.roundToInt

class MockAnimationBackend(
    val eventBus: AnimationEventBus = AnimationEventBus(),
    val commandHistory: AnimationCommandHistory = AnimationCommandHistory()
) : IAnimationController {

    private val _clips = mutableListOf<ClipData>()
    private var _activeClipId: String = ""

    // Clipboard for Keyframes & Tracks
    var keyframeClipboard: List<KeyframeData> = emptyList()
    var trackClipboard: TrackData? = null

    init {
        initializeDefaultPresets()
    }

    private fun initializeDefaultPresets() {
        // -------------------------------------------------------------
        // 1. Idle Clip (12 Frames, 12 FPS, Looping)
        // -------------------------------------------------------------
        val idleClip = ClipData(
            id = "clip_idle",
            name = "Idle",
            iconEmoji = "🧍",
            fps = 12,
            durationFrames = 12,
            loopMode = LoopMode.LOOP,
            isFavorite = true,
            tracks = mutableListOf(
                TrackData(
                    name = "Position Y",
                    category = TrackCategory.TRANSFORM,
                    propertyPath = "transform.position.y",
                    defaultValue = 0f,
                    keyframes = mutableListOf(
                        KeyframeData(frame = 0, value = 0f, interpolation = InterpolationType.EASE_IN_OUT),
                        KeyframeData(frame = 6, value = -8f, interpolation = InterpolationType.EASE_IN_OUT),
                        KeyframeData(frame = 12, value = 0f, interpolation = InterpolationType.EASE_IN_OUT)
                    )
                ),
                TrackData(
                    name = "Scale Y",
                    category = TrackCategory.TRANSFORM,
                    propertyPath = "transform.scale.y",
                    defaultValue = 1f,
                    keyframes = mutableListOf(
                        KeyframeData(frame = 0, value = 1.0f, interpolation = InterpolationType.EASE_IN_OUT),
                        KeyframeData(frame = 6, value = 1.08f, interpolation = InterpolationType.EASE_IN_OUT),
                        KeyframeData(frame = 12, value = 1.0f, interpolation = InterpolationType.EASE_IN_OUT)
                    )
                ),
                TrackData(
                    name = "Sprite Frame",
                    category = TrackCategory.SPRITE,
                    propertyPath = "sprite.frame",
                    defaultValue = 0f,
                    keyframes = mutableListOf(
                        KeyframeData(frame = 0, value = 0f, interpolation = InterpolationType.CONSTANT, type = KeyframeType.SPRITE_FRAME),
                        KeyframeData(frame = 6, value = 1f, interpolation = InterpolationType.CONSTANT, type = KeyframeType.SPRITE_FRAME)
                    )
                )
            ),
            markers = mutableListOf(
                AnimationMarkerData(frame = 0, label = "Loop Start", colorHex = 0xFF22C55E),
                AnimationMarkerData(frame = 6, label = "Apex Breath", colorHex = 0xFF38BDF8)
            )
        )

        // -------------------------------------------------------------
        // 2. Walk Clip (16 Frames, 24 FPS, Looping with Footstep Events)
        // -------------------------------------------------------------
        val walkClip = ClipData(
            id = "clip_walk",
            name = "Walk",
            iconEmoji = "🚶",
            fps = 24,
            durationFrames = 16,
            loopMode = LoopMode.LOOP,
            isFavorite = true,
            tracks = mutableListOf(
                TrackData(
                    name = "Position X",
                    category = TrackCategory.TRANSFORM,
                    propertyPath = "transform.position.x",
                    defaultValue = 0f,
                    keyframes = mutableListOf(
                        KeyframeData(frame = 0, value = -14f, interpolation = InterpolationType.LINEAR),
                        KeyframeData(frame = 8, value = 14f, interpolation = InterpolationType.LINEAR),
                        KeyframeData(frame = 16, value = -14f, interpolation = InterpolationType.LINEAR)
                    )
                ),
                TrackData(
                    name = "Position Y",
                    category = TrackCategory.TRANSFORM,
                    propertyPath = "transform.position.y",
                    defaultValue = 0f,
                    keyframes = mutableListOf(
                        KeyframeData(frame = 0, value = 0f, interpolation = InterpolationType.EASE_IN_OUT),
                        KeyframeData(frame = 4, value = -12f, interpolation = InterpolationType.EASE_IN_OUT),
                        KeyframeData(frame = 8, value = 0f, interpolation = InterpolationType.EASE_IN_OUT),
                        KeyframeData(frame = 12, value = -12f, interpolation = InterpolationType.EASE_IN_OUT),
                        KeyframeData(frame = 16, value = 0f, interpolation = InterpolationType.EASE_IN_OUT)
                    )
                ),
                TrackData(
                    name = "Rotation",
                    category = TrackCategory.TRANSFORM,
                    propertyPath = "transform.rotation",
                    defaultValue = 0f,
                    keyframes = mutableListOf(
                        KeyframeData(frame = 0, value = -8f, interpolation = InterpolationType.EASE_IN_OUT),
                        KeyframeData(frame = 8, value = 8f, interpolation = InterpolationType.EASE_IN_OUT),
                        KeyframeData(frame = 16, value = -8f, interpolation = InterpolationType.EASE_IN_OUT)
                    )
                ),
                TrackData(
                    name = "Sprite Frame",
                    category = TrackCategory.SPRITE,
                    propertyPath = "sprite.frame",
                    defaultValue = 0f,
                    keyframes = mutableListOf(
                        KeyframeData(frame = 0, value = 0f, interpolation = InterpolationType.CONSTANT, type = KeyframeType.SPRITE_FRAME),
                        KeyframeData(frame = 4, value = 1f, interpolation = InterpolationType.CONSTANT, type = KeyframeType.SPRITE_FRAME),
                        KeyframeData(frame = 8, value = 2f, interpolation = InterpolationType.CONSTANT, type = KeyframeType.SPRITE_FRAME),
                        KeyframeData(frame = 12, value = 3f, interpolation = InterpolationType.CONSTANT, type = KeyframeType.SPRITE_FRAME)
                    )
                )
            ),
            events = mutableListOf(
                AnimationEventData(frame = 4, name = "Footstep_L", functionName = "PlaySound", parameters = "footstep_grass_01"),
                AnimationEventData(frame = 12, name = "Footstep_R", functionName = "PlaySound", parameters = "footstep_grass_02")
            ),
            markers = mutableListOf(
                AnimationMarkerData(frame = 4, label = "Step Left", colorHex = 0xFFFACC15),
                AnimationMarkerData(frame = 12, label = "Step Right", colorHex = 0xFFFACC15)
            )
        )

        // -------------------------------------------------------------
        // 3. Run Clip (12 Frames, 24 FPS, Fast Looping)
        // -------------------------------------------------------------
        val runClip = ClipData(
            id = "clip_run",
            name = "Run",
            iconEmoji = "🏃",
            fps = 24,
            durationFrames = 12,
            loopMode = LoopMode.LOOP,
            isFavorite = true,
            tracks = mutableListOf(
                TrackData(
                    name = "Position X",
                    category = TrackCategory.TRANSFORM,
                    propertyPath = "transform.position.x",
                    defaultValue = 0f,
                    keyframes = mutableListOf(
                        KeyframeData(frame = 0, value = -22f, interpolation = InterpolationType.LINEAR),
                        KeyframeData(frame = 6, value = 22f, interpolation = InterpolationType.LINEAR),
                        KeyframeData(frame = 12, value = -22f, interpolation = InterpolationType.LINEAR)
                    )
                ),
                TrackData(
                    name = "Position Y",
                    category = TrackCategory.TRANSFORM,
                    propertyPath = "transform.position.y",
                    defaultValue = 0f,
                    keyframes = mutableListOf(
                        KeyframeData(frame = 0, value = 0f, interpolation = InterpolationType.EASE_IN_OUT),
                        KeyframeData(frame = 3, value = -18f, interpolation = InterpolationType.EASE_IN_OUT),
                        KeyframeData(frame = 6, value = 0f, interpolation = InterpolationType.EASE_IN_OUT),
                        KeyframeData(frame = 9, value = -18f, interpolation = InterpolationType.EASE_IN_OUT),
                        KeyframeData(frame = 12, value = 0f, interpolation = InterpolationType.EASE_IN_OUT)
                    )
                ),
                TrackData(
                    name = "Rotation",
                    category = TrackCategory.TRANSFORM,
                    propertyPath = "transform.rotation",
                    defaultValue = 0f,
                    keyframes = mutableListOf(
                        KeyframeData(frame = 0, value = 14f, interpolation = InterpolationType.EASE_IN_OUT),
                        KeyframeData(frame = 6, value = 8f, interpolation = InterpolationType.EASE_IN_OUT),
                        KeyframeData(frame = 12, value = 14f, interpolation = InterpolationType.EASE_IN_OUT)
                    )
                )
            ),
            events = mutableListOf(
                AnimationEventData(frame = 3, name = "Dust_Puff_L", functionName = "SpawnParticle", parameters = "dust_puff_run"),
                AnimationEventData(frame = 9, name = "Dust_Puff_R", functionName = "SpawnParticle", parameters = "dust_puff_run")
            )
        )

        // -------------------------------------------------------------
        // 4. Jump Clip (20 Frames, 24 FPS, Once)
        // -------------------------------------------------------------
        val jumpClip = ClipData(
            id = "clip_jump",
            name = "Jump",
            iconEmoji = "🦘",
            fps = 24,
            durationFrames = 20,
            loopMode = LoopMode.OFF,
            tracks = mutableListOf(
                TrackData(
                    name = "Position Y",
                    category = TrackCategory.TRANSFORM,
                    propertyPath = "transform.position.y",
                    defaultValue = 0f,
                    keyframes = mutableListOf(
                        KeyframeData(frame = 0, value = 0f, interpolation = InterpolationType.EASE_IN),
                        KeyframeData(frame = 3, value = 10f, interpolation = InterpolationType.EASE_IN), // squash anticipation
                        KeyframeData(frame = 10, value = -80f, interpolation = InterpolationType.EASE_OUT), // apex
                        KeyframeData(frame = 16, value = 0f, interpolation = InterpolationType.EASE_IN), // landing
                        KeyframeData(frame = 18, value = 6f, interpolation = InterpolationType.EASE_OUT), // squash recovery
                        KeyframeData(frame = 20, value = 0f, interpolation = InterpolationType.EASE_IN_OUT)
                    )
                ),
                TrackData(
                    name = "Scale Y",
                    category = TrackCategory.TRANSFORM,
                    propertyPath = "transform.scale.y",
                    defaultValue = 1f,
                    keyframes = mutableListOf(
                        KeyframeData(frame = 0, value = 1.0f, interpolation = InterpolationType.EASE_IN_OUT),
                        KeyframeData(frame = 3, value = 0.75f, interpolation = InterpolationType.EASE_IN_OUT), // squash
                        KeyframeData(frame = 6, value = 1.35f, interpolation = InterpolationType.EASE_IN_OUT), // stretch
                        KeyframeData(frame = 10, value = 1.0f, interpolation = InterpolationType.EASE_IN_OUT), // apex
                        KeyframeData(frame = 16, value = 0.8f, interpolation = InterpolationType.EASE_IN_OUT), // impact squash
                        KeyframeData(frame = 20, value = 1.0f, interpolation = InterpolationType.EASE_IN_OUT)
                    )
                )
            ),
            events = mutableListOf(
                AnimationEventData(frame = 3, name = "Audio_Jump", functionName = "PlaySound", parameters = "sfx_player_jump"),
                AnimationEventData(frame = 16, name = "Audio_Land", functionName = "PlaySound", parameters = "sfx_player_land")
            ),
            markers = mutableListOf(
                AnimationMarkerData(frame = 3, label = "Takeoff", colorHex = 0xFFEF4444),
                AnimationMarkerData(frame = 10, label = "Apex Float", colorHex = 0xFF38BDF8),
                AnimationMarkerData(frame = 16, label = "Touchdown", colorHex = 0xFF22C55E)
            )
        )

        // -------------------------------------------------------------
        // 5. Attack Clip (18 Frames, 30 FPS, Combat Combo)
        // -------------------------------------------------------------
        val attackClip = ClipData(
            id = "clip_attack",
            name = "Attack",
            iconEmoji = "⚔️",
            fps = 30,
            durationFrames = 18,
            loopMode = LoopMode.OFF,
            isFavorite = true,
            tracks = mutableListOf(
                TrackData(
                    name = "Position X",
                    category = TrackCategory.TRANSFORM,
                    propertyPath = "transform.position.x",
                    defaultValue = 0f,
                    keyframes = mutableListOf(
                        KeyframeData(frame = 0, value = 0f, interpolation = InterpolationType.EASE_IN),
                        KeyframeData(frame = 4, value = -16f, interpolation = InterpolationType.EASE_IN), // windup
                        KeyframeData(frame = 7, value = 36f, interpolation = InterpolationType.EASE_OUT), // swift slash forward
                        KeyframeData(frame = 14, value = 10f, interpolation = InterpolationType.EASE_IN_OUT),
                        KeyframeData(frame = 18, value = 0f, interpolation = InterpolationType.EASE_IN_OUT)
                    )
                ),
                TrackData(
                    name = "Rotation",
                    category = TrackCategory.TRANSFORM,
                    propertyPath = "transform.rotation",
                    defaultValue = 0f,
                    keyframes = mutableListOf(
                        KeyframeData(frame = 0, value = 0f, interpolation = InterpolationType.EASE_IN),
                        KeyframeData(frame = 4, value = -35f, interpolation = InterpolationType.EASE_IN),
                        KeyframeData(frame = 7, value = 55f, interpolation = InterpolationType.EASE_OUT),
                        KeyframeData(frame = 14, value = 15f, interpolation = InterpolationType.EASE_IN_OUT),
                        KeyframeData(frame = 18, value = 0f, interpolation = InterpolationType.EASE_IN_OUT)
                    )
                ),
                TrackData(
                    name = "Scale X",
                    category = TrackCategory.TRANSFORM,
                    propertyPath = "transform.scale.x",
                    defaultValue = 1f,
                    keyframes = mutableListOf(
                        KeyframeData(frame = 0, value = 1.0f, interpolation = InterpolationType.LINEAR),
                        KeyframeData(frame = 7, value = 1.4f, interpolation = InterpolationType.EASE_OUT),
                        KeyframeData(frame = 18, value = 1.0f, interpolation = InterpolationType.LINEAR)
                    )
                )
            ),
            events = mutableListOf(
                AnimationEventData(frame = 5, name = "Slash_SFX", functionName = "PlaySound", parameters = "sfx_sword_slash_heavy"),
                AnimationEventData(frame = 7, name = "Hitbox_Active", functionName = "EnableHitbox", parameters = "damage:45,radius:60"),
                AnimationEventData(frame = 11, name = "Hitbox_Disable", functionName = "DisableHitbox", parameters = "")
            ),
            markers = mutableListOf(
                AnimationMarkerData(frame = 4, label = "Wind-up", colorHex = 0xFFFB923C),
                AnimationMarkerData(frame = 7, label = "Impact", colorHex = 0xFFEF4444),
                AnimationMarkerData(frame = 14, label = "Recovery", colorHex = 0xFF38BDF8)
            )
        )

        // -------------------------------------------------------------
        // 6. Hit Clip (8 Frames, 24 FPS)
        // -------------------------------------------------------------
        val hitClip = ClipData(
            id = "clip_hit",
            name = "Hit",
            iconEmoji = "💥",
            fps = 24,
            durationFrames = 8,
            loopMode = LoopMode.OFF,
            tracks = mutableListOf(
                TrackData(
                    name = "Position X",
                    category = TrackCategory.TRANSFORM,
                    propertyPath = "transform.position.x",
                    defaultValue = 0f,
                    keyframes = mutableListOf(
                        KeyframeData(frame = 0, value = 0f, interpolation = InterpolationType.EASE_OUT),
                        KeyframeData(frame = 2, value = -25f, interpolation = InterpolationType.EASE_OUT),
                        KeyframeData(frame = 8, value = 0f, interpolation = InterpolationType.EASE_IN_OUT)
                    )
                ),
                TrackData(
                    name = "Opacity",
                    category = TrackCategory.VISUAL,
                    propertyPath = "visual.opacity",
                    defaultValue = 1f,
                    keyframes = mutableListOf(
                        KeyframeData(frame = 0, value = 1.0f, interpolation = InterpolationType.CONSTANT),
                        KeyframeData(frame = 2, value = 0.4f, interpolation = InterpolationType.CONSTANT),
                        KeyframeData(frame = 4, value = 0.9f, interpolation = InterpolationType.CONSTANT),
                        KeyframeData(frame = 8, value = 1.0f, interpolation = InterpolationType.CONSTANT)
                    )
                )
            ),
            events = mutableListOf(
                AnimationEventData(frame = 1, name = "Damage_FX", functionName = "ScreenShake", parameters = "intensity:8,duration:0.15")
            )
        )

        // -------------------------------------------------------------
        // 7. Death Clip (24 Frames, 24 FPS)
        // -------------------------------------------------------------
        val deathClip = ClipData(
            id = "clip_death",
            name = "Death",
            iconEmoji = "💀",
            fps = 24,
            durationFrames = 24,
            loopMode = LoopMode.OFF,
            tracks = mutableListOf(
                TrackData(
                    name = "Position Y",
                    category = TrackCategory.TRANSFORM,
                    propertyPath = "transform.position.y",
                    defaultValue = 0f,
                    keyframes = mutableListOf(
                        KeyframeData(frame = 0, value = 0f, interpolation = InterpolationType.EASE_IN),
                        KeyframeData(frame = 6, value = -25f, interpolation = InterpolationType.EASE_OUT),
                        KeyframeData(frame = 16, value = 30f, interpolation = InterpolationType.EASE_IN),
                        KeyframeData(frame = 24, value = 30f, interpolation = InterpolationType.CONSTANT)
                    )
                ),
                TrackData(
                    name = "Rotation",
                    category = TrackCategory.TRANSFORM,
                    propertyPath = "transform.rotation",
                    defaultValue = 0f,
                    keyframes = mutableListOf(
                        KeyframeData(frame = 0, value = 0f, interpolation = InterpolationType.EASE_IN_OUT),
                        KeyframeData(frame = 16, value = 90f, interpolation = InterpolationType.EASE_IN_OUT),
                        KeyframeData(frame = 24, value = 90f, interpolation = InterpolationType.CONSTANT)
                    )
                ),
                TrackData(
                    name = "Opacity",
                    category = TrackCategory.VISUAL,
                    propertyPath = "visual.opacity",
                    defaultValue = 1f,
                    keyframes = mutableListOf(
                        KeyframeData(frame = 0, value = 1f, interpolation = InterpolationType.LINEAR),
                        KeyframeData(frame = 14, value = 1f, interpolation = InterpolationType.LINEAR),
                        KeyframeData(frame = 24, value = 0f, interpolation = InterpolationType.LINEAR)
                    )
                )
            ),
            events = mutableListOf(
                AnimationEventData(frame = 0, name = "Death_Voice", functionName = "PlaySound", parameters = "sfx_player_grunt_death"),
                AnimationEventData(frame = 16, name = "Smoke_Burst", functionName = "SpawnParticle", parameters = "smoke_poof_large")
            )
        )

        _clips.addAll(listOf(idleClip, walkClip, runClip, jumpClip, attackClip, hitClip, deathClip))
        _activeClipId = idleClip.id
    }

    override fun getClips(): List<ClipData> = _clips

    override fun getActiveClip(): ClipData? = _clips.find { it.id == _activeClipId } ?: _clips.firstOrNull()

    override fun setActiveClip(clipId: String) {
        _activeClipId = clipId
        eventBus.emit(AnimationEngineEvent.ClipSwitched(clipId))
    }

    override fun createClip(name: String, fps: Int, durationFrames: Int): ClipData {
        val newClip = ClipData(
            name = name,
            iconEmoji = "✨",
            fps = fps.coerceIn(1, 120),
            durationFrames = durationFrames.coerceAtLeast(1),
            tracks = mutableListOf(
                TrackData(name = "Position X", category = TrackCategory.TRANSFORM, propertyPath = "transform.position.x", defaultValue = 0f, keyframes = mutableListOf(KeyframeData(frame = 0, value = 0f))),
                TrackData(name = "Position Y", category = TrackCategory.TRANSFORM, propertyPath = "transform.position.y", defaultValue = 0f, keyframes = mutableListOf(KeyframeData(frame = 0, value = 0f))),
                TrackData(name = "Rotation", category = TrackCategory.TRANSFORM, propertyPath = "transform.rotation", defaultValue = 0f, keyframes = mutableListOf(KeyframeData(frame = 0, value = 0f))),
                TrackData(name = "Scale Y", category = TrackCategory.TRANSFORM, propertyPath = "transform.scale.y", defaultValue = 1f, keyframes = mutableListOf(KeyframeData(frame = 0, value = 1f)))
            )
        )
        _clips.add(newClip)
        _activeClipId = newClip.id
        eventBus.emit(AnimationEngineEvent.ClipCreated(newClip))
        return newClip
    }

    override fun duplicateClip(clipId: String): ClipData? {
        val src = _clips.find { it.id == clipId } ?: return null
        val duplicated = src.copy(
            id = java.util.UUID.randomUUID().toString(),
            name = "${src.name} (Copy)",
            tracks = src.tracks.map { trk ->
                trk.copy(
                    id = java.util.UUID.randomUUID().toString(),
                    keyframes = trk.keyframes.map { it.copy(id = java.util.UUID.randomUUID().toString()) }.toMutableList()
                )
            }.toMutableList(),
            events = src.events.map { it.copy(id = java.util.UUID.randomUUID().toString()) }.toMutableList(),
            markers = src.markers.map { it.copy(id = java.util.UUID.randomUUID().toString()) }.toMutableList()
        )
        _clips.add(duplicated)
        _activeClipId = duplicated.id
        eventBus.emit(AnimationEngineEvent.ClipCreated(duplicated))
        return duplicated
    }

    override fun deleteClip(clipId: String): Boolean {
        if (_clips.size <= 1) return false
        val removed = _clips.removeAll { it.id == clipId }
        if (removed) {
            if (_activeClipId == clipId) {
                _activeClipId = _clips.first().id
            }
            eventBus.emit(AnimationEngineEvent.ClipDeleted(clipId))
            return true
        }
        return false
    }

    override fun renameClip(clipId: String, newName: String): Boolean {
        val clip = _clips.find { it.id == clipId } ?: return false
        clip.name = newName
        return true
    }

    override fun addTrack(clipId: String, category: TrackCategory, name: String, propertyPath: String): TrackData? {
        val clip = _clips.find { it.id == clipId } ?: return null
        val newTrack = TrackData(
            name = name,
            category = category,
            propertyPath = propertyPath,
            defaultValue = if (name.contains("Scale")) 1f else 0f,
            keyframes = mutableListOf(KeyframeData(frame = 0, value = if (name.contains("Scale")) 1f else 0f))
        )
        clip.tracks.add(newTrack)
        eventBus.emit(AnimationEngineEvent.TrackChanged(newTrack.id))
        return newTrack
    }

    override fun removeTrack(clipId: String, trackId: String): Boolean {
        val clip = _clips.find { it.id == clipId } ?: return false
        return clip.tracks.removeAll { it.id == trackId }
    }

    override fun addKeyframe(clipId: String, trackId: String, frame: Int, value: Float, interp: InterpolationType): KeyframeData? {
        val clip = _clips.find { it.id == clipId } ?: return null
        val track = clip.tracks.find { it.id == trackId } ?: return null

        val existing = track.keyframes.find { it.frame == frame }
        if (existing != null) {
            val oldVal = existing.value
            commandHistory.execute(ChangePropertyCommand(existing, oldVal, value) {
                eventBus.emit(AnimationEngineEvent.KeyframeMoved(trackId, existing.id, frame))
            })
            return existing
        }

        val newKf = KeyframeData(frame = frame, value = value, interpolation = interp)
        commandHistory.execute(AddKeyframeCommand(track, newKf) {
            eventBus.emit(AnimationEngineEvent.KeyframeAdded(trackId, newKf))
        })
        return newKf
    }

    override fun removeKeyframe(clipId: String, trackId: String, keyframeId: String): Boolean {
        val clip = _clips.find { it.id == clipId } ?: return false
        val track = clip.tracks.find { it.id == trackId } ?: return false
        val kf = track.keyframes.find { it.id == keyframeId } ?: return false

        return commandHistory.execute(DeleteKeyframeCommand(track, kf) {
            eventBus.emit(AnimationEngineEvent.KeyframeRemoved(trackId, keyframeId))
        })
    }

    override fun moveKeyframe(clipId: String, trackId: String, keyframeId: String, newFrame: Int): Boolean {
        val clip = _clips.find { it.id == clipId } ?: return false
        val track = clip.tracks.find { it.id == trackId } ?: return false
        val kf = track.keyframes.find { it.id == keyframeId } ?: return false
        val oldFrame = kf.frame

        return commandHistory.execute(MoveKeyframeCommand(track, keyframeId, oldFrame, newFrame) {
            eventBus.emit(AnimationEngineEvent.KeyframeMoved(trackId, keyframeId, newFrame))
        })
    }

    override fun setKeyframeValue(clipId: String, trackId: String, keyframeId: String, newValue: Float): Boolean {
        val clip = _clips.find { it.id == clipId } ?: return false
        val track = clip.tracks.find { it.id == trackId } ?: return false
        val kf = track.keyframes.find { it.id == keyframeId } ?: return false
        val oldVal = kf.value

        return commandHistory.execute(ChangePropertyCommand(kf, oldVal, newValue) {
            eventBus.emit(AnimationEngineEvent.PropertyAnimated(track.propertyPath, newValue))
        })
    }

    override fun setInterpolation(clipId: String, trackId: String, keyframeId: String, interp: InterpolationType): Boolean {
        val clip = _clips.find { it.id == clipId } ?: return false
        val track = clip.tracks.find { it.id == trackId } ?: return false
        val kf = track.keyframes.find { it.id == keyframeId } ?: return false
        val oldInterp = kf.interpolation

        return commandHistory.execute(ChangeInterpolationCommand(kf, oldInterp, interp) {
            eventBus.emit(AnimationEngineEvent.TrackChanged(trackId))
        })
    }

    override fun addEvent(clipId: String, frame: Int, name: String, functionName: String, parameters: String): AnimationEventData? {
        val clip = _clips.find { it.id == clipId } ?: return null
        val ev = AnimationEventData(frame = frame, name = name, functionName = functionName, parameters = parameters)
        clip.events.add(ev)
        clip.events.sortBy { it.frame }
        return ev
    }

    override fun removeEvent(clipId: String, eventId: String): Boolean {
        val clip = _clips.find { it.id == clipId } ?: return false
        return clip.events.removeAll { it.id == eventId }
    }

    override fun addMarker(clipId: String, frame: Int, label: String, colorHex: Long): AnimationMarkerData? {
        val clip = _clips.find { it.id == clipId } ?: return null
        val marker = AnimationMarkerData(frame = frame, label = label, colorHex = colorHex)
        clip.markers.add(marker)
        clip.markers.sortBy { it.frame }
        return marker
    }

    override fun removeMarker(clipId: String, markerId: String): Boolean {
        val clip = _clips.find { it.id == clipId } ?: return false
        return clip.markers.removeAll { it.id == markerId }
    }

    // =========================================================================
    // Advanced Timing & Batch Manipulation Engine
    // =========================================================================

    fun moveSelectedKeyframes(clipId: String, keyframeIds: Set<String>, deltaFrames: Int): Boolean {
        if (deltaFrames == 0 || keyframeIds.isEmpty()) return false
        val clip = _clips.find { it.id == clipId } ?: return false

        val moveRecords = mutableListOf<BatchMoveKeyframesCommand.KeyframeMoveRecord>()
        clip.tracks.forEach { track ->
            track.keyframes.filter { keyframeIds.contains(it.id) }.forEach { kf ->
                val targetFrame = (kf.frame + deltaFrames).coerceIn(0, clip.durationFrames + 100)
                moveRecords.add(BatchMoveKeyframesCommand.KeyframeMoveRecord(track.id, kf.id, kf.frame, targetFrame))
            }
        }

        if (moveRecords.isEmpty()) return false
        return commandHistory.execute(BatchMoveKeyframesCommand(clip, moveRecords) {
            eventBus.emit(AnimationEngineEvent.TrackChanged(clipId))
        })
    }

    fun deleteSelectedKeyframes(clipId: String, keyframeIds: Set<String>): Boolean {
        if (keyframeIds.isEmpty()) return false
        val clip = _clips.find { it.id == clipId } ?: return false

        val deletedItems = mutableListOf<Pair<String, KeyframeData>>()
        clip.tracks.forEach { track ->
            track.keyframes.filter { keyframeIds.contains(it.id) }.forEach { kf ->
                deletedItems.add(Pair(track.id, kf.copy()))
            }
        }

        if (deletedItems.isEmpty()) return false
        return commandHistory.execute(BatchDeleteKeyframesCommand(clip, deletedItems) {
            eventBus.emit(AnimationEngineEvent.TrackChanged(clipId))
        })
    }

    var clipboardItems: List<KeyframeClipboardItem> = emptyList()

    fun copySelectedKeyframes(clipId: String, keyframeIds: Set<String>): Int {
        val clip = _clips.find { it.id == clipId } ?: return 0
        val selectedKeys = mutableListOf<Pair<String, KeyframeData>>()

        clip.tracks.forEach { track ->
            track.keyframes.filter { keyframeIds.contains(it.id) }.forEach { kf ->
                selectedKeys.add(Pair(track.id, kf))
            }
        }

        if (selectedKeys.isEmpty()) return 0
        val minFrame = selectedKeys.minOf { it.second.frame }

        clipboardItems = selectedKeys.map { (trackId, kf) ->
            KeyframeClipboardItem(
                trackId = trackId,
                relativeFrame = kf.frame - minFrame,
                keyframe = kf.copy(id = java.util.UUID.randomUUID().toString())
            )
        }
        return clipboardItems.size
    }

    fun pasteKeyframesAt(clipId: String, targetFrame: Int): Set<String> {
        if (clipboardItems.isEmpty()) return emptySet()
        val clip = _clips.find { it.id == clipId } ?: return emptySet()

        val itemsToAdd = mutableListOf<Pair<String, KeyframeData>>()
        val newIds = mutableSetOf<String>()

        clipboardItems.forEach { item ->
            val newFrame = targetFrame + item.relativeFrame
            val newKf = item.keyframe.copy(
                id = java.util.UUID.randomUUID().toString(),
                frame = newFrame
            )
            itemsToAdd.add(Pair(item.trackId, newKf))
            newIds.add(newKf.id)
        }

        val maxTargetFrame = itemsToAdd.maxOfOrNull { it.second.frame } ?: clip.durationFrames
        if (maxTargetFrame > clip.durationFrames) {
            clip.durationFrames = maxTargetFrame
        }

        commandHistory.execute(BatchAddKeyframesCommand(clip, itemsToAdd) {
            eventBus.emit(AnimationEngineEvent.TrackChanged(clipId))
        })
        return newIds
    }

    fun duplicateSelectedKeyframes(clipId: String, keyframeIds: Set<String>, offsetFrames: Int = 1): Set<String> {
        val clip = _clips.find { it.id == clipId } ?: return emptySet()
        val selected = mutableListOf<Pair<String, KeyframeData>>()

        clip.tracks.forEach { track ->
            track.keyframes.filter { keyframeIds.contains(it.id) }.forEach { kf ->
                selected.add(Pair(track.id, kf))
            }
        }

        if (selected.isEmpty()) return emptySet()
        val maxSelectedFrame = selected.maxOf { it.second.frame }
        val minSelectedFrame = selected.minOf { it.second.frame }
        val span = (maxSelectedFrame - minSelectedFrame) + offsetFrames

        val itemsToAdd = mutableListOf<Pair<String, KeyframeData>>()
        val newIds = mutableSetOf<String>()

        selected.forEach { (trackId, kf) ->
            val newFrame = kf.frame + span
            val dupKf = kf.copy(
                id = java.util.UUID.randomUUID().toString(),
                frame = newFrame
            )
            itemsToAdd.add(Pair(trackId, dupKf))
            newIds.add(dupKf.id)
        }

        val maxTargetFrame = itemsToAdd.maxOfOrNull { it.second.frame } ?: clip.durationFrames
        if (maxTargetFrame > clip.durationFrames) {
            clip.durationFrames = maxTargetFrame
        }

        commandHistory.execute(BatchAddKeyframesCommand(clip, itemsToAdd) {
            eventBus.emit(AnimationEngineEvent.TrackChanged(clipId))
        })
        return newIds
    }

    fun scaleKeyframesTiming(clipId: String, keyframeIds: Set<String>, scaleFactor: Float): Boolean {
        if (keyframeIds.size < 2 || scaleFactor <= 0.05f) return false
        val clip = _clips.find { it.id == clipId } ?: return false

        val selectedKeys = mutableListOf<Pair<String, KeyframeData>>()
        clip.tracks.forEach { track ->
            track.keyframes.filter { keyframeIds.contains(it.id) }.forEach { kf ->
                selectedKeys.add(Pair(track.id, kf))
            }
        }

        if (selectedKeys.size < 2) return false
        val minFrame = selectedKeys.minOf { it.second.frame }

        val records = selectedKeys.map { (trackId, kf) ->
            val rel = kf.frame - minFrame
            val newRel = (rel * scaleFactor).roundToInt()
            ScaleKeyframesTimingCommand.KeyframeScaleRecord(
                trackId = trackId,
                keyframeId = kf.id,
                oldFrame = kf.frame,
                newFrame = minFrame + newRel
            )
        }

        return commandHistory.execute(ScaleKeyframesTimingCommand(clip, records) {
            eventBus.emit(AnimationEngineEvent.TrackChanged(clipId))
        })
    }

    fun scaleKeyframesToDuration(clipId: String, keyframeIds: Set<String>, targetDuration: Int): Boolean {
        if (keyframeIds.size < 2 || targetDuration < 1) return false
        val clip = _clips.find { it.id == clipId } ?: return false

        val selectedKeys = mutableListOf<Pair<String, KeyframeData>>()
        clip.tracks.forEach { track ->
            track.keyframes.filter { keyframeIds.contains(it.id) }.forEach { kf ->
                selectedKeys.add(Pair(track.id, kf))
            }
        }

        if (selectedKeys.size < 2) return false
        val minFrame = selectedKeys.minOf { it.second.frame }
        val maxFrame = selectedKeys.maxOf { it.second.frame }
        val originalDuration = (maxFrame - minFrame).coerceAtLeast(1)

        val scaleFactor = targetDuration.toFloat() / originalDuration.toFloat()
        return scaleKeyframesTiming(clipId, keyframeIds, scaleFactor)
    }

    fun distributeKeyframesEvenly(clipId: String, keyframeIds: Set<String>): Boolean {
        if (keyframeIds.size < 3) return false
        val clip = _clips.find { it.id == clipId } ?: return false

        val records = mutableListOf<DistributeKeyframesCommand.KeyframeDistributionRecord>()

        clip.tracks.forEach { track ->
            val keysOnTrack = track.keyframes.filter { keyframeIds.contains(it.id) }.sortedBy { it.frame }
            if (keysOnTrack.size >= 3) {
                val minF = keysOnTrack.first().frame
                val maxF = keysOnTrack.last().frame
                val count = keysOnTrack.size
                val step = (maxF - minF).toFloat() / (count - 1).toFloat()

                keysOnTrack.forEachIndexed { idx, kf ->
                    val newF = (minF + idx * step).roundToInt()
                    records.add(DistributeKeyframesCommand.KeyframeDistributionRecord(track.id, kf.id, kf.frame, newF))
                }
            }
        }

        if (records.isEmpty()) return false
        return commandHistory.execute(DistributeKeyframesCommand(clip, records) {
            eventBus.emit(AnimationEngineEvent.TrackChanged(clipId))
        })
    }

    fun reverseKeyframesTiming(clipId: String, keyframeIds: Set<String>): Boolean {
        if (keyframeIds.size < 2) return false
        val clip = _clips.find { it.id == clipId } ?: return false

        val records = mutableListOf<ReverseKeyframesCommand.KeyframeReverseRecord>()

        clip.tracks.forEach { track ->
            val keysOnTrack = track.keyframes.filter { keyframeIds.contains(it.id) }.sortedBy { it.frame }
            if (keysOnTrack.size >= 2) {
                val minF = keysOnTrack.first().frame
                val maxF = keysOnTrack.last().frame
                val framesList = keysOnTrack.map { it.frame }
                val reversedFrames = framesList.reversed()

                keysOnTrack.forEachIndexed { idx, kf ->
                    records.add(ReverseKeyframesCommand.KeyframeReverseRecord(track.id, kf.id, kf.frame, reversedFrames[idx]))
                }
            }
        }

        if (records.isEmpty()) return false
        return commandHistory.execute(ReverseKeyframesCommand(clip, records) {
            eventBus.emit(AnimationEngineEvent.TrackChanged(clipId))
        })
    }

    fun holdKeyframes(clipId: String, keyframeIds: Set<String>): Boolean {
        val clip = _clips.find { it.id == clipId } ?: return false
        var modified = false
        clip.tracks.forEach { track ->
            track.keyframes.filter { keyframeIds.contains(it.id) }.forEach { kf ->
                kf.interpolation = InterpolationType.CONSTANT
                kf.type = KeyframeType.HOLD
                modified = true
            }
        }
        if (modified) {
            eventBus.emit(AnimationEngineEvent.TrackChanged(clipId))
        }
        return modified
    }

    fun stretchSelectedKeyframes(clipId: String, keyframeIds: Set<String>, amount: Int = 1): Boolean {
        val clip = _clips.find { it.id == clipId } ?: return false
        val selectedKeys = clip.tracks.flatMap { it.keyframes }.filter { keyframeIds.contains(it.id) }
        if (selectedKeys.size < 2) return false
        val minF = selectedKeys.minOf { it.frame }
        val maxF = selectedKeys.maxOf { it.frame }
        val currentDuration = (maxF - minF).coerceAtLeast(1)
        val newDuration = currentDuration + amount
        return scaleKeyframesToDuration(clipId, keyframeIds, newDuration)
    }

    fun compressSelectedKeyframes(clipId: String, keyframeIds: Set<String>, amount: Int = 1): Boolean {
        val clip = _clips.find { it.id == clipId } ?: return false
        val selectedKeys = clip.tracks.flatMap { it.keyframes }.filter { keyframeIds.contains(it.id) }
        if (selectedKeys.size < 2) return false
        val minF = selectedKeys.minOf { it.frame }
        val maxF = selectedKeys.maxOf { it.frame }
        val currentDuration = (maxF - minF).coerceAtLeast(1)
        val newDuration = (currentDuration - amount).coerceAtLeast(1)
        return scaleKeyframesToDuration(clipId, keyframeIds, newDuration)
    }

    fun holdSelectedKeyframes(clipId: String, keyframeIds: Set<String>): Boolean =
        holdKeyframes(clipId, keyframeIds)

    fun removeGapsBetweenKeyframes(clipId: String, fixedGap: Int = 2): Boolean =
        removeGaps(clipId, fixedGap)

    fun insertTimeAt(clipId: String, atFrame: Int, framesToInsert: Int): Boolean {
        if (framesToInsert <= 0) return false
        val clip = _clips.find { it.id == clipId } ?: return false

        return commandHistory.execute(InsertTimeCommand(clip, atFrame, framesToInsert) {
            eventBus.emit(AnimationEngineEvent.TrackChanged(clipId))
        })
    }

    fun deleteTimeRange(clipId: String, startFrame: Int, endFrame: Int): Boolean {
        if (startFrame > endFrame) return false
        val clip = _clips.find { it.id == clipId } ?: return false

        return commandHistory.execute(DeleteTimeRangeCommand(clip, startFrame, endFrame) {
            eventBus.emit(AnimationEngineEvent.TrackChanged(clipId))
        })
    }

    fun removeGaps(clipId: String, fixedGap: Int = 2): Boolean {
        val clip = _clips.find { it.id == clipId } ?: return false
        return commandHistory.execute(RemoveGapsCommand(clip, fixedGap) {
            eventBus.emit(AnimationEngineEvent.TrackChanged(clipId))
        })
    }

    fun toggleTrackSolo(clipId: String, trackId: String) {
        val clip = _clips.find { it.id == clipId } ?: return
        val target = clip.tracks.find { it.id == trackId } ?: return
        target.isSolo = !target.isSolo
        eventBus.emit(AnimationEngineEvent.TrackChanged(trackId))
    }

    fun toggleTrackMute(clipId: String, trackId: String) {
        val clip = _clips.find { it.id == clipId } ?: return
        val target = clip.tracks.find { it.id == trackId } ?: return
        target.isMuted = !target.isMuted
        eventBus.emit(AnimationEngineEvent.TrackChanged(trackId))
    }

    fun toggleTrackFocus(clipId: String, trackId: String) {
        val clip = _clips.find { it.id == clipId } ?: return
        val target = clip.tracks.find { it.id == trackId } ?: return
        val newFocusedState = !target.isFocused
        clip.tracks.forEach { it.isFocused = false }
        target.isFocused = newFocusedState
        eventBus.emit(AnimationEngineEvent.TrackChanged(trackId))
    }

    // =========================================================================
    // Mathematical Interpolation Evaluation Engine
    // =========================================================================
    override fun evaluateTrackAt(track: TrackData, frame: Float): Float {
        if (track.keyframes.isEmpty()) return track.defaultValue
        if (track.keyframes.size == 1) return track.keyframes[0].value

        val sorted = track.keyframes.sortedBy { it.frame }

        if (frame <= sorted.first().frame) return sorted.first().value
        if (frame >= sorted.last().frame) return sorted.last().value

        for (i in 0 until sorted.size - 1) {
            val k0 = sorted[i]
            val k1 = sorted[i + 1]

            if (frame >= k0.frame && frame <= k1.frame) {
                val span = (k1.frame - k0.frame).toFloat()
                if (span <= 0.0001f) return k0.value

                val rawT = (frame - k0.frame) / span
                val t = rawT.coerceIn(0f, 1f)

                return when (k0.interpolation) {
                    InterpolationType.CONSTANT -> k0.value
                    InterpolationType.LINEAR -> k0.value + (k1.value - k0.value) * t
                    InterpolationType.EASE_IN -> {
                        val easedT = t * t * t
                        k0.value + (k1.value - k0.value) * easedT
                    }
                    InterpolationType.EASE_OUT -> {
                        val easedT = 1f - (1f - t).pow(3)
                        k0.value + (k1.value - k0.value) * easedT
                    }
                    InterpolationType.EASE_IN_OUT -> {
                        val easedT = if (t < 0.5f) {
                            4f * t * t * t
                        } else {
                            1f - (-2f * t + 2f).pow(3) / 2f
                        }
                        k0.value + (k1.value - k0.value) * easedT
                    }
                    InterpolationType.BEZIER -> {
                        // Cubic Hermite / Bezier with handle calculation
                        val p0 = k0.value
                        val p3 = k1.value
                        val p1 = p0 + (p3 - p0) * k0.handleOut.y
                        val p2 = p0 + (p3 - p0) * k1.handleIn.y

                        val u = 1f - t
                        val tt = t * t
                        val uu = u * u
                        val uuu = uu * u
                        val ttt = tt * t

                        (uuu * p0) + (3f * uu * t * p1) + (3f * u * tt * p2) + (ttt * p3)
                    }
                }
            }
        }
        return sorted.last().value
    }
}

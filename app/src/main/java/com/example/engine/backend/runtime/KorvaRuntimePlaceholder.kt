package com.example.engine.backend.runtime

/**
 * ============================================================================
 * Korva Engine - Native C++ Backend Integration Placeholder
 * ============================================================================
 *
 * When the real C++ Korva Core Engine is ready to be linked:
 * 1. Place C++ sources in `cpp/KorvaRuntime/` (Core, Renderer, Physics, Audio, Scripting).
 * 2. Implement `KorvaEngineJniAdapter` implementing `com.example.engine.interfaces.IEngine`.
 * 3. Swap the dependency injection binding from `MockKorvaEngine` to `KorvaEngineJniAdapter`.
 *
 * The GUI layer (Compose screens, Panels, Dynamic Property Cards, Timeline,
 * Layer Manager, Viewport) will work seamlessly without modifying ANY GUI code!
 *
 * Flow:
 * User Touch -> Korva GUI (Compose) -> IEngine & ICommand -> JNI Adapter -> C++ Korva Core -> GPU/Vulkan
 */
object KorvaRuntimePlaceholder {
    const val STATUS = "READY_FOR_CPP_CORE_DROP_IN"
    const val SPECIFICATION_VERSION = "2.0.0-PRO"
    const val TARGET_PLATFORM = "Android 2D Pure Mobile Engine"
}

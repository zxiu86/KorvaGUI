package com.example.engine.backend.mock

import com.example.engine.interfaces.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class MockEventBus : IEventBus {
    private val _events = MutableSharedFlow<EngineEvent>(extraBufferCapacity = 64)
    override val events: SharedFlow<EngineEvent> = _events.asSharedFlow()

    override fun post(event: EngineEvent) {
        _events.tryEmit(event)
    }
}

class MockCommandHistory(private val eventBus: IEventBus? = null) : ICommandHistory {
    private val _undoStack = mutableListOf<ICommand>()
    private val _redoStack = mutableListOf<ICommand>()

    override val canUndo: Boolean get() = _undoStack.isNotEmpty()
    override val canRedo: Boolean get() = _redoStack.isNotEmpty()
    override val historySize: Int get() = _undoStack.size
    override val undoStack: List<ICommand> get() = _undoStack.toList()
    override val redoStack: List<ICommand> get() = _redoStack.toList()

    override fun execute(command: ICommand): Boolean {
        val success = command.execute()
        if (success) {
            _undoStack.add(command)
            _redoStack.clear()
        }
        return success
    }

    override fun undo(): Boolean {
        if (_undoStack.isEmpty()) return false
        val command = _undoStack.removeAt(_undoStack.size - 1)
        val success = command.undo()
        if (success) {
            _redoStack.add(command)
        }
        return success
    }

    override fun redo(): Boolean {
        if (_redoStack.isEmpty()) return false
        val command = _redoStack.removeAt(_redoStack.size - 1)
        val success = command.redo()
        if (success) {
            _undoStack.add(command)
        }
        return success
    }

    override fun clear() {
        _undoStack.clear()
        _redoStack.clear()
    }
}

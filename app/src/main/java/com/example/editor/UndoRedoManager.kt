package com.example.editor

import com.example.data.model.TimelineClip
import java.util.ArrayDeque

class UndoRedoManager(private val maxCapacity: Int = 30) {
    private val undoStack = ArrayDeque<List<TimelineClip>>()
    private val redoStack = ArrayDeque<List<TimelineClip>>()

    val canUndo: Boolean
        get() = undoStack.isNotEmpty()

    val canRedo: Boolean
        get() = redoStack.isNotEmpty()

    fun pushState(currentState: List<TimelineClip>) {
        if (undoStack.size >= maxCapacity) {
            undoStack.removeLast()
        }
        undoStack.push(currentState)
        redoStack.clear()
    }

    fun undo(currentState: List<TimelineClip>): List<TimelineClip>? {
        if (!canUndo) return null
        val previousState = undoStack.pop()
        redoStack.push(currentState)
        return previousState
    }

    fun redo(currentState: List<TimelineClip>): List<TimelineClip>? {
        if (!canRedo) return null
        val nextState = redoStack.pop()
        undoStack.push(currentState)
        return nextState
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}

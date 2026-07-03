package org.videolan.vlc.media

import android.support.v4.media.session.PlaybackStateCompat

internal object PlaylistIndexResolver {

    data class NavigationIndices(val previousIndex: Int, val nextIndex: Int)

    enum class RemovedCurrentAction {
        None,
        PlayNext,
        PlayCurrent,
        Stop
    }

    fun afterItemAdded(currentIndex: Int, addedIndex: Int, expanding: Boolean): Int {
        return if (!expanding && currentIndex >= addedIndex) currentIndex + 1 else currentIndex
    }

    fun afterItemRemoved(currentIndex: Int, removedIndex: Int, expanding: Boolean): Int {
        return if (!expanding && currentIndex >= removedIndex) currentIndex - 1 else currentIndex
    }

    fun afterCurrentItemRemoved(currentRemoved: Boolean, expanding: Boolean, currentIndex: Int, nextIndex: Int): RemovedCurrentAction {
        if (!currentRemoved || expanding) return RemovedCurrentAction.None
        return when {
            nextIndex != -1 -> RemovedCurrentAction.PlayNext
            currentIndex != -1 -> RemovedCurrentAction.PlayCurrent
            else -> RemovedCurrentAction.Stop
        }
    }

    fun afterItemMoved(currentIndex: Int, indexBefore: Int, indexAfter: Int): Int {
        return when (currentIndex) {
            indexBefore -> if (indexAfter > indexBefore) indexAfter - 1 else indexAfter
            in indexAfter until indexBefore -> currentIndex + 1
            in (indexBefore + 1) until indexAfter -> currentIndex - 1
            else -> currentIndex
        }
    }

    fun linearNavigation(currentIndex: Int, size: Int, repeatMode: Int): NavigationIndices {
        val previousIndex = if (currentIndex > 0) currentIndex - 1 else -1
        val nextIndex = when {
            currentIndex + 1 < size -> currentIndex + 1
            repeatMode == PlaybackStateCompat.REPEAT_MODE_NONE -> -1
            size > 0 -> 0
            else -> -1
        }
        return NavigationIndices(previousIndex, nextIndex)
    }

    fun validShuffleHistoryTop(history: List<Int>, size: Int): List<Int> {
        var validSize = history.size
        while (validSize > 0 && history[validSize - 1] !in 0 until size) {
            validSize--
        }
        return history.take(validSize)
    }
}

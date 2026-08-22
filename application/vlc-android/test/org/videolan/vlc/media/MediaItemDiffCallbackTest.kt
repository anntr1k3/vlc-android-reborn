package org.videolan.vlc.media

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.videolan.medialibrary.media.DummyItem
import org.videolan.vlc.util.MediaItemDiffCallback

class MediaItemDiffCallbackTest {

    @Test
    fun areItemsTheSame_withSameReference_returnsTrue() {
        val callback = MediaItemDiffCallback<DummyItem>()
        val item = DummyItem(1L, "Song 1", "Desc")
        callback.oldList = listOf(item)
        callback.newList = listOf(item)

        assertTrue(callback.areItemsTheSame(0, 0))
    }

    @Test
    fun areItemsTheSame_withEqualDistinctInstances_returnsTrue() {
        val callback = MediaItemDiffCallback<DummyItem>()
        val item1 = DummyItem(1L, "Song 1", "Desc 1")
        val item2 = DummyItem(1L, "Song 1", "Desc 2")
        callback.oldList = listOf(item1)
        callback.newList = listOf(item2)

        assertTrue(callback.areItemsTheSame(0, 0))
    }

    @Test
    fun areItemsTheSame_withDifferentIds_returnsFalse() {
        val callback = MediaItemDiffCallback<DummyItem>()
        val item1 = DummyItem(1L, "Song 1", "Desc")
        val item2 = DummyItem(2L, "Song 2", "Desc")
        callback.oldList = listOf(item1)
        callback.newList = listOf(item2)

        assertFalse(callback.areItemsTheSame(0, 0))
    }

    @Test
    fun areContentsTheSame_withSameIdAndTitle_returnsTrue() {
        val callback = MediaItemDiffCallback<DummyItem>()
        val item1 = DummyItem(1L, "Song 1", "Desc 1")
        val item2 = DummyItem(1L, "Song 1", "Desc 2")
        callback.oldList = listOf(item1)
        callback.newList = listOf(item2)

        assertTrue(callback.areContentsTheSame(0, 0))
    }

    @Test
    fun areContentsTheSame_withChangedTitle_returnsFalse() {
        val callback = MediaItemDiffCallback<DummyItem>()
        val item1 = DummyItem(1L, "Song 1", "Desc")
        val item2 = DummyItem(1L, "Song 1 Renamed", "Desc")
        callback.oldList = listOf(item1)
        callback.newList = listOf(item2)

        assertFalse(callback.areContentsTheSame(0, 0))
    }
}

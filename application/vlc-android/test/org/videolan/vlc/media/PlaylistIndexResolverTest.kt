package org.videolan.vlc.media

import android.support.v4.media.session.PlaybackStateCompat
import junit.framework.TestCase.assertEquals
import org.junit.Test

class PlaylistIndexResolverTest {

    @Test
    fun afterItemAdded_beforeCurrentIndex_ShiftsCurrentIndexForward() {
        assertEquals(3, PlaylistIndexResolver.afterItemAdded(currentIndex = 2, addedIndex = 1, expanding = false))
    }

    @Test
    fun afterItemAdded_atCurrentIndex_ShiftsCurrentIndexForward() {
        assertEquals(3, PlaylistIndexResolver.afterItemAdded(currentIndex = 2, addedIndex = 2, expanding = false))
    }

    @Test
    fun afterItemAdded_afterCurrentIndex_KeepsCurrentIndex() {
        assertEquals(2, PlaylistIndexResolver.afterItemAdded(currentIndex = 2, addedIndex = 3, expanding = false))
    }

    @Test
    fun afterItemAdded_whileExpanding_KeepsCurrentIndex() {
        assertEquals(2, PlaylistIndexResolver.afterItemAdded(currentIndex = 2, addedIndex = 1, expanding = true))
    }

    @Test
    fun afterItemRemoved_beforeCurrentIndex_ShiftsCurrentIndexBack() {
        assertEquals(1, PlaylistIndexResolver.afterItemRemoved(currentIndex = 2, removedIndex = 1, expanding = false))
    }

    @Test
    fun afterItemRemoved_atCurrentIndex_ShiftsCurrentIndexBack() {
        assertEquals(1, PlaylistIndexResolver.afterItemRemoved(currentIndex = 2, removedIndex = 2, expanding = false))
    }

    @Test
    fun afterItemRemoved_afterCurrentIndex_KeepsCurrentIndex() {
        assertEquals(2, PlaylistIndexResolver.afterItemRemoved(currentIndex = 2, removedIndex = 3, expanding = false))
    }

    @Test
    fun afterItemRemoved_whileExpanding_KeepsCurrentIndex() {
        assertEquals(2, PlaylistIndexResolver.afterItemRemoved(currentIndex = 2, removedIndex = 1, expanding = true))
    }

    @Test
    fun afterCurrentItemRemoved_whenDifferentItemWasRemoved_DoesNothing() {
        assertEquals(
                PlaylistIndexResolver.RemovedCurrentAction.None,
                PlaylistIndexResolver.afterCurrentItemRemoved(currentRemoved = false, expanding = false, currentIndex = 1, nextIndex = 2)
        )
    }

    @Test
    fun afterCurrentItemRemoved_whileExpanding_DoesNothing() {
        assertEquals(
                PlaylistIndexResolver.RemovedCurrentAction.None,
                PlaylistIndexResolver.afterCurrentItemRemoved(currentRemoved = true, expanding = true, currentIndex = 1, nextIndex = 2)
        )
    }

    @Test
    fun afterCurrentItemRemoved_withNextIndex_PlaysNext() {
        assertEquals(
                PlaylistIndexResolver.RemovedCurrentAction.PlayNext,
                PlaylistIndexResolver.afterCurrentItemRemoved(currentRemoved = true, expanding = false, currentIndex = 1, nextIndex = 2)
        )
    }

    @Test
    fun afterCurrentItemRemoved_withoutNextIndexAndValidCurrentIndex_PlaysCurrent() {
        assertEquals(
                PlaylistIndexResolver.RemovedCurrentAction.PlayCurrent,
                PlaylistIndexResolver.afterCurrentItemRemoved(currentRemoved = true, expanding = false, currentIndex = 0, nextIndex = -1)
        )
    }

    @Test
    fun afterCurrentItemRemoved_withoutNextIndexOrCurrentIndex_Stops() {
        assertEquals(
                PlaylistIndexResolver.RemovedCurrentAction.Stop,
                PlaylistIndexResolver.afterCurrentItemRemoved(currentRemoved = true, expanding = false, currentIndex = -1, nextIndex = -1)
        )
    }

    @Test
    fun afterItemMoved_whenCurrentItemMovesForward_TracksNewIndexAfterRemovalAdjustment() {
        assertEquals(3, PlaylistIndexResolver.afterItemMoved(currentIndex = 1, indexBefore = 1, indexAfter = 4))
    }

    @Test
    fun afterItemMoved_whenCurrentItemMovesBack_TracksNewIndex() {
        assertEquals(1, PlaylistIndexResolver.afterItemMoved(currentIndex = 4, indexBefore = 4, indexAfter = 1))
    }

    @Test
    fun afterItemMoved_whenEarlierItemMovesAfterCurrent_ShiftsCurrentIndexBack() {
        assertEquals(1, PlaylistIndexResolver.afterItemMoved(currentIndex = 2, indexBefore = 0, indexAfter = 3))
    }

    @Test
    fun afterItemMoved_whenLaterItemMovesBeforeCurrent_ShiftsCurrentIndexForward() {
        assertEquals(3, PlaylistIndexResolver.afterItemMoved(currentIndex = 2, indexBefore = 4, indexAfter = 1))
    }

    @Test
    fun afterItemMoved_whenMoveDoesNotCrossCurrentIndex_KeepsCurrentIndex() {
        assertEquals(4, PlaylistIndexResolver.afterItemMoved(currentIndex = 4, indexBefore = 0, indexAfter = 2))
    }

    @Test
    fun linearNavigation_fromMiddle_UsesPreviousAndNextItems() {
        assertEquals(
                PlaylistIndexResolver.NavigationIndices(previousIndex = 1, nextIndex = 3),
                PlaylistIndexResolver.linearNavigation(
                        currentIndex = 2,
                        size = 5,
                        repeatMode = PlaybackStateCompat.REPEAT_MODE_NONE
                )
        )
    }

    @Test
    fun linearNavigation_fromFirstItem_HasNoPrevious() {
        assertEquals(
                PlaylistIndexResolver.NavigationIndices(previousIndex = -1, nextIndex = 1),
                PlaylistIndexResolver.linearNavigation(
                        currentIndex = 0,
                        size = 3,
                        repeatMode = PlaybackStateCompat.REPEAT_MODE_NONE
                )
        )
    }

    @Test
    fun linearNavigation_fromLastItemWithoutRepeat_HasNoNext() {
        assertEquals(
                PlaylistIndexResolver.NavigationIndices(previousIndex = 1, nextIndex = -1),
                PlaylistIndexResolver.linearNavigation(
                        currentIndex = 2,
                        size = 3,
                        repeatMode = PlaybackStateCompat.REPEAT_MODE_NONE
                )
        )
    }

    @Test
    fun linearNavigation_fromLastItemWithRepeat_WrapsToFirstItem() {
        assertEquals(
                PlaylistIndexResolver.NavigationIndices(previousIndex = 1, nextIndex = 0),
                PlaylistIndexResolver.linearNavigation(
                        currentIndex = 2,
                        size = 3,
                        repeatMode = PlaybackStateCompat.REPEAT_MODE_ALL
                )
        )
    }

    @Test
    fun linearNavigation_withEmptyList_HasNoPreviousOrNext() {
        assertEquals(
                PlaylistIndexResolver.NavigationIndices(previousIndex = -1, nextIndex = -1),
                PlaylistIndexResolver.linearNavigation(
                        currentIndex = -1,
                        size = 0,
                        repeatMode = PlaybackStateCompat.REPEAT_MODE_ALL
                )
        )
    }

    @Test
    fun validShuffleHistoryTop_withValidTop_KeepsHistory() {
        assertEquals(listOf(0, 5, 2), PlaylistIndexResolver.validShuffleHistoryTop(listOf(0, 5, 2), size = 3))
    }

    @Test
    fun validShuffleHistoryTop_withInvalidTop_RemovesInvalidTailOnly() {
        assertEquals(listOf(0, 2), PlaylistIndexResolver.validShuffleHistoryTop(listOf(0, 2, 4, -1), size = 3))
    }

    @Test
    fun validShuffleHistoryTop_withNoValidTop_ReturnsEmptyHistory() {
        assertEquals(emptyList<Int>(), PlaylistIndexResolver.validShuffleHistoryTop(listOf(4, -1), size = 3))
    }
}

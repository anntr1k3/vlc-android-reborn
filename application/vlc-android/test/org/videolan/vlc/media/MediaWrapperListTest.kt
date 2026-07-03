package org.videolan.vlc.media

import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertSame
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.videolan.medialibrary.interfaces.media.MediaWrapper

class MediaWrapperListTest {

    private val mediaList = MediaWrapperList()
    private val listener = RecordingListener()

    @Test
    fun whenMediaIsAdded_ListContainsMediaAndListenerIsNotified() {
        mediaList.addEventListener(listener)
        val media = media("file:///audio-1.mp3", MediaWrapper.TYPE_AUDIO)

        mediaList.add(media)

        assertEquals(1, mediaList.size())
        assertSame(media, mediaList.getMedia(0))
        assertTrue(mediaList.isAudioList)
        assertEquals(listOf(Event.Added(0, media.location)), listener.events)
    }

    @Test
    fun whenVideoIsAdded_ListIsNotAudioOnlyUntilVideoIsRemoved() {
        val audio = media("file:///audio-1.mp3", MediaWrapper.TYPE_AUDIO)
        val video = media("file:///video-1.mp4", MediaWrapper.TYPE_VIDEO)

        mediaList.add(audio)
        mediaList.add(video)

        assertFalse(mediaList.isAudioList)

        mediaList.remove(video.location)

        assertTrue(mediaList.isAudioList)
        assertEquals(listOf(audio), mediaList.copy)
    }

    @Test
    fun whenMediaIsInsertedPastEnd_ListenerReceivesActualInsertedIndex() {
        mediaList.addEventListener(listener)
        val first = media("file:///audio-1.mp3")
        val second = media("file:///audio-2.mp3")
        mediaList.add(first)

        mediaList.insert(10, second)

        assertEquals(listOf(first, second), mediaList.copy)
        assertEquals(
                listOf(
                        Event.Added(0, first.location),
                        Event.Added(1, second.location)
                ),
                listener.events
        )
    }

    @Test
    fun whenMediaIsMoved_ListOrderAndListenerAreUpdated() {
        mediaList.addEventListener(listener)
        val first = media("file:///audio-1.mp3")
        val second = media("file:///audio-2.mp3")
        val third = media("file:///audio-3.mp3")
        mediaList.map(listOf(first, second, third))

        mediaList.move(0, 3)

        assertEquals(listOf(second, third, first), mediaList.copy)
        assertEquals(listOf(Event.Moved(0, 3, first.location)), listener.events)
    }

    @Test
    fun whenMediaIsRemovedByPosition_ListAndListenerAreUpdated() {
        mediaList.addEventListener(listener)
        val first = media("file:///audio-1.mp3")
        val second = media("file:///audio-2.mp3")
        mediaList.map(listOf(first, second))

        mediaList.remove(0)

        assertEquals(listOf(second), mediaList.copy)
        assertEquals(listOf(Event.Removed(0, first.location)), listener.events)
    }

    @Test
    fun whenMediaIsRemovedByLocation_AllMatchingItemsAreRemoved() {
        mediaList.addEventListener(listener)
        val first = media("file:///audio-1.mp3")
        val second = media("file:///audio-2.mp3")
        val duplicate = media(first.location)
        mediaList.map(listOf(first, second, duplicate))

        mediaList.remove(first.location)

        assertEquals(listOf(second), mediaList.copy)
        assertEquals(
                listOf(
                        Event.Removed(0, first.location),
                        Event.Removed(1, duplicate.location)
                ),
                listener.events
        )
    }

    @Test
    fun whenListIsCleared_AllRemoveEventsAreSent() {
        mediaList.addEventListener(listener)
        val first = media("file:///audio-1.mp3")
        val second = media("file:///audio-2.mp3")
        mediaList.map(listOf(first, second))

        mediaList.clear()

        assertEquals(0, mediaList.size())
        assertTrue(mediaList.isAudioList)
        assertEquals(
                listOf(
                        Event.Removed(0, first.location),
                        Event.Removed(1, second.location)
                ),
                listener.events
        )
    }

    @Test
    fun whenListenerChangesSubscriptionsDuringCallback_DispatchContinuesFromSnapshot() {
        val secondListener = RecordingListener()
        val removingListener = object : RecordingListener() {
            override fun onItemAdded(index: Int, mrl: String) {
                super.onItemAdded(index, mrl)
                mediaList.removeEventListener(this)
                mediaList.addEventListener(RecordingListener())
            }
        }
        mediaList.addEventListener(removingListener)
        mediaList.addEventListener(secondListener)
        val first = media("file:///audio-1.mp3")
        val second = media("file:///audio-2.mp3")

        mediaList.add(first)
        mediaList.add(second)

        assertEquals(listOf(Event.Added(0, first.location)), removingListener.events)
        assertEquals(
                listOf(
                        Event.Added(0, first.location),
                        Event.Added(1, second.location)
                ),
                secondListener.events
        )
    }

    private fun media(location: String, type: Int = MediaWrapper.TYPE_AUDIO): MediaWrapper =
            mockk {
                every { this@mockk.location } returns location
                every { this@mockk.type } returns type
            }

    private sealed class Event {
        data class Added(val index: Int, val mrl: String) : Event()
        data class Removed(val index: Int, val mrl: String) : Event()
        data class Moved(val indexBefore: Int, val indexAfter: Int, val mrl: String) : Event()
    }

    private open class RecordingListener : MediaWrapperList.EventListener {
        val events = ArrayList<Event>()

        override fun onItemAdded(index: Int, mrl: String) {
            events.add(Event.Added(index, mrl))
        }

        override fun onItemRemoved(index: Int, mrl: String) {
            events.add(Event.Removed(index, mrl))
        }

        override fun onItemMoved(indexBefore: Int, indexAfter: Int, mrl: String) {
            events.add(Event.Moved(indexBefore, indexAfter, mrl))
        }
    }
}

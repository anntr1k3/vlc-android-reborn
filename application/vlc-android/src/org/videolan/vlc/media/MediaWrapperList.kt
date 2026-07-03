/*****************************************************************************
 * MediaWrapperList.java
 *
 * Copyright © 2013-2015 VLC authors and VideoLAN
 * Copyright © 2013 Edward Wang
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 */
package org.videolan.vlc.media

import org.videolan.medialibrary.interfaces.media.MediaWrapper
import java.util.*

class MediaWrapperList {

    private val internalList = ArrayList<MediaWrapper>()
    private val eventListenerList = ArrayList<EventListener>()
    private var videoCount = 0

    val copy: MutableList<MediaWrapper>
        @Synchronized get() = ArrayList(internalList)

    val isAudioList: Boolean
        @Synchronized get() = videoCount == 0

    interface EventListener {
        fun onItemAdded(index: Int, mrl: String)
        fun onItemRemoved(index: Int, mrl: String)
        fun onItemMoved(indexBefore: Int, indexAfter: Int, mrl: String)
    }

    fun add(media: MediaWrapper) {
        val event = synchronized(this) {
            internalList.add(media)
            if (media.type == MediaWrapper.TYPE_VIDEO)
                ++videoCount
            Event(EVENT_ADDED, internalList.size - 1, -1, media.location)
        }
        signalEventListeners(event)
    }

    @Synchronized
    fun addEventListener(listener: EventListener) {
        if (!eventListenerList.contains(listener))
            eventListenerList.add(listener)
    }

    @Synchronized
    fun removeEventListener(listener: EventListener) {
        eventListenerList.remove(listener)
    }

    private fun signalEventListeners(event: Event) {
        val listeners = synchronized(this) { ArrayList(eventListenerList) }
        for (listener in listeners) {
            when (event.type) {
                EVENT_ADDED -> listener.onItemAdded(event.arg1, event.mrl)
                EVENT_REMOVED -> listener.onItemRemoved(event.arg1, event.mrl)
                EVENT_MOVED -> listener.onItemMoved(event.arg1, event.arg2, event.mrl)
            }
        }
    }

    /**
     * Clear the media list. (remove all media)
     */
    fun clear() {
        val events = synchronized(this) {
            val removed = internalList.mapIndexed { index, media ->
                Event(EVENT_REMOVED, index, -1, media.location)
            }
            internalList.clear()
            videoCount = 0
            removed
        }
        events.forEach(::signalEventListeners)
    }

    @Synchronized
    private fun isValid(position: Int): Boolean {
        return position >= 0 && position < internalList.size
    }

    fun insert(position: Int, media: MediaWrapper) {
        if (position < 0) return
        val event = synchronized(this) {
            val insertPosition = position.coerceAtMost(internalList.size)
            internalList.add(insertPosition, media)
            if (media.type == MediaWrapper.TYPE_VIDEO)
                ++videoCount
            Event(EVENT_ADDED, insertPosition, -1, media.location)
        }
        signalEventListeners(event)
    }

    /**
     * Move a media from one position to another
     *
     * @param startPosition start position
     * @param endPosition end position
     * @throws IndexOutOfBoundsException
     */
    fun move(startPosition: Int, endPosition: Int) {
        val event = synchronized(this) {
            if (!(isValid(startPosition)
                            && endPosition >= 0 && endPosition <= internalList.size))
                throw IndexOutOfBoundsException("Indexes out of range")

            val toMove = internalList[startPosition]
            internalList.removeAt(startPosition)
            if (startPosition >= endPosition)
                internalList.add(endPosition, toMove)
            else
                internalList.add(endPosition - 1, toMove)
            Event(EVENT_MOVED, startPosition, endPosition, toMove.location)
        }
        signalEventListeners(event)
    }

    fun remove(position: Int) {
        val event = synchronized(this) {
            if (!isValid(position)) return
            if (internalList[position].type == MediaWrapper.TYPE_VIDEO)
                --videoCount
            val uri = internalList[position].location
            internalList.removeAt(position)
            Event(EVENT_REMOVED, position, -1, uri)
        }
        signalEventListeners(event)
    }

    fun remove(location: String) {
        val events = synchronized(this) {
            val removed = ArrayList<Event>()
            var i = 0
            while (i < internalList.size) {
                val uri = internalList[i].location
                if (uri == location) {
                    if (internalList[i].type == MediaWrapper.TYPE_VIDEO)
                        --videoCount
                    internalList.removeAt(i)
                    removed.add(Event(EVENT_REMOVED, i, -1, uri))
                    i--
                }
                ++i
            }
            removed
        }
        events.forEach(::signalEventListeners)
    }

    @Synchronized
    fun size(): Int {
        return internalList.size
    }

    @Synchronized
    fun getMedia(position: Int): MediaWrapper? {
        return if (isValid(position)) internalList[position] else null
    }

    @Synchronized
    fun replaceWith(list: List<MediaWrapper>) {
        internalList.clear()
        internalList.addAll(list)
        videoCount = internalList.count { it.type == MediaWrapper.TYPE_VIDEO }
    }

    @Synchronized
    fun map(list: List<MediaWrapper>) {
        internalList.addAll(list)
        videoCount = internalList.count { it.type == MediaWrapper.TYPE_VIDEO }
    }

    /**
     * @param position The index of the media in the list
     * @return null if not found
     */
    @Synchronized
    private fun getMRL(position: Int): String? {
        return if (!isValid(position)) null else internalList[position].location
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("LibVLC Media List: {")
        for (i in 0 until size()) {
            sb.append(i.toString())
            sb.append(": ")
            sb.append(getMRL(i))
            sb.append(", ")
        }
        sb.append("}")
        return sb.toString()
    }

    companion object {
        private const val TAG = "VLC/MediaWrapperList"

        private const val EVENT_ADDED = 0
        private const val EVENT_REMOVED = 1
        private const val EVENT_MOVED = 2
    }

    private data class Event(val type: Int, val arg1: Int, val arg2: Int, val mrl: String)
}

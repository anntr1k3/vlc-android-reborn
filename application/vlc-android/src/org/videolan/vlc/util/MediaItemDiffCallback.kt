package org.videolan.vlc.util

import org.videolan.medialibrary.media.MediaLibraryItem
import org.videolan.vlc.gui.DiffUtilAdapter


class MediaItemDiffCallback<T : MediaLibraryItem> : DiffUtilAdapter.DiffCallback<T>() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        if (oldItem === newItem) return true
        if (oldItem.itemType != newItem.itemType) return false
        if (oldItem.id != 0L && newItem.id != 0L) return oldItem.id == newItem.id
        return oldItem.equals(newItem)
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return oldItem === newItem || (oldItem.id != 0L && oldItem.id == newItem.id && oldItem.title == newItem.title)
    }

    companion object {
        private const val TAG = "VLC/MediaItemDiffCallback"
    }
}

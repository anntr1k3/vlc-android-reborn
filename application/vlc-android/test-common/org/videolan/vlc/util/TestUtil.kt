package org.videolan.vlc.util

import android.net.Uri
import androidx.core.net.toUri
import org.videolan.libvlc.interfaces.IMedia
import org.videolan.resources.TYPE_LOCAL_FAV
import org.videolan.resources.TYPE_NETWORK_FAV
import org.videolan.vlc.gui.dialogs.State
import org.videolan.vlc.gui.dialogs.SubtitleItem
import org.videolan.vlc.mediadb.models.BrowserFav
import org.videolan.vlc.mediadb.models.CustomDirectory
import org.videolan.vlc.mediadb.models.ExternalSub
import org.videolan.vlc.mediadb.models.Slave

object TestUtil {
    private const val fakeUri: String = "https://www.videolan.org/fake_"
    private const val fakeSubUri: String = "/storage/emulated/0/Android/data/org.videolan.vlc.reborn.debug/files/subs/"
    private const val fakeMediaUri: String = "/storage/emulated/0/Android/data/org.videolan.vlc.reborn.debug/files/media/"

    fun createLocalFav(uri: Uri, title: String, iconUrl: String?): BrowserFav {
        return BrowserFav(uri, TYPE_LOCAL_FAV, title, iconUrl)
    }

    fun createLocalUris(count: Int): List<String> {
        return (0 until count).map {
            "${fakeMediaUri}local_$it.mp4"
        }
    }

    fun createLocalFavs(count: Int): List<BrowserFav> {
        return (0 until count).map {
            createLocalFav("${fakeMediaUri}_$it.mp4".toUri(), "local$it", null)
        }
    }

    fun createNetworkFav(uri: Uri, title: String, iconUrl: String?): BrowserFav {
        return BrowserFav(uri, TYPE_NETWORK_FAV, title, iconUrl)
    }

    fun createNetworkUris(count: Int): List<String> {
        return (0 until count).map { "${fakeUri}_network$it.mp4" }
    }

    fun createNetworkFavs(count: Int): List<BrowserFav> {
        return (0 until count).map {
            createNetworkFav(
                    "${fakeUri}network${it}".toUri(),
                    "network" + 1,
                    null)
        }
    }

    fun createExternalSub(
            idSubtitle: String,
            subtitlePath: String,
            mediaPath: String,
            subLanguageID: String,
            movieReleaseName: String): ExternalSub {
        return ExternalSub(idSubtitle, subtitlePath, mediaPath, subLanguageID, movieReleaseName, false)
    }

    fun createExternalSubsForMedia(mediaPath: String, mediaName: String, count: Int): List<ExternalSub> {
        return (0 until count).map {
            ExternalSub(it.toString(), "${fakeSubUri}$mediaName$it", mediaPath, "en", mediaName, false)
        }
    }

    fun createSubtitleSlave(mediaPath: String, uri: String): Slave {
        return Slave(mediaPath, IMedia.Slave.Type.Subtitle, 2, uri)
    }

    fun createSubtitleSlavesForMedia(mediaName: String, count: Int): List<Slave> {
        return (0 until count).map {
            createSubtitleSlave("$fakeMediaUri$mediaName", "$fakeSubUri$mediaName$it.srt")
        }
    }

    fun createCustomDirectory(path: String): CustomDirectory {
        return CustomDirectory(path)
    }

    fun createCustomDirectories(count: Int): List<CustomDirectory> {
        val directory = "/sdcard/foo"
        return (0 until count).map {
            createCustomDirectory("$directory$it")
        }
    }

    fun createDownloadingSubtitleItem(
            idSubtitle: String,
            mediaPath: String,
            subLanguageID: String,
            movieReleaseName: String,
            zipDownloadLink: String): SubtitleItem = SubtitleItem(idSubtitle, -1L, mediaPath.toUri(), subLanguageID, movieReleaseName, State.Downloading, zipDownloadLink, false, 0F, 0L, "")
}

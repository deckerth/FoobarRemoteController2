package com.deckerth.thomas.foobarremotecontroller2.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.collection.emptyLongSet
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.deckerth.thomas.foobarremotecontroller2.connector.HTTPConnector
import com.deckerth.thomas.foobarremotecontroller2.connector.PlayerAccess
import com.deckerth.thomas.foobarremotecontroller2.connector.PlaylistAccess
import com.deckerth.thomas.foobarremotecontroller2.foobarMediaService
import com.deckerth.thomas.foobarremotecontroller2.getIpAddress
import com.deckerth.thomas.foobarremotecontroller2.mediaSession
import com.deckerth.thomas.foobarremotecontroller2.model.PlaybackState
import com.deckerth.thomas.foobarremotecontroller2.model.Player
import com.deckerth.thomas.foobarremotecontroller2.model.Playlist
import com.deckerth.thomas.foobarremotecontroller2.model.VolumeControl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.zhanghai.compose.preference.Preferences
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.floor

var preferences: Preferences? = null
var ip_address: String? = null
var valid = false
val connector = HTTPConnector()
val foobVolumeControl: VolumeControl = VolumeControl(false, 0, 1, "db", 0)

@Composable
fun UpdatePreferences() {
    ip_address = getIpAddress()
    Thread {
        if (ip_address != null) {
            var count = 0
            do {
                valid = connector.checkConnection(ip_address!!)
                count++
                if (!valid) {
                    Thread.sleep(500)
                }
                if (count > 20) {
                    break
                }
            } while (!valid)

            println("FOOB $ip_address valid:$valid")

            observer?.cancel(true)
            if (valid) {
                startPlayerObserver()
            }
        }
    }.start()
}

var autoscroll by mutableStateOf(true)
var autoScrollIndex by mutableIntStateOf(0)
var displayedPlaylist by mutableStateOf<Playlist?>(null)
var loadingList by mutableStateOf(false)
var playlistState by mutableStateOf(LazyListState())

var selectedPlaylist by mutableStateOf("")

fun updateList() {
    invalidatePlaylist(selectedPlaylist)
}

fun getCurrentAlbumIndex(): Int {
    if (player == null || player!!.getIndex() == -1 || displayedPlaylist == null || displayedPlaylist!!.albums.isEmpty())
        return -1
    for ((index, album) in displayedPlaylist!!.albums.withIndex()) {
        if (album.hasIndex(player!!.getIndex()))
            return index
    }
    return -1
}

var player by mutableStateOf<Player?>(null)

fun updatePlayer() {
    player = PlayerAccess.getInstance().playerState
    val currentAlbumIndex = getCurrentAlbumIndex()
    if (autoscroll && player != null &&
        displayedPlaylist != null &&
        displayedPlaylist!!.albums.isNotEmpty() &&
        currentAlbumIndex != autoScrollIndex &&
        currentAlbumIndex != -1
    ) {
        autoScrollIndex = currentAlbumIndex
    }
    if (mediaSession != null && player != null) {
        val state = when (player!!.playbackState) {
            PlaybackState.STOPPED -> PlaybackStateCompat.STATE_STOPPED
            PlaybackState.PLAYING -> PlaybackStateCompat.STATE_PLAYING
            PlaybackState.PAUSED -> PlaybackStateCompat.STATE_PAUSED
        }

        if (player!!.getIndex() == -1)
            mediaSession!!.setMetadata(
                MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, player!!.title)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, player!!.artist)
                    .putBitmap(
                        MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                        getBitmapFromURL(player!!.artworkUrl)
                    )
                    .putLong(
                        MediaMetadataCompat.METADATA_KEY_DURATION,
                        (floor(player!!.duration.toDouble() * 1000)).toLong()
                    )
                    .build()
            )
        else  // no longer in playlist -> no image
            mediaSession!!.setMetadata(
                MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, player!!.title)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, player!!.artist)
                    .putLong(
                        MediaMetadataCompat.METADATA_KEY_DURATION,
                        (floor(player!!.duration.toDouble() * 1000)).toLong()
                    )
                    .build()
            )
        System.out.println("FOOB updatePlayer: ${player!!.position}")
        mediaSession!!.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(
                    state,
                    floor(player!!.position.toDouble() * 1000).toLong(),
                    1f
                )
                .setActions(
                    (if (state == PlaybackStateCompat.STATE_PLAYING) PlaybackStateCompat.ACTION_PAUSE else PlaybackStateCompat.ACTION_PLAY) or
                            PlaybackStateCompat.ACTION_PLAY_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
                .build()
        )
        foobarMediaService?.updateNotification()
    }


}

private fun getBitmapFromURL(src: String?): Bitmap? {
    return if (!(src!!.contains("-1")))
        try {
            println("FOOB getBitmapFromURL: $src")
            val url = URL(src)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    else
        null
}

var observer: ScheduledFuture<*>? = null

fun startPlayerObserver() {
    try {
        if (observer != null && !observer!!.isDone)
            observer!!.cancel(true)
        val scheduler = Executors.newSingleThreadScheduledExecutor()
        observer = scheduler.scheduleWithFixedDelay({
            try {
                updatePlayer()
                val playlists = PlaylistAccess.getInstance().playlists
                if (autoscroll && player != null && player!!.playlistId.isNotEmpty() && player!!.playlistId != selectedPlaylist)
                    setSelectedPlaylist(player!!.playlistId)
                if (playlists != null)
                    setPlaylists(playlists)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 0, 1, TimeUnit.SECONDS)

    } catch (e: Exception) {
        e.printStackTrace()
    }
}
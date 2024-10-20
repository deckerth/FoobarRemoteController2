package com.deckerth.thomas.foobarremotecontroller2

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.deckerth.thomas.foobarremotecontroller2.connector.HTTPConnector
import com.deckerth.thomas.foobarremotecontroller2.connector.PlayerAccess
import com.deckerth.thomas.foobarremotecontroller2.connector.PlaylistAccess
import com.deckerth.thomas.foobarremotecontroller2.model.PlaybackState
import com.deckerth.thomas.foobarremotecontroller2.model.Player
import com.deckerth.thomas.foobarremotecontroller2.model.Playlist
import com.deckerth.thomas.foobarremotecontroller2.model.VolumeControl
import kotlinx.coroutines.flow.MutableStateFlow
import me.zhanghai.compose.preference.Preferences
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.math.floor

var preferences: Preferences? = null
var ip_address: String? = null
var valid = false
val connector = HTTPConnector()
val foobVolumeControl: VolumeControl = VolumeControl(false, 0, 1, "db", 0)

fun updatePreferences(preferenceFlow: MutableStateFlow<Preferences>) {
    preferences = preferenceFlow.value
    val ip:String? = preferences!!["ip_address_preference"]
    Thread{
        if (ip != null) {
            valid = connector.checkConnection(ip)
            println("FOOB $ip valid:$valid")
            ip_address = ip
            observer?.cancel(true)
            if (valid){
                startPlayerObserver()
            }
        }
    }.start()
}

var playlistId: String? = null;
var playlist by mutableStateOf<Playlist?>(null)
var loadingList by mutableStateOf(false)

private fun updateList() {
    loadingList = true
    //list.clear()
    Thread {
        val currentPlaylist = PlaylistAccess.getInstance().currentPlaylist ?: return@Thread
        playlistId = currentPlaylist.playlistEntity.playlistId
        playlist = currentPlaylist
        loadingList = false
    }.start()
}

var player by mutableStateOf<Player?>(null)

private fun updatePlayer() {
    player = PlayerAccess.getInstance().playerState
    if (mediaSession != null && player != null) {
        val state = when (player!!.playbackState){
            PlaybackState.STOPPED -> PlaybackStateCompat.STATE_STOPPED
            PlaybackState.PLAYING -> PlaybackStateCompat.STATE_PLAYING
            PlaybackState.PAUSED -> PlaybackStateCompat.STATE_PAUSED
        }

        mediaSession!!.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, player!!.title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, player!!.artist)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, getBitmapFromURL(player!!.artworkUrl))
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, (floor(player!!.duration.toDouble()*1000)).toLong())
                .build()
        )
        System.out.println("FOOB updatePlayer: ${player!!.position}")
        mediaSession!!.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(state, floor( player!!.position.toDouble()*1000).toLong(), 1f)
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
    return try {
        val url = URL(src)
        val connection = url.openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()
        val input = connection.inputStream
        BitmapFactory.decodeStream(input)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

//    public void startPlayerObserver() {
//
//        if (mObserverIsRunning) return;
//
//        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
//
//        final Runnable task = () -> {
//            mLastPlayerState = mConnector.getData("player?columns=%25catalog%25,%25composer%25,%25album%25,%25title%25,%25artist%25,%25discnumber%25,%25track%25,%25playback_time%25");
//            mActivity.runOnUiThread(() -> {
//                parsePlayerState(mLastPlayerState);
//                getArtwork();
//                mObserverIsRunning = true;
//            });
//        };
//
//        // Schedule the task to run with an initial delay and then periodically
//        // For example, to run every second after an initial delay of 0 seconds
//        scheduler.scheduleWithFixedDelay(task, 0, 1, TimeUnit.SECONDS);
//    }

private var observer: ScheduledFuture<*>? = null

fun startPlayerObserver() {
    if (observer != null && !observer!!.isDone)
        observer!!.cancel(true)
    val scheduler = Executors.newSingleThreadScheduledExecutor()
    observer = scheduler.scheduleWithFixedDelay({
        updatePlayer()
        if (!loadingList && PlaylistAccess.getInstance().currentPlaylistID != playlistId)
            updateList()
    }, 0, 1, TimeUnit.SECONDS)
}
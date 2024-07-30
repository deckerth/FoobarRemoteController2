package com.deckerth.thomas.foobarremotecontroller2

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.deckerth.thomas.foobarremotecontroller2.connector.HTTPConnector
import com.deckerth.thomas.foobarremotecontroller2.connector.PlayerAccess
import com.deckerth.thomas.foobarremotecontroller2.connector.PlaylistAccess
import com.deckerth.thomas.foobarremotecontroller2.model.ITitle
import com.deckerth.thomas.foobarremotecontroller2.model.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.subscribe
import me.zhanghai.compose.preference.Preferences
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

var preferences: Preferences? = null
var ip_address: String? = null
var valid = false
val connector = HTTPConnector()

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
val list = mutableStateListOf<ITitle>()
var loadingList by mutableStateOf(false)

private fun updateList() {
    loadingList = true
    //list.clear()
    Thread {
        val currentPlaylist = PlaylistAccess.getInstance().currentPlaylist
        playlistId = currentPlaylist.playlistEntity.playlistId
        val playlist: List<ITitle> =
            if (currentPlaylist != null) currentPlaylist.playlist else return@Thread
        list.clear()
        list.addAll(playlist)
        loadingList = false
    }.start()
}

var player by mutableStateOf<Player?>(null)

private fun updatePlayer() {
    player = PlayerAccess.getInstance().playerState
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
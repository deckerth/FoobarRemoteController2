package com.deckerth.thomas.foobarremotecontroller2

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.deckerth.thomas.foobarremotecontroller2.connector.PlayerAccess
import com.deckerth.thomas.foobarremotecontroller2.connector.PlaylistAccess
import com.deckerth.thomas.foobarremotecontroller2.model.ITitle
import com.deckerth.thomas.foobarremotecontroller2.model.Player
import kotlinx.coroutines.flow.MutableStateFlow
import me.zhanghai.compose.preference.Preferences
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private var observerRunning = false

var ip_address: String? = null

fun updatePreferences(preferenceFlow: MutableStateFlow<Preferences>) {
    var value = preferenceFlow.value
    var ip: String? = value["ip_address_preference"]
    if (ip != null) {
        ip_address = ip
        updateList()
        startPlayerObserver()
    }
}

val list = mutableStateListOf<ITitle>()
var loadingList by mutableStateOf(true)

fun updateList() {
    loadingList = true
    list.clear()
    Thread {
        val currentPlaylist = PlaylistAccess.getInstance().currentPlaylist
        var playlist: List<ITitle> =
            if (currentPlaylist != null) currentPlaylist.playlist else return@Thread
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

fun startPlayerObserver() {
    if (observerRunning)
        return
    var scheduler = Executors.newSingleThreadScheduledExecutor()
    scheduler.scheduleWithFixedDelay(::updatePlayer, 0, 1, TimeUnit.SECONDS)
}
package com.deckerth.thomas.foobarremotecontroller2.connector

import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlaylistsViewModel
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class PlayerObserver(private val vm: AppViewModel, private val playlistAccess: PlaylistAccess, private val playlistsViewModel: PlaylistsViewModel) {

    var observer: ScheduledFuture<*>? = null

    fun startPlayerObserver() {
        try {
            if (observer != null && !observer!!.isDone)
                observer!!.cancel(true)
            val scheduler = Executors.newSingleThreadScheduledExecutor()
            vm.errorHandler.reset()
            observer = scheduler.scheduleWithFixedDelay({
                if (!vm.errorHandler.sick())
                    try {
                        vm.updatePlayer()
                        val playlists = playlistAccess.playlists
                        if (vm.player != null && playlists != null)
                            if (vm.autoscroll && vm.player!!.playlistId.isNotEmpty() && vm.player!!.playlistId != vm.selectedPlaylist)
                                playlistsViewModel.setSelectedPlaylist(vm.player!!.playlistId)
                            else if (vm.selectedPlaylist.isEmpty() && playlists.currentPlaylist != null)
                                playlistsViewModel.setSelectedPlaylist(playlists.currentPlaylist.playlistId)

                        if (playlists != null && playlists.ipAddress == vm.ipAddress)
                            playlistsViewModel.setPlaylists(playlists)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                else if (vm.errorHandler.authorizationErrorOccurred) {
                    vm.askForPassword = true
                    vm.player = null
                }

            }, 0, 1, TimeUnit.SECONDS)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
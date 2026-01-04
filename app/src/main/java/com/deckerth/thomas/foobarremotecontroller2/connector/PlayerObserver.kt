package com.deckerth.thomas.foobarremotecontroller2.connector

import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class PlayerObserver(private val vm: AppViewModel) {

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
                        if (vm.playlistAccess.playlists != null)
                            vm.playlistsViewModel.setPlaylists(vm.playlistAccess.playlists!!) // for delayed playlist update
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                else if (vm.errorHandler.authorizationErrorOccurred) {
                    vm.askForPassword = true
                    vm.player = null
                }

            }, 0, 800, TimeUnit.MILLISECONDS)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
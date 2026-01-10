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

                        // Delayed playlist update:
                        // A typical scenario:
                        // PlaylistAccess gets new playlists, and it turns out that the currently
                        // displayed playlist needs to be updated. A direct update may terminate the
                        // app if Compose is just rendering the current playlist.
                        // In this case, the list is only marked that it requires an update.
                        // When Compose starts to render the playlist the next time, this is detected,
                        // and instead of rendering the list, displayedPlaylist is cleared, and can therefore
                        // now be updated. This call in the player loop triggers this update
                        if (vm.playlistAccess.playlists != null)
                            vm.playlistsViewModel.setPlaylists(vm.playlistAccess.playlists!!)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                else if (vm.errorHandler.authorizationErrorOccurred) {
                    vm.askForPassword = true
                    vm.playerViewModel.valid = false
                }

            }, 0, 800, TimeUnit.MILLISECONDS)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
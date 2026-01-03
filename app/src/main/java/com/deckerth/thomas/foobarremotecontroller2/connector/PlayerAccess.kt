package com.deckerth.thomas.foobarremotecontroller2.connector

import com.deckerth.thomas.foobarremotecontroller2.lastChanged
import com.deckerth.thomas.foobarremotecontroller2.model.OutputDevice
import com.deckerth.thomas.foobarremotecontroller2.model.OutputDevices
import com.deckerth.thomas.foobarremotecontroller2.model.PlaybackMode
import com.deckerth.thomas.foobarremotecontroller2.model.PlaybackState
import com.deckerth.thomas.foobarremotecontroller2.model.Player
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import com.deckerth.thomas.foobarremotecontroller2.volumeProvider
import org.json.JSONException
import org.json.JSONObject
import java.time.Duration
import java.time.Instant
import java.util.Locale
import kotlin.math.roundToInt

class PlayerAccess(private val vm: AppViewModel) {
    var playerState: Player?
        get() {
            if (lastKnownPlayerState == null) return null

            val currentPlayer = lastKnownPlayerState!!.clonePlayer()

            // update position
            if (!currentPlayer.duration.isBlank() && currentPlayer.playbackState == PlaybackState.PLAYING) {
                val duration = currentPlayer.duration.toFloat()
                val position = currentPlayer.position.toFloat()

                val now = System.currentTimeMillis()
                val elapsedSeconds = (now - lastKnownPlayerState!!.timestamp) / 1000f
                var newPosition = position + elapsedSeconds
                if (newPosition > duration) newPosition = duration
                currentPlayer.position = newPosition.toString()

                val timeParts = currentPlayer.playbackTime.split(":")
                if (timeParts.size == 2) {
                    try {
                        // Convert parts to numbers and calculate total seconds
                        val minutes = timeParts[0].toLong()
                        val seconds = timeParts[1].toLong()
                        val currentTotalSeconds = (minutes * 60) + seconds

                        var newTotalSeconds = currentTotalSeconds + elapsedSeconds
                        if (newTotalSeconds > duration) newTotalSeconds = duration

                        val newPlaybackTimeSeconds = newTotalSeconds.roundToInt()

                        // Format the new total seconds back into a "minutes:seconds" string
                        val newMinutes = newPlaybackTimeSeconds / 60
                        val newSeconds = newPlaybackTimeSeconds % 60
                        currentPlayer.playbackTime =
                            String.format(Locale.getDefault(), "%d:%02d", newMinutes, newSeconds)

                    } catch (e: NumberFormatException) {
                        // Handle cases where the string is not in the expected format
                        // For now, we'll just leave the original time
                    }
                }
            }
            return currentPlayer
        }
        set(playerState) {
            lastKnownPlayerState = playerState
            vm.updatePlayer()
        }

    fun setPosition(position: Float?) {
        val jsonString = "{\"position\":$position}"
        vm.connector.postData("player/", jsonString, vm)
    }

    private var lastKnownPlayerState: Player? = null

    fun parsePlayerState(usedIpAddress: String, contentObject: JSONObject): Player? {
        try {
            val playerObject = contentObject.getJSONObject("player")
            val activeItemObject = playerObject.getJSONObject("activeItem")
            val volumeObject = playerObject.getJSONObject("volume")
            val columns = activeItemObject.getJSONArray("columns")
            /*
                "player": {
                    "activeItem": {
                        "columns": [
                            "DGG",
                            "7353 72726",
                            "Franck, César (1822-1890)",
                            "Klavierquintett f-Moll", !! OPTIONAL may be just ""
                            "1. Molto moderato quasi lento - Allegro",
                            "Khatia Buniatishvili, Klavier / Gidon Kremer & Marija Nemanytė, Violine / Maxim Rysanov, Viola / Giedrė Dirvanauskaitė, Cello",
                            "44100",
                            "Orchestral",
                            "01",
                            "01",
                            "?",
                            "02",
                            "0:28"
                            "<filename>"
                        ],
                        "duration": 955.8266666666667,
                        "index": 13,
                        "playlistId": "p4",
                        "playlistIndex": 3,
                        "position": 28.9795
                  },
                 "volume": {
                    "isMuted": false,
                    "max": 0,
                    "min": -100,
                    "type": "db",
                    "value": 0
                }
               */
            val state = playerObject.getString("playbackState")
            val playbackState = when (state) {
                "playing" -> PlaybackState.PLAYING
                "paused" -> PlaybackState.PAUSED
                else -> PlaybackState.STOPPED
            }
            if (Duration.between(lastChanged, Instant.now()).toMillis() > 500) {
                println("FOOB Volume set")
                val volumeControl = vm.foobVolumeControl
                volumeControl.muted = volumeObject.getBoolean("isMuted")
                volumeControl.min = volumeObject.getInt("min")
                volumeControl.max = volumeObject.getInt("max")
                volumeControl.type = volumeObject.getString("type")
                volumeControl.value = volumeObject.getInt("value")
                volumeProvider.setCurrentVolume(volumeControl.currentValuePercent)
            }

            if (columns.length() > 0) {
                val title = columns.getString(4)
                val filename = columns.getString(11)
                var effectiveTitle = ""
                val index = activeItemObject.getString("index")
                val imageURL = if (!index.isEmpty() && index != "-1") vm.connector.serverAddress(usedIpAddress) + "artwork/" + activeItemObject.getString(
                    "playlistId"
                ) + "/" + activeItemObject.getString("index")
                else vm.connector.serverAddress(usedIpAddress) + "artwork/current"
                if (title != filename) effectiveTitle = title
                return Player(
                    columns.getString(0),
                    columns.getString(1),
                    columns.getString(2),
                    columns.getString(3),
                    effectiveTitle,
                    columns.getString(5),
                    columns.getString(6),
                    columns.getString(7),
                    columns.getString(8),
                    columns.getString(9),
                    columns.getString(10),
                    activeItemObject.getString("playlistId"),
                    activeItemObject.getString("index"),
                    activeItemObject.getString("duration"),
                    activeItemObject.getString("position"),
                    imageURL,
                    playbackState,
                    PlaybackMode.entries[playerObject.getInt("playbackMode")],
                    usedIpAddress, false
                )
            } else return Player(
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                playbackState,
                PlaybackMode.entries[playerObject.getInt("playbackMode")],
                usedIpAddress, false
            )
        } catch (e: JSONException) {
            e.printStackTrace()
            vm.errorHandler.logError(
                ErrorType.API,
                ErrorCode.BAD_RESPONSE,
                ErrorSource.PLAYER_STATE,
                e
            )
            return null
        }
    }

    fun startPlayback() {
        Thread {
            vm.connector.postData("player/play", vm)
        }.start()
    }

    fun pausePlayback() {
        Thread {
            vm.connector.postData("player/pause", vm)
        }.start()
    }

    fun previousTrack() {
        Thread {
            vm.connector.postData("player/previous", vm)
        }.start()
    }

    fun nextTrack() {
        Thread { vm.connector.postData("player/next", vm) }.start()
    }

    fun playTrack(playlistId: String?, index: Int) {
        Thread {
            vm.connector.postData("player/play/$playlistId/$index", vm)
        }.start()
    }

    fun setVolume(value: Int?) {
        Thread {
            val jsonString = "{\"volume\":$value}"
            vm.connector.postData("player/", jsonString, vm)
        }.start()
    }

    fun setPlaybackMode(mode: PlaybackMode) {
        Thread {
            ->
            val jsonString =
                "{\"options\":[{\"id\": \"playbackOrder\", \"value\": " + mode.ordinal + "}]}"
            vm.connector.postData("player/", jsonString, vm)
        }.start()
    }

    val outputDevices: OutputDevices
        get() {
            val response: Response?
            try {
                response = vm.connector.getData("outputs", vm)
            } catch (e: Exception) {
                vm.errorHandler.logError(
                    ErrorType.NETWORK,
                    ErrorCode.CONNECTION_ERROR,
                    ErrorSource.PLAYER_STATE,
                    e
                )
                val deviceList = OutputDevices()
                deviceList.invalidate()
                return deviceList // to indicate that the Beefweb component needs to be updated in foobar
            }
            return parseOutputDevices(response)
        }

    private fun parseOutputDevices(response: Response): OutputDevices {
        // {
        //  "outputs": {
        //    "active": {
        //      "typeId": "string",
        //      "deviceId": "string"
        //    },
        //    "types": [
        //      {
        //        "id": "string",
        //        "name": "string",
        //        "devices": [
        //          {
        //            "id": "string",
        //            "name": "string"
        //          }
        //        ]
        //      }
        //    ]
        //  }
        //}

        val deviceList = OutputDevices()

        try {
            val contentObject = JSONObject(response.message)
            val outputsObject = contentObject.getJSONObject("outputs")
            val activeOutputObject = outputsObject.getJSONObject("active")
            deviceList.setActiveDevice(
                OutputDevice(
                    activeOutputObject.getString("typeId"),
                    activeOutputObject.getString("deviceId"),
                    ""
                )
            )
            val types = outputsObject.getJSONArray("types")
            for (i in 0..<types.length()) {
                val type = types.getJSONObject(i)
                val devices = type.getJSONArray("devices")
                for (j in 0..<devices.length()) {
                    val device = devices.getJSONObject(j)
                    deviceList.addDevice(
                        OutputDevice(
                            type.getString("id"), device.getString("id"), device.getString("name")
                        )
                    )
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            vm.errorHandler.logError(
                ErrorType.API,
                ErrorCode.BAD_RESPONSE,
                ErrorSource.PLAYER_STATE,
                e
            )
            deviceList.invalidate()
        }
        return deviceList
    }

    fun setOutputDevice(device: OutputDevice) {
        Thread {
            // {
            //  "typeId": "string",
            //  "deviceId": "string"
            //}
            val jsonString =
                "{\"typeId\": \"" + device.typeId + "\", \"deviceId\": \"" + device.deviceId + "\"}"
            vm.connector.postData("outputs/active/", jsonString, vm)
        }.start()
    }
}

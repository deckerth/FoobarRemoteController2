package com.deckerth.thomas.foobarremotecontroller2.connector

import com.deckerth.thomas.foobarremotecontroller2.lastChanged
import com.deckerth.thomas.foobarremotecontroller2.model.CustomFieldsContent
import com.deckerth.thomas.foobarremotecontroller2.model.OutputDevice
import com.deckerth.thomas.foobarremotecontroller2.model.OutputDevices
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlaybackMode
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlaybackState
import com.deckerth.thomas.foobarremotecontroller2.volumeProvider
import org.json.JSONException
import org.json.JSONObject
import java.time.Duration
import java.time.Instant

class PlayerAccess(private val vm: AppViewModel) {

    fun setPosition(position: Float?) {
        val jsonString = "{\"position\":$position}"
        vm.connector.postData("player/", jsonString, vm)
    }

    // Columns:
    //    1 %25label%25,
    //    2 %25catalog%25,
    //    3 %25composer%25,
    //    4 %25album%25,
    //    5 %25title%25,
    //    6 %25artist%25,
    //    7 %25album artist%25,
    //    8 %25samplerate%25,
    //    9 %25genre%25,
    //    10 %25discnumber%25,
    //    11 %25track%25,
    //    12 %25playback_time%25,
    //    13 %25length_seconds_fp%25,
    //    14 %24filename%28%25path%25%29%24&" becomes:$filename(%path%) / %25path%25%

    private val columnListWithoutPath  = "%25label%25,%25catalog%25,%25composer%25,%25album%25,%25title%25,%25artist%25,%25album artist%25,%25samplerate%25,%25genre%25,%25discnumber%25,%25track%25,%25playback_time%25,%25length_seconds_fp%25,%24filename%28%25path%25%29%24"
    private val columnListWithPath     = "%25label%25,%25catalog%25,%25composer%25,%25album%25,%25title%25,%25artist%25,%25album artist%25,%25samplerate%25,%25genre%25,%25discnumber%25,%25track%25,%25playback_time%25,%25length_seconds_fp%25,%25path%25%"

    fun getColumnList(withPath: Boolean): String {
        val standardPath =  if (withPath) columnListWithPath else columnListWithoutPath
        return standardPath + vm.customFields.getEncodedColumnList()
    }

    fun parsePlayerState(usedIpAddress: String, contentObject: JSONObject) {
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
                            "Khatia Buniatishvili",
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
                val index = activeItemObject.getString("index")
                val imageURL = if (!index.isEmpty() && index != "-1") vm.connector.serverAddress(usedIpAddress) + "artwork/" + activeItemObject.getString(
                    "playlistId"
                ) + "/" + activeItemObject.getString("index")
                else vm.connector.serverAddress(usedIpAddress) + "artwork/current"

                val customFieldsContent = CustomFieldsContent()
                if (columns.length() > 13) {
                    for (i in 14 until columns.length()) {
                        val field = vm.customFields.get(i - 14)
                        if (field != null)
                            customFieldsContent.setCustomFieldContent(
                                field,
                                columns.getString(i)
                            )
                    }
                }
                vm.playerViewModel.update(
                    columns.getString(0),
                    columns.getString(1),
                    columns.getString(2),
                    columns.getString(3),
                    columns.getString(4),
                    columns.getString(5),
                    columns.getString(6),
                    columns.getString(7),
                    columns.getString(8),
                    columns.getString(9),
                    columns.getString(10),
                    columns.getString(11),
                    activeItemObject.getString("playlistId"),
                    activeItemObject.getString("index"),
                    activeItemObject.getString("duration"),
                    activeItemObject.getString("position"),
                    imageURL,
                    playbackState,
                    PlaybackMode.entries[playerObject.getInt("playbackMode")],
                    usedIpAddress, false,
                    customFieldsContent
                )
            } else {
                vm.playerViewModel.update(
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
                    "",
                    playbackState,
                    PlaybackMode.entries[playerObject.getInt("playbackMode")],
                    usedIpAddress, false,
                    CustomFieldsContent()
                )
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            vm.errorHandler.logError(
                ErrorType.API,
                ErrorCode.BAD_RESPONSE,
                ErrorSource.PLAYER_STATE,
                e
            )
            vm.playerViewModel.valid = false
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

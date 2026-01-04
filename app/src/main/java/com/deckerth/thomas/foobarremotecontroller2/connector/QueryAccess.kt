package com.deckerth.thomas.foobarremotecontroller2.connector

import android.os.Process
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

private var instanceCounter = 0

class QueryAccess(val vm: AppViewModel) {

    private val listeners: MutableList<(AppViewModel, String, String, JSONObject) -> Unit> =
        mutableListOf()

    private var endListening = false
    private var restart = false
    private var playlistId = ""
    private var isListening = false

    val currentProcessId = Process.myPid()
    val currentInstanceNumber = instanceCounter++

    fun addListener(listener: (AppViewModel, String, String, JSONObject) -> Unit) {
        listeners.add(listener)
    }

    fun stop() {
        if (isListening) {
            println("FOOBQUERY(${vm.owner}) Stop initiated. Process ID: $currentProcessId, Instance: $currentInstanceNumber")
            endListening = true
        } else
            println("FOOBQUERY(${vm.owner}) Stop not required, not listening. Process ID: $currentProcessId, Instance: $currentInstanceNumber\"")
    }

    fun start() {
        Thread {
            endListening = false
            isListening = true

            println("FOOBQUERY(${vm.owner}) Started. Process ID: $currentProcessId, Instance: $currentInstanceNumber")

            var url: URL
            var urlConnection: HttpURLConnection? = null
            var reader: BufferedReader? = null

            try {
                while (!endListening) {
                    restart = false

                    val usedIpAddress = vm.ipAddress
                    var endpoint =
                        "query/updates?player=true&" +
                                "trcolumns=" +
                                "%25label%25,%25catalog%25,%25composer%25,%25album%25,%25title%25,%25artist%25,%25samplerate%25,%25genre%25,%25discnumber%25,%25track%25,%25playback_time%25,%24filename%28%25path%25%29%24&" +
                                "playlists=true"
                    if (playlistId.isNotEmpty())
                        endpoint += "&playlistItems=true&plref=$playlistId&plrange=0:1" +
                                "&plcolumns=" +
                                "%25label%25,%25catalog%25,%25composer%25,%25album%25,%25title%25,%25artist%25,%25samplerate%25,%25genre%25,%25discnumber%25,%25track%25,%25playback_time%25,%24filename%28%25path%25%29%24&"

                    url = URL(vm.connector.serverAddress(usedIpAddress) + endpoint)
                    //open an URL connection
                    urlConnection = url.openConnection() as HttpURLConnection
                    vm.connector.setCredentials(urlConnection, vm)

                    val connectionResponse =
                        urlConnection.responseCode  // 401 -> Unauthorized, 200 -> OK
                    println("FOOBQUERY(${vm.owner}) response: $connectionResponse")
                    if (connectionResponse == NOT_AUTHORIZED) {
                        throw IOException("Unauthorized")
                    } else if (connectionResponse == NO_CONNECTION)
                        throw IOException("No connection")
                    else if (connectionResponse in 200..299) {
                        reader = BufferedReader(InputStreamReader(urlConnection.inputStream))

                        while (!endListening && !restart) {
                            val line = reader.readLine() ?: break
                            if (!endListening && !restart && line.startsWith("data: ")) {
                                val data = line.removePrefix("data: ")
                                if (data != "{}") {
                                    val json = JSONObject(data)
                                    listeners.forEach { t ->
                                        t(
                                            vm,
                                            usedIpAddress!!,
                                            playlistId,
                                            json
                                        )
                                    }
                                }
                            }
                        }
                        if (!endListening && !restart) {
                            println("FOOBQUERY(${vm.owner}) listener ended irregularly, restarting...")
                        } else if (restart) {
                            println("FOOBQUERY(${vm.owner}) listener restarting")
                        }
                    }
                }
            } catch (e: Exception) {
                if (e.message != null && e.message.equals("Unauthorized"))
                    vm.errorHandler.logError(
                        ErrorType.NETWORK,
                        ErrorCode.AUTHORIZATION_ERROR,
                        ErrorSource.HTTP_CONNECTOR,
                        e
                    )
                else {
                    e.printStackTrace()
                    vm.errorHandler.logError(
                        ErrorType.NETWORK,
                        ErrorCode.CONNECTION_ERROR,
                        ErrorSource.HTTP_CONNECTOR,
                        e
                    )
                }
            } finally {
                urlConnection?.disconnect()
                reader?.close()
                isListening = false
                println("FOOBQUERY(${vm.owner}) listener stopped.  Process ID: $currentProcessId, Instance: $currentInstanceNumber ")
            }
        }.start()
    }

    fun setPlaylist(playlistId: String) {
        if (playlistId == this.playlistId)
            return
        this.playlistId = playlistId
        startOrRestart()
    }

    fun startOrRestart() {
        if (isListening)
            restart = true
        else
            start()
    }
}

fun startQueryAccess(vm: AppViewModel) {
    if (vm.queryAccess == null)
        vm.queryAccess = QueryAccess(vm).apply {
            addListener(::analyzePlayer)
            addListener(::analyzePlaylists)
            addListener(::analyzePlaylistItems)
            if (vm.displayedPlaylist != null)
                setPlaylist(vm.displayedPlaylist!!.playlistEntity.playlistId)
            start()
        }
    else
        vm.queryAccess!!.startOrRestart()
}

fun analyzePlayer(
    vm: AppViewModel,
    usedIpAddress: String,
    observedPlaylistId: String,
    json: JSONObject
) {
    val player = json.optJSONObject("player") ?: return
    val activeItem = player.optJSONObject("activeItem") ?: return
    val columns = activeItem.optJSONArray("columns") ?: return
    val playbackState = player.getString("playbackState")
    if (columns.length() == 0 && playbackState == "playing")
        return
    println("FOOBQUERY(${vm.owner}) Player: $playbackState")
    println("FOOBQUERY(${vm.owner})  Current Title: ${columns.optString(0)}")
    println("FOOBQUERY(${vm.owner})    Duration: ${activeItem.optInt("duration")}")
    println("FOOBQUERY(${vm.owner})    Position: ${activeItem.optInt("position")}")
    vm.playerAccess.playerState = vm.playerAccess.parsePlayerState(usedIpAddress, json)
}

fun analyzePlaylists(
    vm: AppViewModel,
    usedIpAddress: String,
    observedPlaylistId: String,
    json: JSONObject
) {
    val playlists = json.optJSONArray("playlists") ?: return

    println("FOOBQUERY(${vm.owner}) Playlists:")
    for (i in 0..<playlists.length()) {
        val item = playlists.getJSONObject(i)
        println("FOOBQUERY(${vm.owner}) - $i: ${item.getString("title")}")
        println("FOOBQUERY(${vm.owner})     id: ${item.getString("id")}")
        println("FOOBQUERY(${vm.owner})     isCurrent: ${item.getBoolean("isCurrent")}")
        println("FOOBQUERY(${vm.owner})     itemCount: ${item.getInt("itemCount")}")
    }
    vm.playlistAccess.playlists = vm.playlistAccess.parsePlaylists(usedIpAddress, json)
}

fun analyzePlaylistItems(
    vm: AppViewModel,
    usedIpAddress: String,
    observedPlaylistId: String,
    json: JSONObject
) {
    json.optJSONObject("playlistItems") ?: return

    println("FOOBQUERY(${vm.owner}) PlaylistItems for $observedPlaylistId changed")

    vm.playlistAccess.fetchPlaylists()
}
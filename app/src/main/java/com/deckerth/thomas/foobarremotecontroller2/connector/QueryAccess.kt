package com.deckerth.thomas.foobarremotecontroller2.connector

import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class QueryAccess(val vm: AppViewModel) {

    private val listeners: MutableList<(AppViewModel, String, JSONObject) -> Unit> = mutableListOf()

    private var endListening = false
    private var playlistId = ""

    fun addListener(listener: (AppViewModel, String, JSONObject) -> Unit) {
        listeners.add(listener)
    }

    fun stop() {
        endListening = true
    }

    fun start() {
        var url: URL
        var urlConnection: HttpURLConnection? = null
        var reader: BufferedReader? = null
        val usedIpAddress = vm.ipAddress
        val endpoint =
            "query/updates?player=true&" +
                    "trcolumns=" +
                    "%25label%25,%25catalog%25,%25composer%25,%25album%25,%25title%25,%25artist%25,%25samplerate%25,%25genre%25,%25discnumber%25,%25track%25,%25playback_time%25,%24filename%28%25path%25%29%24&" +
                    "playqueue=true&playlists=true&" +
                    "plref=" + playlistId + "&" +
                    "plrange=0:1"

        try {
            url = URL(vm.connector.serverAddress(usedIpAddress) + endpoint)
            //open a URL connection
            urlConnection = url.openConnection() as HttpURLConnection
            vm.connector.setCredentials(urlConnection, vm)

            val connectionResponse =
                urlConnection.responseCode  // 401 -> Unauthorized, 200 -> OK
            println("FOOB response (getData): $connectionResponse")
            if (connectionResponse == NOT_AUTHORIZED) {
                throw IOException("Unauthorized")
            } else {
                reader = BufferedReader(InputStreamReader(urlConnection.inputStream))

                while (!endListening) {
                    val line = reader.readLine() ?: break
                    if (line.startsWith("data: ")) {
                        val data = line.removePrefix("data: ")
                        val json = JSONObject(data)
                        listeners.forEach { t -> t(vm, usedIpAddress!!, json) }
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
            throw e
        } finally {
            urlConnection?.disconnect()
            reader?.close()
        }
    }

    fun setPlaylist(playlistId: String) {
        stop()
        this.playlistId = playlistId
        start()
    }

}

var queryAccess: QueryAccess? = null

fun startQueryAccess(vm: AppViewModel): QueryAccess {
    queryAccess?.stop()
    queryAccess = QueryAccess(vm).apply {
        addListener(::analyzePlayer)
        addListener(::analyzePlaylists)
        if (vm.displayedPlaylist != null)
            setPlaylist(vm.displayedPlaylist!!.playlistEntity.playlistId)
        start()
    }
    return queryAccess!!
}

fun analyzePlayer(vm: AppViewModel, usedIpAddress: String, json: JSONObject) {
    val player = json.optJSONObject("player") ?: return
    val activeItem = player.optJSONObject("activeItem") ?: return
    val columns = activeItem.optJSONArray("columns") ?: return
    val playbackState = player.getString("playbackState")
    if (columns.length() == 0 && playbackState == "playing")
        return
    println("FOOBQUERY Player: $playbackState")
    println("FOOBQUERY  Current Title: ${columns.optString(0)}")
    println("FOOBQUERY    Duration: ${activeItem.optInt("duration")}")
    println("FOOBQUERY    Position: ${activeItem.optInt("position")}")
    vm.playerAccess.playerState = vm.playerAccess.parsePlayerState(usedIpAddress, json)
}

fun analyzePlaylists(vm: AppViewModel, usedIpAddress: String, json: JSONObject) {
    val playlists = json.optJSONArray("playlists") ?: return
    println("FOOBQUERY Playlists:")
    for (i in 0..<playlists.length()) {
        val item = playlists.getJSONObject(i)
        println("FOOBQUERY - $i: ${item.getString("title")}")
        println("FOOBQUERY     id: ${item.getString("id")}")
        println("FOOBQUERY     isCurrent: ${item.getBoolean("isCurrent")}")
        println("FOOBQUERY     itemCount: ${item.getInt("itemCount")}")
    }
}
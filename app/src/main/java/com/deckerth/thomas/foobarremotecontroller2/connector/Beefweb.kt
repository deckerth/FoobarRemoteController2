package com.deckerth.thomas.foobarremotecontroller2.connector

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class Beefweb(val ip: String) {

    private val listeners: MutableList<(JSONObject) -> Unit> = mutableListOf();

    fun addListener(listener: (JSONObject) -> Unit){
        listeners.add(listener)
    }

    fun start(){
        try {
            val url = URL(ip)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val reader = BufferedReader(InputStreamReader(connection.inputStream))

            while (true) {
                val line = reader.readLine() ?: break
                if (line.startsWith("data: ")){
                    val data = line.removePrefix("data: ")
                    val json = JSONObject(data)
                    listeners.forEach { t -> t(json) }
                }
            }

            reader.close()
            connection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setPlaylist(pl : String){

    }

}

fun main() {
    Beefweb("http://localhost:8880/api/query/updates?player=true&trcolumns=%title%&playqueue=true&playlists=true&plrange=0:100").apply {
        addListener(::analyzePlayer)
        addListener(::analyzePlaylists)
        setPlaylist("pl1")
        start()
    }
}

fun analyzePlayer(json: JSONObject){
    val player = json.optJSONObject("player") ?: return
    val activeItem = player.optJSONObject("activeItem") ?: return
    val columns = activeItem.optJSONArray("columns") ?: return
    val playbackState = player.getString("playbackState")
    if (columns.length() == 0 && playbackState == "playing")
        return
    println("Player: $playbackState")
    println("  Current Title: ${columns.optString(0)}")
    println("    Duration: ${activeItem.optInt("duration")}")
    println("    Position: ${activeItem.optInt("position")}")
}

fun analyzePlaylists(json: JSONObject){
    val playlists = json.optJSONArray("playlists") ?: return
    println("Playlists:")
    for (i in 0..<playlists.length()){
        val item = playlists.getJSONObject(i)
        println("- $i: ${item.getString("title")}")
        println("    id: ${item.getString("id")}")
        println("    isCurrent: ${item.getBoolean("isCurrent")}")
        println("    itemCount: ${item.getInt("itemCount")}")
    }
}
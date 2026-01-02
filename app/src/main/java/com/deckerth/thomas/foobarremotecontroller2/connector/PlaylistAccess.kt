package com.deckerth.thomas.foobarremotecontroller2.connector

import android.os.Build
import com.deckerth.thomas.foobarremotecontroller2.model.AddTracksBehaviors
import com.deckerth.thomas.foobarremotecontroller2.model.Playlist
import com.deckerth.thomas.foobarremotecontroller2.model.PlaylistEntity
import com.deckerth.thomas.foobarremotecontroller2.model.Playlists
import com.deckerth.thomas.foobarremotecontroller2.model.Title
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class PlaylistAccess(private val vm: AppViewModel) {
    private val errorHandler: ErrorHandler = vm.errorHandler

    val playlists: Playlists?
        get() {
            val response =
                queryPlaylists()
            return if (response == null) null
            else parsePlaylists(response)
        }

    private fun getFilenameWithoutExtension(fullPath: String): String {
        val lastSeparatorIndex = fullPath.lastIndexOf('\\')
        val filenameWithExtension =
            if (lastSeparatorIndex != -1) fullPath.substring(lastSeparatorIndex + 1) else fullPath

        val lastDotIndex = filenameWithExtension.lastIndexOf('.')
        return if (lastDotIndex != -1) filenameWithExtension.substring(
            0,
            lastDotIndex
        ) else filenameWithExtension
    }

    fun getPlaylist(
        playlistEntity: PlaylistEntity,
        startIndex: Int,
        withPaths: Boolean
    ): Playlist? {
        val response: Response?
        try {
            response = if (withPaths) vm.connector.getData(
                "playlists/" + playlistEntity.playlistId +
                        "/items/" + startIndex + "%3A" + 1000 + "?columns=%25label%25,%25catalog%25,%25composer%25,%25album%25,%25title%25,%25artist%25,%25samplerate%25,%25genre%25,%25discnumber%25,%25track%25,%25length%25,%25path%25",
                vm
            )
            else vm.connector.getData(
                "playlists/" + playlistEntity.playlistId +
                        "/items/" + startIndex + "%3A" + 1000 + "?columns=%25label%25,%25catalog%25,%25composer%25,%25album%25,%25title%25,%25artist%25,%25samplerate%25,%25genre%25,%25discnumber%25,%25track%25,%25length%25,%24filename%28%25path%25%29%24",
                vm
            )
        } catch (e: Exception) {
            errorHandler.logError(
                ErrorType.NETWORK,
                ErrorCode.CONNECTION_ERROR,
                ErrorSource.PLAYLIST_ITEMS,
                e
            )
            return null
        }
        return parsePlaylist(response, playlistEntity, startIndex, withPaths)
    }

    private fun parsePlaylist(
        input: Response,
        playlistEntity: PlaylistEntity,
        startIndex: Int,
        withPaths: Boolean
    ): Playlist? {
        val playlist = Playlist(playlistEntity)
        playlist.ipAddress = input.usedIpAddress
        try {
            val contentObject = JSONObject(input.message)
            val playlistItemsObject = contentObject.getJSONObject("playlistItems")
            val itemsArray = playlistItemsObject.getJSONArray("items")

            for (i in 0..<itemsArray.length()) {
                val itemObject = itemsArray.getJSONObject(i)
                val columnsArray = itemObject.getJSONArray("columns")

                /*
                      {
                      "playlistItems": {
                        "items": [
                            {
                                "columns": [
                                    "DGG",
                                    "0 9463 22000 2 0",
                                    "Macdonald, Rory",
                                    "Amazing Things",
                                    "Amazing Things",
                                    "Runrig",
                                    "44100",
                                    "Orchestral"
                                    "?",
                                    "01",
                                    "4:18"
                                    "<path>" / "<filename>"
                                ]
                            },
                            {
                */
                val label = columnsArray.getString(0)
                val catalog = columnsArray.getString(1)
                val composer = columnsArray.getString(2)
                val album = columnsArray.getString(3)
                val title = columnsArray.getString(4)
                val artist = columnsArray.getString(5)
                val samplerRate = columnsArray.getString(6)
                val genre = columnsArray.getString(7)
                val discNumber = columnsArray.getString(8)
                val track = columnsArray.getString(9)
                val length = columnsArray.getString(10)
                var path = ""
                val filename: String?
                if (withPaths) {
                    path = columnsArray.getString(11)
                    filename = getFilenameWithoutExtension(path)
                } else filename = columnsArray.getString(11)
                var effectiveTitle = ""
                if (title != filename) effectiveTitle = title

                playlist.addTitle(
                    Title(
                        playlistEntity.playlistId,
                        i + startIndex,
                        label,
                        catalog,
                        composer,
                        album,
                        effectiveTitle,
                        artist,
                        samplerRate,
                        genre,
                        discNumber,
                        track,
                        length, "", "",
                        vm.connector.serverAddress(input.usedIpAddress) + "artwork/" + playlistEntity.playlistId + "/" + (i + startIndex),
                        path
                    )
                )
            }
        } catch (e: JSONException) {
            errorHandler.logError(
                ErrorType.API,
                ErrorCode.BAD_RESPONSE,
                ErrorSource.PLAYLIST_ITEMS,
                e
            )
            //e.printStackTrace();
            return null
        }

        return playlist
    }

    private fun queryPlaylists(): Response? {
        val response: Response?
        try {
            response = vm.connector.getData("playlists", vm)
        } catch (e: Exception) {
            errorHandler.logError(
                ErrorType.NETWORK,
                ErrorCode.CONNECTION_ERROR,
                ErrorSource.PLAYLIST_ITEMS,
                e
            )
            return null
        }
        return response
    }

    private fun parsePlaylists(input: Response): Playlists? {
        val result = Playlists()
        result.ipAddress = input.usedIpAddress
        try {
            val playlistsObject = JSONObject(input.message)
            val playlistArray = playlistsObject.getJSONArray("playlists")
            for (i in 0..<playlistArray.length()) {
                val playlistObject = playlistArray.getJSONObject(i)

                /*                "id": "p1",
                        "index": 0,
                        "isCurrent": false,
                        "itemCount": 12,
                        "title": "Default Playlist",
                        "totalTime": 0 */
                result.addPlaylistEntity(
                    PlaylistEntity(
                        playlistObject.getString("id"),
                        playlistObject.getString("title"),
                        playlistObject.getBoolean("isCurrent"),
                        playlistObject.getInt("itemCount")
                    )
                )
            }
        } catch (e: JSONException) {
            errorHandler.logError(ErrorType.API, ErrorCode.BAD_RESPONSE, ErrorSource.PLAYLISTS, e)
            //e.printStackTrace();
            return null
        }
        return result
    }

    fun addPathsToPlaylist(playlistId: String?, path: String?, addBehavior: AddTracksBehaviors) {
        val paths: MutableList<String> = ArrayList()
        paths.add(path!!)
        addPathsToPlaylist(playlistId, paths, addBehavior)
    }

    fun addPathsToPlaylist(
        playlistId: String?,
        paths: List<String>,
        addBehavior: AddTracksBehaviors
    ) {
//        {
//            "items": [
//            "T:\\Music\\Alpha\\Alpha 634"
//             ],
//            "play": true
//        }
        //val playValue: String?
        val playValue = when (addBehavior) {
            AddTracksBehaviors.ADD_BEHAVIOR_ADD -> {
                "false"
            }

            AddTracksBehaviors.ADD_BEHAVIOR_ADD_PLAY -> {
                "false"
            }

            AddTracksBehaviors.ADD_BEHAVIOR_REPLACE_PLAY -> {
                "true"
            }
        }
        var pos = 0
        val pathString = StringBuilder()
        for (path in paths) {
            pathString.append("\"").append(path.replace("\\", "\\\\")).append("\"")
            if (pos < paths.size - 1)  // 0, 1 : indexes.size() = 2
                pathString.append(", ")
            pos++
        }
        val jsonString =
            "{\"items\":[ $pathString], \"play\":$playValue, \"replace\":$playValue }"
        vm.connector.postData("playlists/$playlistId/items/add/", jsonString, vm)
    }

    fun removeTitles(playlistId: String, indexes: List<Int>) {
        //        {
        //            "items": [
        //               1,
        //               4
        //             ]
        //        }

        val jsonString = StringBuilder("{\"items\":[ ")
        var pos = 0
        for (index in indexes) {
            jsonString.append(index)
            if (pos < indexes.size - 1)  // 0, 1 : indexes.size() = 2
                jsonString.append(", ")
            pos++
        }
        jsonString.append(" ] }")
        vm.connector.postData(
            endpoint = "playlists/$playlistId/items/remove",
            data = jsonString.toString(),
            vm = vm
        )
    }

    fun copyTitles(
        fromPlaylistId: String,
        toPlaylistId: String,
        targetIndex: Int,
        indexes: MutableList<Int>
    ) {
        //        playlists/{sourceId}/{targetId}/items/copy
        //
        //        {
        //            "items": [
        //               1,
        //               4
        //             ]
        //            "targetIndex": 1
        //        }

        val jsonString = StringBuilder("{\"items\":[ ")
        var pos = 0
        for (index in indexes) {
            jsonString.append(index)
            if (pos < indexes.size - 1)  // 0, 1 : indexes.size() = 2
                jsonString.append(", ")
            pos++
        }
        jsonString.append(" ] }")
        vm.connector.postData(
            "playlists/$fromPlaylistId/$toPlaylistId/items/copy",
            jsonString.toString(),
            vm
        )
    }

    fun addTitleToPlaybackQueue(fromPlaylistId: String?, index: Int) {
        //        playqueue/add
        //        {
        //            "plref": "p1"
        //            "index": 1
        //        }

        vm.connector.postData(
            "playqueue/add",
            "{ \"plref\": \"$fromPlaylistId\", \"itemIndex\":$index}",
            vm
        )
    }

    @JvmOverloads
    fun addPlaylist(
        position: Int,
        name: String?,
        paths: List<String>? = null,
        addBehavior: AddTracksBehaviors? = null // either both parameters are null or both are set
    ) {
        // http://localhost:8880/api/playlists/add?index=11&title=test

        var encodedName: String?
        try {
            encodedName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU is API level 33
                URLEncoder.encode(name, StandardCharsets.UTF_8)
            } else {
                // Use the deprecated version for older APIs
                URLEncoder.encode(name, StandardCharsets.UTF_8.name())
            }
        } catch (e: UnsupportedEncodingException) {
            // Handle the exception, though UTF-8 should always be supported
            e.printStackTrace()
            encodedName = name // Fallback or throw an error
        }
        vm.connector.postData("playlists/add?index=$position&title=$encodedName", vm)
        if (paths != null && addBehavior != null) addPathsToPlaylist("" + position, paths, addBehavior)
    }
}

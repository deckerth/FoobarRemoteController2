package com.deckerth.thomas.foobarremotecontroller2.connector;

import android.os.Build;

import com.deckerth.thomas.foobarremotecontroller2.model.AddTracksBehaviors;
import com.deckerth.thomas.foobarremotecontroller2.model.Playlist;
import com.deckerth.thomas.foobarremotecontroller2.model.PlaylistEntity;
import com.deckerth.thomas.foobarremotecontroller2.model.Playlists;
import com.deckerth.thomas.foobarremotecontroller2.model.Title;
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PlaylistAccess {

    private final ErrorHandler errorHandler;
    private final AppViewModel vm;

    public PlaylistAccess(AppViewModel vm) {
        this.vm = vm;
        this.errorHandler = vm.getErrorHandler();
    }

    public Playlists getPlaylists() {
        Response response = queryPlaylists();
        if (response == null)
            return null;
        else
            return parsePlaylists(response);
    }

    private String getFilenameWithoutExtension(String fullPath) {
        int lastSeparatorIndex = fullPath.lastIndexOf('\\');
        String filenameWithExtension = (lastSeparatorIndex != -1) ?
                fullPath.substring(lastSeparatorIndex + 1) :
                fullPath;

        int lastDotIndex = filenameWithExtension.lastIndexOf('.');
        return (lastDotIndex != -1) ?
                filenameWithExtension.substring(0, lastDotIndex) :
                filenameWithExtension;
    }

    public Playlist getPlaylist(PlaylistEntity playlistEntity, int startIndex, boolean withPaths) {
        Response response;
        try {
            if (withPaths)
                response = vm.connector.getData("playlists/" + playlistEntity.getPlaylistId() +
                        "/items/" + startIndex + "%3A" + 1000 + "?columns=%25label%25,%25catalog%25,%25composer%25,%25album%25,%25title%25,%25artist%25,%25samplerate%25,%25genre%25,%25discnumber%25,%25track%25,%25length%25,%25path%25", vm);
            else
                response = vm.connector.getData("playlists/" + playlistEntity.getPlaylistId() +
                        "/items/" + startIndex + "%3A" + 1000 + "?columns=%25label%25,%25catalog%25,%25composer%25,%25album%25,%25title%25,%25artist%25,%25samplerate%25,%25genre%25,%25discnumber%25,%25track%25,%25length%25,%24filename%28%25path%25%29%24", vm);
        } catch (Exception e) {
            errorHandler.logError(ErrorType.NETWORK, ErrorCode.CONNECTION_ERROR, ErrorSource.PLAYLIST_ITEMS, e);
            return null;
        }
        return parsePlaylist(response, playlistEntity, startIndex, withPaths);
    }

    private Playlist parsePlaylist(Response input, PlaylistEntity playlistEntity, int startIndex, boolean withPaths) {
        Playlist playlist = new Playlist(playlistEntity);
        playlist.setIpAddress(input.getUsedIpAddress());
        try {
            JSONObject contentObject = new JSONObject(input.getMessage());
            JSONObject playlistItemsObject = contentObject.getJSONObject("playlistItems");
            JSONArray itemsArray = playlistItemsObject.getJSONArray("items");

            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject itemObject = itemsArray.getJSONObject(i);
                JSONArray columnsArray = itemObject.getJSONArray("columns");
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

                String label = columnsArray.getString(0);
                String catalog = columnsArray.getString(1);
                String composer = columnsArray.getString(2);
                String album = columnsArray.getString(3);
                String title = columnsArray.getString(4);
                String artist = columnsArray.getString(5);
                String samplerRate = columnsArray.getString(6);
                String genre = columnsArray.getString(7);
                String discNumber = columnsArray.getString(8);
                String track = columnsArray.getString(9);
                String length = columnsArray.getString(10);
                String path = "";
                String filename;
                if (withPaths) {
                    path = columnsArray.getString(11);
                    filename = getFilenameWithoutExtension(path);
                } else
                    filename = columnsArray.getString(11);
                String effectiveTitle = "";
                if (!title.equals(filename)) effectiveTitle = title;

                playlist.addTitle(
                        new Title(
                                playlistEntity.getPlaylistId(),
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
                                vm.connector.serverAddress(input.getUsedIpAddress()) + "artwork/" + playlistEntity.getPlaylistId() + "/" + (i + startIndex),
                                path));
            }
        } catch (JSONException e) {
            errorHandler.logError(ErrorType.API, ErrorCode.BAD_RESPONSE, ErrorSource.PLAYLIST_ITEMS, e);
            //e.printStackTrace();
            return null;
        }

        return playlist;
    }

    private Response queryPlaylists() {
        Response response;
        try {
            response = vm.connector.getData("playlists", vm);
        } catch (Exception e) {
            errorHandler.logError(ErrorType.NETWORK, ErrorCode.CONNECTION_ERROR, ErrorSource.PLAYLIST_ITEMS, e);
            return null;
        }
        return response;
    }

    private Playlists parsePlaylists(Response input) {
        Playlists result = new Playlists();
        result.ipAddress = input.getUsedIpAddress();
        try {
            JSONObject playlistsObject = new JSONObject(input.getMessage());
            JSONArray playlistArray = playlistsObject.getJSONArray("playlists");
            for (int i = 0; i < playlistArray.length(); i++) {
                JSONObject playlistObject = playlistArray.getJSONObject(i);
      /*                "id": "p1",
                        "index": 0,
                        "isCurrent": false,
                        "itemCount": 12,
                        "title": "Default Playlist",
                        "totalTime": 0 */

                result.addPlaylistEntity(new PlaylistEntity(playlistObject.getString("id"),
                        playlistObject.getString("title"),
                        playlistObject.getBoolean("isCurrent"),
                        playlistObject.getInt("itemCount")));
            }
        } catch (JSONException e) {
            errorHandler.logError(ErrorType.API, ErrorCode.BAD_RESPONSE, ErrorSource.PLAYLISTS, e);
            //e.printStackTrace();
            return null;
        }
        return result;
    }

    public void addPathsToPlaylist(String playlistId, String path, AddTracksBehaviors addBehavior) {
        List<String> paths = new ArrayList<>();
        paths.add(path);
        addPathsToPlaylist(playlistId, paths, addBehavior);
    }

    public void addPathsToPlaylist(String playlistId, List<String> paths, AddTracksBehaviors addBehavior) {
//        {
//            "items": [
//            "T:\\Music\\Alpha\\Alpha 634"
//             ],
//            "play": true
//        }
        String playValue;
        String replaceValue = switch (addBehavior) {
            case ADD_BEHAVIOR_ADD -> {
                playValue = "false";
                yield "false";
            }
            case ADD_BEHAVIOR_ADD_PLAY -> {
                playValue = "true";
                yield "false";
            }
            case ADD_BEHAVIOR_REPLACE_PLAY -> {
                playValue = "true";
                yield "true";
            }
        };
        int pos = 0;
        StringBuilder pathString = new StringBuilder();
        for (String path : paths) {
            pathString.append("\"").append(path.replace("\\", "\\\\")).append("\"");
            if (pos < paths.size() - 1) // 0, 1 : indexes.size() = 2
                pathString.append(", ");
            pos++;
        }
        String jsonString = "{\"items\":[ " + pathString + "], \"play\":" + playValue + ", \"replace\":" + replaceValue + " }";
        vm.connector.postData("playlists/" + playlistId + "/items/add/", jsonString, vm);
    }

    public void removeTitles(String playlistId, List<Integer> indexes) {

        //        {
        //            "items": [
        //               1,
        //               4
        //             ]
        //        }

        StringBuilder jsonString = new StringBuilder("{\"items\":[ ");
        int pos = 0;
        for (Integer index : indexes) {
            jsonString.append(index);
            if (pos < indexes.size() - 1) // 0, 1 : indexes.size() = 2
                jsonString.append(", ");
            pos++;
        }
        jsonString.append(" ] }");
        vm.connector.postData("playlists/" + playlistId + "/items/remove", jsonString.toString(), vm);
    }

    public void copyTitles(String fromPlaylistId, String toPlaylistId, int targetIndex, List<Integer> indexes) {

        //        playlists/{sourceId}/{targetId}/items/copy
        //
        //        {
        //            "items": [
        //               1,
        //               4
        //             ]
        //            "targetIndex": 1
        //        }

        StringBuilder jsonString = new StringBuilder("{\"items\":[ ");
        int pos = 0;
        for (Integer index : indexes) {
            jsonString.append(index);
            if (pos < indexes.size() - 1) // 0, 1 : indexes.size() = 2
                jsonString.append(", ");
            pos++;
        }
        jsonString.append(" ] }");
        vm.connector.postData("playlists/" + fromPlaylistId + "/" + toPlaylistId + "/items/copy", jsonString.toString(), vm);
    }

    public void addTitleToPlaybackQueue(String fromPlaylistId, int index) {

        //        playqueue/add
        //        {
        //            "plref": "p1"
        //            "index": 1
        //        }

        vm.connector.postData("playqueue/add", "{ \"plref\": \"" + fromPlaylistId + "\", \"itemIndex\":" + index + "}", vm);
    }

    public void addPlaylist(int position, String name) {
        addPlaylist(position, name, null, null);
    }

    public void addPlaylist(int position, String name, List<String> paths, AddTracksBehaviors addBehavior) {
        // http://localhost:8880/api/playlists/add?index=11&title=test

        String encodedName;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU is API level 33
                encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
            } else {
                // Use the deprecated version for older APIs
                encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8.name());
            }
        } catch (UnsupportedEncodingException e) {
            // Handle the exception, though UTF-8 should always be supported
            e.printStackTrace();
            encodedName = name; // Fallback or throw an error
        }
        vm.connector.postData("playlists/add?index=" + position + "&title=" + encodedName, vm);
        if (paths != null)
            addPathsToPlaylist("" + position, paths, addBehavior);
    }
}

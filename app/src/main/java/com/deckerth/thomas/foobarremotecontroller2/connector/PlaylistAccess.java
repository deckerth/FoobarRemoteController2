package com.deckerth.thomas.foobarremotecontroller2.connector;

import com.deckerth.thomas.foobarremotecontroller2.model.AddTracksBehaviors;
import com.deckerth.thomas.foobarremotecontroller2.model.Playlist;
import com.deckerth.thomas.foobarremotecontroller2.model.PlaylistEntity;
import com.deckerth.thomas.foobarremotecontroller2.model.Playlists;
import com.deckerth.thomas.foobarremotecontroller2.model.Title;
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public Playlist getPlaylist(PlaylistEntity playlistEntity, int startIndex) {
        Response response;
        try {
            response = vm.connector.getData("playlists/" + playlistEntity.getPlaylistId() +
                    "/items/" + startIndex + "%3A" + 1000 + "?columns=%25label%25,%25catalog%25,%25composer%25,%25album%25,%25title%25,%25artist%25,%25discnumber%25,%25track%25,%25length%25,%24filename%28%25path%25%29%24");
        } catch (Exception e) {
            errorHandler.logError(ErrorType.NETWORK, ErrorCode.CONNECTION_ERROR, ErrorSource.PLAYLIST_ITEMS, e);
            return null;
        }
        return parsePlaylist(response, playlistEntity, startIndex);
    }

    private Playlist parsePlaylist(Response input, PlaylistEntity playlistEntity, int startIndex) {
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
                                    "?",
                                    "01",
                                    "4:18"
                                    "<filename>"
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
                String discNumber = columnsArray.getString(6);
                String track = columnsArray.getString(7);
                String length = columnsArray.getString(8);
                String filename = columnsArray.getString(9);

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
                                discNumber,
                                track,
                                length, "", "",
                                vm.connector.serverAddress(input.getUsedIpAddress()) + "artwork/" + playlistEntity.getPlaylistId() + "/" + (i + startIndex)));
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
            response = vm.connector.getData("playlists");
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

    public void addPathToPlaylist(String playlistId, String path, AddTracksBehaviors addBehavior) {
//        {
//            "items": [
//            "T:\\Music\\Alpha\\Alpha 634"
//             ],
//            "play": true
//        }
        String playValue = "false";
        String replaceValue = "true";
        switch (addBehavior) {
            case ADD_BEHAVIOR_ADD:
                playValue = "false";
                replaceValue = "false";
                break;
            case ADD_BEHAVIOR_ADD_PLAY:
                playValue = "true";
                replaceValue = "false";
                break;
            case ADD_BEHAVIOR_REPLACE_PLAY:
                playValue = "true";
                replaceValue = "true";
                break;
        }
        String jsonString = "{\"items\":[\"" + path + "\"], \"play\":" + playValue + ", \"replace\":" + replaceValue + " }";
        vm.connector.postData("playlists/" + playlistId + "/items/add/", jsonString);
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
        vm.connector.postData("playlists/" + playlistId + "/items/remove", jsonString.toString());
    }
}

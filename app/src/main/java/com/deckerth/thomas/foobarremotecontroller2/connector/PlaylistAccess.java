package com.deckerth.thomas.foobarremotecontroller2.connector;

import android.annotation.SuppressLint;

import com.deckerth.thomas.foobarremotecontroller2.model.Playlist;
import com.deckerth.thomas.foobarremotecontroller2.model.PlaylistEntity;
import com.deckerth.thomas.foobarremotecontroller2.model.Playlists;
import com.deckerth.thomas.foobarremotecontroller2.model.Title;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlaylistAccess {

    @SuppressLint("StaticFieldLeak")
    private static PlaylistAccess INSTANCE;
    private HTTPConnector mConnector;

    private final ErrorHandler errorHandler;

    public PlaylistAccess() {
        this.errorHandler = ErrorHandlerKt.getErrorHandler();
    }

    public static PlaylistAccess getInstance() {
        if (INSTANCE == null)
            INSTANCE = new PlaylistAccess();
        return INSTANCE;
    }

    public Playlists getPlaylists() {
        if (mConnector == null)
            this.mConnector = new HTTPConnector();
        String response = queryPlaylists();
        if (response == null)
            return null;
        else
            return parsePlaylists(response);
    }

    public Playlist getPlaylist(PlaylistEntity playlistEntity, int startIndex) {
        String response;
        try {
            response = mConnector.getData("playlists/" + playlistEntity.getPlaylistId() +
                    "/items/" + startIndex + "%3A" + 1000 + "?columns=%25label%25,%25catalog%25,%25composer%25,%25album%25,%25title%25,%25artist%25,%25discnumber%25,%25track%25,%25length%25");
        } catch (Exception e) {
            errorHandler.logError(ErrorType.NETWORK, ErrorCode.CONNECTION_ERROR, ErrorSource.PLAYLIST_ITEMS, e);
            return null;
        }
        return parsePlaylist(response, playlistEntity, startIndex);
    }

    private Playlist parsePlaylist(String input, PlaylistEntity playlistEntity, int startIndex) {
        Playlist playlist = new Playlist(playlistEntity);
        try {
            JSONObject contentObject = new JSONObject(input);
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

                playlist.addTitle(
                        new Title(
                                playlistEntity.getPlaylistId(),
                                i + startIndex,
                                label,
                                catalog,
                                composer,
                                album,
                                title,
                                artist,
                                discNumber,
                                track,
                                length, "", "",
                                mConnector.getServerAddress() + "artwork/" + playlistEntity.getPlaylistId() + "/" + (i + startIndex)));
            }
        } catch (JSONException e) {
            errorHandler.logError(ErrorType.API, ErrorCode.BAD_RESPONSE, ErrorSource.PLAYLIST_ITEMS, e);
            e.printStackTrace();
            return null;
        }

        return playlist;
    }

    private String queryPlaylists() {
        String response;
        try {
            response = mConnector.getData("playlists");
        } catch (Exception e) {
            errorHandler.logError(ErrorType.NETWORK, ErrorCode.CONNECTION_ERROR, ErrorSource.PLAYLIST_ITEMS, e);
            return null;
        }
        return response;
    }

    private Playlists parsePlaylists(String input) {
        Playlists result = new Playlists();
        try {
            JSONObject playlistsObject = new JSONObject(input);
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
            e.printStackTrace();
            return null;
        }
        return result;
    }

}

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

    public static PlaylistAccess getInstance() {
        if (INSTANCE == null)
            INSTANCE = new PlaylistAccess();
        return INSTANCE;
    }

    public Playlist getCurrentPlaylist() {
        if (mConnector == null)
            this.mConnector = new HTTPConnector();
        String response = queryPlaylists();
        Playlists playlists = parsePlaylists(response);
        PlaylistEntity current = playlists.getCurrentPlaylist();
        if (current != null) {
            return getPlaylist(current);
        }
        return null;
    }

    public Playlist getPlaylist(PlaylistEntity playlistEntity) {
        String response = mConnector.getData("playlists/" + playlistEntity.getPlaylistId() +
                "/items/0%3A100?columns=%25catalog%25,%25composer%25,%25album%25,%25title%25,%25artist%25,%25discnumber%25,%25track%25,%25length%25");
        return parsePlaylist(response, playlistEntity);
//        try {
//                mActivity.runOnUiThread(() -> {
//                    Playlist playlist = parsePlaylist(response, playlistEntity);
//                    PlaylistViewModel viewModel = PlaylistViewModel.getInstance();
//                    Objects.requireNonNull(viewModel).setDisplayedPlaylist(playlistEntity);
//                    viewModel.setPlaylist(playlist);
//                });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private Playlist parsePlaylist(String input, PlaylistEntity playlistEntity) {
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

                String catalog = columnsArray.getString(0);
                String composer = columnsArray.getString(1);
                String album = columnsArray.getString(2);
                String title = columnsArray.getString(3);
                String artist = columnsArray.getString(4);
                String discNumber = columnsArray.getString(5);
                String track = columnsArray.getString(6);
                String length = columnsArray.getString(7);

                playlist.addTitle(
                        new Title(
                                playlistEntity.getPlaylistId(),
                                String.valueOf(i),
                                catalog,
                                composer,
                                album,
                                title,
                                artist,
                                discNumber,
                                track,
                                length, "", "",
                                mConnector.getServerAddress() + "artwork/" + playlistEntity.getPlaylistId() + "/" + i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return playlist;
    }

    private String queryPlaylists() {
        return mConnector.getData("playlists");

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
                result.addPlaylistEntity(new PlaylistEntity(playlistObject.getString("id"), playlistObject.getString("title"), playlistObject.getBoolean("isCurrent")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}

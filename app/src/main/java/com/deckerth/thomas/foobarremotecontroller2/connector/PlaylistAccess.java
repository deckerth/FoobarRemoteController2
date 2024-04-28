package com.deckerth.thomas.foobarremotecontroller2.connector;

import android.app.Activity;
import android.graphics.Bitmap;

import androidx.preference.PreferenceManager;

import com.deckerth.thomas.foobarremotecontroller2.model.ITitle;
import com.deckerth.thomas.foobarremotecontroller2.model.Playlist;
import com.deckerth.thomas.foobarremotecontroller2.model.PlaylistEntity;
import com.deckerth.thomas.foobarremotecontroller2.model.Playlists;
import com.deckerth.thomas.foobarremotecontroller2.model.Title;
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlaylistViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaylistAccess {

    private static PlaylistAccess INSTANCE;

    public static PlaylistAccess getInstance() {
        if (INSTANCE == null)
            INSTANCE = new PlaylistAccess();
        return INSTANCE;
    }

    private HTTPConnector mConnector;
    private Activity mActivity;

    public void getCurrentPlaylist(Activity activity) {
        mActivity = activity;
        if (mConnector == null)
            this.mConnector = new HTTPConnector(PreferenceManager.getDefaultSharedPreferences(mActivity.getBaseContext()));
        new Thread(() -> {
            String response = queryPlaylists();
            Playlists playlists = parsePlaylists(response);
            String current = playlists.getCurrentPlaylist();
            if (!current.isEmpty()) {
                getPlaylist(new PlaylistEntity(current, true));
            }
        }).start();
    }

    private void getPlaylist(PlaylistEntity playlistEntity) {
        new Thread(() -> {
            String response = mConnector.getData("playlists/" + playlistEntity.getPlaylistId() +
                    "/items/0%3A100?columns=%25catalog%25,%25composer%25,%25album%25,%25title%25,%25artist%25,%25discnumber%25,%25track%25,%25length%25");
            try {
                mActivity.runOnUiThread(() -> {
                    Playlist playlist = parsePlaylist(response, playlistEntity);
                    PlaylistViewModel.getInstance().setPlaylist(playlist);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private Playlist parsePlaylist(String input, PlaylistEntity playlistEntity) {
        Playlist playlist = new Playlist(playlistEntity);
        try {
            JSONObject contentObject = new JSONObject(input);
            JSONObject playlistItemsObject = contentObject.getJSONObject("playlistItems");
            JSONArray itemsArray = playlistItemsObject.getJSONArray("items");

            for (int i=0; i<itemsArray.length(); i++){
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
                                length, "", ""));
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
            for (int i=0; i<playlistArray.length(); i++){
                JSONObject playlistObject = playlistArray.getJSONObject(i);
      /*                "id": "p1",
                        "index": 0,
                        "isCurrent": false,
                        "itemCount": 12,
                        "title": "Default Playlist",
                        "totalTime": 0 */
                result.addPlaylistEntity(new PlaylistEntity(playlistObject.getString("id"), playlistObject.getBoolean("isCurrent")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  result;
    }

    private Map<String, Bitmap> mCoverCache = new HashMap<>();

    public void getCovers(List<ITitle> playlist) {
        new Thread(() -> {
            List<ITitle> playlistWithCovers = new ArrayList<>();
            Boolean coversChanged = false;
            for (ITitle title : playlist) {
                if (title.getIsAlbum() && title.getArtwork() == null) {
                    ITitle clonedTitle = title.clone();
                    if (mCoverCache.containsKey(title.getCatalog())) {
                        clonedTitle.setArtwork(mCoverCache.get(title.getCatalog()));
                        coversChanged = true;
                    }
                    else {
                        Bitmap cover = mConnector.getImage("artwork/" + title.getPlaylistId() + "/" + title.getIndex());
                        mCoverCache.put(title.getCatalog(), cover);
                        if (cover != null) {
                            clonedTitle.setArtwork(cover);
                            coversChanged = true;
                        }
                    }
                    playlistWithCovers.add(clonedTitle);
                }
                else
                    playlistWithCovers.add(title);
            }
            if (coversChanged)
                mActivity.runOnUiThread(() -> {
                    PlaylistViewModel.getInstance().setCovers(playlistWithCovers); });
                }).start();
    }
}

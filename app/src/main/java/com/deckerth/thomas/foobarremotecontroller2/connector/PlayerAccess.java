package com.deckerth.thomas.foobarremotecontroller2.connector;

import android.app.Activity;
import android.graphics.Bitmap;

import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlayerViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlayerAccess {

    private static PlayerAccess INSTANCE;


    public static PlayerAccess getInstance() {
        return INSTANCE;
    }
    public static PlayerAccess getInstance(Activity activity) {
        if (INSTANCE == null)
            INSTANCE = new PlayerAccess(activity);
        return INSTANCE;
    }

    private PlayerViewModel mPlayerViewModel;
    private Activity mActivity;
    private HTTPConnector mConnector;

    public PlayerAccess(Activity mActivity) {
        this.mPlayerViewModel = PlayerViewModel.getInstance();
        this.mActivity = mActivity;
        this.mConnector = new HTTPConnector();
    }

    private String mLastPlayerState;

    public void startPlayerObserver() {

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        final Runnable task = new Runnable() {
            public void run() {
                mLastPlayerState = mConnector.getData("player?columns=%25catalog%25,%25composer%25,%25album%25,%25title%25,%25artist%25,%25discnumber%25,%25track%25,%25playback_time%25");
                mActivity.runOnUiThread(() -> {
                    parsePlayerState(mLastPlayerState);
                    getArtwork();
                });
            }
        };

        // Schedule the task to run with an initial delay and then periodically
        // For example, to run every second after an initial delay of 0 seconds
        scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
    }

    private void getPlayerState() {
        new Thread(() -> {
            mLastPlayerState = mConnector.getData("player?columns=%25catalog%25,%25composer%25,%25album%25,%25title%25,%25artist%25,%25discnumber%25,%25track%25,%25playback_time%25");
            mActivity.runOnUiThread(() -> {
                parsePlayerState(mLastPlayerState);
                getArtwork();
            });
        }).start();
    }

    String mCurrentPlaylistId = "";
    String mCurrentIndex = "";

    private void parsePlayerState(String input) {
        try {
            JSONObject contentObject = new JSONObject(input);
            JSONObject playerObject = contentObject.getJSONObject("player");
            JSONObject activeItemObject = playerObject.getJSONObject("activeItem");
            JSONArray columns = activeItemObject.getJSONArray("columns");

            if (columns.length() == 0) {
                mPlayerViewModel.ClearPlaybackState();
                mCurrentCatalog = "";
                mLastCatalog = "";
            } else {

              /*
                "player": {
                    "activeItem": {
                        "columns": [
                            "7353 72726",
                            "Franck, César (1822-1890)",
                            "Klavierquintett f-Moll",
                            "1. Molto moderato quasi lento - Allegro",
                            "Khatia Buniatishvili, Klavier / Gidon Kremer & Marija Nemanytė, Violine / Maxim Rysanov, Viola / Giedrė Dirvanauskaitė, Cello",
                            "?",
                            "02",
                            "0:28"
                        ],
                        "duration": 955.8266666666667,
                        "index": 13,
                        "playlistId": "p4",
                        "playlistIndex": 3,
                        "position": 28.9795
                  },
                */

                String column = columns.getString(0);
                mPlayerViewModel.setCatalog(column);
                mCurrentCatalog = column;
                column = columns.getString(1);
                mPlayerViewModel.setComposer(column);
                column = columns.getString(2);
                mPlayerViewModel.setAlbum(column);
                column = columns.getString(3);
                mPlayerViewModel.setTitle(column);
                column = columns.getString(4);
                mPlayerViewModel.setArtist(column);
                column = columns.getString(5);
                mPlayerViewModel.setDiscNumber(column);
                column = columns.getString(6);
                mPlayerViewModel.setTrack(column);
                column = columns.getString(7);
                mPlayerViewModel.setPlaybackTime(column);

                mPlayerViewModel.setPlaylistId(activeItemObject.getString("playlistId"));
                mPlayerViewModel.setIndex(activeItemObject.getString("index"));
                mPlayerViewModel.setDuration(activeItemObject.getString("duration"));
                mPlayerViewModel.setPosition(activeItemObject.getString("position"));
             }
            mCurrentPlaylistId = activeItemObject.getString("playlistId");
            mCurrentIndex = activeItemObject.getString("index");

            String state = playerObject.getString("playbackState");
            switch (state) {
                case "stopped":
                    mPlayerViewModel.setPlaybackState(PlayerViewModel.PlaybackState.STOPPED);
                    break;
                case "playing":
                    mPlayerViewModel.setPlaybackState(PlayerViewModel.PlaybackState.PLAYING);
                    break;
                case "paused":
                    mPlayerViewModel.setPlaybackState(PlayerViewModel.PlaybackState.PAUSED);
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
            mPlayerViewModel.ClearPlaybackState();
        }
    }

    private String mCurrentCatalog = "";
    private String mLastCatalog = "";

    private void getArtwork() {
        String currentCatalog = mPlayerViewModel.getCatalog().getValue();
        if (!currentCatalog.isEmpty() &&
                !currentCatalog.equals(mLastCatalog)) {
            mLastCatalog = mPlayerViewModel.getCatalog().getValue();
            new Thread(() -> {
                Bitmap artwork = mConnector.getImage("artwork/" + mCurrentPlaylistId + "/" + mCurrentIndex);
                mActivity.runOnUiThread(() -> {
                    mPlayerViewModel.setArtwork(artwork);
                });
            }).start();
        }
    }

    public void StartPlayback() {
        new Thread(() -> {
            mConnector.postData("player/play");
            getPlayerState();
        }).start();
    }

    public void PausePlayback() {
        new Thread(() -> {
            mConnector.postData("player/pause");
            getPlayerState();
        }).start();
    }

    public void StopPlayback() {
        new Thread(() -> {
            mConnector.postData("player/stop");
            getPlayerState();
        }).start();
    }

    public void PreviousTrack() {
        new Thread(() -> {
            mConnector.postData("player/previous");
            getPlayerState();
        }).start();
    }

    public void NextTrack() {
        new Thread(() -> {
            mConnector.postData("player/next");
        }).start();
    }

    public void PlayTrack(String playlistId, String index) {
        new Thread(() -> {
            mConnector.postData("player/play/"+playlistId+"/"+index);
            getPlayerState();
        }).start();
    }

}

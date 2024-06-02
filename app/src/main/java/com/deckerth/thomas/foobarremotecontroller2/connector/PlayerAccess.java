package com.deckerth.thomas.foobarremotecontroller2.connector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;

import androidx.preference.PreferenceManager;

import com.deckerth.thomas.foobarremotecontroller2.model.VolumeControl;
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlayerViewModel;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlayerAccess {

    @SuppressLint("StaticFieldLeak")
    private static PlayerAccess INSTANCE;


    public static PlayerAccess getInstance() {
        return INSTANCE;
    }

    public static PlayerAccess getInstance(Activity activity) {
        if (INSTANCE == null)
            INSTANCE = new PlayerAccess(activity);
        return INSTANCE;
    }

    private final PlayerViewModel mPlayerViewModel;
    private final Activity mActivity;
    private final HTTPConnector mConnector;

    public PlayerAccess(Activity mActivity) {
        this.mPlayerViewModel = PlayerViewModel.getInstance();
        this.mActivity = mActivity;
        this.mConnector = new HTTPConnector(PreferenceManager.getDefaultSharedPreferences(mActivity.getBaseContext()));
    }

    private String mLastPlayerState;
    private Boolean mObserverIsRunning = false;

    public void startPlayerObserver() {

        if (mObserverIsRunning) return;

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        final Runnable task = () -> {
            mLastPlayerState = mConnector.getData("player?columns=%25catalog%25,%25composer%25,%25album%25,%25title%25,%25artist%25,%25discnumber%25,%25track%25,%25playback_time%25");
            mActivity.runOnUiThread(() -> {
                parsePlayerState(mLastPlayerState);
                getArtwork();
                mObserverIsRunning = true;
            });
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
            JSONObject volumeObject = playerObject.getJSONObject("volume");
            JSONArray columns = activeItemObject.getJSONArray("columns");

            if (columns.length() == 0) {
                mPlayerViewModel.ClearPlaybackState();
                mLastCatalog = "";
            } else {

              /*
                "player": {
                    "activeItem": {
                        "columns": [
                            "7353 72726",
                            "Franck, César (1822-1890)",
                            "Klavierquintett f-Moll", !! OPTIONAL may be just ""
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
                 "volume": {
                    "isMuted": false,
                    "max": 0,
                    "min": -100,
                    "type": "db",
                    "value": 0
                }
               */

                String column = columns.getString(0);
                mPlayerViewModel.setCatalog(column);
                column = columns.getString(1);
                mPlayerViewModel.setComposer(column);
                column = columns.getString(2);
                mPlayerViewModel.setAlbum(column);
                String title = columns.getString(3);
                column = columns.getString(4);
                mPlayerViewModel.setArtist(column);
                column = columns.getString(5);
                mPlayerViewModel.setDiscNumber(column);
                column = columns.getString(6);
                String track = columns.getString(6);
                if (!title.equals(track))
                    mPlayerViewModel.setTitle(title);
                else
                    mPlayerViewModel.setTitle("");
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

            if (volumeObject != null) {
                VolumeControl volumeControl = mPlayerViewModel.getVolumeControlViewModel().getVolumeControl();
                mPlayerViewModel.getVolumeControlViewModel().setIsMuted(volumeObject.getBoolean("isMuted"));

                volumeControl.setMin(volumeObject.getInt("min"));
                volumeControl.setMax(volumeObject.getInt("max"));
                volumeControl.setType(volumeObject.getString("type"));

                mPlayerViewModel.getVolumeControlViewModel().setVolume(volumeObject.getInt("value"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
            mPlayerViewModel.ClearPlaybackState();
        }
    }

    private String mLastCatalog = "";
    private Bitmap mLastArtwork = null;

    private void getArtwork() {
        String currentCatalog = mPlayerViewModel.getCatalog().getValue();
        Bitmap currentArtwork = mPlayerViewModel.getArtwork().getValue();
        // monitor also the artwork so that it gets reloaded after a restart of the activity
        if (currentCatalog != null && !currentCatalog.isEmpty() &&
                (!currentCatalog.equals(mLastCatalog) || currentArtwork != mLastArtwork)) {
            mLastCatalog = mPlayerViewModel.getCatalog().getValue();
            new Thread(() -> {
                Bitmap artwork = mConnector.getImage("artwork/" + mCurrentPlaylistId + "/" + mCurrentIndex);
                mActivity.runOnUiThread(() -> {
                    mPlayerViewModel.setArtwork(artwork);
                    mLastArtwork = artwork;
                });
            }).start();
        }
    }

    public void startPlayback() {
        new Thread(() -> {
            mConnector.postData("player/play");
            getPlayerState();
        }).start();
    }

    public void pausePlayback() {
        new Thread(() -> {
            mConnector.postData("player/pause");
            getPlayerState();
        }).start();
    }

    public void previousTrack() {
        new Thread(() -> {
            mConnector.postData("player/previous");
            getPlayerState();
        }).start();
    }

    public void nextTrack() {
        new Thread(() -> mConnector.postData("player/next")).start();
    }

    public void playTrack(String playlistId, String index) {
        new Thread(() -> {
            mConnector.postData("player/play/" + playlistId + "/" + index);
            getPlayerState();
        }).start();
    }

    public void setVolume(Integer value) {
        new Thread(() -> {
            Map<String, Integer> postData = new HashMap<>();
            postData.put("volume", value);
            Gson gson = new Gson();
            String jsonString = gson.toJson(postData);
            mConnector.postData("player/", jsonString);
            getPlayerState();
        }).start();
    }

}

package com.deckerth.thomas.foobarremotecontroller2.connector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;

import com.deckerth.thomas.foobarremotecontroller2.model.PlaybackState;
import com.deckerth.thomas.foobarremotecontroller2.model.Player;
import com.deckerth.thomas.foobarremotecontroller2.model.VolumeControl;

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
        if (INSTANCE == null)
            INSTANCE = new PlayerAccess();
        return INSTANCE;
    }

    private final HTTPConnector mConnector;

    public PlayerAccess() {
        this.mConnector = new HTTPConnector();
    }

//    public void startPlayerObserver() {
//
//        if (mObserverIsRunning) return;
//
//        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
//
//        final Runnable task = () -> {
//            mLastPlayerState = mConnector.getData("player?columns=%25catalog%25,%25composer%25,%25album%25,%25title%25,%25artist%25,%25discnumber%25,%25track%25,%25playback_time%25");
//            mActivity.runOnUiThread(() -> {
//                parsePlayerState(mLastPlayerState);
//                getArtwork();
//                mObserverIsRunning = true;
//            });
//        };
//
//        // Schedule the task to run with an initial delay and then periodically
//        // For example, to run every second after an initial delay of 0 seconds
//        scheduler.scheduleWithFixedDelay(task, 0, 1, TimeUnit.SECONDS);
//    }

    public Player getPlayerState() {
        return parsePlayerState(mConnector.getData("player?columns=%25catalog%25,%25composer%25,%25album%25,%25title%25,%25artist%25,%25discnumber%25,%25track%25,%25playback_time%25"));
    }

    private Player parsePlayerState(String input) {
        try {
            JSONObject contentObject = new JSONObject(input);
            JSONObject playerObject = contentObject.getJSONObject("player");
            JSONObject activeItemObject = playerObject.getJSONObject("activeItem");
            JSONObject volumeObject = playerObject.getJSONObject("volume");
            JSONArray columns = activeItemObject.getJSONArray("columns");
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
            String state = playerObject.getString("playbackState");
            PlaybackState playbackState;
            switch (state) {
                case "playing":
                    playbackState = PlaybackState.PLAYING;
                    break;
                case "paused":
                    playbackState = PlaybackState.PAUSED;
                    break;
                default:
                    playbackState = PlaybackState.STOPPED;
                    break;
            }
            Player player = new Player(
                    columns.getString(0),
                    columns.getString(1),
                    columns.getString(2),
                    columns.getString(3),
                    columns.getString(4),
                    columns.getString(5),
                    columns.getString(6),
                    columns.getString(7),
                    activeItemObject.getString("playlistId"),
                    activeItemObject.getString("index"),
                    activeItemObject.getString("duration"),
                    activeItemObject.getString("position"),
                    mConnector.getServerAddress()+ "artwork/" + activeItemObject.getString("playlistId") + "/" + activeItemObject.getString("index"),
                    playbackState
            );
            return player;
//
//            String column = columns.getString(0);
//            mPlayerViewModel.setCatalog(column);
//            column = columns.getString(1);
//            mPlayerViewModel.setComposer(column);
//            column = columns.getString(2);
//            mPlayerViewModel.setAlbum(column);
//            String title = columns.getString(3);
//            column = columns.getString(4);
//            mPlayerViewModel.setArtist(column);
//            column = columns.getString(5);
//            mPlayerViewModel.setDiscNumber(column);
//            column = columns.getString(6);
//            String track = columns.getString(6);
//            if (!title.equals(track))
//                mPlayerViewModel.setTitle(title);
//            else
//                mPlayerViewModel.setTitle("");
//            mPlayerViewModel.setTrack(column);
//            column = columns.getString(7);
//            mPlayerViewModel.setPlaybackTime(column);
//
//            mPlayerViewModel.setPlaylistId(activeItemObject.getString("playlistId"));
//            mPlayerViewModel.setIndex(activeItemObject.getString("index"));
//            mPlayerViewModel.setDuration(activeItemObject.getString("duration"));
//            mPlayerViewModel.setPosition(activeItemObject.getString("position"));
//
//            mCurrentPlaylistId = activeItemObject.getString("playlistId");
//            mCurrentIndex = activeItemObject.getString("index");
//
//
//
//            if (volumeObject != null) {
//                VolumeControl volumeControl = mPlayerViewModel.getVolumeControlViewModel().getVolumeControl();
//                mPlayerViewModel.getVolumeControlViewModel().setIsMuted(volumeObject.getBoolean("isMuted"));
//
//                volumeControl.setMin(volumeObject.getInt("min"));
//                volumeControl.setMax(volumeObject.getInt("max"));
//                volumeControl.setType(volumeObject.getString("type"));
//
//                mPlayerViewModel.getVolumeControlViewModel().setVolume(volumeObject.getInt("value"));
//            }

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
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

//    public void setVolume(Integer value) {
//        new Thread(() -> {
//            Map<String, Integer> postData = new HashMap<>();
//            postData.put("volume", value);
//            Gson gson = new Gson();
//            String jsonString = gson.toJson(postData);
//            mConnector.postData("player/", jsonString);
//            getPlayerState();
//        }).start();
//    }

}

package com.deckerth.thomas.foobarremotecontroller2.connector;

import com.deckerth.thomas.foobarremotecontroller2.FoobarMediaServiceKt;
import com.deckerth.thomas.foobarremotecontroller2.model.PlaybackMode;
import com.deckerth.thomas.foobarremotecontroller2.model.PlaybackState;
import com.deckerth.thomas.foobarremotecontroller2.model.Player;
import com.deckerth.thomas.foobarremotecontroller2.model.VolumeControl;
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.Instant;

public class PlayerAccess {

    private final AppViewModel vm;

    public PlayerAccess(AppViewModel vm) {
        this.vm = vm;
    }

//    public void startPlayerObserver() {
//
//        if (mObserverIsRunning) return;
//
//        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
//
//        final Runnable task = () -> {
//            mLastPlayerState = vm.connector.getData("player?columns=%25catalog%25,%25composer%25,%25album%25,%25title%25,%25artist%25,%25discnumber%25,%25track%25,%25playback_time%25");
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
        Response response;
        try {
            //response = vm.connector.getData("player?columns=%25label%25,%25catalog%25,%25composer%25,%25album%25,%25title%25,%25artist%25,%25discnumber%25,%25track%25,%25playback_time%25");
            response = vm.connector.getData("player?columns=%25label%25,%25catalog%25,%25composer%25,%25album%25,%25title%25,%25artist%25,%25samplerate%25,%25discnumber%25,%25track%25,%25playback_time%25,%24filename%28%25path%25%29%24");
        } catch (Exception e) {
            vm.errorHandler.logError(ErrorType.NETWORK, ErrorCode.CONNECTION_ERROR, ErrorSource.PLAYER_STATE, e);
            return null;
        }
        return parsePlayerState(response);
    }

    public void setPosition(Float position) {
            String jsonString = "{\"position\":" + position + "}";
            vm.connector.postData("player/", jsonString);
            getPlayerState();
    }

    private Player parsePlayerState(Response input) {
        try {
            JSONObject contentObject = new JSONObject(input.getMessage());
            JSONObject playerObject = contentObject.getJSONObject("player");
            JSONObject activeItemObject = playerObject.getJSONObject("activeItem");
            JSONObject volumeObject = playerObject.getJSONObject("volume");
            JSONArray columns = activeItemObject.getJSONArray("columns");
              /*
                "player": {
                    "activeItem": {
                        "columns": [
                            "DGG",
                            "7353 72726",
                            "Franck, César (1822-1890)",
                            "Klavierquintett f-Moll", !! OPTIONAL may be just ""
                            "1. Molto moderato quasi lento - Allegro",
                            "Khatia Buniatishvili, Klavier / Gidon Kremer & Marija Nemanytė, Violine / Maxim Rysanov, Viola / Giedrė Dirvanauskaitė, Cello",
                            "44100",
                            "01",
                            "01",
                            "?",
                            "02",
                            "0:28"
                            "<filename>"
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
            if (Duration.between(FoobarMediaServiceKt.getLastChanged(), Instant.now()).toMillis() > 500) {
                System.out.println("FOOB Volume set");
                VolumeControl volumeControl = vm.getFoobVolumeControl();
                volumeControl.setMuted(volumeObject.getBoolean("isMuted"));
                volumeControl.setMin(volumeObject.getInt("min"));
                volumeControl.setMax(volumeObject.getInt("max"));
                volumeControl.setType(volumeObject.getString("type"));
                volumeControl.setValue(volumeObject.getInt("value"));
                FoobarMediaServiceKt.volumeProvider.setCurrentVolume(volumeControl.getCurrentValuePercent());
            }

            if (columns.length() > 0) {
                String title = columns.getString(4);
                String filename = columns.getString(10);
                String effectiveTitle = "";
                if (!title.equals(filename)) effectiveTitle = title;
                return new Player(
                        columns.getString(0),
                        columns.getString(1),
                        columns.getString(2),
                        columns.getString(3),
                        effectiveTitle,
                        columns.getString(5),
                        columns.getString(6),
                        columns.getString(7),
                        columns.getString(8),
                        columns.getString(9),
                        activeItemObject.getString("playlistId"),
                        activeItemObject.getString("index"),
                        activeItemObject.getString("duration"),
                        activeItemObject.getString("position"),
                        vm.connector.serverAddress(input.getUsedIpAddress()) + "artwork/" + activeItemObject.getString("playlistId") + "/" + activeItemObject.getString("index"),
                        playbackState,
                        PlaybackMode.getEntries().get(playerObject.getInt("playbackMode")),
                        input.getUsedIpAddress());
            } else
                return new Player(
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        playbackState,
                        PlaybackMode.getEntries().get(playerObject.getInt("playbackMode")),
                        input.getUsedIpAddress());
        } catch (JSONException e) {
            e.printStackTrace();
            vm.errorHandler.logError(ErrorType.API, ErrorCode.BAD_RESPONSE, ErrorSource.PLAYER_STATE, e);
            return null;
        }
    }

    public void startPlayback() {
        new Thread(() -> {
            vm.connector.postData("player/play");
            getPlayerState();
        }).start();
    }

    public void pausePlayback() {
        new Thread(() -> {
            vm.connector.postData("player/pause");
            getPlayerState();
        }).start();
    }

    public void previousTrack() {
        new Thread(() -> {
            vm.connector.postData("player/previous");
            getPlayerState();
        }).start();
    }

    public void nextTrack() {
        new Thread(() -> vm.connector.postData("player/next")).start();
    }

    public void playTrack(String playlistId, Integer index) {
        new Thread(() -> {
            vm.connector.postData("player/play/" + playlistId + "/" + index.toString());
            getPlayerState();
        }).start();
    }

    public void setVolume(Integer value) {
        new Thread(() -> {
            String jsonString = "{\"volume\":" + value + "}";
            vm.connector.postData("player/", jsonString);
            getPlayerState();
        }).start();
    }

    public void setPlaybackMode(PlaybackMode mode) {
        new Thread(() -> {
            String jsonString = "{\"options\":[{\"id\": \"playbackOrder\", \"value\": " + mode.ordinal() + "}]}";
            vm.connector.postData("player/", jsonString);
            getPlayerState();
        }).start();
    }

}

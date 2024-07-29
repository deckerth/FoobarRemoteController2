package com.deckerth.thomas.foobarremotecontroller2.model;

public class PlaylistEntity {

    private final String mPlaylistId;

    private final String mName;

    private final Boolean mIsCurrent;


    public PlaylistEntity(String playlistId, String name, Boolean isCurrent) {
        this.mPlaylistId = playlistId;
        this.mIsCurrent = isCurrent;
        this.mName = name;
    }

    public String getPlaylistId() {
        return  mPlaylistId;
    }

    public Boolean getIsCurrent() {
        return mIsCurrent;
    }

    public String getName() { return mName; }

}

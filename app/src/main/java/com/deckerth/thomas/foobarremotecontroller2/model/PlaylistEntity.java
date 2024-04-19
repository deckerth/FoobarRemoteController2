package com.deckerth.thomas.foobarremotecontroller2.model;

public class PlaylistEntity {

    private final String mPlaylistId;

    private final Boolean mIsCurrent;


    public PlaylistEntity(String playlistId, Boolean isCurrent) {
        this.mPlaylistId = playlistId;
        this.mIsCurrent = isCurrent;
    }

    public String getPlaylistId() {
        return  mPlaylistId;
    }

    public Boolean getIsCurrent() {
        return mIsCurrent;
    }

}

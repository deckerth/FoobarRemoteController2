package com.deckerth.thomas.foobarremotecontroller2.model;

public class PlaylistEntity {

    private final String mPlaylistId;

    private String mName;

    private Boolean mIsCurrent;

    private int mNoOfTracks;


    public PlaylistEntity(String playlistId, String name, Boolean isCurrent, int noOfTracks) {
        this.mPlaylistId = playlistId;
        this.mIsCurrent = isCurrent;
        this.mName = name;
        this.mNoOfTracks = noOfTracks;
    }

    public String getPlaylistId() {
        return mPlaylistId;
    }

    public Boolean getIsCurrent() {
        return mIsCurrent;
    }

    public String getName() {
        return mName;
    }

    public int getNoOfTracks() {
        return mNoOfTracks;
    }

    public void setIsCurrent(Boolean isCurrent) {
        this.mIsCurrent = isCurrent;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setNoOfTracks(int noOfTracks) {
        this.mNoOfTracks = noOfTracks;
    }

}

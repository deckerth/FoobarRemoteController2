package com.deckerth.thomas.foobarremotecontroller2.model;

public class Title implements ITitle {

    private final String mCatalog;
    private final String mPlaylistId;
    private final String mIndex;
    private final String mComposer;
    private final String mAlbum;
    private final String mTitle;
    private final String mArtist;
    private final String mDiscNumber;
    private final String mTrack;
    private final String mPlaybackTime;

    public Title(String mPlaylistId, String mIndex, String mCatalog, String mComposer, String mAlbum, String mTitle, String mArtist, String mDiscNumber, String mTrack, String mPlaybackTime) {
        this.mCatalog = mCatalog;
        this.mPlaylistId = mPlaylistId;
        this.mIndex = mIndex;
        this.mComposer = mComposer;
        this.mAlbum = mAlbum;
        this.mTitle = mTitle;
        this.mArtist = mArtist;
        this.mDiscNumber = mDiscNumber;
        this.mTrack = mTrack;
        this.mPlaybackTime = mPlaybackTime;
    }

    @Override
    public String getCatalog() {
        return null;
    }

    @Override
    public String getPlaylistId() {
        return mPlaylistId;
    }

    @Override
    public String getIndex() {
        return mIndex;
    }

    @Override
    public String getComposer() {
        return mComposer;
    }

    @Override
    public String getAlbum() {
        return null;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getArtist() {
        return mArtist;
    }

    @Override
    public String getDiscNumber() {
        return mDiscNumber;
    }

    @Override
    public String getTrack() {
        return mTrack;
    }

    @Override
    public String getPlaybackTime() {
        return mPlaybackTime;
    }
}

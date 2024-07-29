package com.deckerth.thomas.foobarremotecontroller2.model;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

public class Title implements ITitle {

    // mPlaylistId, mCatalog, mIndex, mComposer, mAlbum, mArtist
    protected final String mCatalog;
    protected final String mPlaylistId;
    protected final String mIndex;
    protected final String mComposer;
    private final String mTitle;
    private final String mDiscNumber;
    private final String mTrack;
    private final String mPlaybackTime;
    private final Double mDuration;
    private final Double mPosition;
    private final String mArtworkUrl;
    protected String mAlbum;
    protected String mArtist;
    protected Bitmap mArtwork;
    private Boolean mIsCurrent = false;

    public Title(String mPlaylistId, String mIndex, String mCatalog, String mComposer, String mAlbum, String mTitle, String mArtist, String mDiscNumber, String mTrack, String mPlaybackTime, String duration, String position, String mArtworkUrl) {
        this.mCatalog = set(mCatalog);
        this.mPlaylistId = set(mPlaylistId);
        this.mIndex = set(mIndex);
        this.mComposer = set(mComposer);
        this.mAlbum = set(mAlbum);
        this.mTitle = set(mTitle);
        this.mArtist = set(mArtist);
        this.mDiscNumber = set(mDiscNumber);
        this.mTrack = set(mTrack);
        this.mPlaybackTime = set(mPlaybackTime);
        this.mArtworkUrl = set(mArtworkUrl);
        double value;
        try {
            value = Double.parseDouble(duration);
        } catch (NumberFormatException e) {
            value = 0.0;
        }
        mDuration = value;
        try {
            value = Double.parseDouble(position);
        } catch (NumberFormatException e) {
            value = 0.0;
        }
        mPosition = value;
    }

    protected String set(String v) {
        if (v == null)
            return "";
        else
            return v;
    }

    @Override
    public String getCatalog() {
        return mCatalog;
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
        return mAlbum;
    }

    @Override
    public void clearArtist() {
        mArtist = "";
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
    public void clearAlbum() {
        mAlbum = "";
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

    @Override
    public Double getDuration() {
        return mDuration;
    }

    @Override
    public Double getPosition() {
        return mPosition;
    }

    @Override
    public Boolean getIsAlbum() {
        return false;
    }

    @Override
    public Bitmap getArtwork() {
        return mArtwork;
    }

    @Override
    public void setArtwork(Bitmap artwork) {
        mArtwork = artwork;
    }

    @Override
    public String getArtworkUrl() {
        return mArtworkUrl;
    }

    @Override
    public Boolean isCurrentTitle() {
        return mIsCurrent;
    }

    @Override
    public void setIsCurrentTitle(Boolean isCurrentTitle) {
        mIsCurrent = isCurrentTitle;
    }

    @NonNull
    @Override
    public ITitle clone() {
        ITitle result = new Title(mPlaylistId, mIndex, mCatalog, mComposer, mAlbum, mTitle, mArtist, mDiscNumber, mTrack, mPlaybackTime, mDuration.toString(), mPosition.toString(), mArtworkUrl);
        result.setArtwork(mArtwork);
        result.setIsCurrentTitle(mIsCurrent);
        return result;
    }
}

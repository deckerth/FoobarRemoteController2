package com.deckerth.thomas.foobarremotecontroller2.model;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.deckerth.thomas.foobarremotecontroller2.viewmodel.TitleFilter;

public class Title implements ITitle {

    // mPlaylistId, mCatalog, mIndex, mComposer, mAlbum, mArtist
    protected String mLabel;
    protected String mCatalog;
    protected String mPlaylistId;
    protected int mIndex;
    protected String mComposer;
    private String mTitle;
    private String mDiscNumber;
    private String mTrack;
    private String mPlaybackTime;
    private Double mDuration;
    private Double mPosition;
    private String mArtworkUrl;
    protected String mAlbum;
    protected String mArtist;
    protected String mAlbumArtist;
    protected String mSampleRate;
    protected String mGenre;
    protected Bitmap mArtwork;
    private Boolean mIsCurrent = false;
    private String mPath = "";
    protected Float elapsedTimeWhenTitleStarts = 0.0f;

    protected CustomFieldsContent mCustomFields;

    public Title(String mPlaylistId, int mIndex, String mLabel, String mCatalog, String mComposer, String mAlbum, String mTitle, String mArtist, String mAlbumArtist, String mSampleRate, String mGenre, String mDiscNumber, String mTrack, String mPlaybackTime, String duration, String position, String mArtworkUrl, String mPath, CustomFieldsContent customFields) {
        init(mPlaylistId, mIndex, mLabel, mCatalog, mComposer, mAlbum, mTitle, mArtist, mAlbumArtist, mSampleRate, mGenre, mDiscNumber, mTrack, mPlaybackTime, duration, position, mArtworkUrl, mPath, customFields);
    }

    public Title(String mPlaylistId, int mIndex, String mLabel, String mCatalog, String mComposer, String mAlbum, String mTitle, String mArtist, String mAlbumArtist, String mSampleRate, String mGenre, String mDiscNumber, String mTrack, String mPlaybackTime, String duration, String position, String mArtworkUrl, String mPath) {
        init(mPlaylistId, mIndex, mLabel, mCatalog, mComposer, mAlbum, mTitle, mArtist, mAlbumArtist, mSampleRate, mGenre, mDiscNumber, mTrack, mPlaybackTime, duration, position, mArtworkUrl, mPath, new CustomFieldsContent());
    }

    private void init(String mPlaylistId, int mIndex, String mLabel, String mCatalog, String mComposer, String mAlbum, String mTitle, String mArtist, String mAlbumArtist, String mSampleRate, String mGenre, String mDiscNumber, String mTrack, String mPlaybackTime, String duration, String position, String mArtworkUrl, String mPath, CustomFieldsContent customFields) {
        this.mLabel = set(mLabel);
        this.mCatalog = set(mCatalog);
        this.mPlaylistId = set(mPlaylistId);
        this.mIndex = mIndex;
        this.mComposer = set(mComposer);
        this.mAlbum = set(mAlbum);
        this.mAlbumArtist = set(mAlbumArtist);
        this.mTitle = set(mTitle);
        this.mArtist = set(mArtist);
        this.mDiscNumber = set(mDiscNumber);
        this.mTrack = set(mTrack);
        this.mPlaybackTime = set(mPlaybackTime);
        this.mArtworkUrl = set(mArtworkUrl);
        this.mSampleRate = set(mSampleRate);
        this.mGenre = set(mGenre);
        this.mPath = set(mPath);
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
        mCustomFields = customFields;
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
    public String getLabel() {
        return mLabel;
    }

    @Override
    public String getPlaylistId() {
        return mPlaylistId;
    }

    @Override
    public int getIndex() {
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
    public String getAlbumArtist() {
        return mAlbumArtist;
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
    public String getSampleRate() {
        return mSampleRate;
    }

    @Override
    public String getGenre() {
        return mGenre;
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
    public Float getElapsedTimeWhenTitleStarts() {
        return elapsedTimeWhenTitleStarts;
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
    public String getPath() {
        return mPath;
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

    @Override
    public void setElapsedTimeWhenTitleStarts(Float elapsedTimeWhenTitleStarts) {
        this.elapsedTimeWhenTitleStarts = elapsedTimeWhenTitleStarts;
    }

    private Boolean matchesExact(String pattern, CustomFields customFields) {
        if (pattern.isEmpty()) return true;
        String upperPattern = pattern.toUpperCase();
        if (mAlbum.toUpperCase().contains(upperPattern)) return true;
        if (mArtist.toUpperCase().contains(upperPattern)) return true;
        if (mAlbumArtist.toUpperCase().contains(upperPattern)) return true;
        if (mTitle.toUpperCase().contains(upperPattern)) return true;
        if (mComposer.toUpperCase().contains(upperPattern)) return true;
        if (mCatalog.toUpperCase().contains(upperPattern)) return true;
        if (mDiscNumber.toUpperCase().contains(upperPattern)) return true;
        return this.getCustomFields().matches(upperPattern);
    }

    @Override
    public Boolean matches(TitleFilter filter, CustomFields customFields) {
        if (!filter.isActive()) return true;
        String[] tokens = filter.getPattern().trim().split("\\s+");
        for (String token : tokens)
            if (!matchesExact(token, customFields)) return false;
        if (filter.getHighRes()) {
            try {
                int bitRate = Integer.parseInt(mSampleRate);
                if (bitRate <= 48000) return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (!filter.getGenre().isBlank())
            return getGenre().equals(filter.getGenre());
        return true;
    }

    @Override
    public CustomFieldsContent getCustomFields() {
        return mCustomFields;
    }

    @NonNull
    @Override
    public ITitle clone() {
        ITitle result = new Title(mPlaylistId, mIndex, mLabel, mCatalog, mComposer, mAlbum, mAlbumArtist, mTitle, mArtist, mSampleRate, mGenre, mDiscNumber, mTrack, mPlaybackTime, mDuration.toString(), mPosition.toString(), mArtworkUrl, mPath, mCustomFields);
        result.setArtwork(mArtwork);
        result.setIsCurrentTitle(mIsCurrent);
        return result;
    }
}

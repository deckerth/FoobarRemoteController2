package com.deckerth.thomas.foobarremotecontroller2.viewmodel;

import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PlayerViewModel extends ViewModel {

    private static PlayerViewModel INSTANCE = null;

    public static PlayerViewModel getInstance() {
        if (INSTANCE == null)
            INSTANCE = new PlayerViewModel();
        return INSTANCE;
    }

    public enum PlaybackState {
        STOPPED,
        PLAYING,
        PAUSED
    }

    private final MutableLiveData<String> mCatalog;
    private final MutableLiveData<String> mComposer;
    private final MutableLiveData<String> mAlbum;
    private final MutableLiveData<String> mTitle;
    private final MutableLiveData<String> mArtist;
    private final MutableLiveData<String> mDiscNumber;
    private final MutableLiveData<String> mTrack;
    private final MutableLiveData<String> mPlaybackTime;
    private final MutableLiveData<PlaybackState> mPlaybackState;
    private final MutableLiveData<Bitmap> mArtwork;
    private final MutableLiveData<String> mIndex;
    private final MutableLiveData<String> mPlaylistId;
    private final MutableLiveData<Integer> mPercentPlayed;
    private final MutableLiveData<String> mLength;

    private Double mDuration = 0.0;
    private Double mPosition = 0.0;

    private PlayerViewModel() {
        mCatalog = new MutableLiveData<>();
        mCatalog.setValue("");

        mComposer = new MutableLiveData<>();
        mComposer.setValue("");

        mAlbum = new MutableLiveData<>();
        mAlbum.setValue("");

        mTitle = new MutableLiveData<>();
        mTitle.setValue("");

        mArtist = new MutableLiveData<>();
        mArtist.setValue("");

        mDiscNumber = new MutableLiveData<>();
        mDiscNumber.setValue("");

        mTrack = new MutableLiveData<>();
        mTrack.setValue("");

        mPlaybackTime = new MutableLiveData<>();
        mPlaybackTime.setValue("--:--");

        mPlaybackState = new MutableLiveData<>();
        mPlaybackState.setValue(PlaybackState.STOPPED);

        mArtwork = new MutableLiveData<>();

        mIndex = new MutableLiveData<>();
        mIndex.setValue("");

        mPlaylistId = new MutableLiveData<>();
        mPlaylistId.setValue("");

        mPercentPlayed = new MutableLiveData<>();
        mPercentPlayed.setValue(0);

        mLength = new MutableLiveData<>();
        mLength.setValue("");
    }

    public LiveData<PlaybackState> getPlaybackState() {return mPlaybackState; };
    public LiveData<String> getCatalog() { return mCatalog; }
    public LiveData<String> getComposer() { return mComposer; }
    public LiveData<String> getAlbum() { return mAlbum; }
    public LiveData<String> getTitle() { return mTitle; }
    public LiveData<String> getArtist() { return mArtist; }
    public LiveData<String> getDiscNumber() { return mDiscNumber; }
    public LiveData<String> getTrack() { return mTrack; }
    public LiveData<String> getPlaybackTime() { return mPlaybackTime; }
    public LiveData<Bitmap> getArtwork() { return  mArtwork; }
    public LiveData<String> getIndex() { return mIndex; }
    public LiveData<String> getPlaylistId() { return mPlaylistId; }
    public LiveData<Integer> getPercentPlayed() {return mPercentPlayed; }
    public LiveData<String> getLength() { return mLength; }

    public void setCatalog(String value) {
        if (value == null)
            mCatalog.setValue("");
        else
            mCatalog.setValue(value);
    }

    public void setComposer(String composer)     {
        if (composer == null)
            mComposer.setValue("");
        else
            mComposer.setValue(composer);
    }

    public void setAlbum(String value)     {
        if (value == null)
            mAlbum.setValue("");
        else
            mAlbum.setValue(value);
    }

    public void setTitle(String value)     {
        if (value == null)
            mTitle.setValue("");
        else
            mTitle.setValue(value);
    }
    public void setArtist(String value)     {
        if (value == null)
            mArtist.setValue("");
        else
            mArtist.setValue(value);
    }
    public void setDiscNumber(String value)     {
        if ((value == null) || (value.equals("?")))
            mDiscNumber.setValue("");
        else
            mDiscNumber.setValue("Disc " + value);
    }
    public void setTrack(String value)     {
        if (value == null)
            mTrack.setValue("");
        else
            mTrack.setValue("Track "+ value);
    }

    public void ClearPlaybackState() {
        mCatalog.setValue("");
        mArtwork.setValue(null);
        mComposer.setValue("");
        mAlbum.setValue("");
        mTitle.setValue("");
        mArtist.setValue("");
        mDiscNumber.setValue("");
        mTrack.setValue("");
        mPlaybackTime.setValue("--:--");
    }

    public void setPlaybackTime(String value)     {
        if ((value == null) || (value.equals("")))
            mPlaybackTime.setValue("--:--");
        else
            mPlaybackTime.setValue(value);
    }

    public void  setPlaybackState(PlaybackState value) {
        mPlaybackState.setValue(value);
    }

    public void setArtwork(Bitmap value) { mArtwork.setValue(value); }

    public void setIndex(String value) {
        mIndex.setValue(value);
        PlaylistViewModel.getInstance().setCurrentTitleIndex(mPlaylistId.getValue(), value);
    }

    public void setDuration(String value) {
        try {
            mDuration = Double.parseDouble(value);
            if (mPosition > 0 && mDuration > 0)
                mPercentPlayed.setValue((int)(mPosition / mDuration * 100));
            if (mDuration > 0) {
                int duration = mDuration.intValue();
                Integer seconds = (int)(duration % 60);
                Integer minutes = (int)(duration / 60);
                String secondsStr;
                if (seconds<9)
                    secondsStr = "0"+seconds.toString();
                else {
                    secondsStr = seconds.toString();
                }
                mLength.setValue(minutes.toString()+":"+secondsStr);
            } else
                mLength.setValue("");
        } catch (NumberFormatException e) {
            mDuration = 0.0;
            mLength.setValue("");
        }
    }

    public void setPosition(String value) {
        try {
            mPosition = Double.parseDouble(value);
            if (mPosition > 0 && mDuration > 0)
                mPercentPlayed.setValue((int)(mPosition / mDuration * 100));
        } catch (NumberFormatException e) {
            mPosition = 0.0;
        }
    }

    public void setPlaylistId(String value) {mPlaylistId.setValue(value);}
}

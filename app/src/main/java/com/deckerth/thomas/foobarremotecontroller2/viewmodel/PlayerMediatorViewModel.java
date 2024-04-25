package com.deckerth.thomas.foobarremotecontroller2.viewmodel;

import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

public class PlayerMediatorViewModel extends ViewModel {

    private final MediatorLiveData<String> mPlaylists;
    private final MediatorLiveData<String> mCatalog;
    private final MediatorLiveData<String> mComposer;
    private final MediatorLiveData<String> mAlbum;
    private final MediatorLiveData<String> mTitle;
    private final MediatorLiveData<String> mArtist;
    private final MediatorLiveData<String> mDiscNumber;
    private final MediatorLiveData<String> mTrack;
    private final MediatorLiveData<String> mPlaybackTime;
    private final MediatorLiveData<PlayerViewModel.PlaybackState> mPlaybackState;
    private final MediatorLiveData<Bitmap> mArtwork;

    public PlayerMediatorViewModel() {
        mPlaylists = new MediatorLiveData<>();
        mPlaylists.setValue("Playlists"); // OBSOLETE!!!!

        mCatalog = new MediatorLiveData<>();
        mCatalog.setValue("");

        mComposer = new MediatorLiveData<>();
        mComposer.setValue("");

        mAlbum = new MediatorLiveData<>();
        mAlbum.setValue("");

        mTitle = new MediatorLiveData<>();
        mTitle.setValue("");

        mArtist = new MediatorLiveData<>();
        mArtist.setValue("");

        mDiscNumber = new MediatorLiveData<>();
        mDiscNumber.setValue("");

        mTrack = new MediatorLiveData<>();
        mTrack.setValue("");

        mPlaybackTime = new MediatorLiveData<>();
        mPlaybackTime.setValue("--:--");

        mPlaybackState = new MediatorLiveData<>();
        mPlaybackState.setValue(PlayerViewModel.PlaybackState.STOPPED);

        mArtwork = new MediatorLiveData<>();

        // observe the changes  from the web api and forward them
        LiveData<String> catalog = PlayerViewModel.getInstance().getCatalog();
        mCatalog.addSource(catalog, mCatalog::setValue);

        LiveData<String> composer = PlayerViewModel.getInstance().getComposer();
        mCatalog.addSource(composer, mComposer::setValue);

        LiveData<String> album = PlayerViewModel.getInstance().getAlbum();
        mAlbum.addSource(album, mAlbum::setValue);

        LiveData<String> title = PlayerViewModel.getInstance().getTitle();
        mTitle.addSource(title, mTitle::setValue);

        LiveData<String> artist = PlayerViewModel.getInstance().getArtist();
        mArtist.addSource(artist, mArtist::setValue);

        LiveData<String> discNumber = PlayerViewModel.getInstance().getDiscNumber();
        mDiscNumber.addSource(discNumber, mDiscNumber::setValue);

        LiveData<String> track = PlayerViewModel.getInstance().getTrack();
        mTrack.addSource(track, mTrack::setValue);

        LiveData<String> playbackTime = PlayerViewModel.getInstance().getPlaybackTime();
        mPlaybackTime.addSource(playbackTime, mPlaybackTime::setValue);

        LiveData<PlayerViewModel.PlaybackState> playbackState = PlayerViewModel.getInstance().getPlaybackState();
        mPlaybackState.addSource(playbackState, mPlaybackState::setValue);

        LiveData<Bitmap> artwork = PlayerViewModel.getInstance().getArtwork();
        mArtwork.addSource(artwork, mArtwork::setValue);
    }

    public LiveData<PlayerViewModel.PlaybackState> getPlaybackState() {return mPlaybackState; };
    public LiveData<String> getText() {
        return mPlaylists;
    }
    public LiveData<String> getCatalog() { return mCatalog; }
    public LiveData<String> getComposer() { return mComposer; }
    public LiveData<String> getAlbum() { return mAlbum; }
    public LiveData<String> getTitle() { return mTitle; }
    public LiveData<String> getArtist() { return mArtist; }
    public LiveData<String> getDiscNumber() { return mDiscNumber; }
    public LiveData<String> getTrack() { return mTrack; }
    public LiveData<String> getPlaybackTime() { return mPlaybackTime; }
    public LiveData<Bitmap>getArtwork() { return  mArtwork; }

}

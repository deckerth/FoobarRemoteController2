package com.deckerth.thomas.foobarremotecontroller2.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.deckerth.thomas.foobarremotecontroller2.model.ITitle;
import com.deckerth.thomas.foobarremotecontroller2.model.PlaylistEntity;

import java.util.List;

public class PlaylistMediatorViewModel extends ViewModel {

    private final MediatorLiveData<List<ITitle>> mPlaylist;

    private final MediatorLiveData<PlaylistEntity> mDisplayedPlaylist;

    private final MediatorLiveData<List<PlaylistEntity>> mPlaylists;

    public PlaylistMediatorViewModel() {
        mPlaylist = new MediatorLiveData<>();
        mPlaylist.setValue(null);

        mDisplayedPlaylist = new MediatorLiveData<>();
        mDisplayedPlaylist.setValue(null);

        mPlaylists = new MediatorLiveData<>();
        mPlaylists.setValue(null);

        PlaylistViewModel viewModel = PlaylistViewModel.getInstance();

        // observe the changes  from the web api and forward them
        assert viewModel != null;
        LiveData<List<ITitle>> playlist = viewModel.getPlaylist();
        mPlaylist.addSource(playlist, mPlaylist::setValue);

        LiveData<PlaylistEntity> displayedPlaylist = viewModel.getDisplayedPlaylist();
        mDisplayedPlaylist.addSource(displayedPlaylist, mDisplayedPlaylist::setValue);

        LiveData<List<PlaylistEntity>> playlists = viewModel.getPlaylists();
        mPlaylists.addSource(playlists, mPlaylists::setValue);
    }

    public LiveData<List<ITitle>> getPlaylist() {
        return mPlaylist;
    }

}

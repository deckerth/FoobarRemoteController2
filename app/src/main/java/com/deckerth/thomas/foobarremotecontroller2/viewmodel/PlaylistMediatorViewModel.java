package com.deckerth.thomas.foobarremotecontroller2.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.deckerth.thomas.foobarremotecontroller2.model.ITitle;
import com.deckerth.thomas.foobarremotecontroller2.model.PlaylistEntity;

import java.util.List;

public class PlaylistMediatorViewModel extends ViewModel {

    private final MediatorLiveData<List<ITitle>> mPlaylist;

    private PlaylistEntity mPlaylistId;

    public PlaylistMediatorViewModel() {
        mPlaylist = new MediatorLiveData<>();
        mPlaylist.setValue(null);

        // observe the changes  from the web api and forward them
        LiveData<List<ITitle>> playlist = PlaylistViewModel.getInstance().getPlaylist();
        mPlaylist.addSource(playlist, mPlaylist::setValue);

    }

    public LiveData<List<ITitle>> getPlaylist() {
        return mPlaylist;
    }

}

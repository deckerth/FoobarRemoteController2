package com.deckerth.thomas.foobarremotecontroller2.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.deckerth.thomas.foobarremotecontroller2.model.ITitle;
import com.deckerth.thomas.foobarremotecontroller2.model.Playlist;
import com.deckerth.thomas.foobarremotecontroller2.model.PlaylistEntity;
import com.deckerth.thomas.foobarremotecontroller2.model.Title;

import java.util.ArrayList;
import java.util.List;

public class PlaylistViewModel extends ViewModel {

    private static PlaylistViewModel INSTANCE;

    public static PlaylistViewModel getInstance() {
        try {
            if (INSTANCE == null)
                INSTANCE = new PlaylistViewModel();
            return INSTANCE;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private final MutableLiveData<List<ITitle>> mPlaylist;

    private PlaylistEntity mPlaylistId;

    public PlaylistViewModel() {

        mPlaylist = new MutableLiveData<>();
//        mPlaylist.setValue(null);

        List<ITitle> init = new ArrayList<ITitle>();
        init.add(new Title("p4", "1", "catalog", "composer", "artist", "title", "artist"
                , "", "01", "10"));
        mPlaylist.setValue(init);

    }

    public LiveData<List<ITitle>> getPlaylist() {
        return mPlaylist;
    }

    public void setPlaylist(Playlist value) {
        try {
/*
            mPlaylist.setValue(value.getPlaylist());
            mPlaylistId = value.getPlaylistEntity();
*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

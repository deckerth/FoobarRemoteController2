package com.deckerth.thomas.foobarremotecontroller2.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.deckerth.thomas.foobarremotecontroller2.connector.PlaylistAccess;
import com.deckerth.thomas.foobarremotecontroller2.model.Album;
import com.deckerth.thomas.foobarremotecontroller2.model.ITitle;
import com.deckerth.thomas.foobarremotecontroller2.model.Playlist;
import com.deckerth.thomas.foobarremotecontroller2.model.PlaylistEntity;
import com.deckerth.thomas.foobarremotecontroller2.model.Title;

import java.util.ArrayList;
import java.util.List;

public class PlaylistViewModel extends ViewModel {

    private static PlaylistViewModel INSTANCE;

    private static final String UNDEFINED = "";
    private String currentTitleIndex = UNDEFINED;
    private String currentPlaylistId = UNDEFINED;

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
        mPlaylist.setValue(null);

/*
        List<ITitle> init = new ArrayList<ITitle>();
        init.add(new Title("p4", "1", "catalog", "composer", "artist", "title", "artist"
                , "", "01", "10"));
        mPlaylist.setValue(init);
*/

    }

    public LiveData<List<ITitle>> getPlaylist() {
        return mPlaylist;
    }

    public void setPlaylist(Playlist value) {
        try {
            List<ITitle> augmentedList = insertAlbumEntries(value.getPlaylist());
            PlaylistAccess.getInstance().getCovers(augmentedList);
            mPlaylist.setValue(augmentedList);
            mPlaylistId = value.getPlaylistEntity();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCovers(List<ITitle> covers) {
        for (ITitle cover : covers) {
            if (cover.getIsAlbum() && cover.getArtwork() != null) {
                cover.setArtwork(cover.getArtwork());
            }
        }
        mPlaylist.setValue(covers);
        if (currentTitleIndex != UNDEFINED)
            setCurrentTitleIndex(currentPlaylistId, currentTitleIndex);
    }

    public void setCurrentTitleIndex(String currentPlaylistId, String currentTitleIndex) {

        this.currentTitleIndex = currentTitleIndex;
        this.currentPlaylistId = currentPlaylistId;

        List<ITitle> currentPlaylist = mPlaylist.getValue();
        if (currentPlaylist != null) {
            List<ITitle> updatedPlaylist = new ArrayList<>();
            Boolean updated = false;
            for(ITitle title : currentPlaylist) {
                if (title.getIsAlbum())
                    updatedPlaylist.add(title);
                else if (title.isCurrentTitle()) {
                    if (title.getPlaylistId().equals(currentPlaylistId) && title.getIndex().equals(currentTitleIndex))
                        return; // nothing has to be done
                    ITitle clonedTitle = title.clone();
                    clonedTitle.setIsCurrentTitle(false);
                    updatedPlaylist.add(clonedTitle);
                    updated = true;
                } else if (title.getPlaylistId().equals(currentPlaylistId) && title.getIndex().equals(currentTitleIndex)) {
                    ITitle clonedTitle = title.clone();
                    clonedTitle.setIsCurrentTitle(true);
                    updatedPlaylist.add(clonedTitle);
                    updated = true;
                } else
                    updatedPlaylist.add(title);
              }

            if (updated)
                mPlaylist.setValue(updatedPlaylist);
        }
    }

    private List<ITitle> insertAlbumEntries(List<ITitle> playlist) {
        String lastCatalog = "";
        String lastAlbum = "";
        String lastArtist = "";
        List<ITitle> result = new ArrayList<>();

        for (ITitle title : playlist) {
            if (!title.getCatalog().equals(lastCatalog) || !title.getAlbum().equals(lastAlbum)) {
                result.add(new Album(title.getPlaylistId(), title.getCatalog(), title.getIndex(), title.getComposer(), title.getAlbum(), title.getArtist()));
                lastCatalog = title.getCatalog();
                lastAlbum = title.getAlbum();
                lastArtist = title.getArtist();
            }
            if (title.getArtist().equals(lastArtist))
                title.clearArtist();
            result.add(title);
        }
        return result;
    }

}

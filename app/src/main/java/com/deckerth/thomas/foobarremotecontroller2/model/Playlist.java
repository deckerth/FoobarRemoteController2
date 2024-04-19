package com.deckerth.thomas.foobarremotecontroller2.model;

import java.util.ArrayList;
import java.util.List;

public class Playlist {

    private final PlaylistEntity mPlaylistEntity;

    private List<ITitle> mTitles = new ArrayList<ITitle>();

    public Playlist(PlaylistEntity mPlaylistEntity) {
        this.mPlaylistEntity = mPlaylistEntity;
    }

    public PlaylistEntity getPlaylistEntity() {
        return mPlaylistEntity;
    }

    public void clear() {
        mTitles.clear();
    }

    public void addTitle(ITitle title) {
        mTitles.add(title);
    }

    public List<ITitle> getPlaylist() {
        return  mTitles;
    }

}

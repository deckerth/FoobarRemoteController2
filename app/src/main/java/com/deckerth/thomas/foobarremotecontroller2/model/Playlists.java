package com.deckerth.thomas.foobarremotecontroller2.model;

import java.util.ArrayList;
import java.util.List;

public class Playlists {

    private List<PlaylistEntity> mPlaylists = new ArrayList<>();

    public void clear() {
        mPlaylists.clear();
    }

    public void addPlaylistEntity(PlaylistEntity playlistEntity) {
        mPlaylists.add(playlistEntity);
    }

    public List<PlaylistEntity> getPlaylists() {
        return  mPlaylists;
    }

    public String getCurrentPlaylist() {
        if (mPlaylists == null)
            return "";
        else {
            PlaylistEntity entity =  mPlaylists.stream().filter(entry -> entry.getIsCurrent()).findFirst().get();
            if (entity == null)
                return "";
            else
                return entity.getPlaylistId();
        }
    }

}

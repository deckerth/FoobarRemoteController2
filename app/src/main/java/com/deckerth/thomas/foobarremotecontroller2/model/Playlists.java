package com.deckerth.thomas.foobarremotecontroller2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Playlists {

    private final List<PlaylistEntity> mPlaylists = new ArrayList<>();

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
            Optional<PlaylistEntity> entity =  mPlaylists.stream().filter(entry -> entry.getIsCurrent()).findFirst();
            if (!entity.isPresent())
                return "";
            else
                return entity.get().getPlaylistId();
        }
    }

}

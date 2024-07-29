package com.deckerth.thomas.foobarremotecontroller2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Playlists {

    private final List<PlaylistEntity> mPlaylists = new ArrayList<>();

    public void addPlaylistEntity(PlaylistEntity playlistEntity) {
        mPlaylists.add(playlistEntity);
    }

    public List<PlaylistEntity> getPlaylists() {
        return  mPlaylists;
    }

    public PlaylistEntity getCurrentPlaylist() {
        Optional<PlaylistEntity> entity = mPlaylists.stream().filter(PlaylistEntity::getIsCurrent).findFirst();
        return entity.orElse(null);
    }

}

package com.deckerth.thomas.foobarremotecontroller2.model;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

public interface ITitle {

    String getCatalog();

    String getPlaylistId();

    String getIndex();

    String getComposer();

    String getAlbum();

    String getTitle();

    String getArtist();

    void clearArtist();

    String getDiscNumber();

    String getTrack();

    String getPlaybackTime();

    Double getDuration();

    Double getPosition();

    Boolean getIsAlbum();

    Bitmap getArtwork();
    String getArtworkUrl();

    void setArtwork(Bitmap artwork);

    Boolean isCurrentTitle();

    void setIsCurrentTitle(Boolean isCurrentTitle);

    @NonNull
    ITitle clone();

    void clearAlbum();
}

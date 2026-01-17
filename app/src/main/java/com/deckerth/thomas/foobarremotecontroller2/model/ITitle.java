package com.deckerth.thomas.foobarremotecontroller2.model;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.deckerth.thomas.foobarremotecontroller2.viewmodel.TitleFilter;

public interface ITitle {

    String getCatalog();

    String getLabel();

    String getPlaylistId();

    int getIndex();

    String getComposer();

    String getAlbum();
    String getAlbumArtist();

    String getTitle();

    String getArtist();

    String getSampleRate();
    String getGenre();

    void clearArtist();

    String getDiscNumber();

    String getTrack();

    String getPlaybackTime();

    Double getDuration();

    Double getPosition();

    Boolean getIsAlbum();

    Bitmap getArtwork();

    String getPath();

    void setArtwork(Bitmap artwork);

    String getArtworkUrl();

    Boolean isCurrentTitle();

    void setIsCurrentTitle(Boolean isCurrentTitle);

    Boolean matches(TitleFilter filter);

    @NonNull
    ITitle clone();

    void clearAlbum();

}

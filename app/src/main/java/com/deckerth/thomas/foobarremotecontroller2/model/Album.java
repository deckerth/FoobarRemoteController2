package com.deckerth.thomas.foobarremotecontroller2.model;

public class Album extends Title {
    public Album(String mPlaylistId, String mCatalog, String mIndex, String mComposer, String mAlbum, String mArtist) {
        super(mPlaylistId, mIndex, mCatalog, mComposer, mAlbum, "", mArtist, "", "", "", "", "");
    }

    @Override
    public Boolean getIsAlbum() {
        return true;
    }

    @Override
    public ITitle clone() {
        ITitle result = new Album(mPlaylistId, mCatalog, mIndex, mComposer, mAlbum, mArtist);
        result.setArtwork(mArtwork);
        return result;

    }
}

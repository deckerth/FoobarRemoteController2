package com.deckerth.thomas.foobarremotecontroller2.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deckerth.thomas.foobarremotecontroller2.connector.PlaylistAccess;
import com.deckerth.thomas.foobarremotecontroller2.databinding.PlaylistListContentBinding;
import com.deckerth.thomas.foobarremotecontroller2.model.PlaylistEntity;
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlaylistViewModel;

import java.util.List;
import java.util.Objects;

public class PlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<? extends PlaylistEntity> mPlaylists;

    private final PopupWindow mWindow;

    public PlaylistAdapter(PopupWindow window) {
        this.mWindow = window;
    }

    public void setPlaylists(final List<? extends PlaylistEntity> playlists) {
        mPlaylists = playlists;
        if (playlists != null)
            notifyItemRangeInserted(0, playlists.size());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PlaylistListContentBinding binding = PlaylistListContentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PlaylistViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PlaylistEntity entity = mPlaylists.get(position);
        PlaylistEntity displayedPlaylist = Objects.requireNonNull(PlaylistViewModel.getInstance()).getDisplayedPlaylist().getValue();

        PlaylistViewHolder playlistHolder = (PlaylistViewHolder) holder;
        playlistHolder.mBinding.setPlaylist(entity);
        if (displayedPlaylist != null && displayedPlaylist.getPlaylistId().equals(entity.getPlaylistId()))
            playlistHolder.mBinding.marker.setVisibility(View.VISIBLE);
        else
            playlistHolder.mBinding.marker.setVisibility(View.GONE);
        holder.itemView.setTag(mPlaylists.get(position));
        holder.itemView.setOnClickListener(itemView -> {
            PlaylistEntity selected = (PlaylistEntity) itemView.getTag();
            PlaylistAccess.getInstance().getPlaylist(selected);
            mWindow.dismiss();
        });
    }

    @Override
    public int getItemCount() {
        if (mPlaylists == null)
            return 0;
        else
            return mPlaylists.size();
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {

        final PlaylistListContentBinding mBinding;

        public PlaylistViewHolder(PlaylistListContentBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
    }

}


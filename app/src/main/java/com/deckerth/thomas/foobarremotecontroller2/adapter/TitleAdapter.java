package com.deckerth.thomas.foobarremotecontroller2.adapter;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.deckerth.thomas.foobarremotecontroller2.R;
import com.deckerth.thomas.foobarremotecontroller2.TitleDetailFragment;
import com.deckerth.thomas.foobarremotecontroller2.connector.PlayerAccess;
import com.deckerth.thomas.foobarremotecontroller2.controls.Rectangle;
import com.deckerth.thomas.foobarremotecontroller2.databinding.AlbumHeaderBinding;
import com.deckerth.thomas.foobarremotecontroller2.databinding.TitleListContentBinding;
import com.deckerth.thomas.foobarremotecontroller2.model.ITitle;

import java.util.List;

public class TitleAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ALBUM = 1;
    private static final int TYPE_TRACK = 2;

    List<? extends ITitle> mPlaylist;
    private final View mItemDetailFragmentContainer;

    private Boolean compare(String a, String b) {
        if (a == null || b == null)
            return true;
        else if (a != null && b != null && a.equals(b))
            return true;
        else
            return false;
    }

    public void setPlaylist(final List<? extends ITitle> playlist) {
        if (mPlaylist == null) {
            mPlaylist = playlist;
            if (playlist != null)
                notifyItemRangeInserted(0, playlist.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mPlaylist.size();
                }

                @Override
                public int getNewListSize() {
                    return playlist.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    ITitle oldTitle = mPlaylist.get(oldItemPosition);
                    ITitle newTitle = playlist.get(newItemPosition);
                    return (compare(oldTitle.getComposer(), newTitle.getComposer()) &&
                            compare(oldTitle.getAlbum(), newTitle.getAlbum()) &&
                            compare(oldTitle.getTitle(), newTitle.getTitle()) &&
                            compare(oldTitle.getArtist(), newTitle.getArtist()));
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    ITitle oldTitle = mPlaylist.get(oldItemPosition);
                    ITitle newTitle = playlist.get(newItemPosition);
                    Boolean same = areItemsTheSame(oldItemPosition, newItemPosition) && (
                            (oldTitle.getArtwork() == null && newTitle.getArtwork() == null) ||
                                    (oldTitle.getArtwork() != null && newTitle.getArtwork() != null)) &&
                            oldTitle.isCurrentTitle() == newTitle.isCurrentTitle();
                    if (same)
                        return true;
                    else
                        return false;
                }
            });
            mPlaylist = playlist;
            result.dispatchUpdatesTo(this);
        }
    }

    public TitleAdapter(View itemDetailFragmentContainer) {
        mItemDetailFragmentContainer = itemDetailFragmentContainer;
    }

    @Override
    public int getItemViewType(int position) {
        if (mPlaylist.get(position).getIsAlbum())
            return TYPE_ALBUM;
        else
            return TYPE_TRACK;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
/*
        TitleListContentBinding binding;
        if (viewType == TYPE_ALBUM)
            binding = DataBindingUtil
                    .inflate(LayoutInflater.from(parent.getContext()), R.layout.title_list_content,
                            parent, false);
        else
            binding = DataBindingUtil
                    .inflate(LayoutInflater.from(parent.getContext()), R.layout.title_list_content,
                            parent, false);
*/

        if (viewType == TYPE_TRACK) {
            TitleListContentBinding titleBinding =
                    TitleListContentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new TitleAdapter.TitleViewHolder(titleBinding);
        } else {
            AlbumHeaderBinding albumBinding =
                    AlbumHeaderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new TitleAdapter.AlbumViewHolder(albumBinding);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        int viewType = getItemViewType(position);

        ITitle title = mPlaylist.get(position);

        switch (viewType) {
            case TYPE_ALBUM:
                AlbumViewHolder albumHolder = (AlbumViewHolder) holder;
                albumHolder.mBinding.setTitle(title);
                if (title.getArtwork() != null)
                    albumHolder.mArtwork.setImageBitmap(title.getArtwork());
                break;
            case TYPE_TRACK:
                TitleViewHolder titleHolder = (TitleViewHolder) holder;
                titleHolder.mBinding.setTitle(title);
                if (title.isCurrentTitle())
                    titleHolder.mMarker.setVisibility(View.VISIBLE);
                else
                    titleHolder.mMarker.setVisibility(View.GONE);
                break;
        }

        holder.itemView.setTag(mPlaylist.get(position));
        holder.itemView.setOnClickListener(itemView -> {
            ITitle item = (ITitle) itemView.getTag();
            if (!item.getIsAlbum())
                PlayerAccess.getInstance().PlayTrack(item.getPlaylistId(), item.getIndex());
/*
            Bundle arguments = new Bundle();
            arguments.putString(TitleDetailFragment.ARG_ITEM_ID, item.getIndex());
            if (mItemDetailFragmentContainer != null) {
                Navigation.findNavController(mItemDetailFragmentContainer)
                        .navigate(R.id.fragment_title_detail, arguments);
            } else {
                Navigation.findNavController(itemView).navigate(R.id.show_title_detail, arguments);
            }
*/
        });
    }

    @Override
    public int getItemCount() {
        if (mPlaylist == null)
            return 0;
        else
            return mPlaylist.size();
    }

    class TitleViewHolder extends RecyclerView.ViewHolder {
        final TitleListContentBinding mBinding;
        final Rectangle mMarker;

        TitleViewHolder(TitleListContentBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mMarker = binding.marker;
        }
    }

    class AlbumViewHolder extends RecyclerView.ViewHolder {
        final AlbumHeaderBinding mBinding;
        final ImageView mArtwork;

        AlbumViewHolder(AlbumHeaderBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mArtwork = binding.artwork;
        }

    }

}

package com.deckerth.thomas.foobarremotecontroller2.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.deckerth.thomas.foobarremotecontroller2.connector.PlayerAccess;
import com.deckerth.thomas.foobarremotecontroller2.controls.Rectangle;
import com.deckerth.thomas.foobarremotecontroller2.databinding.AlbumHeaderClassicalBinding;
import com.deckerth.thomas.foobarremotecontroller2.databinding.AlbumHeaderPopBinding;
import com.deckerth.thomas.foobarremotecontroller2.databinding.TitleListContentBinding;
import com.deckerth.thomas.foobarremotecontroller2.model.ITitle;

import java.util.List;

public class TitleAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ALBUM = 1;
    private static final int TYPE_TRACK = 2;

    List<? extends ITitle> mPlaylist;

    private final Boolean mClassicalLayout;

    private Boolean compare(String a, String b) {
        return (a == null && b == null) || (a != null && a.equals(b));
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
                    return areItemsTheSame(oldItemPosition, newItemPosition) && (
                            (oldTitle.getArtwork() == null && newTitle.getArtwork() == null) ||
                                    (oldTitle.getArtwork() != null && newTitle.getArtwork() != null)) &&
                            oldTitle.isCurrentTitle() == newTitle.isCurrentTitle();
                }
            });
            mPlaylist = playlist;
            result.dispatchUpdatesTo(this);
        }
    }

    public TitleAdapter(Boolean isClassicalLayout) {
        mClassicalLayout = isClassicalLayout;
    }

    @Override
    public int getItemViewType(int position) {
        if (mPlaylist.get(position).getIsAlbum())
            return TYPE_ALBUM;
        else
            return TYPE_TRACK;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_TRACK) {
            TitleListContentBinding titleBinding =
                    TitleListContentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new TitleViewHolder(titleBinding);
        } else {
            if (mClassicalLayout) {
                AlbumHeaderClassicalBinding albumBinding =
                        AlbumHeaderClassicalBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new ClassicalAlbumViewHolder(albumBinding);
            } else {
                AlbumHeaderPopBinding albumBinding =
                        AlbumHeaderPopBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new PopAlbumViewHolder(albumBinding);
            }

        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        int viewType = getItemViewType(position);

        ITitle title = mPlaylist.get(position);

        switch (viewType) {
            case TYPE_ALBUM:
                if (mClassicalLayout) {
                    ClassicalAlbumViewHolder albumHolder = (ClassicalAlbumViewHolder) holder;
                    albumHolder.mBinding.setTitle(title);
                    if (title.getArtwork() != null)
                        albumHolder.mArtwork.setImageBitmap(title.getArtwork());
                } else {
                    PopAlbumViewHolder albumHolder = (PopAlbumViewHolder) holder;
                    albumHolder.mBinding.setTitle(title);
                    if (title.getArtwork() != null)
                        albumHolder.mArtwork.setImageBitmap(title.getArtwork());
                }
                break;
            case TYPE_TRACK:
                TitleViewHolder titleHolder = (TitleViewHolder) holder;
                titleHolder.mBinding.setTitle(title);
                if (title.isCurrentTitle())
                    titleHolder.mMarker.setVisibility(View.VISIBLE);
                else
                    titleHolder.mMarker.setVisibility(View.GONE);
        }

        holder.itemView.setTag(mPlaylist.get(position));
        holder.itemView.setOnClickListener(itemView -> {
            ITitle item = (ITitle) itemView.getTag();
            if (!item.getIsAlbum())
                PlayerAccess.getInstance().PlayTrack(item.getPlaylistId(), item.getIndex());
        });
    }

    @Override
    public int getItemCount() {
        if (mPlaylist == null)
            return 0;
        else
            return mPlaylist.size();
    }

    static class TitleViewHolder extends RecyclerView.ViewHolder {
        final TitleListContentBinding mBinding;
        final Rectangle mMarker;

        TitleViewHolder(TitleListContentBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mMarker = binding.marker;
        }
    }

    static class ClassicalAlbumViewHolder extends RecyclerView.ViewHolder {
        final AlbumHeaderClassicalBinding mBinding;
        final ImageView mArtwork;

        ClassicalAlbumViewHolder(AlbumHeaderClassicalBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mArtwork = binding.artwork;
        }
    }

    static class PopAlbumViewHolder extends RecyclerView.ViewHolder {
        final AlbumHeaderPopBinding mBinding;
        final ImageView mArtwork;

        PopAlbumViewHolder(AlbumHeaderPopBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mArtwork = binding.artwork;
        }
    }

}

package com.deckerth.thomas.foobarremotecontroller2;

import android.content.ClipData;
import android.content.ClipDescription;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.deckerth.thomas.foobarremotecontroller2.databinding.FragmentTitleListBinding;
import com.deckerth.thomas.foobarremotecontroller2.databinding.TitleListContentBinding;

import com.deckerth.thomas.foobarremotecontroller2.model.ITitle;
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlaylistMediatorViewModel;

import java.util.List;

/**
 * A fragment representing a list of Playlist. This fragment
 * has different presentations for handset and larger screen devices. On
 * handsets, the fragment presents a list of items, which when touched,
 * lead to a {@link TitleDetailFragment} representing
 * item details. On larger screens, the Navigation controller presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class TitleListFragment extends Fragment {

    private FragmentTitleListBinding mBinding;

    private PlaylistMediatorViewModel mViewModel;
    private SimpleItemRecyclerViewAdapter mTitleAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mBinding = FragmentTitleListBinding.inflate(inflater, container, false);
        return mBinding.getRoot();

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = mBinding.titleList;

        // Leaving this not using view binding as it relies on if the view is visible the current
        // layout configuration (layout, layout-sw600dp)
        View itemDetailFragmentContainer = view.findViewById(R.id.title_detail_nav_container);

        mViewModel = new ViewModelProvider(this).get(PlaylistMediatorViewModel.class);

        setupRecyclerView(recyclerView, itemDetailFragmentContainer);
    }

    private void setupRecyclerView(
            RecyclerView recyclerView,
            View itemDetailFragmentContainer
    ) {

        mTitleAdapter = new SimpleItemRecyclerViewAdapter( itemDetailFragmentContainer );
        recyclerView.setAdapter(mTitleAdapter);

        mViewModel.getPlaylist().observe(getViewLifecycleOwner(), new Observer<List<ITitle>>() {
            @Override
            public void onChanged(List<ITitle> titles) {
                if (titles != null) mTitleAdapter.setPlaylist(titles);
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        List<? extends ITitle> mPlaylist;
        private final View mItemDetailFragmentContainer;

        public void setPlaylist(final List<? extends ITitle> playlist) {
            if (mPlaylist == null) {
                mPlaylist = playlist;
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
                        return ( mPlaylist.get(oldItemPosition).getComposer().contentEquals(playlist.get(newItemPosition).getComposer()) &&
                                mPlaylist.get(oldItemPosition).getAlbum().contentEquals(playlist.get(newItemPosition).getAlbum()) &&
                                mPlaylist.get(oldItemPosition).getTitle().contentEquals(playlist.get(newItemPosition).getTitle()) &&
                                mPlaylist.get(oldItemPosition).getArtist().contentEquals(playlist.get(newItemPosition).getArtist()) );
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        return areItemsTheSame(oldItemPosition, newItemPosition);
                    }
                });
                mPlaylist = playlist;
                result.dispatchUpdatesTo(this);
            }
        }

        SimpleItemRecyclerViewAdapter(View itemDetailFragmentContainer) {
            mItemDetailFragmentContainer = itemDetailFragmentContainer;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            TitleListContentBinding binding =
                    TitleListContentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);

        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mIdView.setText(mPlaylist.get(position).getAlbum());
            holder.mContentView.setText(mPlaylist.get(position).getTitle());

            holder.itemView.setTag(mPlaylist.get(position));
            holder.itemView.setOnClickListener(itemView -> {
                ITitle item = (ITitle) itemView.getTag();
                Bundle arguments = new Bundle();
                arguments.putString(TitleDetailFragment.ARG_ITEM_ID, item.getIndex());
                if (mItemDetailFragmentContainer != null) {
                    Navigation.findNavController(mItemDetailFragmentContainer)
                            .navigate(R.id.fragment_title_detail, arguments);
                } else {
                    Navigation.findNavController(itemView).navigate(R.id.show_title_detail, arguments);
                }
            });
        }

        @Override
        public int getItemCount() {
            if (mPlaylist == null)
                return 0;
            else
                return mPlaylist.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(TitleListContentBinding binding) {
                super(binding.getRoot());
                mIdView = binding.idText;
                mContentView = binding.content;
            }

        }
    }
}
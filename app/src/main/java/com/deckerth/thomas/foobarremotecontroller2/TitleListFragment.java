package com.deckerth.thomas.foobarremotecontroller2;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.deckerth.thomas.foobarremotecontroller2.adapter.TitleAdapter;
import com.deckerth.thomas.foobarremotecontroller2.connector.PlayerAccess;
import com.deckerth.thomas.foobarremotecontroller2.connector.PlaylistAccess;
import com.deckerth.thomas.foobarremotecontroller2.databinding.FragmentTitleListBinding;

import com.deckerth.thomas.foobarremotecontroller2.databinding.StubTitleListTitleClassicalBinding;
import com.deckerth.thomas.foobarremotecontroller2.model.ITitle;
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlayerMediatorViewModel;
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlayerViewModel;
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

    private PlayerViewModel mPlayerViewModel;

    private PlayerAccess mPlayerAccess;

    private TitleAdapter mTitleAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mBinding = FragmentTitleListBinding.inflate(inflater, container, false);
        return mBinding.getRoot();

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mViewModel == null)
            mViewModel = new ViewModelProvider(this).get(PlaylistMediatorViewModel.class);

        RecyclerView recyclerView = mBinding.titleList;
        View itemDetailFragmentContainer = view.findViewById(R.id.title_detail_nav_container);

        setupRecyclerView(recyclerView, itemDetailFragmentContainer);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
/*
        if (!prefs.getBoolean("show_composer", false))
            mBinding.composer.setVisibility(View.GONE);
*/

        mPlayerViewModel = PlayerViewModel.getInstance();
        mPlayerViewModel.getArtwork().observe(getViewLifecycleOwner(), mBinding.artwork::setImageBitmap);
        mPlayerViewModel.getComposer().observe(getViewLifecycleOwner(), mBinding.composer::setText);
        mPlayerViewModel.getAlbum().observe(getViewLifecycleOwner(), mBinding.album::setText);
        mPlayerViewModel.getTitle().observe(getViewLifecycleOwner(), mBinding.title::setText);
        mPlayerViewModel.getArtist().observe(getViewLifecycleOwner(), mBinding.artist::setText);
        mPlayerViewModel.getDiscNumber().observe(getViewLifecycleOwner(), mBinding.discNumber::setText);
        mPlayerViewModel.getTrack().observe(getViewLifecycleOwner(), mBinding.track::setText);
        mPlayerViewModel.getPlaybackTime().observe(getViewLifecycleOwner(), mBinding.playbackTime::setText);
        mPlayerViewModel.getAlbum().observe(getViewLifecycleOwner(), mBinding.album::setText);
        mPlayerViewModel.getPercentPlayed().observe(getViewLifecycleOwner(), mBinding.playbackProgress::setProgress);

        mPlayerViewModel.getPlaybackState().observe(getViewLifecycleOwner(), new Observer<PlayerViewModel.PlaybackState>() {
            @Override
            public void onChanged(PlayerViewModel.PlaybackState state) {
                switch (state) {
                    case STOPPED:
                        //stopButton.setEnabled(false);
                        mBinding.play.setImageDrawable(getContext().getDrawable(android.R.drawable.ic_media_play));
                    case PAUSED:
                        mBinding.play.setImageDrawable(getContext().getDrawable(android.R.drawable.ic_media_play));
                        break;
                    case PLAYING:
                        mBinding.play.setImageDrawable(getContext().getDrawable(android.R.drawable.ic_media_pause));
                        break;
                }
            }
        });

        mPlayerAccess = PlayerAccess.getInstance(getActivity());
        mBinding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayerAccess.PreviousTrack();
            }
        });

        mBinding.next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayerAccess.NextTrack();
            }
        });

        mBinding.play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mPlayerViewModel.getPlaybackState().getValue()) {
                    case STOPPED:
                    case PAUSED:
                        mPlayerAccess.StartPlayback();
                        break;
                    case PLAYING:
                        mPlayerAccess.PausePlayback();
                        break;
                }
            }
        });

        mBinding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                PlaylistAccess.getInstance().getCurrentPlaylist(getActivity());
            }
        });

        mBinding.nowPlaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mPlayerViewModel.getCatalog().getValue().isEmpty()) {
                    Navigation.findNavController(v).navigate(R.id.show_title_detail);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void setupRecyclerView(
            RecyclerView recyclerView,
            View itemDetailFragmentContainer
    ) {

        mTitleAdapter = new TitleAdapter(itemDetailFragmentContainer);
        recyclerView.setAdapter(mTitleAdapter);

        if (mViewModel.getPlaylist().getValue() != null)
            mTitleAdapter.setPlaylist(mViewModel.getPlaylist().getValue());

        mViewModel.getPlaylist().observe(getViewLifecycleOwner(), new Observer<List<ITitle>>() {
            @Override
            public void onChanged(List<ITitle> titles) {
                if (titles != null) mTitleAdapter.setPlaylist(titles);
                mBinding.swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
package com.deckerth.thomas.foobarremotecontroller2;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deckerth.thomas.foobarremotecontroller2.adapter.PlaylistAdapter;
import com.deckerth.thomas.foobarremotecontroller2.adapter.TitleAdapter;
import com.deckerth.thomas.foobarremotecontroller2.connector.PlaylistAccess;
import com.deckerth.thomas.foobarremotecontroller2.databinding.FragmentTitleListBinding;
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlayerViewModel;
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlaylistMediatorViewModel;
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlaylistViewModel;

import java.util.Objects;

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

    private PlaylistMediatorViewModel mPlaylistViewModel;

    private PlayerViewModel mPlayerViewModel;

    private TitleAdapter mTitleAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mBinding = FragmentTitleListBinding.inflate(inflater, container, false);
        return mBinding.getRoot();

    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity().getBaseContext());
        if (prefs.getString("title_layout", "contemporary").equals("classical"))
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.now_playing, new TitleListClassicalTitleFragment())
                    .commit();
        else
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.now_playing, new TitleListPopTitleFragment())
                    .commit();

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mPlaylistViewModel == null)
            mPlaylistViewModel = new ViewModelProvider(this).get(PlaylistMediatorViewModel.class);

        RecyclerView recyclerView = mBinding.titleList;

        setupRecyclerView(recyclerView);

        mPlayerViewModel = PlayerViewModel.getInstance();

        Objects.requireNonNull(mBinding.swipeRefreshLayout).setOnRefreshListener(() -> PlaylistAccess.getInstance().getCurrentPlaylist(getActivity()));

        Objects.requireNonNull(PlaylistViewModel.getInstance()).getDisplayedPlaylist().observe(getViewLifecycleOwner(), playlist -> {
            if (playlist != null)
                Objects.requireNonNull(mBinding.playlistName).setText(playlist.getName());
        });

        Objects.requireNonNull(mBinding.choosePlaylist).setOnClickListener(this::showPlaylistsPopup);

        Objects.requireNonNull(mBinding.nowPlaying).setOnClickListener(v -> {
            if (!Objects.requireNonNull(mPlayerViewModel.getCatalog().getValue()).isEmpty()) {
                Navigation.findNavController(v).navigate(R.id.show_title_detail);
            }
        });
    }

    private void showPlaylistsPopup(View anchorView) {
        @SuppressLint("InflateParams") View popupView = getLayoutInflater().inflate(R.layout.playlist_chooser, null);
        PopupWindow popupWindow = new PopupWindow(popupView,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                true); // Focusable

        RecyclerView playlistsView = popupView.findViewById(R.id.playlists);

        PlaylistAdapter playlistAdapter = new PlaylistAdapter(popupWindow);
        playlistAdapter.setPlaylists(Objects.requireNonNull(PlaylistViewModel.getInstance()).getPlaylists().getValue());

        playlistsView.setAdapter(playlistAdapter);

        // Show the popup anchored to the button
        popupWindow.showAsDropDown(anchorView);
    }

    private void setupRecyclerView( RecyclerView recyclerView ) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        mTitleAdapter = new TitleAdapter(prefs.getString("title_layout", "classical").equals("classical"));
        recyclerView.setAdapter(mTitleAdapter);

        if (mPlaylistViewModel.getPlaylist().getValue() != null)
            mTitleAdapter.setPlaylist(mPlaylistViewModel.getPlaylist().getValue());

        mPlaylistViewModel.getPlaylist().observe(getViewLifecycleOwner(), titles -> {
            if (titles != null) mTitleAdapter.setPlaylist(titles);
            Objects.requireNonNull(mBinding.swipeRefreshLayout).setRefreshing(false);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}
package com.deckerth.thomas.foobarremotecontroller2;

import static androidx.core.content.ContextCompat.getDrawable;

import android.content.ClipData;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.DragEvent;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.deckerth.thomas.foobarremotecontroller2.connector.PlayerAccess;
import com.deckerth.thomas.foobarremotecontroller2.databinding.FragmentTitleListBinding;
import com.deckerth.thomas.foobarremotecontroller2.model.ITitle;
import com.deckerth.thomas.foobarremotecontroller2.model.Playlist;
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlayerViewModel;
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlaylistViewModel;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.deckerth.thomas.foobarremotecontroller2.databinding.FragmentTitleDetailBinding;

import java.util.List;

/**
 * A fragment representing a single Title detail screen.
 * This fragment is either contained in a {@link TitleListFragment}
 * in two-pane mode (on larger screen devices) or self-contained
 * on handsets.
 */
public class TitleDetailFragment extends Fragment {

    private FragmentTitleDetailBinding mBinding;

    private PlayerViewModel mPlayerViewModel;

    private PlayerAccess mPlayerAccess;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TitleDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentTitleDetailBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPlayerViewModel = PlayerViewModel.getInstance();
        mPlayerViewModel.getArtwork().observe(getViewLifecycleOwner(), mBinding.artwork::setImageBitmap);
        mPlayerViewModel.getComposer().observe(getViewLifecycleOwner(), mBinding.composer::setText);
        mPlayerViewModel.getAlbum().observe(getViewLifecycleOwner(), mBinding.album::setText);
        mPlayerViewModel.getTitle().observe(getViewLifecycleOwner(), mBinding.title::setText);
        mPlayerViewModel.getArtist().observe(getViewLifecycleOwner(), mBinding.artist::setText);
        mPlayerViewModel.getDiscNumber().observe(getViewLifecycleOwner(), mBinding.discNumber::setText);
        mPlayerViewModel.getTrack().observe(getViewLifecycleOwner(), mBinding.track::setText);
        mPlayerViewModel.getPlaybackTime().observe(getViewLifecycleOwner(), mBinding.playbackTime::setText);
        mPlayerViewModel.getLength().observe(getViewLifecycleOwner(), mBinding.length::setText);
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

    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_title_detail);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

}
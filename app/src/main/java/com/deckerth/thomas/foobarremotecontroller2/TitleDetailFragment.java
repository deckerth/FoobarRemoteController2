package com.deckerth.thomas.foobarremotecontroller2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.deckerth.thomas.foobarremotecontroller2.connector.PlayerAccess;
import com.deckerth.thomas.foobarremotecontroller2.databinding.FragmentTitleDetailBinding;
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlayerViewModel;

import java.util.Objects;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentTitleDetailBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPlayerViewModel = PlayerViewModel.getInstance();
        mPlayerViewModel.getArtwork().observe(getViewLifecycleOwner(), Objects.requireNonNull(mBinding.artwork)::setImageBitmap);
        mPlayerViewModel.getDiscNumber().observe(getViewLifecycleOwner(), Objects.requireNonNull(mBinding.discNumber)::setText);
        mPlayerViewModel.getTrack().observe(getViewLifecycleOwner(), Objects.requireNonNull(mBinding.track)::setText);
        mPlayerViewModel.getPlaybackTime().observe(getViewLifecycleOwner(), Objects.requireNonNull(mBinding.playbackTime)::setText);
        mPlayerViewModel.getLength().observe(getViewLifecycleOwner(), Objects.requireNonNull(mBinding.length)::setText);
        mPlayerViewModel.getPercentPlayed().observe(getViewLifecycleOwner(), Objects.requireNonNull(mBinding.playbackProgress)::setProgress);

        mPlayerViewModel.getPlaybackState().observe(getViewLifecycleOwner(), state -> {
            switch (state) {
                case STOPPED:
                    //stopButton.setEnabled(false);
                    Objects.requireNonNull(mBinding.play).setImageDrawable(requireContext().getDrawable(android.R.drawable.ic_media_play));
                case PAUSED:
                    Objects.requireNonNull(mBinding.play).setImageDrawable(requireContext().getDrawable(android.R.drawable.ic_media_play));
                    break;
                case PLAYING:
                    Objects.requireNonNull(mBinding.play).setImageDrawable(requireContext().getDrawable(android.R.drawable.ic_media_pause));
                    break;
            }
        });

        mPlayerAccess = PlayerAccess.getInstance(getActivity());
        Objects.requireNonNull(mBinding.back).setOnClickListener(v -> mPlayerAccess.previousTrack());

        Objects.requireNonNull(mBinding.next).setOnClickListener(v -> mPlayerAccess.nextTrack());

        Objects.requireNonNull(mBinding.play).setOnClickListener(v -> {
            switch (Objects.requireNonNull(mPlayerViewModel.getPlaybackState().getValue())) {
                case STOPPED:
                case PAUSED:
                    mPlayerAccess.startPlayback();
                    break;
                case PLAYING:
                    mPlayerAccess.pausePlayback();
                    break;
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity().getBaseContext());
        if (prefs.getString("title_layout", "contemporary").equals("classical"))
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.detail_holder, new TitleDetailClassicalFragment())
                    .commit();
        else
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.detail_holder, new TitleDetailPopFragment())
                    .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().setTitle(R.string.title_title_detail);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

}
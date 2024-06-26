package com.deckerth.thomas.foobarremotecontroller2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.deckerth.thomas.foobarremotecontroller2.connector.PlayerAccess;
import com.deckerth.thomas.foobarremotecontroller2.databinding.FragmentTitleListTitleClassicalBinding;
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlayerViewModel;

import java.util.Objects;

public class TitleListClassicalTitleFragment extends Fragment {

    private FragmentTitleListTitleClassicalBinding mBinding;
    private PlayerViewModel mPlayerViewModel;
    private PlayerAccess mPlayerAccess;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mBinding = FragmentTitleListTitleClassicalBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
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
        mPlayerViewModel.getAlbum().observe(getViewLifecycleOwner(), mBinding.album::setText);
        mPlayerViewModel.getPercentPlayed().observe(getViewLifecycleOwner(), mBinding.playbackProgress::setProgress);

        mPlayerViewModel.getPlaybackState().observe(getViewLifecycleOwner(), state -> {
            switch (state) {
                case STOPPED:
                    //stopButton.setEnabled(false);
                    mBinding.play.setImageDrawable(requireContext().getDrawable(android.R.drawable.ic_media_play));
                case PAUSED:
                    mBinding.play.setImageDrawable(requireContext().getDrawable(android.R.drawable.ic_media_play));
                    break;
                case PLAYING:
                    mBinding.play.setImageDrawable(requireContext().getDrawable(android.R.drawable.ic_media_pause));
                    break;
            }
        });

        mPlayerAccess = PlayerAccess.getInstance(getActivity());
        mBinding.back.setOnClickListener(v -> mPlayerAccess.previousTrack());

        mBinding.next.setOnClickListener(v -> mPlayerAccess.nextTrack());

        mBinding.play.setOnClickListener(v -> {
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

}

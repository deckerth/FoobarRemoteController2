package com.deckerth.thomas.foobarremotecontroller2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.deckerth.thomas.foobarremotecontroller2.databinding.FragmentTitleDetailClassicalBinding;
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.PlayerViewModel;

import java.util.Objects;

/**
 * A fragment representing a single Title detail screen.
 * This fragment is either contained in a {@link TitleListFragment}
 * in two-pane mode (on larger screen devices) or self-contained
 * on handsets.
 */
public class TitleDetailClassicalFragment extends Fragment {

    private FragmentTitleDetailClassicalBinding mBinding;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TitleDetailClassicalFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentTitleDetailClassicalBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PlayerViewModel mPlayerViewModel = PlayerViewModel.getInstance();
        mPlayerViewModel.getComposer().observe(getViewLifecycleOwner(), mBinding.composer::setText);
        mPlayerViewModel.getAlbum().observe(getViewLifecycleOwner(), mBinding.album::setText);
        mPlayerViewModel.getTitle().observe(getViewLifecycleOwner(), mBinding.title::setText);
        mPlayerViewModel.getArtist().observe(getViewLifecycleOwner(), mBinding.artist::setText);
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
package com.deckerth.thomas.foobarremotecontroller2;

import android.content.ClipData;
import android.os.Bundle;
import android.view.DragEvent;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.deckerth.thomas.foobarremotecontroller2.model.ITitle;
import com.deckerth.thomas.foobarremotecontroller2.model.Playlist;
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

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The placeholder content this fragment is presenting.
     */
    private ITitle mItem;
    private CollapsingToolbarLayout mToolbarLayout;
    private TextView mTextView;

     private FragmentTitleDetailBinding binding;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TitleDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the placeholder content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            List<ITitle> playlist = PlaylistViewModel.getInstance().getPlaylist().getValue();
            String index = getArguments().getString(ARG_ITEM_ID);
            mItem = playlist.stream().filter(entry -> entry.getIndex().equals(index)).findFirst().get();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentTitleDetailBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        mToolbarLayout = rootView.findViewById(R.id.toolbar_layout);
        mTextView = binding.titleDetail;

        // Show the placeholder content as text in a TextView & in the toolbar if available.
        updateContent();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void updateContent() {
        if (mItem != null) {
            mTextView.setText(mItem.getTitle());
            if (mToolbarLayout != null) {
                mToolbarLayout.setTitle(mItem.getAlbum());
            }
        }
    }
}
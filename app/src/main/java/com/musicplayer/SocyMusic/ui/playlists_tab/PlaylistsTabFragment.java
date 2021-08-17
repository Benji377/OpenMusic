package com.musicplayer.SocyMusic.ui.playlists_tab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.musicplayer.SocyMusic.custom_views.CustomRecyclerView;
import com.musicplayer.musicplayer.R;

public class PlaylistsTabFragment extends Fragment {
    private CustomRecyclerView playlistsRecyclerView;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlists_tab, container, false);
        playlistsRecyclerView = view.findViewById(R.id.recyclerview_playlists_tab_playlists);
//        playlistsRecyclerView.setAdapter(new PlaylistsAdapter(requireContext(), ));
        return view;
    }
}

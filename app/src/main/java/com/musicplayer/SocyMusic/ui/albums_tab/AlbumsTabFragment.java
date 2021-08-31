package com.musicplayer.SocyMusic.ui.albums_tab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.musicplayer.SocyMusic.custom_views.CustomRecyclerView;
import com.musicplayer.SocyMusic.data.Song;
import com.musicplayer.SocyMusic.data.SongsData;
import com.musicplayer.musicplayer.R;

public class AlbumsTabFragment extends Fragment {
    private CustomRecyclerView albumsRecyclerView;

    private SongsData songsData;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        songsData = SongsData.getInstance(requireContext());
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_albums_tab, container, false);
        albumsRecyclerView = view.findViewById(R.id.recyclerview_albums_tab_all);
        AlbumsListAdapter adapter = new AlbumsListAdapter(requireContext(), songsData.getAllAlbums());

        albumsRecyclerView.setAdapter(adapter);
        albumsRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        TextView emptyTextview = view.findViewById(R.id.textview_albums_tab_empty);
        albumsRecyclerView.setEmptyView(emptyTextview);
        return view;
    }
}

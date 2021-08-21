package com.musicplayer.SocyMusic.ui.playlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.musicplayer.SocyMusic.MediaPlayerUtil;
import com.musicplayer.SocyMusic.custom_views.CustomRecyclerView;
import com.musicplayer.SocyMusic.data.Playlist;
import com.musicplayer.musicplayer.R;

public class PlaylistFragment extends Fragment {
    private static final String KEY_PLAYLIST = "com.musicplayer.SocyMusic.ui.playlist.PlaylistFragment.playlist";
    private Playlist playlist;
    private CustomRecyclerView songsRecyclerView;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            playlist = (Playlist) getArguments().getSerializable(KEY_PLAYLIST);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar_playlist);
        toolbar.setNavigationIcon(R.drawable.ic_playlist);
        CollapsingToolbarLayout toolbarLayout = view.findViewById(R.id.layout_playlist_collapsing_toolbar);
        toolbarLayout.setTitle(playlist.getName());

        AppBarLayout appBarLayout = (AppBarLayout) view.findViewById(R.id.layout_playlist_appbar);
        View toolbarContent = view.findViewById(R.id.layout_playlist_toolbar_content);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                float offsetFactor = (float) (-verticalOffset) / (float) scrollRange;
                toolbarContent.setAlpha(1 - offsetFactor);
                if (scrollRange + verticalOffset == 0) {
                    toolbarLayout.setTitle(playlist.getName());
                    toolbar.setNavigationIcon(R.drawable.ic_playlist);
                } else {
                    toolbarLayout.setTitle("");
                    toolbar.setNavigationIcon(null);
                }
            }
        });

        TextView playlistNameTextview = view.findViewById(R.id.textview_playlist_name);
        playlistNameTextview.setText(playlist.getName());

        TextView songCountTextview = view.findViewById(R.id.textview_playlist_song_count);
        songCountTextview.setText(requireContext().getString(R.string.playlist_song_count, playlist.getSongCount()));

        TextView totalDurationTextview = view.findViewById(R.id.textview_playlist_total_duration);
        totalDurationTextview.setText(MediaPlayerUtil.createTime(playlist.calculateTotalDuration()));

        PlaylistSongAdapter adapter = new PlaylistSongAdapter(requireContext(), playlist.getSongList());
        songsRecyclerView = view.findViewById(R.id.recyclerview_playlist_songs);
        songsRecyclerView.setAdapter(adapter);
        songsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        return view;
    }

    public static PlaylistFragment newInstance(Playlist playlist) {
        Bundle args = new Bundle();
        args.putSerializable(KEY_PLAYLIST, playlist);
        PlaylistFragment fragment = new PlaylistFragment();
        fragment.setArguments(args);
        return fragment;
    }
}

package com.musicplayer.SocyMusic.ui.playlist;

import android.content.Context;
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
import com.musicplayer.SocyMusic.data.Song;
import com.musicplayer.SocyMusic.data.SongsData;
import com.musicplayer.musicplayer.R;

public class PlaylistFragment extends Fragment {
    private static final String KEY_PLAYLIST = "com.musicplayer.SocyMusic.ui.playlist.PlaylistFragment.playlist";
    private Playlist playlist;
    private CustomRecyclerView songsRecyclerView;
    private SongsData songsData;
    private Host hostCallback;
    private TextView playlistNameTextview;
    private TextView songCountTextview;
    private TextView totalDurationTextview;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            playlist = (Playlist) getArguments().getSerializable(KEY_PLAYLIST);
        songsData = SongsData.getInstance(requireContext());
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

        playlistNameTextview = view.findViewById(R.id.textview_playlist_name);
        playlistNameTextview.setText(playlist.getName());

        songCountTextview = view.findViewById(R.id.textview_playlist_song_count);
        songCountTextview.setText(requireContext().getString(R.string.playlist_song_count, playlist.getSongCount()));

        totalDurationTextview = view.findViewById(R.id.textview_playlist_total_duration);
        totalDurationTextview.setText(MediaPlayerUtil.createTime(playlist.calculateTotalDuration()));

        PlaylistSongAdapter adapter = new PlaylistSongAdapter(requireContext(), playlist.getSongList());
        adapter.setOnItemClickListener(new PlaylistSongAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                songsData.playPlaylistFrom(playlist, position);
                hostCallback.onSongClick(playlist.getSongAt(position));
            }

            @Override
            public boolean onItemLongClick(int position, View view) {
                return false;
            }
        });

        songsRecyclerView = view.findViewById(R.id.recyclerview_playlist_songs);
        songsRecyclerView.setAdapter(adapter);
        songsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        //TODO: get this empty textview to work with the collapsing toolbar layout
//        TextView listEmptyTextview = view.findViewById(R.id.textview_playlist_empty);
//        songsRecyclerView.setEmptyView(listEmptyTextview);
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            this.hostCallback = (Host) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement PlaylistFragment.Host");
        }
    }

    public static PlaylistFragment newInstance(Playlist playlist) {
        Bundle args = new Bundle();
        args.putSerializable(KEY_PLAYLIST, playlist);
        PlaylistFragment fragment = new PlaylistFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void onPlaylistUpdate(Playlist newPlaylist) {
        playlist = newPlaylist;
        ((PlaylistSongAdapter) songsRecyclerView.getAdapter()).setSongs(playlist.getSongList());
        songsRecyclerView.getAdapter().notifyDataSetChanged();
        songCountTextview.setText(requireContext().getString(R.string.playlist_song_count, playlist.getSongCount()));
        totalDurationTextview.setText(MediaPlayerUtil.createTime(playlist.calculateTotalDuration()));

    }

    interface Host {
        void onSongClick(Song song);
    }
}

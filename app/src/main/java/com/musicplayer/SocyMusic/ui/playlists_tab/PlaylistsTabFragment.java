package com.musicplayer.SocyMusic.ui.playlists_tab;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.musicplayer.SocyMusic.custom_views.CustomRecyclerView;
import com.musicplayer.SocyMusic.data.SongsData;
import com.musicplayer.SocyMusic.ui.playlist.PlaylistActivity;
import com.musicplayer.musicplayer.R;

public class PlaylistsTabFragment extends Fragment {
    private SongsData songsData;
    private CustomRecyclerView playlistsRecyclerView;
    private PlaylistsAdapter playlistsAdapter;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        songsData = SongsData.getInstance(requireContext());
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlists_tab, container, false);
        playlistsRecyclerView = view.findViewById(R.id.recyclerview_playlists_tab_playlists);
        playlistsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        playlistsAdapter = new PlaylistsAdapter(requireContext(), songsData.getAllPlaylists(requireContext()));
        playlistsAdapter.setOnItemClickListener(new PlaylistsAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                Intent intent = new Intent(requireContext(), PlaylistActivity.class);
                intent.putExtra(PlaylistActivity.EXTRA_PLAYLIST, songsData.getAllPlaylists(requireContext()).get(position));
                startActivity(intent);
            }

            @Override
            public boolean onItemLongClick(int position, View view) {
                return false;
            }
        });
        playlistsRecyclerView.setAdapter(playlistsAdapter);
        TextView emptyTextView = view.findViewById(R.id.textview_playlists_tab_empty);
        playlistsRecyclerView.setEmptyView(emptyTextView);
        return view;
    }

    public void updatePlaylistAt(int position) {
        playlistsAdapter.notifyItemChanged(position);
    }
}

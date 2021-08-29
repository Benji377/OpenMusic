package com.musicplayer.SocyMusic.ui.playlists_tab;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.musicplayer.SocyMusic.custom_views.CustomRecyclerView;
import com.musicplayer.SocyMusic.data.SongsData;
import com.musicplayer.SocyMusic.ui.playlist.PlaylistActivity;
import com.musicplayer.SocyMusic.utils.DialogUtils;
import com.musicplayer.musicplayer.R;

public class PlaylistsTabFragment extends Fragment {
    private SongsData songsData;
    private CustomRecyclerView playlistsRecyclerView;
    private PlaylistsAdapter playlistsAdapter;
    private Host hostCallback;
    private FloatingActionButton fabMain, fabDelete, fabCreate;
    private boolean areSubfabsVisible;

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
        playlistsAdapter = new PlaylistsAdapter(requireContext(), songsData.getAllPlaylists());

        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != Activity.RESULT_CANCELED)
                hostCallback.onQueueChanged();
        });
        playlistsAdapter.setOnItemClickListener(new PlaylistsAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                Intent intent = new Intent(requireContext(), PlaylistActivity.class);
                intent.putExtra(PlaylistActivity.EXTRA_PLAYLIST, songsData.getAllPlaylists().get(position));
                intent.putExtra(PlaylistActivity.EXTRA_SHOW_PLAYER, hostCallback.isShowingPlayer());
                hostCallback.onPlaylistClick();
                launcher.launch(intent);
            }

            @Override
            public boolean onItemLongClick(int position, View view) {
                return false;
            }
        });
        playlistsRecyclerView.setAdapter(playlistsAdapter);
        TextView emptyTextView = view.findViewById(R.id.textview_playlists_tab_empty);
        playlistsRecyclerView.setEmptyView(emptyTextView);

        // Manages the floating action button (fab)
        fabMain = view.findViewById(R.id.main_fab);
        fabCreate = view.findViewById(R.id.child_fab_add);
        fabDelete = view.findViewById(R.id.child_fab_remove);

        fabCreate.setVisibility(View.GONE);
        fabDelete.setVisibility(View.GONE);
        areSubfabsVisible = false;

        // FAB click listener
        fabMain.setOnClickListener(v -> {
            if (!areSubfabsVisible) {
                fabCreate.show();
                fabDelete.show();
                areSubfabsVisible = true;
            } else {
                fabCreate.hide();
                fabDelete.hide();
                areSubfabsVisible = false;
            }
        });
        fabCreate.setOnClickListener(v -> DialogUtils.showNewPlaylistDialog(requireContext(), newPlaylist -> notifyPlaylistInserted()));
        fabDelete.setOnClickListener(v -> DialogUtils.showDeletePlaylistDialog(requireContext(), this::notifyPlaylistDeleted));

        return view;
    }

    public void updatePlaylistAt(int position) {
        playlistsAdapter.notifyItemChanged(position);
    }

    public void notifyPlaylistInserted() {
        playlistsAdapter.notifyItemInserted(playlistsAdapter.getItemCount());
    }

    public void notifyPlaylistDeleted(int index) {
        playlistsAdapter.notifyItemRemoved(index);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        try {
            hostCallback = (Host) context;
        } catch (ClassCastException exception) {
            throw new ClassCastException(context.toString() + " must implement PlaylistsTabFragment.Host");
        }
        super.onAttach(context);
    }

    public interface Host {
        boolean isShowingPlayer();

        void onQueueChanged();

        void onPlaylistClick();

    }
}

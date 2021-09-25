package com.musicplayer.SocyMusic.ui.albums_tab;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.musicplayer.SocyMusic.custom_views.CustomRecyclerView;
import com.musicplayer.SocyMusic.data.SongsData;
import com.musicplayer.SocyMusic.ui.album.AlbumActivity;
import com.musicplayer.musicplayer.R;

public class AlbumsTabFragment extends Fragment {
    private CustomRecyclerView albumsRecyclerView;

    private SongsData songsData;
    private Host hostCallback;

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
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != Activity.RESULT_CANCELED)
                hostCallback.onQueueChanged();
        });
        adapter.setOnItemClickListener(new AlbumsListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                Intent intent = new Intent(requireContext(), AlbumActivity.class);
                intent.putExtra(AlbumActivity.EXTRA_ALBUM, songsData.getAllAlbums().get(position));
                intent.putExtra(AlbumActivity.EXTRA_SHOW_PLAYER, hostCallback.isShowingPlayer());
                hostCallback.onSongListClick();
                launcher.launch(intent);
            }

            @Override
            public boolean onItemLongClick(int position, View view) {
                return false;
            }
        });
        albumsRecyclerView.setAdapter(adapter);
        albumsRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        TextView emptyTextview = view.findViewById(R.id.textview_albums_tab_empty);
        albumsRecyclerView.setEmptyView(emptyTextview);
        return view;
    }

    public void invalidateAlbumList() {
        albumsRecyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            this.hostCallback = (Host) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement AlbumsTabFragment.Host");
        }
    }

    public interface Host {
        boolean isShowingPlayer();

        void onSongListClick();

        void onQueueChanged();
    }
}

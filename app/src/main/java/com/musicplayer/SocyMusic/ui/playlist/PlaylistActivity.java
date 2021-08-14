package com.musicplayer.SocyMusic.ui.playlist;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.musicplayer.SocyMusic.custom_views.CustomRecyclerView;
import com.musicplayer.musicplayer.R;

public class PlaylistActivity extends AppCompatActivity {
    private PlaylistData playlistData;
    private CustomRecyclerView playlistRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        playlistData = PlaylistData.getInstance(this);
        setContentView(R.layout.activity_playlist);
        playlistRecyclerView = findViewById(R.id.recyclerview_main_all_playlists);
        playlistData.reloadPlaylists();
        displayPlaylists();
    }

    void displayPlaylists() {
        PlaylistListAdapter customAdapter = new PlaylistListAdapter(this, playlistData.getPlaylistList());
        playlistRecyclerView.setAdapter(customAdapter);
        playlistRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // If you click on an tem in the list, the player fragment opens
        customAdapter.setOnItemClickListener(new PlaylistListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                // Error occured
                if (!playlistData.playlistExists(position)) {
                    Toast.makeText(PlaylistActivity.this, getText(R.string.main_err_file_gone), Toast.LENGTH_LONG).show();
                    playlistData.reloadPlaylists();
                    customAdapter.notifyDataSetChanged();
                    return;
                }
                // Open Playlistdata or somethings

            }
            @Override
            public boolean onItemLongClick(int position, View view) {
                return true;
            }
        });
        // Error occurs --> song not found
        TextView emptyText = findViewById(R.id.textview_playlist_list_empty);
        playlistRecyclerView.setEmptyView(emptyText);

    }

    private void invalidatePlaylistList() {
        PlaylistListAdapter adapter = (PlaylistListAdapter) playlistRecyclerView.getAdapter();
        if (adapter != null) {
            adapter.setAllPlaylist(playlistData.getPlaylistList());
            adapter.notifyDataSetChanged();
        }
    }
}

package com.musicplayer.SocyMusic.ui.playlist;

import android.os.Bundle;
import android.view.Menu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.musicplayer.SocyMusic.data.Playlist;
import com.musicplayer.musicplayer.R;

public class PlaylistActivity extends AppCompatActivity {
    public static final String EXTRA_PLAYLIST = "com.musicplayer.SocyMusic.ui.playlist.PlaylistActivity.EXTRA_PLAYLIST";
    private PlaylistFragment playlistFragment;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        Playlist playlist= (Playlist) getIntent().getExtras().getSerializable(EXTRA_PLAYLIST);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.layout_playlist_container);
        if (fragment == null) {
            playlistFragment = PlaylistFragment.newInstance(playlist);
            fragmentManager.beginTransaction()
                    .add(R.id.layout_playlist_container, playlistFragment)
                    .commit();
        } else
            playlistFragment = (PlaylistFragment) fragment;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }


}

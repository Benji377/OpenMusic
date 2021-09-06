package com.musicplayer.SocyMusic.ui.playlist;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.musicplayer.SocyMusic.SocyMusicApp;
import com.musicplayer.SocyMusic.data.Playlist;
import com.musicplayer.SocyMusic.data.Song;
import com.musicplayer.SocyMusic.ui.player_fragment_host.PlayerFragmentHost;
import com.musicplayer.SocyMusic.utils.ThemeChanger;
import com.musicplayer.musicplayer.R;

public class PlaylistActivity extends PlayerFragmentHost implements PlaylistFragment.Host {
    public static final String EXTRA_PLAYLIST = "com.musicplayer.SocyMusic.ui.playlist.PlaylistActivity.EXTRA_PLAYLIST";
    public static final String EXTRA_SHOW_PLAYER = "com.musicplayer.SocyMusic.ui.playlist.PlaylistActivity.EXTRA_PLAYING";
    public static final String RESULT_EXTRA_QUEUE_CHANGED = "com.musicplayer.SocyMusic.ui.playlist.PlaylistActivity.EXTRA_PLAYING";
    private PlaylistFragment playlistFragment;
    private Playlist playlist;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        // Sets the theme!
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(ThemeChanger.getThemeID(this));
        listener = (prefs1, key) -> {
            if (key.equals(SocyMusicApp.PREFS_KEY_THEME)) {
                recreate();
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);

        super.onCreate(savedInstanceState);
        View childView = getLayoutInflater().inflate(R.layout.content_playlist,
                (ViewGroup) findViewById(R.id.layout_playlist_content), false);
        super.attachContentView(childView);

        playlist = (Playlist) getIntent().getExtras().getSerializable(EXTRA_PLAYLIST);
        boolean showPlayer = getIntent().getExtras().getBoolean(EXTRA_SHOW_PLAYER);
        if (showPlayer)
            super.startPlayer(false);

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
    public void onSongClick(Song songClicked) {
        super.onSongClick(songClicked);
        Intent resultIntent = new Intent();
        resultIntent.putExtra(RESULT_EXTRA_QUEUE_CHANGED, true);
        setResult(Activity.RESULT_OK, resultIntent);
    }

    @Override
    public void onPlaylistUpdate(Playlist playlist) {
        if (this.playlist.equals(playlist) && playlistFragment != null) {
            this.playlist = playlist;
            playlistFragment.onPlaylistUpdate(playlist);
        }
    }

    @Override
    public void onNewPlaylist(Playlist newPlaylist) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing())
            super.unregisterMediaReceiver();
    }
}

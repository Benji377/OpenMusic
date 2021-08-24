package com.musicplayer.SocyMusic.ui.playlist;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.musicplayer.SocyMusic.SocyMusicApp;
import com.musicplayer.SocyMusic.data.Playlist;
import com.musicplayer.SocyMusic.utils.ThemeChanger;
import com.musicplayer.musicplayer.R;

import timber.log.Timber;

public class PlaylistActivity extends AppCompatActivity {
    public static final String EXTRA_PLAYLIST = "com.musicplayer.SocyMusic.ui.playlist.PlaylistActivity.EXTRA_PLAYLIST";
    private PlaylistFragment playlistFragment;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        // Sets the theme!
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(ThemeChanger.getThemeID(this));
        Timber.e("PLAY: Function out = %s", ThemeChanger.getThemeID(this));
        listener = (prefs1, key) -> {
            if (key.equals(SocyMusicApp.PREFS_KEY_THEME)) {
                recreate();
                Timber.e("PLAY: Recreation done!");
            }
        };
        Timber.e("PLAY: Listener set");
        prefs.registerOnSharedPreferenceChangeListener(listener);
        Timber.e("PLAY: Listener registered");

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

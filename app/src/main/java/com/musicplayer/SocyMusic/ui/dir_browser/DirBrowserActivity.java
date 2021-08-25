package com.musicplayer.SocyMusic.ui.dir_browser;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.musicplayer.SocyMusic.SocyMusicApp;
import com.musicplayer.SocyMusic.utils.ThemeChanger;
import com.musicplayer.musicplayer.R;


public class DirBrowserActivity extends AppCompatActivity {
    private DirBrowserFragment dirBrowserFragment;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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
        setContentView(R.layout.activity_dir_browser);
        setTitle(R.string.dir_browser_title);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.layout_settings_container);
        if (fragment == null) {
            dirBrowserFragment = new DirBrowserFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.layout_dir_browser_container, dirBrowserFragment)
                    .commit();
        } else
            dirBrowserFragment = (DirBrowserFragment) fragment;
    }

    @Override
    public void onBackPressed() {
        dirBrowserFragment.onBackPressed();
    }
}

package com.musicplayer.SocyMusic.ui.settings;


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


public class SettingsActivity extends AppCompatActivity {
    private SettingsFragment settingsFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(ThemeChanger.getTheme(getApplicationContext()));
        // Settings listener to update theme in realtime
        // Use instance field for listener
        // It will not be gc'd as long as this instance is kept referenced
        SharedPreferences.OnSharedPreferenceChangeListener listener = (prefs1, key) -> {
            if (key.equals(SocyMusicApp.PREFS_KEY_THEME)) {
                recreate();
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);

        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.main_menu_item_settings);
        setContentView(R.layout.activity_settings);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.layout_settings_container);
        if (fragment == null) {
            settingsFragment = new SettingsFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.layout_settings_container, settingsFragment)
                    .commit();
        } else
            settingsFragment = (SettingsFragment) fragment;

    }
}

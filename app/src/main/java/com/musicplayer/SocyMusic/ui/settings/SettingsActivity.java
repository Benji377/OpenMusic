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

import timber.log.Timber;


public class SettingsActivity extends AppCompatActivity {

    private SettingsFragment settingsFragment;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // Sets the theme!
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(ThemeChanger.getThemeID(this));
        Timber.e("SETTING: Function out = %s", ThemeChanger.getThemeID(this));
        listener = (prefs1, key) -> {
            if (key.equals(SocyMusicApp.PREFS_KEY_THEME)) {
                recreate();
                Timber.e("SETTING: Recreation done!");
            }
        };
        Timber.e("SETTING: Listener set");
        prefs.registerOnSharedPreferenceChangeListener(listener);
        Timber.e("SETTING: Listener registered");

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

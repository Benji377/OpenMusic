package com.musicplayer.SocyMusic.ui.settings;


import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.musicplayer.SocyMusic.SocyMusicApp;
import com.musicplayer.musicplayer.R;


public class SettingsActivity extends AppCompatActivity {

    private SettingsFragment settingsFragment;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = prefs.getString(SocyMusicApp.PREFS_KEY_THEME, "Red_theme");
        switch (theme) {
            case "Red_theme":
                setTheme(R.style.Theme_MusicPlayer);
                break;
            case "Inverted_theme":
                setTheme(R.style.InvertedTheme);
                break;
            case "Blue_theme":
                setTheme(R.style.BlueTheme);
                break;
            case "Green_theme":
                setTheme(R.style.GreenTheme);
                break;
            case "Orange_theme":
                setTheme(R.style.OrangeTheme);
                break;
            case "Yellow_theme":
                setTheme(R.style.YellowTheme);
                break;
            case "Aqua_theme":
                setTheme(R.style.AquaTheme);
                break;
            case "Purple_theme":
                setTheme(R.style.PurpleTheme);
                break;
            case "Purpleblue_theme":
                setTheme(R.style.PurpleblueTheme);
                break;
            case "Orangegreen_theme":
                setTheme(R.style.OrangegreenTheme);
                break;
            case "Purpleaqua_theme":
                setTheme(R.style.PurpleaquaTheme);
                break;
        }
        // Settings listener to update theme in realtime
        // Use instance field for listener
        // It will not be gc'd as long as this instance is kept referenced
        listener = (prefs1, key) -> {
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

package com.musicplayer.SocyMusic.ui.settings;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.musicplayer.musicplayer.R;


public class SettingsActivity extends AppCompatActivity {

    private SettingsFragment settingsFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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

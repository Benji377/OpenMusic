package com.musicplayer.SocyMusic.ui.sleeptime;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.musicplayer.SocyMusic.SocyMusicApp;
import com.musicplayer.SocyMusic.utils.PreferenceUtils;
import com.musicplayer.musicplayer.R;

public class SleepTimeActivity extends AppCompatActivity {
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    private SleepTimeFragment sleepTimeFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // Sets the theme!
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceUtils pUtils = new PreferenceUtils(this);
        setTheme(pUtils.getThemeID());
        setTheme(pUtils.getThemeID());
        listener = (prefs1, key) -> {
            if (key.equals(SocyMusicApp.PREFS_KEY_THEME)) {
                recreate();
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleeptime);
        setTitle("Select Sleeptime");

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.layout_settings_container);
        if (fragment == null) {
            sleepTimeFragment = new SleepTimeFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.layout_sleeptime_container, sleepTimeFragment)
                    .commit();
        } else {
            sleepTimeFragment = (SleepTimeFragment) fragment;
        }
    }
}


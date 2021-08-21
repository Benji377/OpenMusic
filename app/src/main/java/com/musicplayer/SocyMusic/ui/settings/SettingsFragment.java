package com.musicplayer.SocyMusic.ui.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.musicplayer.SocyMusic.SocyMusicApp;
import com.musicplayer.SocyMusic.data.SongsData;
import com.musicplayer.SocyMusic.ui.dir_browser.DirBrowserActivity;
import com.musicplayer.musicplayer.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    private Preference themePreference;
    private Preference libPathPreference;

    private SongsData songsData;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        songsData = SongsData.getInstance(requireContext());
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        libPathPreference = findPreference(SocyMusicApp.PREFS_KEY_LIBRARY_PATHS);
        themePreference = findPreference(SocyMusicApp.PREFS_KEY_THEME);

        libPathPreference.setIntent(new Intent(getContext(), DirBrowserActivity.class));
    }

}
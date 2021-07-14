package com.musicplayer.SocyMusic;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.musicplayer.musicplayer.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    private Preference themePreference;
    private Preference libPathPreference;

    private ActivityResultLauncher<Intent> resultLauncher;
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

        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            //TODO: move code from save method in DirBrowserFragment to here to let settingsFragment handle saving settings
        });

        libPathPreference.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getContext(), DirBrowserActivity.class);
            resultLauncher.launch(intent);
            return true;
        });


        themePreference.setOnPreferenceClickListener(preference -> {
            Toast.makeText(getContext(), getString(R.string.all_coming_soon), Toast.LENGTH_LONG).show();
            return true;
        });

    }

}
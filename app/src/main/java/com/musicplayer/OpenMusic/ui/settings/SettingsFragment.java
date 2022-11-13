package com.musicplayer.OpenMusic.ui.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.musicplayer.OpenMusic.OpenMusicApp;
import com.musicplayer.OpenMusic.data.SongsData;
import com.musicplayer.OpenMusic.ui.dir_browser.DirBrowserActivity;
import com.musicplayer.OpenMusic.ui.sleeptime.SleepTimeActivity;
import com.musicplayer.musicplayer.BuildConfig;
import com.musicplayer.musicplayer.R;

import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat {
    private SongsData songsData;
    private Host hostCallBack;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        songsData = SongsData.getInstance(requireContext());
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        Preference libPathPreference = findPreference(OpenMusicApp.PREFS_KEY_LIBRARY_PATHS);
        Preference versions = findPreference(OpenMusicApp.PREFS_KEY_VERSION);
        //Preference logging = findPreference(OpenMusicApp.PREFS_KEY_LOGGING);
        Preference sleeptime = findPreference(OpenMusicApp.PREFS_KEY_SLEEPTIME);
        Preference menuswitch = findPreference(OpenMusicApp.PREFS_KEY_MENUSWITCH);
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK)
                hostCallBack.onLibraryDirsChanged();
        });
        libPathPreference.setOnPreferenceClickListener(preference -> {
            if (!songsData.isDoneLoading()) {
                Toast.makeText(requireContext(), R.string.settings_cannot_change_lib, Toast.LENGTH_SHORT).show();
                return false;
            }
            Intent intent = new Intent(getContext(), DirBrowserActivity.class);
            launcher.launch(intent);
            return true;
        });
        sleeptime.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getContext(), SleepTimeActivity.class);
            launcher.launch(intent);
            return true;
        });
        menuswitch.setOnPreferenceClickListener(preference -> {
            Objects.requireNonNull(getActivity()).recreate();
            return true;
        });

        versions.setSummary(getString(R.string.about_version, BuildConfig.VERSION_NAME));
    }

    /**
     * If the fragment is being attached to another activity
     *
     * @param context The context of the app
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            this.hostCallBack = (Host) context;
            // If implementation is missing
        } catch (final ClassCastException e) {
            throw new ClassCastException(context + " must implement SettingsFragment.Host");
        }
    }

    public interface Host {
        void onLibraryDirsChanged();
    }
}

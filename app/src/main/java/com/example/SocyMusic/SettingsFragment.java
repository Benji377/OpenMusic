package com.example.SocyMusic;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.musicplayer.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    private Preference themePreference;
    private Preference libPathPreference;

    public static final String PREFS_KEY_LIBRARY_PATHS = "lib_paths";
    public static final String PREFS_KEY_THEME = "theme";


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
        libPathPreference = findPreference(PREFS_KEY_LIBRARY_PATHS);
        themePreference = findPreference(PREFS_KEY_THEME);

        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//                Intent resultIntent = result.getData();
//                if (resultIntent == null)
//                    return;
//                if (result.getResultCode() == Activity.RESULT_OK) {
//                    requireContext().getContentResolver().takePersistableUriPermission(resultIntent.getData(),
//                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                    String newPath = resultIntent.getDataString();
//                    PreferenceManager.getDefaultSharedPreferences(requireContext()).edit().putString("root_path", newPath).apply();
//                    songsData.reloadSongs(requireContext());
//                }
        });

        libPathPreference.setOnPreferenceClickListener(preference -> {
            Toast.makeText(getContext(), getString(R.string.all_coming_soon), Toast.LENGTH_LONG).show();
            return true;
//                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//                resultLauncher.launch(intent);
//                return true;
        });


        themePreference.setOnPreferenceClickListener(preference -> {
            Toast.makeText(getContext(), getString(R.string.all_coming_soon), Toast.LENGTH_LONG).show();
            return true;
        });

    }

}
package com.musicplayer.SocyMusic.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import com.musicplayer.SocyMusic.SocyMusicApp;
import com.musicplayer.musicplayer.R;

/*
Class that holds all themes in a simple switch case.
Doing so simplifies the future management of themes
PS: Might change this later in a full Utils class instead of
"wasting" a whole class for one function
 */
public class PreferenceUtils {
    public static SharedPreferences prefs;

    public PreferenceUtils(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }


    public int getThemeID() {
        String theme = prefs.getString(SocyMusicApp.PREFS_KEY_THEME, "Red_theme");
        switch (theme) {
            case "Red_theme":
                return R.style.Theme_MusicPlayer;
            case "Inverted_theme":
                return R.style.InvertedTheme;
            case "Blue_theme":
                return R.style.BlueTheme;
            case "Green_theme":
                return R.style.GreenTheme;
            case "Orange_theme":
                return R.style.OrangeTheme;
            case "Yellow_theme":
                return R.style.YellowTheme;
            case "Aqua_theme":
                return R.style.AquaTheme;
            case "Purple_theme":
                return R.style.PurpleTheme;
            case "Purpleblue_theme":
                return R.style.PurpleblueTheme;
            case "Orangegreen_theme":
                return R.style.OrangegreenTheme;
            case "Purpleaqua_theme":
                return R.style.PurpleaquaTheme;
            default:
                return R.style.Theme_MusicPlayer;
        }
    }
}

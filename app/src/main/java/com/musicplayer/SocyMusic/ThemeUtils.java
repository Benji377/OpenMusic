package com.musicplayer.SocyMusic;

import android.app.Activity;
import android.content.Intent;

import com.musicplayer.musicplayer.R;

/*
Class to change theme programmatically.
Not working correctly as it just restarts the MainActivity over and over again.
 */
public class ThemeUtils {

    private static int cTheme;
    public final static int RED = 0;
    public final static int WHITE = 1;
    public final static int BLUE = 2;
    public final static int GREEN = 3;
    public final static int ORANGE = 4;

    public static void changeToTheme(Activity activity, int theme) {
        cTheme = theme;
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
    }

    public static void onActivityCreateSetTheme(Activity activity) {
        switch (cTheme) {
            case RED:
                activity.setTheme(R.style.Theme_MusicPlayer);
                break;
            case WHITE:
                activity.setTheme(R.style.WhiteTheme);
                break;
            case BLUE:
                activity.setTheme(R.style.BlueTheme);
                break;
            case GREEN:
                activity.setTheme(R.style.GreenTheme);
                break;
            case ORANGE:
                activity.setTheme(R.style.OrangeTheme);
                break;
        }
    }
}

package com.musicplayer.SocyMusic;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Environment;

import com.musicplayer.musicplayer.R;

import java.util.HashSet;
import java.util.UUID;

import timber.log.Timber;

/**
 * This class is necessary to create notifications according to different Android versions
 */
public class SocyMusicApp extends Application {
    // Unique ID for the media channel only!
    public static final String MEDIA_CHANNEL_ID = "media_channel";

    //keys for preferences
    public static final String PREFS_KEY_LIBRARY_PATHS = "lib_paths";
    public static final String PREFS_KEY_THEME = "theme";
    public static final String PREFS_KEY_VERSION = "versions";
    public static final HashSet<String> defaultPathsSet = new HashSet<>();

    /**
     * When this class gets invoked, this method gets called
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // Init Timber for debugging
        Timber.plant(new Timber.DebugTree());
        createNotificationChannels();
        defaultPathsSet.add(Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    /**
     * According to which version of Android is being used it is necessary to create a
     * NotificationChannel.
     */
    private void createNotificationChannels() {
        // Checks for Buildversion == Androidversion
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    MEDIA_CHANNEL_ID,
                    getString(R.string.notif_channel_media_name),
                    NotificationManager.IMPORTANCE_LOW
            );
            channel1.setDescription(getString(R.string.notif_channel_media_description));

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }

}

package com.musicplayer.SocyMusic;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import androidx.core.content.ContextCompat;

import com.musicplayer.musicplayer.R;

import java.util.HashSet;

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

    public static final String[] PERMISSIONS_NEEDED = {READ_EXTERNAL_STORAGE, RECORD_AUDIO};

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

    public static boolean hasPermissions(Context context) {
        boolean allGranted = true;
        for (String permission : PERMISSIONS_NEEDED)
            allGranted = allGranted && ContextCompat.checkSelfPermission(context, permission) == PERMISSION_GRANTED;
        return allGranted;
    }

}

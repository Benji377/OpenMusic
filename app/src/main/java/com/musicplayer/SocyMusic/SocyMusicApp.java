package com.musicplayer.SocyMusic;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.musicplayer.musicplayer.R;

import java.io.File;
import java.io.IOException;
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
    public static final String PREFS_KEY_LOGGING = "logging";
    public static final HashSet<String> defaultPathsSet = new HashSet<>();

    public static final String[] PERMISSIONS_NEEDED = {READ_EXTERNAL_STORAGE, RECORD_AUDIO};
    public static final String APP_FOLDER_ALBUMS_ART = "albumart";

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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean loggingMode = prefs.getBoolean(PREFS_KEY_LOGGING, true);
        if(loggingMode) {
            enableLogging();
        }
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

    public void enableLogging() {
        // Saves logcat output to a textfile!
        if (isExternalStorageWritable()) {
            File logDirectory = new File(getApplicationContext().getExternalFilesDir(null).getParentFile() + "/logs" );
            File logFile = new File(logDirectory, "logcat_" + System.currentTimeMillis() + ".txt" );
            Timber.e("LOG: %s", logDirectory.getPath());
            Timber.e("FILE: %s", logFile.getPath());

            // create log folder
            if (!logDirectory.exists()) {
                Timber.e(String.valueOf(logDirectory.mkdir()));
            }

            // clear the previous logcat and then write the new one to the file
            try {
                Runtime.getRuntime().exec("logcat -c");
                Runtime.getRuntime().exec("logcat -f " + logFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if ( isExternalStorageReadable() ) {
            Timber.e("Storage only readable");
        } else {
            Timber.e("Storage not accessible");
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

}

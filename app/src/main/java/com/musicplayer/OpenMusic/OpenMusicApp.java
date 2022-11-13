package com.musicplayer.OpenMusic;

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
 * This class contains all important constants of the app. This is especially useful in settings,
 * where one can just use this constants to save values and read values from them.
 * This is also the first class to get executed when the app is launched
 */
public class OpenMusicApp extends Application {
    // Unique ID for the media channel only!
    public static final String MEDIA_CHANNEL_ID = "media_channel";

    // Unique keys used to store values from settings
    public static final String PREFS_KEY_LIBRARY_PATHS = "lib_paths";
    public static final String PREFS_KEY_THEME = "theme";
    public static final String PREFS_KEY_VERSION = "versions";
    public static final String PREFS_KEY_LOGGING = "logging";
    public static final String PREFS_KEY_SLEEPTIME = "sleeptime";
    public static final String PREFS_KEY_TIMEPICKER = "timepicker";
    public static final String PREFS_KEY_TIMEPICKER_SWITCH = "timepicker_switch";
    public static final String PREFS_KEY_MENUSWITCH = "menu_switch";
    public static final HashSet<String> defaultPathsSet = new HashSet<>();

    // Defines all needed permissions for the app. The app will ask for these permissions
    // on the first launch
    public static final String[] PERMISSIONS_NEEDED = {READ_EXTERNAL_STORAGE, RECORD_AUDIO};

    // A folder to store all album arts
    public static final String APP_FOLDER_ALBUMS_ART = "albumart";

    /**
     * A method to verify if the user has granted the necessary permissions to the app
     *
     * @param context The context of the app
     * @return True if granted, else False
     */
    public static boolean hasPermissions(Context context) {
        Timber.i("Inspecting permissions");
        boolean allGranted = true;
        for (String permission : PERMISSIONS_NEEDED)
            allGranted = allGranted && ContextCompat.checkSelfPermission(context, permission) == PERMISSION_GRANTED;
        return allGranted;
    }

    /**
     * This method gets called upon creating the class, You can think about it like some sort of
     * constructor. In this case it mainly does three things:
     * 1. Create a Timber Debug tree for logging possible errors
     * 2. Create a Notification channel to display Music as a Notification
     * 3. Read the settings (preferences) and apply them to the app
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // Init Timber for debugging
        Timber.plant(new Timber.DebugTree());
        // Create a Notification channel where notifications will be displayed
        createNotificationChannels();
        // Sets the path of the App, this is useful to later know where music is stored
        defaultPathsSet.add(Environment.getExternalStorageDirectory().getAbsolutePath());

        // Get the actual settings (preferences) and enable logging if that option is set
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (prefs.getBoolean(PREFS_KEY_LOGGING, true)) {
            enableLogging();
        }
    }

    /**
     * After Android version 26 (Oreo) it is necessary to create a Notification channel.
     * The NotificationChannel is like a place where you can send Notifications and let them
     * display on peoples devices
     */
    private void createNotificationChannels() {
        Timber.i("Creating a Notification channel");
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

    /**
     * This method enables logging of device interactions with the app. This si especially useful when
     * debugging the app on other peoples phones. We can let users send us the log and look
     * at what went wrong. This is much easier then just guessing what the person did or what function
     * might have failed. The logs are simply stored as textfiles and can be read by everyone
     */
    public void enableLogging() {
        Timber.i("Enabling logging");
        // Saves logcat output to a textfile!
        if (isExternalStorageWritable()) {
            // Creates or tries to access a directory called "logs"
            File logDirectory = new File(getApplicationContext().getExternalFilesDir(null).getParentFile() + "/logs");
            // Each file should be prefixed with "logcat_", then the time of creating the log and finally its extension
            File logFile = new File(logDirectory, "logcat_" + System.currentTimeMillis() + ".txt");

            // create log folder if it doesn't exist
            if (!logDirectory.exists()) {
                Timber.e(String.valueOf(logDirectory.mkdir()));
            }

            // clear the previous logcat and then write the new one to the file
            // This doesn't delete the previous logfile, it just clears the console
            try {
                Runtime.getRuntime().exec("logcat -c");
                // TODO: Inspect what this line of code does exactly, and if debugging works as intended
                Runtime.getRuntime().exec(new String[]{"c:/path/to/latlon2utm.exe",
                        logFile.getPath()});
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (isExternalStorageReadable()) {
            Timber.e("Storage only readable");
        } else {
            Timber.e("Storage not accessible");
        }
    }

    /**
     * Checks if external storage is available for read and write.
     * This is important because the app needs to read it to get the music files,
     * but also write to it to save settings for example
     *
     * @return True if the storage is readable and writable, False if otherwise
     */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Checks if external storage is available to at least read
     *
     * @return True if the storage is readable, else false
     */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

}

package com.musicplayer.OpenMusic

import android.app.Application
import timber.log.Timber
import timber.log.Timber.DebugTree
import android.os.Environment
import android.os.Build
import android.app.NotificationChannel
import com.musicplayer.musicplayer.R
import android.app.NotificationManager
import android.Manifest.permission
import android.content.Context
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.preference.PreferenceManager
import java.io.File
import java.io.IOException
import java.util.HashSet

/**
 * This class contains all important constants of the app. This is especially useful in settings,
 * where one can just use this constants to save values and read values from them.
 * This is also the first class to get executed when the app is launched
 */
class OpenMusicApp : Application() {
    /**
     * This method gets called upon creating the class, You can think about it like some sort of
     * constructor. In this case it mainly does three things:
     * 1. Create a Timber Debug tree for logging possible errors
     * 2. Create a Notification channel to display Music as a Notification
     * 3. Read the settings (preferences) and apply them to the app
     */
    override fun onCreate() {
        super.onCreate()
        // Init Timber for debugging
        Timber.plant(DebugTree())
        // Create a Notification channel where notifications will be displayed
        createNotificationChannels()
        // Sets the path of the App, this is useful to later know where music is stored
        defaultPathsSet.add(Environment.getExternalStorageDirectory().absolutePath)

        // Get the actual settings (preferences) and enable logging if that option is set
        val prefs = PreferenceManager.getDefaultSharedPreferences(
            applicationContext
        )
        if (prefs.getBoolean(PREFS_KEY_LOGGING, true)) {
            enableLogging()
        }
    }

    /**
     * After Android version 26 (Oreo) it is necessary to create a Notification channel.
     * The NotificationChannel is like a place where you can send Notifications and let them
     * display on peoples devices
     */
    private fun createNotificationChannels() {
        Timber.i("Creating a Notification channel")
        // Checks for Buildversion == Androidversion
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel1 = NotificationChannel(
                MEDIA_CHANNEL_ID,
                getString(R.string.notif_channel_media_name),
                NotificationManager.IMPORTANCE_LOW
            )
            channel1.description = getString(R.string.notif_channel_media_description)
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(channel1)
        }
    }

    /**
     * This method enables logging of device interactions with the app. This si especially useful when
     * debugging the app on other peoples phones. We can let users send us the log and look
     * at what went wrong. This is much easier then just guessing what the person did or what function
     * might have failed. The logs are simply stored as textfiles and can be read by everyone
     */
    private fun enableLogging() {
        Timber.i("Enabling logging")
        // Saves logcat output to a textfile!
        if (isExternalStorageWritable) {
            // Creates or tries to access a directory called "logs"
            val logDirectory = File(
                applicationContext.getExternalFilesDir(null)!!.parentFile!!.toString() + "/logs"
            )
            // Each file should be prefixed with "logcat_", then the time of creating the log and finally its extension
            val logFile = File(logDirectory, "logcat_" + System.currentTimeMillis() + ".txt")

            // create log folder if it doesn't exist
            if (!logDirectory.exists()) {
                Timber.e(logDirectory.mkdir().toString())
            }

            // clear the previous logcat and then write the new one to the file
            // This doesn't delete the previous logfile, it just clears the console
            try {
                Runtime.getRuntime().exec("logcat -c")
                // TODO: Inspect what this line of code does exactly, and if debugging works as intended
                Runtime.getRuntime().exec(
                    arrayOf(
                        "c:/path/to/latlon2utm.exe",
                        logFile.path
                    )
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else if (isExternalStorageReadable) {
            Timber.e("Storage only readable")
        } else {
            Timber.e("Storage not accessible")
        }
    }

    /**
     * Checks if external storage is available for read and write.
     * This is important because the app needs to read it to get the music files,
     * but also write to it to save settings for example
     *
     * @return True if the storage is readable and writable, False if otherwise
     */
    private val isExternalStorageWritable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }

    /**
     * Checks if external storage is available to at least read
     *
     * @return True if the storage is readable, else false
     */
    private val isExternalStorageReadable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
        }

    companion object {
        // Unique ID for the media channel only!
        const val MEDIA_CHANNEL_ID = "media_channel"

        // Unique keys used to store values from settings
        const val PREFS_KEY_LIBRARY_PATHS = "lib_paths"
        const val PREFS_KEY_THEME = "theme"
        const val PREFS_KEY_VERSION = "versions"
        const val PREFS_KEY_LOGGING = "logging"
        const val PREFS_KEY_SLEEPTIME = "sleeptime"
        const val PREFS_KEY_TIMEPICKER = "timepicker"
        const val PREFS_KEY_TIMEPICKER_SWITCH = "timepicker_switch"
        const val PREFS_KEY_MENUSWITCH = "menu_switch"
        @JvmField
        val defaultPathsSet = HashSet<String>()

        // Defines all needed permissions for the app. The app will ask for these permissions
        // on the first launch
        private val PERMISSIONS_NEEDED = arrayOf(permission.READ_EXTERNAL_STORAGE, permission.RECORD_AUDIO)

        // A folder to store all album arts
        const val APP_FOLDER_ALBUMS_ART = "albumart"

        /**
         * A method to verify if the user has granted the necessary permissions to the app
         *
         * @param context The context of the app
         * @return True if granted, else False
         */
        @JvmStatic
        fun hasPermissions(context: Context): Boolean {
            Timber.i("Inspecting permissions")
            var allGranted = true
            for (permission in PERMISSIONS_NEEDED) allGranted =
                allGranted && ContextCompat.checkSelfPermission(
                    context, permission
                ) == PackageManager.PERMISSION_GRANTED
            return allGranted
        }
    }
}
package com.musicplayer.openmusic

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import com.musicplayer.openmusic.MediaPlayerUtil.stop
import com.musicplayer.openmusic.data.Song
import com.musicplayer.openmusic.data.SongsData
import com.musicplayer.openmusic.ui.main.MainActivity
import timber.log.Timber

/**
 * This class basically connects the logic of the app and what is happening there with the notification.
 * This allows users to not only see the notification, but also to interact with it.
 */
class MediaPlayerService : Service() {
    // Declaring constants
    private val binder: IBinder = LocalBinder()

    // Declaring components
    private var songPlaying: Song? = null
    private var mediaSession: MediaSessionCompat? = null
    private var isPlaying = false
    private var songsData: SongsData? = null

    /**
     * This function gets executed on creation of this class and in this case we mainly use it
     * to assign the actions declared above to each button on the notification and their key-press
     * respectively.
     */
    override fun onCreate() {
        super.onCreate()
        songsData = SongsData.getInstance(this)
        // Creates a new mediaSession with a unique tag
        mediaSession = MediaSessionCompat(this, MEDIA_SESSION_TAG)
        /*
         * This is like aan action listener for buttons on the notification or bluetooth device
         * It retrieves the pressed key and executes actions depending on what key got pressed
         */mediaSession!!.setCallback(object : MediaSessionCompat.Callback() {
            override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
                Timber.i("MediaButton has been pressed")
                // Gets the pressed key and its event (double click for example)
                val event = mediaButtonEvent.extras!!.getString(Intent.EXTRA_KEY_EVENT) as KeyEvent?
                if (event == null || event.action != KeyEvent.ACTION_UP) {
                    Timber.e("KeyPress not recognized, exiting")
                    return false
                }
                val keyCode = event.keyCode
                // Each action is set to an Intent at first
                val intent = Intent()
                when (keyCode) {
                    KeyEvent.KEYCODE_MEDIA_PAUSE -> intent.action = ACTION_PAUSE
                    KeyEvent.KEYCODE_MEDIA_PLAY -> intent.action = ACTION_PLAY
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> intent.action = ACTION_TOGGLE_PLAY_PAUSE
                    KeyEvent.KEYCODE_MEDIA_NEXT -> intent.action = ACTION_NEXT
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS -> intent.action = ACTION_PREV
                    else -> {
                        Timber.e("KeyEvent not in switch case, exiting")
                        return false
                    }
                }
                // The Intent is then sent as a broadcast to the app, and the respective listener
                // will intercept it and use it
                sendBroadcast(intent)
                return true
            }
        })
        // Simply assigns to the local variable if the mediaPlayer is currently playing
        isPlaying = MediaPlayerUtil.isPlaying
    }

    /**
     * Creates a new notification of the app
     *
     * @param intent  For communication between classes
     * @param flags   For extra parameters
     * @param startId ID of the startCommand
     * @return the constant START_NOT_STICKY
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Gets the currently playing song

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            songPlaying = intent.getSerializableExtra(EXTRA_SONG, Song::class.java)
        } else {
            @Suppress("DEPRECATION")
            songPlaying = intent.getSerializableExtra(EXTRA_SONG) as Song
        }
        // Builds the notification for it
        val notification = buildNotification()
        // Starts the notification in the foreground
        startForeground(NOTIFICATION_ID, notification)
        return START_NOT_STICKY
    }

    /**
     * Creates a new notification with the new updated values
     */
    fun refreshNotification() {
        // Same as when creating a new Notification
        songPlaying = songsData!!.songPlaying
        // This time we also check if the song is currently playing
        isPlaying = MediaPlayerUtil.isPlaying
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    /**
     * Sets all parameters and actions of the notification
     *
     * @return The new built notification
     */
    private fun buildNotification(): Notification {
        Timber.i("Building Notification...")
        // FLAG_IMMUTABLE means that once the action is set, its logic will not be altered
        // FLAG_UPDATE_CURRENT means that if the Intent already exist, it should only be updated and not recreated
        // SERVICE_REQUEST_CODE is a constant code set above
        val notificationIntent = Intent(this, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            this, SERVICE_REQUEST_CODE,
            notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )
        // Logo of the Notification
        val artwork = BitmapFactory.decodeResource(resources, R.drawable.music_combined)
        // Sets the action to get the previous song
        val prevIntent = PendingIntent.getBroadcast(
            this, SERVICE_REQUEST_CODE,
            Intent().setAction(ACTION_PREV),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        // Sets the action to play or pause the current song
        val playPauseIntent = PendingIntent.getBroadcast(
            this, SERVICE_REQUEST_CODE,
            Intent().setAction(ACTION_TOGGLE_PLAY_PAUSE),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        // Sets the action to play the next song
        val nextIntent = PendingIntent.getBroadcast(
            this, SERVICE_REQUEST_CODE,
            Intent().setAction(ACTION_NEXT),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        mediaSession!!.setMediaButtonReceiver(playPauseIntent)
        Timber.i("Notification PendingIntent have been set")

        // Creates the Notification with all actions and the frontend styling
        return NotificationCompat.Builder(this, OpenMusicApp.MEDIA_CHANNEL_ID)
            .setContentTitle(songPlaying!!.title)
            .setSmallIcon(R.drawable.ic_music)
            .setLargeIcon(artwork)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_prev, getString(R.string.notif_previous), prevIntent)
            .addAction(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                getString(if (isPlaying) R.string.notif_pause else R.string.notif_play),
                playPauseIntent
            )
            .addAction(R.drawable.ic_next, getString(R.string.notif_next), nextIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            .build()
    }

    /**
     * Gets executed if a task gets removed
     *
     * @param rootIntent The main Intent
     */
    override fun onTaskRemoved(rootIntent: Intent) {
        stop()
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    /**
     * Returns the binder
     *
     * @param intent Intent which gets bound
     * @return The binder
     */
    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    /**
     * Creates a custom binder
     */
    inner class LocalBinder : Binder() {
        val service: MediaPlayerService
            get() = this@MediaPlayerService
    }

    companion object {
        // Declaring all actions that can be executed trough the Notification
        const val ACTION_PREV = "previous"
        const val ACTION_PLAY = "play"
        const val ACTION_PAUSE = "pause"
        const val ACTION_TOGGLE_PLAY_PAUSE = "play_pause"
        const val ACTION_NEXT = "next"
        const val ACTION_CANCEL = "cancel"
        const val EXTRA_SONG = "com.musicplayer.openmusic.song"
        private const val MEDIA_SESSION_TAG = "mediaservicetag"
        private const val SERVICE_REQUEST_CODE = 9034
        private const val NOTIFICATION_ID = 181
    }
}
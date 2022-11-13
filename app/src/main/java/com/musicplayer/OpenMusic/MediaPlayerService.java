package com.musicplayer.OpenMusic;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import com.musicplayer.OpenMusic.data.Song;
import com.musicplayer.OpenMusic.data.SongsData;
import com.musicplayer.OpenMusic.ui.main.MainActivity;
import com.musicplayer.musicplayer.R;
import timber.log.Timber;

/**
 * This class basically connects the logic of the app and what is happening there with the notification.
 * This allows users to not only see the notification, but also to interact with it.
 */
public class MediaPlayerService extends Service {

    // Declaring all actions that can be executed trough the Notification
    public static final String ACTION_PREV = "previous";
    public static final String ACTION_PLAY = "play";
    public static final String ACTION_PAUSE = "pause";
    public static final String ACTION_TOGGLE_PLAY_PAUSE = "play_pause";
    public static final String ACTION_NEXT = "next";
    public static final String ACTION_CANCEL = "cancel";
    public static final String EXTRA_SONG = "com.musicplayer.OpenMusic.song";
    private static final String MEDIA_SESSION_TAG = "mediaservicetag";
    private static final int SERVICE_REQUEST_CODE = 9034;
    private static final int NOTIFICATION_ID = 181;
    // Declaring constants
    private final IBinder binder = new LocalBinder();
    // Declaring components
    private Song songPlaying;
    private MediaSessionCompat mediaSession;
    private boolean isPlaying;
    private SongsData songsData;

    /**
     * This function gets executed on creation of this class and in this case we mainly use it
     * to assign the actions declared above to each button on the notification and their key-press
     * respectively.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        songsData = SongsData.getInstance(this);
        // Creates a new mediasession with a unique tag
        mediaSession = new MediaSessionCompat(this, MEDIA_SESSION_TAG);
        /*
         * This is like aan action listener for buttons on the notification or bluetooth device
         * It retrieves the pressed key and executes actions depending on what key got pressed
         */
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                Timber.i("MediaButton has been pressed");
                // Gets the pressed key and its event (double click for example)
                KeyEvent event = (KeyEvent) mediaButtonEvent.getExtras().get(Intent.EXTRA_KEY_EVENT);
                if (event == null || event.getAction() != KeyEvent.ACTION_UP) {
                    Timber.e("KeyPress not recognized, exiting");
                    return false;
                }
                int keyCode = event.getKeyCode();
                // Each action is set to an Intent at first
                Intent intent = new Intent();
                switch (keyCode) {
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        intent.setAction(ACTION_PAUSE);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                        intent.setAction(ACTION_PLAY);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        intent.setAction(ACTION_TOGGLE_PLAY_PAUSE);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        intent.setAction(ACTION_NEXT);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        intent.setAction(ACTION_PREV);
                        break;
                    default:
                        Timber.e("KeyEvent not in switch case, exiting");
                        return false;
                }
                // The Intent is then sent as a broadcast to the app, and the respective listener
                // will intercept it and use it
                sendBroadcast(intent);
                return true;
            }

        });
        // Simply assigns to the local variable if the mediaPlayer is currently playing
        isPlaying = MediaPlayerUtil.isPlaying();
    }

    /**
     * Creates a new notification of the app
     * @param intent  For communication between classes
     * @param flags   For extra parameters
     * @param startId ID of the startCommand
     * @return the constant START_NOT_STICKY
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Gets the currently playing song
        songPlaying = (Song) intent.getSerializableExtra(EXTRA_SONG);
        // Builds the notification for it
        Notification notification = buildNotification();
        // Starts the notification in the foreground
        startForeground(NOTIFICATION_ID, notification);
        return START_NOT_STICKY;
    }

    /**
     * Gets executed if the notification gets closed
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Creates a new notification with the new updated values
     */
    public void refreshNotification() {
        // Same as when creating a new Notification
        songPlaying = songsData.getSongPlaying();
        // This time we also check if the song is currently playing
        isPlaying = MediaPlayerUtil.isPlaying();
        Notification notification = buildNotification();
        startForeground(NOTIFICATION_ID, notification);
    }

    /**
     * Sets all parameters and actions of the notification
     * @return The new built notification
     */
    private Notification buildNotification() {
        Timber.i("Building Notification...");
        // FLAG_IMMUTABLE means that once the action is set, its logic will not be altered
        // FLAG_UPDATE_CURRENT means that if the Intent already exist, it should only be updated and not recreated
        // SERVICE_REQUEST_CODE is a constant code set above
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, SERVICE_REQUEST_CODE,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        // Logo of the Notification
        Bitmap artwork = BitmapFactory.decodeResource(getResources(), R.drawable.music_combined);
        // Sets the action to get the previous song
        PendingIntent prevIntent = PendingIntent.getBroadcast(this, SERVICE_REQUEST_CODE,
                new Intent().setAction(ACTION_PREV),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        // Sets the action to play or pause the current song
        PendingIntent playPauseIntent = PendingIntent.getBroadcast(this, SERVICE_REQUEST_CODE,
                new Intent().setAction(ACTION_TOGGLE_PLAY_PAUSE),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        // Sets the action to play the next song
        PendingIntent nextIntent = PendingIntent.getBroadcast(this, SERVICE_REQUEST_CODE,
                new Intent().setAction(ACTION_NEXT),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        mediaSession.setMediaButtonReceiver(playPauseIntent);

        Timber.i("Notification PendingIntent have been set");

        // Creates the Notification with all actions and the frontend styling
        return new NotificationCompat.Builder(this, OpenMusicApp.MEDIA_CHANNEL_ID)
                .setContentTitle(songPlaying.getTitle())
                .setSmallIcon(R.drawable.ic_music)
                .setLargeIcon(artwork)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(R.drawable.ic_prev, getString(R.string.notif_previous), prevIntent)
                .addAction(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play, getString(isPlaying ? R.string.notif_pause : R.string.notif_play), playPauseIntent)
                .addAction(R.drawable.ic_next, getString(R.string.notif_next), nextIntent)
                .setStyle(new MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                )
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .setContentIntent(contentIntent)
                .build();
    }

    /**
     * Gets executed if a task gets removed
     * @param rootIntent The main Intent
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        MediaPlayerUtil.stop();
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    /**
     * Returns the binder
     * @param intent Intent which gets binded
     * @return The binder
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * Creates a custom binder
     */
    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }


}

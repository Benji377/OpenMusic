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

public class MediaPlayerService extends Service {

    // Declaring all actions
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
     * Controls Bluetooth key-pressing and assigns them their corresponding actions
     */
    @Override
    public void onCreate() {
        super.onCreate();
        songsData = SongsData.getInstance(this);
        // Creates a new mediasession with a unique tag
        mediaSession = new MediaSessionCompat(this, MEDIA_SESSION_TAG);
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                KeyEvent event = (KeyEvent) mediaButtonEvent.getExtras().get(Intent.EXTRA_KEY_EVENT);
                if (event == null || event.getAction() != KeyEvent.ACTION_UP)
                    return false;
                int keyCode = event.getKeyCode();
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
                        return false;
                }
                sendBroadcast(intent);
                return true;
            }

        });
        isPlaying = MediaPlayerUtil.isPlaying();
    }

    /**
     * Creates a new notification of the app
     *
     * @param intent  For communication between classes
     * @param flags   For extra parameters
     * @param startId ID of the startCommand
     * @return 2
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        songPlaying = (Song) intent.getSerializableExtra(EXTRA_SONG);
        Notification notification = buildNotification();
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
        songPlaying = songsData.getSongPlaying();
        isPlaying = MediaPlayerUtil.isPlaying();
        Notification notification = buildNotification();
        startForeground(NOTIFICATION_ID, notification);
    }

    /**
     * Sets all parameters and actions of the notification
     *
     * @return The new built notification
     */
    private Notification buildNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, SERVICE_REQUEST_CODE,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Bitmap artwork = BitmapFactory.decodeResource(getResources(), R.drawable.music_combined);

        PendingIntent prevIntent = PendingIntent.getBroadcast(this, SERVICE_REQUEST_CODE,
                new Intent().setAction(ACTION_PREV),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent playPauseIntent = PendingIntent.getBroadcast(this, SERVICE_REQUEST_CODE,
                new Intent().setAction(ACTION_TOGGLE_PLAY_PAUSE),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent nextIntent = PendingIntent.getBroadcast(this, SERVICE_REQUEST_CODE,
                new Intent().setAction(ACTION_NEXT),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        mediaSession.setMediaButtonReceiver(playPauseIntent);

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
     *
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
     *
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

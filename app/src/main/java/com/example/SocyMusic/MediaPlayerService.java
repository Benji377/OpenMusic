package com.example.SocyMusic;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
import com.example.musicplayer.R;

public class MediaPlayerService extends Service {

    public static final String ACTION_PREV = "previous";
    public static final String ACTION_TOGGLE_PLAY_PAUSE = "play_pause";
    public static final String ACTION_NEXT = "next";
    public static final String ACTION_CANCEL = "cancel";
    public static final String EXTRA_SONG = "com.example.SocyMusic.song";

    private Song songPlaying;
    private MediaSessionCompat mediaSession;
    private boolean isPlaying;

    private final IBinder binder = new LocalBinder();
    private static final String MEDIA_SESSION_TAG = "mediaservicetag";
    private static final int SERVICE_REQUEST_CODE = 9034;
    private static final int NOTIFICATION_ID = 181;


    @Override
    public void onCreate() {
        super.onCreate();
        mediaSession = new MediaSessionCompat(this, MEDIA_SESSION_TAG);
        isPlaying = MediaPlayerUtil.isPlaying();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        songPlaying = (Song) intent.getSerializableExtra(EXTRA_SONG);
        Notification notification = buildNotification();
        startForeground(NOTIFICATION_ID, notification);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public void refreshNotification() {
        songPlaying = SongsData.getInstance().getSongPlaying();
        isPlaying = MediaPlayerUtil.isPlaying();
        Notification notification = buildNotification();
        startForeground(NOTIFICATION_ID, notification);
    }

    private Notification buildNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, SERVICE_REQUEST_CODE, notificationIntent, 0);

        Bitmap artwork = BitmapFactory.decodeResource(getResources(), R.drawable.music);

        PendingIntent prevIntent = PendingIntent.getBroadcast(this, SERVICE_REQUEST_CODE,
                new Intent().setAction(ACTION_PREV), 0);

        PendingIntent playPauseIntent = PendingIntent.getBroadcast(this, SERVICE_REQUEST_CODE,
                new Intent().setAction(ACTION_TOGGLE_PLAY_PAUSE), 0);

        PendingIntent nextIntent = PendingIntent.getBroadcast(this, SERVICE_REQUEST_CODE,
                new Intent().setAction(ACTION_NEXT), 0);

        PendingIntent cancelIntent = PendingIntent.getBroadcast(this, SERVICE_REQUEST_CODE,
                new Intent().setAction(ACTION_CANCEL), 0);

        mediaSession.setMediaButtonReceiver(playPauseIntent);


        return new NotificationCompat.Builder(this, SocyMusicApp.MEDIA_CHANNEL_ID)
                .setContentTitle(songPlaying.getTitle())
                .setSmallIcon(R.drawable.ic_music)
                .setLargeIcon(artwork)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(R.drawable.ic_prev, "Previous", prevIntent)
                .addAction(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play, isPlaying ? "Pause" : "Play", playPauseIntent)
                .addAction(R.drawable.ic_next, "Next", nextIntent)
                .setStyle(new MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                )
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .build();
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        MediaPlayerUtil.stop();
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }


}

package com.example.SocyMusic;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import com.example.musicplayer.R;

public class SocyMusicApp extends Application {
    public static final String MEDIA_CHANNEL_ID = "media_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    MEDIA_CHANNEL_ID,
                    "Media Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel1.setDescription(getString(R.string.channel_description));

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }

}

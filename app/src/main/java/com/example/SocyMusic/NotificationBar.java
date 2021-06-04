package com.example.SocyMusic;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.musicplayer.R;

// Not working now --> Problem with Intents and icons!
public class NotificationBar {
    private static final String CHNANNEL_ID = "channel_123";
    private NotificationChannel channel;
    private NotificationManager notificationManager;
    private Context context;
    Intent previousIntent;
    Intent playIntent;
    Intent nextIntent;

    public NotificationBar(Context context) {
        this.context = context;
        previousIntent = new Intent(context, NotificationBroadcastReceiver.class);
        playIntent = new Intent(context, NotificationBroadcastReceiver.class);
        nextIntent = new Intent(context, NotificationBroadcastReceiver.class);
    }

    PlayerFragment player = new PlayerFragment();

    public String getChnannelId() {
        return CHNANNEL_ID;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public void setNotificationManager(NotificationManager manager) {
        this.notificationManager = manager;
    }

    public void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            setChannel(new NotificationChannel(getChnannelId(), name, importance));
            getChannel().setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            setNotificationManager(context.getSystemService(NotificationManager.class));
            getNotificationManager().createNotificationChannel(getChannel());
        }
    }

    public PendingIntent getPreviousPendingIntent() {
        previousIntent.putExtra("notificationId", 0);
        return PendingIntent.getActivity(context, 0, previousIntent, 0);
    }

    public PendingIntent getPlayPendingIntent() {
        playIntent.putExtra("notificationId", 1);
        return PendingIntent.getActivity(context, 0, playIntent, 0);
    }

    public PendingIntent getNextPendingIntent() {
        nextIntent.putExtra("notificationId", 2);
        return PendingIntent.getActivity(context, 0, nextIntent, 0);
    }

    public void addNotification() {
        Bitmap large_icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_icon);

        Notification notification = new NotificationCompat.Builder(context.getApplicationContext(), getChnannelId())
                // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_music)
                // Add media control buttons that invoke intents in your media service
                .addAction(R.drawable.ic_prev, "Previous", getPreviousPendingIntent()) // #0
                .addAction(R.drawable.ic_pause, "Play", getPlayPendingIntent())  // #1
                .addAction(R.drawable.ic_next, "Next", getNextPendingIntent())     // #2
                // Apply the media style template
                .setContentTitle("Now Playing")
                .setContentText(SongsData.getInstance().getSongPlaying().getTitle())
                .setLargeIcon(large_icon)
                .build();


        // Add as notification
        setNotificationManager((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
        getNotificationManager().notify(0, notification);
    }

}

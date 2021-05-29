package com.example.SocyMusic;
import android.app.NotificationManager ;
import android.content.BroadcastReceiver ;
import android.content.Context ;
import android.content.Intent ;
public class NotificationBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive (Context context , Intent intent) {
        int notificationId = intent.getIntExtra( "notificationId" , 1 ) ;
        PlayerActivity pactivity = new PlayerActivity();

        if (notificationId == 0) {
            pactivity.previousSong_button.performClick();
        } else if (notificationId == 1) {
            pactivity.playSong_button.performClick();
        } else if (notificationId == 2) {
            pactivity.nextSong_button.performClick();
        } else {
            // if you want cancel notification
            NotificationManager manager = (NotificationManager) context.getSystemService(Context. NOTIFICATION_SERVICE ) ;
            manager.cancel(notificationId) ;
        }
    }
}
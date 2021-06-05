package com.example.SocyMusic;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.musicplayer.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class MainActivity extends AppCompatActivity implements PlayerFragment.OnCompleteListener {
    ListView listView;
    BottomSheetBehavior<FrameLayout> bottomSheetBehavior;
    View songInfoPane;
    TextView songTitleTextView;
    Button playButton;

    private static final String CHNANNEL_ID = "channel_123";
    private static final int NOTIFICATION_ID = 181;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("SocyMusic");


        listView = findViewById(R.id.listViewSong);
        songInfoPane = findViewById(R.id.song_info_pane);
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.player_bottom_sheet));
        songTitleTextView = findViewById(R.id.bsht_song_name_txt);
        songTitleTextView.setSelected(true);

        final FrameLayout playerContainer = findViewById(R.id.player_fragment_container);
        playerContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                playerContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                bottomSheetBehavior.setPeekHeight(songInfoPane.getHeight());
            }
        });

        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED)
                    actionBar.setTitle("Now Playing");
                else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    actionBar.setTitle("SocyMusic");
                    TextView realSongNameTextView = playerContainer.findViewById(R.id.txtsongname);
                    if (!realSongNameTextView.getText().equals(songTitleTextView.getText()))
                        songTitleTextView.setText(realSongNameTextView.getText());
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                songInfoPane.setAlpha(1f - slideOffset);
            }
        });

        songInfoPane.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        playButton = findViewById(R.id.bsh_play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button realPlayButton = playerContainer.findViewById(R.id.playbtn);
                realPlayButton.callOnClick();
                playButton.setBackground(realPlayButton.getBackground());
            }
        });
        runtimePermission();

    }

    @Override
    // Is the option menu you see in the top left corner (3 dots)
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    // Creates the options available and what happens if you click on them
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // To add an item to the menu, add it to menu/main.xml first!

        if (item.getItemId() == R.id.credits) {
            onButtonShowPopupWindowClick(listView);
        } else if (item.getItemId() == R.id.download) {
            // Replace this action
            Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.settings) {
            // Replace this action
            Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        else
            super.onBackPressed();

    }

    public void runtimePermission() {
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        displaySongs();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }


    void displaySongs() {
        customAdapter customAdapter = new customAdapter();
        listView.setAdapter(customAdapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (!SongsData.getInstance().songExists(position)) {
                Toast.makeText(this, "File moved or deleted.", Toast.LENGTH_LONG).show();
                SongsData.getInstance().reloadSongs();
                customAdapter.notifyDataSetChanged();
                return;
            }

            SongsData.getInstance().playAllFrom(position);
            Song songClicked = SongsData.getInstance().getSongPlaying();
            songTitleTextView.setText(songClicked.getTitle());

            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment playerFragment = fragmentManager.findFragmentById(R.id.player_fragment_container);
            if (playerFragment == null) {
                playerFragment = PlayerFragment.newInstance();
                fragmentManager.beginTransaction().add(R.id.player_fragment_container, playerFragment).commit();
            } else {
                ((PlayerFragment) playerFragment).updateSongPlaying();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }

            createNotification();
        });
        TextView emptyText = findViewById(R.id.listEmptyTextView);
        listView.setEmptyView(emptyText);
    }

    @Override
    public void onLoadComplete() {
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }


    class customAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return SongsData.getInstance().songsCount();
        }

        @Override
        public Object getItem(int position) {
            return SongsData.getInstance().getSongAt(position).getTitle();
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View myView = getLayoutInflater().inflate(R.layout.list_item, null);
            TextView textsong = myView.findViewById(R.id.textsongname);
            textsong.setText(SongsData.getInstance().getSongAt(position).getTitle());
            textsong.setSelected(true);
            return myView;
        }
    }


    // Experimenting with Notifications
    private void createNotification() {

        Log.e("Notification", "createNotification()");

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHNANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        Bitmap large_icon = BitmapFactory.decodeResource(this.getResources(), R.drawable.app_icon);

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHNANNEL_ID)
                // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_music)
                // Add media control buttons that invoke intents in your media service
                // Apply the media style template
                .setContentTitle("Now Playing")
                //.setContentText(SongsData.getInstance().getSongPlaying().getTitle())
                .setContentText("ContentText")
                .setLargeIcon(large_icon)
                .build();


        // Add as notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    // Shows a pop up window
    public void onButtonShowPopupWindowClick(View view) {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        popupView.setElevation(20);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }

}

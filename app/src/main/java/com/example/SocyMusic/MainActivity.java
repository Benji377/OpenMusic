package com.example.SocyMusic;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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

public class MainActivity extends AppCompatActivity implements PlayerFragment.PlayerFragmentHost, ServiceConnection {
    ListView listView;
    BottomSheetBehavior<FrameLayout> bottomSheetBehavior;
    View songInfoPane;
    TextView songTitleTextView;
    Button playButton;

    private PlayerFragment playerFragment;
    private MediaPlayerService mediaPlayerService;
    private MediaPlayerReceiver mediaPlayerReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.app_name));


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
                    songTitleTextView.setText(SongsData.getInstance().getSongPlaying().getTitle());
                    playButton.setBackgroundResource(mediaPlayerService.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                songInfoPane.setAlpha(1f - slideOffset);
            }
        });

        songInfoPane.setOnClickListener(v -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));

        playButton = findViewById(R.id.bsh_play_button);
        playButton.setOnClickListener(v -> {
            playerFragment.togglePlayPause();
            playButton.setBackgroundResource(mediaPlayerService.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
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
            Fragment fragment = fragmentManager.findFragmentById(R.id.player_fragment_container);
            if (fragment == null) {
                playerFragment = PlayerFragment.newInstance();
                fragmentManager.beginTransaction().add(R.id.player_fragment_container, playerFragment).commit();

                Intent serviceIntent = new Intent(this, MediaPlayerService.class);
                serviceIntent.putExtra(MediaPlayerService.EXTRA_SONG, songClicked);
                startService(serviceIntent);
                bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
            } else {
                playerFragment = (PlayerFragment) fragment;
                playerFragment.updateSongPlaying();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }

        });
        TextView emptyText = findViewById(R.id.listEmptyTextView);
        listView.setEmptyView(emptyText);
    }

    @Override
    public void onLoadComplete() {
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onTogglePlayPause() {
        if (mediaPlayerService != null)
            mediaPlayerService.togglePlayPause();
    }

    @Override
    public void onSwitchTrack(Song song) {
        if (mediaPlayerService != null)
            mediaPlayerService.updateSong(song);
    }

    @Override
    protected void onPause() {
        if (isFinishing()) {
            unbindService(this);
            if (mediaPlayerService != null)
                mediaPlayerService.stopSelf();
            PlayerFragment.mediaPlayer.stop();
            PlayerFragment.mediaPlayer.release();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayerReceiver == null)
            mediaPlayerReceiver = new MediaPlayerReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MediaPlayerService.ACTION_PREV);
        intentFilter.addAction(MediaPlayerService.ACTION_TOGGLE_PLAY_PAUSE);
        intentFilter.addAction(MediaPlayerService.ACTION_NEXT);
        intentFilter.addAction(MediaPlayerService.ACTION_CANCEL);
        registerReceiver(mediaPlayerReceiver, intentFilter);
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            songTitleTextView.setText(SongsData.getInstance().getSongPlaying().getTitle());
            playButton.setBackgroundResource(mediaPlayerService.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        }
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

    private class MediaPlayerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case MediaPlayerService.ACTION_PREV:
                    playerFragment.playPrevSong();
                    mediaPlayerService.updateSong(SongsData.getInstance().getSongPlaying());
                    break;
                case MediaPlayerService.ACTION_TOGGLE_PLAY_PAUSE:
                    playerFragment.togglePlayPause();
                    playButton.setBackgroundResource(mediaPlayerService.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
                    break;
                case MediaPlayerService.ACTION_NEXT:
                    playerFragment.playNextSong();
                    mediaPlayerService.updateSong(SongsData.getInstance().getSongPlaying());
                    break;
                case MediaPlayerService.ACTION_CANCEL:
                    mediaPlayerService.stopSelf();
                    break;
            }
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder iBinder) {
        mediaPlayerService = ((MediaPlayerService.LocalBinder) iBinder).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mediaPlayerService = null;
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
        popupView.setOnTouchListener((v, event) -> {
            popupWindow.dismiss();
            return true;
        });
    }

}

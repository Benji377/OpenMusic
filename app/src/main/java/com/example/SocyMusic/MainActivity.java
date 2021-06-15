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

import com.example.musicplayer.BuildConfig;
import com.example.musicplayer.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.List;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class MainActivity extends AppCompatActivity implements PlayerFragment.PlayerFragmentHost, QueueFragment.QueueFragmentHost, ServiceConnection {
    ListView listView;
    BottomSheetBehavior<FrameLayout> bottomSheetBehavior;
    View songInfoPane;
    TextView songTitleTextView;
    Button playButton;

    private PlayerFragment playerFragment;
    private QueueFragment queueFragment;
    private MediaPlayerService mediaPlayerService;
    private MediaPlayerReceiver mediaPlayerReceiver;
    private ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();
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
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    invalidateOptionsMenu();
                    actionBar.setTitle(R.string.now_playing);
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    invalidateOptionsMenu();
                    actionBar.setTitle(R.string.app_name);
                    songTitleTextView.setText(SongsData.getInstance().getSongPlaying().getTitle());
                    playButton.setBackgroundResource(MediaPlayerUtil.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
                    hideQueue();
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
            playButton.setBackgroundResource(MediaPlayerUtil.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        });
        runtimePermission();

    }

    @Override
    // Is the option menu you see in the top left corner (3 dots)
    public boolean onCreateOptionsMenu(Menu menu) {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN || bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)
            getMenuInflater().inflate(R.menu.main, menu);
        else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            getMenuInflater().inflate(R.menu.playing, menu);
            MenuItem showQueueButton = menu.findItem(R.id.menu_show_playing_queue);
            if (queueFragment == null)
                showQueueButton.setIcon(R.drawable.ic_queue);
            else
                showQueueButton.setIcon(R.drawable.ic_queue_selected);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    // Creates the options available and what happens if you click on them
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // To add an item to the menu, add it to menu/main.xml first!
        if (item.getItemId() == R.id.about) {
            showPopupWindow(listView);

        } else if (item.getItemId() == R.id.download) {
            Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.playlist) {
            // Replace this action
            Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.menu_show_playing_queue) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentById(R.id.queue_fragment_container);
            if (fragment == null)
                showQueue();
            else
                hideQueue();
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideQueue() {
        if (queueFragment == null)
            return;

        FragmentManager fragmentManager = getSupportFragmentManager();
        View playerFragmentView = findViewById(R.id.player_holder);
        queueFragment.onDestroyView();
        fragmentManager.beginTransaction().remove(queueFragment).commit();

        playerFragmentView.setVisibility(View.VISIBLE);
        playerFragment.initializeVisualizer();
        playerFragment.updatePlayerUI();
        actionBar.setTitle(R.string.now_playing);
        queueFragment = null;
        invalidateOptionsMenu();
    }

    private void showQueue() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        View playerFragmentView = findViewById(R.id.player_holder);
        queueFragment = new QueueFragment();
        fragmentManager.beginTransaction().add(R.id.queue_fragment_container, queueFragment).commit();
        actionBar.setTitle(R.string.playing_queue);
        playerFragment.releaseVisualizer();
        playerFragmentView.setVisibility(View.INVISIBLE);
        invalidateOptionsMenu();
    }

    @Override
    public void onBackPressed() {
        if (queueFragment != null)
            hideQueue();
        else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
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

        // Loading files from SD-Card
        File[] storages = getApplicationContext().getExternalFilesDirs(null);
        SongsData.getInstance().addSongs(storages);

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
                MediaPlayerUtil.startPlaying(this, SongsData.getInstance().getSongPlaying());
                if (mediaPlayerService != null)
                    mediaPlayerService.refreshNotification();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                hideQueue();
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
    public void onPlaybackUpdate() {
        if (mediaPlayerService != null)
            mediaPlayerService.refreshNotification();
        playButton.setBackgroundResource(MediaPlayerUtil.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        playerFragment.updatePlayButton();
    }

    @Override
    public void onSongUpdate() {
        if (mediaPlayerService != null)
            mediaPlayerService.refreshNotification();
        songTitleTextView.setText(SongsData.getInstance().getSongPlaying().getTitle());
        playerFragment.updatePlayerUI();
    }

    @Override
    protected void onPause() {
        if (isFinishing()) {
            unbindService(this);
            if (mediaPlayerService != null)
                mediaPlayerService.stopSelf();
            MediaPlayerUtil.stop();
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
            playButton.setBackgroundResource(MediaPlayerUtil.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        }
        if (playerFragment != null)
            playerFragment.updatePlayerUI();
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
            String action = intent.getAction();
            switch (action) {
                case MediaPlayerService.ACTION_PREV:
                    MediaPlayerUtil.playPrev(MainActivity.this);
                    mediaPlayerService.refreshNotification();
                    if (playerFragment != null)
                        playerFragment.updatePlayerUI();
                    if (queueFragment != null)
                        queueFragment.updateSong();
                    break;
                case MediaPlayerService.ACTION_TOGGLE_PLAY_PAUSE:
                    MediaPlayerUtil.togglePlayPause();
                    mediaPlayerService.refreshNotification();
                    playButton.setBackgroundResource(MediaPlayerUtil.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
                    if (playerFragment != null)
                        playerFragment.updatePlayButton();
                    break;
                case MediaPlayerService.ACTION_NEXT:
                    MediaPlayerUtil.playNext(MainActivity.this);
                    mediaPlayerService.refreshNotification();
                    if (playerFragment != null)
                        playerFragment.updatePlayerUI();
                    if (queueFragment != null)
                        queueFragment.updateSong();
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

    public void showPopupWindow(View view) {

        // Reference:
        // https://blog.fossasia.org/creating-an-awesome-about-us-page-for-the-open-event-organizer-android-app/
        // https://github.com/medyo/android-about-page

        View popupView = new AboutPage(MainActivity.this, R.style.Widget_App_AboutPage)
                .isRTL(false)
                .setImage(R.mipmap.ic_launcher)
                .setDescription(getString(R.string.about_us_description))
                .addItem(new Element("Version " + BuildConfig.VERSION_NAME, R.drawable.ic_info))
                .addGroup("Connect with us")
                .addWebsite("https://benji377.github.io/SocyMusic/")
                .addGitHub("Benji377/SocyMusic")
                .create();

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

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

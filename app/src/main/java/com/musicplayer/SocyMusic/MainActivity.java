package com.musicplayer.SocyMusic;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.util.TypedValue;
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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
<<<<<<< Updated upstream
import androidx.coordinatorlayout.widget.CoordinatorLayout;
=======
import androidx.appcompat.app.AppCompatDelegate;
>>>>>>> Stashed changes
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.musicplayer.musicplayer.BuildConfig;
import com.musicplayer.musicplayer.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class MainActivity extends AppCompatActivity implements PlayerFragment.PlayerFragmentHost, QueueFragment.QueueFragmentHost, ServiceConnection, ActivityResultCallback<ActivityResult> {

    ListView listView;
    BottomSheetBehavior<FrameLayout> bottomSheetBehavior;
    View songInfoPane;
    TextView songTitleTextView;
    Button playButton;

    // Private components
    private ActivityResultLauncher<Intent> resultLauncher;
    private PlayerFragment playerFragment;
    private QueueFragment queueFragment;
    private MediaPlayerService mediaPlayerService;
    private MediaPlayerReceiver mediaPlayerReceiver;
    private ActionBar actionBar;
    private SongsData songsData;

    /**
     * Gets executed every time the app starts
     *
     * @param savedInstanceState Android standard
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        songsData = SongsData.getInstance(this);
        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.all_app_name));

        // Sets all components
        listView = findViewById(R.id.listview_main_songs);
        songInfoPane = findViewById(R.id.layout_main_song_info_pane);
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet_main_player));
        songTitleTextView = findViewById(R.id.textview_main_song_title);
        songTitleTextView.setSelected(true);

        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);

        // Creates a connection to the player fragment
        final FrameLayout playerContainer = findViewById(R.id.layout_main_player_container);
        playerContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                playerContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                bottomSheetBehavior.setPeekHeight(songInfoPane.getHeight());
            }
        });

        // Creates the bottom navigation sheet and sets its behaviour
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            // Controls if the sheet changed state
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    invalidateOptionsMenu();
                    actionBar.setTitle(R.string.player_title);
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    invalidateOptionsMenu();
                    actionBar.setTitle(R.string.all_app_name);
                    songTitleTextView.setText(songsData.getSongPlaying().getTitle());
                    playButton.setBackgroundResource(MediaPlayerUtil.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);

                    ((ViewGroup.MarginLayoutParams) listView.getLayoutParams()).bottomMargin = dpToPixel(50);

                    hideQueue();
                } else if (newState == BottomSheetBehavior.STATE_HIDDEN)
                    ((ViewGroup.MarginLayoutParams) listView.getLayoutParams()).bottomMargin = dpToPixel(0);
            }

            @Override
            // If user slides to the bottom on the sheet
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                songInfoPane.setAlpha(1f - slideOffset);
            }
        });

        // Sets action for the infoPane
        songInfoPane.setOnClickListener(v -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));

        // Sets the playButton
        playButton = findViewById(R.id.button_main_play_pause);
        playButton.setOnClickListener(v -> {
            playerFragment.togglePlayPause();
            playButton.setBackgroundResource(MediaPlayerUtil.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        });
        // Checks for all the required permissions
        runtimePermission();
    }

    /**
     * Creates the option menu you can see in the upper right corner (three dots)
     *
     * @param menu The menu to be created
     * @return The finished created menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN || bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)
            getMenuInflater().inflate(R.menu.main, menu);
        else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            getMenuInflater().inflate(R.menu.playing, menu);
            MenuItem showQueueButton = menu.findItem(R.id.playing_menu_show_queue);
            if (queueFragment == null)
                showQueueButton.setIcon(R.drawable.ic_queue);
            else
                showQueueButton.setIcon(R.drawable.ic_queue_selected);
        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Creates all available options and sets the action to be performed when the suer clicks on them
     *
     * @param item Item of the menu
     * @return Which item has been selected
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // To add an item to the menu, add it to menu/main.xml first!
        if (item.getItemId() == R.id.main_menu_about) {
            showPopupWindow(listView);
        } else if (item.getItemId() == R.id.main_menu_playlist) {
            // TODO: Add Playlist fragment call here
            Toast.makeText(this, getText(R.string.all_coming_soon), Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.main_menu_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            resultLauncher.launch(settingsIntent);
        } else if (item.getItemId() == R.id.playing_menu_show_queue) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.layout_main_queue_container);
            if (fragment == null)
                showQueue();
            else {
                hideQueue();
                playerFragment.updatePlayerUI();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideQueue() {
        if (queueFragment == null)
            return;
        FragmentManager fragmentManager = getSupportFragmentManager();
        View playerFragmentView = findViewById(R.id.layout_player_holder);
        queueFragment.onDestroyView();
        fragmentManager.beginTransaction().remove(queueFragment).commit();

        playerFragmentView.setVisibility(View.VISIBLE);
        playerFragment.initializeVisualizer();
        actionBar.setTitle(R.string.player_title);
        queueFragment = null;
        invalidateOptionsMenu();
    }

    private void showQueue() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        View playerFragmentView = findViewById(R.id.layout_player_holder);
        queueFragment = new QueueFragment();
        fragmentManager.beginTransaction().add(R.id.layout_main_queue_container, queueFragment).commit();
        actionBar.setTitle(R.string.queue_title);
        playerFragment.releaseVisualizer();
        playerFragmentView.setVisibility(View.INVISIBLE);
        invalidateOptionsMenu();
    }

    /**
     * Sets what happens if the user presses the 'back'-key
     */
    @Override
    public void onBackPressed() {
        if (queueFragment != null)
            hideQueue();
        else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        else
            super.onBackPressed();

    }

    /**
     * Checks for all required permissions
     * For musicplayer storage permission to find all the songs and record permission for the visualizer
     */
    public void runtimePermission() {
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        // Display all the songs
                        songsData.reloadSongs(MainActivity.this);
                        displaySongs();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        // Ask again and again until permissions are accepted
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    /**
     * This method searches for all songs it can find on the phone's storage and shows them as a list
     */
    void displaySongs() {
        CustomAdapter customAdapter = new CustomAdapter();
        listView.setAdapter(customAdapter);

        // If you click on an tem in the list, the player fragment opens
        listView.setOnItemClickListener((parent, view, position, id) -> {

            // Error occured
            if (!songsData.songExists(position)) {
                Toast.makeText(this, getText(R.string.main_err_file_gone), Toast.LENGTH_LONG).show();
                songsData.reloadSongs(this);
                customAdapter.notifyDataSetChanged();
                return;
            }
            // Adds all the songs to the queue from that position onward
            songsData.playAllFrom(position);
            // Plays the selected song
            Song songClicked = songsData.getSongPlaying();
            songTitleTextView.setText(songClicked.getTitle());

            // Opens the player fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentById(R.id.layout_main_player_container);
            if (fragment == null) {
                playerFragment = PlayerFragment.newInstance();
                fragmentManager.beginTransaction().add(R.id.layout_main_player_container, playerFragment).commit();

                Intent serviceIntent = new Intent(this, MediaPlayerService.class);
                serviceIntent.putExtra(MediaPlayerService.EXTRA_SONG, songClicked);
                startService(serviceIntent);
                bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
            } else {
                playerFragment = (PlayerFragment) fragment;
                MediaPlayerUtil.startPlaying(this, songsData.getSongPlaying());
                onSongUpdate();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                hideQueue();
            }

        });
        // Error occurs --> song not found
        TextView emptyText = findViewById(R.id.textview_main_list_empty);
        listView.setEmptyView(emptyText);
    }

    /**
     * When the player fragment finishes loading
     * Must be implemented!
     */
    @Override
    public void onLoadComplete() {
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    /**
     * If the song has changed state, the notification needs to be updated
     * This method refreshes the notification and sets the buttons accordingly
     */
    @Override
    public void onPlaybackUpdate() {
        if (mediaPlayerService != null)
            mediaPlayerService.refreshNotification();
        playButton.setBackgroundResource(MediaPlayerUtil.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        playerFragment.updatePlayButton();
    }

    /**
     * If a completely new song is being played, this method updates the notification and the text
     * accordingly
     */
    @Override
    public void onSongUpdate() {
        if (mediaPlayerService != null)
            mediaPlayerService.refreshNotification();
        songTitleTextView.setText(songsData.getSongPlaying().getTitle());
        playButton.setBackgroundResource(R.drawable.ic_pause);
        playerFragment.updatePlayerUI();
        if (queueFragment != null)
            queueFragment.updateSong();
    }

    /**
     * If playing is paused this method stops the mediaplayer service
     */
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

    /**
     * Basically gets executed when the app gets resumed. which means when it is closed and reopened
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Creates a new mediaplayer receiver if none exist
        if (mediaPlayerReceiver == null)
            mediaPlayerReceiver = new MediaPlayerReceiver();
        // Sets all intents for actions
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MediaPlayerService.ACTION_PREV);
        intentFilter.addAction(MediaPlayerService.ACTION_PLAY);
        intentFilter.addAction(MediaPlayerService.ACTION_PAUSE);
        intentFilter.addAction(MediaPlayerService.ACTION_TOGGLE_PLAY_PAUSE);
        intentFilter.addAction(MediaPlayerService.ACTION_NEXT);
        intentFilter.addAction(MediaPlayerService.ACTION_CANCEL);
        registerReceiver(mediaPlayerReceiver, intentFilter);

        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            songTitleTextView.setText(songsData.getSongPlaying().getTitle());
            playButton.setBackgroundResource(MediaPlayerUtil.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
        }
        if (playerFragment != null)
            playerFragment.updatePlayerUI();
    }

    @Override
    public void onActivityResult(ActivityResult result) {
        //TODO: check if settings were changed before updating
//        if (result.getResultCode() != RESULT_OK)
//            return;
        ((CustomAdapter) listView.getAdapter()).notifyDataSetChanged();
    }

    /**
     * Custom adapter for SongsData related actions
     */
    class CustomAdapter extends BaseAdapter {

        /**
         * Gets the amount of songs
         *
         * @return The number of songs
         */
        @Override
        public int getCount() {
            return songsData.songsCount();
        }

        /**
         * Gets the song as object in a defined position in the queue
         *
         * @param position Position to search for the song
         * @return The song as an object
         */
        @Override
        public Object getItem(int position) {
            return songsData.getSongAt(position).getTitle();
        }

        /**
         * Not fully implemented yet!
         * Returns the id of a song at a given position
         *
         * @param position The position of the song in the queue
         * @return The ID of the song
         */
        @Override
        public long getItemId(int position) {
            return 0;
        }

        /**
         * Returns the view of the list_item
         *
         * @param position    Position of the song in the queue
         * @param convertView Unused
         * @param parent      Root view
         * @return The view of the song
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View myView = getLayoutInflater().inflate(R.layout.list_item_main, null);
            TextView textsong = myView.findViewById(R.id.textview_main_item_song_title);
            textsong.setText(songsData.getSongAt(position).getTitle());
            textsong.setSelected(true);
            return myView;
        }
    }

    /**
     * Extends the standard Broadcastreceiver to create a new receiver for the mediaplayer
     */
    private class MediaPlayerReceiver extends BroadcastReceiver {

        /**
         * Sets what should happen when the receiver gets a signal
         *
         * @param context Context of the app
         * @param intent  Intent to get the action from
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // Checks for different actions
            switch (action) {
                case MediaPlayerService.ACTION_PREV:
                    MediaPlayerUtil.playPrev(MainActivity.this);
                    onSongUpdate();
                    break;
                case MediaPlayerService.ACTION_PLAY:
                case MediaPlayerService.ACTION_PAUSE:
                case MediaPlayerService.ACTION_TOGGLE_PLAY_PAUSE:
                    if (action.equals(MediaPlayerService.ACTION_PLAY))
                        MediaPlayerUtil.play();
                    else if (action.equals(MediaPlayerService.ACTION_PAUSE))
                        MediaPlayerUtil.pause();
                    else
                        MediaPlayerUtil.togglePlayPause();
                    mediaPlayerService.refreshNotification();
                    playButton.setBackgroundResource(MediaPlayerUtil.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
                    if (playerFragment != null)
                        playerFragment.updatePlayButton();
                    break;
                case MediaPlayerService.ACTION_NEXT:
                    MediaPlayerUtil.playNext(MainActivity.this);
                    onSongUpdate();
                    break;
                case MediaPlayerService.ACTION_CANCEL:
                    mediaPlayerService.stopSelf();
                    break;
            }

        }
    }

    /**
     * Sets what happens if the service connects
     *
     * @param name    Name of the service
     * @param iBinder Binder for the service
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder iBinder) {
        mediaPlayerService = ((MediaPlayerService.LocalBinder) iBinder).getService();
    }

    /**
     * Sets what happens if the service disconnects
     *
     * @param name Name of the service
     */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        mediaPlayerService = null;
    }

    /**
     * Creates a popUp window, in this case specifically for the About-menu option
     *
     * @param view The view at which the popup should be shown
     */
    public void showPopupWindow(View view) {
        // Reference:
        // https://blog.fossasia.org/creating-an-awesome-about-us-page-for-the-open-event-organizer-android-app/
        // https://github.com/medyo/android-about-page
        View popupView = new AboutPage(MainActivity.this, R.style.Widget_App_AboutPage)
                .isRTL(false)
                .setImage(R.mipmap.ic_launcher)
                .setDescription(getString(R.string.about_description))
                .addItem(new Element(getString(R.string.about_version, BuildConfig.VERSION_NAME), R.drawable.ic_info))
                .addGroup("Connect with us")
                .addWebsite("https://benji377.github.io/SocyMusic/")
                .addGitHub("Benji377/SocyMusic")
                .create();

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

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

    private int dpToPixel(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics());
    }
}

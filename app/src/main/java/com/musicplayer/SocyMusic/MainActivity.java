package com.musicplayer.SocyMusic;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.musicplayer.musicplayer.BuildConfig;
import com.musicplayer.musicplayer.R;

import java.util.List;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class MainActivity extends AppCompatActivity implements PlayerFragment.PlayerFragmentHost, QueueFragment.QueueFragmentHost, ServiceConnection, ActivityResultCallback<ActivityResult> {

    private ListView songsListView;
    private BottomSheetBehavior<FrameLayout> bottomSheetBehavior;
    private ViewPager2 songInfoPager;

    // Private components
    private ActivityResultLauncher<Intent> resultLauncher;
    private PlayerFragment playerFragment;
    private QueueFragment queueFragment;
    private MediaPlayerService mediaPlayerService;
    private MediaPlayerReceiver mediaPlayerReceiver;
    private ActionBar actionBar;
    private SongsData songsData;

    private boolean scrollTriggeredByCode;

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
        songsListView = findViewById(R.id.listview_main_songs);
        songInfoPager = findViewById(R.id.viewpager_main_info_panes);
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet_main_player));

        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);

        // Creates a connection to the player fragment
        final FrameLayout playerContainer = findViewById(R.id.layout_main_player_container);
        playerContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                playerContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                bottomSheetBehavior.setPeekHeight(songInfoPager.getHeight());
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
                    songInfoPager.setUserInputEnabled(false);
//                    songInfoPager.findViewWithTag(songInfoPager.getCurrentItem()).setClickable(false);
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    invalidateOptionsMenu();
                    actionBar.setTitle(R.string.all_app_name);
                    ((ViewGroup.MarginLayoutParams) songsListView.getLayoutParams()).bottomMargin = dpToPixel(50);
                    songInfoPager.setUserInputEnabled(true);
//                    songInfoPager.findViewWithTag(songInfoPager.getCurrentItem()).setClickable(true);
                    hideQueue();
                } else if (newState == BottomSheetBehavior.STATE_HIDDEN)
                    ((ViewGroup.MarginLayoutParams) songsListView.getLayoutParams()).bottomMargin = dpToPixel(0);
            }

            @Override
            // If user slides to the bottom on the sheet
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                songInfoPager.setAlpha(1f - slideOffset);
            }
        });

        // Sets action for the infoPane
        songInfoPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            private int previousPosition;
            private boolean newPageSelected;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                newPageSelected = position != previousPosition;
                previousPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager.SCROLL_STATE_IDLE && newPageSelected && !scrollTriggeredByCode) {
                    newPageSelected = false;
                    int position = songInfoPager.getCurrentItem();
                    songsData.setPlayingIndex(position);
                    MediaPlayerUtil.playCurrent(MainActivity.this);
                    onSongUpdate();
                }
                if (scrollTriggeredByCode && state == ViewPager.SCROLL_STATE_IDLE) {
                    scrollTriggeredByCode = false;
                    newPageSelected = false;
                }

            }
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
            showPopupWindow(songsListView);
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
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
        SongListAdapter customAdapter = new SongListAdapter();
        songsListView.setAdapter(customAdapter);

        // If you click on an tem in the list, the player fragment opens
        songsListView.setOnItemClickListener((parent, view, position, id) -> {

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
        songsListView.setEmptyView(emptyText);
    }

    /**
     * When the player fragment finishes loading
     * Must be implemented!
     */
    @Override
    public void onLoadComplete() {
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        songInfoPager.setAdapter(new InfoPanePagerAdapter(songsData.getPlayingQueue()));
        if (songsData.getPlayingIndex() != 0) {
            scrollTriggeredByCode = true;
            songInfoPager.setCurrentItem(songsData.getPlayingIndex());
        }
    }

    /**
     * If the song has changed state, the notification needs to be updated
     * This method refreshes the notification and sets the buttons accordingly
     */
    @Override
    public void onPlaybackUpdate() {
        if (mediaPlayerService != null)
            mediaPlayerService.refreshNotification();
        if (playerFragment != null)
            playerFragment.updatePlayButton();
        int currentItem = songInfoPager.getCurrentItem();
        songInfoPager.getAdapter().notifyItemChanged(currentItem, songsData.getSongFromQueueAt(currentItem));
    }

    /**
     * If a completely new song is being played, this method updates the notification and the text
     * accordingly
     */
    @Override
    public void onSongUpdate() {
        if (mediaPlayerService != null)
            mediaPlayerService.refreshNotification();
        if (queueFragment != null)
            queueFragment.updateQueue();
        else if (playerFragment != null)
            playerFragment.updatePlayerUI();

        InfoPanePagerAdapter pagerAdapter = (InfoPanePagerAdapter) songInfoPager.getAdapter();
        //determine if queue changed or if simple scroll happened
        if (pagerAdapter.getQueue() != songsData.getPlayingQueue())
            pagerAdapter.updateQueue(songsData.getPlayingQueue());

        scrollTriggeredByCode = true;
        songInfoPager.setCurrentItem(songsData.getPlayingIndex());
        songInfoPager.getAdapter().notifyItemChanged(songInfoPager.getCurrentItem(), songsData.getSongPlaying());
    }

    @Override
    public void onQueueReordered() {
        songInfoPager.getAdapter().notifyDataSetChanged();
        playerFragment.invalidatePager();
        scrollTriggeredByCode = true;
        songInfoPager.setCurrentItem(songsData.getPlayingIndex());
    }

    @Override
    public void onShuffle() {
        if (queueFragment != null)
            queueFragment.updateQueue();
        InfoPanePagerAdapter pagerAdapter = (InfoPanePagerAdapter) songInfoPager.getAdapter();
        pagerAdapter.updateQueue(songsData.getPlayingQueue());
        playerFragment.invalidatePager();
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

        if (playerFragment == null) {
            playerFragment = (PlayerFragment) getSupportFragmentManager().findFragmentById(R.id.layout_main_player_container);
        }
        if (playerFragment != null)
            playerFragment.updatePlayerUI();

    }

    @Override
    public void onActivityResult(ActivityResult result) {
        ((SongListAdapter) songsListView.getAdapter()).notifyDataSetChanged();
    }

    /**
     * Custom adapter for SongsData related actions
     */
    class SongListAdapter extends BaseAdapter {

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
            return songsData.getSongAt(position).hashCode();
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

    class InfoPanePagerAdapter extends RecyclerView.Adapter<InfoPaneHolder> {
        List<Song> queue;

        public InfoPanePagerAdapter(List<Song> queue) {
            this.queue = queue;
        }

        @NonNull
        @Override
        public InfoPaneHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = getLayoutInflater().inflate(R.layout.pager_item_song_pane, parent, false);
            return new InfoPaneHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull InfoPaneHolder holder, int position) {
            holder.itemView.setTag(position);
            Song song = queue.get(position);
            holder.bind(song);
        }

        @Override
        public int getItemCount() {
            return songsData.getPlayingQueueCount();
        }

        public void updateQueue(List<Song> queue) {
            this.queue = queue;
            notifyDataSetChanged();
        }

        public List<Song> getQueue() {
            return queue;
        }
    }

    class InfoPaneHolder extends RecyclerView.ViewHolder {
        private TextView songTitleTextView;
        private Button playPauseButton;

        public InfoPaneHolder(@NonNull View itemView) {
            super(itemView);
            songTitleTextView = itemView.findViewById(R.id.textview_song_pane_item_title);
            playPauseButton = itemView.findViewById(R.id.button_song_pane_item_play_pause);
            playPauseButton.setOnClickListener(v -> playerFragment.togglePlayPause());
            itemView.setOnClickListener(v -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));
        }

        public void bind(Song song) {
            songTitleTextView.setText(song.getTitle());
            playPauseButton.setBackgroundResource(MediaPlayerUtil.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
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
                    onPlaybackUpdate();
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
        // which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        /*
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
         */
    }

    private int dpToPixel(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics());
    }
}

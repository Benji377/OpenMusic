package com.musicplayer.SocyMusic.ui.main;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.musicplayer.SocyMusic.MediaPlayerService;
import com.musicplayer.SocyMusic.MediaPlayerUtil;
import com.musicplayer.SocyMusic.SocyMusicApp;
import com.musicplayer.SocyMusic.Song;
import com.musicplayer.SocyMusic.SongsData;
import com.musicplayer.SocyMusic.ui.all_songs.AllSongsFragment;
import com.musicplayer.SocyMusic.ui.player.PlayerFragment;
import com.musicplayer.SocyMusic.ui.queue.QueueFragment;
import com.musicplayer.SocyMusic.ui.settings.SettingsActivity;
import com.musicplayer.musicplayer.BuildConfig;
import com.musicplayer.musicplayer.R;

import java.util.List;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class MainActivity extends AppCompatActivity implements AllSongsFragment.AllSongsFragmentHost, PlayerFragment.PlayerFragmentHost, QueueFragment.QueueFragmentHost, ServiceConnection, ActivityResultCallback<ActivityResult> {
    private ViewPager2 tabsPager;
    private TabLayout tabsLayout;
    private BottomSheetBehavior<FrameLayout> bottomSheetBehavior;
    private ViewPager2 songInfoPager;

    // Private components
    private ActivityResultLauncher<Intent> resultLauncher;
    private PlayerFragment playerFragment;
    private QueueFragment queueFragment;
    private MediaPlayerService mediaPlayerService;
    private MediaPlayerReceiver mediaPlayerReceiver;
    //private ActionBar actionBar;
    private SongsData songsData;

    private boolean scrollTriggeredByCode;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    /**
     * Gets executed every time the app starts
     *
     * @param savedInstanceState Android standard
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        songsData = SongsData.getInstance(this);
        // Sets the theme!
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = prefs.getString(SocyMusicApp.PREFS_KEY_THEME, "Red_theme");
        switch (theme) {
            case "Red_theme":
                setTheme(R.style.Theme_MusicPlayer);
                break;
            case "White_theme":
                setTheme(R.style.WhiteTheme);
                break;
            case "Blue_theme":
                setTheme(R.style.BlueTheme);
                break;
            case "Green_theme":
                setTheme(R.style.GreenTheme);
                break;
            case "Orange_theme":
                setTheme(R.style.OrangeTheme);
                break;
            case "Yellow_theme":
                setTheme(R.style.YellowTheme);
                break;
            case "Aqua_theme":
                setTheme(R.style.AquaTheme);
                break;
            case "Purple_theme":
                setTheme(R.style.PurpleTheme);
                break;
            case "Purpleblue_theme":
                setTheme(R.style.PurpleblueTheme);
                break;
            case "Orangegreen_theme":
                setTheme(R.style.OrangegreenTheme);
                break;
            case "Purpleaqua_theme":
                setTheme(R.style.PurpleaquaTheme);
                break;
        }
        // Settings listener to update theme in realtime
        // Use instance field for listener
        // It will not be gc'd as long as this instance is kept referenced
        listener = (prefs1, key) -> {
            if (key.equals(SocyMusicApp.PREFS_KEY_THEME)) {
                recreate();
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);

        setContentView(R.layout.activity_main);

        //actionBar = getSupportActionBar();
        //actionBar.setElevation(0);
        //actionBar.setTitle(getString(R.string.all_app_name));

        tabsPager = findViewById(R.id.viewpager_main_tabs);
        tabsLayout = findViewById(R.id.tab_layout_main);

        // Sets all components
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
                    //actionBar.setTitle(R.string.player_title);
                    songInfoPager.setUserInputEnabled(false);
//                    songInfoPager.findViewWithTag(songInfoPager.getCurrentItem()).setClickable(false);
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    invalidateOptionsMenu();
                    //actionBar.setTitle(R.string.all_app_name);
                    ((ViewGroup.MarginLayoutParams) findViewById(R.id.layout_main_tabs_holder).getLayoutParams()).bottomMargin = dpToPixel(50);
                    songInfoPager.setUserInputEnabled(true);
//                    songInfoPager.findViewWithTag(songInfoPager.getCurrentItem()).setClickable(true);
                    hideQueue();
                } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    ((ViewGroup.MarginLayoutParams) findViewById(R.id.layout_main_tabs_holder).getLayoutParams()).bottomMargin = 0;
                }
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
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN || bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            getMenuInflater().inflate(R.menu.main, menu);
            // Searchbar, refrence: https://stackoverflow.com/questions/41867961/android-add-searchview-on-the-action-bar
            MenuItem actionMenuItem = menu.findItem(R.id.action_search);
            final SearchView searchView = (SearchView) actionMenuItem.getActionView();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }
                @Override
                public boolean onQueryTextChange(String newText) {
                    if (TextUtils.isEmpty(newText)) {
                        //adapter.filter("");
                        //listView.clearTextFilter();
                    } else {
                        //adapter.filter(newText);
                    }
                    return true;
                }
            });
        } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            getMenuInflater().inflate(R.menu.playing, menu);
            /*
            MenuItem showQueueButton = menu.findItem(R.id.playing_menu_show_queue);
            if (queueFragment == null)
                showQueueButton.setIcon(R.drawable.ic_queue);
            else
                showQueueButton.setIcon(R.drawable.ic_queue_selected);
             */
        }
        return super.onCreateOptionsMenu(menu);
    }

    public void hideQueue() {
        if (queueFragment == null)
            return;
        FragmentManager fragmentManager = getSupportFragmentManager();
        View playerFragmentView = findViewById(R.id.layout_player_holder);
        queueFragment.onDestroyView();
        fragmentManager.beginTransaction().remove(queueFragment).commit();

        playerFragmentView.setVisibility(View.VISIBLE);
        playerFragment.initializeVisualizer();
        //actionBar.setTitle(R.string.player_title);
        queueFragment = null;
        invalidateOptionsMenu();
    }

    public void showQueue() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        View playerFragmentView = findViewById(R.id.layout_player_holder);
        queueFragment = new QueueFragment();
        fragmentManager.beginTransaction().add(R.id.layout_main_queue_container, queueFragment).commit();
        //actionBar.setTitle(R.string.queue_title);
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
                        tabsPager.setAdapter(new TabsPagerAdapter(MainActivity.this));
                        new TabLayoutMediator(tabsLayout,
                                tabsPager,
                                (tab, position) -> tab.setText(getResources().getStringArray(R.array.main_tabs)[position]))
                                .attach();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        // Ask again and again until permissions are accepted
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    /**
     * When the player fragment finishes loading
     * Must be implemented!
     */
    @Override
    public void onLoadComplete() {
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        InfoPanePagerAdapter customAdapter = new InfoPanePagerAdapter(this, songsData.getPlayingQueue());
        customAdapter.setPaneListeners(new InfoPanePagerAdapter.PaneListeners() {
            @Override
            public void onPaneClick() {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }

            @Override
            public void onPauseButtonClick() {
                playerFragment.togglePlayPause();
            }
        });
        songInfoPager.setAdapter(customAdapter);

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
            pagerAdapter.setQueue(songsData.getPlayingQueue());

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
        pagerAdapter.setQueue(songsData.getPlayingQueue());
        pagerAdapter.notifyDataSetChanged();
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
        songsData.reloadSongs(this);
        tabsPager.setAdapter(new TabsPagerAdapter(this));
    }

    @Override
    public void onSongClick(int position) {
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

            Intent serviceIntent = new Intent(MainActivity.this, MediaPlayerService.class);
            serviceIntent.putExtra(MediaPlayerService.EXTRA_SONG, songClicked);
            startService(serviceIntent);
            bindService(serviceIntent, MainActivity.this, Context.BIND_AUTO_CREATE);
        } else {
            playerFragment = (PlayerFragment) fragment;
            playerFragment.invalidatePager();
            MediaPlayerUtil.startPlaying(MainActivity.this, songClicked);
            onSongUpdate();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            hideQueue();
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

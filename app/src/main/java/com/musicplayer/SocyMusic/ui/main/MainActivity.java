package com.musicplayer.SocyMusic.ui.main;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.musicplayer.SocyMusic.SocyMusicApp;
import com.musicplayer.SocyMusic.custom_views.SidenavComponent;
import com.musicplayer.SocyMusic.data.Playlist;
import com.musicplayer.SocyMusic.data.SongsData;
import com.musicplayer.SocyMusic.ui.albums_tab.AlbumsTabFragment;
import com.musicplayer.SocyMusic.ui.all_songs.AllSongsFragment;
import com.musicplayer.SocyMusic.ui.player_fragment_host.PlayerFragmentHost;
import com.musicplayer.SocyMusic.ui.playlists_tab.PlaylistsTabFragment;
import com.musicplayer.SocyMusic.ui.settings.SettingsFragment;
import com.musicplayer.musicplayer.R;

import java.util.Calendar;
import java.util.List;

import timber.log.Timber;

public class MainActivity extends PlayerFragmentHost implements AllSongsFragment.Host, AlbumsTabFragment.Host, PlaylistsTabFragment.Host, SettingsFragment.Host, ActivityResultCallback<ActivityResult>, SongsData.LoadListener {
    private ViewPager2 tabsPager;
    //private TabLayout tabsLayout;
    private Snackbar loadingSnackBar;
    private SidenavComponent sidenavComponent;
    private Thread thread;
    private boolean thread_can_run = true;

    public RadioButton homeItem;
    public RadioButton songsItem;
    public RadioButton albumsItem;
    public RadioButton artistsItem;
    public RadioButton favoritesItem;
    public RadioButton folderItem;
    public RadioButton playlistsItem;

    /**
     * Gets executed every time the app starts
     *
     * @param savedInstanceState Android standard
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View childView = getLayoutInflater().inflate(R.layout.content_main,
                findViewById(R.id.layout_main_tabs_holder), false);
        super.attachContentView(childView);

        // Instead of an actionbar, we use toolbar to simplify customisation
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.all_app_name);
        toolbar.setElevation(0);

        tabsPager = findViewById(R.id.viewpager_main_tabs);
        //tabsLayout = findViewById(R.id.tab_layout_main);

        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);

        // Checks for all the required permissions
        runtimePermission();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // Retrieves the time set in the Timepicker
        // Retrieves current time and converts it to seconds
        // Retrieves if the function is enabled
        SharedPreferences.OnSharedPreferenceChangeListener listener = (prefs1, key) -> {
            if (key.equals(SocyMusicApp.PREFS_KEY_TIMEPICKER) || key.equals(SocyMusicApp.PREFS_KEY_TIMEPICKER_SWITCH)) {
                // Retrieves the time set in the Timepicker
                int settime = prefs.getInt(SocyMusicApp.PREFS_KEY_TIMEPICKER, 36480);
                Calendar calendar = Calendar.getInstance();
                // Retrieves current time and converts it to seconds
                int currentTimes = (calendar.get(Calendar.HOUR_OF_DAY) * 3600) + (calendar.get(Calendar.MINUTE) * 60);
                // Retrieves if the function is enabled
                boolean time_switch = prefs.getBoolean(SocyMusicApp.PREFS_KEY_TIMEPICKER_SWITCH, false);
                timePreferenceUpdater(settime, currentTimes, time_switch);
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);
        sidenavComponent = new SidenavComponent(this, null);

        homeItem = findViewById(R.id.home_nav_item);
        homeItem.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b)
                tabsPager.setCurrentItem(0, true);
        });
        songsItem = findViewById(R.id.songs_nav_item);
        songsItem.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b)
                tabsPager.setCurrentItem(0, true);
        });
        albumsItem = findViewById(R.id.albums_nav_item);
        albumsItem.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                tabsPager.setCurrentItem(1, true);
        });
        artistsItem = findViewById(R.id.artists_nav_item);
        artistsItem.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                tabsPager.setCurrentItem(0, true);
        });
        favoritesItem = findViewById(R.id.favorites_nav_item);
        favoritesItem.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                tabsPager.setCurrentItem(2, true);
        });
        folderItem = findViewById(R.id.folders_nav_item);
        folderItem.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                tabsPager.setCurrentItem(3, true);
        });
        playlistsItem = findViewById(R.id.playlist_nav_item);
        playlistsItem.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
                tabsPager.setCurrentItem(2, true);
        });
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
            MenuItem settingsItem = menu.findItem(R.id.settings);
            settingsItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    //tabsPager.setCurrentItem(TabsPagerAdapter.TABS.length-1, true);
                    folderItem.setChecked(true);
                    return false;
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

    /**
     * Checks for all required permissions
     * For musicplayer storage permission to find all the songs and record permission for the visualizer
     */
    public void runtimePermission() {
        if (!SocyMusicApp.hasPermissions(MainActivity.this)) {
            loadingSnackBar = Snackbar.make(getRootView(),
                    R.string.all_loading_library,
                    Snackbar.LENGTH_INDEFINITE);
        }
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        try {
                            songsData.loadFromDatabase(MainActivity.this).join();
                            if (loadingSnackBar != null)
                                loadingSnackBar.show();
                            songsData.loadFromFiles(MainActivity.this);
                            finishLoading();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    void finishLoading() {
                        // Display all the songs
                        tabsPager.setAdapter(new TabsPagerAdapter(MainActivity.this));
                        /*
                        new TabLayoutMediator(tabsLayout,
                                tabsPager,
                                (tab, position) -> tab.setText(getResources().getStringArray(R.array.main_tabs)[position]))
                                .attach();

                         */
                    }


                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        // Ask again and again until permissions are accepted
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }


    @Override
    public void onPlaylistUpdate(Playlist playlist) {
        int index = songsData.getAllPlaylists().indexOf(playlist);
        PlaylistsTabFragment playlistsTab = (PlaylistsTabFragment) getTabFragment(TabsPagerAdapter.PLAYLISTS_TAB);
        if (playlistsTab != null)
            playlistsTab.updatePlaylistAt(index);
    }


    @Override
    public void onNewPlaylist(Playlist newPlaylist) {
        PlaylistsTabFragment playlistsTab = (PlaylistsTabFragment) getTabFragment(TabsPagerAdapter.PLAYLISTS_TAB);
        if (playlistsTab != null)
            playlistsTab.notifyPlaylistInserted();
    }

    @Override
    public void onActivityResult(ActivityResult result) {
        try {
            songsData.loadFromDatabase(this).join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        tabsPager.setAdapter(new TabsPagerAdapter(this));
    }

    @Override
    public void onSongListClick() {
        super.unregisterMediaReceiver();
    }

    @Override
    public void onLibraryDirsChanged() {
        loadingSnackBar = Snackbar.make(getRootView(),
                R.string.all_reloading_library,
                Snackbar.LENGTH_INDEFINITE);
        loadingSnackBar.show();
        songsData.loadFromFiles(this);
    }

    private Fragment getTabFragment(long tabId) {
        return getSupportFragmentManager().findFragmentByTag("f" + tabId);
    }

    @Override
    public void onRemovedSongs() {
        AllSongsFragment allSongsTab = ((AllSongsFragment) getTabFragment(TabsPagerAdapter.ALL_SONGS_TAB));
        if (allSongsTab != null)
            allSongsTab.invalidateSongList();
    }

    @Override
    public void onAddedSongs() {
        AllSongsFragment allSongsTab = ((AllSongsFragment) getTabFragment(TabsPagerAdapter.ALL_SONGS_TAB));
        if (allSongsTab != null)
            allSongsTab.invalidateSongList();
    }

    @Override
    public void onAddedAlbums() {
        AlbumsTabFragment albumsTab = ((AlbumsTabFragment) getTabFragment(TabsPagerAdapter.ALBUMS_TAB));
        if (albumsTab != null)
            albumsTab.invalidateAlbumList();
    }

    @Override
    public void onRemovedAlbums() {
        AlbumsTabFragment albumsTab = ((AlbumsTabFragment) getTabFragment(TabsPagerAdapter.ALBUMS_TAB));
        if (albumsTab != null)
            albumsTab.invalidateAlbumList();
    }

    @Override
    public void onLoadComplete() {
        if (loadingSnackBar != null)
            loadingSnackBar.dismiss();
        songsData.setDoneLoading(true);
    }

    /**
     * When there is a change in the sleeptimer, we first stop the thread,
     * check for the new state of the preferences and then if the conditions are still valid,
     * start the thread again with the new values.
     * This removes the need of restarting the app o every change
     */
    public void timePreferenceUpdater(int settime, int currentTime, boolean time_switch) {
        // Stops the thread
        if (thread != null && thread.isAlive()) {
            try {
                thread_can_run = false;
                thread.interrupt();
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread_can_run = true;
        } else {

            // Starts a thread to check for the sleep time to go off
            if (settime > currentTime && time_switch) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        // If the current time becomes the time set in the preference it exits the loop
                        while (thread_can_run) {
                            Calendar calendar = Calendar.getInstance();
                            int currentTime = (calendar.get(Calendar.HOUR_OF_DAY)*3600)+(calendar.get(Calendar.MINUTE)*60);
                            if (settime - currentTime <= 0)
                                break;
                        }
                        // Exit the app and shutdown
                        ActivityManager manager =
                                (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);

                        for (ActivityManager.AppTask task : manager.getAppTasks()) {
                            task.finishAndRemoveTask();
                            System.exit(0);
                        }
                    }
                };
                thread.start();
            } else {
                Timber.e("Thread not started :/");
            }
        }
    }
}

package com.musicplayer.SocyMusic.ui.main;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.musicplayer.SocyMusic.SocyMusicApp;
import com.musicplayer.SocyMusic.custom_views.SidenavMenu;
import com.musicplayer.SocyMusic.data.Playlist;
import com.musicplayer.SocyMusic.data.SongsData;
import com.musicplayer.SocyMusic.ui.albums_tab.AlbumsTabFragment;
import com.musicplayer.SocyMusic.ui.all_songs.AllSongsFragment;
import com.musicplayer.SocyMusic.ui.all_songs.SongListAdapter;
import com.musicplayer.SocyMusic.ui.player_fragment_host.PlayerFragmentHost;
import com.musicplayer.SocyMusic.ui.playlists_tab.PlaylistsTabFragment;
import com.musicplayer.SocyMusic.ui.settings.SettingsFragment;
import com.musicplayer.musicplayer.R;

import java.util.Calendar;
import java.util.List;

import timber.log.Timber;

public class MainActivity extends PlayerFragmentHost implements AllSongsFragment.Host, AlbumsTabFragment.Host, PlaylistsTabFragment.Host, SettingsFragment.Host, ActivityResultCallback<ActivityResult>, SongsData.LoadListener {
    private ViewPager2 tabsPager;
    private Snackbar loadingSnackBar;
    private SharedPreferences prefs;
    private SidenavMenu sidenavmenu;
    SongListAdapter songListAdapter;

    /**
     * Gets executed every time the app starts
     *
     * @param savedInstanceState Android standard
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        View childView = getLayoutInflater().inflate(R.layout.content_main,
                findViewById(R.id.layout_main_tabs_holder), false);
        super.attachContentView(childView);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);

        tabsPager = findViewById(R.id.viewpager_main_tabs);
        tabsPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sidenavmenu.setSelection(position);
            }
        });

        sidenavmenu = (SidenavMenu) findViewById(R.id.sidenavmenu);
        sidenavmenu.setPager(tabsPager);

        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);

        // Checks for all the required permissions
        runtimePermission();
        startSleeptimer();
        songListAdapter = new SongListAdapter(this, SongsData.data.getAllSongs());
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
            MenuItem menuItem = menu.findItem(R.id.app_menu);
            menuItem.setOnMenuItemClickListener(item -> {
                // This controls whetever the sidemenu is visible or not and changes accordingly
                if (sidenavmenu.getVisibility() == View.VISIBLE) {
                    sidenavmenu.setVisibility(View.GONE);
                } else {
                    sidenavmenu.setVisibility(View.VISIBLE);
                }
                return true;
            });
        } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            getMenuInflater().inflate(R.menu.playing, menu);
            MenuItem song_item = menu.findItem(R.id.song_info_button);
            song_item.setOnMenuItemClickListener(item -> {
                Toast.makeText(getApplicationContext(), "Work in progress", Toast.LENGTH_SHORT).show();
                return true;
            });
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

    private void startSleeptimer() {
        // Retrieves the time set in the Timepicker
        int settime = prefs.getInt(SocyMusicApp.PREFS_KEY_TIMEPICKER, 36480);
        Calendar calendar = Calendar.getInstance();
        // Retrieves current time and converts it to seconds
        int currentTimes = (calendar.get(Calendar.HOUR_OF_DAY) * 3600) + (calendar.get(Calendar.MINUTE) * 60);
        // Starts a thread to check for the sleep time to go off
        if (settime > currentTimes && prefs.getBoolean(SocyMusicApp.PREFS_KEY_TIMEPICKER_SWITCH, false)) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    // If the current time becomes the time set in the preference it exits the loop
                    while (true) {
                        Calendar calendar = Calendar.getInstance();
                        int currentTime = (calendar.get(Calendar.HOUR_OF_DAY) * 3600) + (calendar.get(Calendar.MINUTE) * 60);
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

package com.musicplayer.SocyMusic.ui.main;

import android.Manifest;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.musicplayer.SocyMusic.SocyMusicApp;
import com.musicplayer.SocyMusic.data.Playlist;
import com.musicplayer.SocyMusic.ui.PlayerFragmentHost;
import com.musicplayer.SocyMusic.ui.all_songs.AllSongsFragment;
import com.musicplayer.SocyMusic.ui.playlists_tab.PlaylistsTabFragment;
import com.musicplayer.SocyMusic.utils.ThemeChanger;
import com.musicplayer.musicplayer.R;

import java.util.List;

public class MainActivity extends PlayerFragmentHost implements AllSongsFragment.Host, PlaylistsTabFragment.Host, ActivityResultCallback<ActivityResult> {



    private ViewPager2 tabsPager;
    private TabLayout tabsLayout;

    SharedPreferences.OnSharedPreferenceChangeListener listener;

    /**
     * Gets executed every time the app starts
     *
     * @param savedInstanceState Android standard
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sets the theme!
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(ThemeChanger.getThemeID(this));
        listener = (prefs1, key) -> {
            if (key.equals(SocyMusicApp.PREFS_KEY_THEME)) {
                recreate();
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);

        View childView = getLayoutInflater().inflate(R.layout.content_main,
                (ViewGroup) findViewById(R.id.layout_main_tabs_holder), false);
        super.attachContentView(childView);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setElevation(0);
        actionBar.setTitle(getString(R.string.all_app_name));
        //actionBar.setBackgroundDrawable();

        tabsPager = findViewById(R.id.viewpager_main_tabs);
        tabsLayout = findViewById(R.id.tab_layout_main);

        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);

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

    /**
     * Checks for all required permissions
     * For musicplayer storage permission to find all the songs and record permission for the visualizer
     */
    public void runtimePermission() {
        if (SocyMusicApp.hasPermissions(this)) {
            tabsPager.setAdapter(new TabsPagerAdapter(MainActivity.this));
            new TabLayoutMediator(tabsLayout,
                    tabsPager,
                    (tab, position) -> tab.setText(getResources().getStringArray(R.array.main_tabs)[position]))
                    .attach();
            return;
        }
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        try {
                            songsData.reloadSongs(MainActivity.this).join();
                            finishLoading();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
//                        (new Handler()).postDelayed(this::finishLoading, 500);

                    }

                    void finishLoading() {
                        // Display all the songs
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


    @Override
    public void onPlaylistUpdate(Playlist playlist) {
        int index = songsData.getAllPlaylists().indexOf(playlist);
        PlaylistsTabFragment playlistsTab = (PlaylistsTabFragment) getSupportFragmentManager().findFragmentByTag("f" + TabsPagerAdapter.PLAYLISTS_TAB);
        if (playlistsTab != null)
            playlistsTab.updatePlaylistAt(index);
    }


    @Override
    public void onNewPlaylist() {
        PlaylistsTabFragment playlistsTab = (PlaylistsTabFragment) getSupportFragmentManager().findFragmentByTag("f" + TabsPagerAdapter.PLAYLISTS_TAB);
        if (playlistsTab != null)
            playlistsTab.notifyPlaylistInserted();
    }

    @Override
    public void onActivityResult(ActivityResult result) {
        try {
            songsData.reloadSongs(this).join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        tabsPager.setAdapter(new TabsPagerAdapter(this));
    }

    @Override
    public void onPlaylistClick() {
        super.unregisterMediaReceiver();
    }
}

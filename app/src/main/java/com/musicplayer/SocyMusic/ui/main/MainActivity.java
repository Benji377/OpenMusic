package com.musicplayer.SocyMusic.ui.main;

import android.Manifest;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
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
import com.musicplayer.SocyMusic.data.Playlist;
import com.musicplayer.SocyMusic.data.SongsData;
import com.musicplayer.SocyMusic.ui.albums_tab.AlbumsTabFragment;
import com.musicplayer.SocyMusic.ui.all_songs.AllSongsFragment;
import com.musicplayer.SocyMusic.ui.player_fragment_host.PlayerFragmentHost;
import com.musicplayer.SocyMusic.ui.playlists_tab.PlaylistsTabFragment;
import com.musicplayer.SocyMusic.ui.settings.SettingsFragment;
import com.musicplayer.SocyMusic.utils.ThemeChanger;
import com.musicplayer.musicplayer.R;

import java.util.List;

public class MainActivity extends PlayerFragmentHost implements AllSongsFragment.Host, AlbumsTabFragment.Host, PlaylistsTabFragment.Host, SettingsFragment.Host, ActivityResultCallback<ActivityResult>, SongsData.LoadListener {
    private ViewPager2 tabsPager;
    private TabLayout tabsLayout;

    SharedPreferences.OnSharedPreferenceChangeListener listener;
    private Snackbar loadingSnackBar;

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
                findViewById(R.id.layout_main_tabs_holder), false);
        super.attachContentView(childView);

        // Gets the primaryColor from the current Theme
        final TypedValue value = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorSurface, value, true);
        // Transforms color to Hex -> Avoids ResourceNotFound issue
        String hexColor = String.format("#%06X", (0xFFFFFF & value.data));
        // Gets the window and forces the statusbar to use the retrieved color
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor(hexColor));

        // Instead of an actionbar, we use toolbar to simplify customisation
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.all_app_name);
        toolbar.setElevation(0);

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
}

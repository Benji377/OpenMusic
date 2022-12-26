package com.musicplayer.openmusic.ui.main

import android.Manifest
import android.app.ActivityManager
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.musicplayer.openmusic.OpenMusicApp
import com.musicplayer.openmusic.OpenMusicApp.Companion.hasPermissions
import com.musicplayer.openmusic.R
import com.musicplayer.openmusic.custom_views.SidenavMenu
import com.musicplayer.openmusic.data.Playlist
import com.musicplayer.openmusic.data.Song
import com.musicplayer.openmusic.data.SongsData
import com.musicplayer.openmusic.data.SongsData.LoadListener
import com.musicplayer.openmusic.ui.albums_tab.AlbumsTabFragment
import com.musicplayer.openmusic.ui.all_songs.AllSongsFragment
import com.musicplayer.openmusic.ui.all_songs.SongListAdapter
import com.musicplayer.openmusic.ui.player.PlayerFragment
import com.musicplayer.openmusic.ui.player_fragment_host.PlayerFragmentHost
import com.musicplayer.openmusic.ui.player_song_info.SonginfoFragment
import com.musicplayer.openmusic.ui.playlists_tab.PlaylistsTabFragment
import com.musicplayer.openmusic.ui.search.SearchFragment
import com.musicplayer.openmusic.ui.settings.SettingsFragment
import timber.log.Timber
import java.util.*
import kotlin.system.exitProcess

abstract class MainActivity : PlayerFragmentHost(), AllSongsFragment.Host, AlbumsTabFragment.Host,
    PlaylistsTabFragment.Host, SettingsFragment.Host, SearchFragment.Host,
    ActivityResultCallback<ActivityResult>, LoadListener {
    private var songListAdapter: SongListAdapter? = null
    private var tabsPager: ViewPager2? = null
    private var tabsLayout: TabLayout? = null
    private var loadingSnackBar: Snackbar? = null
    private var prefs: SharedPreferences? = null
    private var sidenavmenu: SidenavMenu? = null

    /**
     * Gets executed every time the app starts
     *
     * @param savedInstanceState Android standard
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val childView: View = if (prefs!!.getBoolean(OpenMusicApp.PREFS_KEY_MENUSWITCH, false)) {
            layoutInflater.inflate(
                R.layout.content_main2,
                findViewById(R.id.layout_main_tabs_holder), false
            )
        } else {
            layoutInflater.inflate(
                R.layout.content_main,
                findViewById(R.id.layout_main_tabs_holder), false
            )
        }
        super.attachContentView(childView)
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(false)
        tabsPager = findViewById(R.id.viewpager_main_tabs)
        tabsPager?.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sidenavmenu!!.setSelection(position)
            }
        })
        tabsLayout = findViewById(R.id.tab_layout_main)
        sidenavmenu = findViewById(R.id.sidenavmenu)
        sidenavmenu?.setPager(tabsPager!!)
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(), this)

        // Checks for all the required permissions
        runtimePermission()
        startSleeptimer()
        songListAdapter = SongListAdapter(this, SongsData.data!!.getAllSongs() as MutableList<Song>)
    }

    /**
     * Creates the option menu you can see in the upper right corner (three dots)
     *
     * @param menu The menu to be created
     * @return The finished created menu
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_HIDDEN || bottomSheetBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
            menuInflater.inflate(R.menu.main, menu)
            val menuItem = menu.findItem(R.id.app_menu)
            val settingsItem = menu.findItem(R.id.app_settings)
            menuItem.isVisible = prefs!!.getBoolean(OpenMusicApp.PREFS_KEY_MENUSWITCH, false)
            settingsItem.isVisible = !prefs!!.getBoolean(OpenMusicApp.PREFS_KEY_MENUSWITCH, false)
            menuItem.setOnMenuItemClickListener {
                // This controls whetever the sidemenu is visible or not and changes accordingly
                if (sidenavmenu!!.visibility == View.VISIBLE) {
                    sidenavmenu!!.visibility = View.GONE
                } else {
                    sidenavmenu!!.visibility = View.VISIBLE
                }
                true
            }
            settingsItem.setOnMenuItemClickListener {
                tabsPager!!.setCurrentItem(4, true)
                true
            }
        } else if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
            menuInflater.inflate(R.menu.playing, menu)
            val songItem = menu.findItem(R.id.song_info_button)
            songItem.setOnMenuItemClickListener {
                // Checks what the current Fragment is and replaces it with the Songinfo or Player
                val fragment = supportFragmentManager.findFragmentById(R.id.layout_player_container)
                val fragmentTransaction = supportFragmentManager.beginTransaction()
                if (fragment != null && fragment.javaClass.toString() == PlayerFragment::class.java.toString()) {
                    fragmentTransaction.replace(R.id.layout_player_container, SonginfoFragment())
                } else {
                    fragmentTransaction.replace(R.id.layout_player_container, PlayerFragment())
                }
                fragmentTransaction.commit()
                true
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Checks for all required permissions
     * For musicplayer storage permission to find all the songs and record permission for the visualizer
     */
    private fun runtimePermission() {
        if (!hasPermissions(this@MainActivity)) {
            loadingSnackBar = Snackbar.make(
                rootView,
                R.string.all_loading_library,
                Snackbar.LENGTH_INDEFINITE
            )
        }
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
                    try {
                        songsData?.loadFromDatabase(this@MainActivity)?.join()
                        if (loadingSnackBar != null) loadingSnackBar!!.show()
                        songsData?.loadFromFiles(this@MainActivity)
                        finishLoading()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }

                fun finishLoading() {
                    // Display all the songs
                    tabsPager!!.adapter = TabsPagerAdapter(this@MainActivity)
                    TabLayoutMediator(
                        tabsLayout!!,
                        tabsPager!!
                    ) { tab: TabLayout.Tab, position: Int ->
                        tab.text = resources.getStringArray(R.array.main_tabs)[position]
                    }
                        .attach()
                }

                override fun onPermissionRationaleShouldBeShown(
                    list: List<PermissionRequest>,
                    permissionToken: PermissionToken
                ) {
                    // Ask again and again until permissions are accepted
                    permissionToken.continuePermissionRequest()
                }
            }).check()
    }

    override fun onPlaylistUpdate(playlist: Playlist) {
        val index = songsData?.allPlaylists!!.indexOf(playlist)
        val playlistsTab = getTabFragment(TabsPagerAdapter.PLAYLISTS_TAB) as PlaylistsTabFragment
        playlistsTab.updatePlaylistAt(index)
    }

    override fun onNewPlaylist(newPlaylist: Playlist) {
        val playlistsTab = getTabFragment(TabsPagerAdapter.PLAYLISTS_TAB) as PlaylistsTabFragment
        playlistsTab.notifyPlaylistInserted()
    }

    override fun onActivityResult(result: ActivityResult?) {
        try {
            songsData?.loadFromDatabase(this)?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        tabsPager!!.adapter = TabsPagerAdapter(this)
    }

    override fun onSongListClick() {
        super.unregisterMediaReceiver()
    }

    override fun onLibraryDirsChanged() {
        loadingSnackBar = Snackbar.make(
            rootView,
            R.string.all_reloading_library,
            Snackbar.LENGTH_INDEFINITE
        )
        loadingSnackBar!!.show()
        songsData?.loadFromFiles(this)
    }

    private fun getTabFragment(tabId: Long): Fragment {
        return supportFragmentManager.findFragmentByTag("f$tabId")!!
    }

    override fun onRemovedSongs() {
        val allSongsTab = getTabFragment(TabsPagerAdapter.ALL_SONGS_TAB) as AllSongsFragment
        allSongsTab.invalidateSongList()
    }

    override fun onAddedSongs() {
        val allSongsTab = getTabFragment(TabsPagerAdapter.ALL_SONGS_TAB) as AllSongsFragment
        allSongsTab.invalidateSongList()
    }

    override fun onAddedAlbums() {
        val albumsTab = getTabFragment(TabsPagerAdapter.ALBUMS_TAB) as AlbumsTabFragment
        albumsTab.invalidateAlbumList()
    }

    override fun onRemovedAlbums() {
        val albumsTab = getTabFragment(TabsPagerAdapter.ALBUMS_TAB) as AlbumsTabFragment
        albumsTab.invalidateAlbumList()
    }

    override fun onLoadComplete() {
        if (loadingSnackBar != null) loadingSnackBar!!.dismiss()
        songsData?.isDoneLoading = true
    }

    private fun startSleeptimer() {
        // Retrieves the time set in the Timepicker
        val settime = prefs!!.getInt(OpenMusicApp.PREFS_KEY_TIMEPICKER, 36480)
        val calendar = Calendar.getInstance()
        // Retrieves current time and converts it to seconds
        val currentTimes = calendar[Calendar.HOUR_OF_DAY] * 3600 + calendar[Calendar.MINUTE] * 60
        // Starts a thread to check for the sleep time to go off
        if (settime > currentTimes && prefs!!.getBoolean(
                OpenMusicApp.PREFS_KEY_TIMEPICKER_SWITCH,
                false
            )
        ) {
            val thread: Thread = object : Thread() {
                override fun run() {
                    // If the current time becomes the time set in the preference it exits the loop
                    while (true) {
                        val customCalendar = Calendar.getInstance()
                        val currentTime =
                            customCalendar[Calendar.HOUR_OF_DAY] * 3600 + customCalendar[Calendar.MINUTE] * 60
                        if (settime - currentTime <= 0) break
                    }
                    // Exit the app and shutdown
                    val manager =
                        applicationContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager
                    for (task in manager.appTasks) {
                        task.finishAndRemoveTask()
                        exitProcess(0)
                    }
                }
            }
            thread.start()
        } else {
            Timber.e("Thread not started :/")
        }
    }
}
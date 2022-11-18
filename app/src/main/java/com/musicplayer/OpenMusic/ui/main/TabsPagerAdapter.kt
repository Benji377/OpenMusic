package com.musicplayer.OpenMusic.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.musicplayer.OpenMusic.ui.all_songs.AllSongsFragment
import com.musicplayer.OpenMusic.ui.albums_tab.AlbumsTabFragment
import com.musicplayer.OpenMusic.ui.playlists_tab.PlaylistsTabFragment
import com.musicplayer.OpenMusic.ui.settings.SettingsFragment
import com.musicplayer.OpenMusic.ui.search.SearchFragment

class TabsPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        val id = getItemId(position)
        return if (id == ALL_SONGS_TAB) AllSongsFragment()
        else (if (id == ALBUMS_TAB) AlbumsTabFragment()
        else if (id == PLAYLISTS_TAB) PlaylistsTabFragment()
        else if (id == SETTINGS_TAB) SettingsFragment()
        else if (id == SEARCH_TAB) SearchFragment()
        else null)!!
    }

    override fun getItemCount(): Int {
        return TABS.size
    }

    override fun getItemId(position: Int): Long {
        return TABS[position]
    }

    override fun containsItem(itemId: Long): Boolean {
        for (id in TABS) if (id == itemId) return true
        return false
    }

    companion object {
        const val ALL_SONGS_TAB: Long = 720290723
        const val ALBUMS_TAB: Long = 885532984

        // private static final int ARTISTS_TAB = 2;
        const val PLAYLISTS_TAB: Long = 851211671
        const val SETTINGS_TAB: Long = 736239367
        const val SEARCH_TAB: Long = 675435679
        val TABS = longArrayOf(ALL_SONGS_TAB, ALBUMS_TAB, PLAYLISTS_TAB, SEARCH_TAB, SETTINGS_TAB)
    }
}
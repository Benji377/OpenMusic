package com.musicplayer.SocyMusic.ui.main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.musicplayer.SocyMusic.ui.albums_tab.AlbumsTabFragment;
import com.musicplayer.SocyMusic.ui.all_songs.AllSongsFragment;
import com.musicplayer.SocyMusic.ui.playlists_tab.PlaylistsTabFragment;
import com.musicplayer.SocyMusic.ui.settings.SettingsFragment;

public class TabsPagerAdapter extends FragmentStateAdapter {

    public static final long ALL_SONGS_TAB = 720290723;
    public static final long ALBUMS_TAB = 885532984;
    // private static final int ARTISTS_TAB = 2;
    public static final long PLAYLISTS_TAB = 851211671;
    public static final long SETTINGS_TAB = 736239367;
    public static final long[] TABS = {ALL_SONGS_TAB, ALBUMS_TAB, PLAYLISTS_TAB, SETTINGS_TAB};

    public TabsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }


    @Override
    public Fragment createFragment(int position) {
        long id = getItemId(position);
        if (id == ALL_SONGS_TAB)
            return new AllSongsFragment();
        else if (id == ALBUMS_TAB)
            return new AlbumsTabFragment();
        else if (id == PLAYLISTS_TAB)
            return new PlaylistsTabFragment();
        else if (id == SETTINGS_TAB)
            return new SettingsFragment();
        else
            return null;
    }


    @Override
    public int getItemCount() {
        return TABS.length;
    }

    @Override
    public long getItemId(int position) {
        return TABS[position];
    }

    @Override
    public boolean containsItem(long itemId) {
        for (long id : TABS)
            if (id == itemId)
                return true;
        return false;
    }
}

package com.musicplayer.SocyMusic.ui.main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.musicplayer.SocyMusic.ui.all_songs.AllSongsFragment;
import com.musicplayer.SocyMusic.ui.playlists_tab.PlaylistsTabFragment;

public class TabsPagerAdapter extends FragmentStateAdapter {

    private static final int ALL_SONGS_TAB = 0;
    // private static final int ALBUMS_TAB = 1;
    // private static final int ARTISTS_TAB = 2;
    private static final int PLAYLISTS_TAB = 1;
    private static final int[] TABS = {ALL_SONGS_TAB, PLAYLISTS_TAB};

    public TabsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }


    @Override
    public Fragment createFragment(int position) {
        if (position == ALL_SONGS_TAB)
            return new AllSongsFragment();
        else if (position == PLAYLISTS_TAB)
            return new PlaylistsTabFragment();
        else
            return null;
    }

    @Override
    public int getItemCount() {
        return TABS.length;
    }

}

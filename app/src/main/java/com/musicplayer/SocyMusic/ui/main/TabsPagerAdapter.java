package com.musicplayer.SocyMusic.ui.main;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.musicplayer.SocyMusic.ui.allsongs.AllSongsFragment;
import com.musicplayer.SocyMusic.ui.playlist.PlaylistFragment;

public class TabsPagerAdapter extends FragmentStateAdapter {
    private Context context;

    private static final int ALL_SONGS_TAB = 0;
    // private static final int ALBUMS_TAB = 1;
    // private static final int ARTISTS_TAB = 2;
    private static final int PLAYLISTS_TAB = 1;
    private static final int TAB_COUNT = 2;

    public TabsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.context = fragmentActivity;
    }

    @Override
    public Fragment createFragment(int position) {
        if (position == ALL_SONGS_TAB)
            return new AllSongsFragment();
        else if (position == PLAYLISTS_TAB)
            return new PlaylistFragment();
        else
            return null;
    }


    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}

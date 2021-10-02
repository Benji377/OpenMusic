package com.musicplayer.SocyMusic.custom_views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.musicplayer.musicplayer.R;

public class SidenavComponent extends RadioGroup {
    private RadioButton homeItem;
    private RadioButton songsItem;
    private RadioButton albumsItem;
    private RadioButton artistsItem;
    private RadioButton favoritesItem;
    private RadioButton folderItem;
    private RadioButton playlistsItem;

    public SidenavComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        initControl(context);
    }

    private void initControl(Context context) {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.sidenavmenu, this);
        homeItem = findViewById(R.id.home_nav_item);
        songsItem = findViewById(R.id.songs_nav_item);
        albumsItem = findViewById(R.id.albums_nav_item);
        artistsItem = findViewById(R.id.artists_nav_item);
        favoritesItem = findViewById(R.id.favorites_nav_item);
        folderItem = findViewById(R.id.folders_nav_item);
        playlistsItem = findViewById(R.id.playlist_nav_item);
    }

    @Override
    public int getCheckedRadioButtonId() {
        return super.getCheckedRadioButtonId();
    }
}
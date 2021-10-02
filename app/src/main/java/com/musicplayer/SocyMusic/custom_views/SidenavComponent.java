package com.musicplayer.SocyMusic.custom_views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.musicplayer.musicplayer.R;

public class SidenavComponent extends RadioGroup {
    public RadioButton homeItem;
    public RadioButton songsItem;
    public RadioButton albumsItem;
    public RadioButton artistsItem;
    public RadioButton favoritesItem;
    public RadioButton folderItem;
    public RadioButton playlistsItem;

    public SidenavComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
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
}
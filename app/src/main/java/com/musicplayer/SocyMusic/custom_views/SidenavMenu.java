package com.musicplayer.SocyMusic.custom_views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.musicplayer.musicplayer.R;


public class SidenavMenu extends RadioGroup {
    RadioGroup main_item;
    RadioButton songs_item;
    RadioButton albums_item;
    RadioButton playlist_item;
    RadioButton settings_item;

    public SidenavMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.custom_sidenav_menu, this);
        Log.e("Hello", "Constructor completed");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        main_item = findViewById(R.id.radioGroup);
        main_item.setBackgroundColor(Color.parseColor("#FF362E"));
        songs_item = findViewById(R.id.songlist_item);
        albums_item = findViewById(R.id.albums_item);
        playlist_item = findViewById(R.id.playlist_item);
        settings_item = findViewById(R.id.settings_item);
    }
}

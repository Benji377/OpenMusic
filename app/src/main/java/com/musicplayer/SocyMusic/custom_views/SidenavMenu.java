package com.musicplayer.SocyMusic.custom_views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.musicplayer.musicplayer.R;


public class SidenavMenu extends RadioGroup {
    // Defines all the buttons and its parent
    RadioGroup main_item;
    RadioButton songs_item;
    RadioButton albums_item;
    RadioButton playlist_item;
    RadioButton settings_item;
    ViewPager2 tabspager;
    // If you wish to add another button, please note that you need to change the whole file
    // and the XML file too

    public SidenavMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Inflates the XML file
        inflate(context, R.layout.custom_sidenav_menu, this);
    }

    @Override
    protected void onFinishInflate() {
        // Gets called after the inflation
        super.onFinishInflate();
        main_item = findViewById(R.id.radioGroup);
        main_item.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sidenav_background));

        // Every Radiobutton is defined here and has its listener associated
        songs_item = findViewById(R.id.songlist_item);
        songs_item.setChecked(true);
        songs_item.setOnCheckedChangeListener((compoundButton, b) -> {
            if (songs_item.isChecked()) {
                // Go to songs fragment
                tabspager.setCurrentItem(0, true);
            }
        });
        albums_item = findViewById(R.id.albums_item);
        albums_item.setOnCheckedChangeListener((compoundButton, b) -> {
            if (albums_item.isChecked()) {
                // Go to Albums fragment
                tabspager.setCurrentItem(1, true);
            }
        });
        playlist_item = findViewById(R.id.playlist_item);
        playlist_item.setOnCheckedChangeListener((compoundButton, b) -> {
            if (playlist_item.isChecked()) {
                // Go to the Playlist fragment
                tabspager.setCurrentItem(2, true);
            }
        });
        settings_item = findViewById(R.id.settings_item);
        settings_item.setOnCheckedChangeListener((compoundButton, b) -> {
            if (settings_item.isChecked()) {
                // Go to settings fragment
                tabspager.setCurrentItem(3, true);
            }
        });
    }

    /**
     * Depending on the parameter, it sets a specific button as selected
     * @param sel Number of the button
     */
    public void setSelection(int sel) {
        switch (sel) {
            case 0:
                songs_item.setChecked(true);
                break;
            case 1:
                albums_item.setChecked(true);
                break;
            case 2:
                playlist_item.setChecked(true);
                break;
            case 3:
                settings_item.setChecked(true);
                break;
        }
    }

    public void setPager(ViewPager2 pager) {
        this.tabspager = pager;
    }
}

package com.musicplayer.SocyMusic.custom_views;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import com.musicplayer.musicplayer.R;


public class SidenavMenu extends AppCompatActivity {
    RadioGroup rGroup;
    RadioButton songs_item;
    RadioButton albums_item;
    RadioButton playlist_item;
    RadioButton settings_item;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_sidenav_menu);

        rGroup = findViewById(R.id.radioGroup);
        songs_item = findViewById(R.id.songlist_item);
        albums_item = findViewById(R.id.albums_item);
        playlist_item = findViewById(R.id.playlist_item);
        settings_item = findViewById(R.id.settings_item);
    }
}

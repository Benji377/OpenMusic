package com.musicplayer.OpenMusic.ui.player_song_info;

import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.musicplayer.musicplayer.R;

public class SonginfoActivity extends AppCompatActivity {
    private SonginfoFragment songinfoFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_songinfo);
        setTitle("Songinfo");

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.layout_songinfo_container);
        if (fragment == null) {
            songinfoFragment = new SonginfoFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.layout_songinfo_container, songinfoFragment)
                    .commit();
        } else {
            songinfoFragment = (SonginfoFragment) fragment;
        }
    }
}

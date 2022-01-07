package com.musicplayer.SocyMusic.ui.sleeptime;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.musicplayer.musicplayer.R;

public class SleepTimeActivity extends AppCompatActivity {
    private SleepTimeFragment sleepTimeFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleeptime);
        setTitle("Select Sleeptime");

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.layout_settings_container);
        if (fragment == null) {
            sleepTimeFragment = new SleepTimeFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.layout_sleeptime_container, sleepTimeFragment)
                    .commit();
        } else {
            sleepTimeFragment = (SleepTimeFragment) fragment;
        }
    }

    @Override
    public void onBackPressed() {
        sleepTimeFragment.onBackPressed();
    }
}


package com.musicplayer.SocyMusic;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.musicplayer.musicplayer.R;


public class DirBrowserActivity extends AppCompatActivity {
    private DirBrowserFragment dirBrowserFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dir_browser);
        setTitle(R.string.dir_browser_title);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.layout_settings_container);
        if (fragment == null) {
            dirBrowserFragment = new DirBrowserFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.layout_dir_browser_container, dirBrowserFragment)
                    .commit();
        } else
            dirBrowserFragment = (DirBrowserFragment) fragment;
    }

    @Override
    public void onBackPressed() {
        dirBrowserFragment.onBackPressed();
    }
}

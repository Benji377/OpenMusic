package com.musicplayer.OpenMusic.ui.search;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.musicplayer.musicplayer.R;

public class SearchActivity extends AppCompatActivity {
    private SearchFragment searchFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Search");
        setContentView(R.layout.activity_search);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.layout_search_container);
        if (fragment == null) {
            searchFragment = new SearchFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.layout_search_container, searchFragment)
                    .commit();
        } else
            searchFragment = (SearchFragment) fragment;
    }
}

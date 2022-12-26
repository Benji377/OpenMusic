package com.musicplayer.openmusic.ui.search

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.musicplayer.openmusic.R

class SearchActivity : AppCompatActivity() {
    private var searchFragment: SearchFragment? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.title = "Search"
        setContentView(R.layout.activity_search)
        val fragmentManager = supportFragmentManager
        val fragment = fragmentManager.findFragmentById(R.id.layout_search_container)
        if (fragment == null) {
            searchFragment = SearchFragment()
            fragmentManager.beginTransaction()
                .add(R.id.layout_search_container, searchFragment!!)
                .commit()
        } else searchFragment = fragment as SearchFragment
    }
}
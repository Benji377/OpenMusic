package com.musicplayer.openmusic.ui.player_song_info

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.musicplayer.openmusic.R

class SonginfoActivity : AppCompatActivity() {
    private var songinfoFragment: SonginfoFragment? = null
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_songinfo)
        title = "Songinfo"
        val fragmentManager = supportFragmentManager
        val fragment = fragmentManager.findFragmentById(R.id.layout_songinfo_container)
        if (fragment == null) {
            songinfoFragment = SonginfoFragment()
            fragmentManager.beginTransaction()
                .add(R.id.layout_songinfo_container, songinfoFragment!!)
                .commit()
        } else {
            songinfoFragment = fragment as SonginfoFragment
        }
    }
}
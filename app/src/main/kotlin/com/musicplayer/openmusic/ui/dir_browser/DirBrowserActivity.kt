package com.musicplayer.openmusic.ui.dir_browser

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.musicplayer.musicplayer.R

class DirBrowserActivity : AppCompatActivity() {
    private var dirBrowserFragment: DirBrowserFragment? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dir_browser)
        setTitle(R.string.dir_browser_title)
        val fragmentManager = supportFragmentManager
        val fragment = fragmentManager.findFragmentById(R.id.layout_settings_container)
        if (fragment == null) {
            dirBrowserFragment = DirBrowserFragment()
            fragmentManager.beginTransaction()
                .add(R.id.layout_dir_browser_container, dirBrowserFragment!!)
                .commit()
        } else dirBrowserFragment = fragment as DirBrowserFragment?
    }

    override fun onBackPressed() {
        dirBrowserFragment!!.onBackPressed()
    }
}
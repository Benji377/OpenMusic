package com.musicplayer.openmusic.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.musicplayer.openmusic.R

class SettingsActivity : AppCompatActivity() {
    private var settingsFragment: SettingsFragment? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setTitle(R.string.main_menu_item_settings)
        setContentView(R.layout.activity_settings)
        val fragmentManager = supportFragmentManager
        val fragment = fragmentManager.findFragmentById(R.id.layout_settings_container)
        if (fragment == null) {
            settingsFragment = SettingsFragment()
            fragmentManager.beginTransaction()
                .add(R.id.layout_settings_container, settingsFragment!!)
                .commit()
        } else settingsFragment = fragment as SettingsFragment
    }
}
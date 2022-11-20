package com.musicplayer.openmusic.ui.sleeptime

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.musicplayer.openmusic.R

class SleepTimeActivity : AppCompatActivity() {
    private var sleepTimeFragment: SleepTimeFragment? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleeptime)
        title = "Select Sleeptime"
        val fragmentManager = supportFragmentManager
        val fragment = fragmentManager.findFragmentById(R.id.layout_settings_container)
        if (fragment == null) {
            sleepTimeFragment = SleepTimeFragment()
            fragmentManager.beginTransaction()
                .add(R.id.layout_sleeptime_container, sleepTimeFragment!!)
                .commit()
        } else {
            sleepTimeFragment = fragment as SleepTimeFragment
        }
    }

    override fun onBackPressed() {
        sleepTimeFragment!!.onBackPressed()
    }
}
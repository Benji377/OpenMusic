package com.musicplayer.openmusic.custom_views

import android.content.Context
import android.util.AttributeSet
import android.widget.CompoundButton
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.musicplayer.musicplayer.R
import timber.log.Timber

/**
 * The app supports two types of navigation, one of them is a sidebar. The sidebar can be toggled in
 * the settings. It is basically a collection of radioButtons with their respective actions.
 * In standard Android there is no option for sidebar navigation, so we had to create our own
 */
class SidenavMenu(context: Context, attrs: AttributeSet) : RadioGroup(context, attrs) {
    // Defines all the buttons and its parent
    private var mainItem: RadioGroup? = null
    private var songsItem: RadioButton? = null
    private var albumsItem: RadioButton? = null
    private var playlistItem: RadioButton? = null
    private var searchItem: RadioButton? = null
    private var settingsItem: RadioButton? = null
    private var tabspager: ViewPager2? = null
    // If you wish to add another button, please note that you need to change the whole file
    // and the XML file too
    /**
     * Constructor of the SideNavMenu. Basically sets the XML file it will add its logic to
     */
    init {
        // Inflates the XML file
        Timber.i("Creating the sideNavigation")
        inflate(context, R.layout.custom_sidenav_menu, this)
    }

    /**
     * When the XML file has been inflated, it is time to add the logic to it.
     * This function detects that the XML has finished inflating automatically
     */
    override fun onFinishInflate() {
        // Gets called after the inflation
        super.onFinishInflate()
        // The root item is a radioGroup, containing all radioButtons
        mainItem = findViewById(R.id.radioGroup)
        mainItem?.background = ContextCompat.getDrawable(context, R.drawable.sidenav_background)

        // Every Radiobutton is defined here and has its listener associated
        songsItem = findViewById(R.id.songlist_item)
        songsItem?.isChecked = true
        songsItem?.setOnCheckedChangeListener { _: CompoundButton?, _: Boolean ->
            if (songsItem!!.isChecked) {
                // Go to songs fragment
                tabspager!!.setCurrentItem(0, true)
            }
        }
        albumsItem = findViewById(R.id.albums_item)
        albumsItem?.setOnCheckedChangeListener { _: CompoundButton?, _: Boolean ->
            if (albumsItem!!.isChecked) {
                // Go to Albums fragment
                tabspager!!.setCurrentItem(1, true)
            }
        }
        playlistItem = findViewById(R.id.playlist_item)
        playlistItem?.setOnCheckedChangeListener { _: CompoundButton?, _: Boolean ->
            if (playlistItem!!.isChecked) {
                // Go to the Playlist fragment
                tabspager!!.setCurrentItem(2, true)
            }
        }
        searchItem = findViewById(R.id.search_item)
        searchItem?.setOnCheckedChangeListener { _: CompoundButton?, _: Boolean ->
            if (searchItem!!.isChecked) {
                tabspager!!.setCurrentItem(3, true)
            }
        }
        settingsItem = findViewById(R.id.settings_item)
        settingsItem?.setOnCheckedChangeListener { _: CompoundButton?, _: Boolean ->
            if (settingsItem!!.isChecked) {
                // Go to settings fragment
                tabspager!!.setCurrentItem(4, true)
            }
        }
        Timber.i("Finished inflating XML and added logic to it")
    }

    /**
     * Depending on the parameter, it sets a specific button as selected
     *
     * @param sel Number of the button
     */
    fun setSelection(sel: Int) {
        when (sel) {
            0 -> songsItem!!.isChecked = true
            1 -> albumsItem!!.isChecked = true
            2 -> playlistItem!!.isChecked = true
            3 -> searchItem!!.isChecked = true
            4 -> settingsItem!!.isChecked = true
        }
    }

    /**
     * Sets the pager to use with the navigation
     *
     * @param pager a ViewPager
     */
    fun setPager(pager: ViewPager2) {
        tabspager = pager
    }
}
package com.musicplayer.OpenMusic.custom_views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.musicplayer.musicplayer.R;

import timber.log.Timber;

/**
 * The app supports two types of navigation, one of them is a sidebar. The sidebar can be toggled in
 * the settings. It is basically a collection of radioButtons with their respective actions.
 * In standard Android there is no option for sidebar navigation, so we had to create our own
 */
public class SidenavMenu extends RadioGroup {
    // Defines all the buttons and its parent
    RadioGroup main_item;
    RadioButton songs_item;
    RadioButton albums_item;
    RadioButton playlist_item;
    RadioButton search_item;
    RadioButton settings_item;
    ViewPager2 tabspager;
    // If you wish to add another button, please note that you need to change the whole file
    // and the XML file too

    /**
     * Constructor of the SideNavMenu. Basically sets the XML file it will add its logic to
     * @param context Context of the app
     * @param attrs An optional set of attributes
     */
    public SidenavMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Inflates the XML file
        Timber.i("Creating the sideNavigation");
        inflate(context, R.layout.custom_sidenav_menu, this);
    }

    /**
     * When the XML file has been inflated, it is time to add the logic to it.
     * This function detects that the XML has finished inflating automatically
     */
    @Override
    protected void onFinishInflate() {
        // Gets called after the inflation
        super.onFinishInflate();
        // The root item is a radioGroup, containing all radioButtons
        main_item = findViewById(R.id.radioGroup);
        main_item.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sidenav_background));

        // Every Radiobutton is defined here and has its listener associated
        songs_item = findViewById(R.id.songlist_item);
        songs_item.setChecked(true);
        songs_item.setOnCheckedChangeListener((compoundButton, b) -> {
            if (songs_item.isChecked()) {
                // Go to songs fragment
                tabspager.setCurrentItem(0, true);
            }
        });
        albums_item = findViewById(R.id.albums_item);
        albums_item.setOnCheckedChangeListener((compoundButton, b) -> {
            if (albums_item.isChecked()) {
                // Go to Albums fragment
                tabspager.setCurrentItem(1, true);
            }
        });
        playlist_item = findViewById(R.id.playlist_item);
        playlist_item.setOnCheckedChangeListener((compoundButton, b) -> {
            if (playlist_item.isChecked()) {
                // Go to the Playlist fragment
                tabspager.setCurrentItem(2, true);
            }
        });
        search_item = findViewById(R.id.search_item);
        search_item.setOnCheckedChangeListener((compoundButton, b) -> {
            if (search_item.isChecked()) {
                tabspager.setCurrentItem(3, true);
            }
        });
        settings_item = findViewById(R.id.settings_item);
        settings_item.setOnCheckedChangeListener((compoundButton, b) -> {
            if (settings_item.isChecked()) {
                // Go to settings fragment
                tabspager.setCurrentItem(4, true);
            }
        });
        Timber.i("Finished inflating XML and added logic to it");
    }

    /**
     * Depending on the parameter, it sets a specific button as selected
     * @param sel Number of the button
     */
    public void setSelection(int sel) {
        switch (sel) {
            case 0:
                songs_item.setChecked(true);
                break;
            case 1:
                albums_item.setChecked(true);
                break;
            case 2:
                playlist_item.setChecked(true);
                break;
            case 3:
                search_item.setChecked(true);
                break;
            case 4:
                settings_item.setChecked(true);
                break;
        }
    }

    /**
     * Sets the pager to use with the navigation
     * @param pager a ViewPager
     */
    public void setPager(ViewPager2 pager) {
        this.tabspager = pager;
    }
}

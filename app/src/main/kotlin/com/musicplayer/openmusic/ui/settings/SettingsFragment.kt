package com.musicplayer.openmusic.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.musicplayer.openmusic.BuildConfig
import com.musicplayer.openmusic.OpenMusicApp
import com.musicplayer.openmusic.R
import com.musicplayer.openmusic.data.SongsData
import com.musicplayer.openmusic.data.SongsData.Companion.getInstance
import com.musicplayer.openmusic.ui.dir_browser.DirBrowserActivity
import com.musicplayer.openmusic.ui.sleeptime.SleepTimeActivity

class SettingsFragment : PreferenceFragmentCompat() {
    private var songsData: SongsData? = null
    private var hostCallBack: Host? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        songsData = getInstance(requireContext())
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        val libPathPreference = findPreference<Preference>(OpenMusicApp.PREFS_KEY_LIBRARY_PATHS)
        val versions = findPreference<Preference>(OpenMusicApp.PREFS_KEY_VERSION)
        //Preference logging = findPreference(OpenMusicApp.PREFS_KEY_LOGGING);
        val sleeptime = findPreference<Preference>(OpenMusicApp.PREFS_KEY_SLEEPTIME)
        val menuswitch = findPreference<Preference>(OpenMusicApp.PREFS_KEY_MENUSWITCH)
        val launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult -> if (result.resultCode == Activity.RESULT_OK) hostCallBack!!.onLibraryDirsChanged() }
        assert(libPathPreference != null)
        libPathPreference!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener setOnPreferenceClickListener@{
                if (!songsData!!.isDoneLoading) {
                    Toast.makeText(
                        requireContext(),
                        R.string.settings_cannot_change_lib,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnPreferenceClickListener false
                }
                val intent = Intent(context, DirBrowserActivity::class.java)
                launcher.launch(intent)
                true
            }
        assert(sleeptime != null)
        sleeptime!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val intent = Intent(context, SleepTimeActivity::class.java)
                launcher.launch(intent)
                true
            }
        assert(menuswitch != null)
        menuswitch!!.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                requireActivity().recreate()
                true
            }
        assert(versions != null)
        versions!!.summary = getString(R.string.about_version, BuildConfig.VERSION_NAME)
    }

    /**
     * If the fragment is being attached to another activity
     *
     * @param context The context of the app
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            hostCallBack = context as Host
            // If implementation is missing
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement SettingsFragment.Host")
        }
    }

    interface Host {
        fun onLibraryDirsChanged()
    }
}
package com.musicplayer.openmusic.ui.sleeptime

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TimePicker
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.musicplayer.openmusic.OpenMusicApp
import com.musicplayer.openmusic.R

/**
 * Class that controls the sleeptime functionality
 * This functionality basically allows users to set a time at which the app will stop the music
 * and shutdown. It has a clock widget to set the time and a switch to activate or deactivate it.
 * The settings get written in a Preference which is invisible to the user. Every time the user
 * presses the back key the settings get saved automatically.
 */
class SleepTimeFragment : Fragment() {
    private var preferences: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var timePicker: TimePicker? = null
    private var switchCompat: SwitchCompat? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_sleeptime, container, false)
        preferences = PreferenceManager.getDefaultSharedPreferences(view.context)
        timePicker = view.findViewById(R.id.simpleTimePicker)
        timePicker?.setIs24HourView(false)
        switchCompat = view.findViewById(R.id.timePickerSwitch)
        updateTimePicker()
        switchCompat?.isChecked = preferences!!.getBoolean(
            OpenMusicApp.PREFS_KEY_TIMEPICKER_SWITCH,
            false
        )

        // Detects whenever a user changes the time on the clock or switches from AM to PM
        timePicker?.setOnTimeChangedListener { timePicker: TimePicker, hours: Int, minutes: Int ->
            // display a toast with changed values of time picker
            //Toast.makeText(requireContext(), "H: " + hours + " M: " + minutes, Toast.LENGTH_SHORT).show();
            timePicker.hour = hours
            timePicker.minute = minutes
        }
        switchCompat?.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            switchCompat?.isChecked = b
        }
        return view
    }

    // Gets if the time is PM or AM
    val amPm: String
        get() = if (timePicker!!.hour < 12) "AM" else "PM"

    // Gets the time in INT, which is also the format it gets saved in the preferences
    private fun convertInt(): Int {
        return timePicker!!.hour * 3600 + timePicker!!.minute * 60
    }

    private fun updateTimePicker() {
        // On time change it automatically updates the variables to match the new time
        val timee = preferences!!.getInt(OpenMusicApp.PREFS_KEY_TIMEPICKER, 36480)
        val min = timee / 60 % 60
        val hours = timee / 60 / 60
        timePicker!!.hour = hours
        timePicker!!.minute = min
    }

    // True if the switch is on, else false
    private fun switchState(): Boolean {
        return switchCompat!!.isChecked
    }

    fun onBackPressed() {
        // Opens the preferences and manually edits the set time
        editor = preferences!!.edit()
        editor?.putInt(OpenMusicApp.PREFS_KEY_TIMEPICKER, convertInt())
        editor?.putBoolean(OpenMusicApp.PREFS_KEY_TIMEPICKER_SWITCH, switchState())
        editor?.apply()
        // Necessary to actually leave the activity once the user presses back
        val hostActivity: Activity = requireActivity()
        hostActivity.finish()
    }
}
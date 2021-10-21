package com.musicplayer.SocyMusic.ui.sleeptime;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.musicplayer.SocyMusic.SocyMusicApp;
import com.musicplayer.musicplayer.R;

/**
 * Class that controls the sleeptime functionality
 * This functionality basically allows users to set a time at which the app will stop the music
 * and shutdown. It has a clock widget to set the time and a switch to activate or deactivate it.
 * The settings get written in a Preference which is invisible to the user. Every time the user
 * presses the back key the settings get saved automatically.
 */
public class SleepTimeFragment extends Fragment {
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    private TimePicker timePicker;
    private SwitchCompat switchCompat;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sleeptime, container, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        timePicker = view.findViewById(R.id.simpleTimePicker);
        timePicker.setIs24HourView(false);
        switchCompat = view.findViewById(R.id.timePickerSwitch);
        updateTimePicker();
        switchCompat.setChecked(preferences.getBoolean(SocyMusicApp.PREFS_KEY_TIMEPICKER_SWITCH, false));

        // Detects whenever a user changes the time on the clock or switches from AM to PM
        timePicker.setOnTimeChangedListener((timePicker, hours, minutes) -> {
            // display a toast with changed values of time picker
            //Toast.makeText(requireContext(), "H: " + hours + " M: " + minutes, Toast.LENGTH_SHORT).show();
            timePicker.setHour(hours);
            timePicker.setMinute(minutes);
        });
        switchCompat.setOnCheckedChangeListener((compoundButton, b) -> switchCompat.setChecked(b));

        return view;
    }

    // Gets if the time is PM or AM
    public String getAmPm() {
        return (timePicker.getHour() < 12) ? "AM" : "PM";
    }

    // Gets the time in INT, which is also the format it gets saved in the preferences
    public int convertInt() {
        return timePicker.getHour() * 3600 + timePicker.getMinute() * 60;
    }

    public void updateTimePicker() {
        // On time change it automatically updates the variables to match the new time
        int timee = preferences.getInt(SocyMusicApp.PREFS_KEY_TIMEPICKER, 36480);
        int min = (timee / 60) % 60;
        int hours = (timee / 60) / 60;
        timePicker.setHour(hours);
        timePicker.setMinute(min);
    }

    // True if the switch is on, else false
    public boolean switchState() {
        return switchCompat.isChecked();
    }

    protected void onBackPressed() {
        // Opens the preferences and manually edits the set time
        editor = preferences.edit();
        editor.putInt(SocyMusicApp.PREFS_KEY_TIMEPICKER, convertInt());
        editor.putBoolean(SocyMusicApp.PREFS_KEY_TIMEPICKER_SWITCH, switchState());
        editor.apply();
        // Necessary to actually leave the activity once the user presses back
        Activity hostActivity = requireActivity();
        hostActivity.finish();
    }

}

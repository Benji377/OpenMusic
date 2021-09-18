package com.musicplayer.SocyMusic.ui.sleeptime;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.musicplayer.SocyMusic.SocyMusicApp;
import com.musicplayer.musicplayer.R;

import timber.log.Timber;

public class SleepTimeFragment extends Fragment {
    private TimePicker timePicker;
    private SwitchCompat switchCompat;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

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
        timePicker = (TimePicker)view.findViewById(R.id.simpleTimePicker);
        timePicker.setIs24HourView(false);
        retrieveData();
        switchCompat = (SwitchCompat)view.findViewById(R.id.timePickerSwitch);

        timePicker.setOnTimeChangedListener((view1, hourOfDay, minute) -> {
            // display a toast with changed values of time picker
            Toast.makeText(requireContext(), getCombined(), Toast.LENGTH_SHORT).show();
            editor = preferences.edit();
            editor.putString(SocyMusicApp.PREFS_KEY_TIMEPICKER, getCombined());
            editor.apply();
        });

        return view;
    }

    // Gets if the time is PM or AM
    public String getAmPm() {
        return (timePicker.getHour() < 12) ? "AM" : "PM";
    }

    // Sets the time to a displayable String
    public String getCombined() {
        // Time in hh:mm:AA format
        String ret = "";
        ret+=timePicker.getHour()+":";
        ret+=timePicker.getMinute()+":";
        ret+=getAmPm();
        return ret;
    }

    // Converts the time to seconds
    public int timeToINT() {
        return timePicker.getHour()*3600 + timePicker.getMinute()*60;
    }

    // Sets the time that was saved to the preferences
    public void retrieveData() {
        String timee = preferences.getString(SocyMusicApp.PREFS_KEY_TIMEPICKER, "10:08:PM");
        String[] result = timee.split(":");
        for(String val : result)
            Timber.e("VAL: %s", val);


        if (result[2].equals("PM"))
            timePicker.setHour(Integer.parseInt(result[0])+12);
        else
            timePicker.setHour(Integer.parseInt(result[0]));
        timePicker.setMinute(Integer.parseInt(result[1]));
    }

    // True if the switch is on, else false
    public void setSwitchState() {
        boolean val = switchCompat.isChecked();
        editor = preferences.edit();
        editor.putBoolean(SocyMusicApp.PREFS_KEY_TIMEPICKER_SWITCh, val);
        editor.apply();

    }

}

package com.musicplayer.SocyMusic.ui.sleeptime;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.musicplayer.SocyMusic.SocyMusicApp;
import com.musicplayer.musicplayer.R;

public class SleepTimeFragment extends Fragment {
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    private TimePicker timePicker;
    private SwitchCompat switchCompat;
    private Button button;

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
        button = view.findViewById(R.id.confirm_button);
        /*
        editor = preferences.edit();
        editor.putInt(SocyMusicApp.PREFS_KEY_TIMEPICKER, 36480);
        editor.putBoolean(SocyMusicApp.PREFS_KEY_TIMEPICKER_SWITCH, false);
        editor.apply();
         */
        updateTimePicker();
        switchCompat.setChecked(preferences.getBoolean(SocyMusicApp.PREFS_KEY_TIMEPICKER_SWITCH, false));

        timePicker.setOnTimeChangedListener((timePicker, hours, minutes) -> {
            // display a toast with changed values of time picker
            Toast.makeText(requireContext(), "H: " + hours + " M: " + minutes, Toast.LENGTH_SHORT).show();
            timePicker.setHour(hours);
            timePicker.setMinute(minutes);
        });

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                switchCompat.setChecked(b);
            }
        });

        button.setOnClickListener(view12 -> {
            editor = preferences.edit();
            editor.putInt(SocyMusicApp.PREFS_KEY_TIMEPICKER, convertInt());
            editor.putBoolean(SocyMusicApp.PREFS_KEY_TIMEPICKER_SWITCH, switchState());
            editor.apply();
        });


        return view;
    }

    // Gets if the time is PM or AM
    public String getAmPm() {
        return (timePicker.getHour() < 12) ? "AM" : "PM";
    }

    public int convertInt() {
        return timePicker.getHour() * 3600 + timePicker.getMinute() * 60;
    }

    public void updateTimePicker() {
        int timee = preferences.getInt(SocyMusicApp.PREFS_KEY_TIMEPICKER, 36480);
        int sec = timee % 60;
        int min = (timee / 60) % 60;
        int hours = (timee / 60) / 60;
        timePicker.setHour(hours);
        timePicker.setMinute(min);
    }

    // Sets the time to a displayable String
    public String getCombined() {
        // Time in hh:mm:AA format
        String ret = "";
        ret += timePicker.getHour() + ":";
        ret += timePicker.getMinute() + ":";
        ret += getAmPm();
        return ret;
    }

    // True if the switch is on, else false
    public boolean switchState() {
        return switchCompat.isChecked();
    }

}

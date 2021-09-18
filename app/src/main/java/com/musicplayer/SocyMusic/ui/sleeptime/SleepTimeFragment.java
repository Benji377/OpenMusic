package com.musicplayer.SocyMusic.ui.sleeptime;

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
import com.musicplayer.musicplayer.R;

public class SleepTimeFragment extends Fragment {
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
        timePicker = (TimePicker)view.findViewById(R.id.simpleTimePicker);
        timePicker.setIs24HourView(false);
        switchCompat = (SwitchCompat)view.findViewById(R.id.timePickerSwitch);

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                // display a toast with changed values of time picker
                Toast.makeText(requireContext(), getCombined(), Toast.LENGTH_SHORT).show();
                //time.setText("Time is :: " + hourOfDay + " : " + minute); // set the current time in text view
            }
        });

        return view;
    }

    public void setMinute(int minute) {
        timePicker.setMinute(minute);
    }
    public void setHour(int hour) {
        timePicker.setHour(hour);
    }
    public int getMinute() {
        return timePicker.getMinute();
    }
    public int getHour() {
        return timePicker.getHour();
    }
    public String getAmPm() {
        return (getHour() < 12) ? "AM" : "PM";
    }
    public String getCombined() {
        // Time in hh:mm:AA format
        String ret = "";
        ret+=getHour()+":";
        ret+=getMinute()+":";
        ret+=getAmPm();

        return ret;
    }

    public boolean getSwitchState() {
        return switchCompat.isChecked();
    }

    protected void onBackPressed() {

    }

}

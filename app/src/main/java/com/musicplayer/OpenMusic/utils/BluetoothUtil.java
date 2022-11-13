package com.musicplayer.OpenMusic.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.musicplayer.OpenMusic.MediaPlayerUtil;

import timber.log.Timber;

/**
 * Class to identify the state of the Bluetooth-device. This allows us to perform certain actions
 * on connection or disconnection of the device
 */
public class BluetoothUtil extends BroadcastReceiver {

    public final String DEVICE_FOUND = "found";
    public final String DEVICE_CONNECTED = "connected";
    public final String DONE_SEARCHING = "done";
    public final String DISCONNECT_REQUESTED = "requested";
    public final String DISCONNECTED = "disconnected";

    private String selectedAction;
    //private PlayerFragment playerFragment = new PlayerFragment();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        //BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            //Device found
            setSelectedAction(DEVICE_FOUND);
        } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            //Device is now connected
            setSelectedAction(DEVICE_CONNECTED);
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            //Done searching
            setSelectedAction(DONE_SEARCHING);
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
            //Device is about to disconnect
            setSelectedAction(DISCONNECT_REQUESTED);
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            //Device has disconnected
            setSelectedAction(DISCONNECTED);
            if (MediaPlayerUtil.isPlaying()) {
                MediaPlayerUtil.pause();
                //playerFragment.updatePlayButton();
            }
        }
    }

    public String getSelectedAction() {
        return selectedAction;
    }

    public void setSelectedAction(String selectedAction) {
        Timber.e("BLUETOOTH_ACTION: %s", selectedAction);
        this.selectedAction = selectedAction;
    }
}

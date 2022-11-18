package com.musicplayer.OpenMusic.utils

import com.musicplayer.OpenMusic.MediaPlayerUtil.isPlaying
import com.musicplayer.OpenMusic.MediaPlayerUtil.pause
import android.content.BroadcastReceiver
import android.content.Intent
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothAdapter
import android.content.Context
import timber.log.Timber

/**
 * Class to identify the state of the Bluetooth-device. This allows us to perform certain actions
 * on connection or disconnection of the device
 */
class BluetoothUtil : BroadcastReceiver() {
    private val DEVICE_FOUND = "found"
    private val DEVICE_CONNECTED = "connected"
    private val DONE_SEARCHING = "done"
    private val DISCONNECT_REQUESTED = "requested"
    private val DISCONNECTED = "disconnected"
    private var selectedAction: String? = null
        set(selectedAction) {
            Timber.e("BLUETOOTH_ACTION: %s", selectedAction)
            field = selectedAction
        }

    //private PlayerFragment playerFragment = new PlayerFragment();
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        //BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (BluetoothDevice.ACTION_FOUND == action) {
            //Device found
            selectedAction = DEVICE_FOUND
        } else if (BluetoothDevice.ACTION_ACL_CONNECTED == action) {
            //Device is now connected
            selectedAction = DEVICE_CONNECTED
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
            //Done searching
            selectedAction = DONE_SEARCHING
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED == action) {
            //Device is about to disconnect
            selectedAction = DISCONNECT_REQUESTED
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED == action) {
            //Device has disconnected
            selectedAction = DISCONNECTED
            if (isPlaying) {
                pause()
                //playerFragment.updatePlayButton();
            }
        }
    }
}
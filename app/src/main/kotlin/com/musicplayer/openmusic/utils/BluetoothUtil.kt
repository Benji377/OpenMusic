package com.musicplayer.openmusic.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.musicplayer.openmusic.MediaPlayerUtil.isPlaying
import com.musicplayer.openmusic.MediaPlayerUtil.pause
import timber.log.Timber

/**
 * Class to identify the state of the Bluetooth-device. This allows us to perform certain actions
 * on connection or disconnection of the device
 */
class BluetoothUtil : BroadcastReceiver() {
    private val deviceFound = "found"
    private val deviceConnected = "connected"
    private val doneSearching = "done"
    private val disconnectRequested = "requested"
    private val disconnected = "disconnected"
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
            selectedAction = deviceFound
        } else if (BluetoothDevice.ACTION_ACL_CONNECTED == action) {
            //Device is now connected
            selectedAction = deviceConnected
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
            //Done searching
            selectedAction = doneSearching
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED == action) {
            //Device is about to disconnect
            selectedAction = disconnectRequested
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED == action) {
            //Device has disconnected
            selectedAction = disconnected
            if (isPlaying) {
                pause()
                //playerFragment.updatePlayButton();
            }
        }
    }
}
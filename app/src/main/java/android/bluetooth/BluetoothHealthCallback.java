package android.bluetooth;

import android.os.ParcelFileDescriptor;
import android.util.Log;

public abstract class BluetoothHealthCallback {
    private static final String TAG = "BluetoothHealthCallback";

    public void onHealthAppConfigurationStatusChange(BluetoothHealthAppConfiguration config, int status) {
        Log.d(TAG, "onHealthAppConfigurationStatusChange: " + config + "Status: " + status);
    }

    public void onHealthChannelStateChange(BluetoothHealthAppConfiguration config, BluetoothDevice device, int prevState, int newState, ParcelFileDescriptor fd, int channelId) {
        Log.d(TAG, "onHealthChannelStateChange: " + config + "Device: " + device + "prevState:" + prevState + "newState:" + newState + "ParcelFd:" + fd + "ChannelId:" + channelId);
    }
}

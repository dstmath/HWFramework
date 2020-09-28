package android.bluetooth;

import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

public abstract class BluetoothHealthCallback {
    private static final String TAG = "BluetoothHealthCallback";

    public void onHealthAppConfigurationStatusChange(BluetoothHealthAppConfiguration config, int status) {
        Log.d(TAG, "onHealthAppConfigurationStatusChange: " + config + "Status: " + status);
    }

    public void onHealthChannelStateChange(BluetoothHealthAppConfiguration config, BluetoothDevice device, int prevState, int newState, ParcelFileDescriptor fd, int channelId) {
        Log.d(TAG, "onHealthChannelStateChange: " + config + "Device: " + getPartAddress(device) + "prevState:" + prevState + "newState:" + newState + "ParcelFd:" + fd + "ChannelId:" + channelId);
    }

    private static String getPartAddress(String address) {
        if (TextUtils.isEmpty(address)) {
            return "";
        }
        return address.substring(0, address.length() / 2) + ":**:**:**";
    }

    private static String getPartAddress(BluetoothDevice device) {
        if (device == null) {
            return "";
        }
        return getPartAddress(device.getAddress());
    }
}

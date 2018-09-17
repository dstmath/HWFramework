package android.bluetooth;

import android.util.Log;

public abstract class BluetoothHidDeviceCallback {
    private static final String TAG = BluetoothHidDeviceCallback.class.getSimpleName();

    public void onAppStatusChanged(BluetoothDevice pluggedDevice, BluetoothHidDeviceAppConfiguration config, boolean registered) {
        Log.d(TAG, "onAppStatusChanged: pluggedDevice=" + pluggedDevice + " registered=" + registered);
    }

    public void onConnectionStateChanged(BluetoothDevice device, int state) {
        Log.d(TAG, "onConnectionStateChanged: device=" + device + " state=" + state);
    }

    public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {
        Log.d(TAG, "onGetReport: device=" + device + " type=" + type + " id=" + id + " bufferSize=" + bufferSize);
    }

    public void onSetReport(BluetoothDevice device, byte type, byte id, byte[] data) {
        Log.d(TAG, "onSetReport: device=" + device + " type=" + type + " id=" + id);
    }

    public void onSetProtocol(BluetoothDevice device, byte protocol) {
        Log.d(TAG, "onSetProtocol: device=" + device + " protocol=" + protocol);
    }

    public void onIntrData(BluetoothDevice device, byte reportId, byte[] data) {
        Log.d(TAG, "onIntrData: device=" + device + " reportId=" + reportId);
    }

    public void onVirtualCableUnplug(BluetoothDevice device) {
        Log.d(TAG, "onVirtualCableUnplug: device=" + device);
    }
}

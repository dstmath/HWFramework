package com.huawei.android.bluetooth;

import android.bluetooth.BluetoothAdapter;

public class BluetoothAdapterExt {
    public static final String ACTION_BLE_STATE_CHANGED = "android.bluetooth.adapter.action.BLE_STATE_CHANGED";

    public static int getConnectionState(BluetoothAdapter bluetoothAdapter) {
        if (bluetoothAdapter == null) {
            return -1;
        }
        return bluetoothAdapter.getConnectionState();
    }
}

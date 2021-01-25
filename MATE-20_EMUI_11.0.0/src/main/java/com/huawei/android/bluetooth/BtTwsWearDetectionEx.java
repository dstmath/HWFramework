package com.huawei.android.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothDeviceAdapterEx;
import android.util.Log;

public class BtTwsWearDetectionEx {
    public static final int SIDE_IGNORE = 0;
    public static final int SIDE_LEFT = 1;
    public static final int SIDE_RIGHT = 2;
    public static final int SUPPORT_OFF = 0;
    public static final int SUPPORT_ON = 1;
    public static final int SUPPORT_SYSTEM_ERROR = -2;
    public static final int SUPPORT_UNKNOWN = -1;
    private static final String TAG = "BtTwsWearDetectionEx";
    public static final int WEAR_STATE_FAIL_SYSTEM_ERROR = -2;
    public static final int WEAR_STATE_FAIL_WRONG_PARA = -3;
    public static final int WEAR_STATE_IN = 1;
    public static final int WEAR_STATE_OUT = 0;
    public static final int WEAR_STATE_UNKNOWN = -1;

    public static void setTwsWearDetectionSupport(BluetoothDevice device, int support) {
        if (device == null) {
            Log.d(TAG, "setTwsWearDetectionSupport got null device");
        } else if (support == 1 || support == 0) {
            BluetoothDeviceAdapterEx.setWearDetectionSupport(device, support);
        } else {
            Log.d(TAG, "setTwsWearDetectionSupport got illegal support value");
        }
    }

    public static int getTwsWearDetectionSupport(BluetoothDevice device) {
        BluetoothDevice aimDevice = device;
        if (aimDevice == null) {
            Log.d(TAG, "getTwsWearDetectionSupport got null device");
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter == null) {
                Log.d(TAG, "getTwsWearDetectionSupport got null BluetoothAdapter");
                return -2;
            }
            aimDevice = adapter.getRemoteDevice("02:00:00:00:00:00");
            if (aimDevice == null) {
                Log.d(TAG, "getTwsWearDetectionSupport got null BluetoothDevice");
                return -2;
            }
        }
        return BluetoothDeviceAdapterEx.getTwsWearDetectionSupport(aimDevice);
    }

    public static int getTwsWearState(BluetoothDevice device, int side) {
        if (side < 0 || side > 2) {
            Log.d(TAG, "getTwsWearState got illegal direction value");
            return -3;
        }
        BluetoothDevice aimDevice = device;
        if (aimDevice == null) {
            Log.d(TAG, "getTwsWearState got null device");
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter == null) {
                Log.d(TAG, "getTwsWearState got null BluetoothAdapter");
                return -2;
            }
            aimDevice = adapter.getRemoteDevice("02:00:00:00:00:00");
            if (aimDevice == null) {
                Log.d(TAG, "getTwsWearState got null BluetoothDevice");
                return -2;
            }
        }
        return BluetoothDeviceAdapterEx.getTwsWearState(aimDevice, side);
    }
}

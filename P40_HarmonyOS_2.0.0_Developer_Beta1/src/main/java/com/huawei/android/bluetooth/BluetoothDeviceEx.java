package com.huawei.android.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothDeviceAdapterEx;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.huawei.android.util.NoExtAPIException;

public class BluetoothDeviceEx implements Parcelable {
    public static final Parcelable.Creator<BluetoothDeviceEx> CREATOR = new Parcelable.Creator<BluetoothDeviceEx>() {
        /* class com.huawei.android.bluetooth.BluetoothDeviceEx.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public BluetoothDeviceEx createFromParcel(Parcel in) {
            return new BluetoothDeviceEx(in);
        }

        @Override // android.os.Parcelable.Creator
        public BluetoothDeviceEx[] newArray(int size) {
            return new BluetoothDeviceEx[size];
        }
    };
    private static final String HW_REMOTE_DEVICE_INFO_STR_ERROR = "";
    private static final String HW_REMOTE_DEVICE_INFO_STR_OK = "OK";
    private static final String TAG = "BluetoothDeviceEx";
    public static final int TYPE_DEVICE_BR_RANGE_THRESHOLD = 20;
    public static final int TYPE_DEVICE_BR_READ_RANGE = 22;
    public static final int TYPE_DEVICE_BR_READ_RSSI = 21;
    private BluetoothDevice mDevice;

    public BluetoothDeviceEx(BluetoothDevice device) {
        this.mDevice = device;
    }

    public BluetoothDeviceEx(Parcel in) {
        Log.d(TAG, "Mac Address from Parcel is: " + in.readString());
    }

    public static boolean isEncrypted(BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice != null) {
            return BluetoothDeviceAdapterEx.isEncrypted(bluetoothDevice);
        }
        return false;
    }

    public static boolean isConnected(BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice != null) {
            return BluetoothDeviceAdapterEx.isConnected(bluetoothDevice);
        }
        return false;
    }

    public boolean authorizeService(String service, boolean authorized, boolean always) {
        throw new NoExtAPIException("method not supported.");
    }

    public boolean getBlockedState() {
        throw new NoExtAPIException("method not supported.");
    }

    public byte getDeviceType() {
        throw new NoExtAPIException("method not supported.");
    }

    public boolean setBlockedState(int value) {
        throw new NoExtAPIException("method not supported.");
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mDevice.getAddress());
    }

    public static String getDeviceInfo(BluetoothDevice device, int type) {
        if (device != null) {
            return BluetoothDeviceAdapterEx.getDeviceInfo(device, type);
        }
        Log.e(TAG, "getHwDeviceInfoByType got null device");
        return "";
    }

    public static String getDeviceInfo(BluetoothDevice device, int type, BluetoothDeviceInforCallback callback) {
        if (device == null) {
            Log.e(TAG, "getHwDeviceInfoByType got null device");
            return "";
        } else if (callback == null) {
            Log.e(TAG, "getHwDeviceInfoByType callback null");
            return "";
        } else {
            BluetoothDeviceInfor.getDefault().getDeviceInfor(device, type, callback);
            return HW_REMOTE_DEVICE_INFO_STR_OK;
        }
    }

    public static boolean updateDeviceInfoByType(BluetoothDevice device, int type, String value) {
        if (device != null) {
            return BluetoothDeviceAdapterEx.updateDeviceInfoByType(device, type, value);
        }
        Log.e(TAG, "updateDeviceInfoByType got null device");
        return false;
    }
}

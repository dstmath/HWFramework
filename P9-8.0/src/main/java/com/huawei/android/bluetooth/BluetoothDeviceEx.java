package com.huawei.android.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import com.huawei.android.util.NoExtAPIException;

public class BluetoothDeviceEx implements Parcelable {
    public static final Creator<BluetoothDeviceEx> CREATOR = new Creator<BluetoothDeviceEx>() {
        public BluetoothDeviceEx createFromParcel(Parcel in) {
            return new BluetoothDeviceEx(in);
        }

        public BluetoothDeviceEx[] newArray(int size) {
            return new BluetoothDeviceEx[size];
        }
    };
    private static final String TAG = "BluetoothDevice";
    private BluetoothDevice mDevice;

    public BluetoothDeviceEx(BluetoothDevice device) {
        this.mDevice = device;
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

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mDevice.getAddress());
    }

    public BluetoothDeviceEx(Parcel in) {
        String address = in.readString();
        String regularMac = ":[\\w]{1,}:[\\w]{1,}:";
        if (address != null) {
            Log.d(TAG, "address = " + address.replaceAll(regularMac, ":**:**:"));
        }
    }

    public static boolean isConnected(BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice != null) {
            return bluetoothDevice.isConnected();
        }
        return false;
    }

    public static boolean isEncrypted(BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice != null) {
            return bluetoothDevice.isEncrypted();
        }
        return false;
    }
}

package com.huawei.android.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.android.util.NoExtAPIException;

public class BluetoothDeviceEx implements Parcelable {
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
}

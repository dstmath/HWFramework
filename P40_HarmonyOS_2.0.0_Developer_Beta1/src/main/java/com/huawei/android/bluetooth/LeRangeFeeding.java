package com.huawei.android.bluetooth;

import android.bluetooth.BluetoothAdapterEx;
import android.bluetooth.le.ScanResult;
import android.os.Parcel;
import android.util.Log;

public class LeRangeFeeding {
    private static final int CODE_FEED_RSSI = 1007;
    private static final String DESCRIPTOR = "android.bluetooth.IBluetooth";
    private static final String TAG = "LeRangeFeeding";
    private String mUuid;

    public LeRangeFeeding(String uuid) {
        this.mUuid = uuid;
    }

    public String getUuid() {
        Log.d(TAG, "getUuid");
        return this.mUuid;
    }

    public void feedRssi(ScanResult result) {
        Log.d(TAG, "feedRssi");
        if (result == null) {
            Log.e(TAG, "feedRssi result is null");
            return;
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(DESCRIPTOR);
        result.writeToParcel(data, 0);
        data.writeString(this.mUuid);
        if (!BluetoothAdapterEx.feedRssi((int) CODE_FEED_RSSI, data, reply)) {
            Log.e(TAG, "feedRssi error");
        }
    }
}

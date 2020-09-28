package com.huawei.android.bluetooth;

import android.bluetooth.BluetoothUuidAdapterEx;
import android.os.ParcelUuid;

public class BluetoothUuidEx {
    public static boolean isUuidPresent(ParcelUuid[] uuidArray, ParcelUuid uuid) {
        return BluetoothUuidAdapterEx.isUuidPresent(uuidArray, uuid);
    }

    public static final ParcelUuid getObexObjectPush() {
        return BluetoothUuidAdapterEx.getObexObjectPush();
    }
}

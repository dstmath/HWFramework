package com.huawei.android.bluetooth;

import android.bluetooth.BluetoothUuid;
import android.os.ParcelUuid;

public class BluetoothUuidEx {
    public static boolean isUuidPresent(ParcelUuid[] uuidArray, ParcelUuid uuid) {
        return BluetoothUuid.isUuidPresent(uuidArray, uuid);
    }

    public static final ParcelUuid getObexObjectPush() {
        return BluetoothUuid.ObexObjectPush;
    }
}

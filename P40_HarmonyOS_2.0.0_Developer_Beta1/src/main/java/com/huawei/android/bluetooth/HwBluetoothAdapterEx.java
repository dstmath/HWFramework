package com.huawei.android.bluetooth;

import android.bluetooth.BluetoothAdapterEx;
import android.os.Bundle;

public class HwBluetoothAdapterEx {
    public static byte[] createHiLinkAdv(int key, Bundle values) {
        return BluetoothAdapterEx.createHiLinkAdv(key, values);
    }
}

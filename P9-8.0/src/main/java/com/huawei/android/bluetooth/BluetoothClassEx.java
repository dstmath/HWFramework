package com.huawei.android.bluetooth;

import android.bluetooth.BluetoothClass;

public class BluetoothClassEx {
    public static final int getProfileOpp() {
        return 2;
    }

    public static boolean doesClassMatch(BluetoothClass btclass, int profile) {
        return btclass.doesClassMatch(profile);
    }
}

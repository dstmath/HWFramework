package com.android.bluetooth;

import android.bluetooth.BluetoothClass;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class BluetoothClassAdapterEx {
    public static final int PROFILE_OPP = 2;

    public static boolean doesClassMatch(BluetoothClass btclass, int profile) {
        return btclass.doesClassMatch(profile);
    }
}

package com.huawei.android.os;

import huawei.android.os.HwSdCardCryptdManager;

public class HwSdCardCryptdEx {
    public static int setSdCardCryptdEnable(boolean enable, String volId) {
        return HwSdCardCryptdManager.getInstance().setSdCardCryptdEnable(enable, volId);
    }
}

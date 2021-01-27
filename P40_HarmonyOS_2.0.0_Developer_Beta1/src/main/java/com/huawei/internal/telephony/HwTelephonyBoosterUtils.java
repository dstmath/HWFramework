package com.huawei.internal.telephony;

import android.common.HwFrameworkFactory;
import android.net.booster.IHwCommBoosterServiceManager;

public class HwTelephonyBoosterUtils {
    public static IHwCommBoosterServiceManager getHwCommBoosterServiceManager() {
        return HwFrameworkFactory.getHwCommBoosterServiceManager();
    }
}

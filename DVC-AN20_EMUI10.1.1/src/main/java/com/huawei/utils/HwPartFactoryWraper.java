package com.huawei.utils;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.hwcontrol.HwWidgetFactory;
import android.vrsystem.IVRSystemServiceManager;

public class HwPartFactoryWraper {
    private HwPartFactoryWraper() {
    }

    public static IVRSystemServiceManager getVRSystemServiceManager() {
        return HwFrameworkFactory.getVRSystemServiceManager();
    }

    public static boolean isHwTheme(Context context) {
        return HwWidgetFactory.isHwTheme(context);
    }
}

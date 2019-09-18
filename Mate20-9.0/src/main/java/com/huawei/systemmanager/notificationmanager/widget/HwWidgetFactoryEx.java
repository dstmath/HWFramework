package com.huawei.systemmanager.notificationmanager.widget;

import android.content.Context;
import com.huawei.android.hwcontrol.HwWidgetFactoryImpl;

public class HwWidgetFactoryEx {
    public static boolean isHwDarkTheme(Context context) {
        return new HwWidgetFactoryImpl().isHwDarkTheme(context);
    }
}

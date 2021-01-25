package com.huawei.util;

import android.util.Log;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class LogEx {
    @HwSystemApi
    public static boolean getLogHWInfo() {
        return Log.HWINFO;
    }

    @HwSystemApi
    public static boolean getHWModuleLog() {
        return Log.HWModuleLog;
    }

    @HwSystemApi
    public static boolean getHwLog() {
        return Log.HWLog;
    }
}

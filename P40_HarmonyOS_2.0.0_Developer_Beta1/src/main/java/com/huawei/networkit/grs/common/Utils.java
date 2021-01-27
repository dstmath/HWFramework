package com.huawei.networkit.grs.common;

import android.os.SystemClock;

public class Utils {
    public static long getCurrentTime(boolean bRealTime) {
        if (bRealTime) {
            return SystemClock.elapsedRealtime();
        }
        return System.currentTimeMillis();
    }
}

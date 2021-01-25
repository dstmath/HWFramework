package com.android.internal.telephony;

import android.app.ActivityManager;
import android.content.Context;
import com.huawei.hwparttelephonyopt.BuildConfig;
import java.util.List;

public class HwTelephonyActivityManagerUtils {
    private HwTelephonyActivityManagerUtils() {
    }

    public static String getAppName(Context context, int pid) {
        ActivityManager am;
        List<ActivityManager.RunningAppProcessInfo> processInfoList;
        if (context == null || (am = (ActivityManager) context.getSystemService("activity")) == null || (processInfoList = am.getRunningAppProcesses()) == null) {
            return BuildConfig.FLAVOR;
        }
        for (ActivityManager.RunningAppProcessInfo info : processInfoList) {
            if (info != null && info.pid == pid) {
                return info.processName;
            }
        }
        return BuildConfig.FLAVOR;
    }
}

package com.msic.qarth.Utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.SystemProperties;

public class CommonUtil {
    private static final String TAG = "CommonUtil";

    public static String determineCpuAbi(Context context) {
        String cpuAbi = SystemProperties.get("ro.product.cpu.abi");
        if (context == null) {
            if (cpuAbi == null || cpuAbi.isEmpty()) {
                cpuAbi = "armeabi-v7a";
            }
            return cpuAbi;
        }
        ApplicationInfo info = context.getApplicationInfo();
        if (info != null) {
            cpuAbi = info.primaryCpuAbi;
            if (cpuAbi == null && info.secondaryCpuAbi != null) {
                cpuAbi = info.secondaryCpuAbi;
            }
        }
        return cpuAbi;
    }
}

package com.msic.qarth.Utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.SystemProperties;

public class CommonUtil {
    private static final String TAG = "CommonUtil";

    public static String determineCpuAbi(Context context) {
        String cpuAbi = SystemProperties.get("ro.product.cpu.abi");
        if (context != null) {
            ApplicationInfo info = context.getApplicationInfo();
            if (info == null) {
                return cpuAbi;
            }
            String cpuAbi2 = info.primaryCpuAbi;
            if (cpuAbi2 != null || info.secondaryCpuAbi == null) {
                return cpuAbi2;
            }
            return info.secondaryCpuAbi;
        } else if (cpuAbi == null || cpuAbi.isEmpty()) {
            return "armeabi-v7a";
        } else {
            return cpuAbi;
        }
    }
}

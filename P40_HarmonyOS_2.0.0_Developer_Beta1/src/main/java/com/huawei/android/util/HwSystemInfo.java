package com.huawei.android.util;

import android.os.SystemProperties;
import android.util.Log;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HwSystemInfo {
    private static final long GB_IN_KB = 1048576;
    static final String LOG_TAG = "DeviceInfo";
    private static int configEmmcSize = SystemProperties.getInt("ro.config.hw_emmcSize", -1);
    private static int configRamSize = SystemProperties.getInt("ro.config.hw_ramSize", -1);
    private static final Pattern sEmmcSizePattern = Pattern.compile("\\s\\d+\\s+\\d+\\s+(\\d+)\\smmcblk0");
    private static final String sKernelCmdLine = getProcInfo("/proc/meminfo");
    private static final String sKernelPartitions = getProcInfo("/proc/partitions");
    private static final Pattern sRamSizePattern = Pattern.compile("MemTotal:\\s*(\\d+)\\s*");

    public static String getDeviceRam() {
        int i = configRamSize;
        if (-1 != i) {
            return String.valueOf(i);
        }
        String ramSize = "";
        Matcher matcher = sRamSizePattern.matcher(sKernelCmdLine);
        if (matcher.find()) {
            ramSize = matcher.group(1);
        } else {
            Log.e(LOG_TAG, "Ram Info not found, display nothing");
        }
        long ramLong = 0;
        try {
            ramLong = Long.parseLong(ramSize);
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "getDeviceRam catch NumberFormatException : " + e.toString());
        }
        if (ramLong > 0) {
            long timesToGb = ramLong / GB_IN_KB;
            if (ramLong % GB_IN_KB != 0) {
                ramSize = String.valueOf((1 + timesToGb) * GB_IN_KB);
            }
        }
        Log.d(LOG_TAG, "ramSize =" + ramSize);
        return ramSize;
    }

    public static String getDeviceEmmc() {
        int i = configEmmcSize;
        if (-1 != i) {
            return String.valueOf(i);
        }
        String emmcSize = "";
        Matcher matcher = sEmmcSizePattern.matcher(sKernelPartitions);
        if (matcher.find()) {
            emmcSize = matcher.group(1);
        } else {
            Log.e(LOG_TAG, "Emmc Info not found, display nothing");
        }
        Log.d(LOG_TAG, "emmcSize =" + emmcSize);
        return emmcSize;
    }

    private static String getProcInfo(String path) {
        String procInfo = "";
        FileInputStream is = null;
        try {
            FileInputStream is2 = new FileInputStream(path);
            byte[] buffer = new byte[2048];
            int count = is2.read(buffer);
            if (count > 0) {
                procInfo = new String(buffer, 0, count, "UTF-8");
            }
            try {
                is2.close();
            } catch (IOException e) {
            }
        } catch (IOException e2) {
            Log.d(LOG_TAG, "No path exception=" + e2);
            if (0 != 0) {
                is.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    is.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
        return procInfo;
    }
}

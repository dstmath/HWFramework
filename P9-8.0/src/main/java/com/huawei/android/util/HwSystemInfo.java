package com.huawei.android.util;

import android.os.SystemProperties;
import android.util.Log;
import com.huawei.hsm.permission.StubController;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HwSystemInfo {
    private static final long GB_IN_KB = 1048576;
    private static final long KB_IN_B = 1024;
    static final String LOG_TAG = "DeviceInfo";
    private static int configEmmcSize = SystemProperties.getInt("ro.config.hw_emmcSize", -1);
    private static int configRamSize = SystemProperties.getInt("ro.config.hw_ramSize", -1);
    private static final Pattern sEmmcSizePattern = Pattern.compile("\\s\\d+\\s+\\d+\\s+(\\d+)\\smmcblk0");
    private static final String sKernelCmdLine = getProcInfo("/proc/meminfo");
    private static final String sKernelPartitions = getProcInfo("/proc/partitions");
    private static final Pattern sRamSizePattern = Pattern.compile("MemTotal:\\s*(\\d+)\\s*");
    private static final long sSectorLong = 512;
    private static final String sSectorSize = getProcInfo("/proc/bootdevice/size");

    public static String getDeviceRam() {
        if (-1 != configRamSize) {
            return String.valueOf(configRamSize);
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
            e.printStackTrace();
        }
        if (ramLong > 0) {
            long timesToGb = ramLong / GB_IN_KB;
            if (ramLong % GB_IN_KB != 0) {
                ramSize = String.valueOf(GB_IN_KB * (1 + timesToGb));
            }
        }
        Log.d(LOG_TAG, "ramSize =" + ramSize);
        return ramSize;
    }

    public static String getDeviceEmmc() {
        if (-1 != configEmmcSize) {
            return String.valueOf(configEmmcSize);
        }
        String emmcSize = "";
        Matcher matcher = sEmmcSizePattern.matcher(sKernelPartitions);
        if (matcher.find()) {
            emmcSize = matcher.group(1);
        } else {
            Log.e(LOG_TAG, "Emmc Info not found, display nothing");
        }
        if ("".equals(emmcSize)) {
            long emmcLong = 0;
            long sectors = 0;
            try {
                sectors = Long.parseLong(sSectorSize.trim());
            } catch (NumberFormatException e) {
                Log.e(LOG_TAG, "getDeviceEmmc has NumberFormatException : " + e);
            }
            if (sectors > 0) {
                emmcLong = (sSectorLong * sectors) / KB_IN_B;
            }
            emmcSize = String.valueOf(emmcLong);
        }
        Log.d(LOG_TAG, "emmcSize =" + emmcSize);
        return emmcSize;
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0043 A:{SYNTHETIC, Splitter: B:19:0x0043} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x004c A:{SYNTHETIC, Splitter: B:24:0x004c} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String getProcInfo(String path) {
        IOException e;
        Throwable th;
        String procInfo = "";
        FileInputStream is = null;
        try {
            FileInputStream is2 = new FileInputStream(path);
            try {
                byte[] buffer = new byte[StubController.PERMISSION_CALENDAR];
                int count = is2.read(buffer);
                if (count > 0) {
                    procInfo = new String(buffer, 0, count, "UTF-8");
                }
                if (is2 != null) {
                    try {
                        is2.close();
                    } catch (IOException e2) {
                    }
                }
                is = is2;
            } catch (IOException e3) {
                e = e3;
                is = is2;
                try {
                    Log.d(LOG_TAG, "No path exception=" + e);
                    if (is != null) {
                    }
                    return procInfo;
                } catch (Throwable th2) {
                    th = th2;
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e4) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                is = is2;
                if (is != null) {
                }
                throw th;
            }
        } catch (IOException e5) {
            e = e5;
            Log.d(LOG_TAG, "No path exception=" + e);
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e6) {
                }
            }
            return procInfo;
        }
        return procInfo;
    }
}

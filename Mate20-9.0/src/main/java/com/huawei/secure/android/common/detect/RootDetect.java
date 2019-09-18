package com.huawei.secure.android.common.detect;

import android.os.Build;
import android.util.Log;
import com.huawei.secure.android.common.util.LogsUtil;
import com.huawei.secure.android.common.util.ReflectUtil;
import java.io.File;

public final class RootDetect {
    private static final String BUSYBOX = "busybox";
    private static final String DEFAULT_PATH = "/sbin:/vendor/bin:/system/sbin:/system/bin:/system/xbin:/system/bin/.ext:/system/sd/xbin:/system/usr/we-need-root:/cache:/data:/dev:/system/vendor/bin:/vendor/xbin:/system/vendor/xbin:/product/bin:/product/xbin:/data/local/tmp:/data/local/bin:/data/local/xbin:/data/local:/system/bin/failsafe";
    private static final String MAGISK = "magisk";
    private static final String SU_FILE = "su";
    private static final String TAG = "RootDetect";

    private RootDetect() {
    }

    public static boolean isRoot() {
        if (Build.VERSION.SDK_INT >= 27) {
            if (SD.irtj()) {
                LogsUtil.e(TAG, "root exists", true);
                return true;
            }
        } else if (checkSu()) {
            LogsUtil.e(TAG, "su file exists", true);
            return true;
        } else if (checkSecureProperty()) {
            LogsUtil.e(TAG, "SecureProperty is wrong", true);
            return true;
        } else if (checkEmulator() || SD.iej()) {
            LogsUtil.i(TAG, "app run in emulator", true);
            return true;
        } else if (checkBuildTags()) {
            LogsUtil.i(TAG, "build.tags is wrong", true);
            return true;
        } else if (checkMagisk()) {
            LogsUtil.e(TAG, "Magisk exists", true);
            return true;
        }
        return false;
    }

    private static boolean checkSu() {
        return checkForBinary(SU_FILE);
    }

    private static boolean checkForBinary(String filename) {
        for (String path : DEFAULT_PATH.split(":")) {
            if (new File(path + File.separator + filename).exists()) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkMagisk() {
        return checkForBinary(MAGISK);
    }

    private static boolean checkBusybox() {
        return checkForBinary(BUSYBOX);
    }

    private static boolean checkBuildTags() {
        String buildTags = Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private static boolean checkEmulator() {
        boolean isEmulator = false;
        try {
            if (Build.HARDWARE.contains("goldfish") || Build.FINGERPRINT.contains("generic") || Build.MODEL.contains("google_sdk") || Build.MODEL.contains("Emulator") || Build.MODEL.contains("Android SDK built for x86") || Build.MODEL.contains("Android SDK built for arm64") || "google_sdk".equals(Build.PRODUCT)) {
                isEmulator = true;
            }
            return isEmulator;
        } catch (Exception e) {
            Log.e(TAG, "Check emulator " + e.getMessage());
            return false;
        }
    }

    private static boolean checkSecureProperty() {
        return "0".equals(ReflectUtil.getSystemProperty("ro.secure"));
    }
}

package com.huawei.android.app;

import com.huawei.android.content.pm.HwPackageManager;

public class MspesEx {
    public static String readFile(String fileName) {
        return HwPackageManager.readMspesFile(fileName);
    }

    public static boolean writeFile(String fileName, String content) {
        return HwPackageManager.writeMspesFile(fileName, content);
    }

    public static String getMspesOEMConfig() {
        return HwPackageManager.getMspesOEMConfig();
    }

    public static int updateMspesOEMConfig(String src) {
        return HwPackageManager.updateMspesOEMConfig(src);
    }
}

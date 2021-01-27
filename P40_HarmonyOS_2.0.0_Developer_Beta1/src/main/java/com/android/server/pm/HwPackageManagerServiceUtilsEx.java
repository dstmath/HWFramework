package com.android.server.pm;

import android.content.pm.PackageParser;
import android.content.pm.PackageParserEx;
import java.util.HashMap;
import java.util.HashSet;

public class HwPackageManagerServiceUtilsEx {
    public static final int EVENT_UNINSTALLED_APPLICATION = 907400027;

    public static long hwTimingsBegin() {
        return HwPackageManagerServiceUtils.hwTimingsBegin();
    }

    public static void hwTimingsEnd(String tag, String op, long begin) {
        HwPackageManagerServiceUtils.hwTimingsEnd(tag, op, begin);
    }

    public static void addFlagsForRemovablePreApk(PackageParserEx.PackageEx pkg, int hwFlags) {
        HwPackageManagerServiceUtils.addFlagsForRemovablePreApk((PackageParser.Package) pkg.getPackage(), hwFlags);
    }

    public static boolean isNoSystemPreApp(String codePath) {
        return HwPackageManagerServiceUtils.isNoSystemPreApp(codePath);
    }

    public static boolean isSupportCloneAppInCust(String packageName) {
        return HwPackageManagerService.isSupportCloneAppInCust(packageName);
    }

    public static String getCustPackagePath(String codePath) {
        return HwPackageManagerServiceUtils.getCustPackagePath(codePath);
    }

    public static void reportPmsParseFileException(String fileName, String exceptionName, int userId, String areaName) {
        HwPackageManagerServiceUtils.reportPmsParseFileException(fileName, exceptionName, userId, areaName);
    }

    public static void setDelMultiInstallMap(HashMap<String, HashSet<String>> delMultiInstallMap) {
        HwPackageManagerServiceUtils.setDelMultiInstallMap(delMultiInstallMap);
    }
}

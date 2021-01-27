package com.msic.qarth;

public class QarthVersion {
    private static final int PATCH_FILE_VERSION_LENGTH = 3;
    public static final String QARTH_VERSION = "0.0.1";
    private static final String TAG = "QarthVersion";

    public static int compareVersion(QarthContext qc) {
        if (qc == null || qc.qarthVersion == null) {
            return -1;
        }
        String[] fileVersions = qc.qarthVersion.split("\\.");
        String[] patchVersion = QARTH_VERSION.split("\\.");
        if (!checkPatchFileVersionFormat(fileVersions)) {
            QarthLog.e(TAG, "patch file version format err");
            return -1;
        }
        for (int patchIndex = 0; patchIndex < patchVersion.length; patchIndex++) {
            try {
                if (Integer.parseInt(patchVersion[patchIndex]) != Integer.parseInt(fileVersions[patchIndex])) {
                    return -1;
                }
            } catch (NumberFormatException e) {
                QarthLog.e(TAG, "parse patch version exception");
                return -1;
            }
        }
        return 0;
    }

    private static boolean checkPatchFileVersionFormat(String[] patchFileVersions) {
        if (patchFileVersions.length != 3) {
            return false;
        }
        for (String patchVersionItem : patchFileVersions) {
            try {
                Integer.parseInt(patchVersionItem);
            } catch (NumberFormatException e) {
                QarthLog.e(TAG, "parse patch file version exception:" + patchVersionItem);
                return false;
            }
        }
        return true;
    }
}

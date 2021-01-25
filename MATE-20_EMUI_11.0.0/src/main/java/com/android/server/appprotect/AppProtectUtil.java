package com.android.server.appprotect;

import android.text.TextUtils;
import com.android.server.pm.PackageManagerService;
import java.io.File;

public class AppProtectUtil {
    private static final int MAX_FILE_SIZE = 10485760;
    private static final String STRING_EMPTY = "";
    private static final String TAG = "AppProtectUtil";
    private static PackageManagerService sPms = null;

    public static void setPms(PackageManagerService pms) {
        sPms = pms;
    }

    public static String getInstallerPackageName(String packageName) {
        PackageManagerService packageManagerService;
        if (TextUtils.isEmpty(packageName) || (packageManagerService = sPms) == null) {
            return "";
        }
        return packageManagerService.getInstallerPackageName(packageName);
    }

    static boolean verifyFile(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.length() < 10485760;
    }
}

package com.android.server.pm;

import android.content.pm.ApplicationInfo;
import android.util.Flog;
import android.util.Log;
import java.io.File;

public class HwPackageManagerUtils {
    private static final String FILE_POLICY_CLASS_NAME = "com.huawei.cust.HwCfgFilePolicy";
    private static final String METHOD_NAME_FOR_FILE = "getCfgFile";
    static final String TAG = "HwPackageManagerUtils";

    public static File getCfgFile(String fileName, int type) throws Exception, NoClassDefFoundError {
        Class<?> filePolicyClazz = Class.forName(FILE_POLICY_CLASS_NAME);
        return (File) filePolicyClazz.getMethod(METHOD_NAME_FOR_FILE, new Class[]{String.class, Integer.TYPE}).invoke(filePolicyClazz, new Object[]{fileName, Integer.valueOf(type)});
    }

    public static File getCustomizedFileName(String xmlName, int flag) {
        try {
            return getCfgFile("xml/" + xmlName, flag);
        } catch (NoClassDefFoundError e) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            return null;
        } catch (Exception e2) {
            Log.d(TAG, "getCustomizedFileName get layout file exception");
            return null;
        }
    }

    public static final boolean isPackageFilename(String name) {
        return name != null && name.endsWith(".apk");
    }

    public static boolean isHaveApkFile(File[] dirs, String codePath) {
        for (File dir : dirs) {
            if (dir != null && dir.exists()) {
                for (String fi : dir.list()) {
                    File file = new File(dir, fi);
                    String[] filesSub = file.list();
                    if (file.getPath().equals(codePath)) {
                        for (String subFile : filesSub) {
                            if (isPackageFilename(subFile)) {
                                return true;
                            }
                        }
                        continue;
                    }
                }
                continue;
            }
        }
        return false;
    }

    public static boolean isSystemApp(ApplicationInfo appInfo) {
        boolean isSystemApp = false;
        if (appInfo != null) {
            boolean z = true;
            if ((appInfo.flags & 1) == 0) {
                z = false;
            }
            isSystemApp = z;
        }
        Flog.d(205, "Android Wear-checkLimitePackageBroadcast: isSystemApp=" + isSystemApp);
        return isSystemApp;
    }
}

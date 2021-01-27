package com.android.server.pm;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParserEx;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.content.pm.ApplicationInfoEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.util.SlogEx;
import com.huawei.hwpartbasicplatformservices.BuildConfig;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class HwPackageManagerUtils extends DefaultHwPackageManagerUtils {
    private static final String FILE_POLICY_CLASS_NAME = "com.huawei.cust.HwCfgFilePolicy";
    private static final String METHOD_NAME_FOR_FILE = "getCfgFile";
    static final String TAG = "HwPackageManagerUtils";

    public static File getCfgFile(String fileName, int type) throws Exception, NoClassDefFoundError {
        Class<?> filePolicyClass = Class.forName(FILE_POLICY_CLASS_NAME);
        Object result = filePolicyClass.getMethod(METHOD_NAME_FOR_FILE, String.class, Integer.TYPE).invoke(filePolicyClass, fileName, Integer.valueOf(type));
        if (result instanceof File) {
            return (File) result;
        }
        Log.w(TAG, "com.huawei.cust.HwCfgFilePolicy:getCfgFile not return File object!");
        return null;
    }

    public File getCustomizedFileNameEx(String xmlName, int flag) {
        return getCustomizedFileName(xmlName, flag);
    }

    public static File getCustomizedFileName(String xmlName, int flag) {
        if (xmlName == null) {
            return null;
        }
        try {
            return getCfgFile("xml/" + xmlName, flag);
        } catch (NoClassDefFoundError e) {
            Log.e(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            return null;
        } catch (Exception e2) {
            Log.e(TAG, "getCustomizedFileName get layout file exception");
            return null;
        }
    }

    public boolean isPackageFilenameEx(String name) {
        return isPackageFilename(name);
    }

    public static final boolean isPackageFilename(String name) {
        return name != null && name.toLowerCase(Locale.ENGLISH).endsWith(".apk");
    }

    public boolean isHepFileNameEx(String name) {
        return isHepFileName(name);
    }

    public static final boolean isHepFileName(String name) {
        return name != null && name.toLowerCase(Locale.ENGLISH).endsWith(".hep");
    }

    public static boolean isHaveApkFile(File[] dirs, String codePath) {
        String[] files;
        if (dirs == null || codePath == null) {
            return false;
        }
        for (File dir : dirs) {
            if (dir != null && dir.exists() && (files = dir.list()) != null && isContainInTheSubFiles(codePath, dir, files)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isContainInTheSubFiles(String codePath, File dir, String[] files) {
        for (String fileName : files) {
            File file = new File(dir, fileName);
            String[] filesSubs = file.list();
            if (file.getPath().equals(codePath) && filesSubs != null) {
                for (String subFile : filesSubs) {
                    if (isPackageFilename(subFile)) {
                        return true;
                    }
                }
                continue;
            }
        }
        return false;
    }

    public static boolean isDynamicApplication(ApplicationInfo applicationInfo) {
        return (applicationInfo == null || applicationInfo.splitNames == null || applicationInfo.splitNames.length <= 0) ? false : true;
    }

    public static boolean isBundleApplication(ApplicationInfo applicationInfo) {
        if (applicationInfo == null || !isDynamicApplication(applicationInfo)) {
            return false;
        }
        int[] splitFlags = ApplicationInfoEx.getHwSplitFlags(applicationInfo);
        for (int i = 0; i < applicationInfo.splitNames.length; i++) {
            if ((splitFlags[i] & 536870912) != 0) {
                return true;
            }
            if ((splitFlags[i] & 1073741824) != 0 && (splitFlags[i] & Integer.MIN_VALUE) == 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSplitApplication(ApplicationInfo applicationInfo) {
        if (applicationInfo == null || !isDynamicApplication(applicationInfo)) {
            return false;
        }
        int[] splitFlags = ApplicationInfoEx.getHwSplitFlags(applicationInfo);
        for (int i = 0; i < applicationInfo.splitNames.length; i++) {
            if (!((splitFlags[i] & 1073741824) == 0 || (splitFlags[i] & Integer.MIN_VALUE) == 0)) {
                return true;
            }
        }
        return false;
    }

    public String getPackageNameFromApkEx(String apkFile) {
        return getPackageNameFromApk(apkFile);
    }

    public static String getPackageNameFromApk(String apkFile) {
        PackageParserEx.ApkLiteEx apkLite;
        if (TextUtils.isEmpty(apkFile)) {
            return BuildConfig.FLAVOR;
        }
        try {
            File tempFile = new File(apkFile.trim()).getCanonicalFile();
            if (tempFile.exists() && (apkLite = PackageParserEx.parseApkLite(tempFile, 0)) != null) {
                return apkLite.getPackageName();
            }
            return BuildConfig.FLAVOR;
        } catch (IOException e) {
            SlogEx.e(TAG, "getPackageNameFromApk IOException:" + apkFile);
            return BuildConfig.FLAVOR;
        } catch (PackageParserEx.PackageParserExceptionEx e2) {
            SlogEx.e(TAG, "getPackageNameFromApk PackageParserException:" + apkFile);
            return BuildConfig.FLAVOR;
        }
    }

    public boolean isUserUnlockedEx(Context context) {
        return isUserUnlocked(context);
    }

    public static boolean isUserUnlocked(Context context) {
        UserManager userManager;
        if (context == null || (userManager = (UserManager) context.getSystemService("user")) == null) {
            return false;
        }
        return userManager.isUserUnlocked(UserHandleEx.getUserHandle(0));
    }

    public static String getPackageNameFromFile(File file) {
        if (file == null) {
            SlogEx.e(TAG, "getPackageNameFromFile file is null");
            return BuildConfig.FLAVOR;
        } else if (!file.exists()) {
            SlogEx.e(TAG, "getPackageNameFromFile file is not exist");
            return BuildConfig.FLAVOR;
        } else {
            try {
                return PackageParserEx.parsePackageLite(file, 0).getPackageName();
            } catch (PackageParserEx.PackageParserExceptionEx e) {
                SlogEx.e(TAG, "getPackageNameFromFile occure PackageParserException:" + file.getPath());
                return BuildConfig.FLAVOR;
            } catch (Exception e2) {
                SlogEx.e(TAG, "getPackageNameFromFile occure Exception: " + file.getPath());
                return BuildConfig.FLAVOR;
            }
        }
    }
}

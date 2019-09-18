package com.android.server.security.securityprofile;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Slog;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SecurityProfileUtils {
    private static final int DEFAULT_USER_ID = 0;
    private static final String TAG = "SecurityProfileUtils";

    public static List<Integer> getUserIdListOnPhone(Context context) {
        List<Integer> UserIdList = new ArrayList<>();
        try {
            for (UserInfo userInfo : ((UserManager) context.getSystemService("user")).getUsers()) {
                UserIdList.add(Integer.valueOf(userInfo.id));
            }
        } catch (Exception e) {
            Slog.e(TAG, "get user id list err:" + e.getMessage() + ".i must use default user id:" + 0);
            UserIdList.add(0);
        }
        return UserIdList;
    }

    public static List<String> getInstalledPackages(Context context) {
        List<String> result = new ArrayList<>();
        HashSet<String> set = new HashSet<>();
        for (Integer intValue : getUserIdListOnPhone(context)) {
            for (PackageInfo info : context.getPackageManager().getInstalledPackagesAsUser(0, intValue.intValue())) {
                set.add(info.packageName);
            }
        }
        result.addAll(set);
        return result;
    }

    public static String getInstalledApkPath(String packageName, Context context) {
        for (Integer intValue : getUserIdListOnPhone(context)) {
            int userId = intValue.intValue();
            try {
                return context.getPackageManager().getApplicationInfoAsUser(packageName, 0, userId).sourceDir;
            } catch (PackageManager.NameNotFoundException e) {
                Slog.d(TAG, "getInstalledApkPath name not found,packageName:" + packageName + ",userId:" + userId);
            }
        }
        return null;
    }

    public static boolean isAccessibilitySelectToSpeakActive(Context context) {
        return isAccessibilitySelectToSpeakActive(context, getCurrentActiveUserId());
    }

    public static boolean isAccessibilitySelectToSpeakActive(Context context, int userId) {
        String enabledServicesSetting = Settings.Secure.getStringForUser(context.getContentResolver(), "enabled_accessibility_services", userId);
        if (enabledServicesSetting == null || !enabledServicesSetting.contains("SelectToSpeakService")) {
            return false;
        }
        return true;
    }

    public static int getCurrentActiveUserId() {
        return ActivityManager.getCurrentUser();
    }
}

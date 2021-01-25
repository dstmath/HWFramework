package com.huawei.server.security.permissionmanager.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.UserManager;
import android.support.annotation.NonNull;
import android.util.Log;
import com.huawei.android.content.pm.PackageManagerExt;
import com.huawei.android.content.pm.UserInfoExt;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserManagerExt;
import com.huawei.hwpartsecurityservices.BuildConfig;
import com.huawei.util.LogEx;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HwPermUtils {
    public static final int DEFAULT_LIST_CAPACITY = 10;
    public static final int DEFAULT_PACKAGES_CAPACITY = 128;
    public static final int DEFAULT_SET_CAPACIY = 16;
    private static final int DEFAULT_USERS_CAPACITY = 8;
    public static final boolean IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemPropertiesEx.get("ro.product.locale.region", BuildConfig.FLAVOR));
    public static final boolean IS_DEBUG = LogEx.getLogHWInfo();
    private static final String TAG = "HwPermUtils";

    private HwPermUtils() {
    }

    @NonNull
    public static List<Integer> getUserIdListOnPhone(@NonNull Context context) {
        List<Integer> userIdList = new ArrayList<>(8);
        UserManager userManager = null;
        if (context.getSystemService("user") instanceof UserManager) {
            userManager = (UserManager) context.getSystemService("user");
        }
        if (userManager == null) {
            Log.w(TAG, "get user manager null, I must use default user id");
            userIdList.add(0);
            return userIdList;
        }
        for (UserInfoExt userInfo : UserManagerExt.getUsers(userManager, false)) {
            userIdList.add(Integer.valueOf(userInfo.getUserId()));
        }
        if (userIdList.isEmpty()) {
            Log.w(TAG, "get user id list size 0, I must use default user id");
            userIdList.add(0);
        }
        return userIdList;
    }

    @NonNull
    public static List<String> getInstalledPackages(@NonNull Context context) {
        Set<String> tempSet = new HashSet<>(128);
        List<Integer> userIdList = getUserIdListOnPhone(context);
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            Log.w(TAG, "get package manager null, when get installed packages");
            return Collections.emptyList();
        }
        for (Integer num : userIdList) {
            for (PackageInfo info : PackageManagerExt.getInstalledPackagesAsUser(pm, 0, num.intValue())) {
                tempSet.add(info.packageName);
            }
        }
        return new ArrayList(tempSet);
    }
}

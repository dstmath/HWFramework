package com.huawei.networkit.grs.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import com.huawei.networkit.grs.common.Logger;
import java.util.Locale;

public class AgentUtil {
    private static final String TAG = "AgentUtil";
    private static String USER_AGENT = null;
    private static final String VERSION = "1.0.13.300";

    public static String getVersion() {
        return "1.0.13.300";
    }

    public static String getVersionName(Context context) {
        if (context == null) {
            return "";
        }
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Logger.w(TAG, "", e);
            return "";
        }
    }

    public static String getAgent(Context context, String sdkName) {
        if (context == null) {
            Locale locale = Locale.ROOT;
            return String.format(locale, sdkName + "/%s", getVersion());
        }
        String packageName = context.getPackageName();
        String versionName = getVersionName(context);
        String androidVersionName = Build.VERSION.RELEASE;
        String androidModel = Build.MODEL;
        Locale locale2 = Locale.ROOT;
        return String.format(locale2, "%s/%s (Linux; Android %s; %s) " + sdkName + "/%s", packageName, versionName, androidVersionName, androidModel, getVersion());
    }

    public static String getUserAgent(Context context, String sdkName) {
        if (USER_AGENT == null) {
            USER_AGENT = getAgent(context, sdkName);
        }
        return USER_AGENT;
    }
}

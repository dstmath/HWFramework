package com.android.server.pfw.autostartup.comm;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import com.android.server.pfw.log.HwPFWLogger;
import java.util.ArrayList;
import java.util.List;

public class AutoStartupUtil {
    private static final String TAG = "AutoStartupUtil";

    public static boolean isSystemUnRemovablePkg(ApplicationInfo applicationInfo) {
        if ((applicationInfo.flags & 1) == 0 || applicationInfo.hwFlags != 0) {
            return false;
        }
        return true;
    }

    public static boolean isAppStopped(ApplicationInfo applicationInfo) {
        return (applicationInfo.flags & 2097152) != 0;
    }

    public static boolean isComponentDisabled(PackageManager pm, ComponentInfo componentInfo) {
        int status = pm.getComponentEnabledSetting(new ComponentName(componentInfo.packageName, componentInfo.name));
        HwPFWLogger.i(TAG, "isComponentDisabled " + componentInfo + " result: " + status);
        if (2 == status || 3 == status) {
            return true;
        }
        return false;
    }

    public static List<String> getWidgetListFromLauncher(Context ctx) {
        List<String> result = new ArrayList();
        try {
            Bundle bundle = ctx.getContentResolver().call(Uri.parse("content://com.huawei.android.launcher.settings/widget/"), "get_widget", null, null);
            if (bundle != null) {
                List<String> widgetList = bundle.getStringArrayList("addedwidget");
                if (widgetList == null || widgetList.isEmpty()) {
                    HwPFWLogger.w(TAG, "getWidgetListFromLauncher empty bundle data.");
                    HwPFWLogger.d(TAG, "getWidgetListFromLauncher result is: " + result);
                    return result;
                }
                result.addAll(widgetList);
                HwPFWLogger.d(TAG, "getWidgetListFromLauncher result is: " + result);
                return result;
            }
            HwPFWLogger.w(TAG, "getWidgetListFromLauncher null return of call.");
            HwPFWLogger.d(TAG, "getWidgetListFromLauncher result is: " + result);
            return result;
        } catch (Exception ex) {
            HwPFWLogger.e(TAG, "call launcher widget get exception: " + ex.getMessage());
        }
    }
}

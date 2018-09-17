package tmsdkobf;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.os.Build.VERSION;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class mg {
    public static int a(boolean z, String str, String str2) {
        d.e("TMSLiteService--TMS", "tryOpenOps:[" + str2 + "]");
        d.e("TMSLiteService--TMS", "getPerOtherSwitch:[" + z + "]");
        if (!z) {
            return 0;
        }
        if (VERSION.SDK_INT >= 19) {
            Context applicaionContext = TMSDKContext.getApplicaionContext();
            try {
                d.e("TMSLiteService--TMS", "uid:[" + applicaionContext.getPackageManager().getApplicationInfo(str, 1).uid + "]");
                try {
                    Object systemService = applicaionContext.getSystemService("appops");
                    Class cls = Class.forName(systemService.getClass().getName());
                    Field declaredField = cls.getDeclaredField("mService");
                    declaredField.setAccessible(true);
                    cls.getDeclaredField(str2).setAccessible(true);
                    cls.getDeclaredField("MODE_ALLOWED").setAccessible(true);
                    systemService = declaredField.get(systemService);
                    Method declaredMethod = Class.forName(systemService.getClass().getName()).getDeclaredMethod("setMode", new Class[]{Integer.TYPE, Integer.TYPE, String.class, Integer.TYPE});
                    declaredMethod.setAccessible(true);
                    declaredMethod.invoke(systemService, new Object[]{Integer.valueOf(r4.getInt(null)), Integer.valueOf(r1), str, Integer.valueOf(r2.getInt(null))});
                    d.e("TMSLiteService--TMS", "tryOpenOps--success");
                    return 1;
                } catch (Throwable th) {
                    d.c("TMSLiteService--TMS", "tryOpenOps--failed!!--[" + th + "]");
                    return 2;
                }
            } catch (Throwable th2) {
                d.c("TMSLiteService--TMS", "getApplicationInfo-Throwable:[" + th2 + "]");
                return 2;
            }
        }
        d.e("TMSLiteService--TMS", "version[" + VERSION.SDK_INT + "] < SDKUtil.OS_4_4_0--return");
        return 0;
    }

    public static String eM() {
        ActivityManager activityManager = (ActivityManager) TMSDKContext.getApplicaionContext().getSystemService("activity");
        if (VERSION.SDK_INT >= 21) {
            try {
                List<RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
                if (runningAppProcesses == null) {
                    return null;
                }
                if (runningAppProcesses.size() <= 5) {
                    return null;
                }
                for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
                    if (runningAppProcessInfo.importance == 100) {
                        return runningAppProcessInfo.pkgList[0];
                    }
                }
                return null;
            } catch (Exception e) {
                return null;
            }
        }
        List runningTasks;
        try {
            runningTasks = activityManager.getRunningTasks(1);
        } catch (Throwable th) {
            runningTasks = null;
        }
        String flattenToString = (runningTasks != null && runningTasks.size() > 0) ? ((RunningTaskInfo) runningTasks.get(0)).topActivity.flattenToString() : null;
        return flattenToString;
    }
}

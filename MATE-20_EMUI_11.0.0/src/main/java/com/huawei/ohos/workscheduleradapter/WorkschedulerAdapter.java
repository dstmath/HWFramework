package com.huawei.ohos.workscheduleradapter;

import android.app.ActivityManager;
import android.app.mtm.IMultiTaskManagerService;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import com.huawei.android.pgmng.plug.PowerKit;
import java.util.ArrayList;
import java.util.List;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IPCSkeleton;

public final class WorkschedulerAdapter {
    public static final int HAP_ACTIVE = 0;
    public static final int HAP_FREQUENT = 2;
    public static final int HAP_NEVER = 4;
    public static final int HAP_RARE = 3;
    public static final int HAP_WORKING = 1;
    private static final Object INSTANCE_LOCK = new Object();
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218109696, TAG);
    private static final int RAM_BASE = 1024;
    private static final String TAG = "WorkschedulerAdapter";
    private static volatile IMultiTaskManagerService service;
    private static volatile UsageStatsManager usageStatsManager;

    private static int convertStandbyLevel(int i) {
        if (i == 50) {
            return 4;
        }
        if (i > 30) {
            return 3;
        }
        if (i > 20) {
            return 2;
        }
        return i > 10 ? 1 : 0;
    }

    private WorkschedulerAdapter() {
    }

    public static List<String> getAppForbidden() {
        try {
            if (getService() != null) {
                List<String> retrieveAppStartupPackages = service.retrieveAppStartupPackages((List) null, new int[]{0, -1, -1, 0}, new int[]{1, -1, -1, 1}, (int[]) null);
                retrieveAppStartupPackages.addAll(service.retrieveAppStartupPackages((List) null, new int[]{0, -1, -1, 0}, new int[]{1, -1, -1, 0}, (int[]) null));
                return retrieveAppStartupPackages;
            }
        } catch (RemoteException unused) {
            HiLog.error(LOG_LABEL, "get forbidden app failed", new Object[0]);
        }
        return new ArrayList();
    }

    public static List<String> getHibernateApps(Context context) {
        List<String> hibernateApps;
        if (context == null) {
            return new ArrayList();
        }
        try {
            PowerKit instance = PowerKit.getInstance();
            if (!(instance == null || (hibernateApps = instance.getHibernateApps(context)) == null)) {
                return hibernateApps;
            }
        } catch (RemoteException unused) {
            HiLog.error(LOG_LABEL, "get hibernate app failed", new Object[0]);
        }
        return new ArrayList();
    }

    public static long getFreeMem(Context context) {
        ActivityManager.MemoryInfo memoryInfo;
        if (context == null || (memoryInfo = getMemoryInfo(context)) == null) {
            return 0;
        }
        return (memoryInfo.availMem / 1024) / 1024;
    }

    public static int getInitUserId() {
        return UserHandle.getUserId(IPCSkeleton.getCallingUid());
    }

    public static int getStandbyLevel(Context context, String str) {
        if (context != null) {
            return convertStandbyLevel(getStandbyLevelInner(context, str));
        }
        HiLog.info(LOG_LABEL, "getStandbyLevel context null.", new Object[0]);
        return 0;
    }

    private static int getStandbyLevelInner(Context context, String str) {
        if (context == null) {
            HiLog.error(LOG_LABEL, "getStandbyLevel context null.", new Object[0]);
            return 0;
        }
        UsageStatsManager usageStatsManager2 = getUsageStatsManager(context);
        if (usageStatsManager2 == null) {
            return 0;
        }
        try {
            return usageStatsManager2.getAppStandbyBucket(str);
        } catch (IllegalArgumentException unused) {
            HiLog.error(LOG_LABEL, "getStandbyLevel occur exception.", new Object[0]);
            return 0;
        }
    }

    private static IMultiTaskManagerService getService() {
        IBinder service2;
        if (service == null) {
            synchronized (INSTANCE_LOCK) {
                if (service == null && (service2 = ServiceManager.getService("multi_task")) != null) {
                    service = IMultiTaskManagerService.Stub.asInterface(service2);
                }
            }
        }
        return service;
    }

    private static UsageStatsManager getUsageStatsManager(Context context) {
        if (usageStatsManager == null) {
            synchronized (INSTANCE_LOCK) {
                if (usageStatsManager == null) {
                    Object systemService = context.getSystemService("usagestats");
                    if (systemService instanceof UsageStatsManager) {
                        usageStatsManager = (UsageStatsManager) systemService;
                    }
                }
            }
        }
        return usageStatsManager;
    }

    private static ActivityManager.MemoryInfo getMemoryInfo(Context context) {
        Object systemService = context.getSystemService("activity");
        if (!(systemService instanceof ActivityManager)) {
            return null;
        }
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ((ActivityManager) systemService).getMemoryInfo(memoryInfo);
        return memoryInfo;
    }
}

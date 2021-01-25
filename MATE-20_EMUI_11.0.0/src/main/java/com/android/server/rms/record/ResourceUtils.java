package com.android.server.rms.record;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Environment;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.rms.utils.Utils;
import android.util.IMonitor;
import android.util.Log;
import android.util.Slog;
import com.android.server.HwBinderMonitor;
import com.android.server.wm.HwSplitBarConstants;
import java.io.File;
import java.util.Calendar;
import java.util.List;

public class ResourceUtils {
    public static final String CURRENT_MONTH = "rmsCurMonth";
    public static final int FAULT_CODE_ACTIVITY = 901003020;
    public static final int FAULT_CODE_ALARM = 901003014;
    public static final int FAULT_CODE_APPCRASH = 901003016;
    public static final int FAULT_CODE_APPOPS = 901003015;
    public static final int FAULT_CODE_BIGDATA = 901003010;
    public static final int FAULT_CODE_BROADCAST = 901003012;
    public static final int FAULT_CODE_CURSOR = 901003018;
    public static final int FAULT_CODE_IOABN_WR_UID = 901003019;
    public static final int FAULT_CODE_IPC_TIMEOUT = 901003021;
    public static final int FAULT_CODE_NOTIFICATION = 901003011;
    public static final int FAULT_CODE_PIDS = 901003017;
    public static final int FAULT_CODE_RECEIVER = 901003013;
    private static final int INT_SHIFT_FOUR_BYTE = 32;
    public static final int MONTHLY_BIGDATA_INFO_UPLOAD_COUNT_LIMIT = (Utils.DEBUG ? 20 : HwSplitBarConstants.DARK_MODE_DELAY);
    public static final String MONTHLY_BIGDATA_INFO_UPLOAD_LIMIT = "rmsUploadLimit";
    public static final String RMS_SHARED_PREFERENCES = "rms_shared_preferences";
    private static final String TAG = "RMS.ResourceUtils";
    private static String[] defaultPermissionLists = {"android.permission.CAMERA"};
    private static HwBinderMonitor sIBinderM = new HwBinderMonitor();
    private static String[] sResourceNameLists = {"NOTIFICATION", "BROADCAST", "RECEIVER", "ALARM", "APPOPS", "PIDS", "CURSOR", "APPSERVICE", "APP", "CPU", "IO", "SCHEDGROUP", "ANR", "DELAY", "FRAMELOST", "IOABN_WR_UID", "APPMNGMEMNORMALNATIVE", "APPMNGMEMNORMALPERS", "APPMNGWHITELIST", "CONTENTOBSERVER", "ACTIVITY", "ORDEREDBROADCAST", "AWAREBRPROXY", "IPCTIMEOUT", "TELEPHONY"};

    private ResourceUtils() {
    }

    public static long getResourceId(int callingUid, int pid, int resourceType) {
        long id;
        if (resourceType == 15) {
            id = (((long) pid) << 32) + ((long) callingUid);
        } else {
            id = (((long) resourceType) << 32) + ((long) callingUid);
        }
        if (Utils.DEBUG) {
            Log.d(TAG, "getResourceId resourceType/" + resourceType + " callingUid/" + callingUid + " id/" + id);
        }
        return id;
    }

    public static String getResourceName(int resourceType) {
        if (resourceType < 10 || resourceType > 34) {
            return "";
        }
        int i = resourceType - 10;
        String[] strArr = sResourceNameLists;
        if (i >= strArr.length) {
            Log.w(TAG, "fail to get resourceName");
            return "";
        }
        String resourceName = strArr[resourceType - 10];
        if (Utils.DEBUG || Utils.HWFLOW) {
            Log.w(TAG, " getResourceName: resourceName:" + resourceName);
        }
        return resourceName;
    }

    public static int getResourcesType(String resourceName) {
        if ("pids".equals(resourceName)) {
            return 15;
        }
        return 0;
    }

    public static String composeName(String pkg, int resourceType) {
        if (pkg == null) {
            return getResourceName(resourceType);
        }
        return pkg + "__" + getResourceName(resourceType);
    }

    public static int checkSysProcPermission(int pid, int uid) {
        if (pid == Process.myPid() || uid == 0 || uid == 1000) {
            return 0;
        }
        return -1;
    }

    public static int checkAppUidPermission(int uid) {
        if (uid == 0 || uid == 1000) {
            return 0;
        }
        if (UserHandle.isIsolated(uid)) {
            return -1;
        }
        for (String defaultPermission : defaultPermissionLists) {
            if (ActivityManager.checkUidPermission(defaultPermission, uid) == 0) {
                return 0;
            }
        }
        return -1;
    }

    public static int getProcessTypeId(int callingUid, String pkg, int processTpye) {
        int typeId = processTpye;
        if (processTpye == -1 && pkg != null) {
            try {
                int i = 0;
                ApplicationInfo appInfo = AppGlobals.getPackageManager().getApplicationInfo(pkg, 0, 0);
                if (appInfo == null) {
                    Log.w(TAG, "get appInfo is null from package: " + pkg);
                    return -1;
                }
                boolean isUpdatedRemovableApp = true;
                boolean isSystemApp = (appInfo.flags & 1) != 0;
                boolean isRemovablePreinstalledApk = (appInfo.hwFlags & 33554432) == 0;
                if ((appInfo.hwFlags & 67108864) != 0) {
                    isUpdatedRemovableApp = false;
                }
                if (isSystemApp && isRemovablePreinstalledApk && isUpdatedRemovableApp) {
                    i = 2;
                }
                typeId = i;
            } catch (RemoteException e) {
                Log.w(TAG, " get PacakgeManager failed!");
            }
        }
        if (Utils.DEBUG) {
            Log.d(TAG, "third app packageName: " + pkg + ", uid: " + callingUid + ", typeId: " + typeId);
        }
        return typeId;
    }

    public static long getAppTime(Context context, String pkg) {
        if (context == null || pkg == null) {
            return 0;
        }
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.add(2, -1);
        long startTime = calendar.getTimeInMillis();
        List<UsageStats> queryUsageStats = null;
        Object usageStatsService = context.getSystemService("usagestats");
        if (usageStatsService instanceof UsageStatsManager) {
            queryUsageStats = ((UsageStatsManager) usageStatsService).queryUsageStats(2, startTime, endTime);
        }
        if (queryUsageStats != null && !queryUsageStats.isEmpty()) {
            for (UsageStats usageStats : queryUsageStats) {
                if (pkg.equals(usageStats.getPackageName())) {
                    return usageStats.getTotalTimeInForeground();
                }
            }
        }
        return 0;
    }

    /* access modifiers changed from: private */
    public enum ResFaultInfo {
        NOTIFICATION(ResourceUtils.FAULT_CODE_NOTIFICATION, 0, 1, 2, 3),
        BROADCAST(ResourceUtils.FAULT_CODE_BROADCAST, 0, 1, 2, 3),
        RECEIVER(ResourceUtils.FAULT_CODE_RECEIVER, 0, 1, 2, 3),
        ALARM(ResourceUtils.FAULT_CODE_ALARM, 0, 1, 2, 3),
        APPOPS(ResourceUtils.FAULT_CODE_APPOPS, 0, 1, 2, 3),
        APP(ResourceUtils.FAULT_CODE_APPCRASH, 0, 1, 2, 3),
        CURSOR(ResourceUtils.FAULT_CODE_CURSOR, 0, 1, 2, 3),
        IOABN_WR_UID(ResourceUtils.FAULT_CODE_IOABN_WR_UID, 0, 1, 2, 3),
        ACTIVITY(ResourceUtils.FAULT_CODE_ACTIVITY, 0, 1, 2, 3),
        IPCTIMEOUT(ResourceUtils.FAULT_CODE_IPC_TIMEOUT, 0, 1, 2, 3);
        
        short resFaultKeyCurrentCount;
        short resFaultKeyOverloadNum;
        short resFaultKeyPackageName;
        short resFaultKeyResType;
        int resFaultNum;

        private ResFaultInfo(int resFaultNum2, short resFaultKeyPackageName2, short resFaultKeyResType2, short resFaultKeyOverloadNum2, short resFaultKeyCurrentCount2) {
            this.resFaultNum = resFaultNum2;
            this.resFaultKeyPackageName = resFaultKeyPackageName2;
            this.resFaultKeyResType = resFaultKeyResType2;
            this.resFaultKeyOverloadNum = resFaultKeyOverloadNum2;
            this.resFaultKeyCurrentCount = resFaultKeyCurrentCount2;
        }

        public static ResFaultInfo getResFaultInfo(int resType) {
            if (resType == 16) {
                return CURSOR;
            }
            if (resType == 18) {
                return APP;
            }
            if (resType == 25) {
                return IOABN_WR_UID;
            }
            if (resType == 30) {
                return ACTIVITY;
            }
            if (resType == 33) {
                return IPCTIMEOUT;
            }
            switch (resType) {
                case 10:
                    return NOTIFICATION;
                case 11:
                    return BROADCAST;
                case 12:
                    return RECEIVER;
                case 13:
                    return ALARM;
                case 14:
                    return APPOPS;
                default:
                    return null;
            }
        }
    }

    public static void uploadBigDataLogToIMonitor(int resType, String pkgName, int overloadNum, int currentNum) {
        if (pkgName == null && Utils.DEBUG) {
            Log.e(TAG, "uploadBigDataLogToIMonitor! failed due to Null pkgName");
        } else if (Utils.IS_DEBUG_VERSION || !Utils.DEBUG) {
            ResFaultInfo info = ResFaultInfo.getResFaultInfo(resType);
            if (info != null) {
                IMonitor.EventStream eventStream = IMonitor.openEventStream(info.resFaultNum);
                if (eventStream != null) {
                    eventStream.setParam(info.resFaultKeyPackageName, pkgName);
                    eventStream.setParam(info.resFaultKeyResType, resType);
                    eventStream.setParam(info.resFaultKeyOverloadNum, overloadNum);
                    eventStream.setParam(info.resFaultKeyCurrentCount, currentNum);
                    IMonitor.sendEvent(eventStream);
                    IMonitor.closeEventStream(eventStream);
                    if (Utils.DEBUG) {
                        Log.i(TAG, "uploadBigDataLogToIMonitor! pkgName:" + pkgName + " resType:" + resType + " overloadNum:" + overloadNum + " currentNum" + currentNum);
                    }
                } else if (Utils.DEBUG) {
                    Log.i(TAG, "uploadBigDataLogToIMonitor! failed due to null eventStream");
                }
            }
        } else {
            Log.i(TAG, "uploadBigDataLogToIMonitor! failed due to !IS_DEBUG_VERSION");
        }
    }

    public static SharedPreferences getPinnedSharedPrefs(Context context) {
        if (context == null) {
            return null;
        }
        try {
            return context.getSharedPreferences(new File(new File(Environment.getDataUserCePackageDirectory(StorageManager.UUID_PRIVATE_INTERNAL, context.getUserId(), context.getPackageName()), "shared_prefs"), "rms_shared_preferences.xml"), 0);
        } catch (IllegalStateException e) {
            Slog.e(TAG, "illegal state exception");
            return null;
        }
    }

    public static boolean killApplicationProcess(int pid) {
        if (pid <= 0) {
            return false;
        }
        Process.killProcess(pid);
        Log.e(TAG, "kill process pid:" + pid);
        return true;
    }

    public static int getLockOwnerPid(Object lock) {
        if (lock == null) {
            return -1;
        }
        int lockHolderTid = Thread.getLockOwnerThreadId(lock);
        int pid = sIBinderM.catchBadproc(lockHolderTid, 1);
        Log.i(TAG, "blocking in thread:" + lockHolderTid + " remote:" + pid);
        return pid;
    }

    public static int isNativeProcess(int pid) {
        if (pid <= 0) {
            Slog.w(TAG, "pid less than 0, pid is " + pid);
            return -1;
        }
        String pidAdj = getAdjForPid(pid);
        if (pidAdj == null) {
            Slog.w(TAG, "no such oom_score file and pid is" + pid);
            return -1;
        }
        try {
            if (Integer.parseInt(pidAdj.trim()) == 0) {
                return 1;
            }
            return 0;
        } catch (NumberFormatException e) {
            Slog.e(TAG, "isNativeProcess NumberFormatException: " + e.toString());
            return -1;
        }
    }

    private static String getAdjForPid(int pid) {
        String[] outStrings = new String[1];
        Process.readProcFile("/proc/" + pid + "/oom_score", new int[]{4128}, outStrings, null, null);
        return outStrings[0];
    }
}

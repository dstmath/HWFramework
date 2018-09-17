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
import android.util.IMonitor.EventStream;
import android.util.Log;
import android.util.Slog;
import com.android.server.HwBinderMonitor;
import huawei.com.android.server.policy.HwGlobalActionsData;
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
    public static final int MONTHLY_BIGDATA_INFO_UPLOAD_COUNT_LIMIT;
    public static final String MONTHLY_BIGDATA_INFO_UPLOAD_LIMIT = "rmsUploadLimit";
    public static final String RMS_SHARED_PREFERENCES = "rms_shared_preferences";
    private static final String TAG = "RMS.ResourceUtils";
    private static String[] defaultPermissionList = new String[]{"android.permission.CAMERA"};
    private static HwBinderMonitor mIBinderM = new HwBinderMonitor();
    private static String[] mResourceNameList = new String[]{"NOTIFICATION", "BROADCAST", "RECEIVER", "ALARM", "APPOPS", "PIDS", "CURSOR", "APPSERVICE", "APP", "CPU", "IO", "SCHEDGROUP", "ANR", "DELAY", "FRAMELOST", "IOABN_WR_UID", "APPMNGMEMNORMALNATIVE", "APPMNGMEMNORMALPERS", "APPMNGWHITELIST", "CONTENTOBSERVER", "ACTIVITY", "ORDEREDBROADCAST", "AWAREBRPROXY", "IPCTIMEOUT"};

    static {
        int i = 20;
        if (!Utils.DEBUG) {
            i = 5000;
        }
        MONTHLY_BIGDATA_INFO_UPLOAD_COUNT_LIMIT = i;
    }

    public static long getResourceId(int callingUid, int pid, int resourceType) {
        long id = resourceType == 15 ? (((long) pid) << 32) + ((long) callingUid) : (((long) resourceType) << 32) + ((long) callingUid);
        if (Utils.DEBUG) {
            Log.d(TAG, "getResourceId  resourceType/" + resourceType + " callingUid/" + callingUid + " id/" + id);
        }
        return id;
    }

    public static String getResourceName(int resourceType) {
        if (resourceType < 10 || resourceType > 33) {
            return null;
        }
        if (resourceType - 10 >= mResourceNameList.length) {
            Log.w(TAG, "fail to get resourceName");
            return null;
        }
        String resourceName = mResourceNameList[resourceType - 10];
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
        if (pkg != null) {
            return pkg + "__" + getResourceName(resourceType);
        }
        return getResourceName(resourceType);
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
        try {
            for (String s : defaultPermissionList) {
                if (ActivityManager.checkUidPermission(s, uid) == 0) {
                    return 0;
                }
            }
        } catch (Exception e) {
            Slog.e(TAG, "PackageManager exception", e);
        }
        return -1;
    }

    public static int getProcessTypeId(int callingUid, String pkg, int processTpye) {
        int typeID = processTpye;
        if (-1 == processTpye && pkg != null) {
            try {
                ApplicationInfo appInfo = AppGlobals.getPackageManager().getApplicationInfo(pkg, 0, 0);
                if (appInfo == null) {
                    Log.w(TAG, "get appInfo is null from package: " + pkg);
                    return -1;
                }
                typeID = ((appInfo.flags & 1) != 0 && (appInfo.hwFlags & HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM) == 0 && (appInfo.hwFlags & 67108864) == 0) ? 2 : 0;
            } catch (RemoteException e) {
                Log.w(TAG, " get PacakgeManager failed!");
            }
        }
        if (Utils.DEBUG) {
            Log.d(TAG, "third app packageName: " + pkg + ", uid: " + callingUid + ", typeID: " + typeID);
        }
        return typeID;
    }

    public static long getAppTime(Context context, String pkg) {
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.add(2, -1);
        List<UsageStats> queryUsageStats = ((UsageStatsManager) context.getSystemService("usagestats")).queryUsageStats(2, calendar.getTimeInMillis(), endTime);
        if (!(queryUsageStats == null || (queryUsageStats.isEmpty() ^ 1) == 0)) {
            for (UsageStats usageStats : queryUsageStats) {
                if (pkg != null && pkg.equals(usageStats.getPackageName())) {
                    return usageStats.getTotalTimeInForeground();
                }
            }
        }
        return 0;
    }

    public static void uploadBigDataLogToIMonitor(int resType, String pkgName, int overloadNum, int currentNum) {
        if (pkgName == null) {
            if (Utils.DEBUG) {
                Log.e(TAG, "uploadBigDataLogToIMonitor! failed due to Null pkgName");
            }
        } else if (Utils.IS_DEBUG_VERSION) {
            int resFaultNum;
            short resFaultKey_PName;
            short resFaultKey_ResType;
            short resFaultKey_OverloadNum;
            short resFaultKey_CurrentCount;
            switch (resType) {
                case 10:
                    resFaultNum = FAULT_CODE_NOTIFICATION;
                    resFaultKey_PName = (short) 0;
                    resFaultKey_ResType = (short) 1;
                    resFaultKey_OverloadNum = (short) 2;
                    resFaultKey_CurrentCount = (short) 3;
                    break;
                case 11:
                    resFaultNum = FAULT_CODE_BROADCAST;
                    resFaultKey_PName = (short) 0;
                    resFaultKey_ResType = (short) 1;
                    resFaultKey_OverloadNum = (short) 2;
                    resFaultKey_CurrentCount = (short) 3;
                    break;
                case 12:
                    resFaultNum = FAULT_CODE_RECEIVER;
                    resFaultKey_PName = (short) 0;
                    resFaultKey_ResType = (short) 1;
                    resFaultKey_OverloadNum = (short) 2;
                    resFaultKey_CurrentCount = (short) 3;
                    break;
                case 13:
                    resFaultNum = FAULT_CODE_ALARM;
                    resFaultKey_PName = (short) 0;
                    resFaultKey_ResType = (short) 1;
                    resFaultKey_OverloadNum = (short) 2;
                    resFaultKey_CurrentCount = (short) 3;
                    break;
                case 14:
                    resFaultNum = FAULT_CODE_APPOPS;
                    resFaultKey_PName = (short) 0;
                    resFaultKey_ResType = (short) 1;
                    resFaultKey_OverloadNum = (short) 2;
                    resFaultKey_CurrentCount = (short) 3;
                    break;
                case 16:
                    resFaultNum = FAULT_CODE_CURSOR;
                    resFaultKey_PName = (short) 0;
                    resFaultKey_ResType = (short) 1;
                    resFaultKey_OverloadNum = (short) 2;
                    resFaultKey_CurrentCount = (short) 3;
                    break;
                case 18:
                    resFaultNum = FAULT_CODE_APPCRASH;
                    resFaultKey_PName = (short) 0;
                    resFaultKey_ResType = (short) 1;
                    resFaultKey_OverloadNum = (short) 2;
                    resFaultKey_CurrentCount = (short) 3;
                    break;
                case 25:
                    resFaultNum = FAULT_CODE_IOABN_WR_UID;
                    resFaultKey_PName = (short) 0;
                    resFaultKey_ResType = (short) 1;
                    resFaultKey_OverloadNum = (short) 2;
                    resFaultKey_CurrentCount = (short) 3;
                    break;
                case 30:
                    resFaultNum = FAULT_CODE_ACTIVITY;
                    resFaultKey_PName = (short) 0;
                    resFaultKey_ResType = (short) 1;
                    resFaultKey_OverloadNum = (short) 2;
                    resFaultKey_CurrentCount = (short) 3;
                    break;
                case 33:
                    resFaultNum = FAULT_CODE_IPC_TIMEOUT;
                    resFaultKey_PName = (short) 0;
                    resFaultKey_ResType = (short) 1;
                    resFaultKey_OverloadNum = (short) 2;
                    resFaultKey_CurrentCount = (short) 3;
                    break;
                default:
                    if (Utils.DEBUG) {
                        Log.w(TAG, "uploadBigDataLogToIMonitor! failed due to No such resource Type");
                    }
                    return;
            }
            EventStream eStream = IMonitor.openEventStream(resFaultNum);
            if (eStream != null) {
                eStream.setParam(resFaultKey_PName, pkgName);
                eStream.setParam(resFaultKey_ResType, resType);
                eStream.setParam(resFaultKey_OverloadNum, overloadNum);
                eStream.setParam(resFaultKey_CurrentCount, currentNum);
                IMonitor.sendEvent(eStream);
                IMonitor.closeEventStream(eStream);
                if (Utils.DEBUG) {
                    Log.i(TAG, "uploadBigDataLogToIMonitor! pkgName:" + pkgName + " resType:" + resType + " overloadNum:" + overloadNum + " currentNum" + currentNum);
                }
            } else if (Utils.DEBUG) {
                Log.i(TAG, "uploadBigDataLogToIMonitor! failed due to null eStream");
            }
        } else {
            if (Utils.DEBUG) {
                Log.i(TAG, "uploadBigDataLogToIMonitor! failed due to !IS_DEBUG_VERSION");
            }
        }
    }

    public static SharedPreferences getPinnedSharedPrefs(Context context) {
        if (context == null) {
            return null;
        }
        SharedPreferences sp = null;
        try {
            sp = context.getSharedPreferences(new File(new File(Environment.getDataUserCePackageDirectory(StorageManager.UUID_PRIVATE_INTERNAL, context.getUserId(), context.getPackageName()), "shared_prefs"), "rms_shared_preferences.xml"), 0);
        } catch (Exception e) {
            Log.w(TAG, "getPinnedSharedPrefs failed");
        }
        return sp;
    }

    public static boolean killApplicationProcess(int pid) {
        if (pid <= 0 || isNativeProcess(pid) != 0) {
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
        return mIBinderM.catchBadproc(Thread.getLockOwnerThreadId(lock), 1);
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

    private static final String getAdjForPid(int pid) {
        String[] outStrings = new String[1];
        Process.readProcFile("/proc/" + pid + "/oom_score", new int[]{4128}, outStrings, null, null);
        return outStrings[0];
    }
}

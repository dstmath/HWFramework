package com.android.server.rms.record;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import com.android.server.rms.utils.Utils;
import com.android.server.security.trustcircle.IOTController;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.util.Calendar;
import java.util.List;

public class ResourceUtils {
    private static final String TAG = "RMS.ResourceUtils";
    private static String[] defaultPermissionList;
    private static String[] mResourceNameList;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.record.ResourceUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.record.ResourceUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.record.ResourceUtils.<clinit>():void");
    }

    public static long getResourceId(int callingUid, int pid, int resourceType) {
        long id = resourceType == 16 ? (((long) pid) << 32) + ((long) callingUid) : (((long) resourceType) << 32) + ((long) callingUid);
        if (Utils.DEBUG) {
            Log.d(TAG, "getResourceId  resourceType/" + resourceType + " callingUid/" + callingUid + " id/" + id);
        }
        return id;
    }

    public static String getResourceName(int resourceType) {
        if (resourceType < 10 || resourceType > 38) {
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
            return 16;
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
        if (pid == Process.myPid() || uid == 0 || uid == IOTController.TYPE_MASTER) {
            return 0;
        }
        return -1;
    }

    public static int checkAppUidPermission(int uid) {
        if (uid == 0 || uid == IOTController.TYPE_MASTER) {
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
        if (!(queryUsageStats == null || queryUsageStats.isEmpty())) {
            for (UsageStats usageStats : queryUsageStats) {
                if (pkg != null && pkg.equals(usageStats.getPackageName())) {
                    return usageStats.getTotalTimeInForeground();
                }
            }
        }
        return 0;
    }
}

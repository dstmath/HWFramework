package com.huawei.android.app;

import android.app.ActivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Singleton;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.huawei.android.app.IHwActivityManager;
import com.huawei.android.fsm.HwFoldScreenManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HwActivityManager {
    private static final int COMMON_ERR = -1;
    private static final Singleton<IHwActivityManager> IActivityManagerSingleton = new Singleton<IHwActivityManager>() {
        /* class com.huawei.android.app.HwActivityManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IHwActivityManager create() {
            try {
                return IHwActivityManager.Stub.asInterface(ActivityManager.getService().getHwInnerService());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    };
    public static final boolean IS_PHONE = (!"tablet".equals(SystemProperties.get("ro.build.characteristics", "")) && !HwFoldScreenManager.isFoldable());
    private static final String TAG = "HwActivityManager";

    public static IHwActivityManager getService() {
        return IActivityManagerSingleton.get();
    }

    public static void registerDAMonitorCallback(IHwDAMonitorCallback callback) {
        if (getService() != null) {
            long token = Binder.clearCallingIdentity();
            try {
                getService().registerDAMonitorCallback(callback);
            } catch (RemoteException e) {
                Log.e(TAG, "registerDAMonitorCallback failed: catch RemoteException!");
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
            Binder.restoreCallingIdentity(token);
        }
    }

    public static void setCpusetSwitch(boolean enable, int subSwitch) {
        IHwActivityManager am = getService();
        if (am != null) {
            long token = Binder.clearCallingIdentity();
            try {
                am.setCpusetSwitch(enable, subSwitch);
            } catch (RemoteException e) {
                Log.e(TAG, "setCpusetSwitch failed: catch RemoteException!");
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
            Binder.restoreCallingIdentity(token);
        }
    }

    public static boolean cleanPackageRes(List<String> packageList, Map alarmTags, int targetUid, boolean cleanAlarm, boolean isNative, boolean hasPerceptAlarm) {
        if (getService() == null) {
            return false;
        }
        long token = Binder.clearCallingIdentity();
        try {
            return getService().cleanPackageRes(packageList, alarmTags, targetUid, cleanAlarm, isNative, hasPerceptAlarm);
        } catch (RemoteException e) {
            Log.e(TAG, "cleanPackageRes failed: catch RemoteException!");
            return false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public static void reportScreenRecord(int uid, int pid, int status) {
        if (getService() != null) {
            long token = Binder.clearCallingIdentity();
            try {
                getService().reportScreenRecord(uid, pid, status);
            } catch (RemoteException e) {
                Log.e(TAG, "reportScreenRecord failed: catch RemoteException!");
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
            Binder.restoreCallingIdentity(token);
        }
    }

    public static boolean handleANRFilterFIFO(int uid, int cmd) {
        try {
            return getService().handleANRFilterFIFO(uid, cmd);
        } catch (RemoteException e) {
            Log.e(TAG, "handleANRFilterFIFO failed", e);
            return false;
        }
    }

    public static void handleShowAppEyeAnrUi(int pid, int uid, String processName, String packageName) {
        try {
            getService().handleShowAppEyeAnrUi(pid, uid, processName, packageName);
        } catch (RemoteException e) {
            Log.e(TAG, "handleShowAppEyeAnrUi failed", e);
        }
    }

    public static void setProcessRecForPid(int pid) {
        try {
            IHwActivityManager am = getService();
            if (am != null) {
                am.setProcessRecForPid(pid);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "setProcessRecForPid failed", e);
        }
    }

    public static void requestProcessGroupChange(int pid, int oldGroup, int newGroup, int isLimit) {
        try {
            IHwActivityManager am = getService();
            if (am != null) {
                am.requestProcessGroupChange(pid, oldGroup, newGroup, isLimit);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "requestProcessGroupChange failed", e);
        }
    }

    public static int preloadAppForLauncher(String packageName, int userId, int preloadType) {
        if (getService() == null) {
            return -1;
        }
        try {
            return getService().preloadAppForLauncher(packageName, userId, preloadType);
        } catch (RemoteException e) {
            Log.e(TAG, "preloadAppForLauncher failed: catch RemoteException!");
            return -1;
        }
    }

    /* JADX INFO: finally extract failed */
    public static List<String> getPidWithUiFromUid(int uid) {
        if (getService() == null) {
            return null;
        }
        long token = Binder.clearCallingIdentity();
        try {
            List<String> pidWithUiFromUid = getService().getPidWithUiFromUid(uid);
            Binder.restoreCallingIdentity(token);
            return pidWithUiFromUid;
        } catch (RemoteException e) {
            Log.e(TAG, "getPidWithUiFromUid failed: catch RemoteException!");
            Binder.restoreCallingIdentity(token);
            return new ArrayList();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    public static int preloadApplication(String packageName, int userId) {
        if (getService() == null) {
            return -1;
        }
        try {
            return getService().preloadApplication(packageName, userId);
        } catch (RemoteException e) {
            Log.e(TAG, "preloadApplication failed: catch RemoteException!");
            return -1;
        }
    }

    public static boolean killProcessRecordFromIAware(ProcessInfo procInfo, boolean restartservice, boolean isAsynchronous, String reason, boolean checkAdj) {
        if (getService() == null) {
            return false;
        }
        long token = Binder.clearCallingIdentity();
        try {
            return getService().killProcessRecordFromIAware(procInfo, restartservice, isAsynchronous, reason, checkAdj);
        } catch (RemoteException e) {
            Log.e(TAG, "killProcessRecordFromIAware failed: catch RemoteException!");
            return false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public static boolean killProcessRecordFromIAwareNative(ProcessInfo procInfo, boolean restartservice, boolean isAsynchronous, String reason) {
        if (getService() == null) {
            return false;
        }
        long token = Binder.clearCallingIdentity();
        try {
            return getService().killProcessRecordFromIAwareNative(procInfo, restartservice, isAsynchronous, reason);
        } catch (RemoteException e) {
            Log.e(TAG, "killProcessRecordFromIAwareNative failed: catch RemoteException!");
            return false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public static boolean killProcessRecordFromMTM(ProcessInfo procInfo, boolean restartservice, String reason) {
        if (getService() == null) {
            return false;
        }
        long token = Binder.clearCallingIdentity();
        try {
            return getService().killProcessRecordFromMTM(procInfo, restartservice, reason);
        } catch (RemoteException e) {
            Log.e(TAG, "killProcessRecordFromMTM failed: catch RemoteException!");
            return false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* JADX INFO: finally extract failed */
    public static boolean isProcessExistLocked(String processName, int uid) {
        if (getService() == null) {
            return false;
        }
        long token = Binder.clearCallingIdentity();
        try {
            boolean isProcessExistLocked = getService().isProcessExistLocked(processName, uid);
            Binder.restoreCallingIdentity(token);
            return isProcessExistLocked;
        } catch (RemoteException e) {
            Log.e(TAG, "isProcessExistLocked failed: catch RemoteException!");
            Binder.restoreCallingIdentity(token);
            return true;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    public static void removePackageAlarm(String pkg, List<String> tags, int targetUid) {
        if (getService() != null) {
            long token = Binder.clearCallingIdentity();
            try {
                getService().removePackageAlarm(pkg, tags, targetUid);
            } catch (RemoteException e) {
                Log.e(TAG, "removePackageAlarm failed: catch RemoteException!");
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
            Binder.restoreCallingIdentity(token);
        }
    }

    public static boolean getProcessRecordFromMTM(ProcessInfo procInfo) {
        if (getService() == null) {
            return false;
        }
        long token = Binder.clearCallingIdentity();
        try {
            return getService().getProcessRecordFromMTM(procInfo);
        } catch (RemoteException e) {
            Log.e(TAG, "getProcessRecordFromMTM failed: catch RemoteException!");
            return false;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public static void setAndRestoreMaxAdjIfNeed(List<String> adjCustPkg) {
        if (getService() != null) {
            long token = Binder.clearCallingIdentity();
            try {
                getService().setAndRestoreMaxAdjIfNeed(adjCustPkg);
            } catch (RemoteException e) {
                Log.e(TAG, "setAndRestoreMaxAdjIfNeed failed: catch RemoteException!");
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
            Binder.restoreCallingIdentity(token);
        }
    }

    public static void reportProcessDied(int pid) {
        if (getService() != null) {
            long token = Binder.clearCallingIdentity();
            try {
                getService().reportProcessDied(pid);
            } catch (RemoteException e) {
                Log.e(TAG, "reportProcessDied failed: catch RemoteException!");
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
            Binder.restoreCallingIdentity(token);
        }
    }

    public static void reportAssocDisable() {
        if (getService() != null) {
            long token = Binder.clearCallingIdentity();
            try {
                getService().reportAssocDisable();
            } catch (RemoteException e) {
                Log.e(TAG, "reportAssocDisable failed: catch RemoteException!");
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
            Binder.restoreCallingIdentity(token);
        }
    }

    public static boolean canPickColor(String pkgName) {
        try {
            return getService().canPickColor(pkgName);
        } catch (RemoteException e) {
            Log.e(TAG, "canPickColor failed", e);
            return false;
        }
    }

    public static boolean cleanProcessResourceFast(String processName, int pid, IBinder thread, boolean restartService, boolean isNative) {
        try {
            return getService().cleanProcessResourceFast(processName, pid, thread, restartService, isNative);
        } catch (RemoteException e) {
            Log.e(TAG, "cleanProcessResourceFast failed: catch RemoteException!");
            return false;
        }
    }

    public static boolean killProcessRecordFast(String processName, int pid, int uid, boolean restartservice, boolean isAsynchronous, String reason, boolean needCheckAdj) {
        try {
            return getService().killProcessRecordFast(processName, pid, uid, restartservice, isAsynchronous, reason, needCheckAdj);
        } catch (RemoteException e) {
            Log.e(TAG, "killProcessRecordFast failed: catch RemoteException!");
            return false;
        }
    }

    public static boolean killNativeProcessRecordFast(String processName, int pid, int uid, boolean restartservice, boolean isAsynchronous, String reason) {
        try {
            return getService().killNativeProcessRecordFast(processName, pid, uid, restartservice, isAsynchronous, reason);
        } catch (RemoteException e) {
            Log.e(TAG, "killNativeProcessRecordFast failed: catch RemoteException!");
            return false;
        }
    }

    public static void forceStopPackages(List<String> packagesNames, int userId) {
        try {
            getService().forceStopPackages(packagesNames, userId);
        } catch (RemoteException e) {
            Log.e(TAG, "forceStopPackages failed", e);
        }
    }
}

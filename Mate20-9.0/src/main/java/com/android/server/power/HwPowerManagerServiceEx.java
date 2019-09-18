package com.android.server.power;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.WorkSource;
import android.pc.IHwPCManager;
import android.util.HwLog;
import android.util.HwPCUtils;
import com.android.server.LocalServices;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;

public final class HwPowerManagerServiceEx implements IHwPowerManagerServiceEx {
    static final String TAG = "HwPowerManagerServiceEx";
    final Context mContext;
    private HwFoldScreenManagerInternal mFoldScreenManagerService;
    IHwPowerManagerInner mIPowerInner = null;

    public HwPowerManagerServiceEx(IHwPowerManagerInner pms, Context context) {
        this.mIPowerInner = pms;
        this.mContext = context;
    }

    public boolean isAwarePreventScreenOn(String pkgName, String tag) {
        if (pkgName == null || tag == null || this.mIPowerInner == null || this.mIPowerInner.getPowerMonitor() == null) {
            return false;
        }
        return this.mIPowerInner.getPowerMonitor().isAwarePreventScreenOn(pkgName, tag);
    }

    public void notifyWakeLockAcquiredToDubai(int flags, int lock, String tag, WorkSource workSource, int uid, int pid, String name) {
        int i = lock;
        WorkSource workSource2 = workSource;
        if ((flags & 65535) == 1) {
            String lockTag = (tag == null || tag.length() <= 0) ? "NULL" : tag;
            if (workSource2 == null) {
                HwLog.dubaie("DUBAI_TAG_PARTIAL_WAKELOCK_ACQUIRE", "lock=" + i + " tag=" + lockTag + " count=1 name=" + name + " uid=" + uid + " pid=" + pid);
            } else {
                int i2 = uid;
                int i3 = pid;
                String str = name;
                int length = workSource.size();
                StringBuilder value = new StringBuilder("lock=" + i + " tag=" + lockTag + " count=" + length);
                for (int i4 = 0; i4 < length; i4++) {
                    int wsUid = workSource2.get(i4);
                    String wsName = workSource2.getName(i4);
                    if (wsName == null) {
                        wsName = "NULL";
                    } else if (wsName.indexOf(58) > 0) {
                        wsName = wsName.substring(0, wsName.indexOf(58));
                    }
                    value.append(" name=");
                    value.append(wsName);
                    value.append(" uid=");
                    value.append(wsUid);
                    value.append(" pid=-1");
                }
                HwLog.dubaie("DUBAI_TAG_PARTIAL_WAKELOCK_ACQUIRE", value.toString());
            }
        }
    }

    public void notifyWakeLockReleasedToDubai(int flags, int lock) {
        if ((65535 & flags) == 1) {
            HwLog.dubaie("DUBAI_TAG_PARTIAL_WAKELOCK_RELEASE", "lock=" + lock);
        }
    }

    public void requestNoUserActivityNotification(int timeout) {
        if (this.mIPowerInner != null) {
            this.mIPowerInner.sendNoUserActivityNotification(timeout * 1000);
        }
    }

    public int addWakeLockFlagsForPC(String pkgName, int uid, int flags) {
        if (pkgName == null || HwPCUtils.enabledInPad()) {
            return flags;
        }
        boolean isRunningOnPCMode = false;
        if (HwPCUtils.isPcCastModeInServer()) {
            try {
                IHwPCManager pcManager = HwPCUtils.getHwPCManager();
                if (pcManager != null) {
                    isRunningOnPCMode = pcManager.isPackageRunningOnPCMode(pkgName, uid);
                }
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "fail to get is package running on pc mode");
            }
        }
        if ((isRunningOnPCMode || HwPCUtils.getPhoneDisplayID() != -1) && (65535 & flags) == 10) {
            flags = (flags & -11) | 6;
            HwPCUtils.log(TAG, "Replace SCREEN_BRIGHT_WAKE_LOCK flag with SCREEN_DIM_WAKE_LOCK.");
        }
        return flags;
    }

    public void prepareWakeupEx(int wakeuptye, int uid, String opPackageName, String reason) {
        if (this.mFoldScreenManagerService == null) {
            this.mFoldScreenManagerService = (HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class);
        }
        Bundle extra = new Bundle();
        extra.putInt("uid", uid);
        extra.putString("opPackageName", opPackageName);
        extra.putString("reason", reason);
        this.mFoldScreenManagerService.prepareWakeup(wakeuptye, extra);
    }

    public void startWakeupEx(int wakeuptye, int uid, String opPackageName, String reason) {
        if (this.mFoldScreenManagerService == null) {
            this.mFoldScreenManagerService = (HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class);
        }
        Bundle extra = new Bundle();
        extra.putInt("uid", uid);
        extra.putString("opPackageName", opPackageName);
        extra.putString("reason", reason);
        this.mFoldScreenManagerService.startWakeup(wakeuptye, extra);
    }

    public void notifySleepEx() {
        if (this.mFoldScreenManagerService == null) {
            this.mFoldScreenManagerService = (HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class);
        }
        this.mFoldScreenManagerService.notifySleep();
    }
}

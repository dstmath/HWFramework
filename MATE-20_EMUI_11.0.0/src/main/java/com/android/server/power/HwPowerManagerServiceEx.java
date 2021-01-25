package com.android.server.power;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.WorkSource;
import android.pc.IHwPCManager;
import android.util.HwPCUtils;
import android.util.Log;
import com.android.server.tv.HwTvPowerManager;
import com.android.server.tv.HwTvPowerManagerPolicy;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import com.huawei.android.hardware.display.DisplayManagerInternalEx;
import com.huawei.android.os.IScreenStateCallback;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.WorkSourceEx;
import com.huawei.android.server.LocalServicesExt;
import com.huawei.android.server.SystemServiceEx;
import com.huawei.android.util.HwLogEx;
import com.huawei.android.util.SlogEx;
import com.huawei.util.HwPartCommInterfaceWraper;
import com.huawei.util.LogEx;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class HwPowerManagerServiceEx extends DefaultHwPowerManagerServiceEx {
    private static final int FAILED_RETURN_VALUE = -1;
    private static final boolean IS_DEBUG = (LogEx.getLogHWInfo() || (LogEx.getHWModuleLog() && Log.isLoggable(TAG, 4)));
    private static final boolean IS_DEBUG_ALL;
    private static final boolean IS_TV = "tv".equalsIgnoreCase(SystemPropertiesEx.get("ro.build.characteristics"));
    private static final Set<String> LOG_DROP_SET = new HashSet<String>(6) {
        /* class com.android.server.power.HwPowerManagerServiceEx.AnonymousClass1 */

        {
            add("RILJ1001");
            add("LocationManagerService1000");
            add("*alarm*1000");
            add("*dexopt*1000");
            add("bluetooth_timer1002");
            add("GnssLocationProvider1000");
        }
    };
    private static final int MILLISECOND = 1000;
    private static final int SUCCESSED_RETURN_VALUE = 0;
    private static final String TAG = "HwPowerManagerServiceEx";
    private static final String TAG_POWER_MS = "PowerMS";
    private static final int USER_TYPE_DOMESTIC_BETA = 3;
    private static final int USER_TYPE_INVALID = -1;
    private static final int USER_TYPE_OVERSEAS_BETA = 5;
    private static final int WAKELOCK_TYPE_BRIGHT = 2;
    private static final int WAKELOCK_TYPE_DIM = 3;
    private static final int WAKELOCK_TYPE_FULL = 1;
    private static final int WAKELOCK_TYPE_INVALID = -1;
    private static final int WAKELOCK_TYPE_PARTIAL = 0;
    private ClientCallback mClientCallback;
    final Context mContext;
    private DisplayManagerInternalEx mDisplayManagerInternal;
    private HwFoldScreenManagerInternal mFoldScreenManagerService;
    IHwPowerManagerInnerEx mIhwPowerInner = null;
    private long mLastOneSecActivityTime = 0;
    private final Object mLock = new Object();
    private HwTvPowerManagerPolicy mTvPolicy;

    static {
        boolean z = false;
        if (LogEx.getHwLog() || !SystemPropertiesEx.getBoolean("ro.config.pms_log_filter_enable", true)) {
            z = true;
        }
        IS_DEBUG_ALL = z;
    }

    public HwPowerManagerServiceEx(IHwPowerManagerInnerEx pms, Context context) {
        super(pms, context);
        this.mIhwPowerInner = pms;
        this.mContext = context;
        if (IS_TV) {
            this.mTvPolicy = new HwTvPowerManager(context);
            LocalServicesExt.addService(HwTvPowerManagerPolicy.class, this.mTvPolicy);
        }
    }

    public void systemReady() {
        HwTvPowerManagerPolicy hwTvPowerManagerPolicy = this.mTvPolicy;
        if (hwTvPowerManagerPolicy != null) {
            hwTvPowerManagerPolicy.systemReady();
        }
    }

    public void onBootPhase(int phase) {
        HwTvPowerManagerPolicy hwTvPowerManagerPolicy;
        if (phase == SystemServiceEx.PHASE_BOOT_COMPLETED && (hwTvPowerManagerPolicy = this.mTvPolicy) != null) {
            hwTvPowerManagerPolicy.bootCompleted();
        }
    }

    public void onEnterGoToSleep(long eventTime, int reason, int flags, int uid, boolean isAsleep) {
        HwTvPowerManagerPolicy hwTvPowerManagerPolicy = this.mTvPolicy;
        if (hwTvPowerManagerPolicy != null) {
            hwTvPowerManagerPolicy.onEnterGoToSleep(flags, isAsleep);
        }
    }

    public boolean interceptBeforeGoToSleep(long eventTime, int reason, int flags, int uid) {
        HwTvPowerManagerPolicy hwTvPowerManagerPolicy = this.mTvPolicy;
        if (hwTvPowerManagerPolicy == null) {
            return false;
        }
        hwTvPowerManagerPolicy.onEarlyGoToSleep(flags);
        return false;
    }

    public boolean isWakeLockDisabled(String packageName, int pid, int uid, WorkSource workSource) {
        HwTvPowerManagerPolicy hwTvPowerManagerPolicy = this.mTvPolicy;
        if (hwTvPowerManagerPolicy == null || !hwTvPowerManagerPolicy.isWakeLockDisabled(packageName, pid, uid)) {
            return false;
        }
        return true;
    }

    public boolean isWakelockCauseWakeUpDisabled() {
        HwTvPowerManagerPolicy hwTvPowerManagerPolicy = this.mTvPolicy;
        if (hwTvPowerManagerPolicy == null || !hwTvPowerManagerPolicy.isWakelockCauseWakeUpDisabled()) {
            return false;
        }
        return true;
    }

    public boolean isAwarePreventScreenOn(String pkgName, String tag) {
        IHwPowerManagerInnerEx iHwPowerManagerInnerEx;
        if (pkgName == null || tag == null || (iHwPowerManagerInnerEx = this.mIhwPowerInner) == null) {
            return false;
        }
        return iHwPowerManagerInnerEx.isAwarePreventScreenOn(pkgName, tag);
    }

    private int getDubaiWakelockType(int flags) {
        int userType = SystemPropertiesEx.getInt("ro.logsystem.usertype", -1);
        boolean beta = userType == 3 || userType == 5;
        int i = 65535 & flags;
        if (i == 1) {
            return 0;
        }
        if (i != 6) {
            if (i != 10) {
                if (i == 26 && beta) {
                    return 1;
                }
                return -1;
            } else if (beta) {
                return 2;
            } else {
                return -1;
            }
        } else if (beta) {
            return 3;
        } else {
            return -1;
        }
    }

    private void sendWakelockStartToDubai(String msg, int type, int lock, String tag, WorkSource workSource, int uid, int pid, String processName) {
        String lockTag = (tag == null || tag.length() <= 0) ? "NULL" : tag;
        if (workSource == null || WorkSourceEx.size(workSource) == 0) {
            HwLogEx.dubaie(msg, "type=" + type + " lock=" + lock + " tag=" + lockTag + " count=1 name=" + processName + " uid=" + uid + " pid=" + pid);
            return;
        }
        int length = WorkSourceEx.size(workSource);
        StringBuilder value = new StringBuilder("type=" + type + " lock=" + lock + " tag=" + lockTag + " count=" + length);
        for (int i = 0; i < length; i++) {
            String wsName = WorkSourceEx.getName(workSource, i);
            if (wsName == null) {
                wsName = "NULL";
            } else if (wsName.indexOf(58) > 0) {
                wsName = wsName.substring(0, wsName.indexOf(58));
            }
            int wsUid = WorkSourceEx.get(workSource, i);
            value.append(" name=" + wsName + " uid=" + wsUid + " pid=-1");
        }
        HwLogEx.dubaie(msg, value.toString());
    }

    private void sendWakelockStopToDubai(int lock) {
        HwLogEx.dubaie("DUBAI_TAG_WAKELOCK_RELEASE", "lock=" + lock);
    }

    public void notifyWakeLockAcquiredToDubai(int flags, int lock, String tag, WorkSource workSource, int uid, int pid, String processName) {
        int type = getDubaiWakelockType(flags);
        if (type >= 0) {
            sendWakelockStartToDubai("DUBAI_TAG_WAKELOCK_ACQUIRE", type, lock, tag, workSource, uid, pid, processName);
        }
    }

    public void notifyWakeLockChangingToDubai(int oldFlags, int newFlags, int lock, String tag, WorkSource workSource, int uid, int pid, String processName) {
        int oldType = getDubaiWakelockType(oldFlags);
        int newType = getDubaiWakelockType(newFlags);
        if (oldType < 0 && newType < 0) {
            return;
        }
        if (oldType < 0 && newType >= 0) {
            sendWakelockStartToDubai("DUBAI_TAG_WAKELOCK_ACQUIRE", newType, lock, tag, workSource, uid, pid, processName);
        } else if (oldType < 0 || newType >= 0) {
            sendWakelockStartToDubai("DUBAI_TAG_WAKELOCK_CHANGING", newType, lock, tag, workSource, uid, pid, processName);
        } else {
            sendWakelockStopToDubai(lock);
        }
    }

    public void notifyWakeLockReleasedToDubai(int flags, int lock) {
        sendWakelockStopToDubai(lock);
    }

    public void requestNoUserActivityNotification(int timeout) {
        IHwPowerManagerInnerEx iHwPowerManagerInnerEx = this.mIhwPowerInner;
        if (iHwPowerManagerInnerEx != null) {
            iHwPowerManagerInnerEx.sendNoUserActivityNotification(timeout * 1000);
        }
    }

    private boolean isScreenOrProximityLock(int flags) {
        int i = 65535 & flags;
        if (i == 6 || i == 10 || i == 26 || i == 32) {
            return true;
        }
        return false;
    }

    private boolean shouldDropLogs(String tag, String packageName, int uid) {
        if (IS_DEBUG_ALL) {
            return false;
        }
        Set<String> set = LOG_DROP_SET;
        if (set.contains(tag + uid)) {
            return true;
        }
        return false;
    }

    public void wakeLockLog(String process, IBinder lock, int flags, String tag, String packageName, WorkSource ws, int uid, int pid) {
        if (isScreenOrProximityLock(flags) || (IS_DEBUG && !shouldDropLogs(tag, packageName, uid))) {
            SlogEx.i(TAG_POWER_MS, process + ":L=" + Objects.hashCode(lock) + ",F=0x" + Integer.toHexString(flags) + ",T=" + tag + ",N=" + packageName + ",WS=" + ws + ",U=" + uid + ",P=" + pid);
        }
    }

    public void userActivityLog(long eventTime, int event, int flags, int uid) {
        if (IS_DEBUG && eventTime - this.mLastOneSecActivityTime >= 1000) {
            this.mLastOneSecActivityTime = eventTime;
            SlogEx.i(TAG_POWER_MS, "userActivity:eventTime=" + eventTime + ",event=" + event + ",flags=0x" + Integer.toHexString(flags) + ",uid=" + uid);
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
        if ((!isRunningOnPCMode && HwPCUtils.getPhoneDisplayID() == -1) || (65535 & flags) != 10) {
            return flags;
        }
        int tempFlags = (flags & -11) | 6;
        HwPCUtils.log(TAG, "Replace SCREEN_BRIGHT_WAKE_LOCK flag with SCREEN_DIM_WAKE_LOCK.");
        return tempFlags;
    }

    public boolean checkWakeLockFlag(int flags) {
        int i = 65535 & flags;
        if (i == 6 || i == 10 || i == 26) {
            return true;
        }
        return false;
    }

    public boolean registerScreenStateCallback(IScreenStateCallback callback) {
        if (callback == null) {
            SlogEx.i(TAG, "callback is null");
            return false;
        }
        synchronized (this.mLock) {
            if (this.mClientCallback != null) {
                SlogEx.i(TAG, "already registered ScreenStateCallback");
                return false;
            }
            this.mClientCallback = new ClientCallback(callback);
            return true;
        }
    }

    public boolean unRegisterScreenStateCallback() {
        synchronized (this.mLock) {
            if (this.mClientCallback == null) {
                SlogEx.i(TAG, "not registered ScreenStateCallback");
                return false;
            }
            this.mClientCallback = null;
            return true;
        }
    }

    public void notifyScreenState(int screenState) {
        synchronized (this.mLock) {
            if (this.mClientCallback == null || this.mClientCallback.mCallback == null) {
                SlogEx.i(TAG, "no need to notifyScreenState, not registered");
                return;
            }
            try {
                SlogEx.i(TAG, "notifyScreenState state:" + screenState);
                this.mClientCallback.mCallback.onStateChange(screenState);
            } catch (RemoteException e) {
                SlogEx.e(TAG, "notifyScreenState RemoteException");
            }
        }
    }

    /* access modifiers changed from: private */
    public final class ClientCallback implements IBinder.DeathRecipient {
        private final IScreenStateCallback mCallback;

        ClientCallback(IScreenStateCallback callback) {
            this.mCallback = callback;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            Log.i(HwPowerManagerServiceEx.TAG, "Client mCallback:" + this.mCallback + " is died");
            synchronized (HwPowerManagerServiceEx.this.mLock) {
                HwPowerManagerServiceEx.this.mClientCallback = null;
            }
        }
    }

    public int setHwBrightnessData(String name, Bundle data) {
        if (this.mDisplayManagerInternal == null) {
            this.mDisplayManagerInternal = DisplayManagerInternalEx.getInstance();
        }
        int[] results = {0};
        if (this.mDisplayManagerInternal.setHwBrightnessData(name, data, results)) {
            return results[0];
        }
        return -1;
    }

    public int getHwBrightnessData(String name, Bundle data) {
        if (this.mDisplayManagerInternal == null) {
            this.mDisplayManagerInternal = DisplayManagerInternalEx.getInstance();
        }
        int[] results = {0};
        if (this.mDisplayManagerInternal.getHwBrightnessData(name, data, results)) {
            return results[0];
        }
        return -1;
    }

    public boolean isPowerDisabled(int reason) {
        if (reason != 4 || !HwPartCommInterfaceWraper.disallowOp(61)) {
            return false;
        }
        return true;
    }
}

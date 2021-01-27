package com.android.server.power;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.WorkSource;
import com.huawei.android.os.IScreenStateCallback;

public class DefaultHwPowerManagerServiceEx implements IHwPowerManagerServiceEx {
    public DefaultHwPowerManagerServiceEx(IHwPowerManagerInnerEx pms, Context context) {
    }

    public void wakeLockLog(String process, IBinder lock, int flags, String tag, String packageName, WorkSource ws, int uid, int pid) {
    }

    public void notifyWakeLockAcquiredToDubai(int flags, int lock, String tag, WorkSource workSource, int uid, int pid, String processName) {
    }

    public void notifyWakeLockChangingToDubai(int oldFlags, int newFlags, int lock, String tag, WorkSource workSource, int uid, int pid, String processName) {
    }

    public void notifyWakeLockReleasedToDubai(int flags, int lock) {
    }

    public void userActivityLog(long eventTime, int event, int flags, int uid) {
    }

    public boolean isPowerDisabled(int reason) {
        return false;
    }

    public void onEnterGoToSleep(long eventTime, int reason, int flags, int uid, boolean isAsleep) {
    }

    public boolean interceptBeforeGoToSleep(long eventTime, int reason, int flags, int uid) {
        return false;
    }

    public boolean isWakeLockDisabled(String packageName, int pid, int uid, WorkSource workSource) {
        return false;
    }

    public void notifyScreenState(int screenState) {
    }

    public boolean isAwarePreventScreenOn(String pkgName, String tag) {
        return false;
    }

    public int addWakeLockFlagsForPC(String pkgName, int uid, int flags) {
        return -1;
    }

    public int setHwBrightnessData(String name, Bundle data) {
        return -1;
    }

    public int getHwBrightnessData(String name, Bundle data) {
        return -1;
    }

    public boolean isWakelockCauseWakeUpDisabled() {
        return false;
    }

    public void requestNoUserActivityNotification(int timeout) {
    }

    public boolean checkWakeLockFlag(int flags) {
        return false;
    }

    public boolean registerScreenStateCallback(IScreenStateCallback callback) {
        return false;
    }

    public boolean unRegisterScreenStateCallback() {
        return false;
    }

    public void systemReady() {
    }

    public void onBootPhase(int phase) {
    }
}

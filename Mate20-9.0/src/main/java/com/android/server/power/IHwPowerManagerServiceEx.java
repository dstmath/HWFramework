package com.android.server.power;

import android.os.WorkSource;

public interface IHwPowerManagerServiceEx {
    int addWakeLockFlagsForPC(String str, int i, int i2);

    boolean isAwarePreventScreenOn(String str, String str2);

    void notifySleepEx();

    void notifyWakeLockAcquiredToDubai(int i, int i2, String str, WorkSource workSource, int i3, int i4, String str2);

    void notifyWakeLockReleasedToDubai(int i, int i2);

    void prepareWakeupEx(int i, int i2, String str, String str2);

    void requestNoUserActivityNotification(int i);

    void startWakeupEx(int i, int i2, String str, String str2);
}

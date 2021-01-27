package com.android.server.power;

import android.os.Bundle;
import android.os.IBinder;
import android.os.WorkSource;
import com.huawei.android.os.IScreenStateCallback;

public interface IHwPowerManagerServiceEx {
    int addWakeLockFlagsForPC(String str, int i, int i2);

    boolean checkWakeLockFlag(int i);

    int getHwBrightnessData(String str, Bundle bundle);

    boolean interceptBeforeGoToSleep(long j, int i, int i2, int i3);

    boolean isAwarePreventScreenOn(String str, String str2);

    boolean isPowerDisabled(int i);

    boolean isWakeLockDisabled(String str, int i, int i2, WorkSource workSource);

    boolean isWakelockCauseWakeUpDisabled();

    void notifyScreenState(int i);

    void notifyWakeLockAcquiredToDubai(int i, int i2, String str, WorkSource workSource, int i3, int i4, String str2);

    void notifyWakeLockChangingToDubai(int i, int i2, int i3, String str, WorkSource workSource, int i4, int i5, String str2);

    void notifyWakeLockReleasedToDubai(int i, int i2);

    void onBootPhase(int i);

    void onEnterGoToSleep(long j, int i, int i2, int i3, boolean z);

    boolean registerScreenStateCallback(IScreenStateCallback iScreenStateCallback);

    void requestNoUserActivityNotification(int i);

    int setHwBrightnessData(String str, Bundle bundle);

    void systemReady();

    boolean unRegisterScreenStateCallback();

    void userActivityLog(long j, int i, int i2, int i3);

    void wakeLockLog(String str, IBinder iBinder, int i, String str2, String str3, WorkSource workSource, int i2, int i3);
}

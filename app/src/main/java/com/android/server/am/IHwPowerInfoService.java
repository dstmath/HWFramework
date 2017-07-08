package com.android.server.am;

public interface IHwPowerInfoService {
    void notePowerInfoAcquireWakeLock(String str, int i);

    void notePowerInfoBatteryState(int i, int i2);

    void notePowerInfoBrightness(int i);

    void notePowerInfoChangeWakeLock(String str, int i, String str2, int i2);

    void notePowerInfoConnectionState(int i, boolean z);

    void notePowerInfoGPSStatus(int i);

    void notePowerInfoReleaseWakeLock(String str, int i);

    void notePowerInfoSignalStrength(int i);

    void notePowerInfoStartAlarm(String str, int i);

    void notePowerInfoSuspendState(boolean z);

    void notePowerInfoTopApp(String str, int i);

    void notePowerInfoWakeupReason(String str);

    void notePowerInfoWifiState(int i);

    void noteShutdown();

    void noteStartCamera();

    void noteStopCamera();
}

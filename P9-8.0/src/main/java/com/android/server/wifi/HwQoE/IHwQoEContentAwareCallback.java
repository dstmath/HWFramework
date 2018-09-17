package com.android.server.wifi.HwQoE;

public interface IHwQoEContentAwareCallback {
    void onForegroundAppTypeChange(int i, String str);

    void onForegroundAppWifiSleepChange(boolean z, int i, int i2, String str);

    void onPeriodSpeed(long j);

    void onSensitiveAppStateChange(int i, int i2, boolean z);
}

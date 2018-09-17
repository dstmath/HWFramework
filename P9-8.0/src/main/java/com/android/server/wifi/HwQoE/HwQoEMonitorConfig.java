package com.android.server.wifi.HwQoE;

public class HwQoEMonitorConfig {
    public boolean isNeedNetworkCheck;
    public int mPeriod;

    public HwQoEMonitorConfig(int period, boolean isCheck) {
        this.mPeriod = period;
        this.isNeedNetworkCheck = isCheck;
    }
}

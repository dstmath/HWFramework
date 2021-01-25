package com.huawei.systemmanager.netassistant;

import com.huawei.android.net.HwNetworkStats;

public class HwNetworkStatsEx implements IHwNetworkStatsEx {
    private static final String TAG = "HwNetworkStatsEx";

    @Override // com.huawei.systemmanager.netassistant.IHwNetworkStatsEx
    public boolean setAlertPeriodType(int period) {
        return HwNetworkStats.setAlertPeriodType(period);
    }

    @Override // com.huawei.systemmanager.netassistant.IHwNetworkStatsEx
    public boolean setMpTrafficEnabled(boolean enable) {
        return HwNetworkStats.setMpTrafficEnabled(enable);
    }
}

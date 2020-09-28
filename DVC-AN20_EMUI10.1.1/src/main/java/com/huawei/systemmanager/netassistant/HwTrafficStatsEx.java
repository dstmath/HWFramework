package com.huawei.systemmanager.netassistant;

import com.huawei.android.net.HwTrafficStats;
import java.util.HashMap;

public class HwTrafficStatsEx {
    private static final String TAG = "HwTrafficStatsEx";

    public HashMap<String, Long> getMobileTxBytes() {
        return HwTrafficStats.getMobileTxBytes();
    }

    public HashMap<String, Long> getMobileRxBytes() {
        return HwTrafficStats.getMobileRxBytes();
    }
}

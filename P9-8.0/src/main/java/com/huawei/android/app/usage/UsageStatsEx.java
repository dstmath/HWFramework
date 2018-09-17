package com.huawei.android.app.usage;

import android.app.usage.UsageStats;

public class UsageStatsEx {
    private static final String TAG = "UsageStatsEx";

    public static int getLaunchCount(UsageStats usageStats) {
        return usageStats.mLaunchCount;
    }
}

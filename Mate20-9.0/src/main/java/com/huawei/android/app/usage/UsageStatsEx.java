package com.huawei.android.app.usage;

import android.app.usage.UsageStats;

public class UsageStatsEx {
    private static final String TAG = "UsageStatsEx";

    public static int getLaunchCount(UsageStats usageStats) {
        return usageStats.mLaunchCount;
    }

    public static long getTimeInPCForeground(UsageStats usageStats, boolean isWireless) {
        return isWireless ? usageStats.mTimeInWirelessPCForeground : usageStats.mTimeInPCForeground;
    }

    public static long getLastTimeUsedInPC(UsageStats usageStats, boolean isWireless) {
        return isWireless ? usageStats.mLastTimeUsedInWirelessPC : usageStats.mLastTimeUsedInPC;
    }
}

package com.huawei.android.app.usage;

import android.app.usage.UsageStats;

public class UsageStatsEx {
    private static final String TAG = "UsageStatsEx";

    public static int getLaunchCount(UsageStats usageStats) {
        if (usageStats != null) {
            return usageStats.mLaunchCount;
        }
        return -1;
    }

    public static int getAppLaunchCount(UsageStats usageStats) {
        if (usageStats != null) {
            return usageStats.mAppLaunchCount;
        }
        return -1;
    }

    public static long getTimeInPCForeground(UsageStats usageStats, boolean isWireless) {
        if (usageStats != null) {
            return isWireless ? usageStats.mTimeInWirelessPCForeground : usageStats.mTimeInPCForeground;
        }
        return -1;
    }

    public static long getLastTimeUsedInPC(UsageStats usageStats, boolean isWireless) {
        if (usageStats != null) {
            return isWireless ? usageStats.mLastTimeUsedInWirelessPC : usageStats.mLastTimeUsedInPC;
        }
        return -1;
    }
}

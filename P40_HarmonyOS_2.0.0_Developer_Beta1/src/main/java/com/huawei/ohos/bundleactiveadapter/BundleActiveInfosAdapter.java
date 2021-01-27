package com.huawei.ohos.bundleactiveadapter;

import android.app.usage.UsageStats;

public final class BundleActiveInfosAdapter {
    public static final long INVALID_LONG_VALUE = -1;
    public static final String INVALID_STRING_VALUE = "";
    private UsageStats mUsageStats;

    public BundleActiveInfosAdapter(UsageStats usageStats) {
        this.mUsageStats = usageStats;
    }

    public long queryAbilityPrevAccessMs() {
        UsageStats usageStats = this.mUsageStats;
        if (usageStats == null) {
            return -1;
        }
        return usageStats.getLastTimeUsed();
    }

    public long queryAbilityInFgWholeMs() {
        UsageStats usageStats = this.mUsageStats;
        if (usageStats == null) {
            return -1;
        }
        return usageStats.getTotalTimeInForeground();
    }

    public void merge(BundleActiveInfosAdapter bundleActiveInfosAdapter) {
        UsageStats usageStats = this.mUsageStats;
        if (usageStats != null && bundleActiveInfosAdapter != null) {
            usageStats.add(bundleActiveInfosAdapter.mUsageStats);
        }
    }

    public long queryInfosBeginMs() {
        UsageStats usageStats = this.mUsageStats;
        if (usageStats == null) {
            return -1;
        }
        return usageStats.getFirstTimeStamp();
    }

    public long queryFgAbilityPrevAccessMs() {
        UsageStats usageStats = this.mUsageStats;
        if (usageStats == null) {
            return -1;
        }
        return usageStats.getLastTimeForegroundServiceUsed();
    }

    public long queryInfosEndMs() {
        UsageStats usageStats = this.mUsageStats;
        if (usageStats == null) {
            return -1;
        }
        return usageStats.getLastTimeStamp();
    }

    public long queryAbilityPrevSeenMs() {
        UsageStats usageStats = this.mUsageStats;
        if (usageStats == null) {
            return -1;
        }
        return usageStats.getLastTimeVisible();
    }

    public long queryFgAbilityAccessWholeMs() {
        UsageStats usageStats = this.mUsageStats;
        if (usageStats == null) {
            return -1;
        }
        return usageStats.getTotalTimeForegroundServiceUsed();
    }

    public long queryAbilitySeenWholeMs() {
        UsageStats usageStats = this.mUsageStats;
        if (usageStats == null) {
            return -1;
        }
        return usageStats.getTotalTimeVisible();
    }

    public String queryBundleName() {
        UsageStats usageStats = this.mUsageStats;
        if (usageStats == null) {
            return "";
        }
        return usageStats.getPackageName();
    }
}

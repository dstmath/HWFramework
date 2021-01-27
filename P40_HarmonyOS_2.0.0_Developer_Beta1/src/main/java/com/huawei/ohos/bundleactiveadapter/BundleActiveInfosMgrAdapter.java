package com.huawei.ohos.bundleactiveadapter;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import ohos.app.Context;

public final class BundleActiveInfosMgrAdapter {
    public static final int BY_ANNUALLY = 4;
    public static final int BY_DAILY = 1;
    public static final int BY_MONTHLY = 3;
    public static final int BY_OPTIMIZED = 0;
    public static final int BY_WEEKLY = 2;
    public static final int USAGE_PRIORITY_GROUP_ALIVE = 1;
    public static final int USAGE_PRIORITY_GROUP_NEVER = 5;
    public static final int USAGE_PRIORITY_GROUP_OFTEN = 3;
    public static final int USAGE_PRIORITY_GROUP_PREFERENCE = 2;
    public static final int USAGE_PRIORITY_GROUP_PRIVILEGE = 0;
    public static final int USAGE_PRIORITY_GROUP_SELDOM = 4;
    private final UsageStatsManager mUsageStateManager;

    private BundleActiveInfosMgrAdapter(UsageStatsManager usageStatsManager) {
        this.mUsageStateManager = usageStatsManager;
    }

    public static BundleActiveInfosMgrAdapter newInstance(Context context) {
        if (context == null) {
            return null;
        }
        Object hostContext = context.getHostContext();
        if (!(hostContext instanceof android.content.Context)) {
            return null;
        }
        Object systemService = ((android.content.Context) hostContext).getSystemService("usagestats");
        if (!(systemService instanceof UsageStatsManager)) {
            return null;
        }
        return new BundleActiveInfosMgrAdapter((UsageStatsManager) systemService);
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x002a  */
    public List<BundleActiveInfosAdapter> queryBundleActiveInfosByInterval(int i, long j, long j2) {
        List<UsageStats> queryUsageStats;
        ArrayList arrayList = new ArrayList();
        if (i >= 0) {
            int i2 = 4;
            if (i <= 4) {
                int i3 = 2;
                if (i != 0) {
                    if (i != 1) {
                        if (i == 2) {
                            i3 = 1;
                        } else if (i != 3) {
                            if (i == 4) {
                                i3 = 3;
                            }
                        }
                        queryUsageStats = this.mUsageStateManager.queryUsageStats(i3, j, j2);
                        if (queryUsageStats != null) {
                            queryUsageStats.forEach(new Consumer(arrayList) {
                                /* class com.huawei.ohos.bundleactiveadapter.$$Lambda$BundleActiveInfosMgrAdapter$cvuNA7rebcoJ19FrrlYI_FeZwE */
                                private final /* synthetic */ ArrayList f$0;

                                {
                                    this.f$0 = r1;
                                }

                                @Override // java.util.function.Consumer
                                public final void accept(Object obj) {
                                    BundleActiveInfosMgrAdapter.lambda$queryBundleActiveInfosByInterval$0(this.f$0, (UsageStats) obj);
                                }
                            });
                        }
                    } else {
                        i2 = 0;
                    }
                }
                i3 = i2;
                queryUsageStats = this.mUsageStateManager.queryUsageStats(i3, j, j2);
                if (queryUsageStats != null) {
                }
            }
        }
        return arrayList;
    }

    public Map<String, BundleActiveInfosAdapter> queryBundleActiveInfos(long j, long j2) {
        HashMap hashMap = new HashMap();
        Map<String, UsageStats> queryAndAggregateUsageStats = this.mUsageStateManager.queryAndAggregateUsageStats(j, j2);
        if (queryAndAggregateUsageStats != null) {
            queryAndAggregateUsageStats.forEach(new BiConsumer(hashMap) {
                /* class com.huawei.ohos.bundleactiveadapter.$$Lambda$BundleActiveInfosMgrAdapter$eZWGYFzb1w4NmEvMqOi9UzVabuU */
                private final /* synthetic */ HashMap f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.BiConsumer
                public final void accept(Object obj, Object obj2) {
                    BundleActiveInfosMgrAdapter.lambda$queryBundleActiveInfos$1(this.f$0, (String) obj, (UsageStats) obj2);
                }
            });
        }
        return hashMap;
    }

    public BundleActiveStatesAdapter queryBundleActiveStates(long j, long j2) {
        return new BundleActiveStatesAdapter(this.mUsageStateManager.queryEvents(j, j2));
    }

    public BundleActiveStatesAdapter queryCurrentBundleActiveStates(long j, long j2) {
        return new BundleActiveStatesAdapter(this.mUsageStateManager.queryEventsForSelf(j, j2));
    }

    public boolean isIdleState(String str) {
        return this.mUsageStateManager.isAppInactive(str);
    }

    public int queryAppUsagePriorityGroup() {
        int appStandbyBucket = this.mUsageStateManager.getAppStandbyBucket();
        if (appStandbyBucket == 5) {
            return 0;
        }
        if (appStandbyBucket == 10) {
            return 1;
        }
        if (appStandbyBucket == 20) {
            return 2;
        }
        if (appStandbyBucket == 30) {
            return 3;
        }
        if (appStandbyBucket == 40) {
            return 4;
        }
        if (appStandbyBucket != 50) {
        }
        return 5;
    }
}

package com.android.server.content;

import android.app.usage.UsageStatsManagerInternal;
import android.os.SystemClock;
import android.util.Pair;
import com.android.server.AppStateTracker;
import com.android.server.LocalServices;
import java.util.HashMap;

class SyncAdapterStateFetcher {
    private final HashMap<Pair<Integer, String>, Integer> mBucketCache = new HashMap<>();

    public int getStandbyBucket(int userId, String packageName) {
        Pair<Integer, String> key = Pair.create(Integer.valueOf(userId), packageName);
        Integer cached = this.mBucketCache.get(key);
        if (cached != null) {
            return cached.intValue();
        }
        UsageStatsManagerInternal usmi = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
        if (usmi == null) {
            return -1;
        }
        int value = usmi.getAppStandbyBucket(packageName, userId, SystemClock.elapsedRealtime());
        this.mBucketCache.put(key, Integer.valueOf(value));
        return value;
    }

    public boolean isAppActive(int uid) {
        AppStateTracker ast = (AppStateTracker) LocalServices.getService(AppStateTracker.class);
        if (ast == null) {
            return false;
        }
        return ast.isUidActive(uid);
    }
}

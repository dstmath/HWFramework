package android.app.usage;

import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Log;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class UsageStatsManager {
    public static final int INTERVAL_BEST = 4;
    public static final int INTERVAL_COUNT = 4;
    public static final int INTERVAL_DAILY = 0;
    public static final int INTERVAL_MONTHLY = 2;
    public static final int INTERVAL_WEEKLY = 1;
    public static final int INTERVAL_YEARLY = 3;
    private static final String TAG = "UsageStatsManager";
    private static final UsageEvents sEmptyResults = new UsageEvents();
    private final Context mContext;
    private final IUsageStatsManager mService;

    public UsageStatsManager(Context context, IUsageStatsManager service) {
        this.mContext = context;
        this.mService = service;
    }

    public List<UsageStats> queryUsageStats(int intervalType, long beginTime, long endTime) {
        try {
            ParceledListSlice<UsageStats> slice = this.mService.queryUsageStats(intervalType, beginTime, endTime, this.mContext.getOpPackageName());
            if (slice != null) {
                return slice.getList();
            }
        } catch (RemoteException e) {
        }
        return Collections.emptyList();
    }

    public List<ConfigurationStats> queryConfigurations(int intervalType, long beginTime, long endTime) {
        try {
            ParceledListSlice<ConfigurationStats> slice = this.mService.queryConfigurationStats(intervalType, beginTime, endTime, this.mContext.getOpPackageName());
            if (slice != null) {
                return slice.getList();
            }
        } catch (RemoteException e) {
        }
        return Collections.emptyList();
    }

    public UsageEvents queryEvents(long beginTime, long endTime) {
        try {
            UsageEvents iter = this.mService.queryEvents(beginTime, endTime, this.mContext.getOpPackageName());
            if (iter != null) {
                return iter;
            }
        } catch (RemoteException e) {
        }
        return sEmptyResults;
    }

    public Map<String, UsageStats> queryAndAggregateUsageStats(long beginTime, long endTime) {
        List<UsageStats> stats = queryUsageStats(4, beginTime, endTime);
        if (stats.isEmpty()) {
            return Collections.emptyMap();
        }
        ArrayMap<String, UsageStats> aggregatedStats = new ArrayMap();
        int statCount = stats.size();
        for (int i = 0; i < statCount; i++) {
            UsageStats newStat = (UsageStats) stats.get(i);
            UsageStats existingStat = (UsageStats) aggregatedStats.get(newStat.getPackageName());
            if (existingStat == null) {
                aggregatedStats.put(newStat.mPackageName, newStat);
            } else {
                existingStat.add(newStat);
            }
        }
        return aggregatedStats;
    }

    public boolean isAppInactive(String packageName) {
        try {
            return this.mService.isAppInactive(packageName, UserHandle.myUserId());
        } catch (RemoteException e) {
            return false;
        }
    }

    public void setAppInactive(String packageName, boolean inactive) {
        try {
            this.mService.setAppInactive(packageName, inactive, UserHandle.myUserId());
        } catch (RemoteException e) {
        }
    }

    public void whitelistAppTemporarily(String packageName, long duration, UserHandle user) {
        try {
            this.mService.whitelistAppTemporarily(packageName, duration, user.getIdentifier());
        } catch (RemoteException e) {
        }
    }

    public void onCarrierPrivilegedAppsChanged() {
        try {
            this.mService.onCarrierPrivilegedAppsChanged();
        } catch (RemoteException e) {
        }
    }

    public void reportChooserSelection(String packageName, int userId, String contentType, String[] annotations, String action) {
        try {
            this.mService.reportChooserSelection(packageName, userId, contentType, annotations, action);
        } catch (RemoteException e) {
            Log.e(TAG, "reportChooserSelection()");
        }
    }
}

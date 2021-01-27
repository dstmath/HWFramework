package android.app.usage;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.backup.FullBackup;
import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.media.TtmlUtils;
import android.os.IncidentManager;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.ArrayMap;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class UsageStatsManager {
    @SystemApi
    public static final String EXTRA_OBSERVER_ID = "android.app.usage.extra.OBSERVER_ID";
    @SystemApi
    public static final String EXTRA_TIME_LIMIT = "android.app.usage.extra.TIME_LIMIT";
    @SystemApi
    public static final String EXTRA_TIME_USED = "android.app.usage.extra.TIME_USED";
    public static final int INTERVAL_BEST = 4;
    public static final int INTERVAL_COUNT = 4;
    public static final int INTERVAL_DAILY = 0;
    public static final int INTERVAL_MONTHLY = 2;
    public static final int INTERVAL_WEEKLY = 1;
    public static final int INTERVAL_YEARLY = 3;
    public static final int REASON_MAIN_DEFAULT = 256;
    public static final int REASON_MAIN_FORCED = 1024;
    public static final int REASON_MAIN_MASK = 65280;
    public static final int REASON_MAIN_PREDICTED = 1280;
    public static final int REASON_MAIN_TIMEOUT = 512;
    public static final int REASON_MAIN_USAGE = 768;
    public static final int REASON_SUB_MASK = 255;
    public static final int REASON_SUB_PREDICTED_RESTORED = 1;
    public static final int REASON_SUB_USAGE_ACTIVE_TIMEOUT = 7;
    public static final int REASON_SUB_USAGE_EXEMPTED_SYNC_SCHEDULED_DOZE = 12;
    public static final int REASON_SUB_USAGE_EXEMPTED_SYNC_SCHEDULED_NON_DOZE = 11;
    public static final int REASON_SUB_USAGE_EXEMPTED_SYNC_START = 13;
    public static final int REASON_SUB_USAGE_FOREGROUND_SERVICE_START = 15;
    public static final int REASON_SUB_USAGE_MOVE_TO_BACKGROUND = 5;
    public static final int REASON_SUB_USAGE_MOVE_TO_FOREGROUND = 4;
    public static final int REASON_SUB_USAGE_NOTIFICATION_SEEN = 2;
    public static final int REASON_SUB_USAGE_SLICE_PINNED = 9;
    public static final int REASON_SUB_USAGE_SLICE_PINNED_PRIV = 10;
    public static final int REASON_SUB_USAGE_SYNC_ADAPTER = 8;
    public static final int REASON_SUB_USAGE_SYSTEM_INTERACTION = 1;
    public static final int REASON_SUB_USAGE_SYSTEM_UPDATE = 6;
    public static final int REASON_SUB_USAGE_UNEXEMPTED_SYNC_SCHEDULED = 14;
    public static final int REASON_SUB_USAGE_USER_INTERACTION = 3;
    public static final int STANDBY_BUCKET_ACTIVE = 10;
    @SystemApi
    public static final int STANDBY_BUCKET_EXEMPTED = 5;
    public static final int STANDBY_BUCKET_FREQUENT = 30;
    @SystemApi
    public static final int STANDBY_BUCKET_NEVER = 50;
    public static final int STANDBY_BUCKET_RARE = 40;
    public static final int STANDBY_BUCKET_WORKING_SET = 20;
    @SystemApi
    public static final int USAGE_SOURCE_CURRENT_ACTIVITY = 2;
    @SystemApi
    public static final int USAGE_SOURCE_TASK_ROOT_ACTIVITY = 1;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private static final UsageEvents sEmptyResults = new UsageEvents();
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private final Context mContext;
    @UnsupportedAppUsage
    private final IUsageStatsManager mService;

    @Retention(RetentionPolicy.SOURCE)
    public @interface StandbyBuckets {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface UsageSource {
    }

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

    public List<EventStats> queryEventStats(int intervalType, long beginTime, long endTime) {
        try {
            ParceledListSlice<EventStats> slice = this.mService.queryEventStats(intervalType, beginTime, endTime, this.mContext.getOpPackageName());
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

    public UsageEvents queryEventsForSelf(long beginTime, long endTime) {
        try {
            UsageEvents events = this.mService.queryEventsForPackage(beginTime, endTime, this.mContext.getOpPackageName());
            if (events != null) {
                return events;
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
        ArrayMap<String, UsageStats> aggregatedStats = new ArrayMap<>();
        int statCount = stats.size();
        for (int i = 0; i < statCount; i++) {
            UsageStats newStat = stats.get(i);
            UsageStats existingStat = aggregatedStats.get(newStat.getPackageName());
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
            return this.mService.isAppInactive(packageName, this.mContext.getUserId());
        } catch (RemoteException e) {
            return false;
        }
    }

    public void setAppInactive(String packageName, boolean inactive) {
        try {
            this.mService.setAppInactive(packageName, inactive, this.mContext.getUserId());
        } catch (RemoteException e) {
        }
    }

    public int getAppStandbyBucket() {
        try {
            return this.mService.getAppStandbyBucket(this.mContext.getOpPackageName(), this.mContext.getOpPackageName(), this.mContext.getUserId());
        } catch (RemoteException e) {
            return 10;
        }
    }

    @SystemApi
    public int getAppStandbyBucket(String packageName) {
        try {
            return this.mService.getAppStandbyBucket(packageName, this.mContext.getOpPackageName(), this.mContext.getUserId());
        } catch (RemoteException e) {
            return 10;
        }
    }

    @SystemApi
    public void setAppStandbyBucket(String packageName, int bucket) {
        try {
            this.mService.setAppStandbyBucket(packageName, bucket, this.mContext.getUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public Map<String, Integer> getAppStandbyBuckets() {
        try {
            List<AppStandbyInfo> bucketList = this.mService.getAppStandbyBuckets(this.mContext.getOpPackageName(), this.mContext.getUserId()).getList();
            ArrayMap<String, Integer> bucketMap = new ArrayMap<>();
            int n = bucketList.size();
            for (int i = 0; i < n; i++) {
                AppStandbyInfo bucketInfo = bucketList.get(i);
                bucketMap.put(bucketInfo.mPackageName, Integer.valueOf(bucketInfo.mStandbyBucket));
            }
            return bucketMap;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void setAppStandbyBuckets(Map<String, Integer> appBuckets) {
        if (appBuckets != null) {
            List<AppStandbyInfo> bucketInfoList = new ArrayList<>(appBuckets.size());
            for (Map.Entry<String, Integer> bucketEntry : appBuckets.entrySet()) {
                bucketInfoList.add(new AppStandbyInfo(bucketEntry.getKey(), bucketEntry.getValue().intValue()));
            }
            try {
                this.mService.setAppStandbyBuckets(new ParceledListSlice<>(bucketInfoList), this.mContext.getUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @SystemApi
    public void registerAppUsageObserver(int observerId, String[] observedEntities, long timeLimit, TimeUnit timeUnit, PendingIntent callbackIntent) {
        try {
            this.mService.registerAppUsageObserver(observerId, observedEntities, timeUnit.toMillis(timeLimit), callbackIntent, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void unregisterAppUsageObserver(int observerId) {
        try {
            this.mService.unregisterAppUsageObserver(observerId, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void registerUsageSessionObserver(int sessionObserverId, String[] observedEntities, Duration timeLimit, Duration sessionThresholdTime, PendingIntent limitReachedCallbackIntent, PendingIntent sessionEndCallbackIntent) {
        try {
            this.mService.registerUsageSessionObserver(sessionObserverId, observedEntities, timeLimit.toMillis(), sessionThresholdTime.toMillis(), limitReachedCallbackIntent, sessionEndCallbackIntent, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void unregisterUsageSessionObserver(int sessionObserverId) {
        try {
            this.mService.unregisterUsageSessionObserver(sessionObserverId, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void registerAppUsageLimitObserver(int observerId, String[] observedEntities, Duration timeLimit, Duration timeUsed, PendingIntent callbackIntent) {
        try {
            this.mService.registerAppUsageLimitObserver(observerId, observedEntities, timeLimit.toMillis(), timeUsed.toMillis(), callbackIntent, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void unregisterAppUsageLimitObserver(int observerId) {
        try {
            this.mService.unregisterAppUsageLimitObserver(observerId, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void reportUsageStart(Activity activity, String token) {
        try {
            this.mService.reportUsageStart(activity.getActivityToken(), token, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void reportUsageStart(Activity activity, String token, long timeAgoMs) {
        try {
            this.mService.reportPastUsageStart(activity.getActivityToken(), token, timeAgoMs, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void reportUsageStop(Activity activity, String token) {
        try {
            this.mService.reportUsageStop(activity.getActivityToken(), token, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public int getUsageSource() {
        try {
            return this.mService.getUsageSource();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void forceUsageSourceSettingRead() {
        try {
            this.mService.forceUsageSourceSettingRead();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static String reasonToString(int standbyReason) {
        StringBuilder sb = new StringBuilder();
        int i = 65280 & standbyReason;
        if (i == 256) {
            sb.append("d");
        } else if (i != 512) {
            if (i == 768) {
                sb.append("u");
                switch (standbyReason & 255) {
                    case 1:
                        sb.append("-si");
                        break;
                    case 2:
                        sb.append("-ns");
                        break;
                    case 3:
                        sb.append("-ui");
                        break;
                    case 4:
                        sb.append("-mf");
                        break;
                    case 5:
                        sb.append("-mb");
                        break;
                    case 6:
                        sb.append("-su");
                        break;
                    case 7:
                        sb.append("-at");
                        break;
                    case 8:
                        sb.append("-sa");
                        break;
                    case 9:
                        sb.append("-lp");
                        break;
                    case 10:
                        sb.append("-lv");
                        break;
                    case 11:
                        sb.append("-en");
                        break;
                    case 12:
                        sb.append("-ed");
                        break;
                    case 13:
                        sb.append("-es");
                        break;
                    case 14:
                        sb.append("-uss");
                        break;
                    case 15:
                        sb.append("-fss");
                        break;
                }
            } else if (i == 1024) {
                sb.append(FullBackup.FILES_TREE_TOKEN);
            } else if (i == 1280) {
                sb.append(TtmlUtils.TAG_P);
                if ((standbyReason & 255) == 1) {
                    sb.append("-r");
                }
            }
        } else {
            sb.append(IncidentManager.URI_PARAM_TIMESTAMP);
        }
        return sb.toString();
    }

    public static String usageSourceToString(int usageSource) {
        if (usageSource == 1) {
            return "TASK_ROOT_ACTIVITY";
        }
        if (usageSource == 2) {
            return "CURRENT_ACTIVITY";
        }
        return "UNKNOWN(" + usageSource + ")";
    }

    @SystemApi
    public void whitelistAppTemporarily(String packageName, long duration, UserHandle user) {
        try {
            this.mService.whitelistAppTemporarily(packageName, duration, user.getIdentifier());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void onCarrierPrivilegedAppsChanged() {
        try {
            this.mService.onCarrierPrivilegedAppsChanged();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void reportChooserSelection(String packageName, int userId, String contentType, String[] annotations, String action) {
        try {
            this.mService.reportChooserSelection(packageName, userId, contentType, annotations, action);
        } catch (RemoteException e) {
        }
    }
}

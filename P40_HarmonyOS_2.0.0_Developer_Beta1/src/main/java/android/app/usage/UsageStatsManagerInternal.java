package android.app.usage;

import android.content.ComponentName;
import android.content.res.Configuration;
import android.os.UserHandle;
import java.util.List;
import java.util.Set;

public abstract class UsageStatsManagerInternal {
    public abstract void addAppIdleStateChangeListener(AppIdleStateChangeListener appIdleStateChangeListener);

    public abstract void applyRestoredPayload(int i, String str, byte[] bArr);

    public abstract int getAppStandbyBucket(String str, int i, long j);

    public abstract AppUsageLimitData getAppUsageLimit(String str, UserHandle userHandle);

    public abstract byte[] getBackupPayload(int i, String str);

    public abstract int[] getIdleUidsForUser(int i);

    public abstract long getTimeSinceLastJobRun(String str, int i);

    public abstract boolean isAppIdle(String str, int i, int i2);

    public abstract boolean isAppIdleParoleOn();

    public abstract void onActiveAdminAdded(String str, int i);

    public abstract void onAdminDataAvailable();

    public abstract void prepareForPossibleShutdown();

    public abstract void prepareShutdown();

    public abstract List<UsageStats> queryUsageStatsForUser(int i, int i2, long j, long j2, boolean z);

    public abstract void removeAppIdleStateChangeListener(AppIdleStateChangeListener appIdleStateChangeListener);

    public abstract void reportAppJobState(String str, int i, int i2, long j);

    public abstract void reportConfigurationChange(Configuration configuration, int i);

    public abstract void reportContentProviderUsage(String str, String str2, int i);

    public abstract void reportEvent(ComponentName componentName, int i, int i2, int i3, ComponentName componentName2);

    public abstract void reportEvent(String str, int i, int i2);

    public abstract void reportExemptedSyncStart(String str, int i);

    public abstract void reportInterruptiveNotification(String str, String str2, int i);

    public abstract void reportShortcutUsage(String str, String str2, int i);

    public abstract void reportSyncScheduled(String str, int i, boolean z);

    public abstract void setActiveAdminApps(Set<String> set, int i);

    public abstract void setLastJobRunTime(String str, int i, long j);

    public static abstract class AppIdleStateChangeListener {
        public abstract void onAppIdleStateChanged(String str, int i, boolean z, int i2, int i3);

        public abstract void onParoleStateChanged(boolean z);

        public void onUserInteractionStarted(String packageName, int userId) {
        }
    }

    public static class AppUsageLimitData {
        private final long mTotalUsageLimit;
        private final long mUsageRemaining;

        public AppUsageLimitData(long totalUsageLimit, long usageRemaining) {
            this.mTotalUsageLimit = totalUsageLimit;
            this.mUsageRemaining = usageRemaining;
        }

        public long getTotalUsageLimit() {
            return this.mTotalUsageLimit;
        }

        public long getUsageRemaining() {
            return this.mUsageRemaining;
        }
    }
}

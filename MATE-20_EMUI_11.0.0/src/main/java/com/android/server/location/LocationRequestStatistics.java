package com.android.server.location;

import android.os.SystemClock;
import android.util.Log;
import com.android.server.job.controllers.JobStatus;
import java.util.HashMap;

public class LocationRequestStatistics {
    private static final String TAG = "LocationStats";
    public final HashMap<PackageProviderKey, PackageStatistics> statistics = new HashMap<>();

    public void startRequesting(String packageName, String providerName, long intervalMs, boolean isForeground) {
        PackageProviderKey key = new PackageProviderKey(packageName, providerName);
        PackageStatistics stats = this.statistics.get(key);
        if (stats == null) {
            stats = new PackageStatistics();
            this.statistics.put(key, stats);
        }
        stats.startRequesting(intervalMs);
        stats.updateForeground(isForeground);
    }

    public void stopRequesting(String packageName, String providerName) {
        PackageStatistics stats = this.statistics.get(new PackageProviderKey(packageName, providerName));
        if (stats != null) {
            stats.stopRequesting();
        }
    }

    public void updateForeground(String packageName, String providerName, boolean isForeground) {
        PackageStatistics stats = this.statistics.get(new PackageProviderKey(packageName, providerName));
        if (stats != null) {
            stats.updateForeground(isForeground);
        }
    }

    public static class PackageProviderKey {
        public final String packageName;
        public final String providerName;

        public PackageProviderKey(String packageName2, String providerName2) {
            this.packageName = packageName2;
            this.providerName = providerName2;
        }

        public boolean equals(Object other) {
            if (!(other instanceof PackageProviderKey)) {
                return false;
            }
            PackageProviderKey otherKey = (PackageProviderKey) other;
            if (!this.packageName.equals(otherKey.packageName) || !this.providerName.equals(otherKey.providerName)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return this.packageName.hashCode() + (this.providerName.hashCode() * 31);
        }
    }

    public static class PackageStatistics {
        private long mFastestIntervalMs;
        private long mForegroundDurationMs;
        private final long mInitialElapsedTimeMs;
        private long mLastActivitationElapsedTimeMs;
        private long mLastForegroundElapsedTimeMs;
        private long mLastStopElapsedTimeMs;
        private int mNumActiveRequests;
        private long mSlowestIntervalMs;
        private long mTotalDurationMs;

        private PackageStatistics() {
            this.mInitialElapsedTimeMs = SystemClock.elapsedRealtime();
            this.mNumActiveRequests = 0;
            this.mTotalDurationMs = 0;
            this.mFastestIntervalMs = JobStatus.NO_LATEST_RUNTIME;
            this.mSlowestIntervalMs = 0;
            this.mForegroundDurationMs = 0;
            this.mLastForegroundElapsedTimeMs = 0;
            this.mLastStopElapsedTimeMs = 0;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void startRequesting(long intervalMs) {
            if (this.mNumActiveRequests == 0) {
                this.mLastActivitationElapsedTimeMs = SystemClock.elapsedRealtime();
            }
            if (intervalMs < this.mFastestIntervalMs) {
                this.mFastestIntervalMs = intervalMs;
            }
            if (intervalMs > this.mSlowestIntervalMs) {
                this.mSlowestIntervalMs = intervalMs;
            }
            this.mNumActiveRequests++;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void updateForeground(boolean isForeground) {
            long nowElapsedTimeMs = SystemClock.elapsedRealtime();
            long j = this.mLastForegroundElapsedTimeMs;
            long j2 = 0;
            if (j != 0) {
                this.mForegroundDurationMs += nowElapsedTimeMs - j;
            }
            if (isForeground) {
                j2 = nowElapsedTimeMs;
            }
            this.mLastForegroundElapsedTimeMs = j2;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void stopRequesting() {
            int i = this.mNumActiveRequests;
            if (i <= 0) {
                Log.e(LocationRequestStatistics.TAG, "Reference counting corrupted in usage statistics.");
                return;
            }
            this.mNumActiveRequests = i - 1;
            if (this.mNumActiveRequests == 0) {
                this.mLastStopElapsedTimeMs = SystemClock.elapsedRealtime();
                this.mTotalDurationMs += this.mLastStopElapsedTimeMs - this.mLastActivitationElapsedTimeMs;
                updateForeground(false);
            }
        }

        public long getDurationMs() {
            long currentDurationMs = this.mTotalDurationMs;
            if (this.mNumActiveRequests > 0) {
                return currentDurationMs + (SystemClock.elapsedRealtime() - this.mLastActivitationElapsedTimeMs);
            }
            return currentDurationMs;
        }

        public long getForegroundDurationMs() {
            long currentDurationMs = this.mForegroundDurationMs;
            if (this.mLastForegroundElapsedTimeMs != 0) {
                return currentDurationMs + (SystemClock.elapsedRealtime() - this.mLastForegroundElapsedTimeMs);
            }
            return currentDurationMs;
        }

        public long getTimeSinceFirstRequestMs() {
            return SystemClock.elapsedRealtime() - this.mInitialElapsedTimeMs;
        }

        public long getTimeSinceLastRequestStoppedMs() {
            return SystemClock.elapsedRealtime() - this.mLastStopElapsedTimeMs;
        }

        public long getFastestIntervalMs() {
            return this.mFastestIntervalMs;
        }

        public long getSlowestIntervalMs() {
            return this.mSlowestIntervalMs;
        }

        public boolean isActive() {
            return this.mNumActiveRequests > 0;
        }

        public String toString() {
            StringBuilder s = new StringBuilder();
            if (this.mFastestIntervalMs == this.mSlowestIntervalMs) {
                s.append("Interval ");
                s.append(this.mFastestIntervalMs / 1000);
                s.append(" seconds");
            } else {
                s.append("Min interval ");
                s.append(this.mFastestIntervalMs / 1000);
                s.append(" seconds");
                s.append(": Max interval ");
                s.append(this.mSlowestIntervalMs / 1000);
                s.append(" seconds");
            }
            s.append(": Duration requested ");
            s.append((getDurationMs() / 1000) / 60);
            s.append(" total, ");
            s.append((getForegroundDurationMs() / 1000) / 60);
            s.append(" foreground, out of the last ");
            s.append((getTimeSinceFirstRequestMs() / 1000) / 60);
            s.append(" minutes");
            if (isActive()) {
                s.append(": Currently active");
            } else {
                s.append(": Last active ");
                s.append((getTimeSinceLastRequestStoppedMs() / 1000) / 60);
                s.append(" minutes ago");
            }
            return s.toString();
        }
    }
}

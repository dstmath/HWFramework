package com.android.server.location;

import android.os.SystemClock;
import android.util.Log;
import com.android.server.job.controllers.JobStatus;
import java.util.HashMap;

public class LocationRequestStatistics {
    private static final String TAG = "LocationStats";
    public final HashMap<PackageProviderKey, PackageStatistics> statistics = new HashMap();

    public static class PackageProviderKey {
        public final String packageName;
        public final String providerName;

        public PackageProviderKey(String packageName, String providerName) {
            this.packageName = packageName;
            this.providerName = providerName;
        }

        public boolean equals(Object other) {
            boolean z = false;
            if (!(other instanceof PackageProviderKey)) {
                return false;
            }
            PackageProviderKey otherKey = (PackageProviderKey) other;
            if (this.packageName.equals(otherKey.packageName)) {
                z = this.providerName.equals(otherKey.providerName);
            }
            return z;
        }

        public int hashCode() {
            return this.packageName.hashCode() + (this.providerName.hashCode() * 31);
        }
    }

    public static class PackageStatistics {
        private long mFastestIntervalMs;
        private final long mInitialElapsedTimeMs;
        private long mLastActivitationElapsedTimeMs;
        private int mNumActiveRequests;
        private long mSlowestIntervalMs;
        private long mTotalDurationMs;

        /* synthetic */ PackageStatistics(PackageStatistics -this0) {
            this();
        }

        private PackageStatistics() {
            this.mInitialElapsedTimeMs = SystemClock.elapsedRealtime();
            this.mNumActiveRequests = 0;
            this.mTotalDurationMs = 0;
            this.mFastestIntervalMs = JobStatus.NO_LATEST_RUNTIME;
            this.mSlowestIntervalMs = 0;
        }

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

        private void stopRequesting() {
            if (this.mNumActiveRequests <= 0) {
                Log.e(LocationRequestStatistics.TAG, "Reference counting corrupted in usage statistics.");
                return;
            }
            this.mNumActiveRequests--;
            if (this.mNumActiveRequests == 0) {
                this.mTotalDurationMs += SystemClock.elapsedRealtime() - this.mLastActivitationElapsedTimeMs;
            }
        }

        public long getDurationMs() {
            long currentDurationMs = this.mTotalDurationMs;
            if (this.mNumActiveRequests > 0) {
                return currentDurationMs + (SystemClock.elapsedRealtime() - this.mLastActivitationElapsedTimeMs);
            }
            return currentDurationMs;
        }

        public long getTimeSinceFirstRequestMs() {
            return SystemClock.elapsedRealtime() - this.mInitialElapsedTimeMs;
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
                s.append("Interval ").append(this.mFastestIntervalMs / 1000).append(" seconds");
            } else {
                s.append("Min interval ").append(this.mFastestIntervalMs / 1000).append(" seconds");
                s.append(": Max interval ").append(this.mSlowestIntervalMs / 1000).append(" seconds");
            }
            s.append(": Duration requested ").append((getDurationMs() / 1000) / 60).append(" out of the last ").append((getTimeSinceFirstRequestMs() / 1000) / 60).append(" minutes");
            if (isActive()) {
                s.append(": Currently active");
            }
            return s.toString();
        }
    }

    public void startRequesting(String packageName, String providerName, long intervalMs) {
        PackageProviderKey key = new PackageProviderKey(packageName, providerName);
        PackageStatistics stats = (PackageStatistics) this.statistics.get(key);
        if (stats == null) {
            stats = new PackageStatistics();
            this.statistics.put(key, stats);
        }
        stats.startRequesting(intervalMs);
    }

    public void stopRequesting(String packageName, String providerName) {
        PackageStatistics stats = (PackageStatistics) this.statistics.get(new PackageProviderKey(packageName, providerName));
        if (stats != null) {
            stats.stopRequesting();
        } else {
            Log.e(TAG, "Couldn't find package statistics when removing location request.");
        }
    }
}

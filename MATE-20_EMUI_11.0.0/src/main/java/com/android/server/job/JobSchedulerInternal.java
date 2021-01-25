package com.android.server.job;

import android.app.job.JobInfo;
import com.android.server.slice.SliceClientPermissions;
import java.util.List;

public interface JobSchedulerInternal {
    void addBackingUpUid(int i);

    long baseHeartbeatForApp(String str, int i, int i2);

    void cancelJobsForUid(int i, String str);

    void clearAllBackingUpUids();

    long currentHeartbeat();

    JobStorePersistStats getPersistStats();

    List<JobInfo> getSystemScheduledPendingJobs();

    long nextHeartbeatForBucket(int i);

    void noteJobStart(String str, int i);

    boolean proxyService(int i, List<String> list);

    void removeBackingUpUid(int i);

    void reportAppUsage(String str, int i);

    public static class JobStorePersistStats {
        public int countAllJobsLoaded = -1;
        public int countAllJobsSaved = -1;
        public int countSystemServerJobsLoaded = -1;
        public int countSystemServerJobsSaved = -1;
        public int countSystemSyncManagerJobsLoaded = -1;
        public int countSystemSyncManagerJobsSaved = -1;

        public JobStorePersistStats() {
        }

        public JobStorePersistStats(JobStorePersistStats source) {
            this.countAllJobsLoaded = source.countAllJobsLoaded;
            this.countSystemServerJobsLoaded = source.countSystemServerJobsLoaded;
            this.countSystemSyncManagerJobsLoaded = source.countSystemSyncManagerJobsLoaded;
            this.countAllJobsSaved = source.countAllJobsSaved;
            this.countSystemServerJobsSaved = source.countSystemServerJobsSaved;
            this.countSystemSyncManagerJobsSaved = source.countSystemSyncManagerJobsSaved;
        }

        public String toString() {
            return "FirstLoad: " + this.countAllJobsLoaded + SliceClientPermissions.SliceAuthority.DELIMITER + this.countSystemServerJobsLoaded + SliceClientPermissions.SliceAuthority.DELIMITER + this.countSystemSyncManagerJobsLoaded + " LastSave: " + this.countAllJobsSaved + SliceClientPermissions.SliceAuthority.DELIMITER + this.countSystemServerJobsSaved + SliceClientPermissions.SliceAuthority.DELIMITER + this.countSystemSyncManagerJobsSaved;
        }
    }
}

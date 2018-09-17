package com.android.server.job;

import android.app.job.JobInfo;
import java.util.List;

public interface JobSchedulerInternal {
    void addBackingUpUid(int i);

    void clearAllBackingUpUids();

    List<JobInfo> getSystemScheduledPendingJobs();

    boolean proxyService(int i, List<String> list);

    void removeBackingUpUid(int i);
}

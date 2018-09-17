package com.android.server.job;

import android.app.job.JobInfo;
import java.util.List;

public interface JobSchedulerInternal {
    List<JobInfo> getSystemScheduledPendingJobs();
}

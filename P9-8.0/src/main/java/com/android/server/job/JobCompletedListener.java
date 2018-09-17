package com.android.server.job;

import com.android.server.job.controllers.JobStatus;

public interface JobCompletedListener {
    void onJobCompletedLocked(JobStatus jobStatus, boolean z);
}

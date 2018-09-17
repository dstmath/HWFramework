package android.app.job;

import java.util.List;

public abstract class JobScheduler {
    public static final int RESULT_FAILURE = 0;
    public static final int RESULT_SUCCESS = 1;

    public abstract void cancel(int i);

    public abstract void cancelAll();

    public abstract int enqueue(JobInfo jobInfo, JobWorkItem jobWorkItem);

    public abstract List<JobInfo> getAllPendingJobs();

    public abstract JobInfo getPendingJob(int i);

    public abstract int schedule(JobInfo jobInfo);

    public abstract int scheduleAsPackage(JobInfo jobInfo, String str, int i, String str2);
}

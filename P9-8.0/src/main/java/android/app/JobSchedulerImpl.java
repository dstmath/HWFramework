package android.app;

import android.app.job.IJobScheduler;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobWorkItem;
import android.os.RemoteException;
import java.util.List;

public class JobSchedulerImpl extends JobScheduler {
    IJobScheduler mBinder;

    JobSchedulerImpl(IJobScheduler binder) {
        this.mBinder = binder;
    }

    public int schedule(JobInfo job) {
        if (this.mBinder == null) {
            return 0;
        }
        try {
            return this.mBinder.schedule(job);
        } catch (RemoteException e) {
            return 0;
        }
    }

    public int enqueue(JobInfo job, JobWorkItem work) {
        try {
            return this.mBinder.enqueue(job, work);
        } catch (RemoteException e) {
            return 0;
        }
    }

    public int scheduleAsPackage(JobInfo job, String packageName, int userId, String tag) {
        try {
            return this.mBinder.scheduleAsPackage(job, packageName, userId, tag);
        } catch (RemoteException e) {
            return 0;
        }
    }

    public void cancel(int jobId) {
        try {
            this.mBinder.cancel(jobId);
        } catch (RemoteException e) {
        }
    }

    public void cancelAll() {
        try {
            this.mBinder.cancelAll();
        } catch (RemoteException e) {
        }
    }

    public List<JobInfo> getAllPendingJobs() {
        try {
            return this.mBinder.getAllPendingJobs();
        } catch (RemoteException e) {
            return null;
        }
    }

    public JobInfo getPendingJob(int jobId) {
        try {
            return this.mBinder.getPendingJob(jobId);
        } catch (RemoteException e) {
            return null;
        }
    }
}

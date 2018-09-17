package android.app.job;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public abstract class JobService extends Service {
    public static final String PERMISSION_BIND = "android.permission.BIND_JOB_SERVICE";
    private static final String TAG = "JobService";
    private JobServiceEngine mEngine;

    public abstract boolean onStartJob(JobParameters jobParameters);

    public abstract boolean onStopJob(JobParameters jobParameters);

    public final IBinder onBind(Intent intent) {
        if (this.mEngine == null) {
            this.mEngine = new JobServiceEngine(this) {
                public boolean onStartJob(JobParameters params) {
                    return JobService.this.onStartJob(params);
                }

                public boolean onStopJob(JobParameters params) {
                    return JobService.this.onStopJob(params);
                }
            };
        }
        return this.mEngine.getBinder();
    }

    public final void jobFinished(JobParameters params, boolean needsReschedule) {
        this.mEngine.jobFinished(params, needsReschedule);
    }
}

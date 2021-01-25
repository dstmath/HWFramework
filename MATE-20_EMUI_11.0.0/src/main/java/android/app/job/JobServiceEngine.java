package android.app.job;

import android.app.Service;
import android.app.job.IJobService;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import java.lang.ref.WeakReference;

public abstract class JobServiceEngine {
    private static final int CHINA_BETA = 3;
    private static final int MSG_EXECUTE_JOB = 0;
    private static final int MSG_JOB_FINISHED = 2;
    private static final int MSG_STOP_JOB = 1;
    private static final int OVERSEA_BETA = 5;
    private static final String TAG = "JobServiceEngine";
    private static final int USER_TYPE = SystemProperties.getInt("ro.logsystem.usertype", 1);
    private final IJobService mBinder = new JobInterface(this);
    JobHandler mHandler;

    public abstract boolean onStartJob(JobParameters jobParameters);

    public abstract boolean onStopJob(JobParameters jobParameters);

    static final class JobInterface extends IJobService.Stub {
        final WeakReference<JobServiceEngine> mService;

        JobInterface(JobServiceEngine service) {
            this.mService = new WeakReference<>(service);
        }

        @Override // android.app.job.IJobService
        public void startJob(JobParameters jobParams) throws RemoteException {
            JobServiceEngine service = this.mService.get();
            if (service != null) {
                Message.obtain(service.mHandler, 0, jobParams).sendToTarget();
            }
        }

        @Override // android.app.job.IJobService
        public void stopJob(JobParameters jobParams) throws RemoteException {
            JobServiceEngine service = this.mService.get();
            if (service != null) {
                Message.obtain(service.mHandler, 1, jobParams).sendToTarget();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logJobService(Message msg, JobParameters jobParameters) {
        int i = USER_TYPE;
        if ((i == 3 || i == 5) && jobParameters != null && msg != null) {
            Log.i(TAG, "JobService job message id:" + msg.what + " jobId:" + jobParameters.getJobId());
        }
    }

    class JobHandler extends Handler {
        JobHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            JobParameters params = (JobParameters) msg.obj;
            JobServiceEngine.this.logJobService(msg, params);
            int i = msg.what;
            if (i != 0) {
                boolean needsReschedule = true;
                if (i == 1) {
                    try {
                        ackStopMessage(params, JobServiceEngine.this.onStopJob(params));
                    } catch (Exception e) {
                        Log.e(JobServiceEngine.TAG, "Application unable to handle onStopJob.", e);
                        throw new RuntimeException(e);
                    }
                } else if (i != 2) {
                    Log.e(JobServiceEngine.TAG, "Unrecognised message received.");
                } else {
                    if (msg.arg2 != 1) {
                        needsReschedule = false;
                    }
                    IJobCallback callback = params.getCallback();
                    if (callback != null) {
                        try {
                            callback.jobFinished(params.getJobId(), needsReschedule);
                        } catch (RemoteException e2) {
                            Log.e(JobServiceEngine.TAG, "Error reporting job finish to system: binder has goneaway.");
                        }
                    } else {
                        Log.e(JobServiceEngine.TAG, "finishJob() called for a nonexistent job id.");
                    }
                }
            } else {
                try {
                    ackStartMessage(params, JobServiceEngine.this.onStartJob(params));
                } catch (Exception e3) {
                    Log.e(JobServiceEngine.TAG, "Error while executing job: " + params.getJobId());
                    throw new RuntimeException(e3);
                }
            }
        }

        private void ackStartMessage(JobParameters params, boolean workOngoing) {
            IJobCallback callback = params.getCallback();
            int jobId = params.getJobId();
            if (callback != null) {
                try {
                    callback.acknowledgeStartMessage(jobId, workOngoing);
                } catch (RemoteException e) {
                    Log.e(JobServiceEngine.TAG, "System unreachable for starting job.");
                }
            } else if (Log.isLoggable(JobServiceEngine.TAG, 3)) {
                Log.d(JobServiceEngine.TAG, "Attempting to ack a job that has already been processed.");
            }
        }

        private void ackStopMessage(JobParameters params, boolean reschedule) {
            IJobCallback callback = params.getCallback();
            int jobId = params.getJobId();
            if (callback != null) {
                try {
                    callback.acknowledgeStopMessage(jobId, reschedule);
                } catch (RemoteException e) {
                    Log.e(JobServiceEngine.TAG, "System unreachable for stopping job.");
                }
            } else if (Log.isLoggable(JobServiceEngine.TAG, 3)) {
                Log.d(JobServiceEngine.TAG, "Attempting to ack a job that has already been processed.");
            }
        }
    }

    public JobServiceEngine(Service service) {
        this.mHandler = new JobHandler(service.getMainLooper());
    }

    public final IBinder getBinder() {
        return this.mBinder.asBinder();
    }

    public void jobFinished(JobParameters params, boolean needsReschedule) {
        if (params != null) {
            Message m = Message.obtain(this.mHandler, 2, params);
            m.arg2 = needsReschedule ? 1 : 0;
            m.sendToTarget();
            return;
        }
        throw new NullPointerException("params");
    }
}

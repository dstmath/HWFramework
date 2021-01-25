package android.support.v4.app;

import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobServiceEngine;
import android.app.job.JobWorkItem;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class JobIntentService extends Service {
    static final boolean DEBUG = false;
    static final String TAG = "JobIntentService";
    static final HashMap<ComponentName, WorkEnqueuer> sClassWorkEnqueuer = new HashMap<>();
    static final Object sLock = new Object();
    final ArrayList<CompatWorkItem> mCompatQueue;
    WorkEnqueuer mCompatWorkEnqueuer;
    CommandProcessor mCurProcessor;
    boolean mDestroyed = DEBUG;
    boolean mInterruptIfStopped = DEBUG;
    CompatJobEngine mJobImpl;
    boolean mStopped = DEBUG;

    /* access modifiers changed from: package-private */
    public interface CompatJobEngine {
        IBinder compatGetBinder();

        GenericWorkItem dequeueWork();
    }

    /* access modifiers changed from: package-private */
    public interface GenericWorkItem {
        void complete();

        Intent getIntent();
    }

    /* access modifiers changed from: protected */
    public abstract void onHandleWork(@NonNull Intent intent);

    /* access modifiers changed from: package-private */
    public static abstract class WorkEnqueuer {
        final ComponentName mComponentName;
        boolean mHasJobId;
        int mJobId;

        /* access modifiers changed from: package-private */
        public abstract void enqueueWork(Intent intent);

        WorkEnqueuer(Context context, ComponentName cn) {
            this.mComponentName = cn;
        }

        /* access modifiers changed from: package-private */
        public void ensureJobId(int jobId) {
            if (!this.mHasJobId) {
                this.mHasJobId = true;
                this.mJobId = jobId;
            } else if (this.mJobId != jobId) {
                throw new IllegalArgumentException("Given job ID " + jobId + " is different than previous " + this.mJobId);
            }
        }

        public void serviceStartReceived() {
        }

        public void serviceProcessingStarted() {
        }

        public void serviceProcessingFinished() {
        }
    }

    /* access modifiers changed from: package-private */
    public static final class CompatWorkEnqueuer extends WorkEnqueuer {
        private final Context mContext;
        private final PowerManager.WakeLock mLaunchWakeLock;
        boolean mLaunchingService;
        private final PowerManager.WakeLock mRunWakeLock;
        boolean mServiceProcessing;

        CompatWorkEnqueuer(Context context, ComponentName cn) {
            super(context, cn);
            this.mContext = context.getApplicationContext();
            PowerManager pm = (PowerManager) context.getSystemService("power");
            this.mLaunchWakeLock = pm.newWakeLock(1, cn.getClassName() + ":launch");
            this.mLaunchWakeLock.setReferenceCounted(JobIntentService.DEBUG);
            this.mRunWakeLock = pm.newWakeLock(1, cn.getClassName() + ":run");
            this.mRunWakeLock.setReferenceCounted(JobIntentService.DEBUG);
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.app.JobIntentService.WorkEnqueuer
        public void enqueueWork(Intent work) {
            Intent intent = new Intent(work);
            intent.setComponent(this.mComponentName);
            if (this.mContext.startService(intent) != null) {
                synchronized (this) {
                    if (!this.mLaunchingService) {
                        this.mLaunchingService = true;
                        if (!this.mServiceProcessing) {
                            this.mLaunchWakeLock.acquire(60000);
                        }
                    }
                }
            }
        }

        @Override // android.support.v4.app.JobIntentService.WorkEnqueuer
        public void serviceStartReceived() {
            synchronized (this) {
                this.mLaunchingService = JobIntentService.DEBUG;
            }
        }

        @Override // android.support.v4.app.JobIntentService.WorkEnqueuer
        public void serviceProcessingStarted() {
            synchronized (this) {
                if (!this.mServiceProcessing) {
                    this.mServiceProcessing = true;
                    this.mRunWakeLock.acquire(600000);
                    this.mLaunchWakeLock.release();
                }
            }
        }

        @Override // android.support.v4.app.JobIntentService.WorkEnqueuer
        public void serviceProcessingFinished() {
            synchronized (this) {
                if (this.mServiceProcessing) {
                    if (this.mLaunchingService) {
                        this.mLaunchWakeLock.acquire(60000);
                    }
                    this.mServiceProcessing = JobIntentService.DEBUG;
                    this.mRunWakeLock.release();
                }
            }
        }
    }

    @RequiresApi(26)
    static final class JobServiceEngineImpl extends JobServiceEngine implements CompatJobEngine {
        static final boolean DEBUG = false;
        static final String TAG = "JobServiceEngineImpl";
        final Object mLock = new Object();
        JobParameters mParams;
        final JobIntentService mService;

        final class WrapperWorkItem implements GenericWorkItem {
            final JobWorkItem mJobWork;

            WrapperWorkItem(JobWorkItem jobWork) {
                this.mJobWork = jobWork;
            }

            @Override // android.support.v4.app.JobIntentService.GenericWorkItem
            public Intent getIntent() {
                return this.mJobWork.getIntent();
            }

            @Override // android.support.v4.app.JobIntentService.GenericWorkItem
            public void complete() {
                synchronized (JobServiceEngineImpl.this.mLock) {
                    if (JobServiceEngineImpl.this.mParams != null) {
                        JobServiceEngineImpl.this.mParams.completeWork(this.mJobWork);
                    }
                }
            }
        }

        JobServiceEngineImpl(JobIntentService service) {
            super(service);
            this.mService = service;
        }

        @Override // android.support.v4.app.JobIntentService.CompatJobEngine
        public IBinder compatGetBinder() {
            return getBinder();
        }

        @Override // android.app.job.JobServiceEngine
        public boolean onStartJob(JobParameters params) {
            this.mParams = params;
            this.mService.ensureProcessorRunningLocked(DEBUG);
            return true;
        }

        @Override // android.app.job.JobServiceEngine
        public boolean onStopJob(JobParameters params) {
            boolean result = this.mService.doStopCurrentWork();
            synchronized (this.mLock) {
                this.mParams = null;
            }
            return result;
        }

        @Override // android.support.v4.app.JobIntentService.CompatJobEngine
        public GenericWorkItem dequeueWork() {
            Throwable th;
            JobWorkItem work;
            synchronized (this.mLock) {
                try {
                    if (this.mParams == null) {
                        return null;
                    }
                    work = this.mParams.dequeueWork();
                    try {
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
            if (work == null) {
                return null;
            }
            work.getIntent().setExtrasClassLoader(this.mService.getClassLoader());
            return new WrapperWorkItem(work);
        }
    }

    /* access modifiers changed from: package-private */
    @RequiresApi(26)
    public static final class JobWorkEnqueuer extends WorkEnqueuer {
        private final JobInfo mJobInfo;
        private final JobScheduler mJobScheduler;

        JobWorkEnqueuer(Context context, ComponentName cn, int jobId) {
            super(context, cn);
            ensureJobId(jobId);
            this.mJobInfo = new JobInfo.Builder(jobId, this.mComponentName).setOverrideDeadline(0).build();
            this.mJobScheduler = (JobScheduler) context.getApplicationContext().getSystemService("jobscheduler");
        }

        /* access modifiers changed from: package-private */
        @Override // android.support.v4.app.JobIntentService.WorkEnqueuer
        public void enqueueWork(Intent work) {
            this.mJobScheduler.enqueue(this.mJobInfo, new JobWorkItem(work));
        }
    }

    /* access modifiers changed from: package-private */
    public final class CompatWorkItem implements GenericWorkItem {
        final Intent mIntent;
        final int mStartId;

        CompatWorkItem(Intent intent, int startId) {
            this.mIntent = intent;
            this.mStartId = startId;
        }

        @Override // android.support.v4.app.JobIntentService.GenericWorkItem
        public Intent getIntent() {
            return this.mIntent;
        }

        @Override // android.support.v4.app.JobIntentService.GenericWorkItem
        public void complete() {
            JobIntentService.this.stopSelf(this.mStartId);
        }
    }

    /* access modifiers changed from: package-private */
    public final class CommandProcessor extends AsyncTask<Void, Void, Void> {
        CommandProcessor() {
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Void... params) {
            while (true) {
                GenericWorkItem work = JobIntentService.this.dequeueWork();
                if (work == null) {
                    return null;
                }
                JobIntentService.this.onHandleWork(work.getIntent());
                work.complete();
            }
        }

        /* access modifiers changed from: protected */
        public void onCancelled(Void aVoid) {
            JobIntentService.this.processorFinished();
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Void aVoid) {
            JobIntentService.this.processorFinished();
        }
    }

    public JobIntentService() {
        if (Build.VERSION.SDK_INT >= 26) {
            this.mCompatQueue = null;
        } else {
            this.mCompatQueue = new ArrayList<>();
        }
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            this.mJobImpl = new JobServiceEngineImpl(this);
            this.mCompatWorkEnqueuer = null;
            return;
        }
        this.mJobImpl = null;
        this.mCompatWorkEnqueuer = getWorkEnqueuer(this, new ComponentName(this, getClass()), DEBUG, 0);
    }

    @Override // android.app.Service
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (this.mCompatQueue == null) {
            return 2;
        }
        this.mCompatWorkEnqueuer.serviceStartReceived();
        synchronized (this.mCompatQueue) {
            this.mCompatQueue.add(new CompatWorkItem(intent != null ? intent : new Intent(), startId));
            ensureProcessorRunningLocked(true);
        }
        return 3;
    }

    @Override // android.app.Service
    public IBinder onBind(@NonNull Intent intent) {
        if (this.mJobImpl != null) {
            return this.mJobImpl.compatGetBinder();
        }
        return null;
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
        if (this.mCompatQueue != null) {
            synchronized (this.mCompatQueue) {
                this.mDestroyed = true;
                this.mCompatWorkEnqueuer.serviceProcessingFinished();
            }
        }
    }

    public static void enqueueWork(@NonNull Context context, @NonNull Class cls, int jobId, @NonNull Intent work) {
        enqueueWork(context, new ComponentName(context, cls), jobId, work);
    }

    public static void enqueueWork(@NonNull Context context, @NonNull ComponentName component, int jobId, @NonNull Intent work) {
        if (work != null) {
            synchronized (sLock) {
                WorkEnqueuer we = getWorkEnqueuer(context, component, true, jobId);
                we.ensureJobId(jobId);
                we.enqueueWork(work);
            }
            return;
        }
        throw new IllegalArgumentException("work must not be null");
    }

    static WorkEnqueuer getWorkEnqueuer(Context context, ComponentName cn, boolean hasJobId, int jobId) {
        WorkEnqueuer we = sClassWorkEnqueuer.get(cn);
        if (we == null) {
            if (Build.VERSION.SDK_INT < 26) {
                we = new CompatWorkEnqueuer(context, cn);
            } else if (hasJobId) {
                we = new JobWorkEnqueuer(context, cn, jobId);
            } else {
                throw new IllegalArgumentException("Can't be here without a job id");
            }
            sClassWorkEnqueuer.put(cn, we);
        }
        return we;
    }

    public void setInterruptIfStopped(boolean interruptIfStopped) {
        this.mInterruptIfStopped = interruptIfStopped;
    }

    public boolean isStopped() {
        return this.mStopped;
    }

    public boolean onStopCurrentWork() {
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean doStopCurrentWork() {
        if (this.mCurProcessor != null) {
            this.mCurProcessor.cancel(this.mInterruptIfStopped);
        }
        this.mStopped = true;
        return onStopCurrentWork();
    }

    /* access modifiers changed from: package-private */
    public void ensureProcessorRunningLocked(boolean reportStarted) {
        if (this.mCurProcessor == null) {
            this.mCurProcessor = new CommandProcessor();
            if (this.mCompatWorkEnqueuer != null && reportStarted) {
                this.mCompatWorkEnqueuer.serviceProcessingStarted();
            }
            this.mCurProcessor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        }
    }

    /* access modifiers changed from: package-private */
    public void processorFinished() {
        if (this.mCompatQueue != null) {
            synchronized (this.mCompatQueue) {
                this.mCurProcessor = null;
                if (this.mCompatQueue != null && this.mCompatQueue.size() > 0) {
                    ensureProcessorRunningLocked(DEBUG);
                } else if (!this.mDestroyed) {
                    this.mCompatWorkEnqueuer.serviceProcessingFinished();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public GenericWorkItem dequeueWork() {
        if (this.mJobImpl != null) {
            return this.mJobImpl.dequeueWork();
        }
        synchronized (this.mCompatQueue) {
            if (this.mCompatQueue.size() <= 0) {
                return null;
            }
            return this.mCompatQueue.remove(0);
        }
    }
}

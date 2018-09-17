package android.support.v4.app;

import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobInfo.Builder;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobServiceEngine;
import android.app.job.JobWorkItem;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.os.BuildCompat;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class JobIntentService extends Service {
    static final boolean DEBUG = false;
    static final String TAG = "JobIntentService";
    static final HashMap<Class, WorkEnqueuer> sClassWorkEnqueuer = new HashMap();
    static final Object sLock = new Object();
    final ArrayList<CompatWorkItem> mCompatQueue;
    WorkEnqueuer mCompatWorkEnqueuer;
    CommandProcessor mCurProcessor;
    CompatJobEngine mJobImpl;

    final class CommandProcessor extends AsyncTask<Void, Void, Void> {
        CommandProcessor() {
        }

        protected Void doInBackground(Void... params) {
            while (true) {
                GenericWorkItem work = JobIntentService.this.dequeueWork();
                if (work == null) {
                    return null;
                }
                JobIntentService.this.onHandleWork(work.getIntent());
                work.complete();
            }
        }

        protected void onPostExecute(Void aVoid) {
            if (JobIntentService.this.mCompatQueue != null) {
                synchronized (JobIntentService.this.mCompatQueue) {
                    JobIntentService.this.mCurProcessor = null;
                    JobIntentService.this.checkForMoreCompatWorkLocked();
                }
            }
        }
    }

    interface CompatJobEngine {
        IBinder compatGetBinder();

        GenericWorkItem dequeueWork();
    }

    static abstract class WorkEnqueuer {
        final ComponentName mComponentName;
        boolean mHasJobId;
        int mJobId;

        abstract void enqueueWork(Intent intent);

        WorkEnqueuer(Context context, Class cls) {
            this.mComponentName = new ComponentName(context, cls);
        }

        void ensureJobId(int jobId) {
            if (!this.mHasJobId) {
                this.mHasJobId = true;
                this.mJobId = jobId;
            } else if (this.mJobId != jobId) {
                throw new IllegalArgumentException("Given job ID " + jobId + " is different than previous " + this.mJobId);
            }
        }

        public void serviceCreated() {
        }

        public void serviceStartReceived() {
        }

        public void serviceDestroyed() {
        }
    }

    static final class CompatWorkEnqueuer extends WorkEnqueuer {
        private final Context mContext;
        private final WakeLock mLaunchWakeLock;
        boolean mLaunchingService;
        private final WakeLock mRunWakeLock;
        boolean mServiceRunning;

        CompatWorkEnqueuer(Context context, Class cls) {
            super(context, cls);
            this.mContext = context.getApplicationContext();
            PowerManager pm = (PowerManager) context.getSystemService("power");
            this.mLaunchWakeLock = pm.newWakeLock(1, cls.getName());
            this.mLaunchWakeLock.setReferenceCounted(JobIntentService.DEBUG);
            this.mRunWakeLock = pm.newWakeLock(1, cls.getName());
            this.mRunWakeLock.setReferenceCounted(JobIntentService.DEBUG);
        }

        void enqueueWork(Intent work) {
            Intent intent = new Intent(work);
            intent.setComponent(this.mComponentName);
            if (this.mContext.startService(intent) != null) {
                synchronized (this) {
                    if (!this.mLaunchingService) {
                        this.mLaunchingService = true;
                        if (!this.mServiceRunning) {
                            this.mLaunchWakeLock.acquire(60000);
                        }
                    }
                }
            }
        }

        public void serviceCreated() {
            synchronized (this) {
                if (!this.mServiceRunning) {
                    this.mServiceRunning = true;
                    this.mRunWakeLock.acquire();
                    this.mLaunchWakeLock.release();
                }
            }
        }

        public void serviceStartReceived() {
            synchronized (this) {
                this.mLaunchingService = JobIntentService.DEBUG;
            }
        }

        public void serviceDestroyed() {
            synchronized (this) {
                if (this.mLaunchingService) {
                    this.mLaunchWakeLock.acquire(60000);
                }
                this.mServiceRunning = JobIntentService.DEBUG;
                this.mRunWakeLock.release();
            }
        }
    }

    interface GenericWorkItem {
        void complete();

        Intent getIntent();
    }

    final class CompatWorkItem implements GenericWorkItem {
        final Intent mIntent;
        final int mStartId;

        CompatWorkItem(Intent intent, int startId) {
            this.mIntent = intent;
            this.mStartId = startId;
        }

        public Intent getIntent() {
            return this.mIntent;
        }

        public void complete() {
            JobIntentService.this.stopSelf(this.mStartId);
        }
    }

    @RequiresApi(26)
    static final class JobServiceEngineImpl extends JobServiceEngine implements CompatJobEngine {
        static final boolean DEBUG = false;
        static final String TAG = "JobServiceEngineImpl";
        JobParameters mParams;
        final JobIntentService mService;

        final class WrapperWorkItem implements GenericWorkItem {
            final JobWorkItem mJobWork;

            WrapperWorkItem(JobWorkItem jobWork) {
                this.mJobWork = jobWork;
            }

            public Intent getIntent() {
                return this.mJobWork.getIntent();
            }

            public void complete() {
                JobServiceEngineImpl.this.mParams.completeWork(this.mJobWork);
            }
        }

        JobServiceEngineImpl(JobIntentService service) {
            super(service);
            this.mService = service;
        }

        public IBinder compatGetBinder() {
            return getBinder();
        }

        public boolean onStartJob(JobParameters params) {
            this.mParams = params;
            this.mService.ensureProcessorRunningLocked();
            return true;
        }

        public boolean onStopJob(JobParameters params) {
            return this.mService.onStopCurrentWork();
        }

        public GenericWorkItem dequeueWork() {
            JobWorkItem work = this.mParams.dequeueWork();
            if (work != null) {
                return new WrapperWorkItem(work);
            }
            return null;
        }
    }

    @RequiresApi(26)
    static final class JobWorkEnqueuer extends WorkEnqueuer {
        private final JobInfo mJobInfo;
        private final JobScheduler mJobScheduler;

        JobWorkEnqueuer(Context context, Class cls, int jobId) {
            super(context, cls);
            ensureJobId(jobId);
            this.mJobInfo = new Builder(jobId, this.mComponentName).setOverrideDeadline(0).build();
            this.mJobScheduler = (JobScheduler) context.getApplicationContext().getSystemService("jobscheduler");
        }

        void enqueueWork(Intent work) {
            this.mJobScheduler.enqueue(this.mJobInfo, new JobWorkItem(work));
        }
    }

    protected abstract void onHandleWork(@NonNull Intent intent);

    public JobIntentService() {
        if (VERSION.SDK_INT >= 26) {
            this.mCompatQueue = null;
        } else {
            this.mCompatQueue = new ArrayList();
        }
    }

    public void onCreate() {
        super.onCreate();
        if (VERSION.SDK_INT >= 26) {
            this.mJobImpl = new JobServiceEngineImpl(this);
            this.mCompatWorkEnqueuer = null;
            return;
        }
        this.mJobImpl = null;
        this.mCompatWorkEnqueuer = getWorkEnqueuer(this, getClass(), DEBUG, 0);
        this.mCompatWorkEnqueuer.serviceCreated();
    }

    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (this.mCompatQueue == null) {
            return 2;
        }
        this.mCompatWorkEnqueuer.serviceStartReceived();
        synchronized (this.mCompatQueue) {
            ArrayList arrayList = this.mCompatQueue;
            if (intent == null) {
                intent = new Intent();
            }
            arrayList.add(new CompatWorkItem(intent, startId));
            ensureProcessorRunningLocked();
        }
        return 3;
    }

    public IBinder onBind(@NonNull Intent intent) {
        if (this.mJobImpl != null) {
            return this.mJobImpl.compatGetBinder();
        }
        return null;
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mCompatWorkEnqueuer != null) {
            this.mCompatWorkEnqueuer.serviceDestroyed();
        }
    }

    public static void enqueueWork(@NonNull Context context, @NonNull Class cls, int jobId, @NonNull Intent work) {
        if (work == null) {
            throw new IllegalArgumentException("work must not be null");
        }
        synchronized (sLock) {
            WorkEnqueuer we = getWorkEnqueuer(context, cls, true, jobId);
            we.ensureJobId(jobId);
            we.enqueueWork(work);
        }
    }

    static WorkEnqueuer getWorkEnqueuer(Context context, Class cls, boolean hasJobId, int jobId) {
        WorkEnqueuer we = (WorkEnqueuer) sClassWorkEnqueuer.get(cls);
        if (we == null) {
            if (!BuildCompat.isAtLeastO()) {
                we = new CompatWorkEnqueuer(context, cls);
            } else if (hasJobId) {
                we = new JobWorkEnqueuer(context, cls, jobId);
            } else {
                throw new IllegalArgumentException("Can't be here without a job id");
            }
            sClassWorkEnqueuer.put(cls, we);
        }
        return we;
    }

    public boolean onStopCurrentWork() {
        return true;
    }

    void ensureProcessorRunningLocked() {
        if (this.mCurProcessor == null) {
            this.mCurProcessor = new CommandProcessor();
            this.mCurProcessor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        }
    }

    void checkForMoreCompatWorkLocked() {
        if (this.mCompatQueue != null && this.mCompatQueue.size() > 0) {
            ensureProcessorRunningLocked();
        }
    }

    GenericWorkItem dequeueWork() {
        if (this.mJobImpl != null) {
            return this.mJobImpl.dequeueWork();
        }
        synchronized (this.mCompatQueue) {
            if (this.mCompatQueue.size() > 0) {
                GenericWorkItem genericWorkItem = (GenericWorkItem) this.mCompatQueue.remove(0);
                return genericWorkItem;
            }
            return null;
        }
    }
}

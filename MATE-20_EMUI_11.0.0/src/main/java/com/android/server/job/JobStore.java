package com.android.server.job;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.app.job.JobInfo;
import android.content.ComponentName;
import android.content.Context;
import android.net.NetworkRequest;
import android.os.Environment;
import android.os.Handler;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.format.DateUtils;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.BitUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.server.IoThread;
import com.android.server.LocalServices;
import com.android.server.content.SyncJobService;
import com.android.server.job.JobSchedulerInternal;
import com.android.server.job.JobStore;
import com.android.server.job.controllers.JobStatus;
import com.android.server.net.watchlist.WatchlistLoggingHandler;
import com.android.server.pm.PackageManagerService;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class JobStore {
    private static final boolean DEBUG = JobSchedulerService.DEBUG;
    private static final int JOBS_FILE_VERSION = 0;
    private static final long JOB_PERSIST_DELAY = 2000;
    private static final String TAG = "JobStore";
    private static final String XML_TAG_EXTRAS = "extras";
    private static final String XML_TAG_ONEOFF = "one-off";
    private static final String XML_TAG_PARAMS_CONSTRAINTS = "constraints";
    private static final String XML_TAG_PERIODIC = "periodic";
    private static JobStore sSingleton;
    private static final Object sSingletonLock = new Object();
    final Context mContext;
    private final Handler mIoHandler = IoThread.getHandler();
    final JobSet mJobSet;
    private final AtomicFile mJobsFile;
    final Object mLock;
    private JobSchedulerInternal.JobStorePersistStats mPersistInfo = new JobSchedulerInternal.JobStorePersistStats();
    private boolean mRtcGood;
    private boolean mWriteInProgress;
    private final Runnable mWriteRunnable = new Runnable() {
        /* class com.android.server.job.JobStore.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            long startElapsed = JobSchedulerService.sElapsedRealtimeClock.millis();
            List<JobStatus> storeCopy = new ArrayList<>();
            synchronized (JobStore.this.mWriteScheduleLock) {
                JobStore.this.mWriteScheduled = false;
            }
            synchronized (JobStore.this.mLock) {
                JobStore.this.mJobSet.forEachJob((Predicate<JobStatus>) null, new Consumer(storeCopy) {
                    /* class com.android.server.job.$$Lambda$JobStore$1$Wgepg1oHZp0Q01q1baIVZKWujU */
                    private final /* synthetic */ List f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        JobStore.AnonymousClass1.lambda$run$0(this.f$0, (JobStatus) obj);
                    }
                });
            }
            writeJobsMapImpl(storeCopy);
            if (JobStore.DEBUG) {
                Slog.v(JobStore.TAG, "Finished writing, took " + (JobSchedulerService.sElapsedRealtimeClock.millis() - startElapsed) + "ms");
            }
            synchronized (JobStore.this.mWriteScheduleLock) {
                JobStore.this.mWriteInProgress = false;
                JobStore.this.mWriteScheduleLock.notifyAll();
            }
        }

        static /* synthetic */ void lambda$run$0(List storeCopy, JobStatus job) {
            if (job.isPersisted()) {
                storeCopy.add(new JobStatus(job));
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:28:0x00d7 A[Catch:{ IOException -> 0x00dd, XmlPullParserException -> 0x00ce, all -> 0x00ca, all -> 0x0105 }] */
        /* JADX WARNING: Removed duplicated region for block: B:33:0x00e6 A[Catch:{ IOException -> 0x00dd, XmlPullParserException -> 0x00ce, all -> 0x00ca, all -> 0x0105 }] */
        private void writeJobsMapImpl(List<JobStatus> jobList) {
            Throwable th;
            IOException e;
            XmlPullParserException e2;
            int numJobs = 0;
            int numSystemJobs = 0;
            int numSyncJobs = 0;
            try {
                long startTime = SystemClock.uptimeMillis();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(baos, StandardCharsets.UTF_8.name());
                out.startDocument(null, true);
                out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                out.startTag(null, "job-info");
                out.attribute(null, "version", Integer.toString(0));
                for (int i = 0; i < jobList.size(); i++) {
                    try {
                        JobStatus jobStatus = jobList.get(i);
                        if (JobStore.DEBUG) {
                            Slog.d(JobStore.TAG, "Saving job " + jobStatus.getJobId());
                        }
                        out.startTag(null, "job");
                        addAttributesToJobTag(out, jobStatus);
                        writeConstraintsToXml(out, jobStatus);
                        writeExecutionCriteriaToXml(out, jobStatus);
                        writeBundleToXml(jobStatus.getJob().getExtras(), out);
                        out.endTag(null, "job");
                        numJobs++;
                        if (jobStatus.getUid() == 1000) {
                            numSystemJobs++;
                            if (JobStore.isSyncJob(jobStatus)) {
                                numSyncJobs++;
                            }
                        }
                    } catch (IOException e3) {
                        e = e3;
                        if (JobStore.DEBUG) {
                            Slog.v(JobStore.TAG, "Error writing out job data.", e);
                        }
                        JobStore.this.mPersistInfo.countAllJobsSaved = numJobs;
                        JobStore.this.mPersistInfo.countSystemServerJobsSaved = numSystemJobs;
                        JobStore.this.mPersistInfo.countSystemSyncManagerJobsSaved = numSyncJobs;
                    } catch (XmlPullParserException e4) {
                        e2 = e4;
                        if (JobStore.DEBUG) {
                            Slog.d(JobStore.TAG, "Error persisting bundle.", e2);
                        }
                        JobStore.this.mPersistInfo.countAllJobsSaved = numJobs;
                        JobStore.this.mPersistInfo.countSystemServerJobsSaved = numSystemJobs;
                        JobStore.this.mPersistInfo.countSystemSyncManagerJobsSaved = numSyncJobs;
                    }
                }
                out.endTag(null, "job-info");
                out.endDocument();
                FileOutputStream fos = JobStore.this.mJobsFile.startWrite(startTime);
                fos.write(baos.toByteArray());
                JobStore.this.mJobsFile.finishWrite(fos);
            } catch (IOException e5) {
                e = e5;
                if (JobStore.DEBUG) {
                }
                JobStore.this.mPersistInfo.countAllJobsSaved = numJobs;
                JobStore.this.mPersistInfo.countSystemServerJobsSaved = numSystemJobs;
                JobStore.this.mPersistInfo.countSystemSyncManagerJobsSaved = numSyncJobs;
            } catch (XmlPullParserException e6) {
                e2 = e6;
                if (JobStore.DEBUG) {
                }
                JobStore.this.mPersistInfo.countAllJobsSaved = numJobs;
                JobStore.this.mPersistInfo.countSystemServerJobsSaved = numSystemJobs;
                JobStore.this.mPersistInfo.countSystemSyncManagerJobsSaved = numSyncJobs;
            } catch (Throwable th2) {
                th = th2;
                JobStore.this.mPersistInfo.countAllJobsSaved = numJobs;
                JobStore.this.mPersistInfo.countSystemServerJobsSaved = numSystemJobs;
                JobStore.this.mPersistInfo.countSystemSyncManagerJobsSaved = numSyncJobs;
                throw th;
            }
            JobStore.this.mPersistInfo.countAllJobsSaved = numJobs;
            JobStore.this.mPersistInfo.countSystemServerJobsSaved = numSystemJobs;
            JobStore.this.mPersistInfo.countSystemSyncManagerJobsSaved = numSyncJobs;
        }

        private void addAttributesToJobTag(XmlSerializer out, JobStatus jobStatus) {
            out.attribute(null, "jobid", Integer.toString(jobStatus.getJobId()));
            out.attribute(null, "package", jobStatus.getServiceComponent().getPackageName());
            out.attribute(null, "class", jobStatus.getServiceComponent().getClassName());
            if (jobStatus.getSourcePackageName() != null) {
                out.attribute(null, "sourcePackageName", jobStatus.getSourcePackageName());
            }
            if (jobStatus.getSourceTag() != null) {
                out.attribute(null, "sourceTag", jobStatus.getSourceTag());
            }
            out.attribute(null, "sourceUserId", String.valueOf(jobStatus.getSourceUserId()));
            out.attribute(null, WatchlistLoggingHandler.WatchlistEventKeys.UID, Integer.toString(jobStatus.getUid()));
            out.attribute(null, "priority", String.valueOf(jobStatus.getPriority()));
            out.attribute(null, "flags", String.valueOf(jobStatus.getFlags()));
            if (jobStatus.getInternalFlags() != 0) {
                out.attribute(null, "internalFlags", String.valueOf(jobStatus.getInternalFlags()));
            }
            out.attribute(null, "lastSuccessfulRunTime", String.valueOf(jobStatus.getLastSuccessfulRunTime()));
            out.attribute(null, "lastFailedRunTime", String.valueOf(jobStatus.getLastFailedRunTime()));
        }

        private void writeBundleToXml(PersistableBundle extras, XmlSerializer out) {
            out.startTag(null, JobStore.XML_TAG_EXTRAS);
            deepCopyBundle(extras, 10).saveToXml(out);
            out.endTag(null, JobStore.XML_TAG_EXTRAS);
        }

        private PersistableBundle deepCopyBundle(PersistableBundle bundle, int maxDepth) {
            if (maxDepth <= 0) {
                return null;
            }
            PersistableBundle copy = (PersistableBundle) bundle.clone();
            for (String key : bundle.keySet()) {
                Object o = copy.get(key);
                if (o instanceof PersistableBundle) {
                    copy.putPersistableBundle(key, deepCopyBundle((PersistableBundle) o, maxDepth - 1));
                }
            }
            return copy;
        }

        private void writeConstraintsToXml(XmlSerializer out, JobStatus jobStatus) {
            out.startTag(null, JobStore.XML_TAG_PARAMS_CONSTRAINTS);
            if (jobStatus.hasConnectivityConstraint()) {
                NetworkRequest network = jobStatus.getJob().getRequiredNetwork();
                out.attribute(null, "net-capabilities", Long.toString(BitUtils.packBits(network.networkCapabilities.getCapabilities())));
                out.attribute(null, "net-unwanted-capabilities", Long.toString(BitUtils.packBits(network.networkCapabilities.getUnwantedCapabilities())));
                out.attribute(null, "net-transport-types", Long.toString(BitUtils.packBits(network.networkCapabilities.getTransportTypes())));
            }
            if (jobStatus.hasIdleConstraint()) {
                out.attribute(null, "idle", Boolean.toString(true));
            }
            if (jobStatus.hasChargingConstraint()) {
                out.attribute(null, "charging", Boolean.toString(true));
            }
            if (jobStatus.hasBatteryNotLowConstraint()) {
                out.attribute(null, "battery-not-low", Boolean.toString(true));
            }
            if (jobStatus.hasStorageNotLowConstraint()) {
                out.attribute(null, "storage-not-low", Boolean.toString(true));
            }
            out.endTag(null, JobStore.XML_TAG_PARAMS_CONSTRAINTS);
        }

        private void writeExecutionCriteriaToXml(XmlSerializer out, JobStatus jobStatus) {
            long delayWallclock;
            long deadlineWallclock;
            JobInfo job = jobStatus.getJob();
            if (jobStatus.getJob().isPeriodic()) {
                out.startTag(null, JobStore.XML_TAG_PERIODIC);
                out.attribute(null, "period", Long.toString(job.getIntervalMillis()));
                out.attribute(null, "flex", Long.toString(job.getFlexMillis()));
            } else {
                out.startTag(null, JobStore.XML_TAG_ONEOFF);
            }
            Pair<Long, Long> utcJobTimes = jobStatus.getPersistedUtcTimes();
            if (JobStore.DEBUG && utcJobTimes != null) {
                Slog.i(JobStore.TAG, "storing original UTC timestamps for " + jobStatus);
            }
            long nowRTC = JobSchedulerService.sSystemClock.millis();
            long nowElapsed = JobSchedulerService.sElapsedRealtimeClock.millis();
            if (jobStatus.hasDeadlineConstraint()) {
                if (utcJobTimes == null) {
                    deadlineWallclock = (jobStatus.getLatestRunTimeElapsed() - nowElapsed) + nowRTC;
                } else {
                    deadlineWallclock = ((Long) utcJobTimes.second).longValue();
                }
                out.attribute(null, "deadline", Long.toString(deadlineWallclock));
            }
            if (jobStatus.hasTimingDelayConstraint()) {
                if (utcJobTimes == null) {
                    delayWallclock = (jobStatus.getEarliestRunTime() - nowElapsed) + nowRTC;
                } else {
                    delayWallclock = ((Long) utcJobTimes.first).longValue();
                }
                out.attribute(null, "delay", Long.toString(delayWallclock));
            }
            if (!(jobStatus.getJob().getInitialBackoffMillis() == 30000 && jobStatus.getJob().getBackoffPolicy() == 1)) {
                out.attribute(null, "backoff-policy", Integer.toString(job.getBackoffPolicy()));
                out.attribute(null, "initial-backoff", Long.toString(job.getInitialBackoffMillis()));
            }
            if (job.isPeriodic()) {
                out.endTag(null, JobStore.XML_TAG_PERIODIC);
            } else {
                out.endTag(null, JobStore.XML_TAG_ONEOFF);
            }
        }
    };
    final Object mWriteScheduleLock;
    private boolean mWriteScheduled;
    private final long mXmlTimestamp;

    static JobStore initAndGet(JobSchedulerService jobManagerService) {
        JobStore jobStore;
        synchronized (sSingletonLock) {
            if (sSingleton == null) {
                sSingleton = new JobStore(jobManagerService.getContext(), jobManagerService.getLock(), Environment.getDataDirectory());
            }
            jobStore = sSingleton;
        }
        return jobStore;
    }

    public static JobStore initAndGetForTesting(Context context, File dataDir) {
        JobStore jobStoreUnderTest = new JobStore(context, new Object(), dataDir);
        jobStoreUnderTest.clear();
        return jobStoreUnderTest;
    }

    private JobStore(Context context, Object lock, File dataDir) {
        this.mLock = lock;
        this.mWriteScheduleLock = new Object();
        this.mContext = context;
        File jobDir = new File(new File(dataDir, "system"), "job");
        jobDir.mkdirs();
        this.mJobsFile = new AtomicFile(new File(jobDir, "jobs.xml"), "jobs");
        this.mJobSet = new JobSet();
        this.mXmlTimestamp = this.mJobsFile.getLastModifiedTime();
        this.mRtcGood = JobSchedulerService.sSystemClock.millis() > this.mXmlTimestamp;
        readJobMapFromDisk(this.mJobSet, this.mRtcGood);
    }

    public boolean jobTimesInflatedValid() {
        return this.mRtcGood;
    }

    public boolean clockNowValidToInflate(long now) {
        return now >= this.mXmlTimestamp;
    }

    public void getRtcCorrectedJobsLocked(ArrayList<JobStatus> toAdd, ArrayList<JobStatus> toRemove) {
        forEachJob(new Consumer(JobSchedulerService.sElapsedRealtimeClock.millis(), ActivityManager.getService(), toAdd, toRemove) {
            /* class com.android.server.job.$$Lambda$JobStore$aRnUP_cO7r3CFj_LAjeNjVBP4 */
            private final /* synthetic */ long f$0;
            private final /* synthetic */ IActivityManager f$1;
            private final /* synthetic */ ArrayList f$2;
            private final /* synthetic */ ArrayList f$3;

            {
                this.f$0 = r1;
                this.f$1 = r3;
                this.f$2 = r4;
                this.f$3 = r5;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                JobStore.lambda$getRtcCorrectedJobsLocked$0(this.f$0, this.f$1, this.f$2, this.f$3, (JobStatus) obj);
            }
        });
    }

    static /* synthetic */ void lambda$getRtcCorrectedJobsLocked$0(long elapsedNow, IActivityManager am, ArrayList toAdd, ArrayList toRemove, JobStatus job) {
        Pair<Long, Long> utcTimes = job.getPersistedUtcTimes();
        if (utcTimes != null) {
            Pair<Long, Long> elapsedRuntimes = convertRtcBoundsToElapsed(utcTimes, elapsedNow);
            JobStatus newJob = new JobStatus(job, job.getBaseHeartbeat(), ((Long) elapsedRuntimes.first).longValue(), ((Long) elapsedRuntimes.second).longValue(), 0, job.getLastSuccessfulRunTime(), job.getLastFailedRunTime());
            newJob.prepareLocked(am);
            toAdd.add(newJob);
            toRemove.add(job);
        }
    }

    public boolean add(JobStatus jobStatus) {
        boolean replaced = this.mJobSet.remove(jobStatus);
        this.mJobSet.add(jobStatus);
        if (jobStatus.isPersisted()) {
            maybeWriteStatusToDiskAsync();
        }
        if (DEBUG) {
            Slog.d(TAG, "Added job status to store: " + jobStatus);
        }
        return replaced;
    }

    public boolean containsJob(JobStatus jobStatus) {
        return this.mJobSet.contains(jobStatus);
    }

    public int size() {
        return this.mJobSet.size();
    }

    public JobSchedulerInternal.JobStorePersistStats getPersistStats() {
        return this.mPersistInfo;
    }

    public int countJobsForUid(int uid) {
        return this.mJobSet.countJobsForUid(uid);
    }

    public boolean remove(JobStatus jobStatus, boolean writeBack) {
        boolean removed = this.mJobSet.remove(jobStatus);
        if (removed) {
            if (writeBack && jobStatus.isPersisted()) {
                maybeWriteStatusToDiskAsync();
            }
            return removed;
        } else if (!DEBUG) {
            return false;
        } else {
            Slog.d(TAG, "Couldn't remove job: didn't exist: " + jobStatus);
            return false;
        }
    }

    public void removeJobsOfNonUsers(int[] whitelist) {
        this.mJobSet.removeJobsOfNonUsers(whitelist);
    }

    public void clear() {
        this.mJobSet.clear();
        maybeWriteStatusToDiskAsync();
    }

    public List<JobStatus> getJobsByUser(int userHandle) {
        return this.mJobSet.getJobsByUser(userHandle);
    }

    public List<JobStatus> getJobsByUid(int uid) {
        return this.mJobSet.getJobsByUid(uid);
    }

    public JobStatus getJobByUidAndJobId(int uid, int jobId) {
        return this.mJobSet.get(uid, jobId);
    }

    public void forEachJob(Consumer<JobStatus> functor) {
        this.mJobSet.forEachJob((Predicate<JobStatus>) null, functor);
    }

    public void forEachJob(Predicate<JobStatus> filterPredicate, Consumer<JobStatus> functor) {
        this.mJobSet.forEachJob(filterPredicate, functor);
    }

    public void forEachJob(int uid, Consumer<JobStatus> functor) {
        this.mJobSet.forEachJob(uid, functor);
    }

    public void forEachJobForSourceUid(int sourceUid, Consumer<JobStatus> functor) {
        this.mJobSet.forEachJobForSourceUid(sourceUid, functor);
    }

    private void maybeWriteStatusToDiskAsync() {
        synchronized (this.mWriteScheduleLock) {
            if (!this.mWriteScheduled) {
                if (DEBUG) {
                    Slog.v(TAG, "Scheduling persist of jobs to disk.");
                }
                this.mIoHandler.postDelayed(this.mWriteRunnable, JOB_PERSIST_DELAY);
                this.mWriteInProgress = true;
                this.mWriteScheduled = true;
            }
        }
    }

    public void readJobMapFromDisk(JobSet jobSet, boolean rtcGood) {
        new ReadJobMapFromDiskRunnable(jobSet, rtcGood).run();
    }

    public boolean waitForWriteToCompleteForTesting(long maxWaitMillis) {
        long start = SystemClock.uptimeMillis();
        long end = start + maxWaitMillis;
        synchronized (this.mWriteScheduleLock) {
            while (this.mWriteInProgress) {
                long now = SystemClock.uptimeMillis();
                if (now >= end) {
                    return false;
                }
                try {
                    this.mWriteScheduleLock.wait((now - start) + maxWaitMillis);
                } catch (InterruptedException e) {
                }
            }
            return true;
        }
    }

    public static Pair<Long, Long> convertRtcBoundsToElapsed(Pair<Long, Long> rtcTimes, long nowElapsed) {
        long earliest;
        long nowWallclock = JobSchedulerService.sSystemClock.millis();
        if (((Long) rtcTimes.first).longValue() > 0) {
            earliest = Math.max(((Long) rtcTimes.first).longValue() - nowWallclock, 0L) + nowElapsed;
        } else {
            earliest = 0;
        }
        long longValue = ((Long) rtcTimes.second).longValue();
        long latest = JobStatus.NO_LATEST_RUNTIME;
        if (longValue < JobStatus.NO_LATEST_RUNTIME) {
            latest = nowElapsed + Math.max(((Long) rtcTimes.second).longValue() - nowWallclock, 0L);
        }
        return Pair.create(Long.valueOf(earliest), Long.valueOf(latest));
    }

    public static boolean isSyncJob(JobStatus status) {
        return SyncJobService.class.getName().equals(status.getServiceComponent().getClassName());
    }

    public final class ReadJobMapFromDiskRunnable implements Runnable {
        private final JobSet jobSet;
        private final boolean rtcGood;

        ReadJobMapFromDiskRunnable(JobSet jobSet2, boolean rtcIsGood) {
            JobStore.this = r1;
            this.jobSet = jobSet2;
            this.rtcGood = rtcIsGood;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:20:0x005e, code lost:
            if (com.android.server.job.JobStore.this.mPersistInfo.countAllJobsLoaded < 0) goto L_0x0060;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0060, code lost:
            com.android.server.job.JobStore.this.mPersistInfo.countAllJobsLoaded = r0;
            com.android.server.job.JobStore.this.mPersistInfo.countSystemServerJobsLoaded = r1;
            com.android.server.job.JobStore.this.mPersistInfo.countSystemSyncManagerJobsLoaded = r2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:0x008f, code lost:
            if (com.android.server.job.JobStore.this.mPersistInfo.countAllJobsLoaded >= 0) goto L_0x00ab;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x00a8, code lost:
            if (com.android.server.job.JobStore.this.mPersistInfo.countAllJobsLoaded >= 0) goto L_0x00ab;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:38:0x00ab, code lost:
            android.util.Slog.i(com.android.server.job.JobStore.TAG, "Read " + r0 + " jobs");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:39:0x00c6, code lost:
            return;
         */
        @Override // java.lang.Runnable
        public void run() {
            int numJobs = 0;
            int numSystemJobs = 0;
            int numSyncJobs = 0;
            try {
                FileInputStream fis = JobStore.this.mJobsFile.openRead();
                synchronized (JobStore.this.mLock) {
                    List<JobStatus> jobs = readJobMapImpl(fis, this.rtcGood);
                    if (jobs != null) {
                        long now = JobSchedulerService.sElapsedRealtimeClock.millis();
                        IActivityManager am = ActivityManager.getService();
                        for (int i = 0; i < jobs.size(); i++) {
                            JobStatus js = jobs.get(i);
                            js.prepareLocked(am);
                            js.enqueueTime = now;
                            this.jobSet.add(js);
                            numJobs++;
                            if (js.getUid() == 1000) {
                                numSystemJobs++;
                                if (JobStore.isSyncJob(js)) {
                                    numSyncJobs++;
                                }
                            }
                        }
                    }
                }
                try {
                    fis.close();
                } catch (IOException | XmlPullParserException e) {
                    Slog.wtf(JobStore.TAG, "Error jobstore xml.", e);
                } catch (Throwable th) {
                    if (JobStore.this.mPersistInfo.countAllJobsLoaded < 0) {
                        JobStore.this.mPersistInfo.countAllJobsLoaded = numJobs;
                        JobStore.this.mPersistInfo.countSystemServerJobsLoaded = numSystemJobs;
                        JobStore.this.mPersistInfo.countSystemSyncManagerJobsLoaded = numSyncJobs;
                    }
                    throw th;
                }
            } catch (FileNotFoundException e2) {
                if (JobStore.DEBUG) {
                    Slog.d(JobStore.TAG, "Could not find jobs file, probably there was nothing to load.");
                }
            }
        }

        private List<JobStatus> readJobMapImpl(FileInputStream fis, boolean rtcIsGood) {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fis, StandardCharsets.UTF_8.name());
            int eventType = parser.getEventType();
            while (eventType != 2 && eventType != 1) {
                eventType = parser.next();
                Slog.d(JobStore.TAG, "Start tag: " + parser.getName());
            }
            if (eventType == 1) {
                if (JobStore.DEBUG) {
                    Slog.d(JobStore.TAG, "No persisted jobs.");
                }
                return null;
            } else if (!"job-info".equals(parser.getName())) {
                return null;
            } else {
                List<JobStatus> jobs = new ArrayList<>();
                try {
                    if (Integer.parseInt(parser.getAttributeValue(null, "version")) != 0) {
                        Slog.d(JobStore.TAG, "Invalid version number, aborting jobs file read.");
                        return null;
                    }
                    int eventType2 = parser.next();
                    do {
                        if (eventType2 == 2) {
                            if ("job".equals(parser.getName())) {
                                JobStatus persistedJob = restoreJobFromXml(rtcIsGood, parser);
                                if (persistedJob != null) {
                                    if (JobStore.DEBUG) {
                                        Slog.d(JobStore.TAG, "Read out " + persistedJob);
                                    }
                                    jobs.add(persistedJob);
                                } else {
                                    Slog.d(JobStore.TAG, "Error reading job from file.");
                                }
                            }
                        }
                        eventType2 = parser.next();
                    } while (eventType2 != 1);
                    return jobs;
                } catch (NumberFormatException e) {
                    Slog.e(JobStore.TAG, "Invalid version number, aborting jobs file read.");
                    return null;
                }
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for r42v0, resolved type: org.xmlpull.v1.XmlPullParser */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r5v4, types: [com.android.server.job.controllers.JobStatus, java.lang.String] */
        /* JADX WARN: Type inference failed for: r5v18 */
        /* JADX WARN: Type inference failed for: r5v22 */
        /* JADX WARNING: Unknown variable types count: 1 */
        private JobStatus restoreJobFromXml(boolean rtcIsGood, XmlPullParser parser) {
            int eventType;
            int eventType2;
            int internalFlags;
            boolean z;
            Pair<Long, Long> elapsedRuntimes;
            int eventType3;
            String sourcePackageName;
            long flexMillis;
            ReadJobMapFromDiskRunnable readJobMapFromDiskRunnable = this;
            int internalFlags2 = 0;
            JobStatus jobStatus = null;
            try {
                JobInfo.Builder jobBuilder = readJobMapFromDiskRunnable.buildBuilderFromXml(parser);
                boolean z2 = true;
                jobBuilder.setPersisted(true);
                int uid = Integer.parseInt(parser.getAttributeValue(null, WatchlistLoggingHandler.WatchlistEventKeys.UID));
                String val = parser.getAttributeValue(null, "priority");
                if (val != null) {
                    jobBuilder.setPriority(Integer.parseInt(val));
                }
                String val2 = parser.getAttributeValue(null, "flags");
                if (val2 != null) {
                    jobBuilder.setFlags(Integer.parseInt(val2));
                }
                String val3 = parser.getAttributeValue(null, "internalFlags");
                if (val3 != null) {
                    internalFlags2 = Integer.parseInt(val3);
                }
                try {
                    String val4 = parser.getAttributeValue(null, "sourceUserId");
                    int sourceUserId = val4 == null ? -1 : Integer.parseInt(val4);
                    String val5 = parser.getAttributeValue(null, "lastSuccessfulRunTime");
                    long lastSuccessfulRunTime = val5 == null ? 0 : Long.parseLong(val5);
                    String val6 = parser.getAttributeValue(null, "lastFailedRunTime");
                    long lastFailedRunTime = val6 == null ? 0 : Long.parseLong(val6);
                    String sourcePackageName2 = parser.getAttributeValue(null, "sourcePackageName");
                    String sourceTag = parser.getAttributeValue(null, "sourceTag");
                    while (true) {
                        eventType = parser.next();
                        if (eventType != 4) {
                            break;
                        }
                        readJobMapFromDiskRunnable = this;
                        jobStatus = null;
                    }
                    if (eventType != 2) {
                        return jobStatus;
                    }
                    if (!JobStore.XML_TAG_PARAMS_CONSTRAINTS.equals(parser.getName())) {
                        return jobStatus;
                    }
                    try {
                        readJobMapFromDiskRunnable.buildConstraintsFromXml(jobBuilder, parser);
                        parser.next();
                        ?? r5 = jobStatus;
                        while (true) {
                            eventType2 = parser.next();
                            if (eventType2 != 4) {
                                break;
                            }
                            z2 = z2;
                            r5 = r5;
                            readJobMapFromDiskRunnable = this;
                        }
                        if (eventType2 != 2) {
                            return r5;
                        }
                        try {
                            Pair<Long, Long> rtcRuntimes = readJobMapFromDiskRunnable.buildRtcExecutionTimesFromXml(parser);
                            long elapsedNow = JobSchedulerService.sElapsedRealtimeClock.millis();
                            Pair<Long, Long> elapsedRuntimes2 = JobStore.convertRtcBoundsToElapsed(rtcRuntimes, elapsedNow);
                            if (JobStore.XML_TAG_PERIODIC.equals(parser.getName())) {
                                try {
                                    long periodMillis = Long.parseLong(parser.getAttributeValue(r5, "period"));
                                    String val7 = parser.getAttributeValue(r5, "flex");
                                    if (val7 != null) {
                                        try {
                                            flexMillis = Long.valueOf(val7).longValue();
                                        } catch (NumberFormatException e) {
                                        }
                                    } else {
                                        flexMillis = periodMillis;
                                    }
                                    internalFlags = internalFlags2;
                                    try {
                                        jobBuilder.setPeriodic(periodMillis, flexMillis);
                                        if (((Long) elapsedRuntimes2.second).longValue() > elapsedNow + periodMillis + flexMillis) {
                                            long clampedLateRuntimeElapsed = elapsedNow + flexMillis + periodMillis;
                                            long clampedEarlyRuntimeElapsed = clampedLateRuntimeElapsed - flexMillis;
                                            z = false;
                                            Slog.w(JobStore.TAG, String.format("Periodic job for uid='%d' persisted run-time is too big [%s, %s]. Clamping to [%s,%s]", Integer.valueOf(uid), DateUtils.formatElapsedTime(((Long) elapsedRuntimes2.first).longValue() / 1000), DateUtils.formatElapsedTime(((Long) elapsedRuntimes2.second).longValue() / 1000), DateUtils.formatElapsedTime(clampedEarlyRuntimeElapsed / 1000), DateUtils.formatElapsedTime(clampedLateRuntimeElapsed / 1000)));
                                            elapsedRuntimes2 = Pair.create(Long.valueOf(clampedEarlyRuntimeElapsed), Long.valueOf(clampedLateRuntimeElapsed));
                                        } else {
                                            z = false;
                                        }
                                        elapsedRuntimes = elapsedRuntimes2;
                                    } catch (NumberFormatException e2) {
                                        Slog.d(JobStore.TAG, "Error reading periodic execution criteria, skipping.");
                                        return null;
                                    }
                                } catch (NumberFormatException e3) {
                                    Slog.d(JobStore.TAG, "Error reading periodic execution criteria, skipping.");
                                    return null;
                                }
                            } else {
                                internalFlags = internalFlags2;
                                z = false;
                                if (JobStore.XML_TAG_ONEOFF.equals(parser.getName())) {
                                    try {
                                        if (((Long) elapsedRuntimes2.first).longValue() != 0) {
                                            try {
                                                jobBuilder.setMinimumLatency(((Long) elapsedRuntimes2.first).longValue() - elapsedNow);
                                            } catch (NumberFormatException e4) {
                                            }
                                        }
                                        if (((Long) elapsedRuntimes2.second).longValue() != JobStatus.NO_LATEST_RUNTIME) {
                                            jobBuilder.setOverrideDeadline(((Long) elapsedRuntimes2.second).longValue() - elapsedNow);
                                        }
                                        elapsedRuntimes = elapsedRuntimes2;
                                    } catch (NumberFormatException e5) {
                                        Slog.d(JobStore.TAG, "Error reading job execution criteria, skipping.");
                                        return null;
                                    }
                                } else if (!JobStore.DEBUG) {
                                    return null;
                                } else {
                                    Slog.d(JobStore.TAG, "Invalid parameter tag, skipping - " + parser.getName());
                                    return null;
                                }
                            }
                            maybeBuildBackoffPolicyFromXml(jobBuilder, parser);
                            parser.nextTag();
                            while (true) {
                                eventType3 = parser.next();
                                if (eventType3 != 4) {
                                    break;
                                }
                            }
                            if (eventType3 == 2) {
                                if (JobStore.XML_TAG_EXTRAS.equals(parser.getName())) {
                                    PersistableBundle extras = PersistableBundle.restoreFromXml(parser);
                                    jobBuilder.setExtras(extras);
                                    parser.nextTag();
                                    try {
                                        jobBuilder.build();
                                        if (!PackageManagerService.PLATFORM_PACKAGE_NAME.equals(sourcePackageName2) || extras == null || !extras.getBoolean("SyncManagerJob", z)) {
                                            sourcePackageName = sourcePackageName2;
                                        } else {
                                            sourcePackageName = extras.getString("owningPackage", sourcePackageName2);
                                            if (JobStore.DEBUG) {
                                                Slog.i(JobStore.TAG, "Fixing up sync job source package name from 'android' to '" + sourcePackageName + "'");
                                            }
                                        }
                                        JobSchedulerInternal service = (JobSchedulerInternal) LocalServices.getService(JobSchedulerInternal.class);
                                        return new JobStatus(jobBuilder.build(), uid, sourcePackageName, sourceUserId, JobSchedulerService.standbyBucketForPackage(sourcePackageName, sourceUserId, elapsedNow), service != null ? service.currentHeartbeat() : 0, sourceTag, ((Long) elapsedRuntimes.first).longValue(), ((Long) elapsedRuntimes.second).longValue(), lastSuccessfulRunTime, lastFailedRunTime, rtcIsGood ? null : rtcRuntimes, internalFlags);
                                    } catch (Exception e6) {
                                        Slog.w(JobStore.TAG, "Unable to build job from XML, ignoring: " + jobBuilder.summarize());
                                        return null;
                                    }
                                }
                            }
                            if (!JobStore.DEBUG) {
                                return null;
                            }
                            Slog.d(JobStore.TAG, "Error reading extras, skipping.");
                            return null;
                        } catch (NumberFormatException e7) {
                            if (!JobStore.DEBUG) {
                                return null;
                            }
                            Slog.d(JobStore.TAG, "Error parsing execution time parameters, skipping.");
                            return null;
                        }
                    } catch (NumberFormatException e8) {
                        Slog.d(JobStore.TAG, "Error reading constraints, skipping.");
                        return jobStatus;
                    }
                } catch (NumberFormatException e9) {
                    Slog.e(JobStore.TAG, "Error parsing job's required fields, skipping");
                    return null;
                }
            } catch (NumberFormatException e10) {
                Slog.e(JobStore.TAG, "Error parsing job's required fields, skipping");
                return null;
            }
        }

        private JobInfo.Builder buildBuilderFromXml(XmlPullParser parser) {
            return new JobInfo.Builder(Integer.parseInt(parser.getAttributeValue(null, "jobid")), new ComponentName(parser.getAttributeValue(null, "package"), parser.getAttributeValue(null, "class")));
        }

        private void buildConstraintsFromXml(JobInfo.Builder jobBuilder, XmlPullParser parser) {
            long unwantedCapabilities;
            String netCapabilities = parser.getAttributeValue(null, "net-capabilities");
            String netUnwantedCapabilities = parser.getAttributeValue(null, "net-unwanted-capabilities");
            String netTransportTypes = parser.getAttributeValue(null, "net-transport-types");
            if (netCapabilities == null || netTransportTypes == null) {
                if (parser.getAttributeValue(null, "connectivity") != null) {
                    jobBuilder.setRequiredNetworkType(1);
                }
                if (parser.getAttributeValue(null, "metered") != null) {
                    jobBuilder.setRequiredNetworkType(4);
                }
                if (parser.getAttributeValue(null, "unmetered") != null) {
                    jobBuilder.setRequiredNetworkType(2);
                }
                if (parser.getAttributeValue(null, "not-roaming") != null) {
                    jobBuilder.setRequiredNetworkType(3);
                }
            } else {
                NetworkRequest request = new NetworkRequest.Builder().build();
                if (netUnwantedCapabilities != null) {
                    unwantedCapabilities = Long.parseLong(netUnwantedCapabilities);
                } else {
                    unwantedCapabilities = BitUtils.packBits(request.networkCapabilities.getUnwantedCapabilities());
                }
                request.networkCapabilities.setCapabilities(BitUtils.unpackBits(Long.parseLong(netCapabilities)), BitUtils.unpackBits(unwantedCapabilities));
                request.networkCapabilities.setTransportTypes(BitUtils.unpackBits(Long.parseLong(netTransportTypes)));
                jobBuilder.setRequiredNetwork(request);
            }
            if (parser.getAttributeValue(null, "idle") != null) {
                jobBuilder.setRequiresDeviceIdle(true);
            }
            if (parser.getAttributeValue(null, "charging") != null) {
                jobBuilder.setRequiresCharging(true);
            }
            if (parser.getAttributeValue(null, "battery-not-low") != null) {
                jobBuilder.setRequiresBatteryNotLow(true);
            }
            if (parser.getAttributeValue(null, "storage-not-low") != null) {
                jobBuilder.setRequiresStorageNotLow(true);
            }
        }

        private void maybeBuildBackoffPolicyFromXml(JobInfo.Builder jobBuilder, XmlPullParser parser) {
            String val = parser.getAttributeValue(null, "initial-backoff");
            if (val != null) {
                jobBuilder.setBackoffCriteria(Long.parseLong(val), Integer.parseInt(parser.getAttributeValue(null, "backoff-policy")));
            }
        }

        private Pair<Long, Long> buildRtcExecutionTimesFromXml(XmlPullParser parser) {
            long earliestRunTimeRtc;
            long latestRunTimeRtc;
            String val = parser.getAttributeValue(null, "delay");
            if (val != null) {
                earliestRunTimeRtc = Long.parseLong(val);
            } else {
                earliestRunTimeRtc = 0;
            }
            String val2 = parser.getAttributeValue(null, "deadline");
            if (val2 != null) {
                latestRunTimeRtc = Long.parseLong(val2);
            } else {
                latestRunTimeRtc = JobStatus.NO_LATEST_RUNTIME;
            }
            return Pair.create(Long.valueOf(earliestRunTimeRtc), Long.valueOf(latestRunTimeRtc));
        }
    }

    public static final class JobSet {
        final SparseArray<ArraySet<JobStatus>> mJobs = new SparseArray<>();
        final SparseArray<ArraySet<JobStatus>> mJobsPerSourceUid = new SparseArray<>();

        public List<JobStatus> getJobsByUid(int uid) {
            ArrayList<JobStatus> matchingJobs = new ArrayList<>();
            ArraySet<JobStatus> jobs = this.mJobs.get(uid);
            if (jobs != null) {
                matchingJobs.addAll(jobs);
            }
            return matchingJobs;
        }

        public List<JobStatus> getJobsByUser(int userId) {
            ArraySet<JobStatus> jobs;
            ArrayList<JobStatus> result = new ArrayList<>();
            for (int i = this.mJobsPerSourceUid.size() - 1; i >= 0; i--) {
                if (UserHandle.getUserId(this.mJobsPerSourceUid.keyAt(i)) == userId && (jobs = this.mJobsPerSourceUid.valueAt(i)) != null) {
                    result.addAll(jobs);
                }
            }
            return result;
        }

        public boolean add(JobStatus job) {
            int uid = job.getUid();
            int sourceUid = job.getSourceUid();
            ArraySet<JobStatus> jobs = this.mJobs.get(uid);
            if (jobs == null) {
                jobs = new ArraySet<>();
                this.mJobs.put(uid, jobs);
            }
            ArraySet<JobStatus> jobsForSourceUid = this.mJobsPerSourceUid.get(sourceUid);
            if (jobsForSourceUid == null) {
                jobsForSourceUid = new ArraySet<>();
                this.mJobsPerSourceUid.put(sourceUid, jobsForSourceUid);
            }
            boolean added = jobs.add(job);
            boolean addedInSource = jobsForSourceUid.add(job);
            if (added != addedInSource) {
                Slog.wtf(JobStore.TAG, "mJobs and mJobsPerSourceUid mismatch; caller= " + added + " source= " + addedInSource);
            }
            return added || addedInSource;
        }

        public boolean remove(JobStatus job) {
            int uid = job.getUid();
            ArraySet<JobStatus> jobs = this.mJobs.get(uid);
            int sourceUid = job.getSourceUid();
            ArraySet<JobStatus> jobsForSourceUid = this.mJobsPerSourceUid.get(sourceUid);
            boolean didRemove = jobs != null && jobs.remove(job);
            boolean sourceRemove = jobsForSourceUid != null && jobsForSourceUid.remove(job);
            if (didRemove != sourceRemove) {
                Slog.wtf(JobStore.TAG, "Job presence mismatch; caller=" + didRemove + " source=" + sourceRemove);
            }
            if (!didRemove && !sourceRemove) {
                return false;
            }
            if (jobs != null && jobs.size() == 0) {
                this.mJobs.remove(uid);
            }
            if (jobsForSourceUid != null && jobsForSourceUid.size() == 0) {
                this.mJobsPerSourceUid.remove(sourceUid);
            }
            return true;
        }

        public void removeJobsOfNonUsers(int[] whitelist) {
            removeAll(new Predicate(whitelist) {
                /* class com.android.server.job.$$Lambda$JobStore$JobSet$D9839QVHHu4XhnxouyIMkP5NWA */
                private final /* synthetic */ int[] f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return JobStore.JobSet.lambda$removeJobsOfNonUsers$0(this.f$0, (JobStatus) obj);
                }
            }.or(new Predicate(whitelist) {
                /* class com.android.server.job.$$Lambda$JobStore$JobSet$id1Y3Yh8Y9sEbnjlNCUNay6U9k */
                private final /* synthetic */ int[] f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return JobStore.JobSet.lambda$removeJobsOfNonUsers$1(this.f$0, (JobStatus) obj);
                }
            }));
        }

        static /* synthetic */ boolean lambda$removeJobsOfNonUsers$0(int[] whitelist, JobStatus job) {
            return !ArrayUtils.contains(whitelist, job.getSourceUserId());
        }

        static /* synthetic */ boolean lambda$removeJobsOfNonUsers$1(int[] whitelist, JobStatus job) {
            return !ArrayUtils.contains(whitelist, job.getUserId());
        }

        private void removeAll(Predicate<JobStatus> predicate) {
            for (int jobSetIndex = this.mJobs.size() - 1; jobSetIndex >= 0; jobSetIndex--) {
                ArraySet<JobStatus> jobs = this.mJobs.valueAt(jobSetIndex);
                for (int jobIndex = jobs.size() - 1; jobIndex >= 0; jobIndex--) {
                    if (predicate.test(jobs.valueAt(jobIndex))) {
                        jobs.removeAt(jobIndex);
                    }
                }
                if (jobs.size() == 0) {
                    this.mJobs.removeAt(jobSetIndex);
                }
            }
            for (int jobSetIndex2 = this.mJobsPerSourceUid.size() - 1; jobSetIndex2 >= 0; jobSetIndex2--) {
                ArraySet<JobStatus> jobs2 = this.mJobsPerSourceUid.valueAt(jobSetIndex2);
                for (int jobIndex2 = jobs2.size() - 1; jobIndex2 >= 0; jobIndex2--) {
                    if (predicate.test(jobs2.valueAt(jobIndex2))) {
                        jobs2.removeAt(jobIndex2);
                    }
                }
                if (jobs2.size() == 0) {
                    this.mJobsPerSourceUid.removeAt(jobSetIndex2);
                }
            }
        }

        public boolean contains(JobStatus job) {
            ArraySet<JobStatus> jobs = this.mJobs.get(job.getUid());
            return jobs != null && jobs.contains(job);
        }

        public JobStatus get(int uid, int jobId) {
            ArraySet<JobStatus> jobs = this.mJobs.get(uid);
            if (jobs == null) {
                return null;
            }
            for (int i = jobs.size() - 1; i >= 0; i--) {
                JobStatus job = jobs.valueAt(i);
                if (job.getJobId() == jobId) {
                    return job;
                }
            }
            return null;
        }

        public List<JobStatus> getAllJobs() {
            ArrayList<JobStatus> allJobs = new ArrayList<>(size());
            for (int i = this.mJobs.size() - 1; i >= 0; i--) {
                ArraySet<JobStatus> jobs = this.mJobs.valueAt(i);
                if (jobs != null) {
                    for (int j = jobs.size() - 1; j >= 0; j--) {
                        allJobs.add(jobs.valueAt(j));
                    }
                }
            }
            return allJobs;
        }

        public void clear() {
            this.mJobs.clear();
            this.mJobsPerSourceUid.clear();
        }

        public int size() {
            int total = 0;
            for (int i = this.mJobs.size() - 1; i >= 0; i--) {
                total += this.mJobs.valueAt(i).size();
            }
            return total;
        }

        public int countJobsForUid(int uid) {
            int total = 0;
            ArraySet<JobStatus> jobs = this.mJobs.get(uid);
            if (jobs != null) {
                for (int i = jobs.size() - 1; i >= 0; i--) {
                    JobStatus job = jobs.valueAt(i);
                    if (job.getUid() == job.getSourceUid()) {
                        total++;
                    }
                }
            }
            return total;
        }

        public void forEachJob(Predicate<JobStatus> filterPredicate, Consumer<JobStatus> functor) {
            for (int uidIndex = this.mJobs.size() - 1; uidIndex >= 0; uidIndex--) {
                ArraySet<JobStatus> jobs = this.mJobs.valueAt(uidIndex);
                if (jobs != null) {
                    for (int i = jobs.size() - 1; i >= 0; i--) {
                        JobStatus jobStatus = jobs.valueAt(i);
                        if (filterPredicate == null || filterPredicate.test(jobStatus)) {
                            functor.accept(jobStatus);
                        }
                    }
                }
            }
        }

        public void forEachJob(int callingUid, Consumer<JobStatus> functor) {
            ArraySet<JobStatus> jobs = this.mJobs.get(callingUid);
            if (jobs != null) {
                for (int i = jobs.size() - 1; i >= 0; i--) {
                    functor.accept(jobs.valueAt(i));
                }
            }
        }

        public void forEachJobForSourceUid(int sourceUid, Consumer<JobStatus> functor) {
            ArraySet<JobStatus> jobs = this.mJobsPerSourceUid.get(sourceUid);
            if (jobs != null) {
                for (int i = jobs.size() - 1; i >= 0; i--) {
                    functor.accept(jobs.valueAt(i));
                }
            }
        }
    }
}

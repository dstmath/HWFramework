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
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.BitUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.server.IoThread;
import com.android.server.LocalServices;
import com.android.server.audio.AudioService;
import com.android.server.content.SyncJobService;
import com.android.server.job.JobSchedulerInternal;
import com.android.server.job.JobStore;
import com.android.server.job.controllers.JobStatus;
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
    /* access modifiers changed from: private */
    public static final boolean DEBUG = JobSchedulerService.DEBUG;
    private static final int JOBS_FILE_VERSION = 0;
    private static final int MAX_OPS_BEFORE_WRITE = 1;
    private static final String TAG = "JobStore";
    private static final String XML_TAG_EXTRAS = "extras";
    private static final String XML_TAG_ONEOFF = "one-off";
    private static final String XML_TAG_PARAMS_CONSTRAINTS = "constraints";
    private static final String XML_TAG_PERIODIC = "periodic";
    private static JobStore sSingleton;
    private static final Object sSingletonLock = new Object();
    final Context mContext;
    /* access modifiers changed from: private */
    public int mDirtyOperations;
    private final Handler mIoHandler = IoThread.getHandler();
    final JobSet mJobSet;
    /* access modifiers changed from: private */
    public final AtomicFile mJobsFile;
    final Object mLock;
    /* access modifiers changed from: private */
    public JobSchedulerInternal.JobStorePersistStats mPersistInfo = new JobSchedulerInternal.JobStorePersistStats();
    private boolean mRtcGood;
    private final Runnable mWriteRunnable = new Runnable() {
        public void run() {
            long startElapsed = JobSchedulerService.sElapsedRealtimeClock.millis();
            List<JobStatus> storeCopy = new ArrayList<>();
            synchronized (JobStore.this.mLock) {
                JobStore.this.mJobSet.forEachJob((Predicate<JobStatus>) null, (Consumer<JobStatus>) new Consumer(storeCopy) {
                    private final /* synthetic */ List f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void accept(Object obj) {
                        JobStore.AnonymousClass1.lambda$run$0(this.f$0, (JobStatus) obj);
                    }
                });
            }
            writeJobsMapImpl(storeCopy);
            if (JobStore.DEBUG) {
                Slog.v(JobStore.TAG, "Finished writing, took " + (JobSchedulerService.sElapsedRealtimeClock.millis() - startElapsed) + "ms");
            }
        }

        static /* synthetic */ void lambda$run$0(List storeCopy, JobStatus job) {
            if (job.isPersisted()) {
                storeCopy.add(new JobStatus(job));
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:42:0x0114 A[Catch:{ IOException -> 0x011c, XmlPullParserException -> 0x010b, all -> 0x0104, all -> 0x0146 }] */
        /* JADX WARNING: Removed duplicated region for block: B:47:0x0125 A[Catch:{ IOException -> 0x011c, XmlPullParserException -> 0x010b, all -> 0x0104, all -> 0x0146 }] */
        private void writeJobsMapImpl(List<JobStatus> jobList) {
            int numSyncJobs;
            int numSystemJobs;
            int numJobs;
            int numJobs2 = 0;
            int numSystemJobs2 = 0;
            int numSyncJobs2 = 0;
            try {
                long startTime = SystemClock.uptimeMillis();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(baos, StandardCharsets.UTF_8.name());
                out.startDocument(null, true);
                out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                out.startTag(null, "job-info");
                out.attribute(null, "version", Integer.toString(0));
                numSyncJobs = 0;
                numSystemJobs = 0;
                numJobs = 0;
                int i = 0;
                while (i < jobList.size()) {
                    try {
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
                            i++;
                        } catch (IOException e) {
                            e = e;
                            numJobs2 = numJobs;
                            numSystemJobs2 = numSystemJobs;
                            numSyncJobs2 = numSyncJobs;
                            if (JobStore.DEBUG) {
                            }
                            JobStore.this.mPersistInfo.countAllJobsSaved = numJobs2;
                            JobStore.this.mPersistInfo.countSystemServerJobsSaved = numSystemJobs2;
                            JobStore.this.mPersistInfo.countSystemSyncManagerJobsSaved = numSyncJobs2;
                        } catch (XmlPullParserException e2) {
                            e = e2;
                            numJobs2 = numJobs;
                            numSystemJobs2 = numSystemJobs;
                            numSyncJobs2 = numSyncJobs;
                            if (JobStore.DEBUG) {
                            }
                            JobStore.this.mPersistInfo.countAllJobsSaved = numJobs2;
                            JobStore.this.mPersistInfo.countSystemServerJobsSaved = numSystemJobs2;
                            JobStore.this.mPersistInfo.countSystemSyncManagerJobsSaved = numSyncJobs2;
                        } catch (Throwable th) {
                            th = th;
                            JobStore.this.mPersistInfo.countAllJobsSaved = numJobs;
                            JobStore.this.mPersistInfo.countSystemServerJobsSaved = numSystemJobs;
                            JobStore.this.mPersistInfo.countSystemSyncManagerJobsSaved = numSyncJobs;
                            throw th;
                        }
                    } catch (IOException e3) {
                        e = e3;
                        List<JobStatus> list = jobList;
                        numJobs2 = numJobs;
                        numSystemJobs2 = numSystemJobs;
                        numSyncJobs2 = numSyncJobs;
                        if (JobStore.DEBUG) {
                        }
                        JobStore.this.mPersistInfo.countAllJobsSaved = numJobs2;
                        JobStore.this.mPersistInfo.countSystemServerJobsSaved = numSystemJobs2;
                        JobStore.this.mPersistInfo.countSystemSyncManagerJobsSaved = numSyncJobs2;
                    } catch (XmlPullParserException e4) {
                        e = e4;
                        List<JobStatus> list2 = jobList;
                        numJobs2 = numJobs;
                        numSystemJobs2 = numSystemJobs;
                        numSyncJobs2 = numSyncJobs;
                        if (JobStore.DEBUG) {
                        }
                        JobStore.this.mPersistInfo.countAllJobsSaved = numJobs2;
                        JobStore.this.mPersistInfo.countSystemServerJobsSaved = numSystemJobs2;
                        JobStore.this.mPersistInfo.countSystemSyncManagerJobsSaved = numSyncJobs2;
                    } catch (Throwable th2) {
                        th = th2;
                        List<JobStatus> list3 = jobList;
                        JobStore.this.mPersistInfo.countAllJobsSaved = numJobs;
                        JobStore.this.mPersistInfo.countSystemServerJobsSaved = numSystemJobs;
                        JobStore.this.mPersistInfo.countSystemSyncManagerJobsSaved = numSyncJobs;
                        throw th;
                    }
                }
                List<JobStatus> list4 = jobList;
                out.endTag(null, "job-info");
                out.endDocument();
                FileOutputStream fos = JobStore.this.mJobsFile.startWrite(startTime);
                fos.write(baos.toByteArray());
                JobStore.this.mJobsFile.finishWrite(fos);
                int unused = JobStore.this.mDirtyOperations = 0;
                JobStore.this.mPersistInfo.countAllJobsSaved = numJobs;
                JobStore.this.mPersistInfo.countSystemServerJobsSaved = numSystemJobs;
                JobStore.this.mPersistInfo.countSystemSyncManagerJobsSaved = numSyncJobs;
                int i2 = numJobs;
                int numJobs3 = numSystemJobs;
                int numSystemJobs3 = numSyncJobs;
            } catch (IOException e5) {
                e = e5;
                List<JobStatus> list5 = jobList;
                if (JobStore.DEBUG) {
                    Slog.v(JobStore.TAG, "Error writing out job data.", e);
                }
                JobStore.this.mPersistInfo.countAllJobsSaved = numJobs2;
                JobStore.this.mPersistInfo.countSystemServerJobsSaved = numSystemJobs2;
                JobStore.this.mPersistInfo.countSystemSyncManagerJobsSaved = numSyncJobs2;
            } catch (XmlPullParserException e6) {
                e = e6;
                List<JobStatus> list6 = jobList;
                if (JobStore.DEBUG) {
                    Slog.d(JobStore.TAG, "Error persisting bundle.", e);
                }
                JobStore.this.mPersistInfo.countAllJobsSaved = numJobs2;
                JobStore.this.mPersistInfo.countSystemServerJobsSaved = numSystemJobs2;
                JobStore.this.mPersistInfo.countSystemSyncManagerJobsSaved = numSyncJobs2;
            } catch (Throwable th3) {
                th = th3;
                numSyncJobs = numSyncJobs2;
                numSystemJobs = numSystemJobs2;
                numJobs = numJobs2;
                JobStore.this.mPersistInfo.countAllJobsSaved = numJobs;
                JobStore.this.mPersistInfo.countSystemServerJobsSaved = numSystemJobs;
                JobStore.this.mPersistInfo.countSystemSyncManagerJobsSaved = numSyncJobs;
                throw th;
            }
        }

        private void addAttributesToJobTag(XmlSerializer out, JobStatus jobStatus) throws IOException {
            out.attribute(null, "jobid", Integer.toString(jobStatus.getJobId()));
            out.attribute(null, "package", jobStatus.getServiceComponent().getPackageName());
            out.attribute(null, AudioService.CONNECT_INTENT_KEY_DEVICE_CLASS, jobStatus.getServiceComponent().getClassName());
            if (jobStatus.getSourcePackageName() != null) {
                out.attribute(null, "sourcePackageName", jobStatus.getSourcePackageName());
            }
            if (jobStatus.getSourceTag() != null) {
                out.attribute(null, "sourceTag", jobStatus.getSourceTag());
            }
            out.attribute(null, "sourceUserId", String.valueOf(jobStatus.getSourceUserId()));
            out.attribute(null, "uid", Integer.toString(jobStatus.getUid()));
            out.attribute(null, "priority", String.valueOf(jobStatus.getPriority()));
            out.attribute(null, "flags", String.valueOf(jobStatus.getFlags()));
            if (jobStatus.getInternalFlags() != 0) {
                out.attribute(null, "internalFlags", String.valueOf(jobStatus.getInternalFlags()));
            }
            out.attribute(null, "lastSuccessfulRunTime", String.valueOf(jobStatus.getLastSuccessfulRunTime()));
            out.attribute(null, "lastFailedRunTime", String.valueOf(jobStatus.getLastFailedRunTime()));
        }

        private void writeBundleToXml(PersistableBundle extras, XmlSerializer out) throws IOException, XmlPullParserException {
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

        private void writeConstraintsToXml(XmlSerializer out, JobStatus jobStatus) throws IOException {
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
            out.endTag(null, JobStore.XML_TAG_PARAMS_CONSTRAINTS);
        }

        private void writeExecutionCriteriaToXml(XmlSerializer out, JobStatus jobStatus) throws IOException {
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
    private final long mXmlTimestamp;

    static final class JobSet {
        @VisibleForTesting
        final SparseArray<ArraySet<JobStatus>> mJobs = new SparseArray<>();
        @VisibleForTesting
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
            ArrayList<JobStatus> result = new ArrayList<>();
            for (int i = this.mJobsPerSourceUid.size() - 1; i >= 0; i--) {
                if (UserHandle.getUserId(this.mJobsPerSourceUid.keyAt(i)) == userId) {
                    ArraySet<JobStatus> jobs = this.mJobsPerSourceUid.valueAt(i);
                    if (jobs != null) {
                        result.addAll(jobs);
                    }
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
                private final /* synthetic */ int[] f$0;

                {
                    this.f$0 = r1;
                }

                public final boolean test(Object obj) {
                    return JobStore.JobSet.lambda$removeJobsOfNonUsers$0(this.f$0, (JobStatus) obj);
                }
            }.or(new Predicate(whitelist) {
                private final /* synthetic */ int[] f$0;

                {
                    this.f$0 = r1;
                }

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
            if (jobs != null) {
                for (int i = jobs.size() - 1; i >= 0; i--) {
                    JobStatus job = jobs.valueAt(i);
                    if (job.getJobId() == jobId) {
                        return job;
                    }
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

    private final class ReadJobMapFromDiskRunnable implements Runnable {
        private final JobSet jobSet;
        private final boolean rtcGood;

        ReadJobMapFromDiskRunnable(JobSet jobSet2, boolean rtcIsGood) {
            this.jobSet = jobSet2;
            this.rtcGood = rtcIsGood;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0060, code lost:
            if (com.android.server.job.JobStore.access$400(r13.this$0).countAllJobsLoaded < 0) goto L_0x0062;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x0062, code lost:
            com.android.server.job.JobStore.access$400(r13.this$0).countAllJobsLoaded = r0;
            com.android.server.job.JobStore.access$400(r13.this$0).countSystemServerJobsLoaded = r1;
            com.android.server.job.JobStore.access$400(r13.this$0).countSystemSyncManagerJobsLoaded = r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:34:0x0090, code lost:
            if (com.android.server.job.JobStore.access$400(r13.this$0).countAllJobsLoaded >= 0) goto L_0x00ac;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:41:0x00a9, code lost:
            if (com.android.server.job.JobStore.access$400(r13.this$0).countAllJobsLoaded >= 0) goto L_0x00ac;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:0x00ac, code lost:
            android.util.Slog.i(com.android.server.job.JobStore.TAG, "Read " + r0 + " jobs");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:43:0x00c7, code lost:
            return;
         */
        public void run() {
            List<JobStatus> jobs;
            int numJobs = 0;
            int numSystemJobs = 0;
            int numSyncJobs = 0;
            try {
                FileInputStream fis = JobStore.this.mJobsFile.openRead();
                synchronized (JobStore.this.mLock) {
                    jobs = readJobMapImpl(fis, this.rtcGood);
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
                List<JobStatus> list = jobs;
                fis.close();
            } catch (FileNotFoundException e) {
                if (JobStore.DEBUG) {
                    Slog.d(JobStore.TAG, "Could not find jobs file, probably there was nothing to load.");
                }
            } catch (IOException | XmlPullParserException e2) {
                try {
                    Slog.wtf(JobStore.TAG, "Error jobstore xml.", e2);
                } catch (Throwable th) {
                    if (JobStore.this.mPersistInfo.countAllJobsLoaded < 0) {
                        JobStore.this.mPersistInfo.countAllJobsLoaded = numJobs;
                        JobStore.this.mPersistInfo.countSystemServerJobsLoaded = numSystemJobs;
                        JobStore.this.mPersistInfo.countSystemSyncManagerJobsLoaded = numSyncJobs;
                    }
                    throw th;
                }
            }
        }

        private List<JobStatus> readJobMapImpl(FileInputStream fis, boolean rtcIsGood) throws XmlPullParserException, IOException {
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
                            String tagName = parser.getName();
                            if ("job".equals(tagName)) {
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
                            String str = tagName;
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

        /* JADX WARNING: type inference failed for: r4v0 */
        /* JADX WARNING: type inference failed for: r4v3, types: [com.android.server.job.controllers.JobStatus, java.lang.String] */
        /* JADX WARNING: type inference failed for: r4v25 */
        private JobStatus restoreJobFromXml(boolean rtcIsGood, XmlPullParser parser) throws XmlPullParserException, IOException {
            int eventType;
            JobStatus jobStatus;
            int eventType2;
            Pair<Long, Long> rtcRuntimes;
            int internalFlags;
            JobInfo.Builder jobBuilder;
            long currentHeartbeat;
            int internalFlags2;
            Pair<Long, Long> elapsedRuntimes;
            int eventType3;
            long flexMillis;
            JobInfo.Builder jobBuilder2;
            Object[] objArr;
            XmlPullParser xmlPullParser = parser;
            int internalFlags3 = 0;
            ? r4 = 0;
            try {
                JobInfo.Builder jobBuilder3 = buildBuilderFromXml(xmlPullParser);
                jobBuilder3.setPersisted(true);
                int uid = Integer.parseInt(xmlPullParser.getAttributeValue(null, "uid"));
                String val = xmlPullParser.getAttributeValue(null, "priority");
                if (val != null) {
                    jobBuilder3.setPriority(Integer.parseInt(val));
                }
                String val2 = xmlPullParser.getAttributeValue(null, "flags");
                if (val2 != null) {
                    jobBuilder3.setFlags(Integer.parseInt(val2));
                }
                String val3 = xmlPullParser.getAttributeValue(null, "internalFlags");
                if (val3 != null) {
                    internalFlags3 = Integer.parseInt(val3);
                }
                try {
                    String val4 = xmlPullParser.getAttributeValue(null, "sourceUserId");
                    int sourceUserId = val4 == null ? -1 : Integer.parseInt(val4);
                    String val5 = xmlPullParser.getAttributeValue(null, "lastSuccessfulRunTime");
                    long lastSuccessfulRunTime = val5 == null ? 0 : Long.parseLong(val5);
                    String val6 = xmlPullParser.getAttributeValue(null, "lastFailedRunTime");
                    long lastFailedRunTime = val6 == null ? 0 : Long.parseLong(val6);
                    String sourcePackageName = xmlPullParser.getAttributeValue(null, "sourcePackageName");
                    String sourceTag = xmlPullParser.getAttributeValue(null, "sourceTag");
                    while (true) {
                        eventType = parser.next();
                        if (eventType != 4) {
                            break;
                        }
                        r4 = 0;
                    }
                    if (eventType != 2) {
                        JobInfo.Builder builder = jobBuilder3;
                        int internalFlags4 = uid;
                        int i = sourceUserId;
                        jobStatus = r4;
                    } else if (!JobStore.XML_TAG_PARAMS_CONSTRAINTS.equals(parser.getName())) {
                        int i2 = internalFlags3;
                        JobInfo.Builder builder2 = jobBuilder3;
                        int internalFlags5 = uid;
                        int i3 = sourceUserId;
                        jobStatus = r4;
                    } else {
                        try {
                            buildConstraintsFromXml(jobBuilder3, xmlPullParser);
                            parser.next();
                            while (true) {
                                eventType2 = parser.next();
                                if (eventType2 != 4) {
                                    break;
                                }
                                int internalFlags6 = internalFlags3;
                                int internalFlags7 = uid;
                                int i4 = eventType2;
                                Object obj = r4;
                                uid = internalFlags7;
                                internalFlags3 = internalFlags6;
                            }
                            if (eventType2 != 2) {
                                return r4;
                            }
                            try {
                                Pair<Long, Long> rtcRuntimes2 = buildRtcExecutionTimesFromXml(xmlPullParser);
                                int sourceUserId2 = sourceUserId;
                                long elapsedNow = JobSchedulerService.sElapsedRealtimeClock.millis();
                                Pair<Long, Long> elapsedRuntimes2 = JobStore.convertRtcBoundsToElapsed(rtcRuntimes2, elapsedNow);
                                if (JobStore.XML_TAG_PERIODIC.equals(parser.getName())) {
                                    try {
                                        long periodMillis = Long.parseLong(xmlPullParser.getAttributeValue(r4, "period"));
                                        String val7 = xmlPullParser.getAttributeValue(r4, "flex");
                                        if (val7 != null) {
                                            try {
                                                flexMillis = Long.valueOf(val7).longValue();
                                            } catch (NumberFormatException e) {
                                                int i5 = internalFlags3;
                                                JobInfo.Builder builder3 = jobBuilder3;
                                                int internalFlags8 = uid;
                                                Pair<Long, Long> pair = rtcRuntimes2;
                                                int i6 = eventType2;
                                                Slog.d(JobStore.TAG, "Error reading periodic execution criteria, skipping.");
                                                return null;
                                            }
                                        } else {
                                            flexMillis = periodMillis;
                                        }
                                        int uid2 = uid;
                                        long periodMillis2 = periodMillis;
                                        long flexMillis2 = flexMillis;
                                        try {
                                            jobBuilder3.setPeriodic(periodMillis2, flexMillis2);
                                            if (((Long) elapsedRuntimes2.second).longValue() > elapsedNow + periodMillis2 + flexMillis2) {
                                                jobBuilder2 = jobBuilder3;
                                                long clampedLateRuntimeElapsed = elapsedNow + flexMillis2 + periodMillis2;
                                                long j = periodMillis2;
                                                long periodMillis3 = clampedLateRuntimeElapsed - flexMillis2;
                                                long j2 = flexMillis2;
                                                try {
                                                    objArr = new Object[5];
                                                    internalFlags = internalFlags3;
                                                    internalFlags2 = uid2;
                                                    try {
                                                        objArr[0] = Integer.valueOf(internalFlags2);
                                                        rtcRuntimes = rtcRuntimes2;
                                                    } catch (NumberFormatException e2) {
                                                        Pair<Long, Long> pair2 = rtcRuntimes2;
                                                        int i7 = eventType2;
                                                        Slog.d(JobStore.TAG, "Error reading periodic execution criteria, skipping.");
                                                        return null;
                                                    }
                                                } catch (NumberFormatException e3) {
                                                    int i8 = internalFlags3;
                                                    Pair<Long, Long> pair3 = rtcRuntimes2;
                                                    int i9 = eventType2;
                                                    int internalFlags9 = uid2;
                                                    Slog.d(JobStore.TAG, "Error reading periodic execution criteria, skipping.");
                                                    return null;
                                                }
                                                try {
                                                    int i10 = eventType2;
                                                    try {
                                                        objArr[1] = DateUtils.formatElapsedTime(((Long) elapsedRuntimes2.first).longValue() / 1000);
                                                        objArr[2] = DateUtils.formatElapsedTime(((Long) elapsedRuntimes2.second).longValue() / 1000);
                                                        objArr[3] = DateUtils.formatElapsedTime(periodMillis3 / 1000);
                                                        objArr[4] = DateUtils.formatElapsedTime(clampedLateRuntimeElapsed / 1000);
                                                        Slog.w(JobStore.TAG, String.format("Periodic job for uid='%d' persisted run-time is too big [%s, %s]. Clamping to [%s,%s]", objArr));
                                                        elapsedRuntimes2 = Pair.create(Long.valueOf(periodMillis3), Long.valueOf(clampedLateRuntimeElapsed));
                                                    } catch (NumberFormatException e4) {
                                                        Slog.d(JobStore.TAG, "Error reading periodic execution criteria, skipping.");
                                                        return null;
                                                    }
                                                } catch (NumberFormatException e5) {
                                                    int i11 = eventType2;
                                                    Slog.d(JobStore.TAG, "Error reading periodic execution criteria, skipping.");
                                                    return null;
                                                }
                                            } else {
                                                internalFlags = internalFlags3;
                                                jobBuilder2 = jobBuilder3;
                                                rtcRuntimes = rtcRuntimes2;
                                                int i12 = eventType2;
                                                internalFlags2 = uid2;
                                            }
                                            elapsedRuntimes = elapsedRuntimes2;
                                            jobBuilder = jobBuilder2;
                                            currentHeartbeat = 0;
                                        } catch (NumberFormatException e6) {
                                            int i13 = internalFlags3;
                                            JobInfo.Builder builder4 = jobBuilder3;
                                            Pair<Long, Long> pair4 = rtcRuntimes2;
                                            int i14 = eventType2;
                                            int internalFlags10 = uid2;
                                            Slog.d(JobStore.TAG, "Error reading periodic execution criteria, skipping.");
                                            return null;
                                        }
                                    } catch (NumberFormatException e7) {
                                        int i15 = internalFlags3;
                                        JobInfo.Builder builder5 = jobBuilder3;
                                        int internalFlags11 = uid;
                                        Pair<Long, Long> pair5 = rtcRuntimes2;
                                        int i16 = eventType2;
                                        Slog.d(JobStore.TAG, "Error reading periodic execution criteria, skipping.");
                                        return null;
                                    }
                                } else {
                                    internalFlags = internalFlags3;
                                    JobInfo.Builder jobBuilder4 = jobBuilder3;
                                    internalFlags2 = uid;
                                    rtcRuntimes = rtcRuntimes2;
                                    int i17 = eventType2;
                                    if (JobStore.XML_TAG_ONEOFF.equals(parser.getName())) {
                                        try {
                                            currentHeartbeat = 0;
                                            if (((Long) elapsedRuntimes2.first).longValue() != 0) {
                                                try {
                                                    jobBuilder = jobBuilder4;
                                                    try {
                                                        jobBuilder.setMinimumLatency(((Long) elapsedRuntimes2.first).longValue() - elapsedNow);
                                                    } catch (NumberFormatException e8) {
                                                        long j3 = elapsedNow;
                                                        int i18 = sourceUserId2;
                                                        Pair<Long, Long> pair6 = rtcRuntimes;
                                                    }
                                                } catch (NumberFormatException e9) {
                                                    JobInfo.Builder builder6 = jobBuilder4;
                                                    long j4 = elapsedNow;
                                                    int i19 = sourceUserId2;
                                                    Pair<Long, Long> pair7 = rtcRuntimes;
                                                    Slog.d(JobStore.TAG, "Error reading job execution criteria, skipping.");
                                                    return null;
                                                }
                                            } else {
                                                jobBuilder = jobBuilder4;
                                            }
                                            try {
                                                if (((Long) elapsedRuntimes2.second).longValue() != JobStatus.NO_LATEST_RUNTIME) {
                                                    jobBuilder.setOverrideDeadline(((Long) elapsedRuntimes2.second).longValue() - elapsedNow);
                                                }
                                                elapsedRuntimes = elapsedRuntimes2;
                                            } catch (NumberFormatException e10) {
                                                long j5 = elapsedNow;
                                                int i20 = sourceUserId2;
                                                Pair<Long, Long> pair8 = rtcRuntimes;
                                                Slog.d(JobStore.TAG, "Error reading job execution criteria, skipping.");
                                                return null;
                                            }
                                        } catch (NumberFormatException e11) {
                                            long j6 = elapsedNow;
                                            int i21 = sourceUserId2;
                                            JobInfo.Builder builder7 = jobBuilder4;
                                            Pair<Long, Long> pair9 = rtcRuntimes;
                                            Slog.d(JobStore.TAG, "Error reading job execution criteria, skipping.");
                                            return null;
                                        }
                                    } else {
                                        int i22 = sourceUserId2;
                                        JobInfo.Builder builder8 = jobBuilder4;
                                        Pair<Long, Long> pair10 = rtcRuntimes;
                                        if (JobStore.DEBUG) {
                                            Slog.d(JobStore.TAG, "Invalid parameter tag, skipping - " + parser.getName());
                                        }
                                        return null;
                                    }
                                }
                                maybeBuildBackoffPolicyFromXml(jobBuilder, xmlPullParser);
                                parser.nextTag();
                                while (true) {
                                    eventType3 = parser.next();
                                    if (eventType3 != 4) {
                                        break;
                                    }
                                    int i23 = eventType3;
                                }
                                if (eventType3 != 2) {
                                    int i24 = sourceUserId2;
                                    Pair<Long, Long> pair11 = rtcRuntimes;
                                } else if (!JobStore.XML_TAG_EXTRAS.equals(parser.getName())) {
                                    long j7 = elapsedNow;
                                    int i25 = sourceUserId2;
                                    Pair<Long, Long> pair12 = rtcRuntimes;
                                } else {
                                    PersistableBundle extras = PersistableBundle.restoreFromXml(parser);
                                    jobBuilder.setExtras(extras);
                                    parser.nextTag();
                                    if (PackageManagerService.PLATFORM_PACKAGE_NAME.equals(sourcePackageName) && extras != null && extras.getBoolean("SyncManagerJob", false)) {
                                        sourcePackageName = extras.getString("owningPackage", sourcePackageName);
                                        if (JobStore.DEBUG) {
                                            Slog.i(JobStore.TAG, "Fixing up sync job source package name from 'android' to '" + sourcePackageName + "'");
                                        }
                                    }
                                    String sourcePackageName2 = sourcePackageName;
                                    JobSchedulerInternal service = (JobSchedulerInternal) LocalServices.getService(JobSchedulerInternal.class);
                                    int sourceUserId3 = sourceUserId2;
                                    int appBucket = JobSchedulerService.standbyBucketForPackage(sourcePackageName2, sourceUserId3, elapsedNow);
                                    if (service != null) {
                                        currentHeartbeat = service.currentHeartbeat();
                                    }
                                    int sourceUserId4 = sourceUserId3;
                                    JobSchedulerInternal jobSchedulerInternal = service;
                                    Pair<Long, Long> pair13 = rtcRuntimes;
                                    PersistableBundle persistableBundle = extras;
                                    long j8 = elapsedNow;
                                    int i26 = sourceUserId4;
                                    JobStatus jobStatus2 = new JobStatus(jobBuilder.build(), internalFlags2, sourcePackageName2, sourceUserId4, appBucket, currentHeartbeat, sourceTag, ((Long) elapsedRuntimes.first).longValue(), ((Long) elapsedRuntimes.second).longValue(), lastSuccessfulRunTime, lastFailedRunTime, rtcIsGood ? null : rtcRuntimes, internalFlags);
                                    return jobStatus2;
                                }
                                if (JobStore.DEBUG) {
                                    Slog.d(JobStore.TAG, "Error reading extras, skipping.");
                                }
                                return null;
                            } catch (NumberFormatException e12) {
                                int i27 = internalFlags3;
                                JobInfo.Builder builder9 = jobBuilder3;
                                int internalFlags12 = uid;
                                int i28 = eventType2;
                                int i29 = sourceUserId;
                                NumberFormatException numberFormatException = e12;
                                if (JobStore.DEBUG) {
                                    Slog.d(JobStore.TAG, "Error parsing execution time parameters, skipping.");
                                }
                                return null;
                            }
                        } catch (NumberFormatException e13) {
                            int i30 = internalFlags3;
                            JobInfo.Builder builder10 = jobBuilder3;
                            int internalFlags13 = uid;
                            int i31 = sourceUserId;
                            JobStatus jobStatus3 = r4;
                            NumberFormatException numberFormatException2 = e13;
                            Slog.d(JobStore.TAG, "Error reading constraints, skipping.");
                            return jobStatus3;
                        }
                    }
                    return jobStatus;
                } catch (NumberFormatException e14) {
                    int i32 = internalFlags3;
                    Slog.e(JobStore.TAG, "Error parsing job's required fields, skipping");
                    return null;
                }
            } catch (NumberFormatException e15) {
                Slog.e(JobStore.TAG, "Error parsing job's required fields, skipping");
                return null;
            }
        }

        private JobInfo.Builder buildBuilderFromXml(XmlPullParser parser) throws NumberFormatException {
            return new JobInfo.Builder(Integer.parseInt(parser.getAttributeValue(null, "jobid")), new ComponentName(parser.getAttributeValue(null, "package"), parser.getAttributeValue(null, AudioService.CONNECT_INTENT_KEY_DEVICE_CLASS)));
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
        }

        private void maybeBuildBackoffPolicyFromXml(JobInfo.Builder jobBuilder, XmlPullParser parser) {
            String val = parser.getAttributeValue(null, "initial-backoff");
            if (val != null) {
                jobBuilder.setBackoffCriteria(Long.parseLong(val), Integer.parseInt(parser.getAttributeValue(null, "backoff-policy")));
            }
        }

        private Pair<Long, Long> buildRtcExecutionTimesFromXml(XmlPullParser parser) throws NumberFormatException {
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

        private Pair<Long, Long> buildExecutionTimesFromXml(XmlPullParser parser) throws NumberFormatException {
            long earliestRunTimeElapsed;
            long earliestRuntimeWallclock;
            XmlPullParser xmlPullParser = parser;
            long nowWallclock = JobSchedulerService.sSystemClock.millis();
            long nowElapsed = JobSchedulerService.sElapsedRealtimeClock.millis();
            long latestRunTimeElapsed = JobStatus.NO_LATEST_RUNTIME;
            String val = xmlPullParser.getAttributeValue(null, "deadline");
            if (val != null) {
                earliestRunTimeElapsed = 0;
                latestRunTimeElapsed = nowElapsed + Math.max(Long.parseLong(val) - nowWallclock, 0);
            } else {
                earliestRunTimeElapsed = 0;
            }
            String val2 = xmlPullParser.getAttributeValue(null, "delay");
            if (val2 != null) {
                earliestRuntimeWallclock = nowElapsed + Math.max(Long.parseLong(val2) - nowWallclock, 0);
            } else {
                earliestRuntimeWallclock = earliestRunTimeElapsed;
            }
            return Pair.create(Long.valueOf(earliestRuntimeWallclock), Long.valueOf(latestRunTimeElapsed));
        }
    }

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

    @VisibleForTesting
    public static JobStore initAndGetForTesting(Context context, File dataDir) {
        JobStore jobStoreUnderTest = new JobStore(context, new Object(), dataDir);
        jobStoreUnderTest.clear();
        return jobStoreUnderTest;
    }

    private JobStore(Context context, Object lock, File dataDir) {
        this.mLock = lock;
        this.mContext = context;
        boolean z = false;
        this.mDirtyOperations = 0;
        File jobDir = new File(new File(dataDir, "system"), "job");
        jobDir.mkdirs();
        this.mJobsFile = new AtomicFile(new File(jobDir, "jobs.xml"), "jobs");
        this.mJobSet = new JobSet();
        this.mXmlTimestamp = this.mJobsFile.getLastModifiedTime();
        this.mRtcGood = JobSchedulerService.sSystemClock.millis() > this.mXmlTimestamp ? true : z;
        readJobMapFromDisk(this.mJobSet, this.mRtcGood);
    }

    public boolean jobTimesInflatedValid() {
        return this.mRtcGood;
    }

    public boolean clockNowValidToInflate(long now) {
        return now >= this.mXmlTimestamp;
    }

    public void getRtcCorrectedJobsLocked(ArrayList<JobStatus> toAdd, ArrayList<JobStatus> toRemove) {
        forEachJob(new Consumer(JobSchedulerService.sElapsedRealtimeClock.millis(), toAdd, toRemove) {
            private final /* synthetic */ long f$0;
            private final /* synthetic */ ArrayList f$1;
            private final /* synthetic */ ArrayList f$2;

            {
                this.f$0 = r1;
                this.f$1 = r3;
                this.f$2 = r4;
            }

            public final void accept(Object obj) {
                JobStore.lambda$getRtcCorrectedJobsLocked$0(this.f$0, this.f$1, this.f$2, (JobStatus) obj);
            }
        });
    }

    static /* synthetic */ void lambda$getRtcCorrectedJobsLocked$0(long elapsedNow, ArrayList toAdd, ArrayList toRemove, JobStatus job) {
        Pair<Long, Long> utcTimes = job.getPersistedUtcTimes();
        if (utcTimes != null) {
            Pair<Long, Long> elapsedRuntimes = convertRtcBoundsToElapsed(utcTimes, elapsedNow);
            Pair<Long, Long> pair = utcTimes;
            JobStatus jobStatus = r4;
            JobStatus jobStatus2 = new JobStatus(job, job.getBaseHeartbeat(), ((Long) elapsedRuntimes.first).longValue(), ((Long) elapsedRuntimes.second).longValue(), 0, job.getLastSuccessfulRunTime(), job.getLastFailedRunTime());
            toAdd.add(jobStatus);
            toRemove.add(job);
            return;
        }
        long j = elapsedNow;
        ArrayList arrayList = toAdd;
        Pair<Long, Long> pair2 = utcTimes;
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

    /* access modifiers changed from: package-private */
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
        if (!removed) {
            if (DEBUG) {
                Slog.d(TAG, "Couldn't remove job: didn't exist: " + jobStatus);
            }
            return false;
        }
        if (writeBack && jobStatus.isPersisted()) {
            maybeWriteStatusToDiskAsync();
        }
        return removed;
    }

    public void removeJobsOfNonUsers(int[] whitelist) {
        this.mJobSet.removeJobsOfNonUsers(whitelist);
    }

    @VisibleForTesting
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
        this.mDirtyOperations++;
        if (this.mDirtyOperations >= 1) {
            if (DEBUG) {
                Slog.v(TAG, "Writing jobs to disk.");
            }
            this.mIoHandler.removeCallbacks(this.mWriteRunnable);
            this.mIoHandler.post(this.mWriteRunnable);
        }
    }

    @VisibleForTesting
    public void readJobMapFromDisk(JobSet jobSet, boolean rtcGood) {
        new ReadJobMapFromDiskRunnable(jobSet, rtcGood).run();
    }

    /* access modifiers changed from: private */
    public static Pair<Long, Long> convertRtcBoundsToElapsed(Pair<Long, Long> rtcTimes, long nowElapsed) {
        long earliest;
        long nowWallclock = JobSchedulerService.sSystemClock.millis();
        if (((Long) rtcTimes.first).longValue() > 0) {
            earliest = Math.max(((Long) rtcTimes.first).longValue() - nowWallclock, 0) + nowElapsed;
        } else {
            earliest = 0;
        }
        long longValue = ((Long) rtcTimes.second).longValue();
        long latest = JobStatus.NO_LATEST_RUNTIME;
        if (longValue < JobStatus.NO_LATEST_RUNTIME) {
            latest = nowElapsed + Math.max(((Long) rtcTimes.second).longValue() - nowWallclock, 0);
        }
        return Pair.create(Long.valueOf(earliest), Long.valueOf(latest));
    }

    /* access modifiers changed from: private */
    public static boolean isSyncJob(JobStatus status) {
        return SyncJobService.class.getName().equals(status.getServiceComponent().getClassName());
    }
}

package com.android.server.job;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobInfo.Builder;
import android.content.ComponentName;
import android.content.Context;
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
import com.android.internal.util.FastXmlSerializer;
import com.android.server.IoThread;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.audio.AudioService;
import com.android.server.job.controllers.JobStatus;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public final class JobStore {
    private static final boolean DEBUG = false;
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
    private int mDirtyOperations;
    private final Handler mIoHandler = IoThread.getHandler();
    final JobSet mJobSet;
    private final AtomicFile mJobsFile;
    final Object mLock;

    public interface JobStatusFunctor {
        void process(JobStatus jobStatus);
    }

    static final class JobSet {
        private SparseArray<ArraySet<JobStatus>> mJobs = new SparseArray();

        public List<JobStatus> getJobsByUid(int uid) {
            ArrayList<JobStatus> matchingJobs = new ArrayList();
            ArraySet<JobStatus> jobs = (ArraySet) this.mJobs.get(uid);
            if (jobs != null) {
                matchingJobs.addAll(jobs);
            }
            return matchingJobs;
        }

        public List<JobStatus> getJobsByUser(int userId) {
            ArrayList<JobStatus> result = new ArrayList();
            for (int i = this.mJobs.size() - 1; i >= 0; i--) {
                if (UserHandle.getUserId(this.mJobs.keyAt(i)) == userId) {
                    ArraySet<JobStatus> jobs = (ArraySet) this.mJobs.valueAt(i);
                    if (jobs != null) {
                        result.addAll(jobs);
                    }
                }
            }
            return result;
        }

        public boolean add(JobStatus job) {
            int uid = job.getUid();
            ArraySet<JobStatus> jobs = (ArraySet) this.mJobs.get(uid);
            if (jobs == null) {
                jobs = new ArraySet();
                this.mJobs.put(uid, jobs);
            }
            return jobs.add(job);
        }

        public boolean remove(JobStatus job) {
            int uid = job.getUid();
            ArraySet<JobStatus> jobs = (ArraySet) this.mJobs.get(uid);
            boolean didRemove = jobs != null ? jobs.remove(job) : false;
            if (didRemove && jobs.size() == 0) {
                this.mJobs.remove(uid);
            }
            return didRemove;
        }

        public void removeJobsOfNonUsers(int[] whitelist) {
            for (int jobIndex = this.mJobs.size() - 1; jobIndex >= 0; jobIndex--) {
                if (!ArrayUtils.contains(whitelist, UserHandle.getUserId(this.mJobs.keyAt(jobIndex)))) {
                    this.mJobs.removeAt(jobIndex);
                }
            }
        }

        public boolean contains(JobStatus job) {
            ArraySet<JobStatus> jobs = (ArraySet) this.mJobs.get(job.getUid());
            return jobs != null ? jobs.contains(job) : false;
        }

        public JobStatus get(int uid, int jobId) {
            ArraySet<JobStatus> jobs = (ArraySet) this.mJobs.get(uid);
            if (jobs != null) {
                for (int i = jobs.size() - 1; i >= 0; i--) {
                    JobStatus job = (JobStatus) jobs.valueAt(i);
                    if (job.getJobId() == jobId) {
                        return job;
                    }
                }
            }
            return null;
        }

        public List<JobStatus> getAllJobs() {
            ArrayList<JobStatus> allJobs = new ArrayList(size());
            for (int i = this.mJobs.size() - 1; i >= 0; i--) {
                ArraySet<JobStatus> jobs = (ArraySet) this.mJobs.valueAt(i);
                if (jobs != null) {
                    for (int j = jobs.size() - 1; j >= 0; j--) {
                        allJobs.add((JobStatus) jobs.valueAt(j));
                    }
                }
            }
            return allJobs;
        }

        public void clear() {
            this.mJobs.clear();
        }

        public int size() {
            int total = 0;
            for (int i = this.mJobs.size() - 1; i >= 0; i--) {
                total += ((ArraySet) this.mJobs.valueAt(i)).size();
            }
            return total;
        }

        public int countJobsForUid(int uid) {
            int total = 0;
            ArraySet<JobStatus> jobs = (ArraySet) this.mJobs.get(uid);
            if (jobs != null) {
                for (int i = jobs.size() - 1; i >= 0; i--) {
                    JobStatus job = (JobStatus) jobs.valueAt(i);
                    if (job.getUid() == job.getSourceUid()) {
                        total++;
                    }
                }
            }
            return total;
        }

        public void forEachJob(JobStatusFunctor functor) {
            for (int uidIndex = this.mJobs.size() - 1; uidIndex >= 0; uidIndex--) {
                ArraySet<JobStatus> jobs = (ArraySet) this.mJobs.valueAt(uidIndex);
                for (int i = jobs.size() - 1; i >= 0; i--) {
                    functor.process((JobStatus) jobs.valueAt(i));
                }
            }
        }

        public void forEachJob(int uid, JobStatusFunctor functor) {
            ArraySet<JobStatus> jobs = (ArraySet) this.mJobs.get(uid);
            if (jobs != null) {
                for (int i = jobs.size() - 1; i >= 0; i--) {
                    functor.process((JobStatus) jobs.valueAt(i));
                }
            }
        }
    }

    private final class ReadJobMapFromDiskRunnable implements Runnable {
        private final JobSet jobSet;

        ReadJobMapFromDiskRunnable(JobSet jobSet) {
            this.jobSet = jobSet;
        }

        public void run() {
            try {
                FileInputStream fis = JobStore.this.mJobsFile.openRead();
                synchronized (JobStore.this.mLock) {
                    List<JobStatus> jobs = readJobMapImpl(fis);
                    if (jobs != null) {
                        long now = SystemClock.elapsedRealtime();
                        IActivityManager am = ActivityManager.getService();
                        for (int i = 0; i < jobs.size(); i++) {
                            JobStatus js = (JobStatus) jobs.get(i);
                            js.prepareLocked(am);
                            js.enqueueTime = now;
                            this.jobSet.add(js);
                        }
                    }
                }
                fis.close();
            } catch (FileNotFoundException e) {
            } catch (XmlPullParserException e2) {
            } catch (IOException e3) {
            }
        }

        private List<JobStatus> readJobMapImpl(FileInputStream fis) throws XmlPullParserException, IOException {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(fis, StandardCharsets.UTF_8.name());
            int eventType = parser.getEventType();
            while (eventType != 2 && eventType != 1) {
                eventType = parser.next();
                Slog.d(JobStore.TAG, "Start tag: " + parser.getName());
            }
            if (eventType == 1) {
                return null;
            }
            if (!"job-info".equals(parser.getName())) {
                return null;
            }
            List<JobStatus> jobs = new ArrayList();
            try {
                if (Integer.parseInt(parser.getAttributeValue(null, "version")) != 0) {
                    Slog.d(JobStore.TAG, "Invalid version number, aborting jobs file read.");
                    return null;
                }
                eventType = parser.next();
                do {
                    if (eventType == 2) {
                        if ("job".equals(parser.getName())) {
                            JobStatus persistedJob = restoreJobFromXml(parser);
                            if (persistedJob != null) {
                                jobs.add(persistedJob);
                            } else {
                                Slog.d(JobStore.TAG, "Error reading job from file.");
                            }
                        }
                    }
                    eventType = parser.next();
                } while (eventType != 1);
                return jobs;
            } catch (NumberFormatException e) {
                Slog.e(JobStore.TAG, "Invalid version number, aborting jobs file read.");
                return null;
            }
        }

        private JobStatus restoreJobFromXml(XmlPullParser parser) throws XmlPullParserException, IOException {
            try {
                int eventType;
                boolean equals;
                Builder jobBuilder = buildBuilderFromXml(parser);
                jobBuilder.setPersisted(true);
                int uid = Integer.parseInt(parser.getAttributeValue(null, "uid"));
                String val = parser.getAttributeValue(null, "priority");
                if (val != null) {
                    jobBuilder.setPriority(Integer.parseInt(val));
                }
                val = parser.getAttributeValue(null, "flags");
                if (val != null) {
                    jobBuilder.setFlags(Integer.parseInt(val));
                }
                val = parser.getAttributeValue(null, "sourceUserId");
                int sourceUserId = val == null ? -1 : Integer.parseInt(val);
                String sourcePackageName = parser.getAttributeValue(null, "sourcePackageName");
                String sourceTag = parser.getAttributeValue(null, "sourceTag");
                do {
                    eventType = parser.next();
                } while (eventType == 4);
                if (eventType == 2) {
                    equals = JobStore.XML_TAG_PARAMS_CONSTRAINTS.equals(parser.getName());
                } else {
                    equals = false;
                }
                if (!equals) {
                    return null;
                }
                try {
                    buildConstraintsFromXml(jobBuilder, parser);
                    parser.next();
                    do {
                        eventType = parser.next();
                    } while (eventType == 4);
                    if (eventType != 2) {
                        return null;
                    }
                    try {
                        Pair<Long, Long> elapsedRuntimes = buildExecutionTimesFromXml(parser);
                        long elapsedNow = SystemClock.elapsedRealtime();
                        if (JobStore.XML_TAG_PERIODIC.equals(parser.getName())) {
                            try {
                                long periodMillis = Long.parseLong(parser.getAttributeValue(null, "period"));
                                val = parser.getAttributeValue(null, "flex");
                                long flexMillis = val != null ? Long.valueOf(val).longValue() : periodMillis;
                                jobBuilder.setPeriodic(periodMillis, flexMillis);
                                if (((Long) elapsedRuntimes.second).longValue() > (elapsedNow + periodMillis) + flexMillis) {
                                    long clampedLateRuntimeElapsed = (elapsedNow + flexMillis) + periodMillis;
                                    Slog.w(JobStore.TAG, String.format("Periodic job for uid='%d' persisted run-time is too big [%s, %s]. Clamping to [%s,%s]", new Object[]{Integer.valueOf(uid), DateUtils.formatElapsedTime(((Long) elapsedRuntimes.first).longValue() / 1000), DateUtils.formatElapsedTime(((Long) elapsedRuntimes.second).longValue() / 1000), DateUtils.formatElapsedTime((clampedLateRuntimeElapsed - flexMillis) / 1000), DateUtils.formatElapsedTime(clampedLateRuntimeElapsed / 1000)}));
                                    elapsedRuntimes = Pair.create(Long.valueOf(clampedEarlyRuntimeElapsed), Long.valueOf(clampedLateRuntimeElapsed));
                                }
                            } catch (NumberFormatException e) {
                                Slog.d(JobStore.TAG, "Error reading periodic execution criteria, skipping.");
                                return null;
                            }
                        } else if (!JobStore.XML_TAG_ONEOFF.equals(parser.getName())) {
                            return null;
                        } else {
                            try {
                                if (((Long) elapsedRuntimes.first).longValue() != 0) {
                                    jobBuilder.setMinimumLatency(((Long) elapsedRuntimes.first).longValue() - elapsedNow);
                                }
                                if (((Long) elapsedRuntimes.second).longValue() != JobStatus.NO_LATEST_RUNTIME) {
                                    jobBuilder.setOverrideDeadline(((Long) elapsedRuntimes.second).longValue() - elapsedNow);
                                }
                            } catch (NumberFormatException e2) {
                                Slog.d(JobStore.TAG, "Error reading job execution criteria, skipping.");
                                return null;
                            }
                        }
                        maybeBuildBackoffPolicyFromXml(jobBuilder, parser);
                        parser.nextTag();
                        do {
                            eventType = parser.next();
                        } while (eventType == 4);
                        if (eventType == 2) {
                            equals = JobStore.XML_TAG_EXTRAS.equals(parser.getName());
                        } else {
                            equals = false;
                        }
                        if (!equals) {
                            return null;
                        }
                        PersistableBundle extras = PersistableBundle.restoreFromXml(parser);
                        jobBuilder.setExtras(extras);
                        parser.nextTag();
                        if ("android".equals(sourcePackageName) && extras != null) {
                            if (extras.getBoolean("SyncManagerJob", false)) {
                                sourcePackageName = extras.getString("owningPackage", sourcePackageName);
                            }
                        }
                        return new JobStatus(jobBuilder.build(), uid, sourcePackageName, sourceUserId, sourceTag, ((Long) elapsedRuntimes.first).longValue(), ((Long) elapsedRuntimes.second).longValue());
                    } catch (NumberFormatException e3) {
                        return null;
                    }
                } catch (NumberFormatException e4) {
                    Slog.d(JobStore.TAG, "Error reading constraints, skipping.");
                    return null;
                }
            } catch (NumberFormatException e5) {
                Slog.e(JobStore.TAG, "Error parsing job's required fields, skipping");
                return null;
            }
        }

        private Builder buildBuilderFromXml(XmlPullParser parser) throws NumberFormatException {
            return new Builder(Integer.parseInt(parser.getAttributeValue(null, "jobid")), new ComponentName(parser.getAttributeValue(null, HwBroadcastRadarUtil.KEY_PACKAGE), parser.getAttributeValue(null, AudioService.CONNECT_INTENT_KEY_DEVICE_CLASS)));
        }

        private void buildConstraintsFromXml(Builder jobBuilder, XmlPullParser parser) {
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
            if (parser.getAttributeValue(null, "idle") != null) {
                jobBuilder.setRequiresDeviceIdle(true);
            }
            if (parser.getAttributeValue(null, "charging") != null) {
                jobBuilder.setRequiresCharging(true);
            }
        }

        private void maybeBuildBackoffPolicyFromXml(Builder jobBuilder, XmlPullParser parser) {
            String val = parser.getAttributeValue(null, "initial-backoff");
            if (val != null) {
                jobBuilder.setBackoffCriteria(Long.parseLong(val), Integer.parseInt(parser.getAttributeValue(null, "backoff-policy")));
            }
        }

        private Pair<Long, Long> buildExecutionTimesFromXml(XmlPullParser parser) throws NumberFormatException {
            long nowWallclock = System.currentTimeMillis();
            long nowElapsed = SystemClock.elapsedRealtime();
            long earliestRunTimeElapsed = 0;
            long latestRunTimeElapsed = JobStatus.NO_LATEST_RUNTIME;
            String val = parser.getAttributeValue(null, "deadline");
            if (val != null) {
                latestRunTimeElapsed = nowElapsed + Math.max(Long.parseLong(val) - nowWallclock, 0);
            }
            val = parser.getAttributeValue(null, "delay");
            if (val != null) {
                earliestRunTimeElapsed = nowElapsed + Math.max(Long.parseLong(val) - nowWallclock, 0);
            }
            return Pair.create(Long.valueOf(earliestRunTimeElapsed), Long.valueOf(latestRunTimeElapsed));
        }
    }

    private final class WriteJobsMapToDiskRunnable implements Runnable {
        /* synthetic */ WriteJobsMapToDiskRunnable(JobStore this$0, WriteJobsMapToDiskRunnable -this1) {
            this();
        }

        private WriteJobsMapToDiskRunnable() {
        }

        public void run() {
            long startElapsed = SystemClock.elapsedRealtime();
            final List<JobStatus> storeCopy = new ArrayList();
            synchronized (JobStore.this.mLock) {
                JobStore.this.mJobSet.forEachJob(new JobStatusFunctor() {
                    public void process(JobStatus job) {
                        if (job.isPersisted()) {
                            storeCopy.add(new JobStatus(job));
                        }
                    }
                });
            }
            writeJobsMapImpl(storeCopy);
        }

        private void writeJobsMapImpl(List<JobStatus> jobList) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(baos, StandardCharsets.UTF_8.name());
                out.startDocument(null, Boolean.valueOf(true));
                out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                out.startTag(null, "job-info");
                out.attribute(null, "version", Integer.toString(0));
                for (int i = 0; i < jobList.size(); i++) {
                    JobStatus jobStatus = (JobStatus) jobList.get(i);
                    out.startTag(null, "job");
                    addAttributesToJobTag(out, jobStatus);
                    writeConstraintsToXml(out, jobStatus);
                    writeExecutionCriteriaToXml(out, jobStatus);
                    writeBundleToXml(jobStatus.getJob().getExtras(), out);
                    out.endTag(null, "job");
                }
                out.endTag(null, "job-info");
                out.endDocument();
                FileOutputStream fos = JobStore.this.mJobsFile.startWrite();
                fos.write(baos.toByteArray());
                JobStore.this.mJobsFile.finishWrite(fos);
                JobStore.this.mDirtyOperations = 0;
            } catch (IOException e) {
            } catch (XmlPullParserException e2) {
            }
        }

        private void addAttributesToJobTag(XmlSerializer out, JobStatus jobStatus) throws IOException {
            out.attribute(null, "jobid", Integer.toString(jobStatus.getJobId()));
            out.attribute(null, HwBroadcastRadarUtil.KEY_PACKAGE, jobStatus.getServiceComponent().getPackageName());
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
            if (jobStatus.needsAnyConnectivity()) {
                out.attribute(null, "connectivity", Boolean.toString(true));
            }
            if (jobStatus.needsMeteredConnectivity()) {
                out.attribute(null, "metered", Boolean.toString(true));
            }
            if (jobStatus.needsUnmeteredConnectivity()) {
                out.attribute(null, "unmetered", Boolean.toString(true));
            }
            if (jobStatus.needsNonRoamingConnectivity()) {
                out.attribute(null, "not-roaming", Boolean.toString(true));
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
            JobInfo job = jobStatus.getJob();
            if (jobStatus.getJob().isPeriodic()) {
                out.startTag(null, JobStore.XML_TAG_PERIODIC);
                out.attribute(null, "period", Long.toString(job.getIntervalMillis()));
                out.attribute(null, "flex", Long.toString(job.getFlexMillis()));
            } else {
                out.startTag(null, JobStore.XML_TAG_ONEOFF);
            }
            if (jobStatus.hasDeadlineConstraint()) {
                out.attribute(null, "deadline", Long.toString(System.currentTimeMillis() + (jobStatus.getLatestRunTimeElapsed() - SystemClock.elapsedRealtime())));
            }
            if (jobStatus.hasTimingDelayConstraint()) {
                out.attribute(null, "delay", Long.toString(System.currentTimeMillis() + (jobStatus.getEarliestRunTime() - SystemClock.elapsedRealtime())));
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

    public static JobStore initAndGetForTesting(Context context, File dataDir) {
        JobStore jobStoreUnderTest = new JobStore(context, new Object(), dataDir);
        jobStoreUnderTest.clear();
        return jobStoreUnderTest;
    }

    private JobStore(Context context, Object lock, File dataDir) {
        this.mLock = lock;
        this.mContext = context;
        this.mDirtyOperations = 0;
        File jobDir = new File(new File(dataDir, "system"), "job");
        jobDir.mkdirs();
        this.mJobsFile = new AtomicFile(new File(jobDir, "jobs.xml"));
        this.mJobSet = new JobSet();
        readJobMapFromDisk(this.mJobSet);
    }

    public boolean add(JobStatus jobStatus) {
        boolean replaced = this.mJobSet.remove(jobStatus);
        this.mJobSet.add(jobStatus);
        if (jobStatus.isPersisted()) {
            maybeWriteStatusToDiskAsync();
        }
        return replaced;
    }

    boolean containsJob(JobStatus jobStatus) {
        return this.mJobSet.contains(jobStatus);
    }

    public int size() {
        return this.mJobSet.size();
    }

    public int countJobsForUid(int uid) {
        return this.mJobSet.countJobsForUid(uid);
    }

    public boolean remove(JobStatus jobStatus, boolean writeBack) {
        boolean removed = this.mJobSet.remove(jobStatus);
        if (!removed) {
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

    public void forEachJob(JobStatusFunctor functor) {
        this.mJobSet.forEachJob(functor);
    }

    public void forEachJob(int uid, JobStatusFunctor functor) {
        this.mJobSet.forEachJob(uid, functor);
    }

    private void maybeWriteStatusToDiskAsync() {
        this.mDirtyOperations++;
        if (this.mDirtyOperations >= 1) {
            this.mIoHandler.post(new WriteJobsMapToDiskRunnable(this, null));
        }
    }

    public void readJobMapFromDisk(JobSet jobSet) {
        new ReadJobMapFromDiskRunnable(jobSet).run();
    }
}

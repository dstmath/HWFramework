package com.android.server.job;

import android.os.SystemClock;
import android.os.UserHandle;
import android.text.format.DateFormat;
import android.util.ArrayMap;
import android.util.SparseArray;
import android.util.TimeUtils;
import com.android.internal.util.RingBufferIndices;
import com.android.server.job.controllers.JobStatus;
import java.io.PrintWriter;

public final class JobPackageTracker {
    static final long BATCHING_TIME = 1800000;
    private static final int EVENT_BUFFER_SIZE = 100;
    public static final int EVENT_NULL = 0;
    public static final int EVENT_START_JOB = 1;
    public static final int EVENT_STOP_JOB = 2;
    static final int NUM_HISTORY = 5;
    DataSet mCurDataSet;
    private final int[] mEventCmds;
    private final RingBufferIndices mEventIndices;
    private final String[] mEventTags;
    private final long[] mEventTimes;
    private final int[] mEventUids;
    DataSet[] mLastDataSets;

    static final class DataSet {
        final SparseArray<ArrayMap<String, PackageEntry>> mEntries;
        int mMaxFgActive;
        int mMaxTotalActive;
        final long mStartClockTime;
        final long mStartElapsedTime;
        final long mStartUptimeTime;
        long mSummedTime;

        public DataSet(DataSet otherTimes) {
            this.mEntries = new SparseArray();
            this.mStartUptimeTime = otherTimes.mStartUptimeTime;
            this.mStartElapsedTime = otherTimes.mStartElapsedTime;
            this.mStartClockTime = otherTimes.mStartClockTime;
        }

        public DataSet() {
            this.mEntries = new SparseArray();
            this.mStartUptimeTime = SystemClock.uptimeMillis();
            this.mStartElapsedTime = SystemClock.elapsedRealtime();
            this.mStartClockTime = System.currentTimeMillis();
        }

        private PackageEntry getOrCreateEntry(int uid, String pkg) {
            ArrayMap<String, PackageEntry> uidMap = (ArrayMap) this.mEntries.get(uid);
            if (uidMap == null) {
                uidMap = new ArrayMap();
                this.mEntries.put(uid, uidMap);
            }
            PackageEntry entry = (PackageEntry) uidMap.get(pkg);
            if (entry != null) {
                return entry;
            }
            entry = new PackageEntry();
            uidMap.put(pkg, entry);
            return entry;
        }

        public PackageEntry getEntry(int uid, String pkg) {
            ArrayMap<String, PackageEntry> uidMap = (ArrayMap) this.mEntries.get(uid);
            if (uidMap == null) {
                return null;
            }
            return (PackageEntry) uidMap.get(pkg);
        }

        long getTotalTime(long now) {
            if (this.mSummedTime > 0) {
                return this.mSummedTime;
            }
            return now - this.mStartUptimeTime;
        }

        void incPending(int uid, String pkg, long now) {
            PackageEntry pe = getOrCreateEntry(uid, pkg);
            if (pe.pendingNesting == 0) {
                pe.pendingStartTime = now;
                pe.pendingCount += JobPackageTracker.EVENT_START_JOB;
            }
            pe.pendingNesting += JobPackageTracker.EVENT_START_JOB;
        }

        void decPending(int uid, String pkg, long now) {
            PackageEntry pe = getOrCreateEntry(uid, pkg);
            if (pe.pendingNesting == JobPackageTracker.EVENT_START_JOB) {
                pe.pastPendingTime += now - pe.pendingStartTime;
            }
            pe.pendingNesting--;
        }

        void incActive(int uid, String pkg, long now) {
            PackageEntry pe = getOrCreateEntry(uid, pkg);
            if (pe.activeNesting == 0) {
                pe.activeStartTime = now;
                pe.activeCount += JobPackageTracker.EVENT_START_JOB;
            }
            pe.activeNesting += JobPackageTracker.EVENT_START_JOB;
        }

        void decActive(int uid, String pkg, long now) {
            PackageEntry pe = getOrCreateEntry(uid, pkg);
            if (pe.activeNesting == JobPackageTracker.EVENT_START_JOB) {
                pe.pastActiveTime += now - pe.activeStartTime;
            }
            pe.activeNesting--;
        }

        void incActiveTop(int uid, String pkg, long now) {
            PackageEntry pe = getOrCreateEntry(uid, pkg);
            if (pe.activeTopNesting == 0) {
                pe.activeTopStartTime = now;
                pe.activeTopCount += JobPackageTracker.EVENT_START_JOB;
            }
            pe.activeTopNesting += JobPackageTracker.EVENT_START_JOB;
        }

        void decActiveTop(int uid, String pkg, long now) {
            PackageEntry pe = getOrCreateEntry(uid, pkg);
            if (pe.activeTopNesting == JobPackageTracker.EVENT_START_JOB) {
                pe.pastActiveTopTime += now - pe.activeTopStartTime;
            }
            pe.activeTopNesting--;
        }

        void finish(DataSet next, long now) {
            for (int i = this.mEntries.size() - 1; i >= 0; i--) {
                ArrayMap<String, PackageEntry> uidMap = (ArrayMap) this.mEntries.valueAt(i);
                for (int j = uidMap.size() - 1; j >= 0; j--) {
                    PackageEntry pe = (PackageEntry) uidMap.valueAt(j);
                    if (pe.activeNesting <= 0 && pe.activeTopNesting <= 0) {
                        if (pe.pendingNesting <= 0) {
                        }
                    }
                    PackageEntry nextPe = next.getOrCreateEntry(this.mEntries.keyAt(i), (String) uidMap.keyAt(j));
                    nextPe.activeStartTime = now;
                    nextPe.activeNesting = pe.activeNesting;
                    nextPe.activeTopStartTime = now;
                    nextPe.activeTopNesting = pe.activeTopNesting;
                    nextPe.pendingStartTime = now;
                    nextPe.pendingNesting = pe.pendingNesting;
                    if (pe.activeNesting > 0) {
                        pe.pastActiveTime += now - pe.activeStartTime;
                        pe.activeNesting = JobPackageTracker.EVENT_NULL;
                    }
                    if (pe.activeTopNesting > 0) {
                        pe.pastActiveTopTime += now - pe.activeTopStartTime;
                        pe.activeTopNesting = JobPackageTracker.EVENT_NULL;
                    }
                    if (pe.pendingNesting > 0) {
                        pe.pastPendingTime += now - pe.pendingStartTime;
                        pe.pendingNesting = JobPackageTracker.EVENT_NULL;
                    }
                }
            }
        }

        void addTo(DataSet out, long now) {
            out.mSummedTime += getTotalTime(now);
            for (int i = this.mEntries.size() - 1; i >= 0; i--) {
                ArrayMap<String, PackageEntry> uidMap = (ArrayMap) this.mEntries.valueAt(i);
                for (int j = uidMap.size() - 1; j >= 0; j--) {
                    PackageEntry pe = (PackageEntry) uidMap.valueAt(j);
                    PackageEntry outPe = out.getOrCreateEntry(this.mEntries.keyAt(i), (String) uidMap.keyAt(j));
                    outPe.pastActiveTime += pe.pastActiveTime;
                    outPe.activeCount += pe.activeCount;
                    outPe.pastActiveTopTime += pe.pastActiveTopTime;
                    outPe.activeTopCount += pe.activeTopCount;
                    outPe.pastPendingTime += pe.pastPendingTime;
                    outPe.pendingCount += pe.pendingCount;
                    if (pe.activeNesting > 0) {
                        outPe.pastActiveTime += now - pe.activeStartTime;
                        outPe.hadActive = true;
                    }
                    if (pe.activeTopNesting > 0) {
                        outPe.pastActiveTopTime += now - pe.activeTopStartTime;
                        outPe.hadActiveTop = true;
                    }
                    if (pe.pendingNesting > 0) {
                        outPe.pastPendingTime += now - pe.pendingStartTime;
                        outPe.hadPending = true;
                    }
                }
            }
            if (this.mMaxTotalActive > out.mMaxTotalActive) {
                out.mMaxTotalActive = this.mMaxTotalActive;
            }
            if (this.mMaxFgActive > out.mMaxFgActive) {
                out.mMaxFgActive = this.mMaxFgActive;
            }
        }

        void printDuration(PrintWriter pw, long period, long duration, int count, String suffix) {
            int percent = (int) ((100.0f * (((float) duration) / ((float) period))) + TaskPositioner.RESIZING_HINT_ALPHA);
            if (percent > 0) {
                pw.print(" ");
                pw.print(percent);
                pw.print("% ");
                pw.print(count);
                pw.print("x ");
                pw.print(suffix);
            } else if (count > 0) {
                pw.print(" ");
                pw.print(count);
                pw.print("x ");
                pw.print(suffix);
            }
        }

        void dump(PrintWriter pw, String header, String prefix, long now, long nowEllapsed, int filterUid) {
            long period = getTotalTime(now);
            pw.print(prefix);
            pw.print(header);
            pw.print(" at ");
            pw.print(DateFormat.format("yyyy-MM-dd-HH-mm-ss", this.mStartClockTime).toString());
            pw.print(" (");
            TimeUtils.formatDuration(this.mStartElapsedTime, nowEllapsed, pw);
            pw.print(") over ");
            TimeUtils.formatDuration(period, pw);
            pw.println(":");
            int NE = this.mEntries.size();
            for (int i = JobPackageTracker.EVENT_NULL; i < NE; i += JobPackageTracker.EVENT_START_JOB) {
                int uid = this.mEntries.keyAt(i);
                if (filterUid == -1 || filterUid == UserHandle.getAppId(uid)) {
                    ArrayMap<String, PackageEntry> uidMap = (ArrayMap) this.mEntries.valueAt(i);
                    int NP = uidMap.size();
                    for (int j = JobPackageTracker.EVENT_NULL; j < NP; j += JobPackageTracker.EVENT_START_JOB) {
                        PackageEntry pe = (PackageEntry) uidMap.valueAt(j);
                        pw.print(prefix);
                        pw.print("  ");
                        UserHandle.formatUid(pw, uid);
                        pw.print(" / ");
                        pw.print((String) uidMap.keyAt(j));
                        pw.print(":");
                        printDuration(pw, period, pe.getPendingTime(now), pe.pendingCount, "pending");
                        printDuration(pw, period, pe.getActiveTime(now), pe.activeCount, "active");
                        printDuration(pw, period, pe.getActiveTopTime(now), pe.activeTopCount, "active-top");
                        if (pe.pendingNesting > 0 || pe.hadPending) {
                            pw.print(" (pending)");
                        }
                        if (pe.activeNesting > 0 || pe.hadActive) {
                            pw.print(" (active)");
                        }
                        if (pe.activeTopNesting > 0 || pe.hadActiveTop) {
                            pw.print(" (active-top)");
                        }
                        pw.println();
                    }
                }
            }
            pw.print(prefix);
            pw.print("  Max concurrency: ");
            pw.print(this.mMaxTotalActive);
            pw.print(" total, ");
            pw.print(this.mMaxFgActive);
            pw.println(" foreground");
        }
    }

    static final class PackageEntry {
        int activeCount;
        int activeNesting;
        long activeStartTime;
        int activeTopCount;
        int activeTopNesting;
        long activeTopStartTime;
        boolean hadActive;
        boolean hadActiveTop;
        boolean hadPending;
        long pastActiveTime;
        long pastActiveTopTime;
        long pastPendingTime;
        int pendingCount;
        int pendingNesting;
        long pendingStartTime;

        PackageEntry() {
        }

        public long getActiveTime(long now) {
            long time = this.pastActiveTime;
            if (this.activeNesting > 0) {
                return time + (now - this.activeStartTime);
            }
            return time;
        }

        public long getActiveTopTime(long now) {
            long time = this.pastActiveTopTime;
            if (this.activeTopNesting > 0) {
                return time + (now - this.activeTopStartTime);
            }
            return time;
        }

        public long getPendingTime(long now) {
            long time = this.pastPendingTime;
            if (this.pendingNesting > 0) {
                return time + (now - this.pendingStartTime);
            }
            return time;
        }
    }

    public JobPackageTracker() {
        this.mEventIndices = new RingBufferIndices(EVENT_BUFFER_SIZE);
        this.mEventCmds = new int[EVENT_BUFFER_SIZE];
        this.mEventTimes = new long[EVENT_BUFFER_SIZE];
        this.mEventUids = new int[EVENT_BUFFER_SIZE];
        this.mEventTags = new String[EVENT_BUFFER_SIZE];
        this.mCurDataSet = new DataSet();
        this.mLastDataSets = new DataSet[NUM_HISTORY];
    }

    public void addEvent(int cmd, int uid, String tag) {
        int index = this.mEventIndices.add();
        this.mEventCmds[index] = cmd;
        this.mEventTimes[index] = SystemClock.elapsedRealtime();
        this.mEventUids[index] = uid;
        this.mEventTags[index] = tag;
    }

    void rebatchIfNeeded(long now) {
        long totalTime = this.mCurDataSet.getTotalTime(now);
        if (totalTime > BATCHING_TIME) {
            DataSet last = this.mCurDataSet;
            last.mSummedTime = totalTime;
            this.mCurDataSet = new DataSet();
            last.finish(this.mCurDataSet, now);
            System.arraycopy(this.mLastDataSets, EVENT_NULL, this.mLastDataSets, EVENT_START_JOB, this.mLastDataSets.length - 1);
            this.mLastDataSets[EVENT_NULL] = last;
        }
    }

    public void notePending(JobStatus job) {
        long now = SystemClock.uptimeMillis();
        rebatchIfNeeded(now);
        this.mCurDataSet.incPending(job.getSourceUid(), job.getSourcePackageName(), now);
    }

    public void noteNonpending(JobStatus job) {
        long now = SystemClock.uptimeMillis();
        this.mCurDataSet.decPending(job.getSourceUid(), job.getSourcePackageName(), now);
        rebatchIfNeeded(now);
    }

    public void noteActive(JobStatus job) {
        long now = SystemClock.uptimeMillis();
        rebatchIfNeeded(now);
        if (job.lastEvaluatedPriority >= 40) {
            this.mCurDataSet.incActiveTop(job.getSourceUid(), job.getSourcePackageName(), now);
        } else {
            this.mCurDataSet.incActive(job.getSourceUid(), job.getSourcePackageName(), now);
        }
        addEvent(EVENT_START_JOB, job.getSourceUid(), job.getBatteryName());
    }

    public void noteInactive(JobStatus job) {
        long now = SystemClock.uptimeMillis();
        if (job.lastEvaluatedPriority >= 40) {
            this.mCurDataSet.decActiveTop(job.getSourceUid(), job.getSourcePackageName(), now);
        } else {
            this.mCurDataSet.decActive(job.getSourceUid(), job.getSourcePackageName(), now);
        }
        rebatchIfNeeded(now);
        addEvent(EVENT_STOP_JOB, job.getSourceUid(), job.getBatteryName());
    }

    public void noteConcurrency(int totalActive, int fgActive) {
        if (totalActive > this.mCurDataSet.mMaxTotalActive) {
            this.mCurDataSet.mMaxTotalActive = totalActive;
        }
        if (fgActive > this.mCurDataSet.mMaxFgActive) {
            this.mCurDataSet.mMaxFgActive = fgActive;
        }
    }

    public float getLoadFactor(JobStatus job) {
        int uid = job.getSourceUid();
        String pkg = job.getSourcePackageName();
        PackageEntry cur = this.mCurDataSet.getEntry(uid, pkg);
        PackageEntry entry = this.mLastDataSets[EVENT_NULL] != null ? this.mLastDataSets[EVENT_NULL].getEntry(uid, pkg) : null;
        if (cur == null) {
            return 0.0f;
        }
        long now = SystemClock.uptimeMillis();
        long time = cur.getActiveTime(now) + cur.getPendingTime(now);
        long period = this.mCurDataSet.getTotalTime(now);
        if (entry != null) {
            time += entry.getActiveTime(now) + entry.getPendingTime(now);
            period += this.mLastDataSets[EVENT_NULL].getTotalTime(now);
        }
        return ((float) time) / ((float) period);
    }

    public void dump(PrintWriter pw, String prefix, int filterUid) {
        DataSet total;
        long now = SystemClock.uptimeMillis();
        long nowEllapsed = SystemClock.elapsedRealtime();
        if (this.mLastDataSets[EVENT_NULL] != null) {
            total = new DataSet(this.mLastDataSets[EVENT_NULL]);
            this.mLastDataSets[EVENT_NULL].addTo(total, now);
        } else {
            total = new DataSet(this.mCurDataSet);
        }
        this.mCurDataSet.addTo(total, now);
        for (int i = EVENT_START_JOB; i < this.mLastDataSets.length; i += EVENT_START_JOB) {
            if (this.mLastDataSets[i] != null) {
                this.mLastDataSets[i].dump(pw, "Historical stats", prefix, now, nowEllapsed, filterUid);
                pw.println();
            }
        }
        total.dump(pw, "Current stats", prefix, now, nowEllapsed, filterUid);
    }

    public boolean dumpHistory(PrintWriter pw, String prefix, int filterUid) {
        int size = this.mEventIndices.size();
        if (size <= 0) {
            return false;
        }
        pw.println("  Job history:");
        long now = SystemClock.elapsedRealtime();
        for (int i = EVENT_NULL; i < size; i += EVENT_START_JOB) {
            int index = this.mEventIndices.indexOf(i);
            int uid = this.mEventUids[index];
            if ((filterUid == -1 || filterUid == UserHandle.getAppId(filterUid)) && this.mEventCmds[index] != 0) {
                String label;
                switch (this.mEventCmds[index]) {
                    case EVENT_START_JOB /*1*/:
                        label = "START";
                        break;
                    case EVENT_STOP_JOB /*2*/:
                        label = " STOP";
                        break;
                    default:
                        label = "   ??";
                        break;
                }
                pw.print(prefix);
                TimeUtils.formatDuration(this.mEventTimes[index] - now, pw, 19);
                pw.print(" ");
                pw.print(label);
                pw.print(": ");
                UserHandle.formatUid(pw, uid);
                pw.print(" ");
                pw.println(this.mEventTags[index]);
            }
        }
        return true;
    }
}

package com.android.server.job;

import android.app.job.JobParameters;
import android.os.UserHandle;
import android.text.format.DateFormat;
import android.util.ArrayMap;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import com.android.internal.util.RingBufferIndices;
import com.android.server.job.controllers.JobStatus;
import com.android.server.slice.SliceClientPermissions;
import java.io.PrintWriter;

public final class JobPackageTracker {
    static final long BATCHING_TIME = 1800000;
    private static final int EVENT_BUFFER_SIZE = 100;
    public static final int EVENT_CMD_MASK = 255;
    public static final int EVENT_NULL = 0;
    public static final int EVENT_START_JOB = 1;
    public static final int EVENT_START_PERIODIC_JOB = 3;
    public static final int EVENT_STOP_JOB = 2;
    public static final int EVENT_STOP_PERIODIC_JOB = 4;
    public static final int EVENT_STOP_REASON_MASK = 65280;
    public static final int EVENT_STOP_REASON_SHIFT = 8;
    static final int NUM_HISTORY = 5;
    DataSet mCurDataSet = new DataSet();
    private final int[] mEventCmds = new int[100];
    private final RingBufferIndices mEventIndices = new RingBufferIndices(100);
    private final int[] mEventJobIds = new int[100];
    private final String[] mEventReasons = new String[100];
    private final String[] mEventTags = new String[100];
    private final long[] mEventTimes = new long[100];
    private final int[] mEventUids = new int[100];
    DataSet[] mLastDataSets = new DataSet[5];

    static final class DataSet {
        final SparseArray<ArrayMap<String, PackageEntry>> mEntries;
        int mMaxFgActive;
        int mMaxTotalActive;
        final long mStartClockTime;
        final long mStartElapsedTime;
        final long mStartUptimeTime;
        long mSummedTime;

        public DataSet(DataSet otherTimes) {
            this.mEntries = new SparseArray<>();
            this.mStartUptimeTime = otherTimes.mStartUptimeTime;
            this.mStartElapsedTime = otherTimes.mStartElapsedTime;
            this.mStartClockTime = otherTimes.mStartClockTime;
        }

        public DataSet() {
            this.mEntries = new SparseArray<>();
            this.mStartUptimeTime = JobSchedulerService.sUptimeMillisClock.millis();
            this.mStartElapsedTime = JobSchedulerService.sElapsedRealtimeClock.millis();
            this.mStartClockTime = JobSchedulerService.sSystemClock.millis();
        }

        private PackageEntry getOrCreateEntry(int uid, String pkg) {
            ArrayMap<String, PackageEntry> uidMap = this.mEntries.get(uid);
            if (uidMap == null) {
                uidMap = new ArrayMap<>();
                this.mEntries.put(uid, uidMap);
            }
            PackageEntry entry = uidMap.get(pkg);
            if (entry != null) {
                return entry;
            }
            PackageEntry entry2 = new PackageEntry();
            uidMap.put(pkg, entry2);
            return entry2;
        }

        public PackageEntry getEntry(int uid, String pkg) {
            ArrayMap<String, PackageEntry> uidMap = this.mEntries.get(uid);
            if (uidMap == null) {
                return null;
            }
            return uidMap.get(pkg);
        }

        /* access modifiers changed from: package-private */
        public long getTotalTime(long now) {
            if (this.mSummedTime > 0) {
                return this.mSummedTime;
            }
            return now - this.mStartUptimeTime;
        }

        /* access modifiers changed from: package-private */
        public void incPending(int uid, String pkg, long now) {
            PackageEntry pe = getOrCreateEntry(uid, pkg);
            if (pe.pendingNesting == 0) {
                pe.pendingStartTime = now;
                pe.pendingCount++;
            }
            pe.pendingNesting++;
        }

        /* access modifiers changed from: package-private */
        public void decPending(int uid, String pkg, long now) {
            PackageEntry pe = getOrCreateEntry(uid, pkg);
            if (pe.pendingNesting == 1) {
                pe.pastPendingTime += now - pe.pendingStartTime;
            }
            pe.pendingNesting--;
        }

        /* access modifiers changed from: package-private */
        public void incActive(int uid, String pkg, long now) {
            PackageEntry pe = getOrCreateEntry(uid, pkg);
            if (pe.activeNesting == 0) {
                pe.activeStartTime = now;
                pe.activeCount++;
            }
            pe.activeNesting++;
        }

        /* access modifiers changed from: package-private */
        public void decActive(int uid, String pkg, long now, int stopReason) {
            PackageEntry pe = getOrCreateEntry(uid, pkg);
            if (pe.activeNesting == 1) {
                pe.pastActiveTime += now - pe.activeStartTime;
            }
            pe.activeNesting--;
            pe.stopReasons.put(stopReason, pe.stopReasons.get(stopReason, 0) + 1);
        }

        /* access modifiers changed from: package-private */
        public void incActiveTop(int uid, String pkg, long now) {
            PackageEntry pe = getOrCreateEntry(uid, pkg);
            if (pe.activeTopNesting == 0) {
                pe.activeTopStartTime = now;
                pe.activeTopCount++;
            }
            pe.activeTopNesting++;
        }

        /* access modifiers changed from: package-private */
        public void decActiveTop(int uid, String pkg, long now, int stopReason) {
            PackageEntry pe = getOrCreateEntry(uid, pkg);
            if (pe.activeTopNesting == 1) {
                pe.pastActiveTopTime += now - pe.activeTopStartTime;
            }
            pe.activeTopNesting--;
            pe.stopReasons.put(stopReason, pe.stopReasons.get(stopReason, 0) + 1);
        }

        /* access modifiers changed from: package-private */
        public void finish(DataSet next, long now) {
            for (int i = this.mEntries.size() - 1; i >= 0; i--) {
                ArrayMap<String, PackageEntry> uidMap = this.mEntries.valueAt(i);
                for (int j = uidMap.size() - 1; j >= 0; j--) {
                    PackageEntry pe = uidMap.valueAt(j);
                    if (pe.activeNesting > 0 || pe.activeTopNesting > 0 || pe.pendingNesting > 0) {
                        PackageEntry nextPe = next.getOrCreateEntry(this.mEntries.keyAt(i), uidMap.keyAt(j));
                        nextPe.activeStartTime = now;
                        nextPe.activeNesting = pe.activeNesting;
                        nextPe.activeTopStartTime = now;
                        nextPe.activeTopNesting = pe.activeTopNesting;
                        nextPe.pendingStartTime = now;
                        nextPe.pendingNesting = pe.pendingNesting;
                        if (pe.activeNesting > 0) {
                            pe.pastActiveTime += now - pe.activeStartTime;
                            pe.activeNesting = 0;
                        }
                        if (pe.activeTopNesting > 0) {
                            pe.pastActiveTopTime += now - pe.activeTopStartTime;
                            pe.activeTopNesting = 0;
                        }
                        if (pe.pendingNesting > 0) {
                            pe.pastPendingTime += now - pe.pendingStartTime;
                            pe.pendingNesting = 0;
                        }
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void addTo(DataSet out, long now) {
            out.mSummedTime += getTotalTime(now);
            for (int i = this.mEntries.size() - 1; i >= 0; i--) {
                ArrayMap<String, PackageEntry> uidMap = this.mEntries.valueAt(i);
                for (int j = uidMap.size() - 1; j >= 0; j--) {
                    PackageEntry pe = uidMap.valueAt(j);
                    PackageEntry outPe = out.getOrCreateEntry(this.mEntries.keyAt(i), uidMap.keyAt(j));
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
                    for (int k = pe.stopReasons.size() - 1; k >= 0; k--) {
                        int type = pe.stopReasons.keyAt(k);
                        outPe.stopReasons.put(type, outPe.stopReasons.get(type, 0) + pe.stopReasons.valueAt(k));
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

        /* access modifiers changed from: package-private */
        public void printDuration(PrintWriter pw, long period, long duration, int count, String suffix) {
            int percent = (int) ((100.0f * (((float) duration) / ((float) period))) + 0.5f);
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

        /* access modifiers changed from: package-private */
        public void dump(PrintWriter pw, String header, String prefix, long now, long nowElapsed, int filterUid) {
            PrintWriter printWriter = pw;
            String str = prefix;
            long j = now;
            int i = filterUid;
            long period = getTotalTime(j);
            printWriter.print(str);
            pw.print(header);
            printWriter.print(" at ");
            printWriter.print(DateFormat.format("yyyy-MM-dd-HH-mm-ss", this.mStartClockTime).toString());
            printWriter.print(" (");
            TimeUtils.formatDuration(this.mStartElapsedTime, nowElapsed, printWriter);
            printWriter.print(") over ");
            TimeUtils.formatDuration(period, printWriter);
            printWriter.println(":");
            int NE = this.mEntries.size();
            int i2 = 0;
            while (true) {
                int i3 = i2;
                if (i3 < NE) {
                    int uid = this.mEntries.keyAt(i3);
                    if (i == -1 || i == UserHandle.getAppId(uid)) {
                        ArrayMap<String, PackageEntry> uidMap = this.mEntries.valueAt(i3);
                        int NP = uidMap.size();
                        int j2 = 0;
                        while (j2 < NP) {
                            PackageEntry pe = uidMap.valueAt(j2);
                            printWriter.print(str);
                            int NP2 = NP;
                            printWriter.print("  ");
                            UserHandle.formatUid(printWriter, uid);
                            printWriter.print(" / ");
                            printWriter.print(uidMap.keyAt(j2));
                            printWriter.println(":");
                            printWriter.print(str);
                            printWriter.print("   ");
                            int j3 = j2;
                            PrintWriter printWriter2 = printWriter;
                            int uid2 = uid;
                            ArrayMap<String, PackageEntry> uidMap2 = uidMap;
                            long j4 = period;
                            int NE2 = NE;
                            int i4 = i3;
                            printDuration(printWriter2, j4, pe.getPendingTime(j), pe.pendingCount, "pending");
                            printDuration(printWriter2, j4, pe.getActiveTime(j), pe.activeCount, "active");
                            printDuration(printWriter2, j4, pe.getActiveTopTime(j), pe.activeTopCount, "active-top");
                            if (pe.pendingNesting > 0 || pe.hadPending) {
                                printWriter.print(" (pending)");
                            }
                            if (pe.activeNesting > 0 || pe.hadActive) {
                                printWriter.print(" (active)");
                            }
                            if (pe.activeTopNesting > 0 || pe.hadActiveTop) {
                                printWriter.print(" (active-top)");
                            }
                            pw.println();
                            if (pe.stopReasons.size() > 0) {
                                printWriter.print(str);
                                printWriter.print("    ");
                                for (int k = 0; k < pe.stopReasons.size(); k++) {
                                    if (k > 0) {
                                        printWriter.print(", ");
                                    }
                                    printWriter.print(pe.stopReasons.valueAt(k));
                                    printWriter.print("x ");
                                    printWriter.print(JobParameters.getReasonName(pe.stopReasons.keyAt(k)));
                                }
                                pw.println();
                            }
                            j2 = j3 + 1;
                            long j5 = nowElapsed;
                            NP = NP2;
                            uid = uid2;
                            uidMap = uidMap2;
                            NE = NE2;
                            i3 = i4;
                            int i5 = filterUid;
                        }
                    }
                    i2 = i3 + 1;
                    long j6 = nowElapsed;
                    NE = NE;
                    i = filterUid;
                } else {
                    printWriter.print(str);
                    printWriter.print("  Max concurrency: ");
                    printWriter.print(this.mMaxTotalActive);
                    printWriter.print(" total, ");
                    printWriter.print(this.mMaxFgActive);
                    printWriter.println(" foreground");
                    return;
                }
            }
        }

        private void printPackageEntryState(ProtoOutputStream proto, long fieldId, long duration, int count) {
            long token = proto.start(fieldId);
            proto.write(1112396529665L, duration);
            proto.write(1120986464258L, count);
            proto.end(token);
        }

        /* access modifiers changed from: package-private */
        public void dump(ProtoOutputStream proto, long fieldId, long now, long nowElapsed, int filterUid) {
            ProtoOutputStream protoOutputStream = proto;
            long j = now;
            int i = filterUid;
            long token = proto.start(fieldId);
            long period = getTotalTime(j);
            protoOutputStream.write(1112396529665L, this.mStartClockTime);
            protoOutputStream.write(1112396529666L, nowElapsed - this.mStartElapsedTime);
            protoOutputStream.write(1112396529667L, period);
            int NE = this.mEntries.size();
            int i2 = 0;
            while (true) {
                int i3 = i2;
                if (i3 < NE) {
                    int uid = this.mEntries.keyAt(i3);
                    if (i == -1 || i == UserHandle.getAppId(uid)) {
                        ArrayMap<String, PackageEntry> uidMap = this.mEntries.valueAt(i3);
                        int NP = uidMap.size();
                        int j2 = 0;
                        while (true) {
                            int j3 = j2;
                            if (j3 >= NP) {
                                break;
                            }
                            int NP2 = NP;
                            int i4 = i3;
                            long peToken = protoOutputStream.start(2246267895812L);
                            PackageEntry pe = uidMap.valueAt(j3);
                            protoOutputStream.write(1120986464257L, uid);
                            int j4 = j3;
                            protoOutputStream.write(1138166333442L, uidMap.keyAt(j3));
                            long pendingTime = pe.getPendingTime(j);
                            int j5 = pe.pendingCount;
                            PackageEntry pe2 = pe;
                            int j6 = j4;
                            long period2 = period;
                            long period3 = peToken;
                            int NP3 = NP2;
                            ArrayMap<String, PackageEntry> uidMap2 = uidMap;
                            int uid2 = uid;
                            long j7 = pendingTime;
                            int NE2 = NE;
                            printPackageEntryState(protoOutputStream, 1146756268035L, j7, j5);
                            PackageEntry pe3 = pe2;
                            ProtoOutputStream protoOutputStream2 = protoOutputStream;
                            PackageEntry pe4 = pe3;
                            printPackageEntryState(protoOutputStream2, 1146756268036L, pe3.getActiveTime(j), pe3.activeCount);
                            printPackageEntryState(protoOutputStream2, 1146756268037L, pe4.getActiveTopTime(j), pe4.activeTopCount);
                            boolean z = true;
                            protoOutputStream.write(1133871366150L, pe4.pendingNesting > 0 || pe4.hadPending);
                            protoOutputStream.write(1133871366151L, pe4.activeNesting > 0 || pe4.hadActive);
                            if (pe4.activeTopNesting <= 0 && !pe4.hadActiveTop) {
                                z = false;
                            }
                            protoOutputStream.write(1133871366152L, z);
                            for (int k = 0; k < pe4.stopReasons.size(); k++) {
                                long srcToken = protoOutputStream.start(2246267895817L);
                                protoOutputStream.write(1159641169921L, pe4.stopReasons.keyAt(k));
                                protoOutputStream.write(1120986464258L, pe4.stopReasons.valueAt(k));
                                protoOutputStream.end(srcToken);
                            }
                            protoOutputStream.end(period3);
                            j2 = j6 + 1;
                            i3 = i4;
                            uidMap = uidMap2;
                            NP = NP3;
                            uid = uid2;
                            NE = NE2;
                            period = period2;
                            int i5 = filterUid;
                        }
                    }
                    i2 = i3 + 1;
                    NE = NE;
                    period = period;
                    i = filterUid;
                } else {
                    long j8 = period;
                    protoOutputStream.write(1120986464261L, this.mMaxTotalActive);
                    protoOutputStream.write(1120986464262L, this.mMaxFgActive);
                    protoOutputStream.end(token);
                    return;
                }
            }
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
        final SparseIntArray stopReasons = new SparseIntArray();

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

    public void addEvent(int cmd, int uid, String tag, int jobId, int stopReason, String debugReason) {
        int index = this.mEventIndices.add();
        this.mEventCmds[index] = ((stopReason << 8) & EVENT_STOP_REASON_MASK) | cmd;
        this.mEventTimes[index] = JobSchedulerService.sElapsedRealtimeClock.millis();
        this.mEventUids[index] = uid;
        this.mEventTags[index] = tag;
        this.mEventJobIds[index] = jobId;
        this.mEventReasons[index] = debugReason;
    }

    /* access modifiers changed from: package-private */
    public void rebatchIfNeeded(long now) {
        long totalTime = this.mCurDataSet.getTotalTime(now);
        if (totalTime > 1800000) {
            DataSet last = this.mCurDataSet;
            last.mSummedTime = totalTime;
            this.mCurDataSet = new DataSet();
            last.finish(this.mCurDataSet, now);
            System.arraycopy(this.mLastDataSets, 0, this.mLastDataSets, 1, this.mLastDataSets.length - 1);
            this.mLastDataSets[0] = last;
        }
    }

    public void notePending(JobStatus job) {
        long now = JobSchedulerService.sUptimeMillisClock.millis();
        job.madePending = now;
        rebatchIfNeeded(now);
        this.mCurDataSet.incPending(job.getSourceUid(), job.getSourcePackageName(), now);
    }

    public void noteNonpending(JobStatus job) {
        long now = JobSchedulerService.sUptimeMillisClock.millis();
        this.mCurDataSet.decPending(job.getSourceUid(), job.getSourcePackageName(), now);
        rebatchIfNeeded(now);
    }

    public void noteActive(JobStatus job) {
        long now = JobSchedulerService.sUptimeMillisClock.millis();
        job.madeActive = now;
        rebatchIfNeeded(now);
        if (job.lastEvaluatedPriority >= 40) {
            this.mCurDataSet.incActiveTop(job.getSourceUid(), job.getSourcePackageName(), now);
        } else {
            this.mCurDataSet.incActive(job.getSourceUid(), job.getSourcePackageName(), now);
        }
        addEvent(job.getJob().isPeriodic() ? 3 : 1, job.getSourceUid(), job.getBatteryName(), job.getJobId(), 0, null);
    }

    public void noteInactive(JobStatus job, int stopReason, String debugReason) {
        long now = JobSchedulerService.sUptimeMillisClock.millis();
        if (job.lastEvaluatedPriority >= 40) {
            this.mCurDataSet.decActiveTop(job.getSourceUid(), job.getSourcePackageName(), now, stopReason);
        } else {
            this.mCurDataSet.decActive(job.getSourceUid(), job.getSourcePackageName(), now, stopReason);
        }
        rebatchIfNeeded(now);
        addEvent(job.getJob().isPeriodic() ? 2 : 4, job.getSourceUid(), job.getBatteryName(), job.getJobId(), stopReason, debugReason);
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
        PackageEntry last = this.mLastDataSets[0] != null ? this.mLastDataSets[0].getEntry(uid, pkg) : null;
        if (cur == null) {
            return 0.0f;
        }
        long now = JobSchedulerService.sUptimeMillisClock.millis();
        long time = 0;
        if (cur != null) {
            time = 0 + cur.getActiveTime(now) + cur.getPendingTime(now);
        }
        long period = this.mCurDataSet.getTotalTime(now);
        if (last != null) {
            time += last.getActiveTime(now) + last.getPendingTime(now);
            period += this.mLastDataSets[0].getTotalTime(now);
        }
        return ((float) time) / ((float) period);
    }

    public void dump(PrintWriter pw, String prefix, int filterUid) {
        DataSet total;
        long now = JobSchedulerService.sUptimeMillisClock.millis();
        long nowElapsed = JobSchedulerService.sElapsedRealtimeClock.millis();
        if (this.mLastDataSets[0] != null) {
            total = new DataSet(this.mLastDataSets[0]);
            this.mLastDataSets[0].addTo(total, now);
        } else {
            total = new DataSet(this.mCurDataSet);
        }
        this.mCurDataSet.addTo(total, now);
        int i = 1;
        while (true) {
            int i2 = i;
            if (i2 < this.mLastDataSets.length) {
                if (this.mLastDataSets[i2] != null) {
                    this.mLastDataSets[i2].dump(pw, "Historical stats", prefix, now, nowElapsed, filterUid);
                    pw.println();
                }
                i = i2 + 1;
            } else {
                total.dump(pw, "Current stats", prefix, now, nowElapsed, filterUid);
                return;
            }
        }
    }

    public void dump(ProtoOutputStream proto, long fieldId, int filterUid) {
        DataSet total;
        int i;
        long token = proto.start(fieldId);
        long now = JobSchedulerService.sUptimeMillisClock.millis();
        long nowElapsed = JobSchedulerService.sElapsedRealtimeClock.millis();
        if (this.mLastDataSets[0] != null) {
            total = new DataSet(this.mLastDataSets[0]);
            this.mLastDataSets[0].addTo(total, now);
        } else {
            total = new DataSet(this.mCurDataSet);
        }
        this.mCurDataSet.addTo(total, now);
        int i2 = 1;
        while (true) {
            int i3 = i2;
            if (i3 < this.mLastDataSets.length) {
                if (this.mLastDataSets[i3] != null) {
                    i = i3;
                    this.mLastDataSets[i3].dump(proto, 2246267895809L, now, nowElapsed, filterUid);
                } else {
                    i = i3;
                }
                i2 = i + 1;
            } else {
                total.dump(proto, 1146756268034L, now, nowElapsed, filterUid);
                proto.end(token);
                return;
            }
        }
    }

    public boolean dumpHistory(PrintWriter pw, String prefix, int filterUid) {
        String label;
        int size = this.mEventIndices.size();
        if (size <= 0) {
            return false;
        }
        pw.println("  Job history:");
        long now = JobSchedulerService.sElapsedRealtimeClock.millis();
        for (int i = 0; i < size; i++) {
            int index = this.mEventIndices.indexOf(i);
            int uid = this.mEventUids[index];
            if (filterUid == -1 || filterUid == UserHandle.getAppId(uid)) {
                int cmd = this.mEventCmds[index] & 255;
                if (cmd != 0) {
                    switch (cmd) {
                        case 1:
                            label = "  START";
                            break;
                        case 2:
                            label = "   STOP";
                            break;
                        case 3:
                            label = "START-P";
                            break;
                        case 4:
                            label = " STOP-P";
                            break;
                        default:
                            label = "     ??";
                            break;
                    }
                    pw.print(prefix);
                    TimeUtils.formatDuration(this.mEventTimes[index] - now, pw, 19);
                    pw.print(" ");
                    pw.print(label);
                    pw.print(": #");
                    UserHandle.formatUid(pw, uid);
                    pw.print(SliceClientPermissions.SliceAuthority.DELIMITER);
                    pw.print(this.mEventJobIds[index]);
                    pw.print(" ");
                    pw.print(this.mEventTags[index]);
                    if (cmd == 2 || cmd == 4) {
                        pw.print(" ");
                        if (this.mEventReasons[index] != null) {
                            pw.print(this.mEventReasons[index]);
                        } else {
                            pw.print(JobParameters.getReasonName((this.mEventCmds[index] & EVENT_STOP_REASON_MASK) >> 8));
                        }
                    }
                    pw.println();
                }
            }
        }
        return true;
    }

    public void dumpHistory(ProtoOutputStream proto, long fieldId, int filterUid) {
        int size;
        ProtoOutputStream protoOutputStream = proto;
        int i = filterUid;
        int size2 = this.mEventIndices.size();
        if (size2 != 0) {
            long token = proto.start(fieldId);
            long now = JobSchedulerService.sElapsedRealtimeClock.millis();
            int i2 = 0;
            while (i2 < size2) {
                int index = this.mEventIndices.indexOf(i2);
                int uid = this.mEventUids[index];
                if (i == -1 || i == UserHandle.getAppId(uid)) {
                    int cmd = this.mEventCmds[index] & 255;
                    if (cmd != 0) {
                        long heToken = protoOutputStream.start(2246267895809L);
                        protoOutputStream.write(1159641169921L, cmd);
                        size = size2;
                        protoOutputStream.write(1112396529666L, now - this.mEventTimes[index]);
                        protoOutputStream.write(1120986464259L, uid);
                        protoOutputStream.write(1120986464260L, this.mEventJobIds[index]);
                        protoOutputStream.write(1138166333445L, this.mEventTags[index]);
                        if (cmd == 2 || cmd == 4) {
                            protoOutputStream.write(1159641169926L, (this.mEventCmds[index] & EVENT_STOP_REASON_MASK) >> 8);
                        }
                        protoOutputStream.end(heToken);
                        i2++;
                        size2 = size;
                        i = filterUid;
                    }
                }
                size = size2;
                i2++;
                size2 = size;
                i = filterUid;
            }
            protoOutputStream.end(token);
        }
    }
}

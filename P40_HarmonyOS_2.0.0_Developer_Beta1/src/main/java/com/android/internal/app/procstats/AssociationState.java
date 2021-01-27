package com.android.internal.app.procstats;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.SettingsStringUtil;
import android.telephony.SmsManager;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import com.android.internal.app.procstats.ProcessStats;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Objects;

public final class AssociationState {
    private static final boolean DEBUG = false;
    private static final String TAG = "ProcessStats";
    private final String mName;
    private int mNumActive;
    private final ProcessStats.PackageState mPackageState;
    private ProcessState mProc;
    private final String mProcessName;
    private final ProcessStats mProcessStats;
    private final ArrayMap<SourceKey, SourceState> mSources = new ArrayMap<>();
    private final SourceKey mTmpSourceKey = new SourceKey(0, null, null);

    static /* synthetic */ int access$110(AssociationState x0) {
        int i = x0.mNumActive;
        x0.mNumActive = i - 1;
        return i;
    }

    public final class SourceState {
        int mActiveCount;
        long mActiveDuration;
        int mActiveProcState = -1;
        long mActiveStartUptime;
        int mCount;
        long mDuration;
        DurationsTable mDurations;
        boolean mInTrackingList;
        final SourceKey mKey;
        int mNesting;
        int mProcState = -1;
        int mProcStateSeq = -1;
        long mStartUptime;
        long mTrackingUptime;

        SourceState(SourceKey key) {
            this.mKey = key;
        }

        public AssociationState getAssociationState() {
            return AssociationState.this;
        }

        public String getProcessName() {
            return this.mKey.mProcess;
        }

        public int getUid() {
            return this.mKey.mUid;
        }

        public void trackProcState(int procState, int seq, long now) {
            int procState2 = ProcessState.PROCESS_STATE_TO_STATE[procState];
            if (seq != this.mProcStateSeq) {
                this.mProcStateSeq = seq;
                this.mProcState = procState2;
            } else if (procState2 < this.mProcState) {
                this.mProcState = procState2;
            }
            if (procState2 < 9 && !this.mInTrackingList) {
                this.mInTrackingList = true;
                this.mTrackingUptime = now;
                AssociationState.this.mProcessStats.mTrackingAssociations.add(this);
            }
        }

        public void stop() {
            this.mNesting--;
            if (this.mNesting == 0) {
                this.mDuration += SystemClock.uptimeMillis() - this.mStartUptime;
                AssociationState.access$110(AssociationState.this);
                stopTracking(SystemClock.uptimeMillis());
            }
        }

        /* access modifiers changed from: package-private */
        public void startActive(long now) {
            if (this.mInTrackingList) {
                if (this.mActiveStartUptime == 0) {
                    this.mActiveStartUptime = now;
                    this.mActiveCount++;
                }
                int i = this.mActiveProcState;
                if (i != this.mProcState) {
                    if (i != -1) {
                        long duration = (this.mActiveDuration + now) - this.mActiveStartUptime;
                        if (duration != 0) {
                            if (this.mDurations == null) {
                                makeDurations();
                            }
                            this.mDurations.addDuration(this.mActiveProcState, duration);
                            this.mActiveDuration = 0;
                        }
                        this.mActiveStartUptime = now;
                    }
                    this.mActiveProcState = this.mProcState;
                    return;
                }
                return;
            }
            Slog.wtf("ProcessStats", "startActive while not tracking: " + this);
        }

        /* access modifiers changed from: package-private */
        public void stopActive(long now) {
            if (this.mActiveStartUptime != 0) {
                if (!this.mInTrackingList) {
                    Slog.wtf("ProcessStats", "stopActive while not tracking: " + this);
                }
                long duration = (this.mActiveDuration + now) - this.mActiveStartUptime;
                DurationsTable durationsTable = this.mDurations;
                if (durationsTable != null) {
                    durationsTable.addDuration(this.mActiveProcState, duration);
                } else {
                    this.mActiveDuration = duration;
                }
                this.mActiveStartUptime = 0;
            }
        }

        /* access modifiers changed from: package-private */
        public void makeDurations() {
            this.mDurations = new DurationsTable(AssociationState.this.mProcessStats.mTableData);
        }

        /* access modifiers changed from: package-private */
        public void stopTracking(long now) {
            stopActive(now);
            if (this.mInTrackingList) {
                this.mInTrackingList = false;
                this.mProcState = -1;
                ArrayList<SourceState> list = AssociationState.this.mProcessStats.mTrackingAssociations;
                for (int i = list.size() - 1; i >= 0; i--) {
                    if (list.get(i) == this) {
                        list.remove(i);
                        return;
                    }
                }
                Slog.wtf("ProcessStats", "Stop tracking didn't find in tracking list: " + this);
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(64);
            sb.append("SourceState{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            sb.append(this.mKey.mProcess);
            sb.append("/");
            sb.append(this.mKey.mUid);
            if (this.mProcState != -1) {
                sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                sb.append(DumpUtils.STATE_NAMES[this.mProcState]);
                sb.append(" #");
                sb.append(this.mProcStateSeq);
            }
            sb.append("}");
            return sb.toString();
        }
    }

    private static final class SourceKey {
        String mPackage;
        String mProcess;
        int mUid;

        SourceKey(int uid, String process, String pkg) {
            this.mUid = uid;
            this.mProcess = process;
            this.mPackage = pkg;
        }

        public boolean equals(Object o) {
            if (!(o instanceof SourceKey)) {
                return false;
            }
            SourceKey s = (SourceKey) o;
            if (s.mUid != this.mUid || !Objects.equals(s.mProcess, this.mProcess) || !Objects.equals(s.mPackage, this.mPackage)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            int hashCode = Integer.hashCode(this.mUid);
            String str = this.mProcess;
            int i = 0;
            int hashCode2 = hashCode ^ (str == null ? 0 : str.hashCode());
            String str2 = this.mPackage;
            if (str2 != null) {
                i = str2.hashCode() * 33;
            }
            return hashCode2 ^ i;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(64);
            sb.append("SourceKey{");
            UserHandle.formatUid(sb, this.mUid);
            sb.append(' ');
            sb.append(this.mProcess);
            sb.append(' ');
            sb.append(this.mPackage);
            sb.append('}');
            return sb.toString();
        }
    }

    public AssociationState(ProcessStats processStats, ProcessStats.PackageState packageState, String name, String processName, ProcessState proc) {
        this.mProcessStats = processStats;
        this.mPackageState = packageState;
        this.mName = name;
        this.mProcessName = processName;
        this.mProc = proc;
    }

    public int getUid() {
        return this.mPackageState.mUid;
    }

    public String getPackage() {
        return this.mPackageState.mPackageName;
    }

    public String getProcessName() {
        return this.mProcessName;
    }

    public String getName() {
        return this.mName;
    }

    public ProcessState getProcess() {
        return this.mProc;
    }

    public void setProcess(ProcessState proc) {
        this.mProc = proc;
    }

    public SourceState startSource(int uid, String processName, String packageName) {
        SourceKey sourceKey = this.mTmpSourceKey;
        sourceKey.mUid = uid;
        sourceKey.mProcess = processName;
        sourceKey.mPackage = packageName;
        SourceState src = this.mSources.get(sourceKey);
        if (src == null) {
            SourceKey key = new SourceKey(uid, processName, packageName);
            src = new SourceState(key);
            this.mSources.put(key, src);
        }
        src.mNesting++;
        if (src.mNesting == 1) {
            src.mCount++;
            src.mStartUptime = SystemClock.uptimeMillis();
            this.mNumActive++;
        }
        return src;
    }

    public void add(AssociationState other) {
        for (int isrc = other.mSources.size() - 1; isrc >= 0; isrc--) {
            SourceKey key = other.mSources.keyAt(isrc);
            SourceState otherSrc = other.mSources.valueAt(isrc);
            SourceState mySrc = this.mSources.get(key);
            if (mySrc == null) {
                mySrc = new SourceState(key);
                this.mSources.put(key, mySrc);
            }
            mySrc.mCount += otherSrc.mCount;
            mySrc.mDuration += otherSrc.mDuration;
            mySrc.mActiveCount += otherSrc.mActiveCount;
            if (otherSrc.mActiveDuration != 0 || otherSrc.mDurations != null) {
                if (mySrc.mDurations != null) {
                    if (otherSrc.mDurations != null) {
                        mySrc.mDurations.addDurations(otherSrc.mDurations);
                    } else {
                        mySrc.mDurations.addDuration(otherSrc.mActiveProcState, otherSrc.mActiveDuration);
                    }
                } else if (otherSrc.mDurations != null) {
                    mySrc.makeDurations();
                    mySrc.mDurations.addDurations(otherSrc.mDurations);
                    if (mySrc.mActiveDuration != 0) {
                        mySrc.mDurations.addDuration(mySrc.mActiveProcState, mySrc.mActiveDuration);
                        mySrc.mActiveDuration = 0;
                        mySrc.mActiveProcState = -1;
                    }
                } else if (mySrc.mActiveDuration == 0) {
                    mySrc.mActiveProcState = otherSrc.mActiveProcState;
                    mySrc.mActiveDuration = otherSrc.mActiveDuration;
                } else if (mySrc.mActiveProcState == otherSrc.mActiveProcState) {
                    mySrc.mDuration += otherSrc.mDuration;
                } else {
                    mySrc.makeDurations();
                    mySrc.mDurations.addDuration(mySrc.mActiveProcState, mySrc.mActiveDuration);
                    mySrc.mDurations.addDuration(otherSrc.mActiveProcState, otherSrc.mActiveDuration);
                    mySrc.mActiveDuration = 0;
                    mySrc.mActiveProcState = -1;
                }
            }
        }
    }

    public boolean isInUse() {
        return this.mNumActive > 0;
    }

    public void resetSafely(long now) {
        if (!isInUse()) {
            this.mSources.clear();
            return;
        }
        for (int isrc = this.mSources.size() - 1; isrc >= 0; isrc--) {
            SourceState src = this.mSources.valueAt(isrc);
            if (src.mNesting > 0) {
                src.mCount = 1;
                src.mStartUptime = now;
                src.mDuration = 0;
                if (src.mActiveStartUptime > 0) {
                    src.mActiveCount = 1;
                    src.mActiveStartUptime = now;
                } else {
                    src.mActiveCount = 0;
                }
                src.mActiveDuration = 0;
                src.mDurations = null;
            } else {
                this.mSources.removeAt(isrc);
            }
        }
    }

    public void writeToParcel(ProcessStats stats, Parcel out, long nowUptime) {
        int NSRC = this.mSources.size();
        out.writeInt(NSRC);
        for (int isrc = 0; isrc < NSRC; isrc++) {
            SourceKey key = this.mSources.keyAt(isrc);
            SourceState src = this.mSources.valueAt(isrc);
            out.writeInt(key.mUid);
            stats.writeCommonString(out, key.mProcess);
            stats.writeCommonString(out, key.mPackage);
            out.writeInt(src.mCount);
            out.writeLong(src.mDuration);
            out.writeInt(src.mActiveCount);
            if (src.mDurations != null) {
                out.writeInt(1);
                src.mDurations.writeToParcel(out);
            } else {
                out.writeInt(0);
                out.writeInt(src.mActiveProcState);
                out.writeLong(src.mActiveDuration);
            }
        }
    }

    public String readFromParcel(ProcessStats stats, Parcel in, int parcelVersion) {
        int NSRC = in.readInt();
        if (NSRC < 0 || NSRC > 100000) {
            return "Association with bad src count: " + NSRC;
        }
        for (int isrc = 0; isrc < NSRC; isrc++) {
            SourceKey key = new SourceKey(in.readInt(), stats.readCommonString(in, parcelVersion), stats.readCommonString(in, parcelVersion));
            SourceState src = new SourceState(key);
            src.mCount = in.readInt();
            src.mDuration = in.readLong();
            src.mActiveCount = in.readInt();
            if (in.readInt() != 0) {
                src.makeDurations();
                if (!src.mDurations.readFromParcel(in)) {
                    return "Duration table corrupt: " + key + " <- " + src;
                }
            } else {
                src.mActiveProcState = in.readInt();
                src.mActiveDuration = in.readLong();
            }
            this.mSources.put(key, src);
        }
        return null;
    }

    public void commitStateTime(long nowUptime) {
        if (isInUse()) {
            for (int isrc = this.mSources.size() - 1; isrc >= 0; isrc--) {
                SourceState src = this.mSources.valueAt(isrc);
                if (src.mNesting > 0) {
                    src.mDuration += nowUptime - src.mStartUptime;
                    src.mStartUptime = nowUptime;
                }
                if (src.mActiveStartUptime > 0) {
                    long duration = (src.mActiveDuration + nowUptime) - src.mActiveStartUptime;
                    if (src.mDurations != null) {
                        src.mDurations.addDuration(src.mActiveProcState, duration);
                    } else {
                        src.mActiveDuration = duration;
                    }
                    src.mActiveStartUptime = nowUptime;
                }
            }
        }
    }

    public boolean hasProcessOrPackage(String procName) {
        int NSRC = this.mSources.size();
        for (int isrc = 0; isrc < NSRC; isrc++) {
            SourceKey key = this.mSources.keyAt(isrc);
            if (procName.equals(key.mProcess) || procName.equals(key.mPackage)) {
                return true;
            }
        }
        return false;
    }

    public void dumpStats(PrintWriter pw, String prefix, String prefixInner, String headerPrefix, long now, long totalTime, String reqPackage, boolean dumpDetails, boolean dumpAll) {
        int NSRC;
        long duration;
        SourceState src;
        int i;
        AssociationState associationState = this;
        String str = reqPackage;
        if (dumpAll) {
            pw.print(prefix);
            pw.print("mNumActive=");
            pw.println(associationState.mNumActive);
        }
        int NSRC2 = associationState.mSources.size();
        int isrc = 0;
        while (isrc < NSRC2) {
            SourceKey key = associationState.mSources.keyAt(isrc);
            SourceState src2 = associationState.mSources.valueAt(isrc);
            if (str == null || str.equals(key.mProcess) || str.equals(key.mPackage)) {
                pw.print(prefixInner);
                pw.print("<- ");
                pw.print(key.mProcess);
                pw.print("/");
                UserHandle.formatUid(pw, key.mUid);
                if (key.mPackage != null) {
                    pw.print(" (");
                    pw.print(key.mPackage);
                    pw.print(")");
                }
                pw.println(SettingsStringUtil.DELIMITER);
                pw.print(prefixInner);
                pw.print("   Total count ");
                pw.print(src2.mCount);
                long duration2 = src2.mDuration;
                if (src2.mNesting > 0) {
                    duration = duration2 + (now - src2.mStartUptime);
                } else {
                    duration = duration2;
                }
                if (dumpAll) {
                    pw.print(": Duration ");
                    TimeUtils.formatDuration(duration, pw);
                    pw.print(" / ");
                } else {
                    pw.print(": time ");
                }
                NSRC = NSRC2;
                DumpUtils.printPercent(pw, ((double) duration) / ((double) totalTime));
                if (src2.mNesting > 0) {
                    pw.print(" (running");
                    if (src2.mProcState != -1) {
                        pw.print(" / ");
                        pw.print(DumpUtils.STATE_NAMES[src2.mProcState]);
                        pw.print(" #");
                        pw.print(src2.mProcStateSeq);
                    }
                    pw.print(")");
                }
                pw.println();
                if (src2.mActiveCount <= 0 && src2.mDurations == null && src2.mActiveDuration == 0 && src2.mActiveStartUptime == 0) {
                    i = -1;
                    src = src2;
                } else {
                    pw.print(prefixInner);
                    pw.print("   Active count ");
                    pw.print(src2.mActiveCount);
                    if (dumpDetails) {
                        if (dumpAll) {
                            pw.print(src2.mDurations != null ? " (multi-field)" : " (inline)");
                        }
                        pw.println(SettingsStringUtil.DELIMITER);
                        i = -1;
                        src = src2;
                        dumpTime(pw, prefixInner, src2, totalTime, now, dumpDetails, dumpAll);
                    } else {
                        i = -1;
                        src = src2;
                        pw.print(": ");
                        dumpActiveDurationSummary(pw, src, totalTime, now, dumpAll);
                        pw.println();
                    }
                }
                if (dumpAll) {
                    if (src.mInTrackingList) {
                        pw.print(prefixInner);
                        pw.print("   mInTrackingList=");
                        pw.println(src.mInTrackingList);
                    }
                    if (src.mProcState != i) {
                        pw.print(prefixInner);
                        pw.print("   mProcState=");
                        pw.print(DumpUtils.STATE_NAMES[src.mProcState]);
                        pw.print(" mProcStateSeq=");
                        pw.println(src.mProcStateSeq);
                    }
                }
            } else {
                NSRC = NSRC2;
            }
            isrc++;
            associationState = this;
            str = reqPackage;
            NSRC2 = NSRC;
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpActiveDurationSummary(PrintWriter pw, SourceState src, long totalTime, long now, boolean dumpAll) {
        long duration = dumpTime(null, null, src, totalTime, now, false, false);
        if (duration < 0) {
            duration = -duration;
        }
        if (dumpAll) {
            pw.print("Duration ");
            TimeUtils.formatDuration(duration, pw);
            pw.print(" / ");
        } else {
            pw.print("time ");
        }
        DumpUtils.printPercent(pw, ((double) duration) / ((double) totalTime));
        if (src.mActiveStartUptime > 0) {
            pw.print(" (running)");
        }
        pw.println();
    }

    /* access modifiers changed from: package-private */
    public long dumpTime(PrintWriter pw, String prefix, SourceState src, long overallTime, long now, boolean dumpDetails, boolean dumpAll) {
        long totalTime;
        long time;
        String running;
        long totalTime2 = 0;
        boolean isRunning = false;
        int iprocstate = 0;
        while (iprocstate < 14) {
            if (src.mDurations != null) {
                time = src.mDurations.getValueForId((byte) iprocstate);
            } else {
                time = src.mActiveProcState == iprocstate ? src.mDuration : 0;
            }
            if (src.mActiveStartUptime == 0 || src.mActiveProcState != iprocstate) {
                running = null;
            } else {
                running = " (running)";
                isRunning = true;
                time += now - src.mActiveStartUptime;
            }
            if (time != 0) {
                if (pw != null) {
                    pw.print(prefix);
                    pw.print("  ");
                    pw.print(DumpUtils.STATE_LABELS[iprocstate]);
                    pw.print(": ");
                    if (dumpAll) {
                        pw.print("Duration ");
                        TimeUtils.formatDuration(time, pw);
                        pw.print(" / ");
                    } else {
                        pw.print("time ");
                    }
                    DumpUtils.printPercent(pw, ((double) time) / ((double) overallTime));
                    if (running != null) {
                        pw.print(running);
                    }
                    pw.println();
                }
                totalTime2 += time;
            } else {
                totalTime2 = totalTime2;
            }
            iprocstate++;
        }
        if (totalTime2 == 0 || pw == null) {
            totalTime = totalTime2;
        } else {
            pw.print(prefix);
            pw.print("  ");
            pw.print(DumpUtils.STATE_LABEL_TOTAL);
            pw.print(": ");
            if (dumpAll) {
                pw.print("Duration ");
                totalTime = totalTime2;
                TimeUtils.formatDuration(totalTime, pw);
                pw.print(" / ");
            } else {
                totalTime = totalTime2;
                pw.print("time ");
            }
            DumpUtils.printPercent(pw, ((double) totalTime) / ((double) overallTime));
            pw.println();
        }
        return isRunning ? -totalTime : totalTime;
    }

    public void dumpTimesCheckin(PrintWriter pw, String pkgName, int uid, long vers, String associationName, long now) {
        int NSRC;
        AssociationState associationState = this;
        int NSRC2 = associationState.mSources.size();
        int isrc = 0;
        while (isrc < NSRC2) {
            SourceKey key = associationState.mSources.keyAt(isrc);
            SourceState src = associationState.mSources.valueAt(isrc);
            pw.print("pkgasc");
            pw.print(SmsManager.REGEX_PREFIX_DELIMITER);
            pw.print(pkgName);
            pw.print(SmsManager.REGEX_PREFIX_DELIMITER);
            pw.print(uid);
            pw.print(SmsManager.REGEX_PREFIX_DELIMITER);
            pw.print(vers);
            pw.print(SmsManager.REGEX_PREFIX_DELIMITER);
            pw.print(associationName);
            pw.print(SmsManager.REGEX_PREFIX_DELIMITER);
            pw.print(key.mProcess);
            pw.print(SmsManager.REGEX_PREFIX_DELIMITER);
            pw.print(key.mUid);
            pw.print(SmsManager.REGEX_PREFIX_DELIMITER);
            pw.print(src.mCount);
            long duration = src.mDuration;
            if (src.mNesting > 0) {
                duration += now - src.mStartUptime;
            }
            pw.print(SmsManager.REGEX_PREFIX_DELIMITER);
            pw.print(duration);
            pw.print(SmsManager.REGEX_PREFIX_DELIMITER);
            pw.print(src.mActiveCount);
            long timeNow = src.mActiveStartUptime != 0 ? now - src.mActiveStartUptime : 0;
            if (src.mDurations != null) {
                int N = src.mDurations.getKeyCount();
                int i = 0;
                long duration2 = duration;
                while (i < N) {
                    int dkey = src.mDurations.getKeyAt(i);
                    long duration3 = src.mDurations.getValue(dkey);
                    if (dkey == src.mActiveProcState) {
                        duration3 += timeNow;
                    }
                    int procState = SparseMappingTable.getIdFromKey(dkey);
                    pw.print(SmsManager.REGEX_PREFIX_DELIMITER);
                    DumpUtils.printArrayEntry(pw, DumpUtils.STATE_TAGS, procState, 1);
                    pw.print(':');
                    pw.print(duration3);
                    i++;
                    duration2 = duration3;
                    NSRC2 = NSRC2;
                }
                NSRC = NSRC2;
            } else {
                NSRC = NSRC2;
                long duration4 = src.mActiveDuration + timeNow;
                if (duration4 != 0) {
                    pw.print(SmsManager.REGEX_PREFIX_DELIMITER);
                    DumpUtils.printArrayEntry(pw, DumpUtils.STATE_TAGS, src.mActiveProcState, 1);
                    pw.print(':');
                    pw.print(duration4);
                }
            }
            pw.println();
            isrc++;
            associationState = this;
            NSRC2 = NSRC;
        }
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId, long now) {
        long token;
        int NSRC;
        ProtoOutputStream protoOutputStream;
        AssociationState associationState = this;
        ProtoOutputStream protoOutputStream2 = proto;
        long token2 = proto.start(fieldId);
        protoOutputStream2.write(1138166333441L, associationState.mName);
        int NSRC2 = associationState.mSources.size();
        int isrc = 0;
        while (isrc < NSRC2) {
            SourceKey key = associationState.mSources.keyAt(isrc);
            SourceState src = associationState.mSources.valueAt(isrc);
            long sourceToken = protoOutputStream2.start(2246267895810L);
            protoOutputStream2.write(1138166333442L, key.mProcess);
            protoOutputStream2.write(1138166333447L, key.mPackage);
            protoOutputStream2.write(1120986464257L, key.mUid);
            protoOutputStream2.write(1120986464259L, src.mCount);
            long duration = src.mDuration;
            if (src.mNesting > 0) {
                duration += now - src.mStartUptime;
            }
            protoOutputStream2.write(1112396529668L, duration);
            if (src.mActiveCount != 0) {
                protoOutputStream2.write(1120986464261L, src.mActiveCount);
            }
            long timeNow = src.mActiveStartUptime != 0 ? now - src.mActiveStartUptime : 0;
            if (src.mDurations != null) {
                int N = src.mDurations.getKeyCount();
                int i = 0;
                while (i < N) {
                    int dkey = src.mDurations.getKeyAt(i);
                    long duration2 = src.mDurations.getValue(dkey);
                    if (dkey == src.mActiveProcState) {
                        duration2 += timeNow;
                    }
                    int procState = SparseMappingTable.getIdFromKey(dkey);
                    long stateToken = proto.start(2246267895814L);
                    DumpUtils.printProto(proto, 1159641169921L, DumpUtils.STATE_PROTO_ENUMS, procState, 1);
                    proto.write(1112396529666L, duration2);
                    proto.end(stateToken);
                    i++;
                    N = N;
                    NSRC2 = NSRC2;
                    token2 = token2;
                }
                token = token2;
                NSRC = NSRC2;
                protoOutputStream = proto;
            } else {
                token = token2;
                NSRC = NSRC2;
                long duration3 = src.mActiveDuration + timeNow;
                if (duration3 != 0) {
                    long stateToken2 = proto.start(2246267895814L);
                    protoOutputStream = proto;
                    DumpUtils.printProto(proto, 1159641169921L, DumpUtils.STATE_PROTO_ENUMS, src.mActiveProcState, 1);
                    protoOutputStream.write(1112396529666L, duration3);
                    protoOutputStream.end(stateToken2);
                } else {
                    protoOutputStream = proto;
                }
            }
            protoOutputStream.end(sourceToken);
            isrc++;
            protoOutputStream2 = protoOutputStream;
            NSRC2 = NSRC;
            token2 = token;
            associationState = this;
        }
        protoOutputStream2.end(token2);
    }

    public String toString() {
        return "AssociationState{" + Integer.toHexString(System.identityHashCode(this)) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.mName + " pkg=" + this.mPackageState.mPackageName + " proc=" + Integer.toHexString(System.identityHashCode(this.mProc)) + "}";
    }
}

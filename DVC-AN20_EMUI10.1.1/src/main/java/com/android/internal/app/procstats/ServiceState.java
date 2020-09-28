package com.android.internal.app.procstats;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.SystemClock;
import android.provider.SettingsStringUtil;
import android.telephony.SmsManager;
import android.util.Slog;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import java.io.PrintWriter;

public final class ServiceState {
    private static final boolean DEBUG = false;
    public static final int SERVICE_BOUND = 2;
    public static final int SERVICE_COUNT = 5;
    public static final int SERVICE_EXEC = 3;
    public static final int SERVICE_FOREGROUND = 4;
    public static final int SERVICE_RUN = 0;
    public static final int SERVICE_STARTED = 1;
    private static final String TAG = "ProcessStats";
    private int mBoundCount;
    private long mBoundStartTime;
    private int mBoundState = -1;
    private final DurationsTable mDurations;
    private int mExecCount;
    private long mExecStartTime;
    private int mExecState = -1;
    private int mForegroundCount;
    private long mForegroundStartTime;
    private int mForegroundState = -1;
    private final String mName;
    private Object mOwner;
    private final String mPackage;
    private ProcessState mProc;
    private final String mProcessName;
    private boolean mRestarting;
    private int mRunCount;
    private long mRunStartTime;
    private int mRunState = -1;
    private boolean mStarted;
    private int mStartedCount;
    private long mStartedStartTime;
    private int mStartedState = -1;

    public ServiceState(ProcessStats processStats, String pkg, String name, String processName, ProcessState proc) {
        this.mPackage = pkg;
        this.mName = name;
        this.mProcessName = processName;
        this.mProc = proc;
        this.mDurations = new DurationsTable(processStats.mTableData);
    }

    public String getPackage() {
        return this.mPackage;
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

    public void setMemFactor(int memFactor, long now) {
        if (isRestarting()) {
            setRestarting(true, memFactor, now);
        } else if (isInUse()) {
            if (this.mStartedState != -1) {
                setStarted(true, memFactor, now);
            }
            if (this.mBoundState != -1) {
                setBound(true, memFactor, now);
            }
            if (this.mExecState != -1) {
                setExecuting(true, memFactor, now);
            }
            if (this.mForegroundState != -1) {
                setForeground(true, memFactor, now);
            }
        }
    }

    public void applyNewOwner(Object newOwner) {
        Object obj = this.mOwner;
        if (obj == newOwner) {
            return;
        }
        if (obj == null) {
            this.mOwner = newOwner;
            this.mProc.incActiveServices(this.mName);
            return;
        }
        this.mOwner = newOwner;
        if (this.mStarted || this.mBoundState != -1 || this.mExecState != -1 || this.mForegroundState != -1) {
            long now = SystemClock.uptimeMillis();
            if (this.mStarted) {
                setStarted(false, 0, now);
            }
            if (this.mBoundState != -1) {
                setBound(false, 0, now);
            }
            if (this.mExecState != -1) {
                setExecuting(false, 0, now);
            }
            if (this.mForegroundState != -1) {
                setForeground(false, 0, now);
            }
        }
    }

    public void clearCurrentOwner(Object owner, boolean silently) {
        if (this.mOwner == owner) {
            this.mProc.decActiveServices(this.mName);
            if (!(!this.mStarted && this.mBoundState == -1 && this.mExecState == -1 && this.mForegroundState == -1)) {
                long now = SystemClock.uptimeMillis();
                if (this.mStarted) {
                    if (!silently) {
                        Slog.wtfStack("ProcessStats", "Service owner " + owner + " cleared while started: pkg=" + this.mPackage + " service=" + this.mName + " proc=" + this.mProc);
                    }
                    setStarted(false, 0, now);
                }
                if (this.mBoundState != -1) {
                    if (!silently) {
                        Slog.wtfStack("ProcessStats", "Service owner " + owner + " cleared while bound: pkg=" + this.mPackage + " service=" + this.mName + " proc=" + this.mProc);
                    }
                    setBound(false, 0, now);
                }
                if (this.mExecState != -1) {
                    if (!silently) {
                        Slog.wtfStack("ProcessStats", "Service owner " + owner + " cleared while exec: pkg=" + this.mPackage + " service=" + this.mName + " proc=" + this.mProc);
                    }
                    setExecuting(false, 0, now);
                }
                if (this.mForegroundState != -1) {
                    if (!silently) {
                        Slog.wtfStack("ProcessStats", "Service owner " + owner + " cleared while foreground: pkg=" + this.mPackage + " service=" + this.mName + " proc=" + this.mProc);
                    }
                    setForeground(false, 0, now);
                }
            }
            this.mOwner = null;
        }
    }

    public boolean isInUse() {
        return this.mOwner != null || this.mRestarting;
    }

    public boolean isRestarting() {
        return this.mRestarting;
    }

    public void add(ServiceState other) {
        this.mDurations.addDurations(other.mDurations);
        this.mRunCount += other.mRunCount;
        this.mStartedCount += other.mStartedCount;
        this.mBoundCount += other.mBoundCount;
        this.mExecCount += other.mExecCount;
        this.mForegroundCount += other.mForegroundCount;
    }

    public void resetSafely(long now) {
        this.mDurations.resetTable();
        int i = 1;
        this.mRunCount = this.mRunState != -1 ? 1 : 0;
        this.mStartedCount = this.mStartedState != -1 ? 1 : 0;
        this.mBoundCount = this.mBoundState != -1 ? 1 : 0;
        this.mExecCount = this.mExecState != -1 ? 1 : 0;
        if (this.mForegroundState == -1) {
            i = 0;
        }
        this.mForegroundCount = i;
        this.mForegroundStartTime = now;
        this.mExecStartTime = now;
        this.mBoundStartTime = now;
        this.mStartedStartTime = now;
        this.mRunStartTime = now;
    }

    public void writeToParcel(Parcel out, long now) {
        this.mDurations.writeToParcel(out);
        out.writeInt(this.mRunCount);
        out.writeInt(this.mStartedCount);
        out.writeInt(this.mBoundCount);
        out.writeInt(this.mExecCount);
        out.writeInt(this.mForegroundCount);
    }

    public boolean readFromParcel(Parcel in) {
        if (!this.mDurations.readFromParcel(in)) {
            return false;
        }
        this.mRunCount = in.readInt();
        this.mStartedCount = in.readInt();
        this.mBoundCount = in.readInt();
        this.mExecCount = in.readInt();
        this.mForegroundCount = in.readInt();
        return true;
    }

    public void commitStateTime(long now) {
        int i = this.mRunState;
        if (i != -1) {
            this.mDurations.addDuration((i * 5) + 0, now - this.mRunStartTime);
            this.mRunStartTime = now;
        }
        int i2 = this.mStartedState;
        if (i2 != -1) {
            this.mDurations.addDuration((i2 * 5) + 1, now - this.mStartedStartTime);
            this.mStartedStartTime = now;
        }
        int i3 = this.mBoundState;
        if (i3 != -1) {
            this.mDurations.addDuration((i3 * 5) + 2, now - this.mBoundStartTime);
            this.mBoundStartTime = now;
        }
        int i4 = this.mExecState;
        if (i4 != -1) {
            this.mDurations.addDuration((i4 * 5) + 3, now - this.mExecStartTime);
            this.mExecStartTime = now;
        }
        int i5 = this.mForegroundState;
        if (i5 != -1) {
            this.mDurations.addDuration((i5 * 5) + 4, now - this.mForegroundStartTime);
            this.mForegroundStartTime = now;
        }
    }

    private void updateRunning(int memFactor, long now) {
        int state = (this.mStartedState == -1 && this.mBoundState == -1 && this.mExecState == -1 && this.mForegroundState == -1) ? -1 : memFactor;
        int i = this.mRunState;
        if (i != state) {
            if (i != -1) {
                this.mDurations.addDuration((i * 5) + 0, now - this.mRunStartTime);
            } else if (state != -1) {
                this.mRunCount++;
            }
            this.mRunState = state;
            this.mRunStartTime = now;
        }
    }

    public void setStarted(boolean started, int memFactor, long now) {
        if (this.mOwner == null) {
            Slog.wtf("ProcessStats", "Starting service " + this + " without owner");
        }
        this.mStarted = started;
        updateStartedState(memFactor, now);
    }

    public void setRestarting(boolean restarting, int memFactor, long now) {
        this.mRestarting = restarting;
        updateStartedState(memFactor, now);
    }

    public void updateStartedState(int memFactor, long now) {
        boolean started = false;
        boolean wasStarted = this.mStartedState != -1;
        if (this.mStarted || this.mRestarting) {
            started = true;
        }
        int state = started ? memFactor : -1;
        int i = this.mStartedState;
        if (i != state) {
            if (i != -1) {
                this.mDurations.addDuration((i * 5) + 1, now - this.mStartedStartTime);
            } else if (started) {
                this.mStartedCount++;
            }
            this.mStartedState = state;
            this.mStartedStartTime = now;
            this.mProc = this.mProc.pullFixedProc(this.mPackage);
            if (wasStarted != started) {
                if (started) {
                    this.mProc.incStartedServices(memFactor, now, this.mName);
                } else {
                    this.mProc.decStartedServices(memFactor, now, this.mName);
                }
            }
            updateRunning(memFactor, now);
        }
    }

    public void setBound(boolean bound, int memFactor, long now) {
        if (this.mOwner == null) {
            Slog.wtf("ProcessStats", "Binding service " + this + " without owner");
        }
        int state = bound ? memFactor : -1;
        int i = this.mBoundState;
        if (i != state) {
            if (i != -1) {
                this.mDurations.addDuration((i * 5) + 2, now - this.mBoundStartTime);
            } else if (bound) {
                this.mBoundCount++;
            }
            this.mBoundState = state;
            this.mBoundStartTime = now;
            updateRunning(memFactor, now);
        }
    }

    public void setExecuting(boolean executing, int memFactor, long now) {
        if (this.mOwner == null) {
            Slog.wtf("ProcessStats", "Executing service " + this + " without owner");
        }
        int state = executing ? memFactor : -1;
        int i = this.mExecState;
        if (i != state) {
            if (i != -1) {
                this.mDurations.addDuration((i * 5) + 3, now - this.mExecStartTime);
            } else if (executing) {
                this.mExecCount++;
            }
            this.mExecState = state;
            this.mExecStartTime = now;
            updateRunning(memFactor, now);
        }
    }

    public void setForeground(boolean foreground, int memFactor, long now) {
        if (this.mOwner == null) {
            Slog.wtf("ProcessStats", "Foregrounding service " + this + " without owner");
        }
        int state = foreground ? memFactor : -1;
        int i = this.mForegroundState;
        if (i != state) {
            if (i != -1) {
                this.mDurations.addDuration((i * 5) + 4, now - this.mForegroundStartTime);
            } else if (foreground) {
                this.mForegroundCount++;
            }
            this.mForegroundState = state;
            this.mForegroundStartTime = now;
            updateRunning(memFactor, now);
        }
    }

    public long getDuration(int opType, int curState, long startTime, int memFactor, long now) {
        long time = this.mDurations.getValueForId((byte) ((memFactor * 5) + opType));
        if (curState == memFactor) {
            return time + (now - startTime);
        }
        return time;
    }

    public void dumpStats(PrintWriter pw, String prefix, String prefixInner, String headerPrefix, long now, long totalTime, boolean dumpSummary, boolean dumpAll) {
        PrintWriter printWriter;
        boolean z = false;
        dumpStats(pw, prefix, prefixInner, headerPrefix, "Running", this.mRunCount, 0, this.mRunState, this.mRunStartTime, now, totalTime, !dumpSummary || dumpAll);
        dumpStats(pw, prefix, prefixInner, headerPrefix, "Started", this.mStartedCount, 1, this.mStartedState, this.mStartedStartTime, now, totalTime, !dumpSummary || dumpAll);
        dumpStats(pw, prefix, prefixInner, headerPrefix, "Foreground", this.mForegroundCount, 4, this.mForegroundState, this.mForegroundStartTime, now, totalTime, !dumpSummary || dumpAll);
        dumpStats(pw, prefix, prefixInner, headerPrefix, "Bound", this.mBoundCount, 2, this.mBoundState, this.mBoundStartTime, now, totalTime, !dumpSummary || dumpAll);
        int i = this.mExecCount;
        int i2 = this.mExecState;
        long j = this.mExecStartTime;
        if (!dumpSummary || dumpAll) {
            z = true;
        }
        dumpStats(pw, prefix, prefixInner, headerPrefix, "Executing", i, 3, i2, j, now, totalTime, z);
        if (dumpAll) {
            if (this.mOwner != null) {
                printWriter = pw;
                printWriter.print("        mOwner=");
                printWriter.println(this.mOwner);
            } else {
                printWriter = pw;
            }
            if (this.mStarted || this.mRestarting) {
                printWriter.print("        mStarted=");
                printWriter.print(this.mStarted);
                printWriter.print(" mRestarting=");
                printWriter.println(this.mRestarting);
            }
        }
    }

    private void dumpStats(PrintWriter pw, String prefix, String prefixInner, String headerPrefix, String header, int count, int serviceType, int state, long startTime, long now, long totalTime, boolean dumpAll) {
        if (count == 0) {
            return;
        }
        if (dumpAll) {
            pw.print(prefix);
            pw.print(header);
            pw.print(" op count ");
            pw.print(count);
            pw.println(SettingsStringUtil.DELIMITER);
            dumpTime(pw, prefixInner, serviceType, state, startTime, now);
            return;
        }
        long myTime = dumpTimeInternal(null, null, serviceType, state, startTime, now, true);
        pw.print(prefix);
        pw.print(headerPrefix);
        pw.print(header);
        pw.print(" count ");
        pw.print(count);
        pw.print(" / time ");
        boolean isRunning = myTime < 0;
        if (isRunning) {
            myTime = -myTime;
        }
        DumpUtils.printPercent(pw, ((double) myTime) / ((double) totalTime));
        if (isRunning) {
            pw.print(" (running)");
        }
        pw.println();
    }

    public long dumpTime(PrintWriter pw, String prefix, int serviceType, int curState, long curStartTime, long now) {
        return dumpTimeInternal(pw, prefix, serviceType, curState, curStartTime, now, false);
    }

    /* access modifiers changed from: package-private */
    public long dumpTimeInternal(PrintWriter pw, String prefix, int serviceType, int curState, long curStartTime, long now, boolean negativeIfRunning) {
        long totalTime = 0;
        int printedScreen = -1;
        boolean isRunning = false;
        int iscreen = 0;
        while (true) {
            long j = 0;
            if (iscreen >= 8) {
                break;
            }
            int printedMem = -1;
            int imem = 0;
            while (imem < 4) {
                int state = imem + iscreen;
                long time = getDuration(serviceType, curState, curStartTime, state, now);
                String running = "";
                if (curState == state && pw != null) {
                    running = " (running)";
                    isRunning = true;
                }
                if (time != j) {
                    if (pw != null) {
                        pw.print(prefix);
                        DumpUtils.printScreenLabel(pw, printedScreen != iscreen ? iscreen : -1);
                        printedScreen = iscreen;
                        DumpUtils.printMemLabel(pw, printedMem != imem ? imem : -1, 0);
                        printedMem = imem;
                        pw.print(": ");
                        TimeUtils.formatDuration(time, pw);
                        pw.println(running);
                    }
                    totalTime += time;
                }
                imem++;
                j = 0;
            }
            iscreen += 4;
        }
        if (!(totalTime == 0 || pw == null)) {
            pw.print(prefix);
            pw.print("    TOTAL: ");
            TimeUtils.formatDuration(totalTime, pw);
            pw.println();
        }
        return (!isRunning || !negativeIfRunning) ? totalTime : -totalTime;
    }

    public void dumpTimesCheckin(PrintWriter pw, String pkgName, int uid, long vers, String serviceName, long now) {
        dumpTimeCheckin(pw, "pkgsvc-run", pkgName, uid, vers, serviceName, 0, this.mRunCount, this.mRunState, this.mRunStartTime, now);
        dumpTimeCheckin(pw, "pkgsvc-start", pkgName, uid, vers, serviceName, 1, this.mStartedCount, this.mStartedState, this.mStartedStartTime, now);
        dumpTimeCheckin(pw, "pkgsvc-fg", pkgName, uid, vers, serviceName, 4, this.mForegroundCount, this.mForegroundState, this.mForegroundStartTime, now);
        dumpTimeCheckin(pw, "pkgsvc-bound", pkgName, uid, vers, serviceName, 2, this.mBoundCount, this.mBoundState, this.mBoundStartTime, now);
        dumpTimeCheckin(pw, "pkgsvc-exec", pkgName, uid, vers, serviceName, 3, this.mExecCount, this.mExecState, this.mExecStartTime, now);
    }

    private void dumpTimeCheckin(PrintWriter pw, String label, String packageName, int uid, long vers, String serviceName, int serviceType, int opCount, int curState, long curStartTime, long now) {
        ServiceState serviceState = this;
        if (opCount > 0) {
            pw.print(label);
            pw.print(SmsManager.REGEX_PREFIX_DELIMITER);
            pw.print(packageName);
            pw.print(SmsManager.REGEX_PREFIX_DELIMITER);
            pw.print(uid);
            pw.print(SmsManager.REGEX_PREFIX_DELIMITER);
            pw.print(vers);
            pw.print(SmsManager.REGEX_PREFIX_DELIMITER);
            pw.print(serviceName);
            pw.print(SmsManager.REGEX_PREFIX_DELIMITER);
            pw.print(opCount);
            boolean didCurState = false;
            int N = serviceState.mDurations.getKeyCount();
            int i = 0;
            while (i < N) {
                int key = serviceState.mDurations.getKeyAt(i);
                long time = serviceState.mDurations.getValue(key);
                int type = SparseMappingTable.getIdFromKey(key);
                int memFactor = type / 5;
                if (type % 5 == serviceType) {
                    if (curState == memFactor) {
                        didCurState = true;
                        time += now - curStartTime;
                    }
                    DumpUtils.printAdjTagAndValue(pw, memFactor, time);
                }
                i++;
                serviceState = this;
            }
            if (!didCurState && curState != -1) {
                DumpUtils.printAdjTagAndValue(pw, curState, now - curStartTime);
            }
            pw.println();
        }
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId, long now) {
        long token = proto.start(fieldId);
        proto.write(1138166333441L, this.mName);
        writeTypeToProto(proto, 2246267895810L, 1, 0, this.mRunCount, this.mRunState, this.mRunStartTime, now);
        writeTypeToProto(proto, 2246267895810L, 2, 1, this.mStartedCount, this.mStartedState, this.mStartedStartTime, now);
        writeTypeToProto(proto, 2246267895810L, 3, 4, this.mForegroundCount, this.mForegroundState, this.mForegroundStartTime, now);
        writeTypeToProto(proto, 2246267895810L, 4, 2, this.mBoundCount, this.mBoundState, this.mBoundStartTime, now);
        writeTypeToProto(proto, 2246267895810L, 5, 3, this.mExecCount, this.mExecState, this.mExecStartTime, now);
        proto.end(token);
    }

    public void writeTypeToProto(ProtoOutputStream proto, long fieldId, int opType, int serviceType, int opCount, int curState, long curStartTime, long now) {
        long time;
        if (opCount > 0) {
            long token = proto.start(fieldId);
            proto.write(1159641169921L, opType);
            proto.write(1120986464258L, opCount);
            int N = this.mDurations.getKeyCount();
            boolean didCurState = false;
            for (int i = 0; i < N; i++) {
                int key = this.mDurations.getKeyAt(i);
                long time2 = this.mDurations.getValue(key);
                int type = SparseMappingTable.getIdFromKey(key);
                int memFactor = type / 5;
                int type2 = type % 5;
                if (type2 == serviceType) {
                    if (curState == memFactor) {
                        didCurState = true;
                        time = time2 + (now - curStartTime);
                    } else {
                        time = time2;
                    }
                    long stateToken = proto.start(2246267895811L);
                    DumpUtils.printProcStateAdjTagProto(proto, 1159641169921L, 1159641169922L, type2);
                    proto.write(1112396529667L, time);
                    proto.end(stateToken);
                }
            }
            if (!didCurState && curState != -1) {
                long stateToken2 = proto.start(2246267895811L);
                DumpUtils.printProcStateAdjTagProto(proto, 1159641169921L, 1159641169922L, curState);
                proto.write(1112396529667L, now - curStartTime);
                proto.end(stateToken2);
            }
            proto.end(token);
        }
    }

    public String toString() {
        return "ServiceState{" + Integer.toHexString(System.identityHashCode(this)) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.mName + " pkg=" + this.mPackage + " proc=" + Integer.toHexString(System.identityHashCode(this.mProc)) + "}";
    }
}

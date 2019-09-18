package com.android.server.am;

import android.os.Binder;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.AtomicFile;
import android.util.LongSparseArray;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.procstats.DumpUtils;
import com.android.internal.app.procstats.IProcessStats;
import com.android.internal.app.procstats.ProcessState;
import com.android.internal.app.procstats.ProcessStats;
import com.android.internal.app.procstats.ServiceState;
import com.android.internal.os.BackgroundThread;
import com.android.server.utils.PriorityDump;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public final class ProcessStatsService extends IProcessStats.Stub {
    static final boolean DEBUG = false;
    static final int MAX_HISTORIC_STATES = 8;
    static final String STATE_FILE_CHECKIN_SUFFIX = ".ci";
    static final String STATE_FILE_PREFIX = "state-";
    static final String STATE_FILE_SUFFIX = ".bin";
    static final String TAG = "ProcessStatsService";
    static long WRITE_PERIOD = 1800000;
    final ActivityManagerService mAm;
    final File mBaseDir;
    boolean mCommitPending;
    AtomicFile mFile;
    @GuardedBy("mAm")
    Boolean mInjectedScreenState;
    int mLastMemOnlyState = -1;
    long mLastWriteTime;
    boolean mMemFactorLowered;
    Parcel mPendingWrite;
    boolean mPendingWriteCommitted;
    AtomicFile mPendingWriteFile;
    final Object mPendingWriteLock = new Object();
    ProcessStats mProcessStats;
    boolean mShuttingDown;
    final ReentrantLock mWriteLock = new ReentrantLock();

    public ProcessStatsService(ActivityManagerService am, File file) {
        this.mAm = am;
        this.mBaseDir = file;
        this.mBaseDir.mkdirs();
        this.mProcessStats = new ProcessStats(true);
        updateFile();
        SystemProperties.addChangeCallback(new Runnable() {
            public void run() {
                synchronized (ProcessStatsService.this.mAm) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        if (ProcessStatsService.this.mProcessStats.evaluateSystemProperties(false)) {
                            ProcessStatsService.this.mProcessStats.mFlags |= 4;
                            ProcessStatsService.this.writeStateLocked(true, true);
                            ProcessStatsService.this.mProcessStats.evaluateSystemProperties(true);
                        }
                    } catch (Throwable th) {
                        while (true) {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                }
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        });
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        try {
            return ProcessStatsService.super.onTransact(code, data, reply, flags);
        } catch (RuntimeException e) {
            if (!(e instanceof SecurityException)) {
                Slog.wtf(TAG, "Process Stats Crash", e);
            }
            throw e;
        }
    }

    public ProcessState getProcessStateLocked(String packageName, int uid, long versionCode, String processName) {
        return this.mProcessStats.getProcessStateLocked(packageName, uid, versionCode, processName);
    }

    public ServiceState getServiceStateLocked(String packageName, int uid, long versionCode, String processName, String className) {
        return this.mProcessStats.getServiceStateLocked(packageName, uid, versionCode, processName, className);
    }

    public boolean isMemFactorLowered() {
        return this.mMemFactorLowered;
    }

    @GuardedBy("mAm")
    public boolean setMemFactorLocked(int memFactor, boolean screenOn, long now) {
        this.mMemFactorLowered = memFactor < this.mLastMemOnlyState;
        this.mLastMemOnlyState = memFactor;
        if (this.mInjectedScreenState != null) {
            screenOn = this.mInjectedScreenState.booleanValue();
        }
        if (screenOn) {
            memFactor += 4;
        }
        if (memFactor == this.mProcessStats.mMemFactor) {
            return false;
        }
        if (this.mProcessStats.mMemFactor != -1) {
            long[] jArr = this.mProcessStats.mMemFactorDurations;
            int i = this.mProcessStats.mMemFactor;
            jArr[i] = jArr[i] + (now - this.mProcessStats.mStartTime);
        }
        this.mProcessStats.mMemFactor = memFactor;
        this.mProcessStats.mStartTime = now;
        ArrayMap<String, SparseArray<LongSparseArray<ProcessStats.PackageState>>> pmap = this.mProcessStats.mPackages.getMap();
        for (int ipkg = pmap.size() - 1; ipkg >= 0; ipkg--) {
            SparseArray<LongSparseArray<ProcessStats.PackageState>> uids = pmap.valueAt(ipkg);
            for (int iuid = uids.size() - 1; iuid >= 0; iuid--) {
                LongSparseArray<ProcessStats.PackageState> vers = uids.valueAt(iuid);
                for (int iver = vers.size() - 1; iver >= 0; iver--) {
                    ArrayMap<String, ServiceState> services = vers.valueAt(iver).mServices;
                    for (int isvc = services.size() - 1; isvc >= 0; isvc--) {
                        services.valueAt(isvc).setMemFactor(memFactor, now);
                    }
                }
            }
        }
        return true;
    }

    public int getMemFactorLocked() {
        if (this.mProcessStats.mMemFactor != -1) {
            return this.mProcessStats.mMemFactor;
        }
        return 0;
    }

    public void addSysMemUsageLocked(long cachedMem, long freeMem, long zramMem, long kernelMem, long nativeMem) {
        this.mProcessStats.addSysMemUsage(cachedMem, freeMem, zramMem, kernelMem, nativeMem);
    }

    public boolean shouldWriteNowLocked(long now) {
        if (now <= this.mLastWriteTime + WRITE_PERIOD) {
            return false;
        }
        if (SystemClock.elapsedRealtime() > this.mProcessStats.mTimePeriodStartRealtime + ProcessStats.COMMIT_PERIOD && SystemClock.uptimeMillis() > this.mProcessStats.mTimePeriodStartUptime + ProcessStats.COMMIT_UPTIME_PERIOD) {
            this.mCommitPending = true;
        }
        return true;
    }

    public void shutdownLocked() {
        Slog.w(TAG, "Writing process stats before shutdown...");
        this.mProcessStats.mFlags |= 2;
        writeStateSyncLocked();
        this.mShuttingDown = true;
    }

    public void writeStateAsyncLocked() {
        writeStateLocked(false);
    }

    public void writeStateSyncLocked() {
        writeStateLocked(true);
    }

    private void writeStateLocked(boolean sync) {
        if (!this.mShuttingDown) {
            boolean commitPending = this.mCommitPending;
            this.mCommitPending = false;
            writeStateLocked(sync, commitPending);
        }
    }

    public void writeStateLocked(boolean sync, boolean commit) {
        synchronized (this.mPendingWriteLock) {
            long now = SystemClock.uptimeMillis();
            if (this.mPendingWrite == null || !this.mPendingWriteCommitted) {
                this.mPendingWrite = Parcel.obtain();
                this.mProcessStats.mTimePeriodEndRealtime = SystemClock.elapsedRealtime();
                this.mProcessStats.mTimePeriodEndUptime = now;
                if (commit) {
                    this.mProcessStats.mFlags |= 1;
                }
                this.mProcessStats.writeToParcel(this.mPendingWrite, 0);
                this.mPendingWriteFile = new AtomicFile(this.mFile.getBaseFile());
                this.mPendingWriteCommitted = commit;
            }
            if (commit) {
                this.mProcessStats.resetSafely();
                updateFile();
                this.mAm.requestPssAllProcsLocked(SystemClock.uptimeMillis(), true, false);
            }
            this.mLastWriteTime = SystemClock.uptimeMillis();
            final long totalTime = SystemClock.uptimeMillis() - now;
            if (!sync) {
                BackgroundThread.getHandler().post(new Runnable() {
                    public void run() {
                        ProcessStatsService.this.performWriteState(totalTime);
                    }
                });
            } else {
                performWriteState(totalTime);
            }
        }
    }

    private void updateFile() {
        File file = this.mBaseDir;
        this.mFile = new AtomicFile(new File(file, STATE_FILE_PREFIX + this.mProcessStats.mTimePeriodStartClockStr + STATE_FILE_SUFFIX));
        this.mLastWriteTime = SystemClock.uptimeMillis();
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:11:?, code lost:
        r0 = r2.startWrite();
        r0.write(r1.marshall());
        r0.flush();
        r2.finishWrite(r0);
        com.android.internal.logging.EventLogTags.writeCommitSysConfigFile("procstats", (android.os.SystemClock.uptimeMillis() - r4) + r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x003d, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x003f, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
        android.util.Slog.w(TAG, "Error writing process statistics", r3);
        r2.failWrite(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0057, code lost:
        r1.recycle();
        trimHistoricStatesWriteLocked();
        r8.mWriteLock.unlock();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0062, code lost:
        throw r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0019, code lost:
        r4 = android.os.SystemClock.uptimeMillis();
        r0 = null;
     */
    public void performWriteState(long initialTime) {
        Parcel data;
        synchronized (this.mPendingWriteLock) {
            data = this.mPendingWrite;
            AtomicFile file = this.mPendingWriteFile;
            this.mPendingWriteCommitted = false;
            if (data != null) {
                this.mPendingWrite = null;
                this.mPendingWriteFile = null;
                this.mWriteLock.lock();
            } else {
                return;
            }
        }
        data.recycle();
        trimHistoricStatesWriteLocked();
        this.mWriteLock.unlock();
    }

    /* access modifiers changed from: package-private */
    public boolean readLocked(ProcessStats stats, AtomicFile file) {
        try {
            FileInputStream stream = file.openRead();
            stats.read(stream);
            stream.close();
            if (stats.mReadError == null) {
                return true;
            }
            Slog.w(TAG, "Ignoring existing stats; " + stats.mReadError);
            return false;
        } catch (Throwable e) {
            stats.mReadError = "caught exception: " + e;
            Slog.e(TAG, "Error reading process statistics", e);
            return false;
        }
    }

    private ArrayList<String> getCommittedFiles(int minNum, boolean inclCurrent, boolean inclCheckedIn) {
        File[] files = this.mBaseDir.listFiles();
        if (files == null || files.length <= minNum) {
            return null;
        }
        ArrayList<String> filesArray = new ArrayList<>(files.length);
        String currentFile = this.mFile.getBaseFile().getPath();
        for (File file : files) {
            String fileStr = file.getPath();
            if ((inclCheckedIn || !fileStr.endsWith(STATE_FILE_CHECKIN_SUFFIX)) && (inclCurrent || !fileStr.equals(currentFile))) {
                filesArray.add(fileStr);
            }
        }
        Collections.sort(filesArray);
        return filesArray;
    }

    public void trimHistoricStatesWriteLocked() {
        ArrayList<String> filesArray = getCommittedFiles(8, false, true);
        if (filesArray != null) {
            while (filesArray.size() > 8) {
                String file = filesArray.remove(0);
                Slog.i(TAG, "Pruning old procstats: " + file);
                new File(file).delete();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean dumpFilteredProcessesCsvLocked(PrintWriter pw, String header, boolean sepScreenStates, int[] screenStates, boolean sepMemStates, int[] memStates, boolean sepProcStates, int[] procStates, long now, String reqPackage) {
        ArrayList<ProcessState> procs = this.mProcessStats.collectProcessesLocked(screenStates, memStates, procStates, procStates, now, reqPackage, false);
        if (procs.size() <= 0) {
            return false;
        }
        if (header != null) {
            pw.println(header);
        }
        DumpUtils.dumpProcessListCsv(pw, procs, sepScreenStates, screenStates, sepMemStates, memStates, sepProcStates, procStates, now);
        return true;
    }

    static int[] parseStateList(String[] states, int mult, String arg, boolean[] outSep, String[] outError) {
        ArrayList<Integer> res = new ArrayList<>();
        int lastPos = 0;
        int i = 0;
        while (i <= arg.length()) {
            char c = i < arg.length() ? arg.charAt(i) : 0;
            if (c == ',' || c == '+' || c == ' ' || c == 0) {
                boolean isSep = c == ',';
                if (lastPos == 0) {
                    outSep[0] = isSep;
                } else if (!(c == 0 || outSep[0] == isSep)) {
                    outError[0] = "inconsistent separators (can't mix ',' with '+')";
                    return null;
                }
                if (lastPos < i - 1) {
                    String str = arg.substring(lastPos, i);
                    int j = 0;
                    while (true) {
                        if (j >= states.length) {
                            break;
                        } else if (str.equals(states[j])) {
                            res.add(Integer.valueOf(j));
                            str = null;
                            break;
                        } else {
                            j++;
                        }
                    }
                    if (str != null) {
                        outError[0] = "invalid word \"" + str + "\"";
                        return null;
                    }
                }
                lastPos = i + 1;
            }
            i++;
        }
        int[] finalRes = new int[res.size()];
        for (int i2 = 0; i2 < res.size(); i2++) {
            finalRes[i2] = res.get(i2).intValue() * mult;
        }
        return finalRes;
    }

    public byte[] getCurrentStats(List<ParcelFileDescriptor> historic) {
        ArrayList<String> files;
        int i;
        this.mAm.mContext.enforceCallingOrSelfPermission("android.permission.PACKAGE_USAGE_STATS", null);
        Parcel current = Parcel.obtain();
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                long now = SystemClock.uptimeMillis();
                this.mProcessStats.mTimePeriodEndRealtime = SystemClock.elapsedRealtime();
                this.mProcessStats.mTimePeriodEndUptime = now;
                this.mProcessStats.writeToParcel(current, now, 0);
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        this.mWriteLock.lock();
        if (historic != null) {
            try {
                files = getCommittedFiles(0, false, true);
                if (files != null) {
                    int i2 = files.size() - 1;
                    while (true) {
                        i = i2;
                        if (i < 0) {
                            break;
                        }
                        historic.add(ParcelFileDescriptor.open(new File(files.get(i)), 268435456));
                        i2 = i - 1;
                    }
                }
            } catch (IOException e) {
                Slog.w(TAG, "Failure opening procstat file " + files.get(i), e);
            } catch (Throwable th2) {
                this.mWriteLock.unlock();
                throw th2;
            }
        }
        this.mWriteLock.unlock();
        return current.marshall();
    }

    public ParcelFileDescriptor getStatsOverTime(long minTime) {
        boolean z;
        long curTime;
        long curTime2;
        this.mAm.mContext.enforceCallingOrSelfPermission("android.permission.PACKAGE_USAGE_STATS", null);
        Parcel current = Parcel.obtain();
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                long now = SystemClock.uptimeMillis();
                this.mProcessStats.mTimePeriodEndRealtime = SystemClock.elapsedRealtime();
                this.mProcessStats.mTimePeriodEndUptime = now;
                z = false;
                this.mProcessStats.writeToParcel(current, now, 0);
                curTime = this.mProcessStats.mTimePeriodEndRealtime - this.mProcessStats.mTimePeriodStartRealtime;
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        this.mWriteLock.lock();
        if (curTime < minTime) {
            try {
                ArrayList<String> files = getCommittedFiles(0, false, true);
                if (files != null && files.size() > 0) {
                    current.setDataPosition(0);
                    ProcessStats stats = (ProcessStats) ProcessStats.CREATOR.createFromParcel(current);
                    current.recycle();
                    int i = files.size() - 1;
                    while (true) {
                        int i2 = i;
                        if (i2 < 0 || stats.mTimePeriodEndRealtime - stats.mTimePeriodStartRealtime >= minTime) {
                            current = Parcel.obtain();
                            stats.writeToParcel(current, 0);
                        } else {
                            AtomicFile file = new AtomicFile(new File(files.get(i2)));
                            int i3 = i2 - 1;
                            ProcessStats moreStats = new ProcessStats(z);
                            readLocked(moreStats, file);
                            if (moreStats.mReadError == null) {
                                stats.add(moreStats);
                                StringBuilder sb = new StringBuilder();
                                sb.append("Added stats: ");
                                sb.append(moreStats.mTimePeriodStartClockStr);
                                sb.append(", over ");
                                curTime2 = curTime;
                                try {
                                    TimeUtils.formatDuration(moreStats.mTimePeriodEndRealtime - moreStats.mTimePeriodStartRealtime, sb);
                                    Slog.i(TAG, sb.toString());
                                } catch (IOException e) {
                                    e = e;
                                    try {
                                        Slog.w(TAG, "Failed building output pipe", e);
                                        this.mWriteLock.unlock();
                                        return null;
                                    } catch (Throwable th2) {
                                        th = th2;
                                        this.mWriteLock.unlock();
                                        throw th;
                                    }
                                }
                            } else {
                                curTime2 = curTime;
                                Slog.w(TAG, "Failure reading " + files.get(i3 + 1) + "; " + moreStats.mReadError);
                            }
                            i = i3;
                            curTime = curTime2;
                            z = false;
                        }
                    }
                    current = Parcel.obtain();
                    stats.writeToParcel(current, 0);
                    final byte[] outData = current.marshall();
                    current.recycle();
                    final ParcelFileDescriptor[] fds = ParcelFileDescriptor.createPipe();
                    new Thread("ProcessStats pipe output") {
                        public void run() {
                            FileOutputStream fout = new ParcelFileDescriptor.AutoCloseOutputStream(fds[1]);
                            try {
                                fout.write(outData);
                                fout.close();
                            } catch (IOException e) {
                                Slog.w(ProcessStatsService.TAG, "Failure writing pipe", e);
                            }
                        }
                    }.start();
                    ParcelFileDescriptor parcelFileDescriptor = fds[0];
                    this.mWriteLock.unlock();
                    return parcelFileDescriptor;
                }
            } catch (IOException e2) {
                e = e2;
                long j = curTime;
                Slog.w(TAG, "Failed building output pipe", e);
                this.mWriteLock.unlock();
                return null;
            } catch (Throwable th3) {
                th = th3;
                long j2 = curTime;
                this.mWriteLock.unlock();
                throw th;
            }
        }
        final byte[] outData2 = current.marshall();
        current.recycle();
        final ParcelFileDescriptor[] fds2 = ParcelFileDescriptor.createPipe();
        new Thread("ProcessStats pipe output") {
            public void run() {
                FileOutputStream fout = new ParcelFileDescriptor.AutoCloseOutputStream(fds2[1]);
                try {
                    fout.write(outData2);
                    fout.close();
                } catch (IOException e) {
                    Slog.w(ProcessStatsService.TAG, "Failure writing pipe", e);
                }
            }
        }.start();
        ParcelFileDescriptor parcelFileDescriptor2 = fds2[0];
        this.mWriteLock.unlock();
        return parcelFileDescriptor2;
    }

    public int getCurrentMemoryState() {
        int i;
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                i = this.mLastMemOnlyState;
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        return i;
    }

    private void dumpAggregatedStats(PrintWriter pw, long aggregateHours, long now, String reqPackage, boolean isCompact, boolean dumpDetails, boolean dumpFullDetails, boolean dumpAll, boolean activeOnly) {
        PrintWriter printWriter = pw;
        ParcelFileDescriptor pfd = getStatsOverTime((((aggregateHours * 60) * 60) * 1000) - (ProcessStats.COMMIT_PERIOD / 2));
        if (pfd == null) {
            printWriter.println("Unable to build stats!");
            return;
        }
        ProcessStats stats = new ProcessStats(false);
        stats.read(new ParcelFileDescriptor.AutoCloseInputStream(pfd));
        if (stats.mReadError != null) {
            printWriter.print("Failure reading: ");
            printWriter.println(stats.mReadError);
            return;
        }
        if (isCompact) {
            stats.dumpCheckinLocked(printWriter, reqPackage);
        } else {
            String str = reqPackage;
            if (dumpDetails || dumpFullDetails) {
                stats.dumpLocked(printWriter, str, now, !dumpFullDetails, dumpAll, activeOnly);
            } else {
                stats.dumpSummaryLocked(printWriter, str, now, activeOnly);
            }
        }
    }

    private static void dumpHelp(PrintWriter pw) {
        pw.println("Process stats (procstats) dump options:");
        pw.println("    [--checkin|-c|--csv] [--csv-screen] [--csv-proc] [--csv-mem]");
        pw.println("    [--details] [--full-details] [--current] [--hours N] [--last N]");
        pw.println("    [--max N] --active] [--commit] [--reset] [--clear] [--write] [-h]");
        pw.println("    [--start-testing] [--stop-testing] ");
        pw.println("    [--pretend-screen-on] [--pretend-screen-off] [--stop-pretend-screen]");
        pw.println("    [<package.name>]");
        pw.println("  --checkin: perform a checkin: print and delete old committed states.");
        pw.println("  -c: print only state in checkin format.");
        pw.println("  --csv: output data suitable for putting in a spreadsheet.");
        pw.println("  --csv-screen: on, off.");
        pw.println("  --csv-mem: norm, mod, low, crit.");
        pw.println("  --csv-proc: pers, top, fore, vis, precept, backup,");
        pw.println("    service, home, prev, cached");
        pw.println("  --details: dump per-package details, not just summary.");
        pw.println("  --full-details: dump all timing and active state details.");
        pw.println("  --current: only dump current state.");
        pw.println("  --hours: aggregate over about N last hours.");
        pw.println("  --last: only show the last committed stats at index N (starting at 1).");
        pw.println("  --max: for -a, max num of historical batches to print.");
        pw.println("  --active: only show currently active processes/services.");
        pw.println("  --commit: commit current stats to disk and reset to start new stats.");
        pw.println("  --reset: reset current stats, without committing.");
        pw.println("  --clear: clear all stats; does both --reset and deletes old stats.");
        pw.println("  --write: write current in-memory stats to disk.");
        pw.println("  --read: replace current stats with last-written stats.");
        pw.println("  --start-testing: clear all stats and starting high frequency pss sampling.");
        pw.println("  --stop-testing: stop high frequency pss sampling.");
        pw.println("  --pretend-screen-on: pretend screen is on.");
        pw.println("  --pretend-screen-off: pretend screen is off.");
        pw.println("  --stop-pretend-screen: forget \"pretend screen\" and use the real state.");
        pw.println("  -a: print everything.");
        pw.println("  -h: print this help text.");
        pw.println("  <package.name>: optional name of package to filter output by.");
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (com.android.internal.util.DumpUtils.checkDumpAndUsageStatsPermission(this.mAm.mContext, TAG, pw)) {
            long ident = Binder.clearCallingIdentity();
            try {
                if (args.length <= 0 || !PriorityDump.PROTO_ARG.equals(args[0])) {
                    dumpInner(pw, args);
                } else {
                    dumpProto(fd);
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:122:0x02c3, code lost:
        com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:453:0x084f, code lost:
        com.android.server.am.ActivityManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:454:0x0852, code lost:
        if (r33 != false) goto L_0x0883;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:455:0x0854, code lost:
        if (r29 == false) goto L_0x0859;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:456:0x0856, code lost:
        r44.println();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:457:0x0859, code lost:
        r14.println("AGGREGATED OVER LAST 24 HOURS:");
        r1 = r13;
        r2 = r14;
        r5 = r16;
        r7 = r28;
        r8 = r31;
        r9 = r34;
        r10 = r18;
        r11 = r23;
        r35 = r12;
        r12 = r22;
        dumpAggregatedStats(r2, 24, r5, r7, r8, r9, r10, r11, r12);
        r44.println();
        r14.println("AGGREGATED OVER LAST 3 HOURS:");
        dumpAggregatedStats(r2, 3, r5, r7, r8, r9, r10, r11, r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:458:0x0883, code lost:
        r35 = r12;
     */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0794 A[Catch:{ Throwable -> 0x07b4 }] */
    /* JADX WARNING: Removed duplicated region for block: B:529:0x07d2 A[SYNTHETIC] */
    private void dumpInner(PrintWriter pw, String[] args) {
        boolean dumpDetails;
        boolean currentOnly;
        boolean isCsv;
        boolean isCompact;
        boolean csvSepProcStats;
        boolean csvSepMemStats;
        boolean csvSepMemStats2;
        boolean activeOnly;
        int maxNum;
        boolean activeOnly2;
        int[] csvMemStats;
        int[] csvScreenStats;
        int[] csvProcStats;
        int lastIndex;
        boolean z;
        boolean z2;
        boolean sepNeeded;
        boolean sepNeeded2;
        String reqPackage;
        int i;
        boolean z3;
        String fileStr;
        String reqPackage2;
        ProcessStats processStats;
        String fileStr2;
        String reqPackage3;
        ProcessStats processStats2;
        ActivityManagerService activityManagerService;
        int[] csvMemStats2;
        boolean isCompact2;
        boolean isCsv2;
        boolean currentOnly2;
        boolean dumpDetails2;
        int i2;
        PrintWriter printWriter = pw;
        String[] strArr = args;
        long now = SystemClock.uptimeMillis();
        int aggregateHours = 0;
        String reqPackage4 = null;
        boolean isCheckin = false;
        boolean quit = false;
        int[] csvScreenStats2 = {0, 4};
        boolean dumpAll = false;
        int[] csvMemStats3 = {3};
        int[] csvProcStats2 = ProcessStats.ALL_PROC_STATES;
        if (strArr != null) {
            csvSepProcStats = true;
            boolean csvSepMemStats3 = false;
            boolean csvSepScreenStats = false;
            boolean activeOnly3 = false;
            int maxNum2 = 2;
            int lastIndex2 = 0;
            int aggregateHours2 = 0;
            boolean dumpFullDetails = false;
            boolean dumpDetails3 = false;
            boolean currentOnly3 = false;
            boolean isCsv3 = false;
            boolean isCompact3 = false;
            int[] csvMemStats4 = csvMemStats3;
            int i3 = 0;
            while (true) {
                int i4 = i3;
                if (i4 >= strArr.length) {
                    isCompact = isCompact3;
                    isCsv = isCsv3;
                    currentOnly = currentOnly3;
                    dumpDetails = dumpDetails3;
                    lastIndex = lastIndex2;
                    csvScreenStats = csvScreenStats2;
                    activeOnly = activeOnly3;
                    activeOnly2 = dumpFullDetails;
                    aggregateHours = aggregateHours2;
                    csvProcStats = csvProcStats2;
                    csvSepMemStats = csvSepMemStats3;
                    csvSepMemStats2 = csvSepScreenStats;
                    maxNum = maxNum2;
                    csvMemStats = csvMemStats4;
                    break;
                }
                String arg = strArr[i4];
                if ("--checkin".equals(arg)) {
                    isCheckin = true;
                } else if ("-c".equals(arg)) {
                    isCompact3 = true;
                } else if ("--csv".equals(arg)) {
                    isCsv3 = true;
                } else {
                    if ("--csv-screen".equals(arg)) {
                        int i5 = i4 + 1;
                        if (i5 >= strArr.length) {
                            printWriter.println("Error: argument required for --csv-screen");
                            dumpHelp(pw);
                            return;
                        }
                        csvMemStats2 = csvMemStats4;
                        boolean[] sep = new boolean[1];
                        isCompact2 = isCompact3;
                        isCsv2 = isCsv3;
                        currentOnly2 = currentOnly3;
                        int[] csvScreenStats3 = parseStateList(DumpUtils.ADJ_SCREEN_NAMES_CSV, 4, strArr[i5], sep, new String[1]);
                        if (csvScreenStats3 == null) {
                            printWriter.println("Error in \"" + strArr[i5] + "\": " + error[0]);
                            dumpHelp(pw);
                            return;
                        }
                        i4 = i5;
                        csvSepScreenStats = sep[0];
                        csvScreenStats2 = csvScreenStats3;
                    } else {
                        csvMemStats2 = csvMemStats4;
                        isCompact2 = isCompact3;
                        isCsv2 = isCsv3;
                        currentOnly2 = currentOnly3;
                        if ("--csv-mem".equals(arg)) {
                            int i6 = i4 + 1;
                            if (i6 >= strArr.length) {
                                printWriter.println("Error: argument required for --csv-mem");
                                dumpHelp(pw);
                                return;
                            }
                            boolean[] sep2 = new boolean[1];
                            int[] csvMemStats5 = parseStateList(DumpUtils.ADJ_MEM_NAMES_CSV, 1, strArr[i6], sep2, new String[1]);
                            if (csvMemStats5 == null) {
                                printWriter.println("Error in \"" + strArr[i6] + "\": " + error[0]);
                                dumpHelp(pw);
                                return;
                            }
                            i4 = i6;
                            csvSepMemStats3 = sep2[0];
                            csvMemStats4 = csvMemStats5;
                            isCompact3 = isCompact2;
                            isCsv3 = isCsv2;
                            currentOnly3 = currentOnly2;
                        } else if ("--csv-proc".equals(arg)) {
                            int i7 = i4 + 1;
                            if (i7 >= strArr.length) {
                                printWriter.println("Error: argument required for --csv-proc");
                                dumpHelp(pw);
                                return;
                            }
                            boolean[] sep3 = new boolean[1];
                            int[] csvProcStats3 = parseStateList(DumpUtils.STATE_NAMES_CSV, 1, strArr[i7], sep3, new String[1]);
                            if (csvProcStats3 == null) {
                                printWriter.println("Error in \"" + strArr[i7] + "\": " + error[0]);
                                dumpHelp(pw);
                                return;
                            }
                            i4 = i7;
                            csvSepProcStats = sep3[0];
                            csvProcStats2 = csvProcStats3;
                        } else if ("--details".equals(arg)) {
                            dumpDetails3 = true;
                        } else if ("--full-details".equals(arg)) {
                            dumpFullDetails = true;
                        } else {
                            if ("--hours".equals(arg)) {
                                i2 = i4 + 1;
                                if (i2 >= strArr.length) {
                                    printWriter.println("Error: argument required for --hours");
                                    dumpHelp(pw);
                                    return;
                                }
                                try {
                                    aggregateHours2 = Integer.parseInt(strArr[i2]);
                                } catch (NumberFormatException e) {
                                    printWriter.println("Error: --hours argument not an int -- " + strArr[i2]);
                                    dumpHelp(pw);
                                    return;
                                }
                            } else if ("--last".equals(arg)) {
                                i2 = i4 + 1;
                                if (i2 >= strArr.length) {
                                    printWriter.println("Error: argument required for --last");
                                    dumpHelp(pw);
                                    return;
                                }
                                try {
                                    lastIndex2 = Integer.parseInt(strArr[i2]);
                                } catch (NumberFormatException e2) {
                                    printWriter.println("Error: --last argument not an int -- " + strArr[i2]);
                                    dumpHelp(pw);
                                    return;
                                }
                            } else if ("--max".equals(arg)) {
                                i2 = i4 + 1;
                                if (i2 >= strArr.length) {
                                    printWriter.println("Error: argument required for --max");
                                    dumpHelp(pw);
                                    return;
                                }
                                try {
                                    maxNum2 = Integer.parseInt(strArr[i2]);
                                } catch (NumberFormatException e3) {
                                    printWriter.println("Error: --max argument not an int -- " + strArr[i2]);
                                    dumpHelp(pw);
                                    return;
                                }
                            } else {
                                if ("--active".equals(arg)) {
                                    currentOnly3 = true;
                                    activeOnly3 = true;
                                } else if ("--current".equals(arg)) {
                                    currentOnly3 = true;
                                } else if ("--commit".equals(arg)) {
                                    synchronized (this.mAm) {
                                        try {
                                            ActivityManagerService.boostPriorityForLockedSection();
                                            this.mProcessStats.mFlags |= 1;
                                            writeStateLocked(true, true);
                                            printWriter.println("Process stats committed.");
                                            quit = true;
                                        } catch (Throwable th) {
                                            while (true) {
                                                ActivityManagerService.resetPriorityAfterLockedSection();
                                                throw th;
                                            }
                                        }
                                    }
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                } else {
                                    if ("--reset".equals(arg)) {
                                        synchronized (this.mAm) {
                                            try {
                                                ActivityManagerService.boostPriorityForLockedSection();
                                                this.mProcessStats.resetSafely();
                                                dumpDetails2 = dumpDetails3;
                                                try {
                                                    this.mAm.requestPssAllProcsLocked(SystemClock.uptimeMillis(), true, false);
                                                    printWriter.println("Process stats reset.");
                                                    quit = true;
                                                } catch (Throwable th2) {
                                                    th = th2;
                                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                                    throw th;
                                                }
                                            } catch (Throwable th3) {
                                                th = th3;
                                                boolean z4 = dumpDetails3;
                                                ActivityManagerService.resetPriorityAfterLockedSection();
                                                throw th;
                                            }
                                        }
                                    } else {
                                        dumpDetails2 = dumpDetails3;
                                        if ("--clear".equals(arg)) {
                                            synchronized (this.mAm) {
                                                try {
                                                    ActivityManagerService.boostPriorityForLockedSection();
                                                    this.mProcessStats.resetSafely();
                                                    this.mAm.requestPssAllProcsLocked(SystemClock.uptimeMillis(), true, false);
                                                    ArrayList<String> files = getCommittedFiles(0, true, true);
                                                    if (files != null) {
                                                        for (int fi = 0; fi < files.size(); fi++) {
                                                            new File(files.get(fi)).delete();
                                                        }
                                                    }
                                                    printWriter.println("All process stats cleared.");
                                                    quit = true;
                                                } catch (Throwable th4) {
                                                    while (true) {
                                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                                        throw th4;
                                                    }
                                                }
                                            }
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                        } else if ("--write".equals(arg)) {
                                            synchronized (this.mAm) {
                                                try {
                                                    ActivityManagerService.boostPriorityForLockedSection();
                                                    writeStateSyncLocked();
                                                    printWriter.println("Process stats written.");
                                                    quit = true;
                                                } catch (Throwable th5) {
                                                    while (true) {
                                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                                        throw th5;
                                                    }
                                                }
                                            }
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                        } else if ("--read".equals(arg)) {
                                            synchronized (this.mAm) {
                                                try {
                                                    ActivityManagerService.boostPriorityForLockedSection();
                                                    readLocked(this.mProcessStats, this.mFile);
                                                    printWriter.println("Process stats read.");
                                                    quit = true;
                                                } catch (Throwable th6) {
                                                    while (true) {
                                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                                        throw th6;
                                                    }
                                                }
                                            }
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                        } else if ("--start-testing".equals(arg)) {
                                            synchronized (this.mAm) {
                                                try {
                                                    ActivityManagerService.boostPriorityForLockedSection();
                                                    this.mAm.setTestPssMode(true);
                                                    printWriter.println("Started high frequency sampling.");
                                                    quit = true;
                                                } catch (Throwable th7) {
                                                    while (true) {
                                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                                        throw th7;
                                                    }
                                                }
                                            }
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                        } else if ("--stop-testing".equals(arg)) {
                                            synchronized (this.mAm) {
                                                try {
                                                    ActivityManagerService.boostPriorityForLockedSection();
                                                    this.mAm.setTestPssMode(false);
                                                    printWriter.println("Stopped high frequency sampling.");
                                                    quit = true;
                                                } catch (Throwable th8) {
                                                    while (true) {
                                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                                        throw th8;
                                                    }
                                                }
                                            }
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                        } else if ("--pretend-screen-on".equals(arg)) {
                                            synchronized (this.mAm) {
                                                try {
                                                    ActivityManagerService.boostPriorityForLockedSection();
                                                    this.mInjectedScreenState = true;
                                                } catch (Throwable th9) {
                                                    while (true) {
                                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                                        throw th9;
                                                    }
                                                }
                                            }
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                            quit = true;
                                        } else if ("--pretend-screen-off".equals(arg)) {
                                            synchronized (this.mAm) {
                                                try {
                                                    ActivityManagerService.boostPriorityForLockedSection();
                                                    this.mInjectedScreenState = false;
                                                } catch (Throwable th10) {
                                                    while (true) {
                                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                                        throw th10;
                                                    }
                                                }
                                            }
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                            quit = true;
                                        } else if ("--stop-pretend-screen".equals(arg)) {
                                            synchronized (this.mAm) {
                                                try {
                                                    ActivityManagerService.boostPriorityForLockedSection();
                                                    this.mInjectedScreenState = null;
                                                } catch (Throwable th11) {
                                                    while (true) {
                                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                                        throw th11;
                                                    }
                                                }
                                            }
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                            quit = true;
                                        } else if ("-h".equals(arg)) {
                                            dumpHelp(pw);
                                            return;
                                        } else if ("-a".equals(arg)) {
                                            dumpDetails3 = true;
                                            dumpAll = true;
                                        } else if (arg.length() <= 0 || arg.charAt(0) != '-') {
                                            reqPackage4 = arg;
                                            dumpDetails3 = true;
                                        } else {
                                            printWriter.println("Unknown option: " + arg);
                                            dumpHelp(pw);
                                            return;
                                        }
                                    }
                                    csvMemStats4 = csvMemStats2;
                                    isCompact3 = isCompact2;
                                    isCsv3 = isCsv2;
                                    currentOnly3 = currentOnly2;
                                    dumpDetails3 = dumpDetails2;
                                }
                                csvMemStats4 = csvMemStats2;
                                isCompact3 = isCompact2;
                                isCsv3 = isCsv2;
                            }
                            i4 = i2;
                        }
                    }
                    csvMemStats4 = csvMemStats2;
                    isCompact3 = isCompact2;
                    isCsv3 = isCsv2;
                    currentOnly3 = currentOnly2;
                }
                i3 = i4 + 1;
            }
        } else {
            isCompact = false;
            isCsv = false;
            currentOnly = false;
            dumpDetails = false;
            lastIndex = 0;
            csvSepProcStats = true;
            csvProcStats = csvProcStats2;
            csvSepMemStats2 = false;
            csvSepMemStats = false;
            activeOnly2 = false;
            maxNum = 2;
            csvScreenStats = csvScreenStats2;
            activeOnly = false;
            csvMemStats = csvMemStats3;
        }
        if (!quit) {
            if (isCsv) {
                printWriter.print("Processes running summed over");
                if (!csvSepMemStats2) {
                    for (int printScreenLabelCsv : csvScreenStats) {
                        printWriter.print(" ");
                        DumpUtils.printScreenLabelCsv(printWriter, printScreenLabelCsv);
                    }
                }
                if (!csvSepMemStats) {
                    for (int printMemLabelCsv : csvMemStats) {
                        printWriter.print(" ");
                        DumpUtils.printMemLabelCsv(printWriter, printMemLabelCsv);
                    }
                }
                if (!csvSepProcStats) {
                    int i8 = 0;
                    while (true) {
                        int i9 = i8;
                        if (i9 >= csvProcStats.length) {
                            break;
                        }
                        printWriter.print(" ");
                        printWriter.print(DumpUtils.STATE_NAMES_CSV[csvProcStats[i9]]);
                        i8 = i9 + 1;
                    }
                }
                pw.println();
                ActivityManagerService activityManagerService2 = this.mAm;
                synchronized (activityManagerService2) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        activityManagerService = activityManagerService2;
                        int i10 = lastIndex;
                        int i11 = aggregateHours;
                        int[] iArr = csvProcStats;
                        int[] iArr2 = csvScreenStats;
                        int[] iArr3 = csvMemStats;
                        String str = reqPackage4;
                        try {
                            dumpFilteredProcessesCsvLocked(printWriter, null, csvSepMemStats2, csvScreenStats, csvSepMemStats, csvMemStats, csvSepProcStats, csvProcStats, now, reqPackage4);
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        } catch (Throwable th12) {
                            th = th12;
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    } catch (Throwable th13) {
                        th = th13;
                        activityManagerService = activityManagerService2;
                        int i12 = lastIndex;
                        int i13 = aggregateHours;
                        int[] iArr4 = csvProcStats;
                        int[] iArr5 = csvScreenStats;
                        int[] iArr6 = csvMemStats;
                        String str2 = reqPackage4;
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            } else {
                int lastIndex3 = lastIndex;
                int[] iArr7 = csvProcStats;
                int[] iArr8 = csvScreenStats;
                int[] iArr9 = csvMemStats;
                String reqPackage5 = reqPackage4;
                int aggregateHours3 = aggregateHours;
                if (aggregateHours3 != 0) {
                    printWriter.print("AGGREGATED OVER LAST ");
                    printWriter.print(aggregateHours3);
                    printWriter.println(" HOURS:");
                    int i14 = aggregateHours3;
                    dumpAggregatedStats(printWriter, (long) aggregateHours3, now, reqPackage5, isCompact, dumpDetails, activeOnly2, dumpAll, activeOnly);
                    return;
                }
                int aggregateHours4 = lastIndex3;
                if (aggregateHours4 > 0) {
                    printWriter.print("LAST STATS AT INDEX ");
                    printWriter.print(aggregateHours4);
                    printWriter.println(":");
                    ArrayList<String> files2 = getCommittedFiles(0, false, true);
                    if (aggregateHours4 >= files2.size()) {
                        printWriter.print("Only have ");
                        printWriter.print(files2.size());
                        printWriter.println(" data sets");
                        return;
                    }
                    AtomicFile file = new AtomicFile(new File(files2.get(aggregateHours4)));
                    ProcessStats processStats3 = new ProcessStats(false);
                    readLocked(processStats3, file);
                    if (processStats3.mReadError != null) {
                        if (isCheckin || isCompact) {
                            printWriter.print("err,");
                        }
                        printWriter.print("Failure reading ");
                        printWriter.print(files2.get(aggregateHours4));
                        printWriter.print("; ");
                        printWriter.println(processStats3.mReadError);
                        return;
                    }
                    boolean checkedIn = file.getBaseFile().getPath().endsWith(STATE_FILE_CHECKIN_SUFFIX);
                    if (isCheckin || isCompact) {
                        processStats3.dumpCheckinLocked(printWriter, reqPackage5);
                    } else {
                        printWriter.print("COMMITTED STATS FROM ");
                        printWriter.print(processStats3.mTimePeriodStartClockStr);
                        if (checkedIn) {
                            printWriter.print(" (checked in)");
                        }
                        printWriter.println(":");
                        if (dumpDetails || activeOnly2) {
                            processStats3.dumpLocked(printWriter, reqPackage5, now, !activeOnly2, dumpAll, activeOnly);
                            if (dumpAll) {
                                printWriter.print("  mFile=");
                                printWriter.println(this.mFile.getBaseFile());
                            }
                        } else {
                            processStats3.dumpSummaryLocked(printWriter, reqPackage5, now, activeOnly);
                        }
                    }
                    return;
                }
                String reqPackage6 = reqPackage5;
                boolean z5 = true;
                boolean sepNeeded3 = false;
                if (dumpAll || isCheckin) {
                    this.mWriteLock.lock();
                    try {
                        ArrayList<String> files3 = getCommittedFiles(0, false, !isCheckin);
                        if (files3 != null) {
                            if (isCheckin) {
                                i = 0;
                            } else {
                                try {
                                    i = files3.size() - maxNum;
                                } catch (Throwable th14) {
                                    th = th14;
                                    String str3 = reqPackage6;
                                    int i15 = aggregateHours4;
                                }
                            }
                            int start = i;
                            if (start < 0) {
                                start = 0;
                            }
                            int i16 = start;
                            while (true) {
                                int i17 = i16;
                                if (i17 >= files3.size()) {
                                    break;
                                }
                                try {
                                    AtomicFile file2 = new AtomicFile(new File(files3.get(i17)));
                                    try {
                                        ProcessStats processStats4 = new ProcessStats(false);
                                        readLocked(processStats4, file2);
                                        if (processStats4.mReadError != null) {
                                            if (isCheckin || isCompact) {
                                                printWriter.print("err,");
                                            }
                                            printWriter.print("Failure reading ");
                                            printWriter.print(files3.get(i17));
                                            printWriter.print("; ");
                                            printWriter.println(processStats4.mReadError);
                                            new File(files3.get(i17)).delete();
                                            z3 = z5;
                                        } else {
                                            String fileStr3 = file2.getBaseFile().getPath();
                                            boolean checkedIn2 = fileStr3.endsWith(STATE_FILE_CHECKIN_SUFFIX);
                                            if (isCheckin) {
                                                processStats = processStats4;
                                                fileStr2 = fileStr3;
                                                z3 = z5;
                                                reqPackage2 = reqPackage6;
                                            } else if (isCompact) {
                                                processStats = processStats4;
                                                fileStr2 = fileStr3;
                                                z3 = z5;
                                                reqPackage2 = reqPackage6;
                                            } else {
                                                if (sepNeeded3) {
                                                    pw.println();
                                                } else {
                                                    sepNeeded3 = true;
                                                }
                                                boolean sepNeeded4 = sepNeeded3;
                                                try {
                                                    printWriter.print("COMMITTED STATS FROM ");
                                                    printWriter.print(processStats4.mTimePeriodStartClockStr);
                                                    if (checkedIn2) {
                                                        try {
                                                            printWriter.print(" (checked in)");
                                                        } catch (Throwable th15) {
                                                            th = th15;
                                                            String str4 = reqPackage6;
                                                            int i18 = aggregateHours4;
                                                            boolean z6 = sepNeeded4;
                                                            this.mWriteLock.unlock();
                                                            throw th;
                                                        }
                                                    }
                                                    printWriter.println(":");
                                                    if (activeOnly2) {
                                                        processStats2 = processStats4;
                                                        fileStr = fileStr3;
                                                        z3 = z5;
                                                        reqPackage3 = reqPackage6;
                                                        try {
                                                            processStats4.dumpLocked(printWriter, reqPackage6, now, false, false, activeOnly);
                                                        } catch (Throwable th16) {
                                                            th = th16;
                                                            int i19 = aggregateHours4;
                                                            boolean z7 = sepNeeded4;
                                                            String str5 = reqPackage3;
                                                            this.mWriteLock.unlock();
                                                            throw th;
                                                        }
                                                    } else {
                                                        processStats2 = processStats4;
                                                        fileStr = fileStr3;
                                                        z3 = z5;
                                                        reqPackage3 = reqPackage6;
                                                        processStats2.dumpSummaryLocked(printWriter, reqPackage3, now, activeOnly);
                                                    }
                                                    sepNeeded3 = sepNeeded4;
                                                    ProcessStats processStats5 = processStats2;
                                                    reqPackage6 = reqPackage3;
                                                    if (!isCheckin) {
                                                        file2.getBaseFile().renameTo(new File(fileStr + STATE_FILE_CHECKIN_SUFFIX));
                                                    }
                                                } catch (Throwable th17) {
                                                    th = th17;
                                                    String str6 = reqPackage6;
                                                    int i20 = aggregateHours4;
                                                    boolean z8 = sepNeeded4;
                                                    this.mWriteLock.unlock();
                                                    throw th;
                                                }
                                            }
                                            reqPackage6 = reqPackage2;
                                            try {
                                                processStats.dumpCheckinLocked(printWriter, reqPackage6);
                                                if (!isCheckin) {
                                                }
                                            } catch (Throwable th18) {
                                                e = th18;
                                                printWriter.print("**** FAILURE DUMPING STATE: ");
                                                printWriter.println(files3.get(i17));
                                                e.printStackTrace(printWriter);
                                                i16 = i17 + 1;
                                                z5 = z3;
                                            }
                                        }
                                    } catch (Throwable th19) {
                                        e = th19;
                                        z3 = z5;
                                        printWriter.print("**** FAILURE DUMPING STATE: ");
                                        printWriter.println(files3.get(i17));
                                        e.printStackTrace(printWriter);
                                        i16 = i17 + 1;
                                        z5 = z3;
                                    }
                                } catch (Throwable th20) {
                                    e = th20;
                                    z3 = z5;
                                    printWriter.print("**** FAILURE DUMPING STATE: ");
                                    printWriter.println(files3.get(i17));
                                    e.printStackTrace(printWriter);
                                    i16 = i17 + 1;
                                    z5 = z3;
                                }
                                i16 = i17 + 1;
                                z5 = z3;
                            }
                        }
                        z2 = z5;
                        z = false;
                        this.mWriteLock.unlock();
                        sepNeeded = sepNeeded3;
                    } catch (Throwable th21) {
                        th = th21;
                        String str7 = reqPackage6;
                        int i21 = aggregateHours4;
                        this.mWriteLock.unlock();
                        throw th;
                    }
                } else {
                    sepNeeded = false;
                    z2 = true;
                    z = false;
                }
                if (!isCheckin) {
                    synchronized (this.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            if (isCompact) {
                                try {
                                    this.mProcessStats.dumpCheckinLocked(printWriter, reqPackage6);
                                    reqPackage = reqPackage6;
                                    sepNeeded2 = sepNeeded;
                                } catch (Throwable th22) {
                                    th = th22;
                                    String str8 = reqPackage6;
                                    int lastIndex4 = aggregateHours4;
                                    while (true) {
                                        try {
                                            break;
                                        } catch (Throwable th23) {
                                            th = th23;
                                        }
                                    }
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            } else {
                                if (sepNeeded) {
                                    pw.println();
                                }
                                printWriter.println("CURRENT STATS:");
                                if (dumpDetails || activeOnly2) {
                                    reqPackage = reqPackage6;
                                    try {
                                        this.mProcessStats.dumpLocked(printWriter, reqPackage6, now, !activeOnly2 ? z2 : z, dumpAll, activeOnly);
                                        if (dumpAll) {
                                            try {
                                                printWriter.print("  mFile=");
                                                printWriter.println(this.mFile.getBaseFile());
                                            } catch (Throwable th24) {
                                                th = th24;
                                            }
                                        }
                                    } catch (Throwable th25) {
                                        th = th25;
                                        int i22 = aggregateHours4;
                                        while (true) {
                                            break;
                                        }
                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                        throw th;
                                    }
                                } else {
                                    this.mProcessStats.dumpSummaryLocked(printWriter, reqPackage6, now, activeOnly);
                                    reqPackage = reqPackage6;
                                }
                                sepNeeded2 = true;
                            }
                            try {
                            } catch (Throwable th26) {
                                th = th26;
                                int i23 = aggregateHours4;
                                boolean z9 = sepNeeded2;
                                while (true) {
                                    break;
                                }
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        } catch (Throwable th27) {
                            th = th27;
                            String str9 = reqPackage6;
                            int i24 = aggregateHours4;
                            while (true) {
                                break;
                            }
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                } else {
                    int i25 = aggregateHours4;
                    boolean z10 = sepNeeded;
                }
            }
        }
    }

    private void dumpAggregatedStats(ProtoOutputStream proto, long fieldId, int aggregateHours, long now) {
        ParcelFileDescriptor pfd = getStatsOverTime(((long) (((aggregateHours * 60) * 60) * 1000)) - (ProcessStats.COMMIT_PERIOD / 2));
        if (pfd != null) {
            ProcessStats stats = new ProcessStats(false);
            stats.read(new ParcelFileDescriptor.AutoCloseInputStream(pfd));
            if (stats.mReadError == null) {
                stats.writeToProto(proto, fieldId, now);
            }
        }
    }

    private void dumpProto(FileDescriptor fd) {
        long now;
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                now = SystemClock.uptimeMillis();
                this.mProcessStats.writeToProto(proto, 1146756268033L, now);
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        ProtoOutputStream protoOutputStream = proto;
        long j = now;
        dumpAggregatedStats(protoOutputStream, 1146756268034L, 3, j);
        dumpAggregatedStats(protoOutputStream, 1146756268035L, 24, j);
        proto.flush();
    }
}

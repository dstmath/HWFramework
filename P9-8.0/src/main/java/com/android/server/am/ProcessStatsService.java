package com.android.server.am;

import android.os.Binder;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseInputStream;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.AtomicFile;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.procstats.DumpUtils;
import com.android.internal.app.procstats.IProcessStats.Stub;
import com.android.internal.app.procstats.ProcessState;
import com.android.internal.app.procstats.ProcessStats;
import com.android.internal.app.procstats.ProcessStats.PackageState;
import com.android.internal.app.procstats.ServiceState;
import com.android.internal.os.BackgroundThread;
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

public final class ProcessStatsService extends Stub {
    static final boolean DEBUG = false;
    static final int MAX_HISTORIC_STATES = 8;
    static final String STATE_FILE_CHECKIN_SUFFIX = ".ci";
    static final String STATE_FILE_PREFIX = "state-";
    static final String STATE_FILE_SUFFIX = ".bin";
    static final String TAG = "ProcessStatsService";
    static long WRITE_PERIOD = HwBroadcastRadarUtil.SYSTEM_BOOT_COMPLETED_TIME;
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
                            ProcessStats processStats = ProcessStatsService.this.mProcessStats;
                            processStats.mFlags |= 4;
                            ProcessStatsService.this.writeStateLocked(true, true);
                            ProcessStatsService.this.mProcessStats.evaluateSystemProperties(true);
                        }
                    } finally {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                }
            }
        });
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        try {
            return super.onTransact(code, data, reply, flags);
        } catch (RuntimeException e) {
            if (!(e instanceof SecurityException)) {
                Slog.wtf(TAG, "Process Stats Crash", e);
            }
            throw e;
        }
    }

    public ProcessState getProcessStateLocked(String packageName, int uid, int versionCode, String processName) {
        return this.mProcessStats.getProcessStateLocked(packageName, uid, versionCode, processName);
    }

    public ServiceState getServiceStateLocked(String packageName, int uid, int versionCode, String processName, String className) {
        return this.mProcessStats.getServiceStateLocked(packageName, uid, versionCode, processName, className);
    }

    public boolean isMemFactorLowered() {
        return this.mMemFactorLowered;
    }

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
        ArrayMap<String, SparseArray<SparseArray<PackageState>>> pmap = this.mProcessStats.mPackages.getMap();
        for (int ipkg = pmap.size() - 1; ipkg >= 0; ipkg--) {
            SparseArray<SparseArray<PackageState>> uids = (SparseArray) pmap.valueAt(ipkg);
            for (int iuid = uids.size() - 1; iuid >= 0; iuid--) {
                SparseArray<PackageState> vers = (SparseArray) uids.valueAt(iuid);
                for (int iver = vers.size() - 1; iver >= 0; iver--) {
                    ArrayMap<String, ServiceState> services = ((PackageState) vers.valueAt(iver)).mServices;
                    for (int isvc = services.size() - 1; isvc >= 0; isvc--) {
                        ((ServiceState) services.valueAt(isvc)).setMemFactor(memFactor, now);
                    }
                }
            }
        }
        return true;
    }

    public int getMemFactorLocked() {
        return this.mProcessStats.mMemFactor != -1 ? this.mProcessStats.mMemFactor : 0;
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
        ProcessStats processStats = this.mProcessStats;
        processStats.mFlags |= 2;
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
            if (this.mPendingWrite == null || (this.mPendingWriteCommitted ^ 1) != 0) {
                this.mPendingWrite = Parcel.obtain();
                this.mProcessStats.mTimePeriodEndRealtime = SystemClock.elapsedRealtime();
                this.mProcessStats.mTimePeriodEndUptime = now;
                if (commit) {
                    ProcessStats processStats = this.mProcessStats;
                    processStats.mFlags |= 1;
                }
                this.mProcessStats.writeToParcel(this.mPendingWrite, 0);
                this.mPendingWriteFile = new AtomicFile(this.mFile.getBaseFile());
                this.mPendingWriteCommitted = commit;
            }
            if (commit) {
                this.mProcessStats.resetSafely();
                updateFile();
            }
            this.mLastWriteTime = SystemClock.uptimeMillis();
            if (sync) {
                performWriteState();
                return;
            }
            BackgroundThread.getHandler().post(new Runnable() {
                public void run() {
                    ProcessStatsService.this.performWriteState();
                }
            });
        }
    }

    private void updateFile() {
        this.mFile = new AtomicFile(new File(this.mBaseDir, STATE_FILE_PREFIX + this.mProcessStats.mTimePeriodStartClockStr + STATE_FILE_SUFFIX));
        this.mLastWriteTime = SystemClock.uptimeMillis();
    }

    /* JADX WARNING: Missing block: B:11:0x001a, code:
            r3 = null;
     */
    /* JADX WARNING: Missing block: B:13:?, code:
            r3 = r2.startWrite();
            r3.write(r0.marshall());
            r3.flush();
            r2.finishWrite(r3);
     */
    /* JADX WARNING: Missing block: B:14:0x0037, code:
            return;
     */
    /* JADX WARNING: Missing block: B:18:0x003b, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:20:?, code:
            android.util.Slog.w(TAG, "Error writing process statistics", r1);
            r2.failWrite(r3);
     */
    /* JADX WARNING: Missing block: B:22:0x0055, code:
            r0.recycle();
            trimHistoricStatesWriteLocked();
            r6.mWriteLock.unlock();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void performWriteState() {
        synchronized (this.mPendingWriteLock) {
            Parcel data = this.mPendingWrite;
            AtomicFile file = this.mPendingWriteFile;
            this.mPendingWriteCommitted = false;
            if (data == null) {
                return;
            }
            this.mPendingWrite = null;
            this.mPendingWriteFile = null;
            this.mWriteLock.lock();
        }
    }

    boolean readLocked(ProcessStats stats, AtomicFile file) {
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
        ArrayList<String> filesArray = new ArrayList(files.length);
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
                String file = (String) filesArray.remove(0);
                Slog.i(TAG, "Pruning old procstats: " + file);
                new File(file).delete();
            }
        }
    }

    boolean dumpFilteredProcessesCsvLocked(PrintWriter pw, String header, boolean sepScreenStates, int[] screenStates, boolean sepMemStates, int[] memStates, boolean sepProcStates, int[] procStates, long now, String reqPackage) {
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
        ArrayList<Integer> res = new ArrayList();
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
                    for (int j = 0; j < states.length; j++) {
                        if (str.equals(states[j])) {
                            res.add(Integer.valueOf(j));
                            str = null;
                            break;
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
        for (i = 0; i < res.size(); i++) {
            finalRes[i] = ((Integer) res.get(i)).intValue() * mult;
        }
        return finalRes;
    }

    public byte[] getCurrentStats(List<ParcelFileDescriptor> historic) {
        this.mAm.mContext.enforceCallingOrSelfPermission("android.permission.PACKAGE_USAGE_STATS", null);
        Parcel current = Parcel.obtain();
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                long now = SystemClock.uptimeMillis();
                this.mProcessStats.mTimePeriodEndRealtime = SystemClock.elapsedRealtime();
                this.mProcessStats.mTimePeriodEndUptime = now;
                this.mProcessStats.writeToParcel(current, now, 0);
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        this.mWriteLock.lock();
        if (historic != null) {
            ArrayList<String> files;
            int i;
            try {
                files = getCommittedFiles(0, false, true);
                if (files != null) {
                    i = files.size() - 1;
                    while (i >= 0) {
                        historic.add(ParcelFileDescriptor.open(new File((String) files.get(i)), 268435456));
                        i--;
                    }
                }
            } catch (IOException e) {
                Slog.w(TAG, "Failure opening procstat file " + ((String) files.get(i)), e);
            } catch (Throwable th) {
                this.mWriteLock.unlock();
            }
        }
        this.mWriteLock.unlock();
        return current.marshall();
    }

    public ParcelFileDescriptor getStatsOverTime(long minTime) {
        long curTime;
        this.mAm.mContext.enforceCallingOrSelfPermission("android.permission.PACKAGE_USAGE_STATS", null);
        Parcel current = Parcel.obtain();
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                long now = SystemClock.uptimeMillis();
                this.mProcessStats.mTimePeriodEndRealtime = SystemClock.elapsedRealtime();
                this.mProcessStats.mTimePeriodEndUptime = now;
                this.mProcessStats.writeToParcel(current, now, 0);
                curTime = this.mProcessStats.mTimePeriodEndRealtime - this.mProcessStats.mTimePeriodStartRealtime;
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        this.mWriteLock.lock();
        if (curTime < minTime) {
            try {
                ArrayList<String> files = getCommittedFiles(0, false, true);
                if (files != null && files.size() > 0) {
                    current.setDataPosition(0);
                    ProcessStats stats = (ProcessStats) ProcessStats.CREATOR.createFromParcel(current);
                    current.recycle();
                    int i = files.size() - 1;
                    while (i >= 0 && stats.mTimePeriodEndRealtime - stats.mTimePeriodStartRealtime < minTime) {
                        AtomicFile file = new AtomicFile(new File((String) files.get(i)));
                        i--;
                        ProcessStats moreStats = new ProcessStats(false);
                        readLocked(moreStats, file);
                        if (moreStats.mReadError == null) {
                            stats.add(moreStats);
                            StringBuilder sb = new StringBuilder();
                            sb.append("Added stats: ");
                            sb.append(moreStats.mTimePeriodStartClockStr);
                            sb.append(", over ");
                            TimeUtils.formatDuration(moreStats.mTimePeriodEndRealtime - moreStats.mTimePeriodStartRealtime, sb);
                            Slog.i(TAG, sb.toString());
                        } else {
                            Slog.w(TAG, "Failure reading " + ((String) files.get(i + 1)) + "; " + moreStats.mReadError);
                        }
                    }
                    current = Parcel.obtain();
                    stats.writeToParcel(current, 0);
                }
            } catch (IOException e) {
                Slog.w(TAG, "Failed building output pipe", e);
                return null;
            } finally {
                this.mWriteLock.unlock();
            }
        }
        final byte[] outData = current.marshall();
        current.recycle();
        final ParcelFileDescriptor[] fds = ParcelFileDescriptor.createPipe();
        new Thread("ProcessStats pipe output") {
            public void run() {
                FileOutputStream fout = new AutoCloseOutputStream(fds[1]);
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

    public int getCurrentMemoryState() {
        int i;
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                i = this.mLastMemOnlyState;
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        return i;
    }

    private void dumpAggregatedStats(PrintWriter pw, long aggregateHours, long now, String reqPackage, boolean isCompact, boolean dumpDetails, boolean dumpFullDetails, boolean dumpAll, boolean activeOnly) {
        ParcelFileDescriptor pfd = getStatsOverTime((((60 * aggregateHours) * 60) * 1000) - (ProcessStats.COMMIT_PERIOD / 2));
        if (pfd == null) {
            pw.println("Unable to build stats!");
            return;
        }
        ProcessStats stats = new ProcessStats(false);
        stats.read(new AutoCloseInputStream(pfd));
        if (stats.mReadError != null) {
            pw.print("Failure reading: ");
            pw.println(stats.mReadError);
            return;
        }
        if (isCompact) {
            stats.dumpCheckinLocked(pw, reqPackage);
        } else if (dumpDetails || dumpFullDetails) {
            stats.dumpLocked(pw, reqPackage, now, dumpFullDetails ^ 1, dumpAll, activeOnly);
        } else {
            stats.dumpSummaryLocked(pw, reqPackage, now, activeOnly);
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

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (com.android.internal.util.DumpUtils.checkDumpAndUsageStatsPermission(this.mAm.mContext, TAG, pw)) {
            long ident = Binder.clearCallingIdentity();
            try {
                dumpInner(fd, pw, args);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private void dumpInner(FileDescriptor fd, PrintWriter pw, String[] args) {
        int i;
        ArrayList<String> files;
        long now = SystemClock.uptimeMillis();
        boolean isCheckin = false;
        boolean isCompact = false;
        boolean isCsv = false;
        boolean currentOnly = false;
        boolean dumpDetails = false;
        boolean dumpFullDetails = false;
        boolean dumpAll = false;
        boolean quit = false;
        int aggregateHours = 0;
        int lastIndex = 0;
        int maxNum = 2;
        boolean activeOnly = false;
        String reqPackage = null;
        boolean csvSepScreenStats = false;
        int[] csvScreenStats = new int[]{0, 4};
        boolean csvSepMemStats = false;
        int[] csvMemStats = new int[]{3};
        boolean csvSepProcStats = true;
        int[] csvProcStats = ProcessStats.ALL_PROC_STATES;
        if (args != null) {
            i = 0;
            while (i < args.length) {
                String arg = args[i];
                boolean[] sep;
                String[] error;
                if ("--checkin".equals(arg)) {
                    isCheckin = true;
                } else if ("-c".equals(arg)) {
                    isCompact = true;
                } else if ("--csv".equals(arg)) {
                    isCsv = true;
                } else if ("--csv-screen".equals(arg)) {
                    i++;
                    if (i >= args.length) {
                        pw.println("Error: argument required for --csv-screen");
                        dumpHelp(pw);
                        return;
                    }
                    sep = new boolean[1];
                    error = new String[1];
                    csvScreenStats = parseStateList(DumpUtils.ADJ_SCREEN_NAMES_CSV, 4, args[i], sep, error);
                    if (csvScreenStats == null) {
                        pw.println("Error in \"" + args[i] + "\": " + error[0]);
                        dumpHelp(pw);
                        return;
                    }
                    csvSepScreenStats = sep[0];
                } else if ("--csv-mem".equals(arg)) {
                    i++;
                    if (i >= args.length) {
                        pw.println("Error: argument required for --csv-mem");
                        dumpHelp(pw);
                        return;
                    }
                    sep = new boolean[1];
                    error = new String[1];
                    csvMemStats = parseStateList(DumpUtils.ADJ_MEM_NAMES_CSV, 1, args[i], sep, error);
                    if (csvMemStats == null) {
                        pw.println("Error in \"" + args[i] + "\": " + error[0]);
                        dumpHelp(pw);
                        return;
                    }
                    csvSepMemStats = sep[0];
                } else if ("--csv-proc".equals(arg)) {
                    i++;
                    if (i >= args.length) {
                        pw.println("Error: argument required for --csv-proc");
                        dumpHelp(pw);
                        return;
                    }
                    sep = new boolean[1];
                    error = new String[1];
                    csvProcStats = parseStateList(DumpUtils.STATE_NAMES_CSV, 1, args[i], sep, error);
                    if (csvProcStats == null) {
                        pw.println("Error in \"" + args[i] + "\": " + error[0]);
                        dumpHelp(pw);
                        return;
                    }
                    csvSepProcStats = sep[0];
                } else if ("--details".equals(arg)) {
                    dumpDetails = true;
                } else if ("--full-details".equals(arg)) {
                    dumpFullDetails = true;
                } else if ("--hours".equals(arg)) {
                    i++;
                    if (i >= args.length) {
                        pw.println("Error: argument required for --hours");
                        dumpHelp(pw);
                        return;
                    }
                    try {
                        aggregateHours = Integer.parseInt(args[i]);
                    } catch (NumberFormatException e) {
                        pw.println("Error: --hours argument not an int -- " + args[i]);
                        dumpHelp(pw);
                        return;
                    }
                } else if ("--last".equals(arg)) {
                    i++;
                    if (i >= args.length) {
                        pw.println("Error: argument required for --last");
                        dumpHelp(pw);
                        return;
                    }
                    try {
                        lastIndex = Integer.parseInt(args[i]);
                    } catch (NumberFormatException e2) {
                        pw.println("Error: --last argument not an int -- " + args[i]);
                        dumpHelp(pw);
                        return;
                    }
                } else if ("--max".equals(arg)) {
                    i++;
                    if (i >= args.length) {
                        pw.println("Error: argument required for --max");
                        dumpHelp(pw);
                        return;
                    }
                    try {
                        maxNum = Integer.parseInt(args[i]);
                    } catch (NumberFormatException e3) {
                        pw.println("Error: --max argument not an int -- " + args[i]);
                        dumpHelp(pw);
                        return;
                    }
                } else if ("--active".equals(arg)) {
                    activeOnly = true;
                    currentOnly = true;
                } else if ("--current".equals(arg)) {
                    currentOnly = true;
                } else if ("--commit".equals(arg)) {
                    synchronized (this.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            ProcessStats processStats = this.mProcessStats;
                            processStats.mFlags |= 1;
                            writeStateLocked(true, true);
                            pw.println("Process stats committed.");
                            quit = true;
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                } else if ("--reset".equals(arg)) {
                    synchronized (this.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            this.mProcessStats.resetSafely();
                            pw.println("Process stats reset.");
                            quit = true;
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                } else if ("--clear".equals(arg)) {
                    synchronized (this.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            this.mProcessStats.resetSafely();
                            files = getCommittedFiles(0, true, true);
                            if (files != null) {
                                for (int fi = 0; fi < files.size(); fi++) {
                                    new File((String) files.get(fi)).delete();
                                }
                            }
                            pw.println("All process stats cleared.");
                            quit = true;
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                } else if ("--write".equals(arg)) {
                    synchronized (this.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            writeStateSyncLocked();
                            pw.println("Process stats written.");
                            quit = true;
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                } else if ("--read".equals(arg)) {
                    synchronized (this.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            readLocked(this.mProcessStats, this.mFile);
                            pw.println("Process stats read.");
                            quit = true;
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                } else if ("--start-testing".equals(arg)) {
                    synchronized (this.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            this.mAm.setTestPssMode(true);
                            pw.println("Started high frequency sampling.");
                            quit = true;
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                } else if ("--stop-testing".equals(arg)) {
                    synchronized (this.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            this.mAm.setTestPssMode(false);
                            pw.println("Stopped high frequency sampling.");
                            quit = true;
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                } else if ("--pretend-screen-on".equals(arg)) {
                    synchronized (this.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            this.mInjectedScreenState = Boolean.valueOf(true);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    quit = true;
                } else if ("--pretend-screen-off".equals(arg)) {
                    synchronized (this.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            this.mInjectedScreenState = Boolean.valueOf(false);
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    quit = true;
                } else if ("--stop-pretend-screen".equals(arg)) {
                    synchronized (this.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            this.mInjectedScreenState = null;
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    quit = true;
                } else if ("-h".equals(arg)) {
                    dumpHelp(pw);
                    return;
                } else if ("-a".equals(arg)) {
                    dumpDetails = true;
                    dumpAll = true;
                } else if (arg.length() <= 0 || arg.charAt(0) != '-') {
                    reqPackage = arg;
                    dumpDetails = true;
                } else {
                    pw.println("Unknown option: " + arg);
                    dumpHelp(pw);
                    return;
                }
                i++;
            }
        }
        if (!quit) {
            AtomicFile atomicFile;
            ProcessStats processStats2;
            boolean checkedIn;
            if (isCsv) {
                pw.print("Processes running summed over");
                if (!csvSepScreenStats) {
                    for (int printScreenLabelCsv : csvScreenStats) {
                        pw.print(" ");
                        DumpUtils.printScreenLabelCsv(pw, printScreenLabelCsv);
                    }
                }
                if (!csvSepMemStats) {
                    for (int printScreenLabelCsv2 : csvMemStats) {
                        pw.print(" ");
                        DumpUtils.printMemLabelCsv(pw, printScreenLabelCsv2);
                    }
                }
                if (!csvSepProcStats) {
                    for (int i2 : csvProcStats) {
                        pw.print(" ");
                        pw.print(DumpUtils.STATE_NAMES_CSV[i2]);
                    }
                }
                pw.println();
                synchronized (this.mAm) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        dumpFilteredProcessesCsvLocked(pw, null, csvSepScreenStats, csvScreenStats, csvSepMemStats, csvMemStats, csvSepProcStats, csvProcStats, now, reqPackage);
                    } finally {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                    }
                }
            } else if (aggregateHours != 0) {
                pw.print("AGGREGATED OVER LAST ");
                pw.print(aggregateHours);
                pw.println(" HOURS:");
                dumpAggregatedStats(pw, (long) aggregateHours, now, reqPackage, isCompact, dumpDetails, dumpFullDetails, dumpAll, activeOnly);
            } else if (lastIndex > 0) {
                pw.print("LAST STATS AT INDEX ");
                pw.print(lastIndex);
                pw.println(":");
                files = getCommittedFiles(0, false, true);
                if (lastIndex >= files.size()) {
                    pw.print("Only have ");
                    pw.print(files.size());
                    pw.println(" data sets");
                    return;
                }
                atomicFile = new AtomicFile(new File((String) files.get(lastIndex)));
                processStats2 = new ProcessStats(false);
                readLocked(processStats2, atomicFile);
                if (processStats2.mReadError != null) {
                    if (isCheckin || isCompact) {
                        pw.print("err,");
                    }
                    pw.print("Failure reading ");
                    pw.print((String) files.get(lastIndex));
                    pw.print("; ");
                    pw.println(processStats2.mReadError);
                    return;
                }
                checkedIn = atomicFile.getBaseFile().getPath().endsWith(STATE_FILE_CHECKIN_SUFFIX);
                if (isCheckin || isCompact) {
                    processStats2.dumpCheckinLocked(pw, reqPackage);
                } else {
                    pw.print("COMMITTED STATS FROM ");
                    pw.print(processStats2.mTimePeriodStartClockStr);
                    if (checkedIn) {
                        pw.print(" (checked in)");
                    }
                    pw.println(":");
                    if (dumpDetails || dumpFullDetails) {
                        processStats2.dumpLocked(pw, reqPackage, now, dumpFullDetails ^ 1, dumpAll, activeOnly);
                        if (dumpAll) {
                            pw.print("  mFile=");
                            pw.println(this.mFile.getBaseFile());
                        }
                    } else {
                        processStats2.dumpSummaryLocked(pw, reqPackage, now, activeOnly);
                    }
                }
            } else {
                boolean sepNeeded = false;
                if (dumpAll || isCheckin) {
                    this.mWriteLock.lock();
                    try {
                        files = getCommittedFiles(0, false, isCheckin ^ 1);
                        if (files != null) {
                            int start = isCheckin ? 0 : files.size() - maxNum;
                            if (start < 0) {
                                start = 0;
                            }
                            i = start;
                            while (i < files.size()) {
                                atomicFile = new AtomicFile(new File((String) files.get(i)));
                                processStats2 = new ProcessStats(false);
                                readLocked(processStats2, atomicFile);
                                if (processStats2.mReadError != null) {
                                    if (isCheckin || isCompact) {
                                        pw.print("err,");
                                    }
                                    pw.print("Failure reading ");
                                    pw.print((String) files.get(i));
                                    pw.print("; ");
                                    pw.println(processStats2.mReadError);
                                    new File((String) files.get(i)).delete();
                                } else {
                                    String fileStr = atomicFile.getBaseFile().getPath();
                                    checkedIn = fileStr.endsWith(STATE_FILE_CHECKIN_SUFFIX);
                                    if (isCheckin || isCompact) {
                                        processStats2.dumpCheckinLocked(pw, reqPackage);
                                    } else {
                                        if (sepNeeded) {
                                            pw.println();
                                        } else {
                                            sepNeeded = true;
                                        }
                                        pw.print("COMMITTED STATS FROM ");
                                        pw.print(processStats2.mTimePeriodStartClockStr);
                                        if (checkedIn) {
                                            pw.print(" (checked in)");
                                        }
                                        pw.println(":");
                                        if (dumpFullDetails) {
                                            processStats2.dumpLocked(pw, reqPackage, now, false, false, activeOnly);
                                        } else {
                                            processStats2.dumpSummaryLocked(pw, reqPackage, now, activeOnly);
                                        }
                                    }
                                    if (isCheckin) {
                                        atomicFile.getBaseFile().renameTo(new File(fileStr + STATE_FILE_CHECKIN_SUFFIX));
                                    }
                                }
                                i++;
                            }
                        }
                        this.mWriteLock.unlock();
                    } catch (Throwable th) {
                        this.mWriteLock.unlock();
                    }
                }
                if (!isCheckin) {
                    synchronized (this.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            if (isCompact) {
                                this.mProcessStats.dumpCheckinLocked(pw, reqPackage);
                            } else {
                                if (sepNeeded) {
                                    pw.println();
                                }
                                pw.println("CURRENT STATS:");
                                if (dumpDetails || dumpFullDetails) {
                                    this.mProcessStats.dumpLocked(pw, reqPackage, now, dumpFullDetails ^ 1, dumpAll, activeOnly);
                                    if (dumpAll) {
                                        pw.print("  mFile=");
                                        pw.println(this.mFile.getBaseFile());
                                    }
                                } else {
                                    this.mProcessStats.dumpSummaryLocked(pw, reqPackage, now, activeOnly);
                                }
                                sepNeeded = true;
                            }
                        } finally {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    if (!currentOnly) {
                        if (sepNeeded) {
                            pw.println();
                        }
                        pw.println("AGGREGATED OVER LAST 24 HOURS:");
                        dumpAggregatedStats(pw, 24, now, reqPackage, isCompact, dumpDetails, dumpFullDetails, dumpAll, activeOnly);
                        pw.println();
                        pw.println("AGGREGATED OVER LAST 3 HOURS:");
                        dumpAggregatedStats(pw, 3, now, reqPackage, isCompact, dumpDetails, dumpFullDetails, dumpAll, activeOnly);
                    }
                }
            }
        }
    }
}

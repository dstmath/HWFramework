package com.android.server.am;

import android.os.Binder;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.format.DateFormat;
import android.util.ArrayMap;
import android.util.AtomicFile;
import android.util.Log;
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
import com.android.internal.logging.EventLogTags;
import com.android.internal.os.BackgroundThread;
import com.android.server.utils.PriorityDump;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
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
    @GuardedBy({"mAm"})
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
            /* class com.android.server.am.ProcessStatsService.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                synchronized (ProcessStatsService.this.mAm) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        if (ProcessStatsService.this.mProcessStats.evaluateSystemProperties(false)) {
                            ProcessStatsService.this.mProcessStats.mFlags |= 4;
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
            return ProcessStatsService.super.onTransact(code, data, reply, flags);
        } catch (RuntimeException e) {
            if (!(e instanceof SecurityException)) {
                Slog.wtf(TAG, "Process Stats Crash", e);
            }
            throw e;
        }
    }

    @GuardedBy({"mAm"})
    public void updateProcessStateHolderLocked(ProcessStats.ProcessStateHolder holder, String packageName, int uid, long versionCode, String processName) {
        holder.pkg = this.mProcessStats.getPackageStateLocked(packageName, uid, versionCode);
        holder.state = this.mProcessStats.getProcessStateLocked(holder.pkg, processName);
    }

    @GuardedBy({"mAm"})
    public ProcessState getProcessStateLocked(String packageName, int uid, long versionCode, String processName) {
        return this.mProcessStats.getProcessStateLocked(packageName, uid, versionCode, processName);
    }

    @GuardedBy({"mAm"})
    public ServiceState getServiceStateLocked(String packageName, int uid, long versionCode, String processName, String className) {
        return this.mProcessStats.getServiceStateLocked(packageName, uid, versionCode, processName, className);
    }

    public boolean isMemFactorLowered() {
        return this.mMemFactorLowered;
    }

    @GuardedBy({"mAm"})
    public boolean setMemFactorLocked(int memFactor, boolean screenOn, long now) {
        this.mMemFactorLowered = memFactor < this.mLastMemOnlyState;
        this.mLastMemOnlyState = memFactor;
        Boolean bool = this.mInjectedScreenState;
        if (bool != null) {
            screenOn = bool.booleanValue();
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
        ProcessStats processStats = this.mProcessStats;
        processStats.mMemFactor = memFactor;
        processStats.mStartTime = now;
        ArrayMap<String, SparseArray<LongSparseArray<ProcessStats.PackageState>>> pmap = processStats.mPackages.getMap();
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

    @GuardedBy({"mAm"})
    public int getMemFactorLocked() {
        if (this.mProcessStats.mMemFactor != -1) {
            return this.mProcessStats.mMemFactor;
        }
        return 0;
    }

    @GuardedBy({"mAm"})
    public void addSysMemUsageLocked(long cachedMem, long freeMem, long zramMem, long kernelMem, long nativeMem) {
        this.mProcessStats.addSysMemUsage(cachedMem, freeMem, zramMem, kernelMem, nativeMem);
    }

    @GuardedBy({"mAm"})
    public void updateTrackingAssociationsLocked(int curSeq, long now) {
        this.mProcessStats.updateTrackingAssociationsLocked(curSeq, now);
    }

    @GuardedBy({"mAm"})
    public boolean shouldWriteNowLocked(long now) {
        if (now <= this.mLastWriteTime + WRITE_PERIOD) {
            return false;
        }
        if (SystemClock.elapsedRealtime() > this.mProcessStats.mTimePeriodStartRealtime + ProcessStats.COMMIT_PERIOD && SystemClock.uptimeMillis() > this.mProcessStats.mTimePeriodStartUptime + ProcessStats.COMMIT_UPTIME_PERIOD) {
            this.mCommitPending = true;
        }
        return true;
    }

    @GuardedBy({"mAm"})
    public void shutdownLocked() {
        Slog.w(TAG, "Writing process stats before shutdown...");
        this.mProcessStats.mFlags |= 2;
        writeStateSyncLocked();
        this.mShuttingDown = true;
    }

    @GuardedBy({"mAm"})
    public void writeStateAsyncLocked() {
        writeStateLocked(false);
    }

    @GuardedBy({"mAm"})
    public void writeStateSyncLocked() {
        writeStateLocked(true);
    }

    @GuardedBy({"mAm"})
    private void writeStateLocked(boolean sync) {
        if (!this.mShuttingDown) {
            boolean commitPending = this.mCommitPending;
            this.mCommitPending = false;
            writeStateLocked(sync, commitPending);
        }
    }

    @GuardedBy({"mAm"})
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
                    /* class com.android.server.am.ProcessStatsService.AnonymousClass2 */

                    @Override // java.lang.Runnable
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
    public void performWriteState(long initialTime) {
        Parcel data;
        AtomicFile file;
        synchronized (this.mPendingWriteLock) {
            data = this.mPendingWrite;
            file = this.mPendingWriteFile;
            this.mPendingWriteCommitted = false;
            if (data != null) {
                this.mPendingWrite = null;
                this.mPendingWriteFile = null;
                this.mWriteLock.lock();
            } else {
                return;
            }
        }
        long startTime = SystemClock.uptimeMillis();
        FileOutputStream stream = null;
        try {
            stream = file.startWrite();
            stream.write(data.marshall());
            stream.flush();
            file.finishWrite(stream);
            EventLogTags.writeCommitSysConfigFile("procstats", (SystemClock.uptimeMillis() - startTime) + initialTime);
        } catch (IOException e) {
            Slog.w(TAG, "Error writing process statistics", e);
            file.failWrite(stream);
        } catch (Throwable th) {
            data.recycle();
            trimHistoricStatesWriteLocked();
            this.mWriteLock.unlock();
            throw th;
        }
        data.recycle();
        trimHistoricStatesWriteLocked();
        this.mWriteLock.unlock();
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mAm"})
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

    @GuardedBy({"mAm"})
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
    @GuardedBy({"mAm"})
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

    static int parseSectionOptions(String optionsStr) {
        String[] sectionsStr = optionsStr.split(",");
        if (sectionsStr.length == 0) {
            return 15;
        }
        int res = 0;
        List<String> optionStrList = Arrays.asList(ProcessStats.OPTIONS_STR);
        for (String sectionStr : sectionsStr) {
            int optionIndex = optionStrList.indexOf(sectionStr);
            if (optionIndex != -1) {
                res |= ProcessStats.OPTIONS[optionIndex];
            }
        }
        return res;
    }

    /* JADX INFO: finally extract failed */
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
            } catch (Throwable th) {
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        this.mWriteLock.lock();
        if (historic != null) {
            try {
                ArrayList<String> files = getCommittedFiles(0, false, true);
                if (files != null) {
                    for (int i = files.size() - 1; i >= 0; i--) {
                        try {
                            historic.add(ParcelFileDescriptor.open(new File(files.get(i)), 268435456));
                        } catch (IOException e) {
                            Slog.w(TAG, "Failure opening procstat file " + files.get(i), e);
                        }
                    }
                }
            } catch (Throwable th2) {
                this.mWriteLock.unlock();
                throw th2;
            }
        }
        this.mWriteLock.unlock();
        return current.marshall();
    }

    public long getCommittedStats(long highWaterMarkMs, int section, boolean doAggregate, List<ParcelFileDescriptor> committedStats) {
        Throwable th;
        IOException e;
        String highWaterMarkStr;
        ArrayList<String> files;
        String str;
        IOException e2;
        IndexOutOfBoundsException e3;
        String str2 = STATE_FILE_PREFIX;
        this.mAm.mContext.enforceCallingOrSelfPermission("android.permission.PACKAGE_USAGE_STATS", null);
        ProcessStats mergedStats = new ProcessStats(false);
        long newHighWaterMark = highWaterMarkMs;
        this.mWriteLock.lock();
        try {
            ArrayList<String> files2 = getCommittedFiles(0, false, true);
            if (files2 != null) {
                try {
                    String highWaterMarkStr2 = DateFormat.format("yyyy-MM-dd-HH-mm-ss", highWaterMarkMs).toString();
                    ProcessStats stats = new ProcessStats(false);
                    int i = files2.size() - 1;
                    while (i >= 0) {
                        String fileName = files2.get(i);
                        try {
                            str = str2;
                            try {
                                if (fileName.substring(fileName.lastIndexOf(str2) + str2.length(), fileName.lastIndexOf(STATE_FILE_SUFFIX)).compareToIgnoreCase(highWaterMarkStr2) > 0) {
                                    InputStream is = new ParcelFileDescriptor.AutoCloseInputStream(ParcelFileDescriptor.open(new File(fileName), 268435456));
                                    stats.reset();
                                    stats.read(is);
                                    is.close();
                                    files = files2;
                                    highWaterMarkStr = highWaterMarkStr2;
                                    try {
                                        if (stats.mTimePeriodStartClock > newHighWaterMark) {
                                            newHighWaterMark = stats.mTimePeriodStartClock;
                                        }
                                        if (doAggregate) {
                                            mergedStats.add(stats);
                                        } else {
                                            committedStats.add(protoToParcelFileDescriptor(stats, section));
                                        }
                                        if (stats.mReadError != null) {
                                            Log.w(TAG, "Failure reading process stats: " + stats.mReadError);
                                        }
                                    } catch (IOException e4) {
                                        e2 = e4;
                                        Slog.w(TAG, "Failure opening procstat file " + fileName, e2);
                                        i--;
                                        str2 = str;
                                        files2 = files;
                                        highWaterMarkStr2 = highWaterMarkStr;
                                    } catch (IndexOutOfBoundsException e5) {
                                        e3 = e5;
                                        Slog.w(TAG, "Failure to read and parse commit file " + fileName, e3);
                                        i--;
                                        str2 = str;
                                        files2 = files;
                                        highWaterMarkStr2 = highWaterMarkStr;
                                    }
                                } else {
                                    files = files2;
                                    highWaterMarkStr = highWaterMarkStr2;
                                }
                            } catch (IOException e6) {
                                e2 = e6;
                                files = files2;
                                highWaterMarkStr = highWaterMarkStr2;
                                Slog.w(TAG, "Failure opening procstat file " + fileName, e2);
                                i--;
                                str2 = str;
                                files2 = files;
                                highWaterMarkStr2 = highWaterMarkStr;
                            } catch (IndexOutOfBoundsException e7) {
                                e3 = e7;
                                files = files2;
                                highWaterMarkStr = highWaterMarkStr2;
                                Slog.w(TAG, "Failure to read and parse commit file " + fileName, e3);
                                i--;
                                str2 = str;
                                files2 = files;
                                highWaterMarkStr2 = highWaterMarkStr;
                            }
                        } catch (IOException e8) {
                            e2 = e8;
                            str = str2;
                            files = files2;
                            highWaterMarkStr = highWaterMarkStr2;
                            Slog.w(TAG, "Failure opening procstat file " + fileName, e2);
                            i--;
                            str2 = str;
                            files2 = files;
                            highWaterMarkStr2 = highWaterMarkStr;
                        } catch (IndexOutOfBoundsException e9) {
                            e3 = e9;
                            str = str2;
                            files = files2;
                            highWaterMarkStr = highWaterMarkStr2;
                            Slog.w(TAG, "Failure to read and parse commit file " + fileName, e3);
                            i--;
                            str2 = str;
                            files2 = files;
                            highWaterMarkStr2 = highWaterMarkStr;
                        }
                        i--;
                        str2 = str;
                        files2 = files;
                        highWaterMarkStr2 = highWaterMarkStr;
                    }
                    if (doAggregate) {
                        committedStats.add(protoToParcelFileDescriptor(mergedStats, section));
                    }
                    this.mWriteLock.unlock();
                    return newHighWaterMark;
                } catch (IOException e10) {
                    e = e10;
                    try {
                        Slog.w(TAG, "Failure opening procstat file", e);
                        this.mWriteLock.unlock();
                        return newHighWaterMark;
                    } catch (Throwable th2) {
                        th = th2;
                    }
                }
            } else {
                this.mWriteLock.unlock();
                return newHighWaterMark;
            }
        } catch (IOException e11) {
            e = e11;
            Slog.w(TAG, "Failure opening procstat file", e);
            this.mWriteLock.unlock();
            return newHighWaterMark;
        } catch (Throwable th3) {
            th = th3;
            this.mWriteLock.unlock();
            throw th;
        }
    }

    private ParcelFileDescriptor protoToParcelFileDescriptor(final ProcessStats stats, final int section) throws IOException {
        final ParcelFileDescriptor[] fds = ParcelFileDescriptor.createPipe();
        new Thread("ProcessStats pipe output") {
            /* class com.android.server.am.ProcessStatsService.AnonymousClass3 */

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                synchronized (ProcessStatsService.this.mAm) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        FileOutputStream fout = new ParcelFileDescriptor.AutoCloseOutputStream(fds[1]);
                        ProtoOutputStream proto = new ProtoOutputStream(fout);
                        stats.writeToProto(proto, stats.mTimePeriodEndRealtime, section);
                        proto.flush();
                        fout.close();
                    } catch (IOException e) {
                        Slog.w(ProcessStatsService.TAG, "Failure writing pipe", e);
                    } catch (IndexOutOfBoundsException e2) {
                        Slog.w(ProcessStatsService.TAG, "Failure writing proto", e2);
                    } catch (NullPointerException e3) {
                        Slog.w(ProcessStatsService.TAG, "Failure writing proto", e3);
                    } catch (Throwable th) {
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }.start();
        return fds[0];
    }

    /* JADX INFO: finally extract failed */
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
            } catch (Throwable th) {
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
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
                    while (i >= 0 && stats.mTimePeriodEndRealtime - stats.mTimePeriodStartRealtime < minTime) {
                        AtomicFile file = new AtomicFile(new File(files.get(i)));
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
                            Slog.w(TAG, "Failure reading " + files.get(i + 1) + "; " + moreStats.mReadError);
                        }
                    }
                    current = Parcel.obtain();
                    stats.writeToParcel(current, 0);
                }
            } catch (IOException e) {
                Slog.w(TAG, "Failed building output pipe", e);
                this.mWriteLock.unlock();
                return null;
            } catch (Throwable th2) {
                this.mWriteLock.unlock();
                throw th2;
            }
        }
        final byte[] outData = current.marshall();
        current.recycle();
        final ParcelFileDescriptor[] fds = ParcelFileDescriptor.createPipe();
        new Thread("ProcessStats pipe output") {
            /* class com.android.server.am.ProcessStatsService.AnonymousClass4 */

            @Override // java.lang.Thread, java.lang.Runnable
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

    private void dumpAggregatedStats(PrintWriter pw, long aggregateHours, long now, String reqPackage, boolean isCompact, boolean dumpDetails, boolean dumpFullDetails, boolean dumpAll, boolean activeOnly, int section) {
        ParcelFileDescriptor pfd = getStatsOverTime((((aggregateHours * 60) * 60) * 1000) - (ProcessStats.COMMIT_PERIOD / 2));
        if (pfd == null) {
            pw.println("Unable to build stats!");
            return;
        }
        ProcessStats stats = new ProcessStats(false);
        stats.read(new ParcelFileDescriptor.AutoCloseInputStream(pfd));
        if (stats.mReadError != null) {
            pw.print("Failure reading: ");
            pw.println(stats.mReadError);
        } else if (isCompact) {
            stats.dumpCheckinLocked(pw, reqPackage, section);
        } else if (dumpDetails || dumpFullDetails) {
            stats.dumpLocked(pw, reqPackage, now, !dumpFullDetails, dumpDetails, dumpAll, activeOnly, section);
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
        pw.println("  --section: proc|pkg-proc|pkg-svc|pkg-asc|pkg-all|all ");
        pw.println("    options can be combined to select desired stats");
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

    /* JADX INFO: finally extract failed */
    /* JADX INFO: Multiple debug info for r0v3 int[]: [D('csvMemStats' int[]), D('csvScreenStats' int[])] */
    /* JADX INFO: Multiple debug info for r0v168 boolean: [D('sep' boolean[]), D('csvSepProcStats' boolean)] */
    /* JADX INFO: Multiple debug info for r0v172 boolean: [D('sep' boolean[]), D('csvSepMemStats' boolean)] */
    /* JADX INFO: Multiple debug info for r2v80 java.lang.String[]: [D('isCompact' boolean), D('error' java.lang.String[])] */
    /* JADX INFO: Multiple debug info for r0v176 boolean: [D('sep' boolean[]), D('csvSepScreenStats' boolean)] */
    /* JADX WARNING: Removed duplicated region for block: B:381:0x0842 A[Catch:{ all -> 0x0867 }] */
    /* JADX WARNING: Removed duplicated region for block: B:382:0x0862  */
    private void dumpInner(PrintWriter pw, String[] args) {
        boolean currentOnly;
        boolean isCsv;
        boolean isCompact;
        boolean csvSepProcStats;
        boolean csvSepMemStats;
        boolean csvSepScreenStats;
        boolean csvSepScreenStats2;
        int maxNum;
        boolean dumpAll;
        boolean activeOnly;
        boolean csvSepScreenStats3;
        int[] csvMemStats;
        int[] csvMemStats2;
        int[] csvProcStats;
        int aggregateHours;
        int lastIndex;
        int section;
        boolean z;
        boolean z2;
        boolean sepNeeded;
        Throwable th;
        boolean sepNeeded2;
        String reqPackage;
        int section2;
        Throwable th2;
        int start;
        boolean z3;
        int i;
        int i2;
        Throwable e;
        String fileStr;
        String reqPackage2;
        ProcessStats processStats;
        int section3;
        boolean sepNeeded3;
        String reqPackage3;
        ProcessStats processStats2;
        int section4;
        ActivityManagerService activityManagerService;
        Throwable th3;
        Throwable th4;
        long now = SystemClock.uptimeMillis();
        String reqPackage4 = null;
        boolean isCheckin = false;
        int aggregateHours2 = 0;
        int aggregateHours3 = 1;
        int[] csvScreenStats = {0, 4};
        boolean quit = false;
        int[] csvScreenStats2 = {3};
        int[] csvProcStats2 = ProcessStats.ALL_PROC_STATES;
        int section5 = 15;
        if (args != null) {
            csvSepProcStats = true;
            boolean csvSepScreenStats4 = false;
            int maxNum2 = 2;
            int[] csvMemStats3 = csvScreenStats2;
            boolean isCompact2 = false;
            int i3 = 0;
            csvSepMemStats = false;
            boolean activeOnly2 = false;
            int lastIndex2 = 0;
            boolean dumpAll2 = false;
            boolean dumpFullDetails = false;
            boolean dumpDetails = false;
            boolean currentOnly2 = false;
            boolean isCsv2 = false;
            while (i3 < args.length) {
                String arg = args[i3];
                if ("--checkin".equals(arg)) {
                    isCheckin = true;
                } else if ("-c".equals(arg)) {
                    isCompact2 = true;
                } else if ("--csv".equals(arg)) {
                    isCsv2 = true;
                } else if ("--csv-screen".equals(arg)) {
                    i3++;
                    if (i3 >= args.length) {
                        pw.println("Error: argument required for --csv-screen");
                        dumpHelp(pw);
                        return;
                    }
                    boolean[] sep = new boolean[aggregateHours3];
                    String[] error = new String[aggregateHours3];
                    int[] csvScreenStats3 = parseStateList(DumpUtils.ADJ_SCREEN_NAMES_CSV, 4, args[i3], sep, error);
                    if (csvScreenStats3 == null) {
                        pw.println("Error in \"" + args[i3] + "\": " + error[0]);
                        dumpHelp(pw);
                        return;
                    }
                    csvSepScreenStats4 = sep[0];
                    csvScreenStats = csvScreenStats3;
                    isCompact2 = isCompact2;
                    isCsv2 = isCsv2;
                    currentOnly2 = currentOnly2;
                } else if ("--csv-mem".equals(arg)) {
                    i3++;
                    if (i3 >= args.length) {
                        pw.println("Error: argument required for --csv-mem");
                        dumpHelp(pw);
                        return;
                    }
                    boolean[] sep2 = new boolean[1];
                    String[] error2 = new String[1];
                    int[] csvMemStats4 = parseStateList(DumpUtils.ADJ_MEM_NAMES_CSV, 1, args[i3], sep2, error2);
                    if (csvMemStats4 == null) {
                        pw.println("Error in \"" + args[i3] + "\": " + error2[0]);
                        dumpHelp(pw);
                        return;
                    }
                    csvSepMemStats = sep2[0];
                    csvMemStats3 = csvMemStats4;
                    isCompact2 = isCompact2;
                    isCsv2 = isCsv2;
                    currentOnly2 = currentOnly2;
                } else if ("--csv-proc".equals(arg)) {
                    i3++;
                    if (i3 >= args.length) {
                        pw.println("Error: argument required for --csv-proc");
                        dumpHelp(pw);
                        return;
                    }
                    boolean[] sep3 = new boolean[1];
                    String[] error3 = new String[1];
                    int[] csvProcStats3 = parseStateList(DumpUtils.STATE_NAMES_CSV, 1, args[i3], sep3, error3);
                    if (csvProcStats3 == null) {
                        pw.println("Error in \"" + args[i3] + "\": " + error3[0]);
                        dumpHelp(pw);
                        return;
                    }
                    csvSepProcStats = sep3[0];
                    csvProcStats2 = csvProcStats3;
                    isCompact2 = isCompact2;
                    isCsv2 = isCsv2;
                    currentOnly2 = currentOnly2;
                } else if ("--details".equals(arg)) {
                    dumpDetails = true;
                    isCompact2 = isCompact2;
                    isCsv2 = isCsv2;
                    currentOnly2 = currentOnly2;
                } else if ("--full-details".equals(arg)) {
                    dumpFullDetails = true;
                    isCompact2 = isCompact2;
                    isCsv2 = isCsv2;
                    currentOnly2 = currentOnly2;
                } else if ("--hours".equals(arg)) {
                    i3++;
                    if (i3 >= args.length) {
                        pw.println("Error: argument required for --hours");
                        dumpHelp(pw);
                        return;
                    }
                    try {
                        aggregateHours2 = Integer.parseInt(args[i3]);
                        isCompact2 = isCompact2;
                        isCsv2 = isCsv2;
                        currentOnly2 = currentOnly2;
                    } catch (NumberFormatException e2) {
                        pw.println("Error: --hours argument not an int -- " + args[i3]);
                        dumpHelp(pw);
                        return;
                    }
                } else if ("--last".equals(arg)) {
                    i3++;
                    if (i3 >= args.length) {
                        pw.println("Error: argument required for --last");
                        dumpHelp(pw);
                        return;
                    }
                    try {
                        lastIndex2 = Integer.parseInt(args[i3]);
                        isCompact2 = isCompact2;
                        isCsv2 = isCsv2;
                        currentOnly2 = currentOnly2;
                    } catch (NumberFormatException e3) {
                        pw.println("Error: --last argument not an int -- " + args[i3]);
                        dumpHelp(pw);
                        return;
                    }
                } else if ("--max".equals(arg)) {
                    i3++;
                    if (i3 >= args.length) {
                        pw.println("Error: argument required for --max");
                        dumpHelp(pw);
                        return;
                    }
                    try {
                        maxNum2 = Integer.parseInt(args[i3]);
                        isCompact2 = isCompact2;
                        isCsv2 = isCsv2;
                        currentOnly2 = currentOnly2;
                    } catch (NumberFormatException e4) {
                        pw.println("Error: --max argument not an int -- " + args[i3]);
                        dumpHelp(pw);
                        return;
                    }
                } else if ("--active".equals(arg)) {
                    currentOnly2 = true;
                    activeOnly2 = true;
                    isCompact2 = isCompact2;
                    isCsv2 = isCsv2;
                } else if ("--current".equals(arg)) {
                    currentOnly2 = true;
                    isCompact2 = isCompact2;
                    isCsv2 = isCsv2;
                } else if ("--commit".equals(arg)) {
                    synchronized (this.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            this.mProcessStats.mFlags |= 1;
                            writeStateLocked(true, true);
                            pw.println("Process stats committed.");
                            quit = true;
                        } catch (Throwable th5) {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th5;
                        }
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    isCompact2 = isCompact2;
                    isCsv2 = isCsv2;
                    currentOnly2 = currentOnly2;
                } else if ("--section".equals(arg)) {
                    i3++;
                    if (i3 >= args.length) {
                        pw.println("Error: argument required for --section");
                        dumpHelp(pw);
                        return;
                    }
                    section5 = parseSectionOptions(args[i3]);
                    isCompact2 = isCompact2;
                    isCsv2 = isCsv2;
                    currentOnly2 = currentOnly2;
                } else if ("--clear".equals(arg)) {
                    synchronized (this.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            this.mProcessStats.resetSafely();
                            try {
                                this.mAm.requestPssAllProcsLocked(SystemClock.uptimeMillis(), true, false);
                                ArrayList<String> files = getCommittedFiles(0, true, true);
                                if (files != null) {
                                    for (int fi = 0; fi < files.size(); fi++) {
                                        new File(files.get(fi)).delete();
                                    }
                                }
                                pw.println("All process stats cleared.");
                                quit = true;
                            } catch (Throwable th6) {
                                th4 = th6;
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                throw th4;
                            }
                        } catch (Throwable th7) {
                            th4 = th7;
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th4;
                        }
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    isCompact2 = isCompact2;
                    isCsv2 = isCsv2;
                    currentOnly2 = currentOnly2;
                    i3 = i3;
                } else if ("--write".equals(arg)) {
                    synchronized (this.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            writeStateSyncLocked();
                            pw.println("Process stats written.");
                            quit = true;
                        } catch (Throwable th8) {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th8;
                        }
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    isCompact2 = isCompact2;
                    isCsv2 = isCsv2;
                    currentOnly2 = currentOnly2;
                    i3 = i3;
                } else if ("--read".equals(arg)) {
                    synchronized (this.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            readLocked(this.mProcessStats, this.mFile);
                            pw.println("Process stats read.");
                            quit = true;
                        } catch (Throwable th9) {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th9;
                        }
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    isCompact2 = isCompact2;
                    isCsv2 = isCsv2;
                    currentOnly2 = currentOnly2;
                    i3 = i3;
                } else if ("--start-testing".equals(arg)) {
                    synchronized (this.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            this.mAm.setTestPssMode(true);
                            pw.println("Started high frequency sampling.");
                            quit = true;
                        } catch (Throwable th10) {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th10;
                        }
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    isCompact2 = isCompact2;
                    isCsv2 = isCsv2;
                    currentOnly2 = currentOnly2;
                    i3 = i3;
                } else if ("--stop-testing".equals(arg)) {
                    synchronized (this.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            this.mAm.setTestPssMode(false);
                            pw.println("Stopped high frequency sampling.");
                            quit = true;
                        } catch (Throwable th11) {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th11;
                        }
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    isCompact2 = isCompact2;
                    isCsv2 = isCsv2;
                    currentOnly2 = currentOnly2;
                    i3 = i3;
                } else if ("--pretend-screen-on".equals(arg)) {
                    synchronized (this.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            this.mInjectedScreenState = true;
                        } catch (Throwable th12) {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th12;
                        }
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    quit = true;
                    isCompact2 = isCompact2;
                    isCsv2 = isCsv2;
                    currentOnly2 = currentOnly2;
                    i3 = i3;
                } else if ("--pretend-screen-off".equals(arg)) {
                    synchronized (this.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            this.mInjectedScreenState = false;
                        } catch (Throwable th13) {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th13;
                        }
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    quit = true;
                    isCompact2 = isCompact2;
                    isCsv2 = isCsv2;
                    currentOnly2 = currentOnly2;
                    i3 = i3;
                } else if ("--stop-pretend-screen".equals(arg)) {
                    synchronized (this.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            this.mInjectedScreenState = null;
                        } catch (Throwable th14) {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th14;
                        }
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    quit = true;
                    isCompact2 = isCompact2;
                    isCsv2 = isCsv2;
                    currentOnly2 = currentOnly2;
                    i3 = i3;
                } else if ("-h".equals(arg)) {
                    dumpHelp(pw);
                    return;
                } else if ("-a".equals(arg)) {
                    dumpDetails = true;
                    dumpAll2 = true;
                    isCompact2 = isCompact2;
                    isCsv2 = isCsv2;
                    currentOnly2 = currentOnly2;
                    i3 = i3;
                } else if (arg.length() <= 0 || arg.charAt(0) != '-') {
                    reqPackage4 = arg;
                    dumpDetails = true;
                    isCompact2 = isCompact2;
                    isCsv2 = isCsv2;
                    currentOnly2 = currentOnly2;
                    i3 = i3;
                } else {
                    pw.println("Unknown option: " + arg);
                    dumpHelp(pw);
                    return;
                }
                i3++;
                aggregateHours3 = 1;
            }
            isCompact = isCompact2;
            isCsv = isCsv2;
            currentOnly = currentOnly2;
            lastIndex = lastIndex2;
            aggregateHours = aggregateHours2;
            dumpAll = dumpAll2;
            csvMemStats = csvMemStats3;
            csvMemStats2 = csvScreenStats;
            csvProcStats = csvProcStats2;
            maxNum = maxNum2;
            csvSepScreenStats = csvSepScreenStats4;
            csvSepScreenStats3 = dumpDetails;
            csvSepScreenStats2 = activeOnly2;
            activeOnly = dumpFullDetails;
            section = section5;
        } else {
            isCompact = false;
            isCsv = false;
            currentOnly = false;
            lastIndex = 0;
            csvSepMemStats = false;
            aggregateHours = 0;
            csvSepProcStats = true;
            csvProcStats = csvProcStats2;
            activeOnly = false;
            dumpAll = false;
            csvSepScreenStats2 = false;
            csvSepScreenStats = false;
            section = 15;
            csvMemStats = csvScreenStats2;
            csvSepScreenStats3 = false;
            maxNum = 2;
            csvMemStats2 = csvScreenStats;
        }
        if (!quit) {
            if (isCsv) {
                pw.print("Processes running summed over");
                if (!csvSepScreenStats) {
                    for (int i4 : csvMemStats2) {
                        pw.print(" ");
                        DumpUtils.printScreenLabelCsv(pw, i4);
                    }
                }
                if (!csvSepMemStats) {
                    for (int i5 : csvMemStats) {
                        pw.print(" ");
                        DumpUtils.printMemLabelCsv(pw, i5);
                    }
                }
                if (!csvSepProcStats) {
                    for (int i6 : csvProcStats) {
                        pw.print(" ");
                        pw.print(DumpUtils.STATE_NAMES_CSV[i6]);
                    }
                }
                pw.println();
                ActivityManagerService activityManagerService2 = this.mAm;
                synchronized (activityManagerService2) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        activityManagerService = activityManagerService2;
                        try {
                            dumpFilteredProcessesCsvLocked(pw, null, csvSepScreenStats, csvMemStats2, csvSepMemStats, csvMemStats, csvSepProcStats, csvProcStats, now, reqPackage4);
                            ActivityManagerService.resetPriorityAfterLockedSection();
                        } catch (Throwable th15) {
                            th3 = th15;
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th3;
                        }
                    } catch (Throwable th16) {
                        th3 = th16;
                        activityManagerService = activityManagerService2;
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th3;
                    }
                }
            } else if (aggregateHours != 0) {
                pw.print("AGGREGATED OVER LAST ");
                pw.print(aggregateHours);
                pw.println(" HOURS:");
                dumpAggregatedStats(pw, (long) aggregateHours, now, reqPackage4, isCompact, csvSepScreenStats3, activeOnly, dumpAll, csvSepScreenStats2, section);
            } else if (lastIndex > 0) {
                pw.print("LAST STATS AT INDEX ");
                pw.print(lastIndex);
                pw.println(":");
                ArrayList<String> files2 = getCommittedFiles(0, false, true);
                if (lastIndex >= files2.size()) {
                    pw.print("Only have ");
                    pw.print(files2.size());
                    pw.println(" data sets");
                    return;
                }
                AtomicFile file = new AtomicFile(new File(files2.get(lastIndex)));
                ProcessStats processStats3 = new ProcessStats(false);
                readLocked(processStats3, file);
                if (processStats3.mReadError != null) {
                    if (isCheckin || isCompact) {
                        pw.print("err,");
                    }
                    pw.print("Failure reading ");
                    pw.print(files2.get(lastIndex));
                    pw.print("; ");
                    pw.println(processStats3.mReadError);
                    return;
                }
                boolean checkedIn = file.getBaseFile().getPath().endsWith(STATE_FILE_CHECKIN_SUFFIX);
                if (!isCheckin) {
                    if (!isCompact) {
                        pw.print("COMMITTED STATS FROM ");
                        pw.print(processStats3.mTimePeriodStartClockStr);
                        if (checkedIn) {
                            pw.print(" (checked in)");
                        }
                        pw.println(":");
                        if (csvSepScreenStats3 || activeOnly) {
                            processStats3.dumpLocked(pw, reqPackage4, now, !activeOnly, csvSepScreenStats3, dumpAll, csvSepScreenStats2, section);
                            if (dumpAll) {
                                pw.print("  mFile=");
                                pw.println(this.mFile.getBaseFile());
                                return;
                            }
                            return;
                        }
                        processStats3.dumpSummaryLocked(pw, reqPackage4, now, csvSepScreenStats2);
                        return;
                    }
                }
                processStats3.dumpCheckinLocked(pw, reqPackage4, section);
            } else {
                int section6 = section;
                String reqPackage5 = reqPackage4;
                boolean z4 = true;
                boolean sepNeeded4 = false;
                if (dumpAll || isCheckin) {
                    this.mWriteLock.lock();
                    try {
                        ArrayList<String> files3 = getCommittedFiles(0, false, !isCheckin);
                        if (files3 != null) {
                            if (isCheckin) {
                                start = 0;
                            } else {
                                try {
                                    start = files3.size() - maxNum;
                                } catch (Throwable th17) {
                                    th2 = th17;
                                    this.mWriteLock.unlock();
                                    throw th2;
                                }
                            }
                            int i7 = start < 0 ? 0 : start;
                            while (i7 < files3.size()) {
                                try {
                                    AtomicFile file2 = new AtomicFile(new File(files3.get(i7)));
                                    try {
                                        ProcessStats processStats4 = new ProcessStats(false);
                                        readLocked(processStats4, file2);
                                        if (processStats4.mReadError != null) {
                                            if (isCheckin || isCompact) {
                                                try {
                                                    pw.print("err,");
                                                } catch (Throwable th18) {
                                                    e = th18;
                                                    i2 = i7;
                                                    z3 = z4;
                                                    pw.print("**** FAILURE DUMPING STATE: ");
                                                    i = i2;
                                                    pw.println(files3.get(i));
                                                    e.printStackTrace(pw);
                                                    i7 = i + 1;
                                                    z4 = z3;
                                                }
                                            }
                                            pw.print("Failure reading ");
                                            pw.print(files3.get(i7));
                                            pw.print("; ");
                                            pw.println(processStats4.mReadError);
                                            new File(files3.get(i7)).delete();
                                            i = i7;
                                            z3 = z4;
                                            i7 = i + 1;
                                            z4 = z3;
                                        } else {
                                            String fileStr2 = file2.getBaseFile().getPath();
                                            boolean checkedIn2 = fileStr2.endsWith(STATE_FILE_CHECKIN_SUFFIX);
                                            if (isCheckin) {
                                                processStats = processStats4;
                                                fileStr = fileStr2;
                                                i2 = i7;
                                                z3 = z4;
                                                section3 = section6;
                                                reqPackage2 = reqPackage5;
                                            } else if (isCompact) {
                                                processStats = processStats4;
                                                fileStr = fileStr2;
                                                i2 = i7;
                                                z3 = z4;
                                                section3 = section6;
                                                reqPackage2 = reqPackage5;
                                            } else {
                                                if (sepNeeded4) {
                                                    pw.println();
                                                    sepNeeded3 = sepNeeded4;
                                                } else {
                                                    sepNeeded3 = true;
                                                }
                                                try {
                                                    pw.print("COMMITTED STATS FROM ");
                                                    pw.print(processStats4.mTimePeriodStartClockStr);
                                                    if (checkedIn2) {
                                                        try {
                                                            pw.print(" (checked in)");
                                                        } catch (Throwable th19) {
                                                            e = th19;
                                                            i2 = i7;
                                                            z3 = z4;
                                                            sepNeeded4 = sepNeeded3;
                                                        }
                                                    }
                                                    pw.println(":");
                                                    if (activeOnly) {
                                                        processStats2 = processStats4;
                                                        fileStr = fileStr2;
                                                        i2 = i7;
                                                        z3 = z4;
                                                        section4 = section6;
                                                        reqPackage3 = reqPackage5;
                                                        try {
                                                            processStats4.dumpLocked(pw, reqPackage5, now, false, false, false, csvSepScreenStats2, section4);
                                                        } catch (Throwable th20) {
                                                            e = th20;
                                                            sepNeeded4 = sepNeeded3;
                                                            section6 = section4;
                                                            reqPackage5 = reqPackage3;
                                                            pw.print("**** FAILURE DUMPING STATE: ");
                                                            i = i2;
                                                            pw.println(files3.get(i));
                                                            e.printStackTrace(pw);
                                                            i7 = i + 1;
                                                            z4 = z3;
                                                        }
                                                    } else {
                                                        processStats2 = processStats4;
                                                        fileStr = fileStr2;
                                                        i2 = i7;
                                                        z3 = z4;
                                                        section4 = section6;
                                                        reqPackage3 = reqPackage5;
                                                        processStats2.dumpSummaryLocked(pw, reqPackage3, now, csvSepScreenStats2);
                                                    }
                                                    sepNeeded4 = sepNeeded3;
                                                    section6 = section4;
                                                    reqPackage5 = reqPackage3;
                                                    if (!isCheckin) {
                                                        file2.getBaseFile().renameTo(new File(fileStr + STATE_FILE_CHECKIN_SUFFIX));
                                                    }
                                                    i = i2;
                                                } catch (Throwable th21) {
                                                    e = th21;
                                                    i2 = i7;
                                                    z3 = z4;
                                                    sepNeeded4 = sepNeeded3;
                                                    pw.print("**** FAILURE DUMPING STATE: ");
                                                    i = i2;
                                                    pw.println(files3.get(i));
                                                    e.printStackTrace(pw);
                                                    i7 = i + 1;
                                                    z4 = z3;
                                                }
                                                i7 = i + 1;
                                                z4 = z3;
                                            }
                                            section6 = section3;
                                            reqPackage5 = reqPackage2;
                                            try {
                                                processStats.dumpCheckinLocked(pw, reqPackage5, section6);
                                                if (!isCheckin) {
                                                }
                                                i = i2;
                                            } catch (Throwable th22) {
                                                e = th22;
                                                pw.print("**** FAILURE DUMPING STATE: ");
                                                i = i2;
                                                pw.println(files3.get(i));
                                                e.printStackTrace(pw);
                                                i7 = i + 1;
                                                z4 = z3;
                                            }
                                            i7 = i + 1;
                                            z4 = z3;
                                        }
                                    } catch (Throwable th23) {
                                        e = th23;
                                        i2 = i7;
                                        z3 = z4;
                                        pw.print("**** FAILURE DUMPING STATE: ");
                                        i = i2;
                                        pw.println(files3.get(i));
                                        e.printStackTrace(pw);
                                        i7 = i + 1;
                                        z4 = z3;
                                    }
                                } catch (Throwable th24) {
                                    e = th24;
                                    i2 = i7;
                                    z3 = z4;
                                    pw.print("**** FAILURE DUMPING STATE: ");
                                    i = i2;
                                    pw.println(files3.get(i));
                                    e.printStackTrace(pw);
                                    i7 = i + 1;
                                    z4 = z3;
                                }
                            }
                            z = z4;
                            z2 = false;
                        } else {
                            z = true;
                            z2 = false;
                        }
                        this.mWriteLock.unlock();
                        sepNeeded = sepNeeded4;
                    } catch (Throwable th25) {
                        th2 = th25;
                        this.mWriteLock.unlock();
                        throw th2;
                    }
                } else {
                    sepNeeded = false;
                    z = true;
                    z2 = false;
                }
                if (!isCheckin) {
                    synchronized (this.mAm) {
                        try {
                            ActivityManagerService.boostPriorityForLockedSection();
                            if (isCompact) {
                                try {
                                    this.mProcessStats.dumpCheckinLocked(pw, reqPackage5, section6);
                                    section2 = section6;
                                    reqPackage = reqPackage5;
                                    sepNeeded2 = sepNeeded;
                                } catch (Throwable th26) {
                                    th = th26;
                                    while (true) {
                                        try {
                                            break;
                                        } catch (Throwable th27) {
                                            th = th27;
                                        }
                                    }
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            } else {
                                if (sepNeeded) {
                                    pw.println();
                                }
                                pw.println("CURRENT STATS:");
                                if (csvSepScreenStats3 || activeOnly) {
                                    section2 = section6;
                                    reqPackage = reqPackage5;
                                    try {
                                        this.mProcessStats.dumpLocked(pw, reqPackage5, now, !activeOnly ? z : z2, csvSepScreenStats3, dumpAll, csvSepScreenStats2, section2);
                                        if (dumpAll) {
                                            try {
                                                pw.print("  mFile=");
                                                pw.println(this.mFile.getBaseFile());
                                            } catch (Throwable th28) {
                                                th = th28;
                                            }
                                        }
                                    } catch (Throwable th29) {
                                        th = th29;
                                        while (true) {
                                            break;
                                        }
                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                        throw th;
                                    }
                                } else {
                                    this.mProcessStats.dumpSummaryLocked(pw, reqPackage5, now, csvSepScreenStats2);
                                    section2 = section6;
                                    reqPackage = reqPackage5;
                                }
                                sepNeeded2 = true;
                            }
                            try {
                            } catch (Throwable th30) {
                                th = th30;
                                while (true) {
                                    break;
                                }
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        } catch (Throwable th31) {
                            th = th31;
                            while (true) {
                                break;
                            }
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    if (!currentOnly) {
                        if (sepNeeded2) {
                            pw.println();
                        }
                        pw.println("AGGREGATED OVER LAST 24 HOURS:");
                        dumpAggregatedStats(pw, 24, now, reqPackage, isCompact, csvSepScreenStats3, activeOnly, dumpAll, csvSepScreenStats2, section2);
                        pw.println();
                        pw.println("AGGREGATED OVER LAST 3 HOURS:");
                        dumpAggregatedStats(pw, 3, now, reqPackage, isCompact, csvSepScreenStats3, activeOnly, dumpAll, csvSepScreenStats2, section2);
                    }
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
                long token = proto.start(fieldId);
                stats.writeToProto(proto, now, 15);
                proto.end(token);
            }
        }
    }

    /* JADX INFO: finally extract failed */
    private void dumpProto(FileDescriptor fd) {
        long now;
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        synchronized (this.mAm) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                now = SystemClock.uptimeMillis();
                long token = proto.start(1146756268033L);
                this.mProcessStats.writeToProto(proto, now, 15);
                proto.end(token);
            } catch (Throwable th) {
                ActivityManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        dumpAggregatedStats(proto, 1146756268034L, 3, now);
        dumpAggregatedStats(proto, 1146756268035L, 24, now);
        proto.flush();
    }
}

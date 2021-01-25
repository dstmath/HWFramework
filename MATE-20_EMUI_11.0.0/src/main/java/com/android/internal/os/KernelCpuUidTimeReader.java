package com.android.internal.os;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.IntArray;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.os.KernelCpuProcStringReader;
import com.android.internal.util.Preconditions;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class KernelCpuUidTimeReader<T> {
    protected static final boolean DEBUG = false;
    private static final long DEFAULT_MIN_TIME_BETWEEN_READ = 1000;
    private long mLastReadTimeMs = 0;
    final SparseArray<T> mLastTimes = new SparseArray<>();
    private long mMinTimeBetweenRead = 1000;
    final KernelCpuProcStringReader mReader;
    final String mTag = getClass().getSimpleName();
    final boolean mThrottle;

    public interface Callback<T> {
        void onUidCpuTime(int i, T t);
    }

    /* access modifiers changed from: package-private */
    public abstract void readAbsoluteImpl(Callback<T> callback);

    /* access modifiers changed from: package-private */
    public abstract void readDeltaImpl(Callback<T> callback);

    KernelCpuUidTimeReader(KernelCpuProcStringReader reader, boolean throttle) {
        this.mReader = reader;
        this.mThrottle = throttle;
    }

    public void readDelta(Callback<T> cb) {
        if (!this.mThrottle) {
            readDeltaImpl(cb);
            return;
        }
        long currTimeMs = SystemClock.elapsedRealtime();
        if (currTimeMs >= this.mLastReadTimeMs + this.mMinTimeBetweenRead) {
            readDeltaImpl(cb);
            this.mLastReadTimeMs = currTimeMs;
        }
    }

    public void readAbsolute(Callback<T> cb) {
        if (!this.mThrottle) {
            readAbsoluteImpl(cb);
            return;
        }
        long currTimeMs = SystemClock.elapsedRealtime();
        if (currTimeMs >= this.mLastReadTimeMs + this.mMinTimeBetweenRead) {
            readAbsoluteImpl(cb);
            this.mLastReadTimeMs = currTimeMs;
        }
    }

    public void removeUid(int uid) {
        this.mLastTimes.delete(uid);
    }

    public void removeUidsInRange(int startUid, int endUid) {
        if (endUid < startUid) {
            String str = this.mTag;
            Slog.e(str, "start UID " + startUid + " > end UID " + endUid);
            return;
        }
        this.mLastTimes.put(startUid, null);
        this.mLastTimes.put(endUid, null);
        int firstIndex = this.mLastTimes.indexOfKey(startUid);
        this.mLastTimes.removeAtRange(firstIndex, (this.mLastTimes.indexOfKey(endUid) - firstIndex) + 1);
    }

    public void setThrottle(long minTimeBetweenRead) {
        if (this.mThrottle && minTimeBetweenRead >= 0) {
            this.mMinTimeBetweenRead = minTimeBetweenRead;
        }
    }

    public static class KernelCpuUidUserSysTimeReader extends KernelCpuUidTimeReader<long[]> {
        private static final String REMOVE_UID_PROC_FILE = "/proc/uid_cputime/remove_uid_range";
        private final long[] mBuffer;
        private final long[] mUsrSysTime;

        public KernelCpuUidUserSysTimeReader(boolean throttle) {
            super(KernelCpuProcStringReader.getUserSysTimeReaderInstance(), throttle);
            this.mBuffer = new long[4];
            this.mUsrSysTime = new long[2];
        }

        @VisibleForTesting
        public KernelCpuUidUserSysTimeReader(KernelCpuProcStringReader reader, boolean throttle) {
            super(reader, throttle);
            this.mBuffer = new long[4];
            this.mUsrSysTime = new long[2];
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:36:0x00f3, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x00f4, code lost:
            $closeResource(r0, r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:38:0x00f8, code lost:
            throw r0;
         */
        @Override // com.android.internal.os.KernelCpuUidTimeReader
        public void readDeltaImpl(Callback<long[]> cb) {
            char c = 1;
            KernelCpuProcStringReader.ProcFileIterator iter = this.mReader.open(!this.mThrottle);
            if (iter != null) {
                while (true) {
                    CharBuffer buf = iter.nextLine();
                    if (buf == null) {
                        $closeResource(null, iter);
                        return;
                    } else if (KernelCpuProcStringReader.asLongs(buf, this.mBuffer) < 3) {
                        Slog.wtf(this.mTag, "Invalid line: " + buf.toString());
                    } else {
                        int uid = (int) this.mBuffer[0];
                        long[] lastTimes = (long[]) this.mLastTimes.get(uid);
                        if (lastTimes == null) {
                            lastTimes = new long[2];
                            this.mLastTimes.put(uid, lastTimes);
                        }
                        long currUsrTimeUs = this.mBuffer[c];
                        long currSysTimeUs = this.mBuffer[2];
                        this.mUsrSysTime[0] = currUsrTimeUs - lastTimes[0];
                        this.mUsrSysTime[c] = currSysTimeUs - lastTimes[c];
                        if (this.mUsrSysTime[0] >= 0) {
                            if (this.mUsrSysTime[c] >= 0) {
                                if (this.mUsrSysTime[0] <= 0) {
                                    if (this.mUsrSysTime[c] <= 0) {
                                        lastTimes[0] = currUsrTimeUs;
                                        lastTimes[1] = currSysTimeUs;
                                        c = 1;
                                    }
                                }
                                if (cb != null) {
                                    cb.onUidCpuTime(uid, this.mUsrSysTime);
                                }
                                lastTimes[0] = currUsrTimeUs;
                                lastTimes[1] = currSysTimeUs;
                                c = 1;
                            }
                        }
                        Slog.e(this.mTag, "Negative user/sys time delta for UID=" + uid + "\nPrev times: u=" + lastTimes[0] + " s=" + lastTimes[1] + " Curr times: u=" + currUsrTimeUs + " s=" + currSysTimeUs);
                        lastTimes[0] = currUsrTimeUs;
                        lastTimes[1] = currSysTimeUs;
                        c = 1;
                    }
                }
            } else if (iter != null) {
                $closeResource(null, iter);
            }
        }

        private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
            if (x0 != null) {
                try {
                    x1.close();
                } catch (Throwable th) {
                    x0.addSuppressed(th);
                }
            } else {
                x1.close();
            }
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0061, code lost:
            r2 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0062, code lost:
            $closeResource(r1, r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x0065, code lost:
            throw r2;
         */
        @Override // com.android.internal.os.KernelCpuUidTimeReader
        public void readAbsoluteImpl(Callback<long[]> cb) {
            KernelCpuProcStringReader.ProcFileIterator iter = this.mReader.open(!this.mThrottle);
            if (iter != null) {
                while (true) {
                    CharBuffer buf = iter.nextLine();
                    if (buf == null) {
                        $closeResource(null, iter);
                        return;
                    } else if (KernelCpuProcStringReader.asLongs(buf, this.mBuffer) < 3) {
                        String str = this.mTag;
                        Slog.wtf(str, "Invalid line: " + buf.toString());
                    } else {
                        this.mUsrSysTime[0] = this.mBuffer[1];
                        this.mUsrSysTime[1] = this.mBuffer[2];
                        cb.onUidCpuTime((int) this.mBuffer[0], this.mUsrSysTime);
                    }
                }
            } else if (iter != null) {
                $closeResource(null, iter);
            }
        }

        @Override // com.android.internal.os.KernelCpuUidTimeReader
        public void removeUid(int uid) {
            KernelCpuUidTimeReader.super.removeUid(uid);
            removeUidsFromKernelModule(uid, uid);
        }

        @Override // com.android.internal.os.KernelCpuUidTimeReader
        public void removeUidsInRange(int startUid, int endUid) {
            KernelCpuUidTimeReader.super.removeUidsInRange(startUid, endUid);
            removeUidsFromKernelModule(startUid, endUid);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x0048, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0049, code lost:
            $closeResource(r2, r1);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x004c, code lost:
            throw r3;
         */
        private void removeUidsFromKernelModule(int startUid, int endUid) {
            String str = this.mTag;
            Slog.d(str, "Removing uids " + startUid + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + endUid);
            int oldMask = StrictMode.allowThreadDiskWritesMask();
            try {
                FileWriter writer = new FileWriter(REMOVE_UID_PROC_FILE);
                writer.write(startUid + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + endUid);
                writer.flush();
                $closeResource(null, writer);
            } catch (IOException e) {
                String str2 = this.mTag;
                Slog.e(str2, "failed to remove uids " + startUid + " - " + endUid + " from uid_cputime module", e);
            } catch (Throwable th) {
                StrictMode.setThreadPolicyMask(oldMask);
                throw th;
            }
            StrictMode.setThreadPolicyMask(oldMask);
        }
    }

    public static class KernelCpuUidFreqTimeReader extends KernelCpuUidTimeReader<long[]> {
        private static final int MAX_ERROR_COUNT = 5;
        private static final String UID_TIMES_PROC_FILE = "/proc/uid_time_in_state";
        private boolean mAllUidTimesAvailable;
        private long[] mBuffer;
        private long[] mCpuFreqs;
        private long[] mCurTimes;
        private long[] mDeltaTimes;
        private int mErrors;
        private int mFreqCount;
        private boolean mPerClusterTimesAvailable;
        private final Path mProcFilePath;

        public KernelCpuUidFreqTimeReader(boolean throttle) {
            this(UID_TIMES_PROC_FILE, KernelCpuProcStringReader.getFreqTimeReaderInstance(), throttle);
        }

        @VisibleForTesting
        public KernelCpuUidFreqTimeReader(String procFile, KernelCpuProcStringReader reader, boolean throttle) {
            super(reader, throttle);
            this.mFreqCount = 0;
            this.mErrors = 0;
            this.mAllUidTimesAvailable = true;
            this.mProcFilePath = Paths.get(procFile, new String[0]);
        }

        public boolean perClusterTimesAvailable() {
            return this.mPerClusterTimesAvailable;
        }

        public boolean allUidTimesAvailable() {
            return this.mAllUidTimesAvailable;
        }

        public SparseArray<long[]> getAllUidCpuFreqTimeMs() {
            return this.mLastTimes;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:28:0x0077, code lost:
            r6 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:29:0x0078, code lost:
            if (r4 != null) goto L_0x007a;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:30:0x007a, code lost:
            $closeResource(r5, r4);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:31:0x007d, code lost:
            throw r6;
         */
        public long[] readFreqs(PowerProfile powerProfile) {
            Preconditions.checkNotNull(powerProfile);
            long[] jArr = this.mCpuFreqs;
            if (jArr != null) {
                return jArr;
            }
            if (!this.mAllUidTimesAvailable) {
                return null;
            }
            int oldMask = StrictMode.allowThreadDiskReadsMask();
            try {
                BufferedReader reader = Files.newBufferedReader(this.mProcFilePath);
                if (readFreqs(reader.readLine()) == null) {
                    $closeResource(null, reader);
                    return null;
                }
                $closeResource(null, reader);
                StrictMode.setThreadPolicyMask(oldMask);
                IntArray numClusterFreqs = extractClusterInfoFromProcFileFreqs();
                int numClusters = powerProfile.getNumCpuClusters();
                if (numClusterFreqs.size() == numClusters) {
                    this.mPerClusterTimesAvailable = true;
                    int i = 0;
                    while (true) {
                        if (i >= numClusters) {
                            break;
                        } else if (numClusterFreqs.get(i) != powerProfile.getNumSpeedStepsInCpuCluster(i)) {
                            this.mPerClusterTimesAvailable = false;
                            break;
                        } else {
                            i++;
                        }
                    }
                } else {
                    this.mPerClusterTimesAvailable = false;
                }
                String str = this.mTag;
                Slog.i(str, "mPerClusterTimesAvailable=" + this.mPerClusterTimesAvailable);
                return this.mCpuFreqs;
            } catch (IOException e) {
                int i2 = this.mErrors + 1;
                this.mErrors = i2;
                if (i2 >= 5) {
                    this.mAllUidTimesAvailable = false;
                }
                String str2 = this.mTag;
                Slog.e(str2, "Failed to read /proc/uid_time_in_state: " + e);
                return null;
            } finally {
                StrictMode.setThreadPolicyMask(oldMask);
            }
        }

        private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
            if (x0 != null) {
                try {
                    x1.close();
                } catch (Throwable th) {
                    x0.addSuppressed(th);
                }
            } else {
                x1.close();
            }
        }

        private long[] readFreqs(String line) {
            if (line == null) {
                return null;
            }
            String[] lineArray = line.split(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            if (lineArray.length <= 1) {
                String str = this.mTag;
                Slog.wtf(str, "Malformed freq line: " + line);
                return null;
            }
            this.mFreqCount = lineArray.length - 1;
            int i = this.mFreqCount;
            this.mCpuFreqs = new long[i];
            this.mCurTimes = new long[i];
            this.mDeltaTimes = new long[i];
            this.mBuffer = new long[(i + 1)];
            for (int i2 = 0; i2 < this.mFreqCount; i2++) {
                this.mCpuFreqs[i2] = Long.parseLong(lineArray[i2 + 1], 10);
            }
            return this.mCpuFreqs;
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:40:0x00d9, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:0x00db, code lost:
            if (r3 != null) goto L_0x00dd;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:43:0x00dd, code lost:
            $closeResource(r0, r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:44:0x00e0, code lost:
            throw r0;
         */
        @Override // com.android.internal.os.KernelCpuUidTimeReader
        public void readDeltaImpl(Callback<long[]> cb) {
            long[] lastTimes;
            KernelCpuProcStringReader.ProcFileIterator iter = this.mReader.open(!this.mThrottle);
            if (checkPrecondition(iter)) {
                while (true) {
                    CharBuffer buf = iter.nextLine();
                    if (buf == null) {
                        $closeResource(null, iter);
                        return;
                    } else if (KernelCpuProcStringReader.asLongs(buf, this.mBuffer) != this.mBuffer.length) {
                        Slog.wtf(this.mTag, "Invalid line: " + buf.toString());
                    } else {
                        int uid = (int) this.mBuffer[0];
                        long[] lastTimes2 = (long[]) this.mLastTimes.get(uid);
                        if (lastTimes2 == null) {
                            lastTimes2 = new long[this.mFreqCount];
                            this.mLastTimes.put(uid, lastTimes2);
                        }
                        copyToCurTimes();
                        boolean notify = false;
                        boolean valid = true;
                        int i = 0;
                        while (i < this.mFreqCount) {
                            this.mDeltaTimes[i] = this.mCurTimes[i] - lastTimes2[i];
                            if (this.mDeltaTimes[i] < 0) {
                                String str = this.mTag;
                                StringBuilder sb = new StringBuilder();
                                sb.append("Negative delta from freq time proc: ");
                                lastTimes = lastTimes2;
                                sb.append(this.mDeltaTimes[i]);
                                Slog.e(str, sb.toString());
                                valid = false;
                            } else {
                                lastTimes = lastTimes2;
                            }
                            notify |= this.mDeltaTimes[i] > 0;
                            i++;
                            lastTimes2 = lastTimes;
                        }
                        if (notify && valid) {
                            System.arraycopy(this.mCurTimes, 0, lastTimes2, 0, this.mFreqCount);
                            if (cb != null) {
                                cb.onUidCpuTime(uid, this.mDeltaTimes);
                            }
                        }
                    }
                }
            } else if (iter != null) {
                $closeResource(null, iter);
            }
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0059, code lost:
            r2 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x005a, code lost:
            if (r0 != null) goto L_0x005c;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x005c, code lost:
            $closeResource(r1, r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x005f, code lost:
            throw r2;
         */
        @Override // com.android.internal.os.KernelCpuUidTimeReader
        public void readAbsoluteImpl(Callback<long[]> cb) {
            KernelCpuProcStringReader.ProcFileIterator iter = this.mReader.open(!this.mThrottle);
            if (checkPrecondition(iter)) {
                while (true) {
                    CharBuffer buf = iter.nextLine();
                    if (buf == null) {
                        $closeResource(null, iter);
                        return;
                    } else if (KernelCpuProcStringReader.asLongs(buf, this.mBuffer) != this.mBuffer.length) {
                        String str = this.mTag;
                        Slog.wtf(str, "Invalid line: " + buf.toString());
                    } else {
                        copyToCurTimes();
                        cb.onUidCpuTime((int) this.mBuffer[0], this.mCurTimes);
                    }
                }
            } else if (iter != null) {
                $closeResource(null, iter);
            }
        }

        private void copyToCurTimes() {
            for (int i = 0; i < this.mFreqCount; i++) {
                this.mCurTimes[i] = this.mBuffer[i + 1] * 10;
            }
        }

        private boolean checkPrecondition(KernelCpuProcStringReader.ProcFileIterator iter) {
            if (iter == null || !iter.hasNextLine()) {
                return false;
            }
            CharBuffer line = iter.nextLine();
            if (this.mCpuFreqs != null) {
                return true;
            }
            if (readFreqs(line.toString()) != null) {
                return true;
            }
            return false;
        }

        private IntArray extractClusterInfoFromProcFileFreqs() {
            IntArray numClusterFreqs = new IntArray();
            int freqsFound = 0;
            int i = 0;
            while (true) {
                int i2 = this.mFreqCount;
                if (i >= i2) {
                    return numClusterFreqs;
                }
                freqsFound++;
                if (i + 1 != i2) {
                    long[] jArr = this.mCpuFreqs;
                    if (jArr[i + 1] > jArr[i]) {
                        i++;
                    }
                }
                numClusterFreqs.add(freqsFound);
                freqsFound = 0;
                i++;
            }
        }
    }

    public static class KernelCpuUidActiveTimeReader extends KernelCpuUidTimeReader<Long> {
        private long[] mBuffer;
        private int mCores = 0;

        public KernelCpuUidActiveTimeReader(boolean throttle) {
            super(KernelCpuProcStringReader.getActiveTimeReaderInstance(), throttle);
        }

        @VisibleForTesting
        public KernelCpuUidActiveTimeReader(KernelCpuProcStringReader reader, boolean throttle) {
            super(reader, throttle);
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x00a1, code lost:
            r2 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:27:0x00a2, code lost:
            if (r0 != null) goto L_0x00a4;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:28:0x00a4, code lost:
            $closeResource(r1, r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:29:0x00a7, code lost:
            throw r2;
         */
        @Override // com.android.internal.os.KernelCpuUidTimeReader
        public void readDeltaImpl(Callback<Long> cb) {
            KernelCpuProcStringReader.ProcFileIterator iter = this.mReader.open(!this.mThrottle);
            if (checkPrecondition(iter)) {
                while (true) {
                    CharBuffer buf = iter.nextLine();
                    if (buf == null) {
                        $closeResource(null, iter);
                        return;
                    } else if (KernelCpuProcStringReader.asLongs(buf, this.mBuffer) != this.mBuffer.length) {
                        String str = this.mTag;
                        Slog.wtf(str, "Invalid line: " + buf.toString());
                    } else {
                        int uid = (int) this.mBuffer[0];
                        long cpuActiveTime = sumActiveTime(this.mBuffer);
                        if (cpuActiveTime > 0) {
                            long delta = cpuActiveTime - ((Long) this.mLastTimes.get(uid, 0L)).longValue();
                            if (delta > 0) {
                                this.mLastTimes.put(uid, Long.valueOf(cpuActiveTime));
                                if (cb != null) {
                                    cb.onUidCpuTime(uid, Long.valueOf(delta));
                                }
                            } else if (delta < 0) {
                                String str2 = this.mTag;
                                Slog.e(str2, "Negative delta from active time proc: " + delta);
                            }
                        }
                    }
                }
            } else if (iter != null) {
                $closeResource(null, iter);
            }
        }

        private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
            if (x0 != null) {
                try {
                    x1.close();
                } catch (Throwable th) {
                    x0.addSuppressed(th);
                }
            } else {
                x1.close();
            }
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x0064, code lost:
            r2 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0065, code lost:
            if (r0 != null) goto L_0x0067;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0067, code lost:
            $closeResource(r1, r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x006a, code lost:
            throw r2;
         */
        @Override // com.android.internal.os.KernelCpuUidTimeReader
        public void readAbsoluteImpl(Callback<Long> cb) {
            KernelCpuProcStringReader.ProcFileIterator iter = this.mReader.open(!this.mThrottle);
            if (checkPrecondition(iter)) {
                while (true) {
                    CharBuffer buf = iter.nextLine();
                    if (buf == null) {
                        $closeResource(null, iter);
                        return;
                    } else if (KernelCpuProcStringReader.asLongs(buf, this.mBuffer) != this.mBuffer.length) {
                        String str = this.mTag;
                        Slog.wtf(str, "Invalid line: " + buf.toString());
                    } else {
                        long cpuActiveTime = sumActiveTime(this.mBuffer);
                        if (cpuActiveTime > 0) {
                            cb.onUidCpuTime((int) this.mBuffer[0], Long.valueOf(cpuActiveTime));
                        }
                    }
                }
            } else if (iter != null) {
                $closeResource(null, iter);
            }
        }

        private static long sumActiveTime(long[] times) {
            double sum = 0.0d;
            for (int i = 1; i < times.length; i++) {
                sum += (((double) times[i]) * 10.0d) / ((double) i);
            }
            return (long) sum;
        }

        private boolean checkPrecondition(KernelCpuProcStringReader.ProcFileIterator iter) {
            if (iter == null || !iter.hasNextLine()) {
                return false;
            }
            CharBuffer line = iter.nextLine();
            if (this.mCores > 0) {
                return true;
            }
            String str = line.toString();
            if (!str.startsWith("cpus:")) {
                String str2 = this.mTag;
                Slog.wtf(str2, "Malformed uid_concurrent_active_time line: " + ((Object) line));
                return false;
            }
            int cores = Integer.parseInt(str.substring(5).trim(), 10);
            if (cores <= 0) {
                String str3 = this.mTag;
                Slog.wtf(str3, "Malformed uid_concurrent_active_time line: " + ((Object) line));
                return false;
            }
            this.mCores = cores;
            this.mBuffer = new long[(this.mCores + 1)];
            return true;
        }
    }

    public static class KernelCpuUidClusterTimeReader extends KernelCpuUidTimeReader<long[]> {
        private long[] mBuffer;
        private int[] mCoresOnClusters;
        private long[] mCurTime;
        private long[] mDeltaTime;
        private int mNumClusters;
        private int mNumCores;

        public KernelCpuUidClusterTimeReader(boolean throttle) {
            super(KernelCpuProcStringReader.getClusterTimeReaderInstance(), throttle);
        }

        @VisibleForTesting
        public KernelCpuUidClusterTimeReader(KernelCpuProcStringReader reader, boolean throttle) {
            super(reader, throttle);
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:40:0x00d9, code lost:
            r0 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:0x00db, code lost:
            if (r3 != null) goto L_0x00dd;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:43:0x00dd, code lost:
            $closeResource(r0, r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:44:0x00e0, code lost:
            throw r0;
         */
        @Override // com.android.internal.os.KernelCpuUidTimeReader
        public void readDeltaImpl(Callback<long[]> cb) {
            long[] lastTimes;
            KernelCpuProcStringReader.ProcFileIterator iter = this.mReader.open(!this.mThrottle);
            if (checkPrecondition(iter)) {
                while (true) {
                    CharBuffer buf = iter.nextLine();
                    if (buf == null) {
                        $closeResource(null, iter);
                        return;
                    } else if (KernelCpuProcStringReader.asLongs(buf, this.mBuffer) != this.mBuffer.length) {
                        Slog.wtf(this.mTag, "Invalid line: " + buf.toString());
                    } else {
                        int uid = (int) this.mBuffer[0];
                        long[] lastTimes2 = (long[]) this.mLastTimes.get(uid);
                        if (lastTimes2 == null) {
                            lastTimes2 = new long[this.mNumClusters];
                            this.mLastTimes.put(uid, lastTimes2);
                        }
                        sumClusterTime();
                        boolean valid = true;
                        boolean notify = false;
                        int i = 0;
                        while (i < this.mNumClusters) {
                            this.mDeltaTime[i] = this.mCurTime[i] - lastTimes2[i];
                            if (this.mDeltaTime[i] < 0) {
                                String str = this.mTag;
                                StringBuilder sb = new StringBuilder();
                                sb.append("Negative delta from cluster time proc: ");
                                lastTimes = lastTimes2;
                                sb.append(this.mDeltaTime[i]);
                                Slog.e(str, sb.toString());
                                valid = false;
                            } else {
                                lastTimes = lastTimes2;
                            }
                            notify |= this.mDeltaTime[i] > 0;
                            i++;
                            lastTimes2 = lastTimes;
                        }
                        if (notify && valid) {
                            System.arraycopy(this.mCurTime, 0, lastTimes2, 0, this.mNumClusters);
                            if (cb != null) {
                                cb.onUidCpuTime(uid, this.mDeltaTime);
                            }
                        }
                    }
                }
            } else if (iter != null) {
                $closeResource(null, iter);
            }
        }

        private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
            if (x0 != null) {
                try {
                    x1.close();
                } catch (Throwable th) {
                    x0.addSuppressed(th);
                }
            } else {
                x1.close();
            }
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0059, code lost:
            r2 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x005a, code lost:
            if (r0 != null) goto L_0x005c;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x005c, code lost:
            $closeResource(r1, r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x005f, code lost:
            throw r2;
         */
        @Override // com.android.internal.os.KernelCpuUidTimeReader
        public void readAbsoluteImpl(Callback<long[]> cb) {
            KernelCpuProcStringReader.ProcFileIterator iter = this.mReader.open(!this.mThrottle);
            if (checkPrecondition(iter)) {
                while (true) {
                    CharBuffer buf = iter.nextLine();
                    if (buf == null) {
                        $closeResource(null, iter);
                        return;
                    } else if (KernelCpuProcStringReader.asLongs(buf, this.mBuffer) != this.mBuffer.length) {
                        String str = this.mTag;
                        Slog.wtf(str, "Invalid line: " + buf.toString());
                    } else {
                        sumClusterTime();
                        cb.onUidCpuTime((int) this.mBuffer[0], this.mCurTime);
                    }
                }
            } else if (iter != null) {
                $closeResource(null, iter);
            }
        }

        private void sumClusterTime() {
            int core = 1;
            for (int i = 0; i < this.mNumClusters; i++) {
                double sum = 0.0d;
                int j = 1;
                while (j <= this.mCoresOnClusters[i]) {
                    sum += (((double) this.mBuffer[core]) * 10.0d) / ((double) j);
                    j++;
                    core++;
                }
                this.mCurTime[i] = (long) sum;
            }
        }

        private boolean checkPrecondition(KernelCpuProcStringReader.ProcFileIterator iter) {
            if (iter == null || !iter.hasNextLine()) {
                return false;
            }
            CharBuffer line = iter.nextLine();
            if (this.mNumClusters > 0) {
                return true;
            }
            String[] lineArray = line.toString().split(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            if (lineArray.length % 2 != 0) {
                String str = this.mTag;
                Slog.wtf(str, "Malformed uid_concurrent_policy_time line: " + ((Object) line));
                return false;
            }
            int[] clusters = new int[(lineArray.length / 2)];
            int cores = 0;
            for (int i = 0; i < clusters.length; i++) {
                if (!lineArray[i * 2].startsWith("policy")) {
                    String str2 = this.mTag;
                    Slog.wtf(str2, "Malformed uid_concurrent_policy_time line: " + ((Object) line));
                    return false;
                }
                clusters[i] = Integer.parseInt(lineArray[(i * 2) + 1], 10);
                cores += clusters[i];
            }
            this.mNumClusters = clusters.length;
            this.mNumCores = cores;
            this.mCoresOnClusters = clusters;
            this.mBuffer = new long[(cores + 1)];
            int i2 = this.mNumClusters;
            this.mCurTime = new long[i2];
            this.mDeltaTime = new long[i2];
            return true;
        }
    }
}

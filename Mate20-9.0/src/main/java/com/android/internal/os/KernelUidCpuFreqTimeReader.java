package com.android.internal.os;

import android.os.StrictMode;
import android.util.IntArray;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.KernelUidCpuFreqTimeReader;
import com.android.internal.os.KernelUidCpuTimeReaderBase;
import com.android.internal.util.Preconditions;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.Consumer;

public class KernelUidCpuFreqTimeReader extends KernelUidCpuTimeReaderBase<Callback> {
    private static final String TAG = KernelUidCpuFreqTimeReader.class.getSimpleName();
    private static final int TOTAL_READ_ERROR_COUNT = 5;
    static final String UID_TIMES_PROC_FILE = "/proc/uid_time_in_state";
    private boolean mAllUidTimesAvailable;
    private long[] mCpuFreqs;
    private int mCpuFreqsCount;
    private long[] mCurTimes;
    private long[] mDeltaTimes;
    private SparseArray<long[]> mLastUidCpuFreqTimeMs;
    private boolean mPerClusterTimesAvailable;
    private final KernelCpuProcReader mProcReader;
    private int mReadErrorCounter;

    public interface Callback extends KernelUidCpuTimeReaderBase.Callback {
        void onUidCpuFreqTime(int i, long[] jArr);
    }

    public KernelUidCpuFreqTimeReader() {
        this.mLastUidCpuFreqTimeMs = new SparseArray<>();
        this.mAllUidTimesAvailable = true;
        this.mProcReader = KernelCpuProcReader.getFreqTimeReaderInstance();
    }

    @VisibleForTesting
    public KernelUidCpuFreqTimeReader(KernelCpuProcReader procReader) {
        this.mLastUidCpuFreqTimeMs = new SparseArray<>();
        this.mAllUidTimesAvailable = true;
        this.mProcReader = procReader;
    }

    public boolean perClusterTimesAvailable() {
        return this.mPerClusterTimesAvailable;
    }

    public boolean allUidTimesAvailable() {
        return this.mAllUidTimesAvailable;
    }

    public SparseArray<long[]> getAllUidCpuFreqTimeMs() {
        return this.mLastUidCpuFreqTimeMs;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002b, code lost:
        r3 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002c, code lost:
        r4 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0030, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0031, code lost:
        r6 = r4;
        r4 = r3;
        r3 = r6;
     */
    public long[] readFreqs(PowerProfile powerProfile) {
        BufferedReader reader;
        Throwable th;
        Throwable th2;
        Preconditions.checkNotNull(powerProfile);
        if (this.mCpuFreqs != null) {
            return this.mCpuFreqs;
        }
        if (!this.mAllUidTimesAvailable) {
            return null;
        }
        int oldMask = StrictMode.allowThreadDiskReadsMask();
        try {
            reader = new BufferedReader(new FileReader(UID_TIMES_PROC_FILE));
            long[] readFreqs = readFreqs(reader, powerProfile);
            reader.close();
            return readFreqs;
        } catch (IOException e) {
            int i = this.mReadErrorCounter + 1;
            this.mReadErrorCounter = i;
            if (i >= 5) {
                this.mAllUidTimesAvailable = false;
            }
            Slog.e(TAG, "Failed to read /proc/uid_time_in_state: " + e);
            return null;
        } finally {
            StrictMode.setThreadPolicyMask(oldMask);
        }
        throw th2;
        if (th != null) {
            try {
                reader.close();
            } catch (Throwable th3) {
                th.addSuppressed(th3);
            }
        } else {
            reader.close();
        }
        throw th2;
    }

    @VisibleForTesting
    public long[] readFreqs(BufferedReader reader, PowerProfile powerProfile) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return null;
        }
        String[] freqStr = line.split(" ");
        this.mCpuFreqsCount = freqStr.length - 1;
        this.mCpuFreqs = new long[this.mCpuFreqsCount];
        this.mCurTimes = new long[this.mCpuFreqsCount];
        this.mDeltaTimes = new long[this.mCpuFreqsCount];
        for (int i = 0; i < this.mCpuFreqsCount; i++) {
            this.mCpuFreqs[i] = Long.parseLong(freqStr[i + 1], 10);
        }
        IntArray numClusterFreqs = extractClusterInfoFromProcFileFreqs();
        int numClusters = powerProfile.getNumCpuClusters();
        if (numClusterFreqs.size() == numClusters) {
            this.mPerClusterTimesAvailable = true;
            int i2 = 0;
            while (true) {
                if (i2 >= numClusters) {
                    break;
                } else if (numClusterFreqs.get(i2) != powerProfile.getNumSpeedStepsInCpuCluster(i2)) {
                    this.mPerClusterTimesAvailable = false;
                    break;
                } else {
                    i2++;
                }
            }
        } else {
            this.mPerClusterTimesAvailable = false;
        }
        Slog.i(TAG, "mPerClusterTimesAvailable=" + this.mPerClusterTimesAvailable);
        return this.mCpuFreqs;
    }

    @VisibleForTesting
    public void readDeltaImpl(Callback callback) {
        if (this.mCpuFreqs != null) {
            readImpl(new Consumer(callback) {
                private final /* synthetic */ KernelUidCpuFreqTimeReader.Callback f$1;

                {
                    this.f$1 = r2;
                }

                public final void accept(Object obj) {
                    KernelUidCpuFreqTimeReader.lambda$readDeltaImpl$0(KernelUidCpuFreqTimeReader.this, this.f$1, (IntBuffer) obj);
                }
            });
        }
    }

    public static /* synthetic */ void lambda$readDeltaImpl$0(KernelUidCpuFreqTimeReader kernelUidCpuFreqTimeReader, Callback callback, IntBuffer buf) {
        int uid = buf.get();
        long[] lastTimes = kernelUidCpuFreqTimeReader.mLastUidCpuFreqTimeMs.get(uid);
        if (lastTimes == null) {
            lastTimes = new long[kernelUidCpuFreqTimeReader.mCpuFreqsCount];
            kernelUidCpuFreqTimeReader.mLastUidCpuFreqTimeMs.put(uid, lastTimes);
        }
        if (kernelUidCpuFreqTimeReader.getFreqTimeForUid(buf, kernelUidCpuFreqTimeReader.mCurTimes)) {
            boolean valid = true;
            boolean notify = false;
            for (int i = 0; i < kernelUidCpuFreqTimeReader.mCpuFreqsCount; i++) {
                kernelUidCpuFreqTimeReader.mDeltaTimes[i] = kernelUidCpuFreqTimeReader.mCurTimes[i] - lastTimes[i];
                if (kernelUidCpuFreqTimeReader.mDeltaTimes[i] < 0) {
                    Slog.e(TAG, "Negative delta from freq time proc: " + kernelUidCpuFreqTimeReader.mDeltaTimes[i]);
                    valid = false;
                }
                notify |= kernelUidCpuFreqTimeReader.mDeltaTimes[i] > 0;
            }
            if (notify && valid) {
                System.arraycopy(kernelUidCpuFreqTimeReader.mCurTimes, 0, lastTimes, 0, kernelUidCpuFreqTimeReader.mCpuFreqsCount);
                if (callback != null) {
                    callback.onUidCpuFreqTime(uid, kernelUidCpuFreqTimeReader.mDeltaTimes);
                }
            }
        }
    }

    public void readAbsolute(Callback callback) {
        readImpl(new Consumer(callback) {
            private final /* synthetic */ KernelUidCpuFreqTimeReader.Callback f$1;

            {
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                KernelUidCpuFreqTimeReader.lambda$readAbsolute$1(KernelUidCpuFreqTimeReader.this, this.f$1, (IntBuffer) obj);
            }
        });
    }

    public static /* synthetic */ void lambda$readAbsolute$1(KernelUidCpuFreqTimeReader kernelUidCpuFreqTimeReader, Callback callback, IntBuffer buf) {
        int uid = buf.get();
        if (kernelUidCpuFreqTimeReader.getFreqTimeForUid(buf, kernelUidCpuFreqTimeReader.mCurTimes)) {
            callback.onUidCpuFreqTime(uid, kernelUidCpuFreqTimeReader.mCurTimes);
        }
    }

    private boolean getFreqTimeForUid(IntBuffer buffer, long[] freqTime) {
        boolean valid = true;
        for (int i = 0; i < this.mCpuFreqsCount; i++) {
            freqTime[i] = ((long) buffer.get()) * 10;
            if (freqTime[i] < 0) {
                String str = TAG;
                Slog.e(str, "Negative time from freq time proc: " + freqTime[i]);
                valid = false;
            }
        }
        return valid;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00a8, code lost:
        return;
     */
    private void readImpl(Consumer<IntBuffer> processUid) {
        synchronized (this.mProcReader) {
            ByteBuffer bytes = this.mProcReader.readBytes();
            if (bytes != null) {
                if (bytes.remaining() > 4) {
                    if ((bytes.remaining() & 3) != 0) {
                        String str = TAG;
                        Slog.wtf(str, "Cannot parse freq time proc bytes to int: " + bytes.remaining());
                        return;
                    }
                    IntBuffer buf = bytes.asIntBuffer();
                    int freqs = buf.get();
                    if (freqs != this.mCpuFreqsCount) {
                        String str2 = TAG;
                        Slog.wtf(str2, "Cpu freqs expect " + this.mCpuFreqsCount + " , got " + freqs);
                    } else if (buf.remaining() % (freqs + 1) != 0) {
                        String str3 = TAG;
                        Slog.wtf(str3, "Freq time format error: " + buf.remaining() + " / " + (freqs + 1));
                    } else {
                        int numUids = buf.remaining() / (freqs + 1);
                        for (int i = 0; i < numUids; i++) {
                            processUid.accept(buf);
                        }
                    }
                }
            }
        }
    }

    public void removeUid(int uid) {
        this.mLastUidCpuFreqTimeMs.delete(uid);
    }

    public void removeUidsInRange(int startUid, int endUid) {
        this.mLastUidCpuFreqTimeMs.put(startUid, null);
        this.mLastUidCpuFreqTimeMs.put(endUid, null);
        int firstIndex = this.mLastUidCpuFreqTimeMs.indexOfKey(startUid);
        this.mLastUidCpuFreqTimeMs.removeAtRange(firstIndex, (this.mLastUidCpuFreqTimeMs.indexOfKey(endUid) - firstIndex) + 1);
    }

    private IntArray extractClusterInfoFromProcFileFreqs() {
        IntArray numClusterFreqs = new IntArray();
        int freqsFound = 0;
        for (int i = 0; i < this.mCpuFreqsCount; i++) {
            freqsFound++;
            if (i + 1 == this.mCpuFreqsCount || this.mCpuFreqs[i + 1] <= this.mCpuFreqs[i]) {
                numClusterFreqs.add(freqsFound);
                freqsFound = 0;
            }
        }
        return numClusterFreqs;
    }
}

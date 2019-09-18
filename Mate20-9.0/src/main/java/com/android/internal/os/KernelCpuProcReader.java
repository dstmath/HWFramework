package com.android.internal.os;

import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class KernelCpuProcReader {
    private static final long DEFAULT_THROTTLE_INTERVAL = 3000;
    private static final int ERROR_THRESHOLD = 5;
    private static final int INITIAL_BUFFER_SIZE = 8192;
    private static final int MAX_BUFFER_SIZE = 1048576;
    private static final String PROC_UID_ACTIVE_TIME = "/proc/uid_cpupower/concurrent_active_time";
    private static final String PROC_UID_CLUSTER_TIME = "/proc/uid_cpupower/concurrent_policy_time";
    private static final String PROC_UID_FREQ_TIME = "/proc/uid_cpupower/time_in_state";
    private static final String TAG = "KernelCpuProcReader";
    private static final KernelCpuProcReader mActiveTimeReader = new KernelCpuProcReader(PROC_UID_ACTIVE_TIME);
    private static final KernelCpuProcReader mClusterTimeReader = new KernelCpuProcReader(PROC_UID_CLUSTER_TIME);
    private static final KernelCpuProcReader mFreqTimeReader = new KernelCpuProcReader(PROC_UID_FREQ_TIME);
    private ByteBuffer mBuffer;
    private int mErrors;
    private long mLastReadTime = Long.MIN_VALUE;
    private final Path mProc;
    private long mThrottleInterval = DEFAULT_THROTTLE_INTERVAL;

    public static KernelCpuProcReader getFreqTimeReaderInstance() {
        return mFreqTimeReader;
    }

    public static KernelCpuProcReader getActiveTimeReaderInstance() {
        return mActiveTimeReader;
    }

    public static KernelCpuProcReader getClusterTimeReaderInstance() {
        return mClusterTimeReader;
    }

    @VisibleForTesting
    public KernelCpuProcReader(String procFile) {
        this.mProc = Paths.get(procFile, new String[0]);
        this.mBuffer = ByteBuffer.allocateDirect(8192);
        this.mBuffer.clear();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00b5, code lost:
        r4 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00b6, code lost:
        r5 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00ba, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00bb, code lost:
        r8 = r5;
        r5 = r4;
        r4 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00cf, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00d1, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:?, code lost:
        r9.mErrors++;
        android.util.Slog.e(TAG, "Error reading: " + r9.mProc, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00f3, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:?, code lost:
        r9.mErrors++;
        android.util.Slog.w(TAG, "File not exist: " + r9.mProc);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0116, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0117, code lost:
        android.os.StrictMode.setThreadPolicyMask(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x011a, code lost:
        throw r1;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00f4 A[ExcHandler: FileNotFoundException | NoSuchFileException (e java.lang.Throwable), Splitter:B:13:0x004a] */
    public ByteBuffer readBytes() {
        FileChannel fc;
        Throwable th;
        Throwable th2;
        if (this.mErrors >= 5) {
            return null;
        }
        if (SystemClock.elapsedRealtime() >= this.mLastReadTime + this.mThrottleInterval) {
            this.mLastReadTime = SystemClock.elapsedRealtime();
            this.mBuffer.clear();
            int oldMask = StrictMode.allowThreadDiskReadsMask();
            try {
                fc = FileChannel.open(this.mProc, new OpenOption[]{StandardOpenOption.READ});
                while (fc.read(this.mBuffer) == this.mBuffer.capacity()) {
                    if (!resize()) {
                        this.mErrors++;
                        Slog.e(TAG, "Proc file is too large: " + this.mProc);
                        if (fc != null) {
                            fc.close();
                        }
                        return null;
                    }
                    fc.position(0);
                }
                if (fc != null) {
                    fc.close();
                }
                StrictMode.setThreadPolicyMask(oldMask);
                this.mBuffer.flip();
                return this.mBuffer.asReadOnlyBuffer().order(ByteOrder.nativeOrder());
            } catch (FileNotFoundException | NoSuchFileException e) {
            } catch (Throwable th3) {
                th.addSuppressed(th3);
            }
        } else if (this.mBuffer.limit() <= 0 || this.mBuffer.limit() >= this.mBuffer.capacity()) {
            return null;
        } else {
            return this.mBuffer.asReadOnlyBuffer().order(ByteOrder.nativeOrder());
        }
        if (fc != null) {
            if (th != null) {
                fc.close();
            } else {
                fc.close();
            }
        }
        throw th2;
        throw th2;
    }

    public void setThrottleInterval(long throttleInterval) {
        if (throttleInterval >= 0) {
            this.mThrottleInterval = throttleInterval;
        }
    }

    private boolean resize() {
        if (this.mBuffer.capacity() >= MAX_BUFFER_SIZE) {
            return false;
        }
        this.mBuffer = ByteBuffer.allocateDirect(Math.min(this.mBuffer.capacity() << 1, MAX_BUFFER_SIZE));
        return true;
    }
}

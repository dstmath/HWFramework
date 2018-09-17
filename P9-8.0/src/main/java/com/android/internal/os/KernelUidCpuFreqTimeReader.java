package com.android.internal.os;

import android.util.Slog;
import android.util.SparseArray;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class KernelUidCpuFreqTimeReader {
    private static final String TAG = "KernelUidCpuFreqTimeReader";
    private static final int TOTAL_READ_ERROR_COUNT = 5;
    private static final String UID_TIMES_PROC_FILE = "/proc/uid_time_in_state";
    private long[] mCpuFreqs;
    private int mCpuFreqsCount;
    private SparseArray<long[]> mLastUidCpuFreqTimeMs = new SparseArray();
    private boolean mProcFileAvailable;
    private int mReadErrorCounter;

    public interface Callback {
        void onCpuFreqs(long[] jArr);

        void onUidCpuFreqTime(int i, long[] jArr);
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x0056 A:{SYNTHETIC, Splitter: B:28:0x0056} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0069 A:{Catch:{ IOException -> 0x005c }} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x005b A:{SYNTHETIC, Splitter: B:31:0x005b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void readDelta(Callback callback) {
        IOException e;
        Throwable th;
        Throwable th2 = null;
        if (this.mProcFileAvailable || this.mReadErrorCounter < 5) {
            BufferedReader reader = null;
            try {
                BufferedReader reader2 = new BufferedReader(new FileReader(UID_TIMES_PROC_FILE));
                try {
                    readDelta(reader2, callback);
                    this.mProcFileAvailable = true;
                    if (reader2 != null) {
                        try {
                            reader2.close();
                        } catch (Throwable th3) {
                            th2 = th3;
                        }
                    }
                    if (th2 != null) {
                        try {
                            throw th2;
                        } catch (IOException e2) {
                            e = e2;
                            reader = reader2;
                        }
                    }
                } catch (Throwable th4) {
                    th = th4;
                    reader = reader2;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (Throwable th5) {
                            if (th2 == null) {
                                th2 = th5;
                            } else if (th2 != th5) {
                                th2.addSuppressed(th5);
                            }
                        }
                    }
                    if (th2 == null) {
                        try {
                            throw th2;
                        } catch (IOException e3) {
                            e = e3;
                            this.mReadErrorCounter++;
                            Slog.e(TAG, "Failed to read /proc/uid_time_in_state: " + e);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th6) {
                th = th6;
                if (reader != null) {
                }
                if (th2 == null) {
                }
            }
        }
    }

    public void removeUid(int uid) {
        this.mLastUidCpuFreqTimeMs.delete(uid);
    }

    public void readDelta(BufferedReader reader, Callback callback) throws IOException {
        String line = reader.readLine();
        if (line != null) {
            readCpuFreqs(line, callback);
            while (true) {
                line = reader.readLine();
                if (line != null) {
                    int index = line.indexOf(32);
                    readTimesForUid(Integer.parseInt(line.substring(0, index - 1), 10), line.substring(index + 1, line.length()), callback);
                } else {
                    return;
                }
            }
        }
    }

    private void readTimesForUid(int uid, String line, Callback callback) {
        long[] uidTimeMs = (long[]) this.mLastUidCpuFreqTimeMs.get(uid);
        if (uidTimeMs == null) {
            uidTimeMs = new long[this.mCpuFreqsCount];
            this.mLastUidCpuFreqTimeMs.put(uid, uidTimeMs);
        }
        String[] timesStr = line.split(" ");
        int size = timesStr.length;
        if (size != uidTimeMs.length) {
            Slog.e(TAG, "No. of readings don't match cpu freqs, readings: " + size + " cpuFreqsCount: " + uidTimeMs.length);
            return;
        }
        long[] deltaUidTimeMs = new long[size];
        for (int i = 0; i < size; i++) {
            long totalTimeMs = Long.parseLong(timesStr[i], 10) * 10;
            deltaUidTimeMs[i] = totalTimeMs - uidTimeMs[i];
            uidTimeMs[i] = totalTimeMs;
        }
        if (callback != null) {
            callback.onUidCpuFreqTime(uid, deltaUidTimeMs);
        }
    }

    private void readCpuFreqs(String line, Callback callback) {
        if (this.mCpuFreqs == null) {
            String[] freqStr = line.split(" ");
            this.mCpuFreqsCount = freqStr.length - 1;
            this.mCpuFreqs = new long[this.mCpuFreqsCount];
            for (int i = 0; i < this.mCpuFreqsCount; i++) {
                this.mCpuFreqs[i] = Long.parseLong(freqStr[i + 1], 10);
            }
        }
        if (callback != null) {
            callback.onCpuFreqs(this.mCpuFreqs);
        }
    }
}

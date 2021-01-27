package com.android.internal.os;

import android.os.StrictMode;
import android.system.Os;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.Slog;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class KernelCpuSpeedReader {
    private static final String TAG = "KernelCpuSpeedReader";
    private final long[] mDeltaSpeedTimesMs;
    private final long mJiffyMillis = (1000 / Os.sysconf(OsConstants._SC_CLK_TCK));
    private final long[] mLastSpeedTimesMs;
    private final int mNumSpeedSteps;
    private final String mProcFile;

    public KernelCpuSpeedReader(int cpuNumber, int numSpeedSteps) {
        this.mProcFile = String.format("/sys/devices/system/cpu/cpu%d/cpufreq/stats/time_in_state", Integer.valueOf(cpuNumber));
        this.mNumSpeedSteps = numSpeedSteps;
        this.mLastSpeedTimesMs = new long[numSpeedSteps];
        this.mDeltaSpeedTimesMs = new long[numSpeedSteps];
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x005b, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x005c, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x005f, code lost:
        throw r3;
     */
    public long[] readDelta() {
        String line;
        StrictMode.ThreadPolicy policy = StrictMode.allowThreadDiskReads();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(this.mProcFile));
            TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(' ');
            for (int speedIndex = 0; speedIndex < this.mLastSpeedTimesMs.length && (line = reader.readLine()) != null; speedIndex++) {
                splitter.setString(line);
                splitter.next();
                long time = Long.parseLong(splitter.next()) * this.mJiffyMillis;
                if (time < this.mLastSpeedTimesMs[speedIndex]) {
                    this.mDeltaSpeedTimesMs[speedIndex] = time;
                } else {
                    this.mDeltaSpeedTimesMs[speedIndex] = time - this.mLastSpeedTimesMs[speedIndex];
                }
                this.mLastSpeedTimesMs[speedIndex] = time;
            }
            $closeResource(null, reader);
        } catch (IOException e) {
            Slog.e(TAG, "Failed to read cpu-freq: " + e.getMessage());
            Arrays.fill(this.mDeltaSpeedTimesMs, 0L);
        } catch (Throwable th) {
            StrictMode.setThreadPolicy(policy);
            throw th;
        }
        StrictMode.setThreadPolicy(policy);
        return this.mDeltaSpeedTimesMs;
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

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0045, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0046, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0049, code lost:
        throw r4;
     */
    public long[] readAbsolute() {
        String line;
        StrictMode.ThreadPolicy policy = StrictMode.allowThreadDiskReads();
        long[] speedTimeMs = new long[this.mNumSpeedSteps];
        try {
            BufferedReader reader = new BufferedReader(new FileReader(this.mProcFile));
            TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(' ');
            for (int speedIndex = 0; speedIndex < this.mNumSpeedSteps && (line = reader.readLine()) != null; speedIndex++) {
                splitter.setString(line);
                splitter.next();
                speedTimeMs[speedIndex] = Long.parseLong(splitter.next()) * this.mJiffyMillis;
            }
            $closeResource(null, reader);
        } catch (IOException e) {
            Slog.e(TAG, "Failed to read cpu-freq: " + e.getMessage());
            Arrays.fill(speedTimeMs, 0L);
        } catch (Throwable th) {
            StrictMode.setThreadPolicy(policy);
            throw th;
        }
        StrictMode.setThreadPolicy(policy);
        return speedTimeMs;
    }
}

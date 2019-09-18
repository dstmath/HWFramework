package com.android.internal.os;

import android.common.HwFrameworkFactory;
import android.common.HwFrameworkMonitor;
import android.os.Bundle;
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
    private final int mCpuNumber;
    private final long[] mDeltaSpeedTimesMs;
    private final long mJiffyMillis;
    private final long[] mLastSpeedTimesMs;
    private HwFrameworkMonitor mMonitor = null;
    private final int mNumSpeedSteps;
    private final String mProcFile;

    public KernelCpuSpeedReader(int cpuNumber, int numSpeedSteps) {
        this.mProcFile = String.format("/sys/devices/system/cpu/cpu%d/cpufreq/stats/time_in_state", new Object[]{Integer.valueOf(cpuNumber)});
        this.mNumSpeedSteps = numSpeedSteps;
        this.mCpuNumber = cpuNumber;
        this.mMonitor = HwFrameworkFactory.getHwFrameworkMonitor();
        this.mLastSpeedTimesMs = new long[numSpeedSteps];
        this.mDeltaSpeedTimesMs = new long[numSpeedSteps];
        this.mJiffyMillis = 1000 / Os.sysconf(OsConstants._SC_CLK_TCK);
    }

    public long[] readDelta() {
        BufferedReader reader;
        StrictMode.ThreadPolicy policy = StrictMode.allowThreadDiskReads();
        try {
            reader = new BufferedReader(new FileReader(this.mProcFile));
            TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(' ');
            int speedIndex = 0;
            while (speedIndex < this.mLastSpeedTimesMs.length) {
                String readLine = reader.readLine();
                String line = readLine;
                if (readLine != null) {
                    splitter.setString(line);
                    String cpuState = splitter.next();
                    String cpuTime = splitter.next();
                    try {
                        Long.parseLong(cpuState);
                        long time = Long.parseLong(cpuTime) * this.mJiffyMillis;
                        if (time < this.mLastSpeedTimesMs[speedIndex]) {
                            this.mDeltaSpeedTimesMs[speedIndex] = time;
                        } else {
                            this.mDeltaSpeedTimesMs[speedIndex] = time - this.mLastSpeedTimesMs[speedIndex];
                        }
                        this.mLastSpeedTimesMs[speedIndex] = time;
                    } catch (NumberFormatException ex) {
                        Slog.e(TAG, "Failed to parse freq-time[" + line + "] for " + ex.getMessage());
                        Bundle data = new Bundle();
                        data.putString("cpuState", cpuState);
                        data.putString("cpuTime", cpuTime);
                        data.putString("extra", "cpu number:" + this.mCpuNumber);
                        if (this.mMonitor != null) {
                            this.mMonitor.monitor(907400016, data);
                        }
                    }
                    speedIndex++;
                }
            }
            $closeResource(null, reader);
        } catch (IOException e) {
            try {
                Slog.e(TAG, "Failed to read cpu-freq: " + e.getMessage());
                Arrays.fill(this.mDeltaSpeedTimesMs, 0);
            } catch (Throwable th) {
                StrictMode.setThreadPolicy(policy);
                throw th;
            }
        } catch (Throwable th2) {
            $closeResource(r2, reader);
            throw th2;
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

    public long[] readAbsolute() {
        BufferedReader reader;
        StrictMode.ThreadPolicy policy = StrictMode.allowThreadDiskReads();
        long[] speedTimeMs = new long[this.mNumSpeedSteps];
        try {
            reader = new BufferedReader(new FileReader(this.mProcFile));
            TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(' ');
            int speedIndex = 0;
            while (speedIndex < this.mNumSpeedSteps) {
                String readLine = reader.readLine();
                String line = readLine;
                if (readLine != null) {
                    splitter.setString(line);
                    splitter.next();
                    speedTimeMs[speedIndex] = Long.parseLong(splitter.next()) * this.mJiffyMillis;
                    speedIndex++;
                }
            }
            $closeResource(null, reader);
        } catch (IOException e) {
            try {
                Slog.e(TAG, "Failed to read cpu-freq: " + e.getMessage());
                Arrays.fill(speedTimeMs, 0);
            } catch (Throwable th) {
                StrictMode.setThreadPolicy(policy);
                throw th;
            }
        } catch (Throwable th2) {
            $closeResource(r3, reader);
            throw th2;
        }
        StrictMode.setThreadPolicy(policy);
        return speedTimeMs;
    }
}

package com.android.internal.os;

import android.common.HwFrameworkFactory;
import android.common.HwFrameworkMonitor;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.system.OsConstants;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.Slog;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import libcore.io.Libcore;

public class KernelCpuSpeedReader {
    private static final String TAG = "KernelCpuSpeedReader";
    private final int mCpuNumber;
    private final long[] mDeltaSpeedTimes;
    private final long mJiffyMillis;
    private final long[] mLastSpeedTimes;
    private HwFrameworkMonitor mMonitor = null;
    private final String mProcFile;

    public KernelCpuSpeedReader(int cpuNumber, int numSpeedSteps) {
        this.mProcFile = String.format("/sys/devices/system/cpu/cpu%d/cpufreq/stats/time_in_state", new Object[]{Integer.valueOf(cpuNumber)});
        this.mCpuNumber = cpuNumber;
        this.mMonitor = HwFrameworkFactory.getHwFrameworkMonitor();
        this.mLastSpeedTimes = new long[numSpeedSteps];
        this.mDeltaSpeedTimes = new long[numSpeedSteps];
        this.mJiffyMillis = 1000 / Libcore.os.sysconf(OsConstants._SC_CLK_TCK);
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x00f5 A:{SYNTHETIC, Splitter: B:29:0x00f5} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0151 A:{SYNTHETIC, Splitter: B:59:0x0151} */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00fa A:{SYNTHETIC, Splitter: B:32:0x00fa} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long[] readDelta() {
        Throwable th;
        ThreadPolicy policy = StrictMode.allowThreadDiskReads();
        Throwable th2 = null;
        BufferedReader reader = null;
        IOException e;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(this.mProcFile));
            try {
                SimpleStringSplitter splitter = new SimpleStringSplitter(' ');
                for (int speedIndex = 0; speedIndex < this.mLastSpeedTimes.length; speedIndex++) {
                    String line = reader2.readLine();
                    if (line == null) {
                        break;
                    }
                    splitter.setString(line);
                    String cpuState = splitter.next();
                    String cpuTime = splitter.next();
                    try {
                        Long.parseLong(cpuState);
                        long time = Long.parseLong(cpuTime) * this.mJiffyMillis;
                        if (time < this.mLastSpeedTimes[speedIndex]) {
                            this.mDeltaSpeedTimes[speedIndex] = time;
                        } else {
                            this.mDeltaSpeedTimes[speedIndex] = time - this.mLastSpeedTimes[speedIndex];
                        }
                        this.mLastSpeedTimes[speedIndex] = time;
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
                }
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
                    } catch (Throwable th4) {
                        th = th4;
                        StrictMode.setThreadPolicy(policy);
                        throw th;
                    }
                }
                StrictMode.setThreadPolicy(policy);
                reader = reader2;
                return this.mDeltaSpeedTimes;
            } catch (Throwable th5) {
                th = th5;
                reader = reader2;
                if (reader != null) {
                }
                if (th2 == null) {
                }
            }
            try {
                Slog.e(TAG, "Failed to read cpu-freq: " + e.getMessage());
                Arrays.fill(this.mDeltaSpeedTimes, 0);
                StrictMode.setThreadPolicy(policy);
                return this.mDeltaSpeedTimes;
            } catch (Throwable th6) {
                th = th6;
                StrictMode.setThreadPolicy(policy);
                throw th;
            }
        } catch (Throwable th7) {
            th = th7;
            if (reader != null) {
                try {
                    reader.close();
                } catch (Throwable th8) {
                    if (th2 == null) {
                        th2 = th8;
                    } else if (th2 != th8) {
                        th2.addSuppressed(th8);
                    }
                }
            }
            if (th2 == null) {
                try {
                    throw th2;
                } catch (IOException e3) {
                    e = e3;
                }
            } else {
                throw th;
            }
        }
    }
}

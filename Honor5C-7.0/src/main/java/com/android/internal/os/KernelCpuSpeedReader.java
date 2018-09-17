package com.android.internal.os;

import android.common.HwFrameworkFactory;
import android.common.HwFrameworkMonitor;
import android.os.Bundle;
import android.system.OsConstants;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.Slog;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import libcore.io.Libcore;

public class KernelCpuSpeedReader {
    private static final String TAG = "KernelCpuSpeedReader";
    private final int mCpuNumber;
    private final long[] mDeltaSpeedTimes;
    private final long mJiffyMillis;
    private final long[] mLastSpeedTimes;
    private HwFrameworkMonitor mMonitor;
    private final String mProcFile;

    public KernelCpuSpeedReader(int cpuNumber, int numSpeedSteps) {
        this.mMonitor = null;
        this.mProcFile = String.format("/sys/devices/system/cpu/cpu%d/cpufreq/stats/time_in_state", new Object[]{Integer.valueOf(cpuNumber)});
        this.mCpuNumber = cpuNumber;
        this.mMonitor = HwFrameworkFactory.getHwFrameworkMonitor();
        this.mLastSpeedTimes = new long[numSpeedSteps];
        this.mDeltaSpeedTimes = new long[numSpeedSteps];
        this.mJiffyMillis = 1000 / Libcore.os.sysconf(OsConstants._SC_CLK_TCK);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long[] readDelta() {
        IOException e;
        Throwable th;
        Throwable th2 = null;
        BufferedReader bufferedReader = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(this.mProcFile));
            try {
                SimpleStringSplitter splitter = new SimpleStringSplitter(' ');
                for (int speedIndex = 0; speedIndex < this.mLastSpeedTimes.length; speedIndex++) {
                    String line = reader.readLine();
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
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    try {
                        throw th2;
                    } catch (IOException e2) {
                        e = e2;
                    }
                } else {
                    return this.mDeltaSpeedTimes;
                }
            } catch (Throwable th4) {
                th = th4;
                bufferedReader = reader;
            }
        } catch (Throwable th5) {
            th = th5;
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Throwable th6) {
                    if (th2 == null) {
                        th2 = th6;
                    } else if (th2 != th6) {
                        th2.addSuppressed(th6);
                    }
                }
            }
            if (th2 != null) {
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

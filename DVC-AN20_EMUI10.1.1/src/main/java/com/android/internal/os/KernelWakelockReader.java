package com.android.internal.os;

import android.os.Process;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.KernelWakelockStats;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

public class KernelWakelockReader {
    private static final int[] PROC_WAKELOCKS_FORMAT = {5129, 8201, 9, 9, 9, 8201};
    private static final String TAG = "KernelWakelockReader";
    private static final int[] WAKEUP_SOURCES_FORMAT = {4105, 8457, 265, 265, 265, 265, 8457};
    private static int sKernelWakelockUpdateVersion = 0;
    private static final String sWakelockFile = "/proc/wakelocks";
    private static final String sWakeupSourceFile = "/d/wakeup_sources";
    private final long[] mProcWakelocksData = new long[3];
    private final String[] mProcWakelocksName = new String[3];

    public final KernelWakelockStats readKernelWakelockStats(KernelWakelockStats staleStats) {
        boolean wakeup_sources;
        FileInputStream is;
        byte[] buffer = new byte[32768];
        int len = 0;
        long startTime = SystemClock.uptimeMillis();
        int oldMask = StrictMode.allowThreadDiskReadsMask();
        try {
            is = new FileInputStream(sWakelockFile);
            wakeup_sources = false;
        } catch (FileNotFoundException e) {
            try {
                is = new FileInputStream(sWakeupSourceFile);
                wakeup_sources = true;
            } catch (FileNotFoundException e2) {
                Slog.wtf(TAG, "neither /proc/wakelocks nor /d/wakeup_sources exists");
                StrictMode.setThreadPolicyMask(oldMask);
                return null;
            }
        }
        while (true) {
            try {
                int cnt = is.read(buffer, len, buffer.length - len);
                if (cnt <= 0) {
                    break;
                }
                len += cnt;
            } catch (IOException e3) {
                Slog.wtf(TAG, "failed to read kernel wakelocks", e3);
                StrictMode.setThreadPolicyMask(oldMask);
                return null;
            } catch (Throwable th) {
                StrictMode.setThreadPolicyMask(oldMask);
                throw th;
            }
        }
        is.close();
        StrictMode.setThreadPolicyMask(oldMask);
        long readTime = SystemClock.uptimeMillis() - startTime;
        if (readTime > 100) {
            Slog.w(TAG, "Reading wakelock stats took " + readTime + "ms");
        }
        if (len > 0) {
            if (len >= buffer.length) {
                Slog.wtf(TAG, "Kernel wake locks exceeded buffer size " + buffer.length);
            }
            int i = 0;
            while (true) {
                if (i >= len) {
                    break;
                } else if (buffer[i] == 0) {
                    len = i;
                    break;
                } else {
                    i++;
                }
            }
        }
        return parseProcWakelocks(buffer, len, wakeup_sources, staleStats);
    }

    @VisibleForTesting
    public KernelWakelockStats parseProcWakelocks(byte[] wlBuffer, int len, boolean wakeup_sources, KernelWakelockStats staleStats) {
        byte b;
        String[] nameStringArray;
        long[] wlData;
        int[] iArr;
        long totalTime;
        int i = 0;
        while (true) {
            b = 10;
            if (i >= len || wlBuffer[i] == 10 || wlBuffer[i] == 0) {
                int startIndex = i + 1;
                int endIndex = startIndex;
            } else {
                i++;
            }
        }
        int startIndex2 = i + 1;
        int endIndex2 = startIndex2;
        synchronized (this) {
            try {
                sKernelWakelockUpdateVersion++;
                int startIndex3 = startIndex2;
                while (true) {
                    if (endIndex2 < len) {
                        int endIndex3 = startIndex3;
                        while (endIndex3 < len) {
                            try {
                                if (wlBuffer[endIndex3] == b || wlBuffer[endIndex3] == 0) {
                                    break;
                                }
                                endIndex3++;
                            } catch (Throwable th) {
                                th = th;
                                throw th;
                            }
                        }
                        if (endIndex3 > len - 1) {
                            break;
                        }
                        try {
                            nameStringArray = this.mProcWakelocksName;
                            wlData = this.mProcWakelocksData;
                            for (int j = startIndex3; j < endIndex3; j++) {
                                if ((wlBuffer[j] & 128) != 0) {
                                    wlBuffer[j] = 63;
                                }
                            }
                            if (wakeup_sources) {
                                iArr = WAKEUP_SOURCES_FORMAT;
                            } else {
                                iArr = PROC_WAKELOCKS_FORMAT;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                        try {
                            boolean parsed = Process.parseProcLine(wlBuffer, startIndex3, endIndex3, iArr, nameStringArray, wlData, null);
                            String name = nameStringArray[0].trim();
                            int count = (int) wlData[1];
                            if (wakeup_sources) {
                                totalTime = wlData[2] * 1000;
                            } else {
                                totalTime = (wlData[2] + 500) / 1000;
                            }
                            if (!parsed || name.length() <= 0) {
                                if (!parsed) {
                                    try {
                                        Slog.wtf(TAG, "Failed to parse proc line: " + new String(wlBuffer, startIndex3, endIndex3 - startIndex3));
                                    } catch (Exception e) {
                                        Slog.wtf(TAG, "Failed to parse proc line!");
                                    }
                                }
                            } else if (!staleStats.containsKey(name)) {
                                staleStats.put(name, new KernelWakelockStats.Entry(count, totalTime, sKernelWakelockUpdateVersion));
                            } else {
                                KernelWakelockStats.Entry kwlStats = (KernelWakelockStats.Entry) staleStats.get(name);
                                if (kwlStats.mVersion == sKernelWakelockUpdateVersion) {
                                    kwlStats.mCount += count;
                                    kwlStats.mTotalTime += totalTime;
                                } else {
                                    kwlStats.mCount = count;
                                    kwlStats.mTotalTime = totalTime;
                                    kwlStats.mVersion = sKernelWakelockUpdateVersion;
                                }
                            }
                            startIndex3 = endIndex3 + 1;
                            endIndex2 = endIndex3;
                            b = 10;
                        } catch (Throwable th3) {
                            th = th3;
                            throw th;
                        }
                    }
                }
                try {
                    Iterator<KernelWakelockStats.Entry> itr = staleStats.values().iterator();
                    while (itr.hasNext()) {
                        if (itr.next().mVersion != sKernelWakelockUpdateVersion) {
                            itr.remove();
                        }
                    }
                    staleStats.kernelWakelockVersion = sKernelWakelockUpdateVersion;
                    return staleStats;
                } catch (Throwable th4) {
                    th = th4;
                    throw th;
                }
            } catch (Throwable th5) {
                th = th5;
                throw th;
            }
        }
    }
}

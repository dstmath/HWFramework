package com.android.internal.os;

import android.os.Process;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.midi.MidiConstants;
import com.android.internal.os.KernelWakelockStats;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

public class KernelWakelockReader {
    private static final int[] PROC_WAKELOCKS_FORMAT = {5129, 8201, 9, 9, 9, 8201};
    private static final String TAG = "KernelWakelockReader";
    private static final int[] WAKEUP_SOURCES_FORMAT = {4105, 8457, MetricsProto.MetricsEvent.NOTIFICATION_TOPIC_NOTIFICATION, MetricsProto.MetricsEvent.NOTIFICATION_TOPIC_NOTIFICATION, MetricsProto.MetricsEvent.NOTIFICATION_TOPIC_NOTIFICATION, MetricsProto.MetricsEvent.NOTIFICATION_TOPIC_NOTIFICATION, 8457};
    private static int sKernelWakelockUpdateVersion = 0;
    private static final String sWakelockFile = "/proc/wakelocks";
    private static final String sWakeupSourceFile = "/d/wakeup_sources";
    private final long[] mProcWakelocksData = new long[3];
    private final String[] mProcWakelocksName = new String[3];

    public final KernelWakelockStats readKernelWakelockStats(KernelWakelockStats staleStats) {
        boolean wakeup_sources;
        FileInputStream is;
        byte[] buffer = new byte[32768];
        long startTime = SystemClock.uptimeMillis();
        int oldMask = StrictMode.allowThreadDiskReadsMask();
        try {
            is = new FileInputStream(sWakelockFile);
            wakeup_sources = false;
            try {
                int len = is.read(buffer);
                is.close();
                StrictMode.setThreadPolicyMask(oldMask);
                if (SystemClock.uptimeMillis() - startTime > 100) {
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
            } catch (IOException e) {
                Slog.wtf(TAG, "failed to read kernel wakelocks", e);
                StrictMode.setThreadPolicyMask(oldMask);
                return null;
            } catch (Throwable th) {
                StrictMode.setThreadPolicyMask(oldMask);
                throw th;
            }
        } catch (FileNotFoundException e2) {
            try {
                is = new FileInputStream(sWakeupSourceFile);
                wakeup_sources = true;
            } catch (FileNotFoundException e3) {
                Slog.wtf(TAG, "neither /proc/wakelocks nor /d/wakeup_sources exists");
                StrictMode.setThreadPolicyMask(oldMask);
                return null;
            }
        }
    }

    @VisibleForTesting
    public KernelWakelockStats parseProcWakelocks(byte[] wlBuffer, int len, boolean wakeup_sources, KernelWakelockStats staleStats) {
        int i;
        byte b;
        int startIndex;
        int[] iArr;
        long totalTime;
        byte[] bArr = wlBuffer;
        int i2 = len;
        KernelWakelockStats kernelWakelockStats = staleStats;
        char c = 0;
        int i3 = 0;
        while (true) {
            i = i3;
            b = 10;
            if (i >= i2 || bArr[i] == 10 || bArr[i] == 0) {
                int i4 = i + 1;
                int endIndex = i4;
                int startIndex2 = i4;
            } else {
                i3 = i + 1;
            }
        }
        int i42 = i + 1;
        int endIndex2 = i42;
        int startIndex22 = i42;
        synchronized (this) {
            try {
                sKernelWakelockUpdateVersion++;
                int startIndex3 = startIndex22;
                while (true) {
                    if (endIndex2 >= i2) {
                        startIndex = startIndex3;
                        break;
                    }
                    int endIndex3 = startIndex3;
                    while (endIndex3 < i2) {
                        try {
                            if (bArr[endIndex3] == b || bArr[endIndex3] == 0) {
                                break;
                            }
                            endIndex3++;
                        } catch (Throwable th) {
                            th = th;
                            int i5 = endIndex3;
                            int i6 = startIndex3;
                            throw th;
                        }
                    }
                    if (endIndex3 > i2 - 1) {
                        int i7 = endIndex3;
                        startIndex = startIndex3;
                        break;
                    }
                    try {
                        String[] nameStringArray = this.mProcWakelocksName;
                        long[] wlData = this.mProcWakelocksData;
                        for (int j = startIndex3; j < endIndex3; j++) {
                            if ((bArr[j] & MidiConstants.STATUS_NOTE_OFF) != 0) {
                                bArr[j] = 63;
                            }
                        }
                        if (wakeup_sources) {
                            iArr = WAKEUP_SOURCES_FORMAT;
                        } else {
                            iArr = PROC_WAKELOCKS_FORMAT;
                        }
                        int endIndex4 = endIndex3;
                        int startIndex4 = startIndex3;
                        try {
                            boolean parsed = Process.parseProcLine(bArr, startIndex3, endIndex3, iArr, nameStringArray, wlData, null);
                            String name = nameStringArray[c];
                            int count = (int) wlData[1];
                            if (wakeup_sources) {
                                totalTime = wlData[2] * 1000;
                            } else {
                                totalTime = (wlData[2] + 500) / 1000;
                            }
                            long totalTime2 = totalTime;
                            if (!parsed || name.length() <= 0) {
                                if (!parsed) {
                                    Slog.wtf(TAG, "Failed to parse proc line: " + new String(bArr, startIndex4, endIndex4 - startIndex4));
                                }
                            } else if (!kernelWakelockStats.containsKey(name)) {
                                kernelWakelockStats.put(name, new KernelWakelockStats.Entry(count, totalTime2, sKernelWakelockUpdateVersion));
                            } else {
                                KernelWakelockStats.Entry kwlStats = (KernelWakelockStats.Entry) kernelWakelockStats.get(name);
                                if (kwlStats.mVersion == sKernelWakelockUpdateVersion) {
                                    kwlStats.mCount += count;
                                    kwlStats.mTotalTime += totalTime2;
                                } else {
                                    kwlStats.mCount = count;
                                    kwlStats.mTotalTime = totalTime2;
                                    kwlStats.mVersion = sKernelWakelockUpdateVersion;
                                }
                            }
                        } catch (Exception e) {
                            Slog.wtf(TAG, "Failed to parse proc line!");
                        } catch (Throwable th2) {
                            th = th2;
                            int i8 = startIndex4;
                            int i9 = endIndex4;
                        }
                        startIndex3 = endIndex4 + 1;
                        endIndex2 = endIndex4;
                        c = 0;
                        b = 10;
                    } catch (Throwable th3) {
                        th = th3;
                        int i10 = startIndex3;
                        int i11 = endIndex3;
                        throw th;
                    }
                }
                try {
                    Iterator<KernelWakelockStats.Entry> itr = staleStats.values().iterator();
                    while (itr.hasNext()) {
                        if (itr.next().mVersion != sKernelWakelockUpdateVersion) {
                            itr.remove();
                        }
                    }
                    kernelWakelockStats.kernelWakelockVersion = sKernelWakelockUpdateVersion;
                    return kernelWakelockStats;
                } catch (Throwable th4) {
                    th = th4;
                    int i12 = startIndex;
                    throw th;
                }
            } catch (Throwable th5) {
                th = th5;
                throw th;
            }
        }
    }
}

package com.android.internal.os;

import android.os.Process;
import android.util.Slog;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.internal.os.KernelWakelockStats.Entry;
import com.huawei.pgmng.log.LogPower;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

public class KernelWakelockReader {
    private static final int[] PROC_WAKELOCKS_FORMAT = null;
    private static final String TAG = "KernelWakelockReader";
    private static final int[] WAKEUP_SOURCES_FORMAT = null;
    private static int sKernelWakelockUpdateVersion = 0;
    private static final String sWakelockFile = "/proc/wakelocks";
    private static final String sWakeupSourceFile = "/d/wakeup_sources";
    private final long[] mProcWakelocksData;
    private final String[] mProcWakelocksName;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.os.KernelWakelockReader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.os.KernelWakelockReader.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.KernelWakelockReader.<clinit>():void");
    }

    public KernelWakelockReader() {
        this.mProcWakelocksName = new String[3];
        this.mProcWakelocksData = new long[3];
    }

    public final KernelWakelockStats readKernelWakelockStats(KernelWakelockStats staleStats) {
        FileInputStream is;
        boolean wakeup_sources;
        byte[] buffer = new byte[AccessibilityNodeInfo.ACTION_PASTE];
        try {
            is = new FileInputStream(sWakelockFile);
            wakeup_sources = false;
        } catch (FileNotFoundException e) {
            try {
                is = new FileInputStream(sWakeupSourceFile);
                wakeup_sources = true;
            } catch (FileNotFoundException e2) {
                Slog.wtf(TAG, "neither /proc/wakelocks nor /d/wakeup_sources exists");
                return null;
            }
        }
        try {
            int len = is.read(buffer);
            is.close();
            if (len > 0) {
                if (len >= buffer.length) {
                    Slog.wtf(TAG, "Kernel wake locks exceeded buffer size " + buffer.length);
                }
                for (int i = 0; i < len; i++) {
                    if (buffer[i] == null) {
                        len = i;
                        break;
                    }
                }
            }
            return parseProcWakelocks(buffer, len, wakeup_sources, staleStats);
        } catch (IOException e3) {
            Slog.wtf(TAG, "failed to read kernel wakelocks", e3);
            return null;
        }
    }

    public KernelWakelockStats parseProcWakelocks(byte[] wlBuffer, int len, boolean wakeup_sources, KernelWakelockStats staleStats) {
        int i = 0;
        while (i < len && wlBuffer[i] != 10 && wlBuffer[i] != null) {
            i++;
        }
        int endIndex = i + 1;
        int startIndex = endIndex;
        synchronized (this) {
            sKernelWakelockUpdateVersion++;
            while (endIndex < len) {
                endIndex = startIndex;
                while (endIndex < len && wlBuffer[endIndex] != 10 && wlBuffer[endIndex] != null) {
                    endIndex++;
                }
                if (endIndex > len - 1) {
                    break;
                }
                int[] iArr;
                long totalTime;
                String[] nameStringArray = this.mProcWakelocksName;
                long[] wlData = this.mProcWakelocksData;
                for (int j = startIndex; j < endIndex; j++) {
                    if ((wlBuffer[j] & LogPower.START_CHG_ROTATION) != 0) {
                        wlBuffer[j] = (byte) 63;
                    }
                }
                if (wakeup_sources) {
                    iArr = WAKEUP_SOURCES_FORMAT;
                } else {
                    iArr = PROC_WAKELOCKS_FORMAT;
                }
                boolean parsed = Process.parseProcLine(wlBuffer, startIndex, endIndex, iArr, nameStringArray, wlData, null);
                String name = nameStringArray[0];
                int count = (int) wlData[1];
                if (wakeup_sources) {
                    totalTime = wlData[2] * 1000;
                } else {
                    totalTime = (wlData[2] + 500) / 1000;
                }
                if (!parsed || name.length() <= 0) {
                    if (parsed) {
                        continue;
                    } else {
                        try {
                            Slog.wtf(TAG, "Failed to parse proc line: " + new String(wlBuffer, startIndex, endIndex - startIndex));
                        } catch (Exception e) {
                            Slog.wtf(TAG, "Failed to parse proc line!");
                        }
                    }
                } else if (staleStats.containsKey(name)) {
                    Entry kwlStats = (Entry) staleStats.get(name);
                    if (kwlStats.mVersion == sKernelWakelockUpdateVersion) {
                        kwlStats.mCount += count;
                        kwlStats.mTotalTime += totalTime;
                    } else {
                        kwlStats.mCount = count;
                        kwlStats.mTotalTime = totalTime;
                        kwlStats.mVersion = sKernelWakelockUpdateVersion;
                    }
                } else {
                    staleStats.put(name, new Entry(count, totalTime, sKernelWakelockUpdateVersion));
                }
                startIndex = endIndex + 1;
            }
            Iterator<Entry> itr = staleStats.values().iterator();
            while (itr.hasNext()) {
                if (((Entry) itr.next()).mVersion != sKernelWakelockUpdateVersion) {
                    itr.remove();
                }
            }
            staleStats.kernelWakelockVersion = sKernelWakelockUpdateVersion;
        }
        return staleStats;
    }
}

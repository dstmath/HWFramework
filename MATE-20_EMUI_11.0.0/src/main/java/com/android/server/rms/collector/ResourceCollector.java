package com.android.server.rms.collector;

import android.os.Debug;
import com.huawei.annotation.HwSystemApi;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@HwSystemApi
public final class ResourceCollector {
    private static final int END_MARK = 5;
    private static final String TAG = "RMS.ResourceCollector";
    private static AtomicBoolean sIsValidPssFast = new AtomicBoolean(false);
    private static AtomicInteger sPssFastRefs = new AtomicInteger(0);

    @HwSystemApi
    public static native String getBuddyInfo();

    @HwSystemApi
    public static native String getIonInfo();

    @HwSystemApi
    public static native int getMemInfo(long[] jArr);

    @HwSystemApi
    public static native long getPressureDetector();

    private static native long getPssFast(int i, long[] jArr, long[] jArr2);

    @HwSystemApi
    public static native int getSumIon();

    @HwSystemApi
    public static native int killProcessGroupForQuickKill(int i, int i2);

    @HwSystemApi
    public static native int registPressureThreshold(long j, int[] iArr);

    @HwSystemApi
    public static final native void setThreadMinUtil(int i, int i2);

    @HwSystemApi
    public static native void setThreadVip(int i, int i2, boolean z);

    @HwSystemApi
    public static final native void setThreadVipPriority(int i, int i2);

    @HwSystemApi
    public static native int[] waitForPressure(long j);

    private ResourceCollector() {
    }

    @HwSystemApi
    public static long getPss(int pid, long[] outUssSwapPss, long[] outMemtrack) {
        long pss = isValidPssFast() ? getPssFast(pid, outUssSwapPss, outMemtrack) : 0;
        if (pss <= 0) {
            if (sPssFastRefs.get() < 5) {
                sPssFastRefs.addAndGet(1);
            }
            return Debug.getPss(pid, outUssSwapPss, outMemtrack);
        }
        sPssFastRefs.set(5);
        sIsValidPssFast.set(true);
        return pss;
    }

    private static boolean isValidPssFast() {
        if (sPssFastRefs.get() == 5) {
            return sIsValidPssFast.get();
        }
        return true;
    }
}

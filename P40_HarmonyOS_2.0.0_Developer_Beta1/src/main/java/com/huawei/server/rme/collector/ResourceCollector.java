package com.huawei.server.rme.collector;

import android.util.Slog;
import com.huawei.server.rme.hyperhold.KillDecision;

public final class ResourceCollector {
    private static final String TAG = "HyperHold.ResourceCollector";

    public static native int getDailySwapGB();

    public static native double[] getModelMetrics();

    public static native void serializeKillModel();

    public static native void setProcessorAffinity(int i, int i2, int i3);

    public static native String[] sortWithKillModel(String[] strArr, long[] jArr);

    public static native void updateKillModel(String str);

    public static native long updateTimeOn(int i);

    private ResourceCollector() {
    }

    public static void killApplication() {
        Slog.i(TAG, "Begin call killApplicationWithThread.");
        KillDecision.getInstance().killApplicationWithNewThread();
    }
}

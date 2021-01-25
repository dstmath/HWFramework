package com.huawei.server.rme.hyperhold;

import android.util.Slog;

public final class JniCommunication {
    private static final String TAG = "JniCommunication";

    public static native void triggerEvent(String str, String str2, int i);

    public static native void triggerPsiEvent(int i, int i2);

    private JniCommunication() {
    }

    public static void killApplication(int level) {
        Slog.i(TAG, "Begin call killApplicationWithThread.");
        KillDecision.getInstance().killApplicationWithNewThread(level, 0);
    }

    public static void handleZswapd() {
        Slog.i(TAG, "Begin call handleZswapd.");
        KernelEventReceiver.getInstance().handleZswapd();
    }

    public static void handleZswapdUnrecycle() {
        Slog.i(TAG, "Begin call handleZswapdUnrecycle.");
        KernelEventReceiver.getInstance().handleZswapdUnmet(false);
    }

    public static void handleZswapdUnmet() {
        Slog.i(TAG, "Begin call handleZswapdUnmet.");
        KernelEventReceiver.getInstance().handleZswapdUnmet(true);
    }
}

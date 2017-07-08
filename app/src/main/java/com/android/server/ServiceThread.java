package com.android.server;

import android.os.HandlerThread;
import android.os.Process;
import android.os.StrictMode;
import android.util.Slog;

public class ServiceThread extends HandlerThread {
    private static final String TAG = "ServiceThread";
    private final boolean mAllowIo;

    public ServiceThread(String name, int priority, boolean allowIo) {
        super(name, priority);
        this.mAllowIo = allowIo;
    }

    public void run() {
        Process.setCanSelfBackground(false);
        if (!this.mAllowIo && StrictMode.conditionallyEnableDebugLogging()) {
            Slog.i(TAG, "Enabled StrictMode logging for " + getName() + " looper.");
        }
        super.run();
    }
}

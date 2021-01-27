package com.android.server;

/* access modifiers changed from: package-private */
public interface INativeDaemonConnectorCallbacks {
    boolean onCheckHoldWakeLock(int i);

    void onDaemonConnected();

    boolean onEvent(int i, String str, String[] strArr);
}

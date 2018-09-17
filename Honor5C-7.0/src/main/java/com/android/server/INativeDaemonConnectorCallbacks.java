package com.android.server;

interface INativeDaemonConnectorCallbacks {
    boolean onCheckHoldWakeLock(int i);

    void onDaemonConnected();

    boolean onEvent(int i, String str, String[] strArr);
}

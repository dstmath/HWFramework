package com.huawei.android.pgmng.plug;

public interface IPGPlugCallbacks {
    void onConnectedTimeout();

    void onDaemonConnected();

    boolean onEvent(int i, String str);
}

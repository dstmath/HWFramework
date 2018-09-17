package com.huawei.pgmng;

public interface IPGPlugCallbacks {
    void onConnectedTimeout();

    void onDaemonConnected();

    boolean onEvent(int i, String str);
}

package com.huawei.hilink.framework.aidl;

public interface HilinkServiceProxyState {
    public static final int PROXY_INIT_FAILED = 2;
    public static final int PROXY_NOT_HILINK_GATEWAY = 1;
    public static final int PROXY_OK = 0;

    void onConnectionState(int i);

    void onProxyLost();

    void onProxyReady(int i);
}

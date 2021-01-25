package com.huawei.android.server;

public interface IHwWatchConnectivityServiceEx {
    public static final int PHASE_BOOT_COMPLETED = 1000;
    public static final int PHASE_SYSTEM_SERVICES_READY = 500;

    void onBootPhase(int i);

    void onStart();
}

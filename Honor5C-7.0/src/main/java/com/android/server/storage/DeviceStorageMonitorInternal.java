package com.android.server.storage;

public interface DeviceStorageMonitorInternal {
    void checkMemory();

    long getMemoryLowThreshold();

    boolean isMemoryLow();
}

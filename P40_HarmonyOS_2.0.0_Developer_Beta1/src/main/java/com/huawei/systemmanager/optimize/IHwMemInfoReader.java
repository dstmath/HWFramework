package com.huawei.systemmanager.optimize;

public interface IHwMemInfoReader {
    long getCachedSize();

    long getFreeSize();

    void readMemInfo();
}

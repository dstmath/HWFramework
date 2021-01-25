package com.huawei.systemmanager.optimize;

public class HwMemInfoManager {
    public static IHwMemInfoReader getHwMemInfoReader() {
        return HwMemInfoReaderImpl.getIsntance();
    }
}

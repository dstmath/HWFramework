package com.huawei.dfr.zrhung;

import android.os.DefaultBlockMonitorEx;

public class DefaultBlockMonitor extends DefaultBlockMonitorEx {
    public static DefaultBlockMonitor getBlockMonitor() {
        return new DefaultBlockMonitor();
    }
}

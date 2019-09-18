package com.huawei.android.hardware.display;

import android.hardware.display.IDisplayManager;

public interface IHwDisplayManagerGlobalInner {
    void addWifiDisplayScanNestCount();

    Object getLock();

    IDisplayManager getService();

    int getWifiDisplayScanNestCount();

    void registerCallbackIfNeededLockedInner();
}

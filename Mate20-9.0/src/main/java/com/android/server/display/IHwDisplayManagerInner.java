package com.android.server.display;

import com.android.server.display.DisplayManagerService;

public interface IHwDisplayManagerInner {
    DisplayManagerService.SyncRoot getLock();

    WifiDisplayAdapter getWifiDisplayAdapter();

    void startWifiDisplayScanInner(int i, int i2);
}

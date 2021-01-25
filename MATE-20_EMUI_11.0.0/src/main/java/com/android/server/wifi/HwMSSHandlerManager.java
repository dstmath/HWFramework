package com.android.server.wifi;

import com.android.server.wifi.wificond.NativeMssResult;

public interface HwMSSHandlerManager {
    void mssSwitchCheck(int i);

    void onMssDrvEvent(NativeMssResult nativeMssResult);
}

package com.huawei.server.wm;

import android.view.SurfaceControl;
import com.android.server.wm.TransactionEx;
import com.android.server.wm.WindowManagerServiceEx;

public class HwSingleHandContentExBridgeEx {
    private HwSingleHandContentExBridge hwSingleHandContentExBridge;
    private WindowManagerServiceEx serviceEx;

    public WindowManagerServiceEx getServiceEx() {
        return this.serviceEx;
    }

    public void setServiceEx(WindowManagerServiceEx serviceEx2) {
        this.serviceEx = serviceEx2;
    }

    public HwSingleHandContentExBridgeEx(WindowManagerServiceEx serviceEx2) {
        this.serviceEx = serviceEx2;
        this.hwSingleHandContentExBridge = new HwSingleHandContentExBridge(serviceEx2);
        this.hwSingleHandContentExBridge.setHwSingleHandContentExBridgeEx(this);
    }

    public void handleSingleHandMode(TransactionEx transaction, SurfaceControl winLayer, SurfaceControl overLayer) {
    }

    public HwSingleHandContentExBridge getHwSingleHandContentExBridge() {
        return this.hwSingleHandContentExBridge;
    }

    public void setHwSingleHandContentExBridge(HwSingleHandContentExBridge hwSingleHandContentExBridge2) {
        this.hwSingleHandContentExBridge = hwSingleHandContentExBridge2;
    }
}

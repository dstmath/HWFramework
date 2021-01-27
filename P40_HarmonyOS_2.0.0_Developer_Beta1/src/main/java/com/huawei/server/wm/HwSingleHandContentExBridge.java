package com.huawei.server.wm;

import android.view.SurfaceControl;
import com.android.server.wm.IHwSingleHandContentEx;
import com.android.server.wm.TransactionEx;
import com.android.server.wm.WindowManagerServiceEx;

public class HwSingleHandContentExBridge implements IHwSingleHandContentEx {
    private static IHwSingleHandContentEx instance;
    private HwSingleHandContentExBridgeEx hwSingleHandContentExBridgeEx;
    private WindowManagerServiceEx serviceEx;

    public void handleSingleHandMode(SurfaceControl.Transaction transaction, SurfaceControl winLayer, SurfaceControl overLayer) {
        this.hwSingleHandContentExBridgeEx.handleSingleHandMode(new TransactionEx(transaction), winLayer, overLayer);
    }

    public static IHwSingleHandContentEx getInstance(WindowManagerServiceEx serviceEx2) {
        if (instance == null) {
            instance = new HwSingleHandContentExBridge(serviceEx2);
        }
        return instance;
    }

    public HwSingleHandContentExBridge(WindowManagerServiceEx serviceEx2) {
        this.serviceEx = serviceEx2;
    }

    public HwSingleHandContentExBridgeEx getHwSingleHandContentExBridgeEx() {
        return this.hwSingleHandContentExBridgeEx;
    }

    public void setHwSingleHandContentExBridgeEx(HwSingleHandContentExBridgeEx hwSingleHandContentExBridgeEx2) {
        this.hwSingleHandContentExBridgeEx = hwSingleHandContentExBridgeEx2;
    }
}

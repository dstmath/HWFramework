package com.android.server.statusbar;

import android.view.KeyEvent;

public class HwStatusBarManagerServiceEx implements IHwStatusBarManagerServiceEx {
    private static final String TAG = "HwStatusBarManagerServiceEx";
    private static HwStatusBarManagerServiceEx sInstance;
    private IHwStatusBarEventListener mStatusBarManagerService;

    private HwStatusBarManagerServiceEx() {
    }

    public static synchronized HwStatusBarManagerServiceEx getInstance() {
        HwStatusBarManagerServiceEx hwStatusBarManagerServiceEx;
        synchronized (HwStatusBarManagerServiceEx.class) {
            if (sInstance == null) {
                sInstance = new HwStatusBarManagerServiceEx();
            }
            hwStatusBarManagerServiceEx = sInstance;
        }
        return hwStatusBarManagerServiceEx;
    }

    @Override // com.android.server.statusbar.IHwStatusBarManagerServiceEx
    public void notifyKeyEvent(KeyEvent event) {
        IHwStatusBarEventListener iHwStatusBarEventListener = this.mStatusBarManagerService;
        if (iHwStatusBarEventListener != null) {
            iHwStatusBarEventListener.onKeyEvent(event);
        }
    }

    @Override // com.android.server.statusbar.IHwStatusBarManagerServiceEx
    public void init(IHwStatusBarEventListener statusBarService) {
        this.mStatusBarManagerService = statusBarService;
    }

    @Override // com.android.server.statusbar.IHwStatusBarManagerServiceEx
    public void updateIsEnableLauncherShadow(boolean isEnable) {
        IHwStatusBarEventListener iHwStatusBarEventListener = this.mStatusBarManagerService;
        if (iHwStatusBarEventListener != null) {
            iHwStatusBarEventListener.onLauncherShadowStateChange(isEnable);
        }
    }
}

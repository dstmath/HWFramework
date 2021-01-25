package com.android.server.statusbar;

import android.view.KeyEvent;

public class HwStatusBarManagerServiceEx implements IHwStatusBarManagerServiceEx {
    private static final String TAG = "HwStatusBarManagerServiceEx";
    private static HwStatusBarManagerServiceEx sInstance;
    private IHwStatusBarKeyEventListener mStatusBarManagerService;

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
        IHwStatusBarKeyEventListener iHwStatusBarKeyEventListener = this.mStatusBarManagerService;
        if (iHwStatusBarKeyEventListener != null) {
            iHwStatusBarKeyEventListener.onKeyEvent(event);
        }
    }

    @Override // com.android.server.statusbar.IHwStatusBarManagerServiceEx
    public void init(IHwStatusBarKeyEventListener statusBarService) {
        this.mStatusBarManagerService = statusBarService;
    }
}

package com.android.server.wm;

import android.content.Context;

public class HwScreenRotationAnimationImplBridgeEx {
    private HwScreenRotationAnimationImplBridge mHwScreenRotationAnimationImplBridge = new HwScreenRotationAnimationImplBridge();

    public HwScreenRotationAnimationImplBridgeEx() {
        this.mHwScreenRotationAnimationImplBridge.setHwScreenRotationAnimationImplBridgeEx(this);
    }

    public HwScreenRotationAnimationImplBridge getScreenRotationAnimationBridge() {
        return this.mHwScreenRotationAnimationImplBridge;
    }

    public ScreenRotationAnimationBridgeEx create(Context context, DisplayContentEx displayContent, boolean isForceDefaultOrientation, boolean isSecure, WindowManagerServiceEx service) {
        return null;
    }
}

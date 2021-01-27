package com.android.server.wm;

import android.content.Context;

public class HwScreenRotationAnimationImplBridge implements IHwScreenRotationAnimation {
    private HwScreenRotationAnimationImplBridgeEx mHwScreenRotationAnimationImplBridgeEx;

    public void setHwScreenRotationAnimationImplBridgeEx(HwScreenRotationAnimationImplBridgeEx bridgeEx) {
        this.mHwScreenRotationAnimationImplBridgeEx = bridgeEx;
    }

    public ScreenRotationAnimationBridge create(Context context, DisplayContent displayContent, boolean isForceDefaultOrientation, boolean isSecure, WindowManagerService service) {
        ScreenRotationAnimationBridgeEx screenBridgeEx = this.mHwScreenRotationAnimationImplBridgeEx.create(context, new DisplayContentEx(displayContent), isForceDefaultOrientation, isSecure, new WindowManagerServiceEx(service));
        if (screenBridgeEx != null) {
            return screenBridgeEx.getScreenRotationAnimationBridge();
        }
        return null;
    }
}

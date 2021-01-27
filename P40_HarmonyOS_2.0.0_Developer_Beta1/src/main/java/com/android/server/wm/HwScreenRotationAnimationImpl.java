package com.android.server.wm;

import android.content.Context;

public class HwScreenRotationAnimationImpl extends HwScreenRotationAnimationImplBridgeEx {
    public ScreenRotationAnimationBridgeEx create(Context context, DisplayContentEx displayContent, boolean isForceDefaultOrientation, boolean isSecure, WindowManagerServiceEx service) {
        return new HwScreenRotationAnimation(context, displayContent, isForceDefaultOrientation, isSecure, service);
    }
}

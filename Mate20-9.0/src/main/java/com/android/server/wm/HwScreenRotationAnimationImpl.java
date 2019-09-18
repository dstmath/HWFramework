package com.android.server.wm;

import android.content.Context;

public class HwScreenRotationAnimationImpl implements IHwScreenRotationAnimation {
    public ScreenRotationAnimation create(Context context, DisplayContent displayContent, boolean forceDefaultOrientation, boolean isSecure, WindowManagerService service) {
        HwScreenRotationAnimation hwScreenRotationAnimation = new HwScreenRotationAnimation(context, displayContent, forceDefaultOrientation, isSecure, service);
        return hwScreenRotationAnimation;
    }
}

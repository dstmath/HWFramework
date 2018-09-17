package com.android.server.wm;

import android.content.Context;
import android.view.SurfaceSession;

public class HwScreenRotationAnimationImpl implements IHwScreenRotationAnimation {
    public ScreenRotationAnimation create(Context context, DisplayContent displayContent, SurfaceSession session, boolean inTransaction, boolean forceDefaultOrientation, boolean isSecure, WindowManagerService service) {
        return new HwScreenRotationAnimation(context, displayContent, session, inTransaction, forceDefaultOrientation, isSecure, service);
    }
}

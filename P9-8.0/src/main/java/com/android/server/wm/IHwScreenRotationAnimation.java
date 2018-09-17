package com.android.server.wm;

import android.content.Context;
import android.view.SurfaceSession;

public interface IHwScreenRotationAnimation {
    ScreenRotationAnimation create(Context context, DisplayContent displayContent, SurfaceSession surfaceSession, boolean z, boolean z2, boolean z3, WindowManagerService windowManagerService);
}

package com.android.server.wm;

import android.view.SurfaceControl;
import android.view.SurfaceSession;

interface SurfaceBuilderFactory {
    SurfaceControl.Builder make(SurfaceSession surfaceSession);
}

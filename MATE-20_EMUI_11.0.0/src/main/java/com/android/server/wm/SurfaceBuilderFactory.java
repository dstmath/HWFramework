package com.android.server.wm;

import android.view.SurfaceControl;
import android.view.SurfaceSession;

/* access modifiers changed from: package-private */
public interface SurfaceBuilderFactory {
    SurfaceControl.Builder make(SurfaceSession surfaceSession);
}

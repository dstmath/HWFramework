package com.android.server.wm;

import com.android.server.policy.WindowManagerPolicy;

public abstract class StartingData {
    protected final WindowManagerService mService;

    /* access modifiers changed from: package-private */
    public abstract WindowManagerPolicy.StartingSurface createStartingSurface(AppWindowToken appWindowToken);

    protected StartingData(WindowManagerService service) {
        this.mService = service;
    }
}

package com.android.server.wm;

import android.view.WindowManagerPolicy.StartingSurface;

public abstract class StartingData {
    protected final WindowManagerService mService;

    abstract StartingSurface createStartingSurface(AppWindowToken appWindowToken);

    protected StartingData(WindowManagerService service) {
        this.mService = service;
    }
}

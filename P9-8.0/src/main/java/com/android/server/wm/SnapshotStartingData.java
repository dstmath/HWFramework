package com.android.server.wm;

import android.app.ActivityManager.TaskSnapshot;
import android.view.WindowManagerPolicy.StartingSurface;

class SnapshotStartingData extends StartingData {
    private final WindowManagerService mService;
    private final TaskSnapshot mSnapshot;

    SnapshotStartingData(WindowManagerService service, TaskSnapshot snapshot) {
        super(service);
        this.mService = service;
        this.mSnapshot = snapshot;
    }

    StartingSurface createStartingSurface(AppWindowToken atoken) {
        return this.mService.mTaskSnapshotController.createStartingSurface(atoken, this.mSnapshot);
    }
}

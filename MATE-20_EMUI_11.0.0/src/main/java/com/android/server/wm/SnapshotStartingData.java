package com.android.server.wm;

import android.app.ActivityManager;
import com.android.server.policy.WindowManagerPolicy;

/* access modifiers changed from: package-private */
public class SnapshotStartingData extends StartingData {
    private final WindowManagerService mService;
    private final ActivityManager.TaskSnapshot mSnapshot;

    SnapshotStartingData(WindowManagerService service, ActivityManager.TaskSnapshot snapshot) {
        super(service);
        this.mService = service;
        this.mSnapshot = snapshot;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.StartingData
    public WindowManagerPolicy.StartingSurface createStartingSurface(AppWindowToken atoken) {
        return this.mService.mTaskSnapshotController.createStartingSurface(atoken, this.mSnapshot);
    }
}

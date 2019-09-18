package com.android.server.autofill.ui;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Binder;
import android.os.IBinder;

class OverlayControl {
    private final AppOpsManager mAppOpsManager;
    private final IBinder mToken = new Binder();

    OverlayControl(Context context) {
        this.mAppOpsManager = (AppOpsManager) context.getSystemService(AppOpsManager.class);
    }

    /* access modifiers changed from: package-private */
    public void hideOverlays() {
        setOverlayAllowed(false);
    }

    /* access modifiers changed from: package-private */
    public void showOverlays() {
        setOverlayAllowed(true);
    }

    private void setOverlayAllowed(boolean allowed) {
        if (this.mAppOpsManager != null) {
            this.mAppOpsManager.setUserRestrictionForUser(24, !allowed, this.mToken, null, -1);
            this.mAppOpsManager.setUserRestrictionForUser(45, !allowed, this.mToken, null, -1);
        }
    }
}

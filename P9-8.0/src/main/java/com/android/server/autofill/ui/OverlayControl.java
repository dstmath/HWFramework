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

    void hideOverlays() {
        setOverlayAllowed(false);
    }

    void showOverlays() {
        setOverlayAllowed(true);
    }

    private void setOverlayAllowed(boolean allowed) {
        if (this.mAppOpsManager != null) {
            this.mAppOpsManager.setUserRestriction(24, allowed ^ 1, this.mToken);
            this.mAppOpsManager.setUserRestriction(45, allowed ^ 1, this.mToken);
        }
    }
}

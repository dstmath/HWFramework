package com.huawei.android.app;

import android.app.StatusBarManager;
import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;
import com.android.internal.statusbar.IStatusBarService;

public class StatusBarManagerExt {
    private StatusBarManager mStatusBarManager;
    private IStatusBarService mStatusBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));

    public StatusBarManagerExt() {
    }

    public StatusBarManagerExt(Context context) {
        this.mStatusBarManager = (StatusBarManager) context.getSystemService("statusbar");
    }

    public void expandNotificationsPanel() {
        StatusBarManager statusBarManager = this.mStatusBarManager;
        if (statusBarManager != null) {
            statusBarManager.expandNotificationsPanel();
        }
    }

    public IBinder asBinder() {
        IStatusBarService iStatusBarService = this.mStatusBarService;
        if (iStatusBarService != null) {
            return iStatusBarService.asBinder();
        }
        return null;
    }
}

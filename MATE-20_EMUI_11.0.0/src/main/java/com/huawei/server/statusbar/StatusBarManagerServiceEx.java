package com.huawei.server.statusbar;

import android.os.ServiceManager;
import com.android.server.statusbar.StatusBarManagerService;

public class StatusBarManagerServiceEx {
    private StatusBarManagerService mStatusBarManagerService = ServiceManager.getService("statusbar");

    public void collapsePanels() {
        StatusBarManagerService statusBarManagerService = this.mStatusBarManagerService;
        if (statusBarManagerService != null) {
            statusBarManagerService.collapsePanels();
        }
    }
}

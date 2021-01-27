package com.huawei.server.statusbar;

import android.os.Bundle;
import android.util.Log;
import com.android.server.LocalServices;
import com.android.server.statusbar.StatusBarManagerInternal;

public class StatusBarManagerInternalEx {
    private static final String TAG = "StatusBarManagerInternalEx";

    public static void startAssist(Bundle bundle) {
        StatusBarManagerInternal statusBarService = (StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class);
        if (statusBarService == null || bundle == null) {
            Log.e(TAG, "Failed to execute startAssist");
        } else {
            statusBarService.startAssist(bundle);
        }
    }

    public static void toggleSplitScreen() {
        StatusBarManagerInternal statusBarService = (StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class);
        if (statusBarService != null) {
            statusBarService.toggleSplitScreen();
        } else {
            Log.e(TAG, "Failed to execute toggleSplitScreen");
        }
    }
}

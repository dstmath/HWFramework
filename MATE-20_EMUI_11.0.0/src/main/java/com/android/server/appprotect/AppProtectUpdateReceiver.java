package com.android.server.appprotect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Slog;
import com.android.server.pm.HwPackageManagerServiceUtils;

public class AppProtectUpdateReceiver extends BroadcastReceiver {
    private static final String ACTION_CFG_UPDATED = "huawei.android.hwouc.intent.action.CFG_UPDATED";
    private static final String TAG = "AppProtectUpdateReceiver";

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            Slog.e(TAG, "HwAppActControlReceiver return null.");
        } else if (ACTION_CFG_UPDATED.equals(intent.getAction())) {
            Slog.i(TAG, "receiver update action ACTION_CFG_UPDATED");
            AppProtectControlUtil instance = AppProtectControlUtil.getInstance();
            instance.readFromXml();
            if (HwPackageManagerServiceUtils.DEBUG_FLAG) {
                Slog.i(TAG, "forbidUninstallSet is " + instance.forbidUninstallSet + ",forbidUpdateSet is " + instance.forbidUpdateSet + ",forbidDisablehashMap is " + instance.forbidDisablehashMap);
            }
        }
    }
}

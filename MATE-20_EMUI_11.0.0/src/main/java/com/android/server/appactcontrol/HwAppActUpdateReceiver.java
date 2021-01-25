package com.android.server.appactcontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Slog;

public class HwAppActUpdateReceiver extends BroadcastReceiver {
    private static final String ACTION_CFG_UPDATED = "huawei.android.hwouc.intent.action.CFG_UPDATED";
    private static final String TAG = "HwAppActUpdateReceiver";

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            Slog.e(TAG, "HwAppActControlReceiver return null.");
        } else if (ACTION_CFG_UPDATED.equals(intent.getAction())) {
            Slog.i(TAG, "receiver update action ACTION_CFG_UPDATED");
            HwAppActController.getInstance().updateAppActControllerDataByOuc();
            ComponentHiddenScenes.getInstance().setComponentHiddenState();
        }
    }
}

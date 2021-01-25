package com.huawei.server.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class HwCameraWhiteListUpdateReceiver extends BroadcastReceiver {
    private static final String ACTION_CLOUD_UPDATE = "huawei.android.hwouc.intent.action.CFG_UPDATED";
    private static final String TAG = "HwCameraAutoImpl";

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "PopupWhitelistUpdateReceive enter.");
        if (context == null || intent == null) {
            Log.i(TAG, "PopupWhitelistUpdateReceive return null.");
        } else if (ACTION_CLOUD_UPDATE.equals(intent.getAction()) && HwCameraAutoImpl.isAutoImplInit()) {
            Log.i(TAG, "receiver update action ACTION_CFG_UPDATED");
            HwCameraAutoImpl.getInstance(context).updateWhiteList();
        }
    }
}

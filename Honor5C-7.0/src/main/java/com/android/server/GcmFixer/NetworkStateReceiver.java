package com.android.server.GcmFixer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.android.server.HwConnectivityService;

public class NetworkStateReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (HwConnectivityService.CONNECTIVITY_CHANGE_ACTION.equals(intent.getAction())) {
                GcmHeartBeatFixer.scheduleHeartbeatRequest(context, true, true);
            } else if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                GcmHeartBeatFixer.scheduleHeartbeatRequest(context, false, true);
            }
        }
    }
}

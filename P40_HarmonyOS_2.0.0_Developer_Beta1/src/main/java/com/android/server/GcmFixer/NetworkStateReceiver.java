package com.android.server.GcmFixer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.android.server.intellicom.common.SmartDualCardConsts;

public class NetworkStateReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (SmartDualCardConsts.ACTION_CONNECTIVITY_CHANGE.equals(intent.getAction())) {
                GcmHeartBeatFixer.scheduleHeartbeatRequest(context, true, true);
            } else if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                GcmHeartBeatFixer.scheduleHeartbeatRequest(context, false, true);
            }
        }
    }
}

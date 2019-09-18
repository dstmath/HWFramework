package com.android.server.GcmFixer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Slog;

public class HeartbeatReceiver extends BroadcastReceiver {
    private static final Intent GTALK_HEART_BEAT_INTENT = new Intent("com.google.android.intent.action.GTALK_HEARTBEAT");
    public static final String HEARTBEAT_FIXER_ACTION = "com.android.intent.action.HEARTBEAT_FIXER";
    private static final Intent MCS_MCS_HEARTBEAT_INTENT = new Intent("com.google.android.intent.action.MCS_HEARTBEAT");
    private static final String TAG = "HeartbeatReceiver";

    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(GTALK_HEART_BEAT_INTENT);
        context.sendBroadcast(MCS_MCS_HEARTBEAT_INTENT);
        Slog.i(TAG, "Sent heartbeat request...");
        GcmHeartBeatFixer.scheduleHeartbeatRequest(context, false, true);
    }
}

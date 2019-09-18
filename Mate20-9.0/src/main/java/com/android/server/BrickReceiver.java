package com.android.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemService;
import android.util.Slog;

public class BrickReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Slog.w("BrickReceiver", "!!! BRICKING DEVICE !!!");
        SystemService.start("brick");
    }
}

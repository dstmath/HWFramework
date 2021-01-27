package com.huawei.server.display;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Slog;

public class ColorCastReceiver extends BroadcastReceiver {
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String TAG = "ColorCastReceiver";

    public ColorCastReceiver() {
        if (HWFLOW) {
            Slog.i(TAG, TAG);
        }
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (HWFLOW) {
            Slog.i(TAG, "onReceive start");
        }
        if (HWFLOW) {
            Slog.i(TAG, "onReceive end");
        }
    }
}

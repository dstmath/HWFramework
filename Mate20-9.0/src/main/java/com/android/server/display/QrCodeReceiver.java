package com.android.server.display;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Slog;

public class QrCodeReceiver extends BroadcastReceiver {
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String TAG = "QrCodeReceiver";

    public QrCodeReceiver() {
        if (HWFLOW) {
            Slog.i(TAG, TAG);
        }
    }

    public void onReceive(Context context, Intent intent) {
        if (HWFLOW) {
            Slog.i(TAG, "onReceive start");
        }
        HwPgSceneDetectionAppName.loadCotaConfig();
        if (HWFLOW) {
            Slog.i(TAG, "onReceive end");
        }
    }
}

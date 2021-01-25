package com.android.server.appactcontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Slog;
import java.lang.Thread;

public class HwAppActBootReceiver extends BroadcastReceiver {
    private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    private static final String TAG = "HwAppActBootReceiver";

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            Slog.e(TAG, "HwAppActBootReceiver return null.");
        } else if (HwAppActController.getInstance().isAppControlPolicyExists() && ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Thread thread = new Thread(new Runnable() {
                /* class com.android.server.appactcontrol.HwAppActBootReceiver.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    ComponentHiddenScenes.getInstance().setComponentHiddenState();
                }
            });
            thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                /* class com.android.server.appactcontrol.HwAppActBootReceiver.AnonymousClass2 */

                @Override // java.lang.Thread.UncaughtExceptionHandler
                public void uncaughtException(Thread t, Throwable e) {
                    Slog.e(HwAppActBootReceiver.TAG, "HwAppActBootReceiver Thread uncaughtException.");
                }
            });
            thread.setName("HwAppActBootThread");
            thread.start();
        }
    }
}

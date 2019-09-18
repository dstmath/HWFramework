package com.android.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.ConditionVariable;
import android.os.RecoverySystem;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import java.io.IOException;
import java.util.Locale;

public class HwRecoverySystemHelper {
    private static final String TAG = "HwRecoverySystemHelper";

    public static boolean clearWipeDataFactoryLowlevel(Context mContext, Intent mIntent) {
        final Context context = mContext;
        Intent intent = mIntent;
        if (!intent.getBooleanExtra("masterClearWipeDataFactoryLowlevel", false)) {
            return false;
        }
        final boolean wipeEuicc = intent.getBooleanExtra("wipeEuicc", false);
        new Thread("Reboot") {
            public void run() {
                try {
                    HwRecoverySystemHelper.rebootWipeUserDataFactoryLowlevel(context, wipeEuicc);
                    Log.wtf(HwRecoverySystemHelper.TAG, "Still running after master clear?!-rebootWipeUserDataFactoryLowlevel");
                } catch (IOException e) {
                    Slog.e(HwRecoverySystemHelper.TAG, "Can't perform master clear/factory reset", e);
                }
            }
        }.start();
        return true;
    }

    public static boolean clearWipeDataFactory(Context mContext, Intent mIntent) {
        final Context context = mContext;
        if (!mIntent.getBooleanExtra("masterClearWipeDataFactory", false)) {
            return false;
        }
        new Thread("Reboot") {
            public void run() {
                try {
                    HwRecoverySystemHelper.rebootWipeUserDataFactory(context);
                    Log.wtf(HwRecoverySystemHelper.TAG, "Still running after master clear?!-rebootWipeUserDataFactory");
                } catch (IOException e) {
                    Slog.e(HwRecoverySystemHelper.TAG, "Can't perform master clear/factory reset", e);
                }
            }
        }.start();
        return true;
    }

    /* access modifiers changed from: private */
    public static void rebootWipeUserDataFactoryLowlevel(Context context, boolean wipeEuicc) throws IOException {
        final ConditionVariable condition = new ConditionVariable();
        Intent intent = new Intent("android.intent.action.MASTER_CLEAR_NOTIFICATION");
        intent.addFlags(268435456);
        context.sendOrderedBroadcastAsUser(intent, UserHandle.OWNER, "android.permission.MASTER_CLEAR", new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                condition.open();
            }
        }, null, 0, null, null);
        condition.block();
        if (wipeEuicc) {
            RecoverySystem.hwWipeEuiccData(context);
        }
        Log.d(TAG, "out rebootWipeUserDataFactoryLowlevel");
        RecoverySystem.hwBootCommand(context, "--wipe_data_factory_lowlevel\n--reset_enter:101\n--locale=" + Locale.getDefault().toString());
    }

    public static void rebootWipeUserDataFactory(Context context) throws IOException {
        final ConditionVariable condition = new ConditionVariable();
        context.sendOrderedBroadcastAsUser(new Intent("android.intent.action.MASTER_CLEAR_NOTIFICATION"), UserHandle.OWNER, "android.permission.MASTER_CLEAR", new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                condition.open();
            }
        }, null, 0, null, null);
        condition.block();
        Log.d(TAG, "out rebootWipeUserDataFactory");
        RecoverySystem.hwBootCommand(context, "--wipe_data_factory_reset\n--reset_enter:141\n--locale=" + Locale.getDefault().toString());
    }
}

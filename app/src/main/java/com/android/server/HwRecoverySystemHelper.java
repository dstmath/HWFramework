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

    /* renamed from: com.android.server.HwRecoverySystemHelper.1 */
    static class AnonymousClass1 extends Thread {
        final /* synthetic */ Context val$context;

        AnonymousClass1(String $anonymous0, Context val$context) {
            this.val$context = val$context;
            super($anonymous0);
        }

        public void run() {
            try {
                HwRecoverySystemHelper.rebootWipeUserDataFactoryLowlevel(this.val$context);
                Log.wtf(HwRecoverySystemHelper.TAG, "Still running after master clear?!-rebootWipeUserDataFactoryLowlevel");
            } catch (IOException e) {
                Slog.e(HwRecoverySystemHelper.TAG, "Can't perform master clear/factory reset", e);
            }
        }
    }

    /* renamed from: com.android.server.HwRecoverySystemHelper.2 */
    static class AnonymousClass2 extends Thread {
        final /* synthetic */ Context val$context;

        AnonymousClass2(String $anonymous0, Context val$context) {
            this.val$context = val$context;
            super($anonymous0);
        }

        public void run() {
            try {
                HwRecoverySystemHelper.rebootWipeUserDataFactory(this.val$context);
                Log.wtf(HwRecoverySystemHelper.TAG, "Still running after master clear?!-rebootWipeUserDataFactory");
            } catch (IOException e) {
                Slog.e(HwRecoverySystemHelper.TAG, "Can't perform master clear/factory reset", e);
            }
        }
    }

    /* renamed from: com.android.server.HwRecoverySystemHelper.3 */
    static class AnonymousClass3 extends BroadcastReceiver {
        final /* synthetic */ ConditionVariable val$condition;

        AnonymousClass3(ConditionVariable val$condition) {
            this.val$condition = val$condition;
        }

        public void onReceive(Context context, Intent intent) {
            this.val$condition.open();
        }
    }

    /* renamed from: com.android.server.HwRecoverySystemHelper.4 */
    static class AnonymousClass4 extends BroadcastReceiver {
        final /* synthetic */ ConditionVariable val$condition;

        AnonymousClass4(ConditionVariable val$condition) {
            this.val$condition = val$condition;
        }

        public void onReceive(Context context, Intent intent) {
            this.val$condition.open();
        }
    }

    public static boolean clearWipeDataFactoryLowlevel(Context mContext, Intent mIntent) {
        Context context = mContext;
        Intent intent = mIntent;
        if (!mIntent.getBooleanExtra("masterClearWipeDataFactoryLowlevel", false)) {
            return false;
        }
        new AnonymousClass1("Reboot", mContext).start();
        return true;
    }

    public static boolean clearWipeDataFactory(Context mContext, Intent mIntent) {
        Context context = mContext;
        Intent intent = mIntent;
        if (!mIntent.getBooleanExtra("masterClearWipeDataFactory", false)) {
            return false;
        }
        new AnonymousClass2("Reboot", mContext).start();
        return true;
    }

    private static void rebootWipeUserDataFactoryLowlevel(Context context) throws IOException {
        ConditionVariable condition = new ConditionVariable();
        Intent intent = new Intent("android.intent.action.MASTER_CLEAR_NOTIFICATION");
        intent.addFlags(268435456);
        context.sendOrderedBroadcastAsUser(intent, UserHandle.OWNER, "android.permission.MASTER_CLEAR", new AnonymousClass3(condition), null, 0, null, null);
        condition.block();
        Log.d(TAG, "out rebootWipeUserDataFactoryLowlevel");
        RecoverySystem.hwBootCommand(context, "--wipe_data_factory_lowlevel\n--locale=" + Locale.getDefault().toString());
    }

    public static void rebootWipeUserDataFactory(Context context) throws IOException {
        ConditionVariable condition = new ConditionVariable();
        context.sendOrderedBroadcastAsUser(new Intent("android.intent.action.MASTER_CLEAR_NOTIFICATION"), UserHandle.OWNER, "android.permission.MASTER_CLEAR", new AnonymousClass4(condition), null, 0, null, null);
        condition.block();
        Log.d(TAG, "out rebootWipeUserDataFactory");
        RecoverySystem.hwBootCommand(context, "--wipe_data_factory_reset\n--locale=" + Locale.getDefault().toString());
    }
}

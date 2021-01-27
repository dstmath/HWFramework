package com.android.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.os.RecoverySystem;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Slog;
import com.huawei.android.os.SystemPropertiesEx;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class HwRecoverySystemHelper {
    private static final String BLOCK_MAIN_SLOT_SWITCH = "block_main_slot_switch";
    private static final String DEVICE_PROPER_CONFIG = "ro.ril.esim_type";
    private static final int DEVICE_TYPE_DUAL_SIM_AND_ESIM = 3;
    private static final int DEVICE_TYPE_NONE_ESIM = 0;
    private static final int DEVICE_TYPE_SINGLE_SIM_AND_ESIM = 2;
    private static final int ESIM_SEND_NOTIFICATION_DOING = 0;
    private static final int ESIM_SEND_NOTIFICATION_DONE = 1;
    private static final String ESIM_SEND_NOTIFICATION_STATUS = "esim_send_notification_status";
    private static final int HW_CARD_TYPE_ESIM = 1;
    private static final int HW_CARD_TYPE_SIM = 0;
    private static final int HW_ERASE_EUICC_WAIT_TIME_MILLIS = 2000;
    private static final int HW_MAX_ERASE_EUICC_TIMES = 2;
    private static final int HW_MAX_SWITCH_SLOT_TIMES = 5;
    private static final int HW_NOTIFICATION_SENDING_MILLIS = 10000;
    private static final int HW_SLOT_CARD_SECOND = 1;
    private static final int HW_SWITCH_SLOT_WAIT_TIME_MILLIS = 2000;
    private static final int MAIN_SLOT_SWITCH_DISABLED = 1;
    private static final int MAIN_SLOT_SWITCH_ENABLED = 0;
    private static final String TAG = "HwRecoverySystemHelper";

    public static boolean clearWipeDataFactoryLowlevel(final Context paramContext, Intent paramIntent) {
        if (!paramIntent.getBooleanExtra("masterClearWipeDataFactoryLowlevel", false)) {
            return false;
        }
        final boolean wipeEuicc = paramIntent.getBooleanExtra("com.android.internal.intent.extra.WIPE_ESIMS", false);
        new Thread("Reboot") {
            /* class com.android.server.HwRecoverySystemHelper.AnonymousClass1 */

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                try {
                    HwRecoverySystemHelper.rebootWipeUserDataFactoryLowlevel(paramContext, wipeEuicc);
                    Log.wtf(HwRecoverySystemHelper.TAG, "Still running after master clear?!-rebootWipeUserDataFactoryLowlevel");
                } catch (IOException e) {
                    Slog.e(HwRecoverySystemHelper.TAG, "Can't perform master clear/factory reset", e);
                }
            }
        }.start();
        return true;
    }

    public static boolean clearWipeDataFactory(final Context paramContext, Intent paramIntent) {
        if (!paramIntent.getBooleanExtra("masterClearWipeDataFactory", false)) {
            return false;
        }
        new Thread("Reboot") {
            /* class com.android.server.HwRecoverySystemHelper.AnonymousClass2 */

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                try {
                    HwRecoverySystemHelper.rebootWipeUserDataFactory(paramContext);
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
            /* class com.android.server.HwRecoverySystemHelper.AnonymousClass3 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                condition.open();
            }
        }, null, 0, null, null);
        condition.block();
        if (wipeEuicc) {
            hwWipeEuiccDatas(context);
        }
        Log.d(TAG, "out rebootWipeUserDataFactoryLowlevel");
        RecoverySystem.hwBootCommand(context, "--wipe_data_factory_lowlevel\n--reset_enter:101\n--locale=" + Locale.getDefault().toString());
    }

    public static void rebootWipeUserDataFactory(Context context) throws IOException {
        final ConditionVariable condition = new ConditionVariable();
        context.sendOrderedBroadcastAsUser(new Intent("android.intent.action.MASTER_CLEAR_NOTIFICATION"), UserHandle.OWNER, "android.permission.MASTER_CLEAR", new BroadcastReceiver() {
            /* class com.android.server.HwRecoverySystemHelper.AnonymousClass4 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                condition.open();
            }
        }, null, 0, null, null);
        condition.block();
        Log.d(TAG, "out rebootWipeUserDataFactory");
        RecoverySystem.hwBootCommand(context, "--wipe_data_factory_reset\n--reset_enter:141\n--locale=" + Locale.getDefault().toString());
    }

    private static void hwWipeEuiccDatas(Context context) {
        Log.d(TAG, "enter hwWipeEuiccDatas");
        if (!isHwEsimPhone()) {
            RecoverySystem.hwWipeEuiccData(context);
            return;
        }
        hwSetMainSlotSwitchStatus(context, 1);
        if (isNetworkConnected(context)) {
            hwPhoneSendNotification(context);
        } else {
            hwPhoneWipeEuiccDatas(context);
        }
        hwSwitchSlotToOrigin();
        hwSetMainSlotSwitchStatus(context, 0);
    }

    private static boolean isHwEsimPhone() {
        int deviceType = SystemPropertiesEx.getInt(DEVICE_PROPER_CONFIG, 0);
        Log.i(TAG, "recovery esim deviceType = " + deviceType);
        if (deviceType == 3 || deviceType == 2) {
            return true;
        }
        return false;
    }

    private static void hwSwitchSlotToOrigin() {
        if (SystemPropertiesEx.getInt(DEVICE_PROPER_CONFIG, 0) == 3) {
            hwSwitchSlots(new int[]{1, 0});
            Log.d(TAG, "switch card slot to SIM successfully");
        }
    }

    private static void hwSwitchSlots(int[] physicalSlots) {
        for (int i = 0; i < 5; i++) {
            CountDownLatch switchS = new CountDownLatch(1);
            boolean isSwitched = TelephonyManager.getDefault().switchSlots(physicalSlots);
            try {
                switchS.await(2000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Log.e(TAG, "hwSwitchSlots interrupted");
            }
            if (isSwitched) {
                return;
            }
        }
    }

    private static void hwSetMainSlotSwitchStatus(Context context, int status) {
        if (context != null) {
            Log.d(TAG, "hwSetMainSlotSwitchStatus staus = " + status);
            Settings.System.putInt(context.getContentResolver(), BLOCK_MAIN_SLOT_SWITCH, status);
        }
    }

    private static void setSendingNotificationStatus(Context context, int status) {
        if (context != null) {
            Log.d(TAG, "setSendingNotificationStatus staus = " + status);
            Settings.System.putInt(context.getContentResolver(), ESIM_SEND_NOTIFICATION_STATUS, status);
        }
    }

    private static void hwPhoneSendNotification(final Context context) {
        Log.d(TAG, "hwPhoneSendNotification enter");
        final CountDownLatch notifiLatch = new CountDownLatch(1);
        setSendingNotificationStatus(context, 0);
        ContentObserver notifiContentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            /* class com.android.server.HwRecoverySystemHelper.AnonymousClass5 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                int status = Settings.System.getInt(context.getContentResolver(), HwRecoverySystemHelper.ESIM_SEND_NOTIFICATION_STATUS, 0);
                Log.d(HwRecoverySystemHelper.TAG, "send notification status = " + status);
                if (status == 1) {
                    Log.d(HwRecoverySystemHelper.TAG, "send notification done");
                    notifiLatch.countDown();
                }
            }
        };
        context.getContentResolver().registerContentObserver(Settings.System.getUriFor(ESIM_SEND_NOTIFICATION_STATUS), true, notifiContentObserver);
        hwPhoneWipeEuiccDatas(context);
        try {
            if (!notifiLatch.await(10000, TimeUnit.MILLISECONDS)) {
                Log.d(TAG, "Settings.System.getInt timeout");
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "notifiLatch interrupted");
        } catch (Throwable th) {
            context.getContentResolver().unregisterContentObserver(notifiContentObserver);
            Log.d(TAG, "Settings.System.getInt unregisterContentObserver");
            throw th;
        }
        context.getContentResolver().unregisterContentObserver(notifiContentObserver);
        Log.d(TAG, "Settings.System.getInt unregisterContentObserver");
    }

    private static void hwPhoneWipeEuiccDatas(Context context) {
        if (!HwTelephonyManager.getDefault().isEuicc(1)) {
            Log.d(TAG, "switch to euicc if the card slot is SIM current.");
            hwSwitchSlots(new int[]{1, 1});
        }
        for (int i = 0; i < 2; i++) {
            if (RecoverySystem.hwWipeEuiccData(context)) {
                Log.d(TAG, "RecoverySystem.hwWipeEuiccData success. times = " + i);
                return;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.d(TAG, "thread exception");
            }
        }
    }

    public static boolean isNetworkConnected(Context context) {
        NetworkInfo info;
        if (context == null || (info = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo()) == null || !info.isAvailable()) {
            return false;
        }
        return true;
    }
}

package com.android.server.power;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Slog;
import android.view.Window;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.huawei.android.content.IntentExEx;

public class HwAutoPowerOffController {
    private static final String CHANNEL_ID = "auto_power_off";
    private static final int DEFAULT_COUNTDOWN = 180;
    private static final int MSG_COUNTDOWN_CHANGED = 2;
    private static final int MSG_COUNTDOWN_CHANGED_TIME = 1000;
    private static final int MSG_DISMISS_DIALOG = 1;
    private static final int MSG_SHOW_DIALOG = 0;
    private static final int POWER_OFF_NOTIFICATION_ID = 1;
    private static final String TAG = "HwAutoPowerOffController";
    private Notification.Builder mBuilder;
    private Context mContext;
    private int mDelayTime;
    private AlertDialog mDialog = null;
    private Handler mHandler = new Handler() {
        /* class com.android.server.power.HwAutoPowerOffController.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                HwAutoPowerOffController.this.showDialog();
                HwAutoPowerOffController.this.mHandler.sendEmptyMessage(2);
            } else if (i == 1) {
                HwAutoPowerOffController.this.dismissDialog();
            } else if (i == 2) {
                HwAutoPowerOffController.this.updateDialogMessage();
                HwAutoPowerOffController.access$410(HwAutoPowerOffController.this);
                if (HwAutoPowerOffController.this.mDelayTime >= 0) {
                    HwAutoPowerOffController.this.mHandler.sendEmptyMessageDelayed(2, 1000);
                } else {
                    HwAutoPowerOffController.this.dismissDialog();
                    HwAutoPowerOffController.this.intentToShutdown();
                }
            }
            super.handleMessage(msg);
        }
    };
    private Notification mNotification;
    private NotificationManager mNotificationManager;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock = null;

    static /* synthetic */ int access$410(HwAutoPowerOffController x0) {
        int i = x0.mDelayTime;
        x0.mDelayTime = i - 1;
        return i;
    }

    public HwAutoPowerOffController(Context context) {
        Slog.d(TAG, "new HwAutoPowerOffController ()");
        if (context == null) {
            Slog.e(TAG, "context is null");
            return;
        }
        this.mContext = context;
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mNotificationManager = (NotificationManager) context.getSystemService(NotificationManager.class);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dismissDialog() {
        this.mHandler.removeCallbacksAndMessages(null);
        releaseWakeLock();
        try {
            if (this.mDialog != null && this.mDialog.isShowing()) {
                this.mDialog.dismiss();
                this.mDialog = null;
            }
            if (this.mNotificationManager != null) {
                this.mNotificationManager.cancel(1);
                this.mNotification = null;
                this.mBuilder = null;
                this.mNotificationManager.deleteNotificationChannel(CHANNEL_ID);
            }
        } catch (IllegalArgumentException e) {
            Slog.e(TAG, "the window has been detached!");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showDialog() {
        this.mDelayTime = 180;
        createDialogIfNeeded();
        acquireWakeLock();
        if (!this.mDialog.isShowing()) {
            this.mDialog.show();
        }
        NotificationManager notificationManager = this.mNotificationManager;
        if (notificationManager != null) {
            notificationManager.notify(1, this.mNotification);
        }
    }

    private void createDialogIfNeeded() {
        if (this.mDialog == null) {
            this.mDialog = createDialog(this.mContext, getDialogMessage(this.mDelayTime));
        }
        if (this.mNotificationManager == null) {
            this.mNotificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
        }
        if (this.mNotification == null && this.mNotificationManager != null) {
            this.mNotification = createNotification(getDialogMessage(this.mDelayTime));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void intentToShutdown() {
        Slog.d(TAG, "Charger has disconnected over 3 minutes, shutdown device now");
        Intent intent = new Intent(IntentExEx.getActionRequestShutdown());
        intent.putExtra(IntentExEx.getExtraKeyConfirm(), false);
        intent.setFlags(268435456);
        intent.addHwFlags(65536);
        try {
            this.mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Slog.d(TAG, "start shutdown device activity falied");
        }
    }

    private AlertDialog createDialog(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, 33947691);
        builder.setTitle(33685531);
        builder.setMessage(message);
        builder.setNegativeButton(17039360, (DialogInterface.OnClickListener) null);
        builder.setPositiveButton(33685530, new DialogInterface.OnClickListener() {
            /* class com.android.server.power.HwAutoPowerOffController.AnonymousClass2 */

            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int which) {
                HwAutoPowerOffController.this.intentToShutdown();
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            /* class com.android.server.power.HwAutoPowerOffController.AnonymousClass3 */

            @Override // android.content.DialogInterface.OnDismissListener
            public void onDismiss(DialogInterface dialog) {
                HwAutoPowerOffController.this.dismissDialog();
            }
        });
        this.mDialog = builder.create();
        this.mDialog.setCancelable(false);
        Window dialogWindow = this.mDialog.getWindow();
        if (dialogWindow != null) {
            dialogWindow.setType(HwArbitrationDEFS.MSG_RECOVERY_FLAG_BY_WIFI_RX_BYTES);
        }
        return this.mDialog;
    }

    private Notification createNotification(String message) {
        String notificationTitle = this.mContext.getResources().getString(33685531);
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, notificationTitle, 3);
        channel.enableLights(true);
        this.mNotificationManager.createNotificationChannel(channel);
        channel.setName(notificationTitle);
        this.mBuilder = new Notification.Builder(this.mContext, CHANNEL_ID).setContentTitle(notificationTitle).setContentText(message).setWhen(System.currentTimeMillis()).setAutoCancel(true).setSmallIcon(17301552);
        this.mNotification = this.mBuilder.build();
        Notification notification = this.mNotification;
        notification.flags = 8;
        return notification;
    }

    private String getDialogMessage(int countdownNum) {
        return this.mContext.getResources().getQuantityString(34406409, countdownNum, Integer.valueOf(countdownNum));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDialogMessage() {
        createDialogIfNeeded();
        this.mDialog.setMessage(getDialogMessage(this.mDelayTime));
        if (!this.mDialog.isShowing()) {
            this.mDialog.show();
        }
        Notification.Builder builder = this.mBuilder;
        if (builder != null && this.mNotificationManager != null) {
            builder.setContentText(getDialogMessage(this.mDelayTime));
            this.mNotificationManager.notify(1, this.mNotification);
        }
    }

    private void acquireWakeLock() {
        Slog.d(TAG, "acquireWakeLock()");
        if (this.mWakeLock == null) {
            this.mWakeLock = this.mPowerManager.newWakeLock(1, TAG);
            PowerManager.WakeLock wakeLock = this.mWakeLock;
            if (wakeLock != null) {
                wakeLock.acquire();
            }
        }
    }

    private void releaseWakeLock() {
        Slog.d(TAG, "releaseWakeLock()");
        PowerManager.WakeLock wakeLock = this.mWakeLock;
        if (wakeLock != null) {
            wakeLock.release();
            this.mWakeLock = null;
        }
    }

    public void startAutoPowerOff() {
        Slog.d(TAG, "startAutoPowerOff()");
        this.mHandler.sendEmptyMessage(0);
    }

    public void stopAutoPowerOff() {
        Slog.d(TAG, "stopAutoPowerOff");
        this.mHandler.sendEmptyMessage(1);
    }
}

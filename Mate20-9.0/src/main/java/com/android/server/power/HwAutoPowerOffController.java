package com.android.server.power;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Slog;
import android.view.Window;
import com.huawei.android.content.IntentExEx;

public class HwAutoPowerOffController {
    private static final int DEFAULT_COUNTDOWN = 180;
    private static final int MSG_COUNTDOWN_CHANGED = 2;
    private static final int MSG_DISMISS_DIALOG = 1;
    private static final int MSG_SHOW_DIALOG = 0;
    private static final String TAG = "HwAutoPowerOffController";
    private Context mContext;
    /* access modifiers changed from: private */
    public int mDelayTime;
    private AlertDialog mDialog = null;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    HwAutoPowerOffController.this.showDialog();
                    HwAutoPowerOffController.this.mHandler.sendEmptyMessage(2);
                    break;
                case 1:
                    HwAutoPowerOffController.this.dismissDialog();
                    break;
                case 2:
                    HwAutoPowerOffController.this.updateDialogMessage();
                    HwAutoPowerOffController.access$410(HwAutoPowerOffController.this);
                    if (HwAutoPowerOffController.this.mDelayTime < 0) {
                        HwAutoPowerOffController.this.dismissDialog();
                        HwAutoPowerOffController.this.intentToShutdown();
                        break;
                    } else {
                        HwAutoPowerOffController.this.mHandler.sendEmptyMessageDelayed(2, 1000);
                        break;
                    }
            }
            super.handleMessage(msg);
        }
    };
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
            Slog.d(TAG, "context is null");
            return;
        }
        this.mContext = context;
        this.mPowerManager = (PowerManager) context.getSystemService("power");
    }

    /* access modifiers changed from: private */
    public void dismissDialog() {
        this.mHandler.removeCallbacksAndMessages(null);
        releaseWakeLock();
        try {
            if (this.mDialog != null && this.mDialog.isShowing()) {
                this.mDialog.dismiss();
                this.mDialog = null;
            }
        } catch (IllegalArgumentException e) {
            Slog.e(TAG, "the window has been detached!");
        }
    }

    /* access modifiers changed from: private */
    public void showDialog() {
        this.mDelayTime = 180;
        createDialogIfNeeded();
        acquireWakeLock();
        if (!this.mDialog.isShowing()) {
            this.mDialog.show();
        }
    }

    private void createDialogIfNeeded() {
        if (this.mDialog == null) {
            this.mDialog = createDialog(this.mContext, getDialogMessage(this.mDelayTime));
        }
    }

    /* access modifiers changed from: private */
    public void intentToShutdown() {
        Slog.d(TAG, "Charger has disconnected over 3 minutes, shutdown device now");
        Intent intent = new Intent(IntentExEx.getActionRequestShutdown());
        intent.putExtra(IntentExEx.getExtraKeyConfirm(), false);
        intent.setFlags(268435456);
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
        builder.setNegativeButton(17039360, null);
        builder.setPositiveButton(33685530, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                HwAutoPowerOffController.this.intentToShutdown();
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                HwAutoPowerOffController.this.dismissDialog();
            }
        });
        this.mDialog = builder.create();
        this.mDialog.setCancelable(false);
        Window dialogWindow = this.mDialog.getWindow();
        if (dialogWindow != null) {
            dialogWindow.setType(2038);
        }
        return this.mDialog;
    }

    private String getDialogMessage(int countdownNum) {
        return this.mContext.getResources().getQuantityString(34406409, countdownNum, new Object[]{Integer.valueOf(countdownNum)});
    }

    /* access modifiers changed from: private */
    public void updateDialogMessage() {
        createDialogIfNeeded();
        this.mDialog.setMessage(getDialogMessage(this.mDelayTime));
        if (!this.mDialog.isShowing()) {
            this.mDialog.show();
        }
    }

    private void acquireWakeLock() {
        Slog.d(TAG, "acquireWakeLock()");
        if (this.mWakeLock == null) {
            this.mWakeLock = this.mPowerManager.newWakeLock(1, TAG);
            if (this.mWakeLock != null) {
                this.mWakeLock.acquire();
            }
        }
    }

    private void releaseWakeLock() {
        Slog.d(TAG, "releaseWakeLock()");
        if (this.mWakeLock != null) {
            this.mWakeLock.release();
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

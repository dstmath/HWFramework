package com.android.internal.app;

import android.app.Activity;
import android.content.Intent;
import android.os.BatteryManagerInternal;
import android.os.Bundle;
import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Flog;
import android.util.Slog;
import com.android.internal.os.HwBootAnimationOeminfo;
import com.android.server.LocalServices;

public class ShutdownActivity extends Activity {
    private static final String EXTRA_TYPE_SHUTDOWN = "shutdown";
    private static final String EXTRA_VALUES_SHUTDOWN = "prepare_shutdown";
    private static final int NO_REBOOT_CHARGE_FLAG = 1;
    private static final String TAG = "ShutdownActivity";
    /* access modifiers changed from: private */
    public boolean mConfirm;
    /* access modifiers changed from: private */
    public boolean mNoReboot = false;
    /* access modifiers changed from: private */
    public boolean mReboot;
    private boolean mUserRequested;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        final String reason;
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        this.mReboot = "android.intent.action.REBOOT".equals(intent.getAction());
        this.mConfirm = intent.getBooleanExtra("android.intent.extra.KEY_CONFIRM", false);
        this.mUserRequested = intent.getBooleanExtra("android.intent.extra.USER_REQUESTED_SHUTDOWN", false);
        if (this.mUserRequested) {
            reason = "userrequested";
        } else {
            reason = intent.getStringExtra("android.intent.extra.REASON");
        }
        String shutdown = intent.getStringExtra(EXTRA_TYPE_SHUTDOWN);
        if (EXTRA_VALUES_SHUTDOWN.equals(shutdown)) {
            BatteryManagerInternal batteryManagerInternal = (BatteryManagerInternal) LocalServices.getService(BatteryManagerInternal.class);
            if (batteryManagerInternal != null) {
                this.mNoReboot = batteryManagerInternal.isPowered(7);
            }
        }
        Slog.i(TAG, "onCreate(): confirm=" + this.mConfirm + ", mNoReboot=" + this.mNoReboot + ", shutdown=" + shutdown);
        StringBuilder rebootInfo = new StringBuilder();
        rebootInfo.append(TAG);
        rebootInfo.append(" ");
        rebootInfo.append("Action:");
        rebootInfo.append(intent.getAction());
        rebootInfo.append(";");
        rebootInfo.append("Package:");
        rebootInfo.append(intent.getPackage());
        rebootInfo.append(";");
        if (intent.getComponent() != null) {
            rebootInfo.append("Component:");
            rebootInfo.append(intent.getComponent().flattenToShortString());
            rebootInfo.append(";");
        }
        Flog.e(1600, rebootInfo.toString());
        Thread thr = new Thread(TAG) {
            public void run() {
                IPowerManager pm = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
                try {
                    if (ShutdownActivity.this.mReboot) {
                        pm.reboot(ShutdownActivity.this.mConfirm, null, false);
                        return;
                    }
                    if (ShutdownActivity.this.mNoReboot) {
                        ShutdownActivity.this.SetShutdownFlag(1);
                    }
                    pm.shutdown(ShutdownActivity.this.mConfirm, reason, false);
                } catch (RemoteException e) {
                }
            }
        };
        thr.start();
        finish();
        try {
            thr.join();
        } catch (InterruptedException e) {
        }
    }

    /* access modifiers changed from: private */
    public void SetShutdownFlag(int flag) {
        try {
            Slog.d(TAG, "writeBootAnimShutFlag = " + flag);
            if (HwBootAnimationOeminfo.setBootChargeShutFlag(flag) != 0) {
                Slog.e(TAG, "writeBootAnimShutFlag error");
            }
        } catch (Exception ex) {
            Slog.e(TAG, ex.toString());
        }
    }
}

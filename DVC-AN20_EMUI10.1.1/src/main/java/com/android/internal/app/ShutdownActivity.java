package com.android.internal.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.BatteryManagerInternal;
import android.os.Bundle;
import android.os.IPowerManager;
import android.os.PowerManager;
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
    private boolean mConfirm;
    private boolean mNoReboot = false;
    private boolean mReboot;
    private boolean mUserRequested;

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        final String reason;
        BatteryManagerInternal batteryManagerInternal;
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        this.mReboot = Intent.ACTION_REBOOT.equals(intent.getAction());
        this.mConfirm = intent.getBooleanExtra(Intent.EXTRA_KEY_CONFIRM, false);
        this.mUserRequested = intent.getBooleanExtra(Intent.EXTRA_USER_REQUESTED_SHUTDOWN, false);
        if (this.mUserRequested) {
            reason = PowerManager.SHUTDOWN_USER_REQUESTED;
        } else {
            reason = intent.getStringExtra(Intent.EXTRA_REASON);
        }
        String shutdown = intent.getStringExtra(EXTRA_TYPE_SHUTDOWN);
        if (EXTRA_VALUES_SHUTDOWN.equals(shutdown) && (batteryManagerInternal = (BatteryManagerInternal) LocalServices.getService(BatteryManagerInternal.class)) != null) {
            this.mNoReboot = batteryManagerInternal.isPowered(7);
        }
        Slog.i(TAG, "onCreate(): confirm=" + this.mConfirm + ", mNoReboot=" + this.mNoReboot + ", shutdown=" + shutdown);
        StringBuilder rebootInfo = new StringBuilder();
        rebootInfo.append(TAG);
        rebootInfo.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
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
            /* class com.android.internal.app.ShutdownActivity.AnonymousClass1 */

            public void run() {
                IPowerManager pm = IPowerManager.Stub.asInterface(ServiceManager.getService(Context.POWER_SERVICE));
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
    /* access modifiers changed from: public */
    private void SetShutdownFlag(int flag) {
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

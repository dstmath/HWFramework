package com.android.server.am;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Slog;

public class HwCustUserControllerImpl extends HwCustUserController {
    private static final boolean SUPPORT_HW_COTA = SystemProperties.getBoolean("ro.config.hw_cota", false);
    static final String TAG = "HwCustUserController";

    public boolean isUpgrade() {
        IPackageManager pm = AppGlobals.getPackageManager();
        if (pm == null || !SUPPORT_HW_COTA) {
            return false;
        }
        try {
            Slog.d(TAG, "cota upgrade");
            return pm.isDeviceUpgrading();
        } catch (RemoteException e) {
            Slog.e(TAG, "failed to get upgrade info!");
            return false;
        }
    }

    public void sendPreBootBroadcastToManagedProvisioning(int userId, final Runnable onFinish) {
        ActivityManagerService localService;
        ActivityManagerService localService2 = ServiceManager.getService("activity");
        if (localService2 != null) {
            if (localService2.isUserRunning(userId, 0)) {
                if (userId == 0) {
                    Intent preBootBroadcastIntent = new Intent("android.intent.action.PRE_BOOT_COMPLETED");
                    preBootBroadcastIntent.addFlags(301990144);
                    preBootBroadcastIntent.setComponent(new ComponentName("com.android.managedprovisioning", "com.android.managedprovisioning.ota.PreBootListener"));
                    IIntentReceiver resultReceiver = new IIntentReceiver.Stub() {
                        /* class com.android.server.am.HwCustUserControllerImpl.AnonymousClass1 */

                        public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
                            Slog.d(HwCustUserControllerImpl.TAG, "cota upgrade receive ACTION_PRE_BOOT_COMPLETED");
                            onFinish.run();
                        }
                    };
                    synchronized (localService2) {
                        try {
                            localService = localService2;
                            localService2.broadcastIntentLocked((ProcessRecord) null, (String) null, preBootBroadcastIntent, (String) null, resultReceiver, 0, (String) null, (Bundle) null, (String[]) null, -1, (Bundle) null, true, false, ActivityManagerService.MY_PID, 1000, Binder.getCallingUid(), Binder.getCallingPid(), userId);
                            return;
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    }
                }
            }
            Slog.i(TAG, "User " + userId + " is no longer running or sub user; skipping remaining receivers");
            onFinish.run();
        }
    }
}

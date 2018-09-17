package com.android.server.security;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Process;
import android.os.UserHandle;
import android.security.IKeyChainService;
import android.util.Slog;
import com.android.server.DeviceIdleController.LocalService;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.am.HwBroadcastRadarUtil;

public class KeyChainSystemService extends SystemService {
    private static final int KEYCHAIN_IDLE_WHITELIST_DURATION_MS = 30000;
    private static final String TAG = "KeyChainSystemService";
    private final BroadcastReceiver mPackageReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent broadcastIntent) {
            if (broadcastIntent.getPackage() == null) {
                try {
                    Intent intent = new Intent(IKeyChainService.class.getName());
                    ComponentName service = intent.resolveSystemService(KeyChainSystemService.this.getContext().getPackageManager(), 0);
                    if (service != null) {
                        intent.setComponent(service);
                        intent.setAction(broadcastIntent.getAction());
                        KeyChainSystemService.this.startServiceInBackgroundAsUser(intent, UserHandle.of(getSendingUserId()));
                    }
                } catch (RuntimeException e) {
                    Slog.e(KeyChainSystemService.TAG, "Unable to forward package removed broadcast to KeyChain", e);
                }
            }
        }
    };

    public KeyChainSystemService(Context context) {
        super(context);
    }

    public void onStart() {
        IntentFilter packageFilter = new IntentFilter("android.intent.action.PACKAGE_REMOVED");
        packageFilter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
        try {
            getContext().registerReceiverAsUser(this.mPackageReceiver, UserHandle.ALL, packageFilter, null, null);
        } catch (RuntimeException e) {
            Slog.w(TAG, "Unable to register for package removed broadcast", e);
        }
    }

    private void startServiceInBackgroundAsUser(Intent intent, UserHandle user) {
        if (intent.getComponent() != null) {
            ((LocalService) LocalServices.getService(LocalService.class)).addPowerSaveTempWhitelistApp(Process.myUid(), intent.getComponent().getPackageName(), 30000, user.getIdentifier(), false, "keychain");
            getContext().startServiceAsUser(intent, user);
        }
    }
}

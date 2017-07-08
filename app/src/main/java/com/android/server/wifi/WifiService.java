package com.android.server.wifi;

import android.content.Context;
import android.util.Log;
import com.android.server.SystemService;

public final class WifiService extends SystemService {
    private static final String TAG = "WifiService";
    final WifiServiceImpl mImpl;

    public WifiService(Context context) {
        super(context);
        this.mImpl = HwWifiServiceFactory.getHwWifiServiceManager().createHwWifiService(context);
    }

    public void onStart() {
        Log.i(TAG, "Registering wifi");
        publishBinderService("wifi", this.mImpl);
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            this.mImpl.checkAndStartWifi();
        }
    }

    public void onSwitchUser(int userId) {
        this.mImpl.handleUserSwitch(userId);
    }
}

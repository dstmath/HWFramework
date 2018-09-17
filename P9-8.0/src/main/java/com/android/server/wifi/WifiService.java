package com.android.server.wifi;

import android.content.Context;
import android.util.Log;
import com.android.server.SystemService;
import com.android.server.wifi.util.WifiAsyncChannel;

public final class WifiService extends SystemService {
    private static final String TAG = "WifiService";
    final WifiServiceImpl mImpl;

    public WifiService(Context context) {
        super(context);
        this.mImpl = HwWifiServiceFactory.getHwWifiServiceManager().createHwWifiService(context, new WifiInjector(context), new WifiAsyncChannel(TAG));
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

    public void onUnlockUser(int userId) {
        this.mImpl.handleUserUnlock(userId);
    }

    public void onStopUser(int userId) {
        this.mImpl.handleUserStop(userId);
    }
}

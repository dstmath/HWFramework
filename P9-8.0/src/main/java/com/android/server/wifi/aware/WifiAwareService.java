package com.android.server.wifi.aware;

import android.content.Context;
import android.util.Log;
import com.android.server.SystemService;
import com.android.server.wifi.HalDeviceManager;
import com.android.server.wifi.WifiInjector;

public final class WifiAwareService extends SystemService {
    private static final String TAG = "WifiAwareService";
    final WifiAwareServiceImpl mImpl;

    public WifiAwareService(Context context) {
        super(context);
        this.mImpl = new WifiAwareServiceImpl(context);
    }

    public void onStart() {
        Log.i(TAG, "Registering wifiaware");
        publishBinderService("wifiaware", this.mImpl);
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            WifiInjector wifiInjector = WifiInjector.getInstance();
            if (wifiInjector == null) {
                Log.e(TAG, "onBootPhase(PHASE_SYSTEM_SERVICES_READY): NULL injector!");
                return;
            }
            HalDeviceManager halDeviceManager = wifiInjector.getHalDeviceManager();
            halDeviceManager.initialize();
            WifiAwareStateManager wifiAwareStateManager = new WifiAwareStateManager();
            wifiAwareStateManager.setNative(new WifiAwareNativeApi(new WifiAwareNativeManager(wifiAwareStateManager, halDeviceManager, new WifiAwareNativeCallback(wifiAwareStateManager))));
            this.mImpl.start(wifiInjector.getWifiAwareHandlerThread(), wifiAwareStateManager);
        } else if (phase == 1000) {
            this.mImpl.startLate();
        }
    }
}

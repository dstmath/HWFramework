package com.android.server.wifi.aware;

import android.content.Context;
import android.os.HandlerThread;
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

    /* JADX WARNING: type inference failed for: r1v1, types: [android.os.IBinder, com.android.server.wifi.aware.WifiAwareServiceImpl] */
    public void onStart() {
        Log.i(TAG, "Registering wifiaware");
        publishBinderService("wifiaware", this.mImpl);
    }

    public void onBootPhase(int phase) {
        int i = phase;
        if (i == 500) {
            WifiInjector wifiInjector = WifiInjector.getInstance();
            if (wifiInjector == null) {
                Log.e(TAG, "onBootPhase(PHASE_SYSTEM_SERVICES_READY): NULL injector!");
                return;
            }
            HalDeviceManager halDeviceManager = wifiInjector.getHalDeviceManager();
            WifiAwareStateManager wifiAwareStateManager = new WifiAwareStateManager();
            WifiAwareNativeCallback wifiAwareNativeCallback = new WifiAwareNativeCallback(wifiAwareStateManager);
            WifiAwareNativeManager wifiAwareNativeManager = new WifiAwareNativeManager(wifiAwareStateManager, halDeviceManager, wifiAwareNativeCallback);
            WifiAwareNativeApi wifiAwareNativeApi = new WifiAwareNativeApi(wifiAwareNativeManager);
            wifiAwareStateManager.setNative(wifiAwareNativeManager, wifiAwareNativeApi);
            WifiAwareShellCommand wifiAwareShellCommand = new WifiAwareShellCommand();
            wifiAwareShellCommand.register("native_api", wifiAwareNativeApi);
            wifiAwareShellCommand.register("native_cb", wifiAwareNativeCallback);
            wifiAwareShellCommand.register("state_mgr", wifiAwareStateManager);
            HandlerThread awareHandlerThread = wifiInjector.getWifiAwareHandlerThread();
            WifiAwareShellCommand wifiAwareShellCommand2 = wifiAwareShellCommand;
            WifiAwareShellCommand wifiAwareShellCommand3 = wifiAwareShellCommand;
            WifiAwareNativeManager wifiAwareNativeManager2 = wifiAwareNativeManager;
            WifiAwareNativeCallback wifiAwareNativeCallback2 = wifiAwareNativeCallback;
            this.mImpl.start(awareHandlerThread, wifiAwareStateManager, wifiAwareShellCommand2, wifiInjector.getWifiMetrics().getWifiAwareMetrics(), wifiInjector.getWifiPermissionsUtil(), wifiInjector.getWifiPermissionsWrapper(), wifiInjector.getFrameworkFacade(), wifiAwareNativeManager, wifiAwareNativeApi, wifiAwareNativeCallback);
        } else if (i == 1000) {
            this.mImpl.startLate();
        }
    }
}

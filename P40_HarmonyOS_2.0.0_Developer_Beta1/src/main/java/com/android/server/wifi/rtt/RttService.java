package com.android.server.wifi.rtt;

import android.content.Context;
import android.os.HandlerThread;
import android.os.ServiceManager;
import android.util.Log;
import com.android.server.SystemService;
import com.android.server.wifi.HalDeviceManager;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.util.WifiPermissionsUtil;

public class RttService extends SystemService {
    private static final String TAG = "RttService";
    private Context mContext;
    private RttServiceImpl mImpl;

    public RttService(Context context) {
        super(context);
        this.mContext = context;
        this.mImpl = new RttServiceImpl(context);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.wifi.rtt.RttService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v1, types: [com.android.server.wifi.rtt.RttServiceImpl, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void onStart() {
        Log.i(TAG, "Registering wifirtt");
        publishBinderService("wifirtt", this.mImpl);
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            Log.i(TAG, "Starting wifirtt");
            WifiInjector wifiInjector = WifiInjector.getInstance();
            if (wifiInjector == null) {
                Log.e(TAG, "onBootPhase(PHASE_SYSTEM_SERVICES_READY): NULL injector!");
                return;
            }
            HalDeviceManager halDeviceManager = wifiInjector.getHalDeviceManager();
            HandlerThread handlerThread = wifiInjector.getRttHandlerThread();
            WifiPermissionsUtil wifiPermissionsUtil = wifiInjector.getWifiPermissionsUtil();
            RttMetrics rttMetrics = wifiInjector.getWifiMetrics().getRttMetrics();
            this.mImpl.start(handlerThread.getLooper(), wifiInjector.getClock(), ServiceManager.getService("wifiaware"), new RttNative(this.mImpl, halDeviceManager), rttMetrics, wifiPermissionsUtil, wifiInjector.getFrameworkFacade());
        }
    }
}

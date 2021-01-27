package com.android.server.wifi.p2p;

import android.content.Context;
import android.util.Log;
import com.android.server.SystemService;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.WifiInjector;

public final class WifiP2pService extends SystemService {
    private static final String TAG = "WifiP2pService";
    final WifiP2pServiceImpl mImpl;

    public WifiP2pService(Context context) {
        super(context);
        this.mImpl = HwWifiServiceFactory.getHwWifiServiceManager().createHwWifiP2pService(context, WifiInjector.getInstance());
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.wifi.p2p.WifiP2pService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v1, types: [com.android.server.wifi.p2p.WifiP2pServiceImpl, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void onStart() {
        Log.i(TAG, "Registering wifip2p");
        publishBinderService("wifip2p", this.mImpl);
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            this.mImpl.connectivityServiceReady();
        }
    }
}

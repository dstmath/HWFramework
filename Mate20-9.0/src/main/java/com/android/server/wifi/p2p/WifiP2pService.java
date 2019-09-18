package com.android.server.wifi.p2p;

import android.content.Context;
import android.util.Log;
import com.android.server.SystemService;
import com.android.server.wifi.HwWifiServiceFactory;

public final class WifiP2pService extends SystemService {
    private static final String TAG = "WifiP2pService";
    final WifiP2pServiceImpl mImpl;

    public WifiP2pService(Context context) {
        super(context);
        this.mImpl = HwWifiServiceFactory.getHwWifiServiceManager().createHwWifiP2pService(context);
    }

    /* JADX WARNING: type inference failed for: r1v1, types: [com.android.server.wifi.p2p.WifiP2pServiceImpl, android.os.IBinder] */
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

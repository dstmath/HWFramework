package com.android.server.wifi.scanner;

import android.content.Context;
import android.os.HandlerThread;
import android.util.Log;
import com.android.server.SystemService;
import com.android.server.am.BatteryStatsService;
import com.android.server.wifi.WifiInjector;

public class WifiScanningService extends SystemService {
    static final String TAG = "WifiScanningService";
    private final HandlerThread mHandlerThread;
    private final WifiScanningServiceImpl mImpl;

    public WifiScanningService(Context context) {
        super(context);
        Log.i(TAG, "Creating wifiscanner");
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mImpl = new WifiScanningServiceImpl(getContext(), this.mHandlerThread.getLooper(), WifiScannerImpl.DEFAULT_FACTORY, BatteryStatsService.getService(), WifiInjector.getInstance());
    }

    public void onStart() {
        Log.i(TAG, "Publishing wifiscanner");
        publishBinderService("wifiscanner", this.mImpl);
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            Log.i(TAG, "Starting wifiscanner");
            this.mImpl.startService();
        }
    }
}

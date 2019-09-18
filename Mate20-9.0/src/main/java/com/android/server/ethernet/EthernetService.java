package com.android.server.ethernet;

import android.content.Context;
import android.util.Log;
import com.android.server.SystemService;

public final class EthernetService extends SystemService {
    private static final String TAG = "EthernetService";
    final EthernetServiceImpl mImpl;

    public EthernetService(Context context) {
        super(context);
        this.mImpl = new EthernetServiceImpl(context);
    }

    /* JADX WARNING: type inference failed for: r1v1, types: [com.android.server.ethernet.EthernetServiceImpl, android.os.IBinder] */
    public void onStart() {
        Log.i(TAG, "Registering service ethernet");
        publishBinderService("ethernet", this.mImpl);
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            this.mImpl.start();
        }
    }
}

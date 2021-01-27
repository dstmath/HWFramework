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

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.ethernet.EthernetService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v1, types: [com.android.server.ethernet.EthernetServiceImpl, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
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

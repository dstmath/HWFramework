package com.android.server;

import android.content.Context;
import android.util.Log;
import com.android.internal.util.ConcurrentUtils;
import com.android.server.location.ContextHubService;
import java.util.concurrent.Future;

class ContextHubSystemService extends SystemService {
    private static final String TAG = "ContextHubSystemService";
    /* access modifiers changed from: private */
    public ContextHubService mContextHubService;
    private Future<?> mInit;

    public ContextHubSystemService(Context context) {
        super(context);
        this.mInit = SystemServerInitThreadPool.get().submit(new Runnable(context) {
            private final /* synthetic */ Context f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ContextHubSystemService.this.mContextHubService = new ContextHubService(this.f$1);
            }
        }, "Init ContextHubSystemService");
    }

    public void onStart() {
    }

    /* JADX WARNING: type inference failed for: r1v2, types: [com.android.server.location.ContextHubService, android.os.IBinder] */
    public void onBootPhase(int phase) {
        if (phase == 500) {
            Log.d(TAG, "onBootPhase: PHASE_SYSTEM_SERVICES_READY");
            ConcurrentUtils.waitForFutureNoInterrupt(this.mInit, "Wait for ContextHubSystemService init");
            this.mInit = null;
            publishBinderService("contexthub", this.mContextHubService);
        }
    }
}

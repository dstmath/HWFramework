package com.android.server;

import android.content.Context;
import android.util.Log;
import com.android.internal.util.ConcurrentUtils;
import com.android.server.location.ContextHubService;
import java.util.concurrent.Future;

/* access modifiers changed from: package-private */
public class ContextHubSystemService extends SystemService {
    private static final String TAG = "ContextHubSystemService";
    private ContextHubService mContextHubService;
    private Future<?> mInit;

    public ContextHubSystemService(Context context) {
        super(context);
        this.mInit = SystemServerInitThreadPool.get().submit(new Runnable(context) {
            /* class com.android.server.$$Lambda$ContextHubSystemService$q5gSEKm3he4vIHcay4DLtf85E */
            private final /* synthetic */ Context f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ContextHubSystemService.this.lambda$new$0$ContextHubSystemService(this.f$1);
            }
        }, "Init ContextHubSystemService");
    }

    public /* synthetic */ void lambda$new$0$ContextHubSystemService(Context context) {
        this.mContextHubService = new ContextHubService(context);
    }

    @Override // com.android.server.SystemService
    public void onStart() {
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.ContextHubSystemService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v4, types: [com.android.server.location.ContextHubService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        if (phase == 500) {
            Log.d(TAG, "onBootPhase: PHASE_SYSTEM_SERVICES_READY");
            ConcurrentUtils.waitForFutureNoInterrupt(this.mInit, "Wait for ContextHubSystemService init");
            this.mInit = null;
            publishBinderService("contexthub", this.mContextHubService);
        }
    }
}

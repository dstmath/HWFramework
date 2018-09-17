package com.android.server;

import android.content.Context;
import android.util.Log;
import com.android.server.location.ContextHubService;

class ContextHubSystemService extends SystemService {
    private static final String TAG = "ContextHubSystemService";
    private final ContextHubService mContextHubService;

    public ContextHubSystemService(Context context) {
        super(context);
        this.mContextHubService = new ContextHubService(context);
    }

    public void onStart() {
    }

    public void onBootPhase(int phase) {
        if (phase == 500) {
            Log.d(TAG, "onBootPhase: PHASE_SYSTEM_SERVICES_READY");
            publishBinderService("contexthub", this.mContextHubService);
        }
    }
}

package com.android.server;

import android.content.Context;
import android.hardware.location.ContextHubService;
import android.util.Log;

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
        if (phase == SystemService.PHASE_SYSTEM_SERVICES_READY) {
            Log.d(TAG, "onBootPhase: PHASE_SYSTEM_SERVICES_READY");
            publishBinderService("contexthub_service", this.mContextHubService);
        }
    }
}
